package de.othr.reversixt.ReversiAlphaGo.general;

import org.apache.commons.cli.*;

class CLI {

    private Options options;
    private CommandLine cmd;

    CLI(String[] args) {
        defineOptions();
        parseCommandLine(args);
    }

    private void defineOptions() {
        this.options = new Options();
        options.addOption("g", ICLIOptions.GROUP_NUMBER, true, "the group number the ai will use to identify itself");
        options.addOption("q", ICLIOptions.QUIET_MODE, false, "suppresses output of the application to a minimum");
        options.addOption("a", ICLIOptions.IP_ADDRESS, true, "ip address of server");
        options.addOption("p", ICLIOptions.PORT, true, "port of server");
        options.addOption("l", ICLIOptions.LEARNER_MODE, false, "defines that the agent learns");
        options.addOption("m", ICLIOptions.ONLY_MCTS, false, "actives the only mcts mode (without NN)");
    }

    private void parseCommandLine(String[] args) {
        CommandLineParser clp = new DefaultParser();
        try{
            cmd =  clp.parse(options, args);
        }
        catch(ParseException e) {
            cmd = null;
            System.err.println("Commandline arguments could not be parsed correctly");
        }
    }

    boolean hasOption(String option){
        return cmd.hasOption(option);
    }
    String getOptionValue(String option){
        return cmd.getOptionValue(option);
    }

}

