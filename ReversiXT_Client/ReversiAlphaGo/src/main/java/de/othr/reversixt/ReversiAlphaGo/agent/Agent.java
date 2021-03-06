package de.othr.reversixt.ReversiAlphaGo.agent;

import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.IPhase;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;
import de.othr.reversixt.ReversiAlphaGo.general.Main;
import de.othr.reversixt.ReversiAlphaGo.mcts.MCTS;

public class Agent {

    private Environment environment;
    private ITurnChoiceAlgorithm itca;

    public Agent(Environment environment){
        this.environment = environment;
        this.itca = new MCTS(environment);
    }

    public Turn play() {

        Turn turn;

        switch (environment.getPhase()) {
            case IPhase.TURN_PHASE:
                itca.chooseTurnPhase1();
                break;
            case IPhase.BOMB_PHASE:
                itca.chooseTurnPhase2();
                break;
            default:
                break;
        }

        turn = itca.getBestTurn();

        if(!Main.QUIET_MODE) {
            System.out.println("agent set stone to: row: " + turn.getRow() + ", col: " + turn.getColumn() + ", remaining overrides" + environment.getPlayerByPlayerIcon(turn.getPlayerIcon()).getRemainingOverrideStones());
            environment.getPlayground().printPlayground();
        }

        return turn;
    }

    public ITurnChoiceAlgorithm getITurnChoiceAlgorithm() {
        return itca;
    }
}