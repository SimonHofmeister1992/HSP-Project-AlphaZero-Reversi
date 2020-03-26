package de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet;

import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// ONLY USE FOR MANUAL TESTING/DEBUGGING
public class PlaygroundTransformerTest extends TestCase {
        private static final Logger log = LoggerFactory.getLogger(PlaygroundTransformerTest.class);

        /**
         * Create the test case
         *
         * @param testName name of the test case
         */
        public PlaygroundTransformerTest ( String testName )
        {
            super( testName );
        }

        /**
         * @return the suite of tests being tested
         */
        public static Test suite()
        {
            return new TestSuite( de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.PlaygroundTransformerTest.class );
        }



        /**
         * Rigourous Tests :-)
         */

        public void testPlaygroundTransformation()
        {

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

            PlaygroundTransformer pgt = new PlaygroundTransformer();
            INDArray transformedPlayground = pgt.transform(playground, player);

            PolicyValuePredictor pvp = PolicyValuePredictor.getInstance();
            ComputationGraph computationGraph = pvp.getComputationGraph();

            INDArray[] output = computationGraph.output(false, transformedPlayground);

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
