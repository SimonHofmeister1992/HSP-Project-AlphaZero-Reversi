package de.othr.reversixt.ReversiAlphaGo.environment;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
@SuppressWarnings("ALL")
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
    private final String TRANSITION = "3 0 0 <-> 3 6 4";
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

    // test methods

    public void testParseRawMap()
    {
        String METHOD_NAME = "testParseRawMap: ";
        // prepare map to test
        StringBuffer sb;
        sb = new StringBuffer();
        sb.append(NUM_OF_PLAYERS).append(System.lineSeparator()).append(NUM_OF_OVERRIDES).append(System.lineSeparator()).append(NUM_OF_BOMBS).append(" ").append(STRENGTH_OF_BOMBS).append(System.lineSeparator()).append(PLAYGROUND_HEIGHT).append(" ").append(PLAYGROUND_WIDTH).append(System.lineSeparator());
        for(char[] line : PLAYGROUND){
            for (char c : line){
                sb.append(c).append(" ");
            }
            sb.append(System.lineSeparator());
        }
        sb.append(TRANSITION);

        // handle map
        environment.parseRawMap(sb.toString());

        // test resulting content
        assertEquals(CLASS_NAME + METHOD_NAME + " number of players incorrect. got:  " + environment.getNumOfPlayers() + ", expected: " + NUM_OF_PLAYERS, environment.getNumOfPlayers(), NUM_OF_PLAYERS);
        assertEquals(CLASS_NAME + METHOD_NAME + " number of overrides incorrect. got:  " + environment.getPlayers()[0].getRemainingOverrideStones() + ", expected: " + NUM_OF_OVERRIDES, environment.getPlayers()[0].getRemainingOverrideStones(), NUM_OF_OVERRIDES);
        assertEquals(CLASS_NAME + METHOD_NAME + " number of bombs incorrect. got:  " + environment.getPlayers()[0].getRemainingBombs() + ", expected: " + NUM_OF_BOMBS, environment.getPlayers()[0].getRemainingBombs(), NUM_OF_BOMBS);
        assertEquals(CLASS_NAME + METHOD_NAME + " strength of bombs incorrect. got:  " + environment.getStrengthOfBombs() + ", expected: " + STRENGTH_OF_BOMBS, environment.getStrengthOfBombs(), STRENGTH_OF_BOMBS);
        assertEquals(CLASS_NAME + METHOD_NAME + " playground height incorrect. got:  " + environment.getPlayground().getPlaygroundHeight() + ", expected: " + PLAYGROUND_HEIGHT, environment.getPlayground().getPlaygroundHeight(), PLAYGROUND_HEIGHT);
        assertEquals(CLASS_NAME + METHOD_NAME + " playground width incorrect. got:  " + environment.getPlayground().getPlaygroundWidth() + ", expected: " + PLAYGROUND_WIDTH, environment.getPlayground().getPlaygroundWidth(), PLAYGROUND_WIDTH);
        assertEquals(CLASS_NAME + METHOD_NAME + " number of transitions incorrect. got:  " + environment.getTransitions().size() + ", expected: " + 2, 2, environment.getTransitions().size());
        assertEquals(CLASS_NAME + METHOD_NAME + " transition 1 for 3 0 0 incorrect. got:  " + environment.getTransitions().get(new TransitionPart(3, 0, 0)) + ", expected: " + "3 6 4", environment.getTransitions().get(new TransitionPart(3, 0, 0)), new TransitionPart(3, 6, 4));
        assertEquals(CLASS_NAME + METHOD_NAME + " transition 1 for 3 6 4 incorrect. got:  " + environment.getTransitions().get(new TransitionPart(3, 6, 4)) + ", expected: " + "3 0 0", environment.getTransitions().get(new TransitionPart(3, 6, 4)), new TransitionPart(3, 0, 0));

        for(int row = 0; row < PLAYGROUND_HEIGHT; row++){
            for(int col = 0; col < PLAYGROUND_WIDTH; col++){
                assertEquals(CLASS_NAME + METHOD_NAME + "map on position row " + row + " and col " + col + " is wrong. expected: " + PLAYGROUND[row][col] + ", got: " + environment.getPlayground().getPlayground()[row][col], PLAYGROUND[row][col], environment.getPlayground().getPlayground()[row][col]);
            }
        }

        Player p1 = environment.getPlayers()[0];
        Player p2 = environment.getPlayers()[1];

        assertEquals("the symbol of player 1 is incorrect, expected: '1', got: " + p1.getSymbol(), '1', p1.getSymbol());
        assertEquals("the number of bombs of player 1 is incorrect, expected: " + NUM_OF_BOMBS + ", got: " + p1.getRemainingBombs(), p1.getRemainingBombs(), NUM_OF_BOMBS);
        assertEquals("the number of overrides of player 1 is incorrect, expected: " + NUM_OF_OVERRIDES + ", got: " + p1.getRemainingOverrideStones(), p1.getRemainingOverrideStones(), NUM_OF_OVERRIDES);

        assertEquals("the symbol of player 2 is incorrect, expected: '2', got: " + p2.getSymbol(), '2', p2.getSymbol());
        assertEquals("the number of bombs of player 2 is incorrect, expected: " + NUM_OF_BOMBS + ", got: " + p2.getRemainingBombs(), p2.getRemainingBombs(), NUM_OF_BOMBS);
        assertEquals("the number of overrides of player 2 is incorrect, expected: " + NUM_OF_OVERRIDES + ", got: " + p2.getRemainingOverrideStones(), p2.getRemainingOverrideStones(), NUM_OF_OVERRIDES);

    }

    public void testUpdatePlayground(){
        String METHOD_NAME = "testUpdatePlayground: ";
        testParseRawMap();
        char[][] expectedResultingPlayground;
        Playground savedPlayground = environment.getPlayground().getCloneOfPlayground();

        // test normal turn

        Turn turn = new Turn('1', 4,1,0); // set onto '0'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', 'b', '1', '2', 'x', '0'},
                {'0', '1', '1', '1', '1', '0'},
                {'0', '0', 'i', '1', '0', '0'},
                {'b', '0', '0', '1', '0', '0'}
        };
        environment.updatePlayground(turn, environment.getPlayground());
        assertTrue(CLASS_NAME + METHOD_NAME+ "normal turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));

        // test 'x' turn

        environment.setPlayground(savedPlayground);

        turn = new Turn('1', 3,4,0); // set onto 'x'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', 'b', '1', '1', '1', '0'},
                {'0', '0', '2', '1', '1', '0'},
                {'0', '0', 'i', '1', '0', '0'},
                {'b', '0', '0', '1', '0', '0'}
        };
        environment.updatePlayground(turn, environment.getPlayground());
        assertTrue(CLASS_NAME + METHOD_NAME+ "'x' turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));
        assertEquals(CLASS_NAME + METHOD_NAME + "transitions: overrides not decreased", environment.getPlayerByPlayerIcon('1').getRemainingOverrideStones(), NUM_OF_OVERRIDES - 1);

        // test 'b' turn: wish Bombs

        environment.setPlayground(savedPlayground);

        turn = new Turn('2', 3, 1, 20); // set onto 'b'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', '2', '2', '2', 'x', '0'},
                {'0', '0', '2', '1', '1', '0'},
                {'0', '0', 'i', '1', '0', '0'},
                {'b', '0', '0', '1', '0', '0'}
        };
        environment.updatePlayground(turn, environment.getPlayground());
        assertTrue(CLASS_NAME + METHOD_NAME+ "b turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));
        assertEquals(CLASS_NAME + METHOD_NAME + "number of bombs not increased", environment.getPlayerByPlayerIcon('2').getRemainingBombs(), NUM_OF_BOMBS + 1);

        // test 'b' turn: wish Overrides

        environment.setPlayground(savedPlayground);

        turn = new Turn('2', 3, 1, 21); // set onto 'b'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', '2', '2', '2', 'x', '0'},
                {'0', '0', '2', '1', '1', '0'},
                {'0', '0', 'i', '1', '0', '0'},
                {'b', '0', '0', '1', '0', '0'}
        };
        environment.updatePlayground(turn, environment.getPlayground());
        assertTrue(CLASS_NAME + METHOD_NAME + "b turns don't work", arePlaygroundsIdentical(expectedResultingPlayground, environment.getPlayground().getPlayground()));
        assertEquals(CLASS_NAME + METHOD_NAME + "number of overrides not increased", environment.getPlayerByPlayerIcon('2').getRemainingOverrideStones(), NUM_OF_OVERRIDES + 1);

        // test 'c' turn

        environment.setPlayground(savedPlayground);

        turn = new Turn('2', 2,2,1); // set onto 'c' // switch with player 1
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', '1', '0', '0', '0'},
                {'0', 'b', '1', '1', 'x', '0'},
                {'0', '0', '1', '2', '2', '0'},
                {'0', '0', 'i', '2', '0', '0'},
                {'b', '0', '0', '2', '0', '0'}
        };
        environment.updatePlayground(turn, environment.getPlayground());
        assertTrue(CLASS_NAME + METHOD_NAME+ "c turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));

        // test 'i' turn

        environment.setPlayground(savedPlayground);

        turn = new Turn('1', 5,2,0); // player 1 set onto 'i'
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', '0', '1', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', 'b', '2', '1', 'x', '0'},
                {'0', '0', '2', '2', '2', '0'},
                {'0', '0', '2', '2', '0', '0'},
                {'b', '0', '0', '2', '0', '0'}
        };
        environment.updatePlayground(turn, environment.getPlayground());
        assertTrue(CLASS_NAME + METHOD_NAME+ "i turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));

        // test 'transition' turn: use override

        environment.setPlayground(savedPlayground);

        turn = new Turn('2', 5,3,0); // using override
        expectedResultingPlayground = new char[][]{
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', '0', '2', '0', '0'},
                {'0', '0', 'c', '0', '0', '0'},
                {'0', 'b', '1', '2', 'x', '0'},
                {'0', '0', '2', '2', '1', '0'},
                {'0', '0', 'i', '2', '0', '0'},
                {'b', '0', '0', '2', '0', '0'}
        };
        environment.updatePlayground(turn, environment.getPlayground());
        assertTrue(CLASS_NAME + METHOD_NAME+ "transition turns don't work", arePlaygroundsIdentical(expectedResultingPlayground,environment.getPlayground().getPlayground()));
        assertEquals(CLASS_NAME + METHOD_NAME + "transitions: overrides not decreased", environment.getPlayerByPlayerIcon('2').getRemainingOverrideStones(), NUM_OF_OVERRIDES);
    
        environment.setPlayground(savedPlayground);
    }

    public void testValidateTurn() {
    	String METHOD_NAME = "testValidateTurn: ";
    	
    	testParseRawMap();
    	
    	boolean isTurnOfPlayer1Valid, isTurnOfPlayer2Valid;
    	char[][] mapPlayer1 = new char[PLAYGROUND_HEIGHT][PLAYGROUND_WIDTH];
    	char[][] mapPlayer2 = new char[PLAYGROUND_HEIGHT][PLAYGROUND_WIDTH];
    	
    	Player p1 = environment.getPlayerByPlayerIcon('1');
    	Player p2 = environment.getPlayerByPlayerIcon('2');
    	for(int i = 0; i < PLAYGROUND_HEIGHT*PLAYGROUND_WIDTH; i++) {
    		p1.increaseNumberOfOverrideStones();
    		p2.increaseNumberOfOverrideStones();
    	}
    	
    	
    	char[][] mapPlayer1ValidationValues = new char[][] {
    		{'0', '0', '0', '0', '0', '0'},
    		{'0', '0', '0', '0', '0', '0'},
    		{'0', '0', '1', '1', '1', '1'},
    		{'0', '1', '0', '0', '1', '1'},
    		{'0', '1', '0', '0', '0', '0'},
    		{'0', '0', '1', '0', '0', '0'},
    		{'0', '0', '0', '0', '0', '0'}
    	};
    	
    	
    	char[][] mapPlayer2ValidationValues = new char[][] {
    		{'0', '0', '0', '1', '0', '0'},
    		{'0', '0', '0', '1', '0', '0'},
    		{'0', '0', '1', '0', '0', '0'},
    		{'0', '1', '0', '1', '1', '1'},
    		{'0', '0', '0', '1', '1', '1'},
    		{'0', '0', '0', '1', '0', '1'},
    		{'0', '0', '0', '1', '1', '0'}
    	};
    	
    	// test with overrides
    	Turn turnP1=new Turn('1',0,0,0), turnP2=new Turn('2',0,0,0);
    	for(int row = 0; row < PLAYGROUND_HEIGHT; row++) {
    		for(int col = 0; col < PLAYGROUND_WIDTH; col++) {
//    			turnP1.setRow(row+1); turnP1.setColumn(col+1);
//    			turnP2.setRow(row+1); turnP2.setColumn(col+1);
                turnP1 = new Turn(turnP1.getPlayerIcon(), row, col, turnP1.getSpecialFieldInfo());
                turnP2 = new Turn(turnP2.getPlayerIcon(), row, col, turnP2.getSpecialFieldInfo());
    			isTurnOfPlayer1Valid = environment.getPlayground().validateTurnPhase1(turnP1, p1);
    			isTurnOfPlayer2Valid = environment.getPlayground().validateTurnPhase1(turnP2, p2);
    			if(isTurnOfPlayer1Valid) mapPlayer1[row][col] = '1';
    			else mapPlayer1[row][col] = '0';
    		
    			if(isTurnOfPlayer2Valid) mapPlayer2[row][col] = '1';
   			else mapPlayer2[row][col] = '0';
    		}
    	}   	
    	
    	assertTrue(CLASS_NAME+METHOD_NAME+"turn validation with overrides for player 1 incorrect", arePlaygroundsIdentical(mapPlayer1, mapPlayer1ValidationValues));
    	assertTrue(CLASS_NAME+METHOD_NAME+"turn validation with overrides for player 2 incorrect", arePlaygroundsIdentical(mapPlayer2, mapPlayer2ValidationValues));
 
    	// test without overrides
    	
    	while(p1.getRemainingOverrideStones() > 0) p1.decreaseNumberOfOverrideStones();
    	while(p2.getRemainingOverrideStones() > 0) p2.decreaseNumberOfOverrideStones();
    	
    	for(int row = 0; row < PLAYGROUND_HEIGHT; row++) {
    		for(int col = 0; col < PLAYGROUND_WIDTH; col++) {
//    			turnP1.setRow(row+1); turnP1.setColumn(col+1);
//    			turnP2.setRow(row+1); turnP2.setColumn(col+1);
                turnP1 = new Turn(turnP1.getPlayerIcon(), row, col, turnP1.getSpecialFieldInfo());
                turnP2 = new Turn(turnP2.getPlayerIcon(), row, col, turnP2.getSpecialFieldInfo());
    			isTurnOfPlayer1Valid = environment.getPlayground().validateTurnPhase1(turnP1, p1);
    			isTurnOfPlayer2Valid = environment.getPlayground().validateTurnPhase1(turnP2, p2);
    			if(isTurnOfPlayer1Valid) mapPlayer1[row][col] = '1';
    			else mapPlayer1[row][col] = '0';
    		
    			if(isTurnOfPlayer2Valid) mapPlayer2[row][col] = '1';
   			else mapPlayer2[row][col] = '0';
    		}
    	}
    	
    	mapPlayer1ValidationValues[3][4] = '0';
    	mapPlayer2ValidationValues[0][3] = '0';
    	mapPlayer2ValidationValues[1][3] = '0';
    	mapPlayer2ValidationValues[3][3] = '0';
    	mapPlayer2ValidationValues[3][4] = '0';
    	mapPlayer2ValidationValues[4][3] = '0';
    	mapPlayer2ValidationValues[4][4] = '0';
    	mapPlayer2ValidationValues[5][3] = '0';
    	mapPlayer2ValidationValues[6][3] = '0';  	
    	
    	assertTrue(CLASS_NAME+METHOD_NAME+"turn validation without overrides for player 1 incorrect", arePlaygroundsIdentical(mapPlayer1, mapPlayer1ValidationValues));
    	assertTrue(CLASS_NAME+METHOD_NAME+"turn validation without overrides for player 2 incorrect", arePlaygroundsIdentical(mapPlayer2, mapPlayer2ValidationValues));  
    }
    
    public void testColoringPhase2() {
    	String METHOD_NAME = "TEST_COLORING_PHASE_2: ";
    	
    	String rawMap = "2" + System.lineSeparator() 
    	+ "0" + System.lineSeparator()
    	+ "1 3" + System.lineSeparator()
    	+ "5 5" + System.lineSeparator()
    	+ "1 - 0 1 2 " + System.lineSeparator()
    	+ "- - 2 2 2 " + System.lineSeparator()
    	+ "0 1 0 2 2 " + System.lineSeparator()
    	+ "0 0 0 2 2 " + System.lineSeparator()
    	+ "0 0 0 2 2 " + System.lineSeparator()
    	+ "4 0 2 <-> 4 4 2";
    	environment.parseRawMap(rawMap);
    	/*
    	environment.setPhase(IPhase.BOMB_PHASE);
    	Turn turn = new Turn('1',0,2,0);
    	environment.updatePlayground(turn, environment.getPlayground());
    	
    	char[][] expectedMap = new char[][] {
    		{'1', '-', '-', '-', '-'},
    		{'-', '-', '-', '-', '-'},
    		{'-', '-', '-', '-', '-'},
    		{'-', '-', '-', '-', '-'},
    		{'0', '0', '0', '2', '-'}
    	};
    	environment.getPlayground().printPlayground();
    	assertTrue(CLASS_NAME + METHOD_NAME + "bombphase coloring does not work", arePlaygroundsIdentical(expectedMap, environment.getPlayground().getPlayground()));
    	*/
    }
    
    
    // non-test methods
    
    public Environment getEnvironment() {
        return environment;
    }
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private boolean arePlaygroundsIdentical(char[][] p1, char[][] p2){
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
