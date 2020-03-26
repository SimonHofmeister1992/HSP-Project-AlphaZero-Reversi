package de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet;


import de.othr.reversixt.ReversiAlphaGo.general.AlphaGoZeroConstants;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.ConvolutionMode;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.ElementWiseVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.preprocessor.CnnToFeedForwardPreProcessor;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.HashMap;

//**************************************************
// Access to an Instance of the Singleton NeuronalNet:
// use the getInstance() method.
//
class NeuronalNet {

    private static NeuronalNet NeuronalNet = null; // instance of the neural net
    private ComputationGraph computationGraph; // model of the neural net

    private final int MAP_SIZE = AlphaGoZeroConstants.DIMENSION_PLAYGROUND;    // width and height of quadratic playground

    // getter
    ComputationGraph getComputationGraph(){
        return this.computationGraph;
    }

    //*************************************************************************
    //******************************************************************
    // Returns the singleton of the neural net.
    // @param: numOfInputPlanes: the numbers of layers of the playground which are forwarded to
    //          the neural net. if there are 2 players, we have 4 planes. 2 are containing the stones for
    //          every one of the players, 1 contains the wholes of in the playground and 1 contains the actual player.
    //
    //          plane1: if player 1 has a stone on row,col, the value there is 1 else 0
    //          plane2: if player 2 has a stone on row,col, the value there is 1 else 0
    //          plane3: if there is a hole in the playground on row,col, the value there is 1 else 0
    //          plane4: if it's the turn of player '1': the value of all fields is 0. if player '2' all values are 1.
    //  So provide 4 as the number of planes.
    // @output: the instance of the class containing the model of the neural net in the member "computationGraph"
    static NeuronalNet getInstance(int numOfInputPlanes){
        if(NeuronalNet.NeuronalNet == null) NeuronalNet.NeuronalNet = new NeuronalNet(numOfInputPlanes);
        return NeuronalNet;
    }

    //*************************************************************************
    //******************************************************************
    // define the whole structure of the neural net
    // @param: see getInstance()
    private NeuronalNet(int numOfInputPlanes){
        buildComputationGraph(numOfInputPlanes);
    }

    //*************************************************************************
    //******************************************************************
    // define the whole structure of the neural net
    // @param: see getInstance()
    // @output: the complete model of the neural net is saved in the member variable computationGraph
    private void buildComputationGraph(int numOfInputPlanes){
        String input = "in";

        int[] kernelSize = new int[]{3,3};
        int[] strides = new int[]{1,1};
        ConvolutionMode convMode = ConvolutionMode.Same;

        ComputationGraphConfiguration.GraphBuilder graphBuilder = buildNeuralNetStructure();

        graphBuilder.addInputs(input);
        BlockOutput preConvOut = addConvBlock(graphBuilder, input, numOfInputPlanes, kernelSize, strides, convMode);
        BlockOutput resLayersOut = addResidualLayers(preConvOut.graphBuilder, preConvOut.nameLastLayer, kernelSize, strides, convMode);
        BlockOutput policyHead = addPolicyHead(resLayersOut.graphBuilder, resLayersOut.nameLastLayer, kernelSize, strides, convMode);
        BlockOutput valueHead = addValueHead(resLayersOut.graphBuilder, resLayersOut.nameLastLayer, kernelSize, strides, convMode);
        graphBuilder = valueHead.graphBuilder;
        graphBuilder.setOutputs(policyHead.nameLastLayer, valueHead.nameLastLayer);

        ComputationGraph computationGraph = new ComputationGraph(graphBuilder.build());
        computationGraph.init();
        this.computationGraph = computationGraph;
    }

    // initialize the main features of the  neural net
    private ComputationGraphConfiguration.GraphBuilder buildNeuralNetStructure(){


        ComputationGraphConfiguration.GraphBuilder graphBuilder = new NeuralNetConfiguration.Builder()
                    .weightInit(WeightInit.RELU_UNIFORM)
                    .cudnnAlgoMode(ConvolutionLayer.AlgoMode.NO_WORKSPACE)
                    .updater(new Sgd())
                    .trainingWorkspaceMode(WorkspaceMode.ENABLED)
                    .cacheMode(CacheMode.DEVICE)
                    .l1(0.9)
                    .l1Bias(0.9)
                    .l2(0.0001)
                    .l2Bias(0.0001)
                    .graphBuilder()
                .backpropType(BackpropType.Standard)
                .setInputTypes(InputType.convolutional(MAP_SIZE, MAP_SIZE, AlphaGoZeroConstants.NUMBER_OF_FEATURE_PLANES_INPUT_NEURONAL_NET));

        return graphBuilder;
    }


    //**********************************************************
    // define the 4 main blocks of the Neuronal Net Architecture
    //**********************************************************

    private BlockOutput addConvBlock(ComputationGraphConfiguration.GraphBuilder graphBuilder, String input, int numOfInputPlanes, int[] kernelSize, int[] stride, ConvolutionMode cMode) {
        String outputNameLastLayer;

        String conv = "convConv", batchNorm = "convBatchNorm", activation = "convActivation";

        graphBuilder.addLayer(conv, new ConvolutionLayer.Builder().kernelSize(kernelSize).stride(stride).convolutionMode(cMode).nIn(numOfInputPlanes).nOut(256).build(), input);
        graphBuilder.addLayer(batchNorm, new BatchNormalization.Builder().nOut(256).build(), conv);
        graphBuilder.addLayer(activation, new ActivationLayer.Builder().activation(Activation.LEAKYRELU).build(), batchNorm);

        outputNameLastLayer = activation;

        return new BlockOutput(graphBuilder, outputNameLastLayer);
    }

