package de.othr.reversixt.ReversiAlphaGo.environment;

import java.util.ArrayList;
import java.util.HashMap;

public class Playground {
    private int playgroundHeight;
    private int playgroundWidth;
    private int numOfPlayer;
    private char[][] playground;
    private HashMap<TransitionPart, TransitionPart> transitions;

    /**
     * the init functions without transition is for parseRawMap (the transitions will set after initialisation)
     */
    public Playground(int playgroundHeight, int playgroundWidth, int numOfPlayer, HashMap<TransitionPart, TransitionPart> transitions) {
        this.playgroundHeight = playgroundHeight;
        this.playgroundWidth = playgroundWidth;
        this.numOfPlayer = numOfPlayer;
        this.playground = new char[playgroundHeight][playgroundWidth];
        this.transitions = transitions;
    }

    public Playground(int playgroundHeight, int playgroundWidth, int numOfPlayer) {
        this.playgroundHeight = playgroundHeight;
        this.playgroundWidth = playgroundWidth;
        this.numOfPlayer = numOfPlayer;
        this.playground = new char[playgroundHeight][playgroundWidth];
    }

    /**
     * Getter Setter
     */
    public int getPlaygroundHeight() {
        return playgroundHeight;
    }

    public int getPlaygroundWidth() {
        return playgroundWidth;
    }

    public char[][] getPlayground() {
        return playground;
    }
    public void setPlayground(char[][] playground){
        this.playground = playground;
    }

    public char getSymbolOnPlaygroundPosition(int row, int col) {
        return this.playground[row][col];
    }

    void setSymbolOnPlaygroundPosition(int row, int col, char symbol) {
        this.playground[row][col] = symbol;
    }

    public void setTransitions(HashMap<TransitionPart, TransitionPart> transitions) {
        this.transitions = transitions;
    }

    /**
     * Game Rule
     * Update Playground - Makes a full move
     *
     * Input: A valid Turn (playerIcon, row, column, specialfieldInfo: 1-8 for choice stones, 20 on bonus stone to get bomb, 21 on bonus stone to get override)
     * The actual player identified by the playericon
     * The number of players on the map, known by the environment
     * Returns: Nothing, the playground of type char[][] of this class is recolored.
     * Hints: Updates the playground by recoloring using valid turns; can handle all rules of ReversiXT.
     * Updates the overrides and bombs gained by 'b' fields.
     */
    void updatePlaygroundPhase1(Turn turn, Player player) {
        ArrayList<int[]> fieldsToColour = new ArrayList<>(); // row, column: overall fields to colour in turn
        ArrayList<int[]> possibleFieldsToColour = new ArrayList<>(); // row, column: fields to colour in actual direction

        int startRow = turn.getRow();
        int startColumn = turn.getColumn();
        int specialFieldInfo = turn.getSpecialFieldInfo(); //0:normal turn, 1-8 and choice: player to switch, 20 and bonus: get bomb, 21 and bonus: get override
        char playerIcon = turn.getPlayerIcon();

        int[] newPosition;
        int actualRow, actualColumn, actualDirection;
        char actualSymbolOnPlayground;

        int[] fieldToAdd;

        char startSymbol = getSymbolOnPlaygroundPosition(startRow, startColumn);

        fieldsToColour.add(new int[]{startRow, startColumn});

        for (int direction = 0; direction < 8; direction++) {
            possibleFieldsToColour.clear();
            actualDirection = direction;
            actualRow = startRow;
            actualColumn = startColumn;
            newPosition = new int[3];

            // gather fields which shall be recolored by direction
            // (see: Documentations\CheatSheets\kurzSpezifikation.pdf)
            while (true) {
                // parameter newPosition to avoid new int[3] in every loop
                newPosition = getNewPosition(newPosition, actualRow, actualColumn, actualDirection);
                actualRow = newPosition[0];
                actualColumn = newPosition[1];
                actualDirection = newPosition[2];

                // loop through a transition -> break
                if (actualRow == startRow && actualColumn == startColumn) {
                    break;
                }
                // validate pointer position on map
                if (actualRow < 0 || actualColumn < 0 || actualRow >= playgroundHeight || actualColumn >= playgroundWidth) {
                    break;
                }

                actualSymbolOnPlayground = getSymbolOnPlaygroundPosition(actualRow, actualColumn);

                // handle own player symbol
                if (actualSymbolOnPlayground == playerIcon) {
                    fieldsToColour.addAll(possibleFieldsToColour);
                    break;
                }
                // handle special fields which must not be recolored as the whole direction
                else if (actualSymbolOnPlayground == '0'
                        || actualSymbolOnPlayground == '-'
                        || actualSymbolOnPlayground == 'i'
                        || actualSymbolOnPlayground == 'b'
                        || actualSymbolOnPlayground == 'c') {
                    break;
                }
                // handle fields which may be recolored
                else {
                    fieldToAdd = new int[2];
                    fieldToAdd[0] = actualRow;
                    fieldToAdd[1] = actualColumn;
                    possibleFieldsToColour.add(fieldToAdd);
                }
            }
        }

        // recolor the playground
        for (int[] field : fieldsToColour) {
            setSymbolOnPlaygroundPosition(field[0], field[1], playerIcon);
        }

        // Make all special Move Checks
        if (startSymbol == 'b') {
            if (turn.getSpecialFieldInfo() == 20) {
                player.increaseNumberOfBombs();
            }
            // Override stone was chosen
            else if (turn.getSpecialFieldInfo() == 21) {
                player.increaseNumberOfOverrideStones();
            }
        } else if (startSymbol == 'x' || (startSymbol >= '1' && startSymbol <= '8')) {
            player.decreaseNumberOfOverrideStones();
        } else if (startSymbol == 'i') {
            invertStones();
        }

        // choice-stone
        if (specialFieldInfo >= 1 && specialFieldInfo <= 8) {
            choiceStone(playerIcon, specialFieldInfo);
        }

    }

