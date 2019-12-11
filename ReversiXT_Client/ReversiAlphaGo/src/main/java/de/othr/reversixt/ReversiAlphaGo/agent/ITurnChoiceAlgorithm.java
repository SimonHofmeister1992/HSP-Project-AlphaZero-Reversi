package de.othr.reversixt.ReversiAlphaGo.agent;

		import de.othr.reversixt.ReversiAlphaGo.environment.Turn;

public interface ITurnChoiceAlgorithm {

	Turn getBestTurn();
	void chooseTurnPhase1();
	void chooseTurnPhase2();

}
