package de.othr.reversixt.ReversiAlphaGo.environment;

public class Transition {

    int fromRow;
    int fromColumn;
    int fromDirection;

    int toRow;
    int toColumn;
    int toDirection;

    public Transition(int fromRow, int fromColumn, int fromDirection,
                      int toRow, int toColumn, int toDirection){
        this.fromRow=fromRow;
        this.fromColumn=fromColumn;
        this.fromDirection=fromDirection;

        this.toRow=toRow;
        this.toColumn=toColumn;
        this.toDirection=toDirection;
    }

}
