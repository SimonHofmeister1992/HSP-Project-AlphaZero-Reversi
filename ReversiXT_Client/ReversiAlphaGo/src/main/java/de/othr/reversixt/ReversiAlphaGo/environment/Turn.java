package de.othr.reversixt.ReversiAlphaGo.environment;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Turn implements Future<Turn> {
    private char playerIcon;
    private int column = -1;
    private int row = -1;
    private int specialFieldInfo; //0:normal turn, 1-8 and choice: player to switch, 20 and bonus: get bomb, 21 and bonus: get override
    private double prior;

    public Turn(char playerIcon, int row, int col, int specialFieldInfo) {
        this.playerIcon = playerIcon;
        this.row = row;
        this.column = col;
        this.specialFieldInfo = specialFieldInfo;
        this.prior = 0.0;
    }

    public Turn(char playerIcon, int row, int col, int specialFieldInfo, double prior) {
        this.playerIcon = playerIcon;
        this.row = row;
        this.column = col;
        this.specialFieldInfo = specialFieldInfo;
        this.prior = prior;
    }


    // Setter not used => After a Turn is initialized no changes are possible

    /**
     * Getter
     */
    public char getPlayerIcon() {
        return playerIcon;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public int getSpecialFieldInfo() {
        return specialFieldInfo;
    }

    public double getPrior() {
        return prior;
    }

    public void setPrior(double prior) {
        this.prior = prior;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        if (this.getRow() != -1) return true;
        else return false;
    }

    @Override
    public Turn get() throws InterruptedException, ExecutionException {
        return this;
    }

    @Override
    public Turn get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this;
    }

}
