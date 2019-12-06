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

    // choose best turn and send turn to server
    // all actions the agent does on the map are 1 indexed. 
    // so the field in the upper left corner is (row:1,col:1)!
    public void play(){
    	
    	ITurnChoiceAlgorithm itca = new RandomTurnChoiceAlgorithm(environment, player);
    	
        Turn turn = itca.chooseTurn();
        // here: 1 indexed turns, on algorithm change to 0 indexed: add 1
        turn.setRow(turn.getRow());
        turn.setColumn(turn.getColumn());
        
        serverCommunicator.sendOwnTurn(turn);
        System.out.println("set to: " + turn.getRow() + " " + turn.getColumn() + ", " + environment.getPlayerByPlayerIcon(turn.getPlayerIcon()).getRemainingOverrideStones());
        if(!Main.QUIET_MODE) environment.getPlayground().printPlayground();
    }

    public Player getPlayer() {
		return player;
	}
    
    public void setPlayer(Player player) {
		this.player = player;
	}
}