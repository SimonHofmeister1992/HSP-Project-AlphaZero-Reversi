package de.othr.reversixt.ReversiAlphaGo.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;

public class RandomTurnChoiceAlgorithm implements ITurnChoiceAlgorithm {

	Environment environment;
	Player player;
	
	public RandomTurnChoiceAlgorithm(Environment environment, Player player) {
		this.environment=environment;
		this.player=player;
	}
	
	@Override
	public Turn chooseTurnPhase1() {
		Turn turn = null;
		Turn turnToCheck = new Turn(player.getSymbol(), 0, 0, 0);
		List<Turn> validTurns = new ArrayList<>();
		for(int row=0; row < environment.getPlayground().getPlaygroundHeight(); row++) {
			for(int col=0; col < environment.getPlayground().getPlaygroundWidth(); col++) {
//				int internalRow = row+1;
//				int internalCol = col+1;
//				turnToCheck.setRow(internalRow);
//				turnToCheck.setColumn(internalCol);
				turnToCheck.setRow(row);
				turnToCheck.setColumn(col);
				// validateTurn is dealing with 1 indexed positions. the upper left corner of the map needs to be 1/1.
				// get symbol on playground position is 0 indexed.
				if(environment.validateTurnPhase1(turnToCheck)) {
//					turn = new Turn(player.getSymbol(), internalRow, internalCol, 0);
					turn = new Turn(player.getSymbol(), row, col, 0);
					if(environment.getPlayground().getSymbolOnPlaygroundPosition(row, col)=='b') turn.setSpecialFieldInfo(21);
					if(environment.getPlayground().getSymbolOnPlaygroundPosition(row, col)=='c') turn.setSpecialFieldInfo(player.getSymbol()-'0');
					validTurns.add(turn);
				}
			}
		}
		
		Random random = new Random();
		if(validTurns.size() > 0) {
			int index = random.nextInt() % validTurns.size();
			if(index < 0) index *= -1;
			turn = validTurns.get(index);
		}
		else { // no turn found, try to put stone on 0/0, normally disqualifies itself and should never happen
			turn.setPlayerIcon(player.getSymbol());
			turn.setSpecialFieldInfo(0);
			turn.setRow(0);
			turn.setColumn(0);
		}
		return turn;
	}
	
	@Override
	public Turn chooseTurnPhase2() {
		ArrayList<Turn> validTurns = new ArrayList<Turn>();
		Turn turn;
		for(int row = 0; row < environment.getPlayground().getPlaygroundHeight(); row++) {
			for(int col = 0; col < environment.getPlayground().getPlaygroundWidth(); col++) {
				if(environment.getPlayground().getSymbolOnPlaygroundPosition(row, col) != '-') {
					turn = new Turn(player.getSymbol(), row, col, 0);
					validTurns.add(turn);
				}
			}
		}
		Random random = new Random();
		if(validTurns.size() > 0) {
			int index = random.nextInt() % validTurns.size();
			if(index < 0) index *= -1;
			turn = validTurns.get(index);
		}
		else {
			turn = new Turn();
			turn.setPlayerIcon(player.getSymbol());
			turn.setSpecialFieldInfo(0);
			turn.setRow(0);
			turn.setColumn(0);
		}
		return turn;
	}
}
