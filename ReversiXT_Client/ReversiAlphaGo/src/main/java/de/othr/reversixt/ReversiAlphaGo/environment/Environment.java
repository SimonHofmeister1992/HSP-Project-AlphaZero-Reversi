package de.othr.reversixt.ReversiAlphaGo.environment;

import java.util.HashMap;

public class Environment {
	
	
    private int phase;
    private int numOfPlayers;
    private int strengthOfBombs;
    private Playground playground;
    private Player[] players;
    private Player ourPlayer;
    private HashMap<TransitionPart, TransitionPart> transitions;
    int numOfTurns = 0;

    public Environment() {
        setPhase(IPhase.TURN_PHASE);
        this.transitions = new HashMap<>();
    }

    public void parseRawMap(String rawMap) {
        int line = 0;
        int numOfBombs = 0;
        int numOfOverrideStones = 0;
        if (!rawMap.isEmpty()) {
            for (String rawMapLine : rawMap.split(System.lineSeparator())) {
                // number of Player
                if (line == 0) {
                    this.numOfPlayers = Integer.parseInt(rawMapLine.trim());
                    this.players = new Player[numOfPlayers];
                    // start number of override stones
                } else if (line == 1) {
                    numOfOverrideStones = Integer.parseInt(rawMapLine.trim());
                }
                // start number of bombs and which bomb strength
                else if (line == 2) {
                    String[] columns = rawMapLine.split(" ");
                    numOfBombs = Integer.parseInt(columns[0].trim());
                    this.setStrengthOfBombs(Integer.parseInt(columns[1].trim()));
                }
                //init Playground height and width
                else if (line == 3) {
                    String[] columns = rawMapLine.split(" ");
                    //create new map and init a blank map
                    playground = new Playground(Integer.parseInt(columns[0].trim()), Integer.parseInt(columns[1].trim()), numOfPlayers);
                }
                //init Playground
                else if (line > 3 && line <= playground.getPlaygroundHeight() + 3) {
                    String[] columns = rawMapLine.trim().split(" ");
                    for (int column = 0; column < columns.length; column++) {
                        playground.setSymbolOnPlaygroundPosition(line - 4, column, columns[column].trim().charAt(0));
                    }
                }
                //init Transition
                else if (line > playground.getPlaygroundHeight() + 3) {
                    String[] transitionParts = rawMapLine.split("->");
                    String[] fromTransition = transitionParts[0].split(" ");
                    String[] toTransition = transitionParts[1].split(" ");
                    TransitionPart first = new TransitionPart(Integer.parseInt(fromTransition[0].trim()),
                            Integer.parseInt(fromTransition[1].trim()),
                            Integer.parseInt(fromTransition[2].trim()));
                    TransitionPart second = new TransitionPart(Integer.parseInt(toTransition[1].trim()),
                            Integer.parseInt(toTransition[2].trim()),
                            Integer.parseInt(toTransition[3].trim()));
                    addTransition(first, second);
                }
                line++;
            }
            //give the transitions to playground
            playground.setTransitions(transitions);
            //init all player instance
            for (int i = 0; i < numOfPlayers; i++) {
                players[i] = new Player((char) (i + 49), numOfOverrideStones, numOfBombs);
            }
        }
    }

    /**
     * Games Rules
     * Update Playground - Makes a full move
     */
    public void updatePlayground(Turn turn, Playground playground) {
        if (getPhase() == IPhase.TURN_PHASE) {
            Player player = getPlayerByPlayerIcon(turn.getPlayerIcon());
            if (player != null) playground.updatePlaygroundPhase1(turn, player);
        }
        /* Bomb Phase
        else if(getPhase()==IPhase.BOMB_PHASE) {
            Player player = getPlayerByPlayerIcon(turn.getPlayerIcon());
            if(player != null) {
                getPlayground().updatePlaygroundPhase2(turn, player, getStrengthOfBombs());
            }
        }*/
    }

    public int getNumOfTurns() {
        return numOfTurns;
    }

    public void increaseNumOfTurns(){
        numOfTurns++;
    }

    public void disqualifyPlayer(char playerIcon) {
        getPlayerByPlayerIcon(playerIcon).setDisqualify();
    }

    public boolean isPlayerDisqualified(char playerIcon) {
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

    public Player getPlayerByPlayerIcon(char icon) {
        for (Player p : players) {
            if (p.getSymbol() == icon) {
                return p;
            }
        }
        return null;
    }

    public Player getOurPlayer() {
        return ourPlayer;
    }

    public void setOurPlayer(char ourIcon) {
        ourPlayer = getPlayerByPlayerIcon(ourIcon);
    }

    /**
     * determines whose turn is next
     *
     * @param playerSymbol implies whose turn it was now
     * @return player: specifies which player is next
     */
    public Player getNextPlayer(char playerSymbol) {
        int nextSymbol = ((playerSymbol - 49 + 1) % this.numOfPlayers) + 49;
        char nextSymbolChar = (char) nextSymbol;
        for (Player player : this.players) {
            if (player.getSymbol() == nextSymbolChar) {
                //System.out.println("Nex Player Symbol: " + nextSymbolChar);
                return player;
            }
        }
        // should not occur
        // if it occur -> invalid player
        return new Player((char) (this.numOfPlayers + 49), 0, 0);
    }

    public HashMap<TransitionPart, TransitionPart> getTransitions() {
        return transitions;
    }

    public void addTransition(TransitionPart firstTransitionPart, TransitionPart secondTransitionPart) {
        this.transitions.put(firstTransitionPart, secondTransitionPart);
        this.transitions.put(secondTransitionPart, firstTransitionPart);
    }

    public int getRankOfPlayer(Player player){

        HashMap<Character, Integer> rankMap = new HashMap<>();
        for(Player p : players){
            rankMap.put(p.getSymbol(), 0);
        }

        for(int row = 0; row < this.playground.getPlaygroundHeight(); row++){
            for(int col = 0; col < this.playground.getPlaygroundWidth(); col++){
                if(playground.getSymbolOnPlaygroundPosition(row, col) >= '1' && playground.getSymbolOnPlaygroundPosition(row, col) <= '8'){
                    rankMap.replace(playground.getSymbolOnPlaygroundPosition(row, col),
                            rankMap.get(playground.getSymbolOnPlaygroundPosition(row, col)),
                            rankMap.get(playground.getSymbolOnPlaygroundPosition(row, col))+1);
                }
            }
        }
        int rank = 1;
        Character ownPlayer = null;
        for(Character key : rankMap.keySet()){
            if((char)key == player.getSymbol()){
                ownPlayer = key;
            }
        }


        for(Character key : rankMap.keySet()){
            if(key != ownPlayer){
                if(rankMap.get(key) > rankMap.get(ownPlayer)){
                    rank++;
                }
            }
        }
        return rank;
    }

}
