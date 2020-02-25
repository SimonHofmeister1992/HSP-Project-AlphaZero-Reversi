package de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet;

import org.deeplearning4j.nn.graph.ComputationGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// *******************************************
// Singleton class, use getInstance() to get access to this object
// used to predict the action-policy and state-value for any playground
public class PolicyValuePredictor {

    private static final Logger log = LoggerFactory.getLogger(PolicyValuePredictor.class);
    private static PolicyValuePredictor PVP = null;
    private NeuronalNet neuronalNet;
    private static final int numberOfInputPlanes = 4;


    public static PolicyValuePredictor getInstance(){
        if(PolicyValuePredictor.PVP == null) {
            PolicyValuePredictor.PVP = new PolicyValuePredictor();
            PVP.neuronalNet = NeuronalNet.getInstance(numberOfInputPlanes);
        }
        return PVP;
    }

    public ComputationGraph getComputationGraph(){
        return this.neuronalNet.getComputationGraph();
    }

}
