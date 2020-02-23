package de.othr.reversixt.ReversiAlphaGo.agent;

import de.othr.reversixt.ReversiAlphaGo.communication.ServerCommunicator;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;

import java.util.concurrent.Callable;

public class AgentCallable implements Callable<Turn> {

    private Agent agent;

    public AgentCallable(Environment environment){
        agent = new Agent(environment);
    }

    @Override
    public Turn call() {
        Turn turn;
        turn = agent.play();
        return turn;
    }

    public Agent getAgent(){
        return agent;
    }
}
