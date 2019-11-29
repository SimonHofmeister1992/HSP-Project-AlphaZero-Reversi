package de.othr.reversixt.ReversiAlphaGo.environment;

public class Environment {

    private int phase;
    private int numOfPlayers;
    private int numOfOverrideStones;
    private int numOfBombs;
    private int strengthOfBombs;
    private Playground playground;
    private Player[] players;


    public Environment(){
        setPhase(IPhase.TURN_PHASE);
        this.playground=new Playground();
    }

    public void parseRawMap(String rawMap) {
        int line = 0;
        if(!rawMap.isEmpty()){
            for(String rawMapLine : rawMap.split("\n")){

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
                else if(line==3) {
                    String[] columns = rawMapLine.split(" ");
                    this.playground.setPlaygroundHeight(Integer.parseInt(columns[0].trim()));
                    this.playground.setPlaygroundWidth(Integer.parseInt(columns[1].trim()));
                    this.playground.initPlayground(playground.getPlaygroundHeight(), playground.getPlaygroundWidth());
                }

                else if (line >3 && line <= playground.getPlaygroundHeight() + 3){
                    String[] columns = rawMapLine.split(" ");
                    for(int column = 0; column < columns.length-1; column++){
                        playground.setSymbolOnPlaygroundPosition(line-4,column,columns[column].charAt(0));
                    }
                }

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

    public void updatePlayground(Turn turn){
        if(getPhase()==IPhase.TURN_PHASE){
            Player player = getPlayerByPlayerIcon(turn.getPlayerIcon());
            if(player != null) this.playground.updatePlaygroundPhase1(turn, player, numOfPlayers);
        }
    }

    public void disqualifyPlayer(char playerIcon){
        for(Player p : players){
            if(p.getSymbol()==playerIcon) p.setDisqualified(Boolean.TRUE);
        }
    }

    public boolean isPlayerDisqualified(char playerIcon){
        for(Player p : players){
            if(p.getSymbol()==playerIcon) {
                return p.isDisqualified();
            }
        }
        return Boolean.FALSE;
    }

    public void increasePhaseNumber() {
        setPhase(getPhase() + 1);
    }

    public int getPhase() {
        return phase;
    }
    public void setPhase(int phase) {
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
        Player player = null;
        for(Player p : players){
            if(p.getSymbol()==icon) {
                player=p;
                break;
            }
        }
        return player;
    }

}
