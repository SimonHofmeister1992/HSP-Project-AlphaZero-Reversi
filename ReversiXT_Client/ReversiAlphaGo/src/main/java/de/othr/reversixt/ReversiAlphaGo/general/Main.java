package de.othr.reversixt.ReversiAlphaGo.general;

import de.othr.reversixt.ReversiAlphaGo.agent.AgentCallable;
import de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.PolicyValuePredictor;
import de.othr.reversixt.ReversiAlphaGo.communication.ServerCommunicator;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;
import de.othr.reversixt.ReversiAlphaGo.mcts.Node;
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
        if(LEARNER_MODE) groupNumber=4;

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
                    if(!Main.QUIET_MODE) System.out.println("Set ourPlayer in environment: " + serverCommunicator.getPlayerIcon());
                    //initialization of Agent Callable
                    environment.setOurPlayer(serverCommunicator.getPlayerIcon());
                    environment.parseRawMap(serverCommunicator.getRawMap());
                    agentCallable = new AgentCallable(environment);
                    break;
                case IMsgType.ENEMY_TURN:
                    environment.updatePlayground(serverCommunicator.getEnemyTurn(), environment.getPlayground());
                    if(!QUIET_MODE) {
                        System.out.println("Enemy Turn");
                        environment.getPlayground().printPlayground();
                    }
                    agentCallable.getAgent().getITurnChoiceAlgorithm().enemyTurn(serverCommunicator.getEnemyTurn());
                    break;
                case IMsgType.TURN_REQUEST:
                    if(!Main.QUIET_MODE) System.out.println("Turn Request - TotalTime: " + timeToWaitInMilis);
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
        }
        else if(Main.LEARNER_MODE){

            pretrainNetwork(pvp, agentCallable);

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

    private static void pretrainNetwork(PolicyValuePredictor pvp, AgentCallable agentCallable){
        ArrayList<Node> history = agentCallable.getAgent().getITurnChoiceAlgorithm().getTurnHistory();

        Playground[] playgrounds = new Playground[history.size()];
        Player[] players = new Player[history.size()];
        INDArray policyOutputs = Nd4j.create(0,AlphaGoZeroConstants.DIMENSION_PLAYGROUND*AlphaGoZeroConstants.DIMENSION_PLAYGROUND+1);
        INDArray policy;

        INDArray valueOutputs = Nd4j.create(0,1);
        INDArray value;

        int index = 0;
        for(Node node : history){
            playgrounds[index] = node.getPlayground();
            players[index] = node.getNextPlayer();

            policy = Nd4j.createFromArray((Nd4j.createFromArray(node.getPriorsOfNN()).toFloatVector())).reshape(1, AlphaGoZeroConstants.DIMENSION_PLAYGROUND*AlphaGoZeroConstants.DIMENSION_PLAYGROUND+1);
            policyOutputs = Nd4j.concat(0, policyOutputs, policy);

            value = Nd4j.createFromArray(Nd4j.createFromArray(node.getSimulationReward()).toFloatVector()).reshape(1,1);
            valueOutputs = Nd4j.concat(0, valueOutputs, value);
            index++;
        }

        pvp.trainComputationGraph(playgrounds, players, policyOutputs, valueOutputs);
    }

    private static void updateStatisticsAndNeuronalNetworks(Environment environment){
        MultiGameHistory mgh = new MultiGameHistory();

        // update statistics
        if(environment.getRankOfPlayer(environment.getOurPlayer()) == 1){
            mgh.declareGameAsWon();
        }
        else{
            mgh.declareGameAsLost();
        }

        // update neuronalnetwork files
        

        if(mgh.getNumberOfGames() == 0){
            double rateWonGames = mgh.getNumberOfWonGames() / AlphaGoZeroConstants.NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE;

            if(rateWonGames >= AlphaGoZeroConstants.NEEDED_WIN_RATE || AlphaGoZeroConstants.NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE <= 1){
                // first save actual model as best model, before overwriting the actual model
                    if((AlphaGoZeroConstants.NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE <= 1 && environment.getRankOfPlayer(environment.getOurPlayer()) == 1) 
                        || AlphaGoZeroConstants.NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE > 1) {
			                    PolicyValuePredictor.saveAsBestModel();
	    	        }
                PolicyValuePredictor.savePretrainedAsActualModel();
            }
            else {
                PolicyValuePredictor.savePretrainedAsActualModel();
            }

        }
    }
}

