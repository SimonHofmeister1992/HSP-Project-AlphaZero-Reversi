package de.othr.reversixt.ReversiAlphaGo.environment;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Turn implements Future<Turn> {
    private char playerIcon;
    private int column=-1;
    private int row=-1;
    private int specialFieldInfo; //0:normal turn, 1-8 and choice: player to switch, 20 and bonus: get bomb, 21 and bonus: get override

    public Turn (){
    	setSpecialFieldInfo(0);
    }
    public Turn (char playerIcon, int row, int col, int specialFieldInfo){
        setPlayerIcon(playerIcon);
        setRow(row);
        setColumn(col);
        setSpecialFieldInfo(specialFieldInfo);
    }

    public char getPlayerIcon() {
        return playerIcon;
    }

    public void setPlayerIcon(char playerIcon) {
        this.playerIcon = playerIcon;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getSpecialFieldInfo() {
        return specialFieldInfo;
    }

    public void setSpecialFieldInfo(int specialFieldInfo) {
        this.specialFieldInfo = specialFieldInfo;
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
        if(this.getRow() != -1) return true;
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
