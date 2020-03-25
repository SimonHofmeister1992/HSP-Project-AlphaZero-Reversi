package de.othr.reversixt.ReversiAlphaGo.general;

import de.othr.reversixt.ReversiAlphaGo.agent.AgentCallable;
import de.othr.reversixt.ReversiAlphaGo.agent.ITurnChoiceAlgorithm;
import de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.PlaygroundTransformer;
import de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.PolicyValuePredictor;
import de.othr.reversixt.ReversiAlphaGo.communication.ServerCommunicator;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;
import de.othr.reversixt.ReversiAlphaGo.mcts.Node;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.jita.conf.CudaEnvironment;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.*;

public class Main {
    public static boolean QUIET_MODE = Boolean.FALSE;
    private static String ip = "127.0.0.1"; // localhost
    private static int port = 7777;
    private static int groupNumber = 3;
    public static boolean LEARNER_MODE = Boolean.FALSE;

    public static void main(String[] args) throws InterruptedException {

        /* *******************************
         *       Enable training on multi-gpu
         */

        CudaEnvironment.getInstance().getConfiguration().allowMultiGPU(true);

        /* ********************************
         *         Commandline options
         */
        CLI cli = new CLI(args);
        setValuesFromCLI(cli);
        if (LEARNER_MODE) groupNumber = 4;

        /* ********************************
         *     Create instances for game
         */

        ServerCommunicator serverCommunicator = new ServerCommunicator(groupNumber);
        Environment environment = new Environment();

        // Initialize singleton neuronal net before connecting to server (time intensive)
        PolicyValuePredictor pvp = PolicyValuePredictor.getInstance();

        /* *********************************
         *         Connect to server
         */

        serverInit(ip, port, serverCommunicator, environment);
        AgentCallable agentCallable = null;

        /* ********************************
         *             Game itself
         */
        int msgType;
        Turn bestTurn;
        long timeToWaitInMilis;
        while ((msgType = serverCommunicator.waitOnServer()) != IMsgType.END_OF_GAME) {

            timeToWaitInMilis = serverCommunicator.getTimeLimit() - 500;

            switch (msgType) {
                case IMsgType.PLAYER_ICON:
                    if (!Main.QUIET_MODE)
                        System.out.println("Set ourPlayer in environment: " + serverCommunicator.getPlayerIcon());
                    //initialization of Agent Callable
                    environment.setOurPlayer(serverCommunicator.getPlayerIcon());
                    environment.parseRawMap(serverCommunicator.getRawMap());
                    agentCallable = new AgentCallable(environment);
                    break;
                case IMsgType.ENEMY_TURN:
                    environment.updatePlayground(serverCommunicator.getEnemyTurn(), environment.getPlayground());
                    if (!QUIET_MODE) {
                        System.out.println("Enemy Turn");
                        environment.getPlayground().printPlayground();
                    }
                    agentCallable.getAgent().getITurnChoiceAlgorithm().enemyTurn(serverCommunicator.getEnemyTurn());
                    break;
                case IMsgType.TURN_REQUEST:
                    if (!Main.QUIET_MODE) System.out.println("Turn Request - TotalTime: " + timeToWaitInMilis);
                    ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1);
                    Future<Turn> futureTurn = executorService.submit(agentCallable);
                    //
                    //the get methods waits until one of two events occur
                    // 1) the time runes out (timeToWaitInMilis) -> TimeoutException -> ShutdownNow() and getBestTurn()
                    // 2) the thread is finished and the .get return a turn -> no ShutdownNow() is needed
                    try {
                        bestTurn = futureTurn.get(timeToWaitInMilis, TimeUnit.MILLISECONDS);
                    } catch (ExecutionException | TimeoutException e) {
                        executorService.shutdownNow();
                        bestTurn = agentCallable.getAgent().getITurnChoiceAlgorithm().getBestTurn();
                    }
                    //
                    serverCommunicator.sendOwnTurn(bestTurn);
                    break;
                case IMsgType.DISQUALIFIED_PLAYER:
                    environment.disqualifyPlayer(serverCommunicator.getDisqualifiedPlayer());
                    break;
                case IMsgType.END_OF_FIRST_PHASE:
                    environment.nextPhase();
                    break;
                default:
                    break;
            }
        }

        /* ********************************
         *             END OF GAME
         */

        if (!QUIET_MODE && environment.isPlayerDisqualified(serverCommunicator.getPlayerIcon())) {
            System.err.println("Agent got disqualified");
        } else if (Main.LEARNER_MODE) {

            trainNetwork(pvp, agentCallable);

            updateStatisticsAndNeuronalNetworks(environment);

        }