    //*********************************************************************
    // adds 40 layers of residual blocks to the graph
    // @param graphBuilder: the actualGraphBuilder which shall be appended
    // @param nameLastLayer: the name of the last added layer which is input to the first layer of this block
    // @param kernelSize: the kernelSize for the convolutional layer
    // @param stride: the stride for the convolutional layer
    // @param cMode: the convolutionalMode for the convolutional layer
    private BlockOutput addResidualLayers(ComputationGraphConfiguration.GraphBuilder graphBuilder, String nameLastLayer, int[] kernelSize, int[] stride, ConvolutionMode cMode) {

        String nameLastBlock = nameLastLayer;
        String mergeBlock, activation;

        String conv1, batchNorm1, activation1, conv2, batchNorm2;


        for(int i = 0; i < 40; i++){
            mergeBlock = "resMerge" + i;
            activation = "resActivation" + i;

            conv1 = "resConv1" + i;
            conv2 = "resConv2" + i;
            batchNorm1 = "resBatchNorm1" + i;
            batchNorm2 = "resBatchNorm2" + i;
            activation1 = "resActivation1_" + i;

            //first Block of ResLayer
            graphBuilder.addLayer(conv1, new ConvolutionLayer.Builder().kernelSize(kernelSize).stride(stride).convolutionMode(cMode).nIn(256).nOut(256).build(), nameLastBlock);
            graphBuilder.addLayer(batchNorm1, new BatchNormalization.Builder().nOut(256).build(), conv1);
            graphBuilder.addLayer(activation1, new ActivationLayer.Builder().activation(Activation.LEAKYRELU).build(), batchNorm1);

            // second Block of ResLayer
            graphBuilder.addLayer(conv2, new ConvolutionLayer.Builder().kernelSize(kernelSize).stride(stride).convolutionMode(cMode).nIn(256).nOut(256).build(), nameLastBlock);
            graphBuilder.addLayer(batchNorm2, new BatchNormalization.Builder().nOut(256).build(), conv2);


            // merge outputs of both resBlocks
            graphBuilder.addVertex(mergeBlock, new ElementWiseVertex(ElementWiseVertex.Op.Add), activation1, batchNorm2);
            graphBuilder.addLayer(activation, new ActivationLayer.Builder().activation(Activation.LEAKYRELU).build(), mergeBlock);

            nameLastBlock = activation;
        }

        nameLastLayer = nameLastBlock;

        return new BlockOutput(graphBuilder, nameLastLayer);
    }

    private BlockOutput addPolicyHead(ComputationGraphConfiguration.GraphBuilder graphBuilder, String nameLastLayer, int[] kernelSize, int[] stride, ConvolutionMode cMode) {
        String outputNameLastLayer;

        String conv = "polConv", batchNorm = "polBatchNorm", activation = "polActivation", dense = "polDense";

        graphBuilder.addLayer(conv, new ConvolutionLayer.Builder().kernelSize(kernelSize).stride(stride).convolutionMode(cMode).nIn(256).nOut(2).build(), nameLastLayer);
        graphBuilder.addLayer(batchNorm, new BatchNormalization.Builder().nOut(2).build(), conv);
        graphBuilder.addLayer(activation, new ActivationLayer.Builder().activation(Activation.LEAKYRELU).build(), batchNorm);
        graphBuilder.addLayer(dense, new OutputLayer.Builder(LossFunctions.LossFunction.MCXENT).nIn(MAP_SIZE*MAP_SIZE*2).nOut(MAP_SIZE*MAP_SIZE+1).build(), activation);

        HashMap<String, InputPreProcessor> map = new HashMap<String, InputPreProcessor>();
        map.put(dense, new CnnToFeedForwardPreProcessor(MAP_SIZE,MAP_SIZE,2));
        graphBuilder.setInputPreProcessors(map);

        outputNameLastLayer = dense;

        return new BlockOutput(graphBuilder, outputNameLastLayer);
    }

    private BlockOutput addValueHead(ComputationGraphConfiguration.GraphBuilder graphBuilder, String nameLastLayer, int[] kernelSize, int[] stride, ConvolutionMode cMode) {
        String outputNameLastLayer;

        String conv = "valConv", batchNorm = "valBatchNorm", activation = "valActivation", dense = "valDense", output="valOutput";

        graphBuilder.addLayer(conv, new ConvolutionLayer.Builder().kernelSize(kernelSize).stride(stride).convolutionMode(cMode).nIn(256).nOut(1).build(), nameLastLayer);
        graphBuilder.addLayer(batchNorm, new BatchNormalization.Builder().nOut(1).build(), conv);
        graphBuilder.addLayer(activation, new ActivationLayer.Builder().activation(Activation.LEAKYRELU).build(), batchNorm);
        graphBuilder.addLayer(dense, new DenseLayer.Builder().nIn(MAP_SIZE * MAP_SIZE).nOut(256).build(), activation);
        graphBuilder.addLayer(output, new OutputLayer.Builder(LossFunctions.LossFunction.MSE).activation(Activation.TANH).nIn(256).nOut(1).build(), dense);

        HashMap<String, InputPreProcessor> map = new HashMap<String, InputPreProcessor>();
        map.put(dense, new CnnToFeedForwardPreProcessor(MAP_SIZE,MAP_SIZE,1));
        graphBuilder.setInputPreProcessors(map);

        outputNameLastLayer = output;

        return new BlockOutput(graphBuilder, outputNameLastLayer);
    }

}
