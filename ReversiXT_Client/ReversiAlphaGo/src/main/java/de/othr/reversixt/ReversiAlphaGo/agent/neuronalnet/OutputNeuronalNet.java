package de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet;

import org.nd4j.linalg.api.ndarray.INDArray;

public class OutputNeuronalNet {

    INDArray outputValueHead;
    INDArray outputPolicyHead;

    public OutputNeuronalNet(INDArray outputPolicyHead, INDArray outputValueHead) {
        this.outputPolicyHead = outputPolicyHead;
        this.outputValueHead = outputValueHead;
    }

    public INDArray getOutputValueHead() {
        return outputValueHead;
    }

    public INDArray getOutputPolicyHead() {
        return outputPolicyHead;
    }
}