        if (!QUIET_MODE) {
            System.out.println("Game finished");
        }
        //executorService.shutdownNow();
    }

    private static void setValuesFromCLI(CLI cli) {
        if (cli.hasOption(ICLIOptions.GROUP_NUMBER))
            groupNumber = Integer.parseInt(cli.getOptionValue(ICLIOptions.GROUP_NUMBER));
        if (cli.hasOption(ICLIOptions.QUIET_MODE)) QUIET_MODE = Boolean.TRUE;
        if (cli.hasOption(ICLIOptions.IP_ADDRESS)) ip = cli.getOptionValue(ICLIOptions.IP_ADDRESS);
        if (cli.hasOption(ICLIOptions.PORT)) port = Integer.parseInt(cli.getOptionValue(ICLIOptions.PORT));
        if (cli.hasOption(ICLIOptions.LEARNER_MODE)) LEARNER_MODE = Boolean.TRUE;
    }

    private static void serverInit(String IP, int port, ServerCommunicator serverComm, Environment environment) {
        try {
            serverComm.connect(IP, port);
        } catch (IOException ServerError) {
            if (!Main.QUIET_MODE) System.err.println("Server error. Aborting");
            serverComm.cleanup();
        }
        int msgType = serverComm.waitOnServer();
        if (msgType == IMsgType.INITIAL_MAP) //got map from server
        {
            if (!Main.QUIET_MODE) {
                System.out.println("Got Map from Server");
                System.out.println("everything seems good, starting game...");
            }
            environment.parseRawMap(serverComm.getRawMap());
        } else //something went wrong!!
        {
            System.err.println("First message was not MAP");
            System.err.println("Aborting!!");
            serverComm.cleanup();
        }
    }

    /**
     * prepares the data for the training process of the NN, called after each completed game
     * needed are the improved values for the output of the NN (value and policy)
     * considered are all actually played moves (saved in turnHistory)
     * for each node the improved move probabilities are calculated which are needed to update NN priors
     * as only moves that have been made are children of the corresponding node all other positions are 0
     * for the last node the game outcome is retrieved (see rewardGame()) which is needed to update NN value
     * finally calls the actual training method
     * @param pvp
     * @param agentCallable
     */
    private static void trainNetwork(PolicyValuePredictor pvp, AgentCallable agentCallable) {
        ITurnChoiceAlgorithm algorithm = agentCallable.getAgent().getITurnChoiceAlgorithm();
        ArrayList<Node> history = algorithm.getTurnHistory();

        Playground[] playgrounds = new Playground[history.size()];
        Player[] players = new Player[history.size()];
        INDArray policyOutputs = Nd4j.create(0, AlphaGoZeroConstants.DIMENSION_PLAYGROUND * AlphaGoZeroConstants.DIMENSION_PLAYGROUND + 1);
        INDArray policy;

        INDArray valueOutputs = Nd4j.create(0, 1);
        INDArray value;
        Node node;
        
        for (int i = 0; i < history.size(); i++) {
            node = history.get(i);
            playgrounds[i] = node.getPlayground();
            players[i] = node.getNextPlayer();

            policy = Nd4j.zeros(AlphaGoZeroConstants.DIMENSION_PLAYGROUND * AlphaGoZeroConstants.DIMENSION_PLAYGROUND + 1).reshape(1, AlphaGoZeroConstants.DIMENSION_PLAYGROUND * AlphaGoZeroConstants.DIMENSION_PLAYGROUND + 1);
            int pos;
            double moveProbability;
            for (Node child : node.getChildren()) {
                pos = child.getCurTurn().getRow() + child.getCurTurn().getColumn() * AlphaGoZeroConstants.DIMENSION_PLAYGROUND;
                moveProbability = ((double) child.getNumVisited()) / node.getNumVisited();
                policy.putScalar(pos, moveProbability);
            }
            policyOutputs = Nd4j.concat(0, policyOutputs, policy);

            // last node, terminal state
            if (i == history.size() - 1) {
                for(int posInHistory = 0; posInHistory < history.size(); posInHistory++){
                    value = Nd4j.createFromArray(Nd4j.createFromArray(algorithm.rewardGame(node.getPlayground()))
                            .toFloatVector()).reshape(1, 1);
                    valueOutputs = Nd4j.concat(0, valueOutputs, value);
                }
            }
        }

        // checks if training the computationGraph works by using one dataset
        if(!Main.QUIET_MODE){
            ComputationGraph computationGraph = pvp.getComputationGraph();

            PlaygroundTransformer playgroundTransformer = new PlaygroundTransformer();
            INDArray transformedPlayground = playgroundTransformer.transform(playgrounds[0], players[0]);

            INDArray[] beforeOutputs = computationGraph.output(false, transformedPlayground);

            pvp.trainComputationGraph(playgrounds, players, policyOutputs, valueOutputs);

            INDArray[] afterOutputs = computationGraph.output(false, transformedPlayground);

            System.out.println("policy before train: " + beforeOutputs[0].toStringFull());
            System.out.println("value before train: " + beforeOutputs[1].toStringFull());

            System.out.println("policy after train: " + afterOutputs[0].toStringFull());
            System.out.println("value after train: " + afterOutputs[1].toStringFull());

            System.out.println("policy changed?: " + beforeOutputs[0].toStringFull().equals(afterOutputs[0].toStringFull()));
            System.out.println("values changed?: " + beforeOutputs[1].toStringFull().equals(afterOutputs[1].toStringFull()));
        }
        pvp.trainComputationGraph(playgrounds, players, policyOutputs, valueOutputs);
    }

    private static void updateStatisticsAndNeuronalNetworks(Environment environment) {
        MultiGameHistory mgh = new MultiGameHistory();

        // update statistics
        if (environment.getRankOfPlayer(environment.getOurPlayer()) == 1) {
            mgh.declareGameAsWon();
        } else {
            mgh.declareGameAsLost();
        }

        // update neuronalnetwork files


        if (mgh.getNumberOfGames() == 0) {
            double rateWonGames = mgh.getNumberOfWonGames() / AlphaGoZeroConstants.NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE;

            if (rateWonGames >= AlphaGoZeroConstants.NEEDED_WIN_RATE || AlphaGoZeroConstants.NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE <= 1) {
                // first save actual model as best model, before overwriting the actual model
                if ((AlphaGoZeroConstants.NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE <= 1 && environment.getRankOfPlayer(environment.getOurPlayer()) == 1)
                        || AlphaGoZeroConstants.NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE > 1) {
                    PolicyValuePredictor.saveAsBestModel();
                }
                PolicyValuePredictor.savePretrainedAsActualModel();
            } else {
                PolicyValuePredictor.savePretrainedAsActualModel();
            }

        }
    }
}

