package de.othr.reversixt.ReversiAlphaGo.environment;

public class Player {
    private char symbol;
    private int remainingOverrideStones;
    private int remainingBombs;
    private boolean isDisqualified = Boolean.FALSE;

    public Player(char symbol, int remainingOverrideStones, int remainingBombs) {
        this.symbol = symbol;
        this.remainingOverrideStones = remainingOverrideStones;
        this.remainingBombs = remainingBombs;
    }

    public char getSymbol() {
        return symbol;
    }

    public int getRemainingBombs() {
        return remainingBombs;
    }

    public int getRemainingOverrideStones() {
        return remainingOverrideStones;
    }

    public void increaseNumberOfOverrideStones() {
        remainingOverrideStones++;
    }

    public void decreaseNumberOfOverrideStones() {
        remainingOverrideStones--;
    }

    public void increaseNumberOfBombs() {
        remainingBombs++;
    }

    public void decreaseNumberOfBombs() {
        remainingBombs--;
    }

    public boolean isDisqualified() {
        return this.isDisqualified;
    }

    public void setDisqualify() {
        this.isDisqualified = Boolean.TRUE;
    }
}
