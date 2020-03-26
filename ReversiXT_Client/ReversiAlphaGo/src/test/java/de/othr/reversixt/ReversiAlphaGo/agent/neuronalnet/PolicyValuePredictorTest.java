package de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet;


import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.general.Main;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;

import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


// ONLY USE FOR MANUAL TESTING/DEBUGGING
public class PolicyValuePredictorTest extends TestCase {
    private static final Logger log = LoggerFactory.getLogger(PolicyValuePredictor.class);

    /**
         * Create the test case
         *
         * @param testName name of the test case
         */
        public PolicyValuePredictorTest ( String testName )
        {
            super( testName );
        }

        /**
         * @return the suite of tests being tested
         */
        public static Test suite()
        {
            return new TestSuite( de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.PolicyValuePredictorTest.class );
        }



        /**
         * Rigourous Tests :-)
         */

        public void testNeuronalNetPrediction()
        {

            // ****************************************
            // SETUP MODEL
            // ****************************************

            PolicyValuePredictor pvp = PolicyValuePredictor.getInstance();

            int miniBatchSize = 1;
            int boardSize = 15;

            log.info("initialize model");
            ComputationGraph cg = pvp.getComputationGraph();

            // ****************************************
            // TRAIN INPUT AND OUTPUT
            // ****************************************

            log.info("create test dummy data");
            INDArray input = Nd4j.create(miniBatchSize,3, boardSize, boardSize);

            INDArray in = Nd4j.ones(miniBatchSize,1, boardSize, boardSize);

            List<INDArray> indArrays = new ArrayList<>();

            INDArray indArray = Nd4j.concat(1, input, in);



            // move prediction has one value for each point on the board (15*15) plus one for passing.
            INDArray policyOutput = Nd4j.ones(miniBatchSize, boardSize * boardSize + 1);

            // the value network spits out a value between 0 and 1 to assess how good the current board situation is.
            INDArray valueOutput = Nd4j.ones(miniBatchSize, 1);

            // ****************************************
            // TRAIN MODEL
            // ****************************************

            log.info("train model");

            cg.fit(new INDArray[] {indArray}, new INDArray[] {policyOutput, valueOutput});

            // ****************************************
            // PREDICTION ON INPUTS
            // ****************************************
            INDArray[] output = cg.output(false, indArray);


            // ****************************************
            // PRINT PREDICTIONS
            // ****************************************
            System.out.println("Policy: " + output[0].toStringFull());
            System.out.println("Value: " + output[1].toStringFull());
            System.out.println("Number of Parameters: " +  customFormat("###,###.###", cg.numParams()));

            assertTrue( true );
        }

    public String customFormat(String pattern, double value ) {
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        String output = myFormatter.format(value);
        return output;
    }
// uncomment to test this. Be careful: it will overwrite the actual saved models
/*    public void testSaveModel(){
        PolicyValuePredictor pvp = PolicyValuePredictor.getInstance();
        ComputationGraph cg = pvp.getComputationGraph();

        pvp.saveAsBestModel();
        pvp.saveAsActualModel();
        assertTrue( true );
    }*/

    public void testLoadModel(){
        System.out.println("test load model play mode");
        Main.LEARNER_MODE = false;
        testNeuronalNetPrediction();
        System.out.println("test load model train mode");
        Main.LEARNER_MODE = true;
        testNeuronalNetPrediction();

    }

    public void testTrainAndEvaluateComputationGraph(){

        // ****************************************
        // SETUP MODEL
        // ****************************************
        System.out.println("test train and evaluate model");
        PolicyValuePredictor pvp = PolicyValuePredictor.getInstance();

        int miniBatchSize = 1;
        int boardSize = 15;

        log.info("initialize model");
        ComputationGraph cg = pvp.getComputationGraph();

        // ****************************************
        // TRAIN INPUT AND OUTPUT
        // ****************************************

        log.info("create test dummy data");
        Playground playground = new Playground(8, 8, 2);

        char[][] innerPlayground = new char[][] {
                {'0','0','0','0','0','0','0','0'},
                {'0','0','0','0','0','0','0','0'},
                {'0','0','0','0','0','0','0','0'},
                {'0','0','0','1','2','0','0','0'},
                {'-','-','0','2','1','0','0','0'},
                {'-','0','0','0','0','0','0','0'},
                {'0','0','0','0','0','0','0','0'},
                {'0','0','0','0','0','0','0','0'},
        };

        Player player = new Player('2', 0,0);

        playground.setPlayground(innerPlayground);
        Playground[] playgrounds = new Playground[1];
        playgrounds[0] = playground;
        Player[] players = new Player[1];
        players[0] = player;

        // move prediction has one value for each point on the board (15*15) plus one for passing.
        INDArray policyOutput = Nd4j.ones(miniBatchSize, boardSize * boardSize + 1);

        // the value network spits out a value between 0 and 1 to assess how good the current board situation is.
        INDArray valueOutput = Nd4j.ones(miniBatchSize, 1);

        // ****************************************
        // TRAIN MODEL
        // ****************************************

        log.info("train model");
        INDArray policyOutputs = Nd4j.create(0,boardSize*boardSize+1);
        policyOutputs = Nd4j.concat(0, policyOutputs, policyOutput);
        INDArray valueOutputs = Nd4j.create(0,1);
        valueOutputs = Nd4j.concat(0, valueOutputs, valueOutput);
        pvp.trainComputationGraph(playgrounds, players, policyOutputs, valueOutputs);

        // ****************************************
        // PREDICTION ON INPUTS
        // ****************************************

        OutputNeuronalNet output = pvp.evaluate(playground, player);


        // ****************************************
        // PRINT PREDICTIONS
        // ****************************************
        System.out.println("Policy: " + output.outputPolicyHead.toStringFull());
        System.out.println("Value: " + output.outputValueHead.toStringFull());
        System.out.println("Number of Parameters: " +  customFormat("###,###.###", cg.numParams()));

        assertTrue( true );
    }

}
