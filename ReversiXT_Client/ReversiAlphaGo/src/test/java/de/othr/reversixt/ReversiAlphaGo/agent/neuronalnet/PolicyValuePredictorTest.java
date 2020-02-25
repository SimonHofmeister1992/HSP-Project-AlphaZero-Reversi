package de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet;

import de.othr.reversixt.ReversiAlphaGo.general.MainTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.dataset.api.DataSetUtil;
import org.nd4j.linalg.dataset.api.iterator.CachingDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.ParallelDataSetIterator;
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher;
import org.nd4j.linalg.factory.Nd4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

            System.out.println(pvp.getComputationGraph().getNumOutputArrays());


            int miniBatchSize = 1;
            int boardSize = 15;
            int numFeaturePlanes = 4;

            log.info("Initializing AGZ model");
            ComputationGraph cg = pvp.getComputationGraph();

            // ****************************************
            // TRAIN INPUT AND OUTPUT
            // ****************************************

            log.info("Create dummy data");
            INDArray input = Nd4j.create(miniBatchSize,numFeaturePlanes, boardSize, boardSize);

            // move prediction has one value for each point on the board (15*15) plus one for passing.
            INDArray policyOutput = Nd4j.ones(miniBatchSize, boardSize * boardSize + 1);

            // the value network spits out a value between 0 and 1 to assess how good the current board situation is.
            INDArray valueOutput = Nd4j.ones(miniBatchSize, 1);

            // ****************************************
            // TRAIN MODEL
            // ****************************************

            log.info("Train AGZ model");
            cg.fit(new INDArray[] {input}, new INDArray[] {policyOutput, valueOutput});

            // ****************************************
            // PREDICTION ON INPUTS
            // ****************************************
            INDArray[] output = cg.output(false, input);


            // ****************************************
            // PRINT PREDICTIONS
            // ****************************************
            System.out.println("Policy");
            System.out.println(output[0].toStringFull());
            System.out.println("Value");
            System.out.println(output[1].toStringFull());

            assertTrue( true );
        }

}
