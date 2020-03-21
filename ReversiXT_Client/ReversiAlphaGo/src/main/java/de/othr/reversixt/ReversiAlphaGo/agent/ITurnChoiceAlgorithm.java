package de.othr.reversixt.ReversiAlphaGo.agent;

import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;
import de.othr.reversixt.ReversiAlphaGo.mcts.Node;

import java.util.ArrayList;

public interface ITurnChoiceAlgorithm {

    Turn getBestTurn();

    void chooseTurnPhase1();

    void chooseTurnPhase2();

    void enemyTurn(Turn turn);

    ArrayList<Node> getTurnHistory();

    int rewardGame(Playground playground);
}
