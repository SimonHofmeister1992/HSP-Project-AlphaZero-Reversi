package de.othr.reversixt.ReversiAlphaGo.general;

import de.othr.reversixt.ReversiAlphaGo.agent.AgentCallable;
import de.othr.reversixt.ReversiAlphaGo.communication.ServerCommunicator;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;

import java.io.IOException;
import java.util.concurrent.*;

public class Main {
    public static boolean QUIET_MODE = Boolean.FALSE;
    private static String ip = "127.0.0.1"; // localhost
    private static int port = 7777;
    private static int groupNumber = 3;

    public static void main(String[] args) {

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

        ScheduledThreadPoolExecutor executorService = new ScheduledThreadPoolExecutor(1);
        executorService.setRemoveOnCancelPolicy(true);

        /* *********************************
         *         Connect to server
         */

        serverInit(ip, port, serverCommunicator, environment);

        /* ********************************
         *             Game itself
         */
        int msgType;
        Turn bestTurn;
        boolean isDisqualified = false;
        long timeToWaitInMilis;
        while ((msgType = serverCommunicator.waitOnServer()) != IMsgType.END_OF_GAME && !isDisqualified) {

            timeToWaitInMilis = serverCommunicator.getTimeLimit() - 500;

            bestTurn = null;
            switch (msgType) {
                case IMsgType.PLAYER_ICON:
                    System.out.println("Set ourPlayer in environment");
                    environment.setOurPlayer(serverCommunicator.getPlayerIcon());
                    break;
                case IMsgType.ENEMY_TURN:
                    environment.updatePlayground(serverCommunicator.getEnemyTurn(), environment.getPlayground());
                    break;
                case IMsgType.TURN_REQUEST:
                    System.out.println("Turn Request - TotalTime: " + timeToWaitInMilis);
                    agentCallable = new AgentCallable(environment);
                    Future<?> futureTurn = executorService.submit(agentCallable);
                    try {
                        executorService.schedule(() -> {
                            try {
                                if (futureTurn.get() == null) futureTurn.cancel(true);
                            } catch (InterruptedException | ExecutionException e) {
                                futureTurn.cancel(true);
                                executorService.purge();
                            }
                        }, timeToWaitInMilis, TimeUnit.MILLISECONDS);
                        bestTurn = (Turn) futureTurn.get(timeToWaitInMilis, TimeUnit.MILLISECONDS);

                    } catch (InterruptedException | TimeoutException | ExecutionException e) {
                        System.out.println("TurnRequest Exception: : " + e.toString());
                        if (agentCallable.getAgent() != null && agentCallable.getAgent().getITurnChoiceAlgorithm() != null) {
                            bestTurn = agentCallable.getAgent().getITurnChoiceAlgorithm().getBestTurn();
                            futureTurn.cancel(true);
                            executorService.purge();
                        }
                        futureTurn.cancel(true);
                    }

                    if (bestTurn == null) {
                        bestTurn = new Turn(environment.getPlayerByPlayerIcon(serverCommunicator.getPlayerIcon()).getSymbol(), 0, 0, 0);
                        isDisqualified = true;
                    }
                    futureTurn.cancel(true);
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
        if (!QUIET_MODE) System.out.println("Game finished");
        executorService.shutdownNow();
    }

    private static void setValuesFromCLI(CLI cli) {
        if (cli.hasOption(ICLIOptions.GROUP_NUMBER))
            groupNumber = Integer.parseInt(cli.getOptionValue(ICLIOptions.GROUP_NUMBER));
        if (cli.hasOption(ICLIOptions.QUIET_MODE)) QUIET_MODE = Boolean.TRUE;
        if (cli.hasOption(ICLIOptions.IP_ADDRESS)) ip = cli.getOptionValue(ICLIOptions.IP_ADDRESS);
        if (cli.hasOption(ICLIOptions.PORT)) port = Integer.parseInt(cli.getOptionValue(ICLIOptions.PORT));
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

