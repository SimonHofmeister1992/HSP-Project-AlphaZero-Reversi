package de.othr.reversixt.ReversiAlphaGo.environment;

public class Environment {

    private int phase;
    private int dummyDisqualifyCounter=0;

    public Environment(){
        setPhase(IPhase.TURN_PHASE);
    }

    // TODO: THIS IS ONLY A DUMMY!!!: parse into correct map
    public void parseRawMap(String rawMap) {
        System.out.println("the raw map is: " + System.lineSeparator() + rawMap);
    }

    // TODO: THIS IS ONLY A DUMMY!!! senseful disqualifier needed
    public void disqualifyPlayer(char playerIcon){

    }
    // TODO: THIS IS ONLY A DUMMY!!! senseful disqualifier needed
    public boolean isPlayerDisqualified(char playerIcon){
        dummyDisqualifyCounter++;
        boolean isDisqualified = false;
        if(dummyDisqualifyCounter>=15) isDisqualified = true;
        return isDisqualified;
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
}
