package de.othr.reversixt.ReversiAlphaGo.environment;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class EnvironmentTest
        extends TestCase

{
    private final String CLASS_NAME = "EnvironmentTest: ";
    private Environment environment;
    private final int NUM_OF_PLAYERS = 2;
    private final int NUM_OF_OVERRIDES = 1;
    private final int NUM_OF_BOMBS = 2;
    private final int STRENGTH_OF_BOMBS = 3;
    private final int PLAYGROUND_HEIGHT = 7;
    private final int PLAYGROUND_WIDTH = 6;
    private final String TRANSITION = "4 1 0 <-> 4 7 4";
    private final char[][] PLAYGROUND = {
            {'0', '0', '0', '1', '0', '0'},
            {'0', '0', '0', '2', '0', '0'},
            {'0', '0', 'c', '0', '0', '0'},
            {'0', 'b', '1', '2', 'x', '0'},
            {'0', '0', '2', '1', '1', '0'},
            {'0', '0', 'i', '1', '0', '0'},
            {'b', '0', '0', '1', '0', '0'}
    };

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public EnvironmentTest(String testName )
    {
        super( testName );
        this.environment = new Environment();
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EnvironmentTest.class );
    }



    /**
     * Rigourous Tests :-)
     */

    public void testParseRawMap()
    {
        String METHOD_NAME = "testParseRawMap: ";
        // prepare map to test
        StringBuffer sb = new StringBuffer();
        sb.append(NUM_OF_PLAYERS + System.lineSeparator()
                + NUM_OF_OVERRIDES + System.lineSeparator()
                + NUM_OF_BOMBS + " " + STRENGTH_OF_BOMBS + System.lineSeparator()
                + PLAYGROUND_HEIGHT + " " + PLAYGROUND_WIDTH + System.lineSeparator()
        );
        for(char[] line : PLAYGROUND){
            for (char c : line){
                sb.append(c + " ");
            }
            sb.append(System.lineSeparator());
        }
        sb.append(TRANSITION);

        // handle map
        environment.parseRawMap(sb.toString());

        // test resulting content
        assertTrue(CLASS_NAME+  METHOD_NAME + " number of players incorrect. got:  " + environment.getNumOfPlayers() + ", expected: " + NUM_OF_PLAYERS,environment.getNumOfPlayers()==NUM_OF_PLAYERS);
        assertTrue(CLASS_NAME+  METHOD_NAME + " number of overrides incorrect. got:  " + environment.getNumOfOverrideStones() + ", expected: " + NUM_OF_OVERRIDES,environment.getNumOfOverrideStones()==NUM_OF_OVERRIDES);
        assertTrue(CLASS_NAME+  METHOD_NAME + " number of bombs incorrect. got:  " + environment.getNumOfBombs() + ", expected: " + NUM_OF_BOMBS,environment.getNumOfBombs()==NUM_OF_BOMBS);
        assertTrue(CLASS_NAME+  METHOD_NAME + " strength of bombs incorrect. got:  " + environment.getStrengthOfBombs() + ", expected: " + STRENGTH_OF_BOMBS,environment.getStrengthOfBombs()==STRENGTH_OF_BOMBS);
        assertTrue(CLASS_NAME+  METHOD_NAME + " playground height incorrect. got:  " + environment.getPlayground().getPlaygroundHeight() + ", expected: " + PLAYGROUND_HEIGHT,environment.getPlayground().getPlaygroundHeight()==PLAYGROUND_HEIGHT);
        assertTrue(CLASS_NAME+  METHOD_NAME + " playground width incorrect. got:  " + environment.getPlayground().getPlaygroundWidth() + ", expected: " + PLAYGROUND_WIDTH,environment.getPlayground().getPlaygroundWidth()==PLAYGROUND_WIDTH);
        assertTrue(CLASS_NAME+  METHOD_NAME + " number of transitions incorrect. got:  " + environment.getPlayground().getTransitions().size() + ", expected: " + 2,environment.getPlayground().getTransitions().size()==2);
        assertTrue(CLASS_NAME+  METHOD_NAME + " transition 1 for 4 1 0 incorrect. got:  " + environment.getPlayground().getTransitions().get(new TransitionPart(4,1,0)) + ", expected: " + "4 7 4",environment.getPlayground().getTransitions().get(new TransitionPart(4,1,0)).equals(new TransitionPart(4,7,4)));
        assertTrue(CLASS_NAME+  METHOD_NAME + " transition 1 for 4 7 4 incorrect. got:  " + environment.getPlayground().getTransitions().get(new TransitionPart(4,7,4)) + ", expected: " + "4 1 0",environment.getPlayground().getTransitions().get(new TransitionPart(4,7,4)).equals(new TransitionPart(4,1,0)));

        for(int row = 0; row < PLAYGROUND_HEIGHT; row++){
            for(int col = 0; col < PLAYGROUND_WIDTH; col++){
                assertTrue(CLASS_NAME+ METHOD_NAME + "map on position row " + row + " and col " + col + " is wrong. expected: " + PLAYGROUND[row][col] + ", got: " + environment.getPlayground().getPlayground()[row][col], PLAYGROUND[row][col]==environment.getPlayground().getPlayground()[row][col]);
            }
        }

        Player p1 = environment.getPlayers()[0];
        Player p2 = environment.getPlayers()[1];

        assertTrue("the symbol of player 1 is incorrect, expected: '1', got: " + p1.getSymbol(),p1.getSymbol()=='1');
        assertTrue("the number of bombs of player 1 is incorrect, expected: " + NUM_OF_BOMBS + ", got: " + p1.getRemainingBombs(),p1.getRemainingBombs()==NUM_OF_BOMBS);
        assertTrue("the number of overrides of player 1 is incorrect, expected: " + NUM_OF_OVERRIDES + ", got: " + p1.getRemainingOverrideStones(),p1.getRemainingOverrideStones()==NUM_OF_OVERRIDES);

        assertTrue("the symbol of player 2 is incorrect, expected: '2', got: " + p2.getSymbol(),p2.getSymbol()=='2');
        assertTrue("the number of bombs of player 2 is incorrect, expected: " + NUM_OF_BOMBS + ", got: " + p2.getRemainingBombs(),p2.getRemainingBombs()==NUM_OF_BOMBS);
        assertTrue("the number of overrides of player 2 is incorrect, expected: " + NUM_OF_OVERRIDES + ", got: " + p2.getRemainingOverrideStones(),p2.getRemainingOverrideStones()==NUM_OF_OVERRIDES);

    }

    public void testUpdatePlayground(){
        String METHOD_NAME = "testUpdatePlayground: ";
        testParseRawMap();
        char[][] expectedResultingPlayground;
        Playground savedPlayground = environment.getPlayground().getCloneOfPlayground();

        // test normal turn

        Turn turn = new Turn('1', 5,2,0); // set onto '0'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', 'b', '1', '2', 'x', '0'},
                {'0', '1', '1', '1', '1', '0'},
                {'0', '0', 'i', '1', '0', '0'},
                {'b', '0', '0', '1', '0', '0'}
        };
        environment.updatePlayground(turn);
        assertTrue(CLASS_NAME + METHOD_NAME+ "normal turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));

        // test 'x' turn

        environment.setPlayground(savedPlayground);

        turn = new Turn('1', 4,5,0); // set onto 'x'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', 'b', '1', '1', '1', '0'},
                {'0', '0', '2', '1', '1', '0'},
                {'0', '0', 'i', '1', '0', '0'},
                {'b', '0', '0', '1', '0', '0'}
        };
        environment.updatePlayground(turn);
        assertTrue(CLASS_NAME + METHOD_NAME+ "'x' turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));
        assertTrue(CLASS_NAME + METHOD_NAME+ "transitions: overrides not decreased", environment.getPlayerByPlayerIcon('1').getRemainingOverrideStones()==NUM_OF_OVERRIDES-1);

        // test 'b' turn: wish Bombs

        environment.setPlayground(savedPlayground);

        turn = new Turn('2', 4,2,20); // set onto 'b'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', '2', '2', '2', 'x', '0'},
                {'0', '0', '2', '1', '1', '0'},
                {'0', '0', 'i', '1', '0', '0'},
                {'b', '0', '0', '1', '0', '0'}
        };
        environment.updatePlayground(turn);
        assertTrue(CLASS_NAME + METHOD_NAME+ "b turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));
        assertTrue(CLASS_NAME + METHOD_NAME+ "number of bombs not increased",environment.getPlayerByPlayerIcon('2').getRemainingBombs()==NUM_OF_BOMBS+1);

        // test 'b' turn: wish Overrides

        environment.setPlayground(savedPlayground);

        turn = new Turn('2', 4,2,21); // set onto 'b'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', '2', '2', '2', 'x', '0'},
                {'0', '0', '2', '1', '1', '0'},
                {'0', '0', 'i', '1', '0', '0'},
                {'b', '0', '0', '1', '0', '0'}
        };
        environment.updatePlayground(turn);
        assertTrue(CLASS_NAME + METHOD_NAME+ "b turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));
        assertTrue(CLASS_NAME + METHOD_NAME+ "number of overrides not increased",environment.getPlayerByPlayerIcon('2').getRemainingOverrideStones()==NUM_OF_OVERRIDES+1);

        // test 'c' turn

        environment.setPlayground(savedPlayground);

        turn = new Turn('2', 3,3,1); // set onto 'c' // switch with player 1
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '1', '0', '0', '0'},
                {'0', 'b', '1', '1', 'x', '0'},
                {'0', '0', '1', '2', '2', '0'},
                {'0', '0', 'i', '2', '0', '0'},
                {'b', '0', '0', '2', '0', '0'}
        };
        environment.updatePlayground(turn);
        assertTrue(CLASS_NAME + METHOD_NAME+ "c turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));

        // test 'i' turn

        environment.setPlayground(savedPlayground);

        turn = new Turn('1', 6,3,0); // player 1 set onto 'i'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', 'b', '2', '1', 'x', '0'},
                {'0', '0', '2', '2', '2', '0'},
                {'0', '0', '2', '2', '0', '0'},
                {'b', '0', '0', '2', '0', '0'}
        };
        environment.updatePlayground(turn);
        assertTrue(CLASS_NAME + METHOD_NAME+ "i turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));

        // test 'transition' turn: use override

        environment.setPlayground(savedPlayground);

        turn = new Turn('2', 6,4,0); // using override
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', 'b', '1', '2', 'x', '0'},
                {'0', '0', '2', '2', '1', '0'},
                {'0', '0', 'i', '2', '0', '0'},
                {'b', '0', '0', '2', '0', '0'}
        };
        environment.updatePlayground(turn);
        assertTrue(CLASS_NAME + METHOD_NAME+ "transition turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));
        assertTrue(CLASS_NAME + METHOD_NAME+ "transitions: overrides not decreased", environment.getPlayerByPlayerIcon('2').getRemainingOverrideStones()==NUM_OF_OVERRIDES);
    }

    public Environment getEnvironment() {
        return environment;
    }
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public boolean arePlaygroundsIdentical(char[][] p1, char[][] p2){
        for(int row = 0; row < environment.getPlayground().getPlaygroundHeight(); row++){
            for(int col = 0; col < environment.getPlayground().getPlaygroundWidth(); col++){
                if(p1[row][col] != p2[row][col]){
                    return false;
                }
            }
        }
        return true;
    }
}
