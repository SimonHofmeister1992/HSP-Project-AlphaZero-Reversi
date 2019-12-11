package de.othr.reversixt.ReversiAlphaGo.agent;

import de.othr.reversixt.ReversiAlphaGo.communication.ServerCommunicator;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.IPhase;
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
    	Turn turn;
    	ITurnChoiceAlgorithm itca = new RandomTurnChoiceAlgorithm(environment, player);

    	switch (environment.getPhase()) {
    	case IPhase.TURN_PHASE: turn = itca.chooseTurnPhase1(); break;
    	case IPhase.BOMB_PHASE: turn = itca.chooseTurnPhase2(); break;
    	default: turn = new Turn(player.getSymbol(), 0, 0, 0);
    	}
        turn.setRow(turn.getRow());
        turn.setColumn(turn.getColumn());
        
        serverCommunicator.sendOwnTurn(turn);
        
        if(!Main.QUIET_MODE) {
        	System.out.println("agent set stone to: row: " + turn.getRow() + ", col: " + turn.getColumn() + ", remaining overrides" + environment.getPlayerByPlayerIcon(turn.getPlayerIcon()).getRemainingOverrideStones());
        	environment.getPlayground().printPlayground();
        }
    }

    public Player getPlayer() {
		return player;
	}
    
    public void setPlayer(Player player) {
		this.player = player;
	}
}