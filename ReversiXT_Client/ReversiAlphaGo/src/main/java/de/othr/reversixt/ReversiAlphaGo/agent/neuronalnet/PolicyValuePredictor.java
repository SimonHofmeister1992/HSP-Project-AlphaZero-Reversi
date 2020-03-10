package de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet;

import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.general.AlphaGoZeroConstants;
import de.othr.reversixt.ReversiAlphaGo.general.Main;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

// *******************************************
// Singleton class, use getInstance() to get access to this object
// used to predict the action-policy and state-value for any playground

public class PolicyValuePredictor {
    private static final Logger log = LoggerFactory.getLogger(PolicyValuePredictor.class);
    private static PolicyValuePredictor PVP = null;
    private static NeuronalNet neuronalNet;
    private static ComputationGraph computationGraph;
    private static final int numberOfInputPlanes = AlphaGoZeroConstants.NUMBER_OF_FEATURE_PLANES_INPUT_NEURONAL_NET;
    private static final File bestComputationGraphFile = new File("model" + File.separator + "bestModel.zip");
    private static final File actualComputationGraphFile = new File("model" + File.separator + "actualModel.zip");
    private PlaygroundTransformer playgroundTransformer;

    //****************************************************************
    //  creates an instance of the PolicyValueGraph
    //****************************************************************
    public static PolicyValuePredictor getInstance() {

        if (PolicyValuePredictor.PVP == null) {
            PolicyValuePredictor.PVP = new PolicyValuePredictor();
            createComputationGraph();
        }
        return PVP;
    }

    private PolicyValuePredictor() {
        this.playgroundTransformer = new PlaygroundTransformer();
    }

    //****************************************************************
    //  returns the computation graph which shall be used
    //****************************************************************
    public ComputationGraph getComputationGraph() {
        return PolicyValuePredictor.computationGraph;
    }

    private static Logger getLogger() {
        return log;
    }

    //****************************************************************
    //  loads and builds the configuration of the computation graph
    //****************************************************************
    public static void createComputationGraph() {
        ComputationGraph load = null;
        try {
            if (actualComputationGraphFile.exists() && Main.LEARNER_MODE) {
                load = ComputationGraph.load(actualComputationGraphFile, true);
                if (!Main.QUIET_MODE) System.out.println("ComputationGraph: LearningGraph Loaded");
            } else if (!actualComputationGraphFile.exists() && Main.LEARNER_MODE) {
                load = ComputationGraph.load(bestComputationGraphFile, true);
                if (!Main.QUIET_MODE) System.out.println("ComputationGraph: BestGraph Loaded");
                saveAsActualModel();
            } else if (!Main.LEARNER_MODE && bestComputationGraphFile.exists()) {
                load = ComputationGraph.load(bestComputationGraphFile, true);
                if (!Main.QUIET_MODE) System.out.println("ComputationGraph: BestGraph Loaded");
            } else if (!Main.LEARNER_MODE && !bestComputationGraphFile.exists()) {
                load = ComputationGraph.load(actualComputationGraphFile, true);
                if (!Main.QUIET_MODE) System.out.println("ComputationGraph: LearningGraph Loaded");
                saveAsBestModel();
            }
        } catch (IOException e) {
            load = null;
        }

        if (load != null) PolicyValuePredictor.computationGraph = load;
        else {
            PVP.neuronalNet = NeuronalNet.getInstance(numberOfInputPlanes);
            PolicyValuePredictor.computationGraph = PolicyValuePredictor.neuronalNet.getComputationGraph();
            if (!Main.QUIET_MODE) System.out.println("ComputationGraph: New Graph Loaded");
            saveAsActualModel();
            saveAsBestModel();
        }
    }

    public static void saveAsBestModel() {
        try {
            computationGraph.save(bestComputationGraphFile, true);
        } catch (IOException e) {
            if (!Main.QUIET_MODE) getLogger().warn("File: " + bestComputationGraphFile + " is not accessable");
        }
    }

    public static void saveAsActualModel() {
        try {
            computationGraph.save(actualComputationGraphFile, true);
        } catch (IOException e) {
            if (!Main.QUIET_MODE) getLogger().warn("File: " + actualComputationGraphFile + " is not accessable");
        }
    }


    //**********************************************************************************
    // trains the neural net
    // @param playgrounds: recorded history of the playgrounds using the actual policy
    // @param players: player corresponding to each playground-state
    // @param policyOutputsToLearn: policies predicted by the neural net, corrected by the MCTS (INDArray containing INDArrays: one for each state)
    // @param valueOutputsToLearn: values predicted by the neural net, corrected by the MCTS (Reward)
    // usage example see PolicyValuePredictorTest.testTrainAndEvaluateComputationGraph()
    //*********************************************************************************
    public void trainComputationGraph(Playground[] playgrounds, Player[] players, INDArray policyOutputsToLearn, INDArray valueOutputsToLearn) {

        if (playgrounds.length == policyOutputsToLearn.size(0) && playgrounds.length == valueOutputsToLearn.size(0) && playgrounds.length == players.length) {

            INDArray transformedPlaygrounds = Nd4j.zeros(DataType.INT, 0, AlphaGoZeroConstants.NUMBER_OF_FEATURE_PLANES_INPUT_NEURONAL_NET, AlphaGoZeroConstants.DIMENSION_PLAYGROUND, AlphaGoZeroConstants.DIMENSION_PLAYGROUND);
            for (int i = 0; i < playgrounds.length; i++) {
                INDArray transformedPlayground = playgroundTransformer.transform(playgrounds[i], players[i]);
                transformedPlaygrounds = Nd4j.concat(0, transformedPlaygrounds, transformedPlayground);
            }
            computationGraph.fit(new INDArray[]{transformedPlaygrounds}, new INDArray[]{policyOutputsToLearn, valueOutputsToLearn});
        }
    }


    //**********************************************************************************
    // evaluates the neural net at a given state
    // @param playground: actual playground state
    // @param player: the player which shall play the next action
    // usage example see PolicyValuePredictorTest.testTrainAndEvaluateComputationGraph()
    //*********************************************************************************
    public OutputNeuronalNet evaluate(Playground playground, Player player) {

        INDArray transformedPlayground = playgroundTransformer.transform(playground, player);
        INDArray[] outputs = computationGraph.output
                (transformedPlayground);

        return new OutputNeuronalNet(outputs[0], outputs[1]);
    }

}
