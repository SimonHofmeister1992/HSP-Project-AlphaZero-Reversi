package de.othr.reversixt.ReversiAlphaGo.agent;

import de.othr.reversixt.ReversiAlphaGo.communication.ServerCommunicator;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;
import de.othr.reversixt.ReversiAlphaGo.general.Main;

public class Agent {

    private Environment environment;
    private ServerCommunicator serverCommunicator;
    private Player player;

    public Agent (Environment environment, ServerCommunicator serverCommunicator){
        this.environment = environment;
        this.serverCommunicator = serverCommunicator;
    }

    // TODO: THIS IS ONLY A DUMMY!!!
    // all actions the agent does on the map are 1 indexed. so the field in the upper left corner is (row:1,col:1)!
    public void play(){
    	int row, col;
    	char choice=0;
    	switch(environment.getTurn()) {
	    	case 1: row=3; col=5; choice=0; break;
	    	case 4: row=3; col=2;break;
	    	default: row=1; col=1;choice='0';
    	}
        Turn turn = new Turn(this.player.getSymbol(), row, col, choice);
        boolean isTurnValid = environment.validateTurn(turn);
        System.out.println("is turn row: " + row + ", col: " + col + " valid?: " + isTurnValid);
        serverCommunicator.sendOwnTurn(turn);
        environment.updatePlayground(turn);
        if(!Main.QUIET_MODE) environment.getPlayground().printPlayground();
    }

    public Player getPlayer() {
		return player;
	}
    
    public void setPlayer(Player player) {
		this.player = player;
	}
}