    /**
     * Game Rule
     * Validate Turn (Phase 1) - Check if turn is possible
     */
    public boolean validateTurnPhase1(Turn turn, Player player) {
        int row = turn.getRow();
        int col = turn.getColumn();
        int numOfColoredFields;
        char actualSymbol;

        if (!(row >= 0 && row < this.playgroundHeight
                && col >= 0 && col < this.playgroundWidth)) {
            return false;
        }

        char startSymbol = this.getSymbolOnPlaygroundPosition(row, col);

        if (startSymbol == '-') {
            return false;
        } else if (startSymbol == 'x' && player.getRemainingOverrideStones() > 0) {
            return true;
        } else if ((startSymbol == 'x' || (startSymbol >= '1' && startSymbol <= '8'))
                && player.getRemainingOverrideStones() <= 0) {
            return false;
        } else {
            int[] newPos = new int[3];
            for (int direction = 0; direction < 8; direction++) {
                numOfColoredFields = 0;
                newPos[0] = row;
                newPos[1] = col;
                newPos[2] = direction;
                while (true) {
                    newPos = getNewPosition(newPos, newPos[0], newPos[1], newPos[2]);

                    if (!(newPos[0] >= 0 && newPos[0] < this.playgroundHeight
                            && newPos[1] >= 0 && newPos[1] < this.playgroundWidth)) {
                        break;
                    }
                    actualSymbol = this.getSymbolOnPlaygroundPosition(newPos[0], newPos[1]);
                    if (newPos[0] == row && newPos[1] == col) {
                        break;
                    } else if (actualSymbol == player.getSymbol() && numOfColoredFields > 0) {
                        return true;
                    } else if (actualSymbol == player.getSymbol()
                            || actualSymbol == 'c'
                            || actualSymbol == 'i'
                            || actualSymbol == 'b'
                            || actualSymbol == '0'
                            || actualSymbol == '-') {
                        break;
                    } else {
                        numOfColoredFields++;
                    }
                }

            }
        }
        return false;
    }

    /**
     * Game Rule
     * Validate Turn (Phase 2) - Check if turn is possible
     *
     * This function is needed for bomb phase
     */
    public boolean validateTurnPhase2(Turn turn) {
        return getSymbolOnPlaygroundPosition(turn.getRow(), turn.getColumn()) != '-';
    }

    /**
     * Inversion Stone was used
     *
     * Color all stones one player "up"
     * Example: 0 1 2 3 0 --> 0 2 3 1 0
     */
    private void invertStones() {
        for (int row = 0; row < playgroundHeight; row++) {
            for (int col = 0; col < playgroundWidth; col++) {
                char symbol = getSymbolOnPlaygroundPosition(row, col);
                if (symbol >= '1' && symbol <= '8') {
                    setSymbolOnPlaygroundPosition(row, col, (char) ((((symbol - 49) + 1) % numOfPlayer) + 49));
                }
            }
        }
    }

