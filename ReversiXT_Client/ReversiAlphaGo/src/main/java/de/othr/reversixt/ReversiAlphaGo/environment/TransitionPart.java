package de.othr.reversixt.ReversiAlphaGo.environment;

public class TransitionPart {

    private int row;
    private int column;
    private int direction;

    TransitionPart(int column, int row, int direction) {
        this.row = row;
        this.column = column;
        this.direction = direction;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TransitionPart) {
            TransitionPart other = (TransitionPart) obj;
            if (this.row == other.row && this.column == other.column && this.direction == other.direction)
                return Boolean.TRUE;
            return Boolean.FALSE;
        }
        return Boolean.FALSE;
    }

    @Override
    public int hashCode() {
        return (row + " " + column + " " + direction).hashCode();
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public int getDirection() {
        return direction;
    }
}
