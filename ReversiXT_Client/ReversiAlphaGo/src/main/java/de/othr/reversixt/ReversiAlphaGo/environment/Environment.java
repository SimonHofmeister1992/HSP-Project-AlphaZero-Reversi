package de.othr.reversixt.ReversiAlphaGo.environment;

public class Environment {

    private int phase;
    private int turn;
    private int numOfPlayers;
    private int numOfOverrideStones;
    private int numOfBombs;
    private int strengthOfBombs;
    private Playground playground;
    private Player[] players;


    public Environment(){
        setPhase(IPhase.TURN_PHASE);
        turn = 1;
        this.playground = new Playground();
    }

    public void parseRawMap(String rawMap) {
        int line = 0;
        if(!rawMap.isEmpty()){
            for(String rawMapLine : rawMap.split(System.lineSeparator())){
                if(line==0) {
                    this.numOfPlayers=Integer.parseInt(rawMapLine.trim());
                    this.players = new Player[numOfPlayers];
                }
                else if(line==1)  setNumOfOverrideStones(Integer.parseInt(rawMapLine.trim()));
                else if(line==2) {
                    String[] columns = rawMapLine.split(" ");
                    this.setNumOfBombs(Integer.parseInt(columns[0].trim()));
                    this.setStrengthOfBombs(Integer.parseInt(columns[1].trim()));
                }
                //init Playground height and width
                else if(line==3) {
                    String[] columns = rawMapLine.split(" ");
                    //create new map and init a blank map
                    playground.initPlayground(Integer.parseInt(columns[0].trim()),Integer.parseInt(columns[1].trim()), numOfPlayers);
                }
                //init Playground
                else if (line >3 && line <= playground.getPlaygroundHeight() + 3){
                    String[] columns = rawMapLine.trim().split(" ");
                    for(int column = 0; column < columns.length; column++){
                        playground.setSymbolOnPlaygroundPosition(line-4,column,columns[column].trim().charAt(0));
                    }
                }
                //init Transition
                else if (line > playground.getPlaygroundHeight() + 3){
                    String[] transitionParts = rawMapLine.split("->");
                    String[] fromTransition = transitionParts[0].split(" ");
                    String[] toTransition = transitionParts[1].split(" ");
                    TransitionPart first=new TransitionPart(Integer.parseInt(fromTransition[0].trim()),
                            Integer.parseInt(fromTransition[1].trim()),
                            Integer.parseInt(fromTransition[2].trim()));
                    TransitionPart second=new TransitionPart(Integer.parseInt(toTransition[1].trim()),
                            Integer.parseInt(toTransition[2].trim()),
                            Integer.parseInt(toTransition[3].trim()));
                    playground.addTransition(first,second);
                }
                line++;
            }
            for(int i = 0; i < numOfPlayers; i++){
                players[i] = new Player((char) (i+49), numOfOverrideStones, numOfBombs);
            }
        }
    }

    public void updatePlayground(Turn turn,  Playground playground){
        if(getPhase()==IPhase.TURN_PHASE){
            Player player = getPlayerByPlayerIcon(turn.getPlayerIcon());
            if(player != null) playground.updatePlaygroundPhase1(turn, player, numOfPlayers);
        }
        /* Bomb Phase
        else if(getPhase()==IPhase.BOMB_PHASE) {
            Player player = getPlayerByPlayerIcon(turn.getPlayerIcon());
            if(player != null) {
                getPlayground().updatePlaygroundPhase2(turn, player, getStrengthOfBombs());
            }
        }*/
        this.turn++;
    }

    public void disqualifyPlayer(char playerIcon){
        getPlayerByPlayerIcon(playerIcon).setDisqualify();
    }

    public boolean isPlayerDisqualified(char playerIcon){
        return getPlayerByPlayerIcon(playerIcon).isDisqualified();
    }

    public void nextPhase() {
        setPhase(getPhase() + 1);
    }

    public int getPhase() {
        return phase;
    }

    void setPhase(int phase) {
        this.phase = phase;
    }

    public Playground getPlayground() {
        return playground;
    }

    public void setPlayground(Playground playground) {
        this.playground = playground.getCloneOfPlayground();
    }

    public int getNumOfOverrideStones() {
        return numOfOverrideStones;
    }
    public void setNumOfOverrideStones(int numOfOverrideStones) {
        this.numOfOverrideStones = numOfOverrideStones;
    }

    public int getNumOfBombs() {
        return numOfBombs;
    }
    public void setNumOfBombs(int numOfBombs) {
        this.numOfBombs = numOfBombs;
    }

    public int getStrengthOfBombs() {
        return strengthOfBombs;
    }
    public void setStrengthOfBombs(int strengthOfBombs) {
        this.strengthOfBombs = strengthOfBombs;
    }

    public int getNumOfPlayers() {
        return numOfPlayers;
    }

    public Player[] getPlayers() {
        return players;
    }

    public Player getPlayerByPlayerIcon(char icon){
        for(Player p : players){
            if(p.getSymbol()==icon) {
                return p;
            }
        }
        return null;
    }

    public boolean validateTurnPhase1(Turn turn, Playground playground) {
        int row=turn.getRow();
        int col=turn.getColumn();
        Player player = getPlayerByPlayerIcon(turn.getPlayerIcon());
        int numOfColoredFields;
        char actualSymbol;

        if(!(row >= 0 && row < playground.getPlaygroundHeight()
                && col >= 0 && col < playground.getPlaygroundWidth())) {
            return false;
        }

        char startSymbol = playground.getSymbolOnPlaygroundPosition(row, col);

        if(startSymbol == '-') {
            return false;
        }
        else if(startSymbol == 'x' && player.getRemainingOverrideStones() > 0) {
            return true;
        }
        else if((startSymbol == 'x' || (startSymbol >= '1' && startSymbol <= '8'))
                && player.getRemainingOverrideStones() <= 0) {
            return false;
        }
        else {
            int[] newPos = new int[3];
            for(int direction = 0; direction < 8; direction++) {
                numOfColoredFields=0;
                newPos[0] = row;
                newPos[1] = col;
                newPos[2] = direction;
                while(true) {
                    newPos = playground.getNewPosition(newPos, newPos[0], newPos[1], newPos[2]);

                    if(!(newPos[0] >= 0 && newPos[0] < playground.getPlaygroundHeight()
                            && newPos[1] >= 0 && newPos[1] < playground.getPlaygroundWidth())) {
                        break;
                    }
                    actualSymbol = playground.getSymbolOnPlaygroundPosition(newPos[0], newPos[1]);
                    if(newPos[0]==row && newPos[1]==col) {
                        break;
                    }
                    else if(actualSymbol == player.getSymbol() && numOfColoredFields > 0) {
                        return true;
                    }
                    else if(actualSymbol == player.getSymbol()
                            || actualSymbol=='c'
                            || actualSymbol=='i'
                            || actualSymbol=='b'
                            || actualSymbol=='0'
                            || actualSymbol=='-') {
                        break;
                    }
                    else {
                        numOfColoredFields++;
                    }
                }

            }
        }
        return false;
    }

    public boolean validateTurnPhase2(Turn turn) {
        return getPlayground().getSymbolOnPlaygroundPosition(turn.getRow(), turn.getColumn())!='-';
    }
}
