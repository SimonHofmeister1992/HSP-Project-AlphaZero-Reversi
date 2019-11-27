package de.othr.reversixt.ReversiAlphaGo.agent;

import de.othr.reversixt.ReversiAlphaGo.communication.ServerCommunicator;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;

public class Agent {

    private Environment environment;
    private ServerCommunicator serverCommunicator;
    private char playerIcon;

    public Agent (Environment environment, ServerCommunicator serverCommunicator){
        this.environment = environment;
        this.serverCommunicator = serverCommunicator;
    }

    // TODO: THIS IS ONLY A DUMMY!!!
    public void play(){
        serverCommunicator.sendOwnTurn(5,3);
    }

    public char getPlayerIcon() {
        return playerIcon;
    }

    public void setPlayerIcon(char playerIcon) {
        this.playerIcon = playerIcon;
    }
}