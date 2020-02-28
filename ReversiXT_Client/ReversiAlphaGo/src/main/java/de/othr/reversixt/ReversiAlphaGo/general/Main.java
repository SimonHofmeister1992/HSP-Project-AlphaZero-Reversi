package de.othr.reversixt.ReversiAlphaGo.general;

import de.othr.reversixt.ReversiAlphaGo.agent.AgentCallable;
import de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.PolicyValuePredictor;
import de.othr.reversixt.ReversiAlphaGo.communication.ServerCommunicator;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;
import org.nd4j.jita.conf.CudaEnvironment;

import java.io.IOException;
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

        /* ********************************
         *     Create instances for game
         */

        ServerCommunicator serverCommunicator = new ServerCommunicator(groupNumber);
        Environment environment = new Environment();
        AgentCallable agentCallable;

        // Initialize singleton neuronal net before connecting to server (time intensive)
        PolicyValuePredictor.getInstance();

        /* *********************************
         *         Connect to server
         */

        serverInit(ip, port, serverCommunicator, environment);

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
                    System.out.println("Set ourPlayer in environment: " + serverCommunicator.getPlayerIcon());
                    environment.setOurPlayer(serverCommunicator.getPlayerIcon());
                    break;
                case IMsgType.ENEMY_TURN:
                    environment.updatePlayground(serverCommunicator.getEnemyTurn(), environment.getPlayground());
                    break;
                case IMsgType.TURN_REQUEST:
                    System.out.println("Turn Request - TotalTime: " + timeToWaitInMilis);
                    agentCallable = new AgentCallable(environment);
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
}

