package de.othr.reversixt.ReversiAlphaGo.environment;

import java.util.HashMap;

public class Playground {
    private int playgroundHeight;
    private int playgroundWidth;
    private char[][] playground;
    private HashMap<TransitionPart,TransitionPart> transitions;

    public Playground(int numOfPlayers){
        transitions=new HashMap<>();
    }

    public HashMap<TransitionPart,TransitionPart> getTransitions() {
        return transitions;
    }
    public TransitionPart getTransitionedPosition (TransitionPart origin){
        return transitions.get(origin);
    }

    public void addTransition(TransitionPart firstTransitionPart, TransitionPart secondTransitionPart){
        this.transitions.put(firstTransitionPart,secondTransitionPart);
        this.transitions.put(secondTransitionPart,firstTransitionPart);
    }

    public int getPlaygroundHeight() {
        return playgroundHeight;
    }

    public void setPlaygroundHeight(int playgroundHeight) {
        this.playgroundHeight = playgroundHeight;
    }

    public int getPlaygroundWidth() {
        return playgroundWidth;
    }

    public void setPlaygroundWidth(int playgroundWidth) {
        this.playgroundWidth = playgroundWidth;
    }

    public char[][] getPlayground() {
        return playground;
    }
    public void initPlayground(int playgroundHeight, int playgroundWidth){
        this.playground = new char[playgroundHeight][playgroundWidth];
    }

    public void setSymbolOnPlaygroundPosition(int row, int col, char symbol){
        this.playground[row][col]=symbol;
    }
    public char getSymbolOnPlaygroundPosition(int row, int col){
        return this.playground[row][col];
    }

    public void updatePlayground(int[] enemyTurn) {

        //TODO: update function

    }
}
