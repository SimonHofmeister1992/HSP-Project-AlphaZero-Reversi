package de.othr.reversixt.ReversiAlphaGo.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;

public class RandomTurnChoiceAlgorithm implements ITurnChoiceAlgorithm {

	private  Environment environment;
	private Player player;
	private Turn bestTurn;
	private Random random;

	public RandomTurnChoiceAlgorithm(Environment environment, Player player) {
		this.environment=environment;
		this.player=player;
	}

	@Override
	public void chooseTurnPhase1() {
		bestTurn = null;
		Turn turn;
		Turn turnToCheck = new Turn(player.getSymbol(), 0, 0, 0);
		List<Turn> validTurns = new ArrayList<>();
		for(int row=0; row < environment.getPlayground().getPlaygroundHeight(); row++) {
			for(int col=0; col < environment.getPlayground().getPlaygroundWidth(); col++) {
				turnToCheck.setRow(row);
				turnToCheck.setColumn(col);

				if(environment.validateTurnPhase1(turnToCheck)) {
					turn = new Turn(player.getSymbol(), row, col, 0);
					if(environment.getPlayground().getSymbolOnPlaygroundPosition(row, col)=='b') turn.setSpecialFieldInfo(21);
					if(environment.getPlayground().getSymbolOnPlaygroundPosition(row, col)=='c') turn.setSpecialFieldInfo(player.getSymbol()-'0');
					validTurns.add(turn);
					setBestTurn(turn);
				}
			}
		}

		random = new Random();
		if(validTurns.size() > 0) {
			int index = random.nextInt() % validTurns.size();
			if(index < 0) index *= -1;
			turn = validTurns.get(index);
			setBestTurn(turn);
		}
		else { // no turn found, try to put stone on 0/0, normally disqualifies itself and should never happen
			turn = new Turn();
			turn.setPlayerIcon(player.getSymbol());
			turn.setSpecialFieldInfo(0);
			turn.setRow(0);
			turn.setColumn(0);
			setBestTurn(bestTurn);
		}
	}

	@Override
	public void chooseTurnPhase2() {
		ArrayList<Turn> validTurns = new ArrayList<>();
		Turn turn;
		for(int row = 0; row < environment.getPlayground().getPlaygroundHeight(); row++) {
			for(int col = 0; col < environment.getPlayground().getPlaygroundWidth(); col++) {
				if(environment.getPlayground().getSymbolOnPlaygroundPosition(row, col) != '-') {
					turn = new Turn(player.getSymbol(), row, col, 0);
					validTurns.add(turn);
				}
			}
		}
		random = new Random();
		if(validTurns.size() > 0) {
			int index = random.nextInt() % validTurns.size();
			if(index < 0) index *= -1;
			turn = validTurns.get(index);
			setBestTurn(turn);
		}
		else {
			turn = new Turn();
			turn.setPlayerIcon(player.getSymbol());
			turn.setSpecialFieldInfo(0);
			turn.setRow(0);
			turn.setColumn(0);
			setBestTurn(turn);
		}
		setBestTurn(turn);
	}

	@Override
	public Turn getBestTurn() {
		return bestTurn;
	}

	private void setBestTurn(Turn bestTurn) {
		this.bestTurn = bestTurn;
	}
}