    /**
     * Choice Stone was used
     *
     * A Player was chosen, which will transform the stones of this player with "our" stones and vice versa
     * Example: 0 1 2 3 0 --> 0 3 2 1 0 (Input: 1, 3)
     */
    private void choiceStone(char playerIcon, int specialFieldInfo) {
        char choicePlayer = (char) (specialFieldInfo + 48);

        for (int row = 0; row < getPlaygroundHeight(); row++) {
            for (int col = 0; col < getPlaygroundWidth(); col++) {
                if (getSymbolOnPlaygroundPosition(row, col) == choicePlayer) {
                    setSymbolOnPlaygroundPosition(row, col, playerIcon);
                } else if (getSymbolOnPlaygroundPosition(row, col) == playerIcon) {
                    setSymbolOnPlaygroundPosition(row, col, choicePlayer);
                }
            }
        }
    }


    /**
     * Get next position on the playground in one direction
     * Function also checks if there is a transition
     */
    private int[] getNewPosition(int[] newPosition, int row, int col, int direction) {
        TransitionPart tp = transitions.get(new TransitionPart(col, row, direction));
        if (tp != null) {
            newPosition[0] = tp.getRow();
            newPosition[1] = tp.getColumn();
            newPosition[2] = (tp.getDirection() + 4) % 8;
            return newPosition;
        } else {
            switch (direction) {
                case 0:
                    row--;
                    break;
                case 1:
                    row--;
                    col++;
                    break;
                case 2:
                    col++;
                    break;
                case 3:
                    row++;
                    col++;
                    break;
                case 4:
                    row++;
                    break;
                case 5:
                    row++;
                    col--;
                    break;
                case 6:
                    col--;
                    break;
                case 7:
                    row--;
                    col--;
                    break;
            }
            newPosition[0] = row;
            newPosition[1] = col;
            newPosition[2] = direction;
            return newPosition;
        }
    }

    /**
     * Get a clone of the complete playground
     * Map cloned complete
     * Transition are the same - these will not be a new instance, because these always the same!
     */
    public Playground getCloneOfPlayground() {
        Playground p = new Playground(this.getPlaygroundHeight(), this.getPlaygroundWidth(), this.numOfPlayer, this.transitions);

        for (int row = 0; row < getPlaygroundHeight(); row++) {
            for (int col = 0; col < getPlaygroundWidth(); col++) {
                p.setSymbolOnPlaygroundPosition(row, col, this.getSymbolOnPlaygroundPosition(row, col));
            }
        }
        return p;
    }

    /* Bomb Phase
    public void updatePlaygroundPhase2(Turn turn, Player player, int strengthOfBombs) {
        int startRow = turn.getRow();
        int startColumn = turn.getColumn();
        player.decreaseNumberOfBombs();
        List<Turn> l = setBomb(startRow, startColumn, strengthOfBombs);

        // color map

        for (Turn t : l) {
            setSymbolOnPlaygroundPosition(t.getRow(), t.getColumn(), '-');
        }
    }

    public List<Turn> setBomb(int row, int col, int strengthOfBombs) {
        List<Turn> l = new ArrayList<>();
        if (strengthOfBombs <= 0) {
            Turn t = new Turn();
            t.setRow(row);
            t.setColumn(col);
            l.add(t);
            return l;
        } else {
            for (int direction = 0; direction < 8; direction++) {
                int r, c;
                int[] newPos = new int[3];
                // calculate new position
                int[] actualPos = getNewPosition(newPos, row, col, direction);
                r = actualPos[0];
                c = actualPos[1];
                // validate new position
                if (r < 0 || r >= getPlaygroundHeight() || c < 0 || c >= getPlaygroundWidth()
                        || getSymbolOnPlaygroundPosition(r, c) == '-') continue;

                // recursive call to get every position
                l.addAll(setBomb(r, c, strengthOfBombs - 1));
            }
        }
        return l;
    }
    */

    /**
     * Print Playground in terminal
     */
    public void printPlayground() {
        for (int row = 0; row < getPlaygroundHeight(); row++) {
            for (int col = 0; col < getPlaygroundWidth(); col++) {
                System.out.print(playground[row][col] + " ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

}
