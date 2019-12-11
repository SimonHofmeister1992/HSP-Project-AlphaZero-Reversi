package de.othr.reversixt.ReversiAlphaGo.general;

import de.othr.reversixt.ReversiAlphaGo.agent.Agent;
import de.othr.reversixt.ReversiAlphaGo.communication.ServerCommunicator;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;

import java.io.IOException;

public class Main
{
    public static boolean QUIET_MODE = Boolean.FALSE;
    private static String ip = "127.0.0.1"; // localhost
    private static int port = 7777;
    private static int groupNumber = 3;

    public static void main( String[] args )
    {
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
        Agent agent = new Agent(environment, serverCommunicator);

        /* *********************************
         *         Connect to server
         */

        serverInit(ip, port, serverCommunicator, environment);

        /* ********************************
         *             Game itself
         */
        int msgType;
        while((msgType=serverCommunicator.waitOnServer()) != IMsgType.END_OF_GAME){
                switch (msgType) {
                    case IMsgType.INITIAL_MAP:
                        environment.parseRawMap(serverCommunicator.getRawMap());
                        break;
                    case IMsgType.PLAYER_ICON:
                        agent.setPlayer(environment.getPlayerByPlayerIcon(serverCommunicator.getPlayerIcon()));
                        break;
                    case IMsgType.ENEMY_TURN:
                        environment.updatePlayground(serverCommunicator.getEnemyTurn());
                        break;
                    case IMsgType.TURN_REQUEST:
                        agent.play();
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

        if(!QUIET_MODE && environment.isPlayerDisqualified(agent.getPlayer().getSymbol())){
            System.err.println("Agent got disqualified");
        }
        if(!QUIET_MODE) System.out.println("Game finished");

    }

    private static void setValuesFromCLI(CLI cli){
        if(cli.hasOption(ICLIOptions.GROUP_NUMBER)) groupNumber =  Integer.parseInt(cli.getOptionValue(ICLIOptions.GROUP_NUMBER));
        if(cli.hasOption(ICLIOptions.QUIET_MODE)) QUIET_MODE = Boolean.TRUE;
        if(cli.hasOption(ICLIOptions.IP_ADDRESS)) ip = cli.getOptionValue(ICLIOptions.IP_ADDRESS);
        if(cli.hasOption(ICLIOptions.PORT)) port = Integer.parseInt(cli.getOptionValue(ICLIOptions.PORT));
    }

    private static void serverInit(String IP, int port, ServerCommunicator serverComm, Environment environment)
    {
        try {serverComm.connect(IP, port);}
        catch(IOException ServerError)
        {
            if(!Main.QUIET_MODE)System.err.println("Server error. Aborting");
            serverComm.cleanup();
        }
        int msgType = serverComm.waitOnServer();
        if (msgType == IMsgType.INITIAL_MAP) //got map from server
        {
            if(!Main.QUIET_MODE)System.out.println("Got Map from Server");
            if(!Main.QUIET_MODE)System.out.println("everything seems good, starting game...");
            environment.parseRawMap(serverComm.getRawMap());
        }
        else //something went wrong!!
        {
            System.err.println("First message was not MAP");
            System.err.println("Aborting!!");
            serverComm.cleanup();
        }
    }
}

