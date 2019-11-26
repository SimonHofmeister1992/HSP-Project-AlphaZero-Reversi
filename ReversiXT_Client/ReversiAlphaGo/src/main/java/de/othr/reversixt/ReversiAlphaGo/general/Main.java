package de.othr.reversixt.ReversiAlphaGo.general;

/**
 * Hello world!
 *
 */
public class Main 
{
    public static boolean QUIET_MODE = Boolean.FALSE;
    private static String ip = "127.0.0.1"; // localhost
    private static int port = 7777;
    private static int groupNumber = 1;

    public static void main( String[] args )
    {
        /**********************************
         *         Commandline options
         */
        CLI cli = new CLI(args);
        setValuesFromCLI(cli);
    }

    private static void setValuesFromCLI(CLI cli){
        if(cli.hasOption(ICLIOptions.GROUP_NUMBER)) groupNumber =  Integer.parseInt(cli.getOptionValue(ICLIOptions.GROUP_NUMBER));
        if(cli.hasOption(ICLIOptions.QUIET_MODE)) QUIET_MODE = Boolean.TRUE;
        if(cli.hasOption(ICLIOptions.IP_ADDRESS)) ip = cli.getOptionValue(ICLIOptions.IP_ADDRESS);
        if(cli.hasOption(ICLIOptions.PORT)) port = Integer.parseInt(cli.getOptionValue(ICLIOptions.PORT));
    }

}
