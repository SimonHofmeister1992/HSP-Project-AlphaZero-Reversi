package de.othr.reversixt.ReversiAlphaGo.agent;

import de.othr.reversixt.ReversiAlphaGo.environment.Turn;

public interface ITurnChoiceAlgorithm {

	public Turn chooseTurnPhase1();
	public Turn chooseTurnPhase2();
	
}
