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
    private ITurnChoiceAlgorithm itca;

    public Agent(Environment environment, ServerCommunicator serverCommunicator){
        this.environment = environment;
        this.serverCommunicator = serverCommunicator;
    }

    public Turn play() {
    	Turn turn;
        itca = new RandomTurnChoiceAlgorithm(environment, player);
    	switch (environment.getPhase()) {
    	case IPhase.TURN_PHASE: itca.chooseTurnPhase1(); break;
    	case IPhase.BOMB_PHASE: itca.chooseTurnPhase2(); break;
    	default: break;
    	}
    	turn =  itca.getBestTurn();

        if(turn != null) serverCommunicator.sendOwnTurn(turn);
        
        if(!Main.QUIET_MODE && turn!=null) {
        	System.out.println("agent set stone to: row: " + turn.getRow() + ", col: " + turn.getColumn() + ", remaining overrides" + environment.getPlayerByPlayerIcon(turn.getPlayerIcon()).getRemainingOverrideStones());
        	environment.getPlayground().printPlayground();
        }

        return turn;
    }

    public Player getPlayer() {
		return player;
	}
    
    public void setPlayer(Player player) {
		this.player = player;
	}

    public ITurnChoiceAlgorithm getITurnChoiceAlgorithm() {
        return itca;
    }
}