package de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet;

import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;

// Output of the blocks of the neural net
// containing the new complete model and a string containing the
// name of the last layer in this graph for further usage in the net
public class BlockOutput {

    String nameLastLayer;
    ComputationGraphConfiguration.GraphBuilder graphBuilder;

    public BlockOutput(ComputationGraphConfiguration.GraphBuilder graphBuilder, String nameLastLayer){
        this.graphBuilder = graphBuilder;
        this.nameLastLayer = nameLastLayer;
    }

}
