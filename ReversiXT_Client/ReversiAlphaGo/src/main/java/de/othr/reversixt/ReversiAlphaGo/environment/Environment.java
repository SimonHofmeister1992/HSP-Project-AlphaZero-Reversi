package de.othr.reversixt.ReversiAlphaGo.environment;

import java.util.ArrayList;

public class Environment {

    private int phase;
    private int dummyDisqualifyCounter=0;
    private int numOfPlayers;
    private int numOfOverrideStonesInGame;
    private int numOfBombsInGame;
    private int strengthOfBombsInGame;
    private int playgroundHeight;
    private int playgroundWidth;
    private char[][] playground;
    ArrayList<Transition> transitions;


    public Environment(){
        setPhase(IPhase.TURN_PHASE);
        transitions=new ArrayList<Transition>();
    }

    public void parseRawMap(String rawMap) {
        int line = 0;
        if(!rawMap.isEmpty()){
            for(String rawMapLine : rawMap.split("\n")){

                if(line==0) numOfPlayers=Integer.parseInt(rawMapLine);
                else if(line==1) numOfOverrideStonesInGame=Integer.parseInt(rawMapLine);
                else if(line==2) {
                    String[] columns = rawMapLine.split(" ");
                    numOfBombsInGame=Integer.parseInt(columns[0]);
                    strengthOfBombsInGame=Integer.parseInt(columns[1]);
                }
                else if(line==3) {
                    String[] columns = rawMapLine.split(" ");
                    playgroundHeight=Integer.parseInt(columns[0]);
                    playgroundWidth=Integer.parseInt(columns[1]);
                    playground = new char[playgroundHeight][playgroundWidth];
                }

                else if (line >3 && line <= playgroundHeight + 3){
                    String[] columns = rawMapLine.split(" ");
                    for(int column = 0; column < columns.length; column++){
                        playground[line][column] = columns[column].charAt(0);
                    }
                }

                else if (line > playgroundHeight + 3){
                    String[] transitionParts = rawMapLine.split("->");
                    String[] fromTransition = transitionParts[0].split(" ");
                    String[] toTransition = transitionParts[1].split(" ");
                    transitions.add(new Transition(Integer.parseInt(fromTransition[0]),
                            Integer.parseInt(fromTransition[1]),
                            Integer.parseInt(fromTransition[2]),
                            Integer.parseInt(toTransition[0]),
                            Integer.parseInt(toTransition[1]),
                            Integer.parseInt(toTransition[2])));
                }

                line++;
            }
        }
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
