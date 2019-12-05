package de.othr.reversixt.ReversiAlphaGo.environment;

import java.util.ArrayList;
import java.util.HashMap;

public class Playground {
    private int playgroundHeight;
    private int playgroundWidth;
    private char[][] playground;
    private HashMap<TransitionPart,TransitionPart> transitions;

    public Playground(){
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

    /** 
     * Input: A valid Turn (playerIcon, row, column, specialfieldInfo: 1-8 for choice stones, 20 on bonus stone to get bomb, 21 on bonus stone to get override)
     * 		  The actual player identified by the playericon
     * 		  The number of players on the map, known by the environment
     * Returns: Nothing, the playground of type char[][] of this class is recolored.
     * Hints: Updates the playground by recoloring using valid turns; can handle all rules of ReversiXT.
     * 		  Updates the overrides and bombs gained by 'b' fields.
    */
    public void updatePlaygroundPhase1(Turn turn, Player player, int numOfPlayers) {

        ArrayList<int[]> fieldsToColour = new ArrayList<>(); // row, column: overall fields to colour in turn
        ArrayList<int[]> possibleFieldsToColour = new ArrayList<>(); // row, column: fields to colour in actual direction

        int startRow = turn.getRow()-1;
        int startColumn = turn.getColumn()-1;
        int specialFieldInfo = turn.getSpecialFieldInfo(); //0:normal turn, 1-8 and choice: player to switch, 20 and bonus: get bomb, 21 and bonus: get override
        char playerIcon = turn.getPlayerIcon();

        int[] newPosition;
        int actualRow, actualColumn, actualDirection;
        char actualSymbolOnPlayground;


        int[] fieldToAdd;
        // check stones to colour in all directions
        char startSymbol = getSymbolOnPlaygroundPosition(startRow, startColumn);

        if(turn.getSpecialFieldInfo()==20 && startSymbol == 'b') player.increaseNumberOfBombs();
        else if(turn.getSpecialFieldInfo()==21 && startSymbol == 'b') player.increaseNumberOfOverrideStones();
        if(startSymbol=='x' || (startSymbol>='1' && startSymbol<='8')){
            player.decreaseNumberOfOverrideStones();
        }

        fieldsToColour.add(new int[]{startRow,startColumn});

        for(int direction=0; direction < 8; direction++){
            possibleFieldsToColour.clear();
            actualDirection=direction;
            actualRow=startRow;
            actualColumn=startColumn;
            newPosition = new int[3];

            // gather fields which shall be recolored by direction 
            // (see: Documentations\CheatSheets\kurzSpezifikation.pdf)
            while(true){
                newPosition = getNewPosition(newPosition, actualRow, actualColumn, actualDirection); // row, col, direction
                if(newPosition==null || (newPosition[0]==startRow && newPosition[1]==startColumn)) break;
                else{
                    actualRow=newPosition[0];
                    actualColumn=newPosition[1];
                    actualDirection=newPosition[2];
                    
                    // validate pointer position on map
                    if(actualRow < 0 || actualColumn < 0 || actualRow >= playgroundHeight || actualColumn >=playgroundWidth) break;
                    actualSymbolOnPlayground = getSymbolOnPlaygroundPosition(actualRow,actualColumn);
                    
                    // handle own player symbol
                    if(actualSymbolOnPlayground==playerIcon) {
                        fieldsToColour.addAll(possibleFieldsToColour);
                        break;
                    }
                    // handle special fields which must not be recolored as the whole direction
                    else if(actualSymbolOnPlayground=='0'
                    		|| actualSymbolOnPlayground=='-'
                            || actualSymbolOnPlayground=='x'
                            || actualSymbolOnPlayground=='i'
                            || actualSymbolOnPlayground=='b'
                            || actualSymbolOnPlayground=='c'){
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
        }
        
        // recolor the playground
        for (int[] field : fieldsToColour){
            setSymbolOnPlaygroundPosition(field[0], field[1], playerIcon);
        }

     // choice-stone
        if(specialFieldInfo>=1 && specialFieldInfo <=8){ 
            for(int row=0; row < getPlaygroundHeight(); row++){
                for(int col=0; col < getPlaygroundWidth(); col++){
                    if(getSymbolOnPlaygroundPosition(row, col) == (char)(specialFieldInfo+48)){
                        setSymbolOnPlaygroundPosition(row, col, playerIcon);
                    }
                    else  if(getSymbolOnPlaygroundPosition(row, col)==playerIcon){
                        setSymbolOnPlaygroundPosition(row, col, (char)(specialFieldInfo+48));
                    }
                }
            }
        }
     // inversion-stone
        if(startSymbol=='i'){
            for(int row=0; row < getPlaygroundHeight(); row++){
                for(int col=0; col < getPlaygroundWidth(); col++){
                    char symbol = getSymbolOnPlaygroundPosition(row, col);
                    if(symbol>='1' && symbol <='8'){
                        setSymbolOnPlaygroundPosition(row, col, (char)((((symbol-49)+1)%numOfPlayers)+49));
                    }
                }
            }
        }


    }

    protected int[] getNewPosition(int[] newPosition, int row, int col, int direction) {
        TransitionPart tp = getTransitionedPosition(new TransitionPart(col, row, direction));
        if (tp != null) {
            newPosition[0] = tp.getRow();
            newPosition[1] = tp.getColumn();
            newPosition[2] = (tp.getDirection()+4)%8;
            return newPosition;
        } else {
            switch (direction) {
                case 0: row--; break;
                case 1: row--; col++; break;
                case 2: col++; break;
                case 3: row++; col++; break;
                case 4: row++; break;
                case 5: row++; col--; break;
                case 6: col--; break;
                case 7: row--; col--; break;
                default: newPosition=null;
            }
            newPosition[0]=row;
            newPosition[1]=col;
            newPosition[2]=direction;
            return newPosition;
        }
    }

    public Playground getCloneOfPlayground(){
        Playground p = new Playground();
        p.setPlaygroundHeight(this.getPlaygroundHeight());
        p.setPlaygroundWidth(this.getPlaygroundWidth());

        for(TransitionPart tp : getTransitions().keySet()){
            p.addTransition(tp, getTransitions().get(tp));
        }

        p.initPlayground(getPlaygroundHeight(), getPlaygroundWidth());
        for(int row = 0; row < getPlaygroundHeight(); row++){
            for(int col = 0; col < getPlaygroundWidth(); col++){
                p.setSymbolOnPlaygroundPosition(row, col, this.getSymbolOnPlaygroundPosition(row, col));
            }
        }
        return p;
    }
    
    public void printPlayground() {
    	for(int row = 0; row < getPlaygroundHeight(); row++) {
    		for(int col = 0; col < getPlaygroundWidth(); col++) {
    			System.out.print(playground[row][col] + " ");
    		}
    		System.out.println();
    	}
		System.out.println();
		System.out.println();
    }
}
