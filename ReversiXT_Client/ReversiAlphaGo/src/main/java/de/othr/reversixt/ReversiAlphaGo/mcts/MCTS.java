package de.othr.reversixt.ReversiAlphaGo.mcts;

import de.othr.reversixt.ReversiAlphaGo.agent.ITurnChoiceAlgorithm;
import de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.OutputNeuronalNet;
import de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.PolicyValuePredictor;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;
import de.othr.reversixt.ReversiAlphaGo.general.AlphaGoZeroConstants;
import de.othr.reversixt.ReversiAlphaGo.general.Main;
import org.deeplearning4j.nn.api.Layer;
import org.nd4j.shade.yaml.snakeyaml.scanner.Constant;
import org.opencv.ximgproc.SelectiveSearchSegmentation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static de.othr.reversixt.ReversiAlphaGo.general.Main.LEARNER_MODE;
import static de.othr.reversixt.ReversiAlphaGo.general.Main.QUIET_MODE;
import static de.othr.reversixt.ReversiAlphaGo.general.Main.ONLY_MCTS;


public class MCTS implements ITurnChoiceAlgorithm {

    private static final int NR_SIMULATIONS = 800;
    private static final int NR_VISITED_NODES = 1000;

    private Environment environment;
    private char ourPlayerSymbol;
    private Node root;
    private Node bestNode;
    private boolean firstCall = Boolean.TRUE;

    // Global Variable to save next Node to explore
    private Node bestUCTNode;

    // Training
    private ArrayList<Node> turnHistory = new ArrayList<>();

    // MCTS without NN
    private ArrayList<Node> leafNodes;

    public MCTS(Environment environment) {
        this.environment = environment;
        this.root = new Node(environment.getPlayground().getCloneOfPlayground(), environment.getOurPlayer());
        this.ourPlayerSymbol = environment.getOurPlayer().getSymbol();
    }

    /**
     * getter
     */
    @Override
    public Turn getBestTurn() {
        if (!QUIET_MODE) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            System.out.println("Ende: " + formatter.format(date));
        }
        if (LEARNER_MODE) {
            turnHistory.add(bestNode);
        }
        //bestNode is next root-node of tree
        if (!ONLY_MCTS) {
            setNewRootNode(bestNode);
        }
        Turn bestTurn = bestNode.getCurTurn();
        if (!QUIET_MODE) System.out.println("bestTurn: " + bestTurn.getColumn() + ", " + bestTurn.getRow());
        return bestTurn;
    }

    @Override
    public void chooseTurnPhase1() {
        if (!QUIET_MODE) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            System.out.println("Start: " + formatter.format(date));
        }
        if (ONLY_MCTS) {
            if (!QUIET_MODE) {
                System.out.println("MCTS without NN is started!");
            }
            //create new root node
            //keep existing tree is not implemented
            this.root = new Node(environment.getPlayground().getCloneOfPlayground(), environment.getOurPlayer());
            this.leafNodes = new ArrayList<Node>();
            leafNodes.add(root);
            searchBestTurnWithoutNN();
        } else {
            searchBestTurn();
        }
    }

    @Override
    public void chooseTurnPhase2() {

    }

    /**
     * enemy makes a turn and the next his move will be the next root node
     *
     * @param turn
     */
    @Override
    public void enemyTurn(Turn turn) {
        if (!ONLY_MCTS) {
            int newRootNodeFound = 0;
            for (Node node : root.getChildren()) {
                if (node.getCurTurn().getRow() == turn.getRow() && node.getCurTurn().getColumn() == turn.getColumn()) {
                    System.out.println("RootNode updated");
                    setNewRootNode(node);
                    newRootNodeFound = 1;
                }
            }
            //enemy turn was not explored -> new root node
            if (newRootNodeFound == 0) {
                if (!QUIET_MODE) System.out.println("Discard tree");
                root = new Node(environment.getPlayground().getCloneOfPlayground(), environment.getOurPlayer());
                firstCall = Boolean.TRUE;
            }
        }
    }

    /**
     * set a next turn from current root node to the new root node
     * then removes all leaf nodes from the other next turns from the leaf array
     * Example:   R
     * A   B   C
     * D E   F G   H I
     * Turn A was played in the game
     * -> set A as new root
     * New Tree:   A
     * D E
     *
     * @param nextNode
     */
    private void setNewRootNode(Node nextNode) {
        this.root = nextNode;
    }

    /**
     * if there are no possible moves an empty ArrayList is returned
     *
     * @param playground represents the current game state and holds the current playground
     * @param player     specifies whose turn it is
     * @return an ArrayList of Turns that holds all possible/valid moves that can be played from this game state by this player,
     */
    private ArrayList<Turn> getPossibleTurns(Playground playground, Player player) {
        Turn turn;
        char playerIcon = player.getSymbol();
        ArrayList<Turn> validTurns = new ArrayList<>();
        for (int row = 0; row < playground.getPlaygroundHeight(); row++) {
            for (int col = 0; col < playground.getPlaygroundWidth(); col++) {
                turn = new Turn(playerIcon, row, col, 0);
                if (playground.validateTurnPhase1(turn, player)) {
                    //System.out.println("Valid Turn: row " + row + " col " + col);
                    //if(environment.getPlayground().getSymbolOnPlaygroundPosition(row, col)=='b') turn.setSpecialFieldInfo(21);
                    //if(environment.getPlayground().getSymbolOnPlaygroundPosition(row, col)=='c') turn.setSpecialFieldInfo(player.getSymbol()-'0');
                    validTurns.add(turn);
                    //setBestTurn(turn);
                }
            }
        }
        return validTurns;
    }

    /**
     * choses the best turn of the all our possible turns
     * based on all simulations
     */
    private void setBestTurn() {
        int maxVisited = Integer.MIN_VALUE;
        int nodeVisited;
        for (Node node : root.getChildren()) {
            nodeVisited = node.getNumVisited();
            if (nodeVisited > maxVisited) {
                bestNode = node;
                maxVisited = nodeVisited;
            }
        }
    }

    /**
     * expands the tree with the corresponding child nodes (as possible next moves)
     * and simulates random playouts starting in each child node (which are eventually backpropagated)
     */
    public void searchBestTurn() {
        if (firstCall) {
            if (!QUIET_MODE) {
                System.out.println("create new root node");
            }
            OutputNeuronalNet outputNN = PolicyValuePredictor.getInstance().evaluate(root.getPlayground(), root.getNextPlayer());
            double reward = outputNN.getOutputValueHead().toDoubleVector()[0];
            double[] priors = outputNN.getOutputPolicyHead().toDoubleVector();
            ArrayList<Turn> validTurns = getPossibleTurns(root.getPlayground(), root.getNextPlayer()); //row col
            for (Turn turn : validTurns) {
                int i = turn.getColumn() + turn.getRow() * AlphaGoZeroConstants.DIMENSION_PLAYGROUND;
                turn.setPrior(priors[i]);
            }
            root.setNextTurns(validTurns);
            root.setSimulationReward(reward);
            root.incNumVistited();

            bestUCTNode = root;
            firstCall = Boolean.FALSE;

        } else {
            searchBestUCT(root);
        }
        Node newNode;
        int count = 0;
        while (bestUCTNode != null && count < NR_SIMULATIONS) {
            if (!QUIET_MODE) {
                System.out.println("Nr Simulation: " + count);
            }
            //needed to interrupt thread
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            newNode = createNextNodeAndEvaluate(bestUCTNode);
            backpropagate(newNode);
            setBestTurn();
            searchBestUCT(root);
            count++;
        }
    }

    /**
     * go through the whole tree and looks for next Node to explore further
     * IMPORTANT: Set bestUCT to Double.MIN_VALUE before call searchBestUCT
     *
     * @param node
     */
    private void searchBestUCT(Node node) {
        double nodeUCT;
        double bestUCT = Double.MIN_VALUE;

        for (Node child : node.getChildren()) {
            //Node has unexplored Turns -> can be next bestUCTNode
            nodeUCT = child.calculateUCT();
            if (nodeUCT > bestUCT) {
                bestUCT = nodeUCT;
                bestUCTNode = child;
            }
        }
        if (bestUCTNode != node) {
            searchBestUCT(bestUCTNode);
        }
    }

    /**
     * searches the best turn in all valid moves from board state of node
     *
     * @param node
     * @return turn with best prior
     */
    private Turn choseNextTurn(Node node) {
        double bestPrior = Double.MIN_VALUE;
        Turn bestTurn = null;
        Node nextNode;
        for (Turn turn : node.getNextTurns()) {
            if (turn.getPrior() > bestPrior) {
                bestTurn = turn;
            }
        }
        return bestTurn;
    }

    /**
     * creates a new node in the tree
     * 1) chose next turn with best prior
     * 2) clone playground from chosenNode
     * 3) make the move of 1) -> update a cloned playground
     * 4) evaluate cloned playground with NN
     * 5) search all validTurns of this playground
     * 6) save priors in all turns
     * 7) create new node
     * 8) set this new node as a child of chosenNode (now the new node is in the tree)
     * 9) delete this turn from all turns in chosenNode (no need to make this move again)
     *
     * @param chosenNode
     */
    private Node createNextNodeAndEvaluate(Node chosenNode) {
        Turn nextTurn = choseNextTurn(chosenNode);
        Playground playground = chosenNode.getPlayground().getCloneOfPlayground();
        environment.updatePlayground(nextTurn, playground);
        Player nextPlayer = environment.getNextPlayer(nextTurn.getPlayerIcon());
        OutputNeuronalNet outputNN = PolicyValuePredictor.getInstance().evaluate(playground, nextPlayer);
        double reward = outputNN.getOutputValueHead().toDoubleVector()[0];
        double[] priors = outputNN.getOutputPolicyHead().toDoubleVector();

        if (!QUIET_MODE) {
            System.out.println("predicted reward: " + reward);
            playground.printPlayground();
        }

        ArrayList<Turn> validTurns = getPossibleTurns(playground, nextPlayer); //row col
        for (Turn turn : validTurns) {
            int i = turn.getColumn() + turn.getRow() * AlphaGoZeroConstants.DIMENSION_PLAYGROUND;
            turn.setPrior(priors[i]);
        }

        Node newNode = new Node(playground, chosenNode, nextPlayer, nextTurn, validTurns, reward);

        if (LEARNER_MODE) {
            newNode.setPriorsOfNN(priors);
        }

        chosenNode.getNextTurns().remove(nextTurn);
        chosenNode.getChildren().add(newNode);
        return newNode;
    }

    /**
     * reward of node will be set on each parent
     * each parent will also increase the number of visits
     *
     * @param node
     */
    private void backpropagate(Node node) {
        double reward = node.getSimulationReward();
        //Backpropagate the reward and numVisited to all parents
        Node nodeBP = node.getParent();
        while (nodeBP != root) {
            nodeBP.incNumVistited();
            nodeBP.addReward(reward);
            nodeBP = nodeBP.getParent();
        }
    }

    public ArrayList<Node> getTurnHistory() {
        return turnHistory;
    }

    /********************************************************************************************************/
    /** BEGIN ******************************** WITHOUT NEURONAL NET *****************************************/
    /********************************************************************************************************/

    /**
     * WITHOUT NN
     * <p>
     * expands the tree with the corresponding child nodes (as possible next moves)
     * and simulates random playouts starting in each child node (which are eventually backpropagated)
     */
    public void searchBestTurnWithoutNN() {
        if (!QUIET_MODE) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            System.out.println("Start: " + formatter.format(date));
        }
        //Without NN
        Node chosenNode = root;
        double chosenNodeUCT;
        double nodeUCT;
        int count = 0;
        while (!leafNodes.isEmpty() && count < NR_VISITED_NODES) {
            count++;
            //Check if Thread was interrupted
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            //search best UCT in every loop of while
            chosenNodeUCT = Double.MIN_VALUE;
            //System.out.println("LeafNodeSize: " + leafNodes.size());
            expand(chosenNode);
            //System.out.println("LeafNodeSize after expand: " + leafNodes.size());
            traverse(chosenNode);
            //System.out.println("LeafNodeSize after traverse: " + leafNodes.size());
            setBestTurn();
            //System.out.println("SetBestTurn finished!");
            //chose the next node which should be explored
            for (Node node : leafNodes) {
                //You can optimize here!!
                nodeUCT = (node.getSimulationReward() / Double.valueOf(node.getNumVisited()));
                //nodeUCT = node.calculateUCT();
                if (nodeUCT > chosenNodeUCT) {
                    chosenNodeUCT = nodeUCT;
                    chosenNode = node;
                    if (!QUIET_MODE) {
                        System.out.println("Best UCT: " + chosenNodeUCT);
                    }
                }
            }
        }
    }

    /**
     * WITHOUT NN
     * <p>
     * from each child node random playouts (meaning choosing random moves until and end state is reached) are simulated
     * when there are no more possible moves the end state is reached and the reward for this outcome is calculated
     * eventually the simulation results are backpropagated to the root node (number how often the node was visited and the the simulation reward are updated)
     */
    private void traverse(Node traverseNode) {
        for (Node child : traverseNode.getChildren()) {
            //clone map to "complete" the map
            Playground playground = child.getPlayground().getCloneOfPlayground();
            //Simulate one path to get a reward
            child.setSimulationReward(simulate(playground, child.getNextPlayer()));
            backpropagate(child);
        }
    }

    /**
     * WITHOUT NN
     * <p>
     * calculates the reward as counting the stones one the playground from the corresponding player
     *
     * @param playground represents the current game state and holds the current playground
     * @return the calculated reward (int)
     */
    private int rewardGameState(Playground playground) {
        //reward = count all our stones
        int reward = 0;

        for (int x = 0; x < playground.getPlaygroundHeight(); x++) {
            for (int y = 0; y < playground.getPlaygroundWidth(); y++) {
                if (playground.getSymbolOnPlaygroundPosition(y, x) == this.ourPlayerSymbol) {
                    reward++;
                }
            }
        }
        //System.out.println("The reward is " + reward);
        return reward;
    }

    /**
     * WITHOUT NN
     * <p>
     * function to simulate the full game by playing random moves till the end
     *
     * @param playground represents a cloned map which will calculated till end
     * @param currPlayer represents the current player
     * @return the reward of the simulated game
     */
    private double simulate(Playground playground, Player currPlayer) {
        if (!QUIET_MODE) {
            System.out.println("Simulate with Start Player: " + currPlayer.getSymbol());
            playground.printPlayground();
        }
        ArrayList<Turn> possTurns = getPossibleTurns(playground, currPlayer);
        //
        while (!possTurns.isEmpty()) {
            // determine next random turn and update playground
            int index = new Random().nextInt(possTurns.size());
            environment.updatePlayground(possTurns.get(index), playground);
            currPlayer = environment.getNextPlayer(currPlayer.getSymbol());
            possTurns = getPossibleTurns(playground, currPlayer);
        }
        return (double) rewardGameState(playground);
    }

    /**
     * WITHOUT NN
     * <p>
     * expand the tree such that all possible next moves are added as child nodes for the current node
     * each child receives their own deep copy of an environment
     * the map is updated according to the possible move
     * also the unvisited child nodes are added to the list of leaf nodes
     */
    private void expand(Node expandNode) {
        if (!QUIET_MODE) {
            System.out.println("Expand Playground with next Player: " + expandNode.getNextPlayer().getSymbol());
            expandNode.getPlayground().printPlayground();
        }
        ArrayList<Turn> possibleTurns = getPossibleTurns(expandNode.getPlayground(), expandNode.getNextPlayer());
        //for each Turn:
        // clone the playground and make a single turn -> create newNode with updated map
        // insert newNode in children of expandNode and in leafNodes
        for (Turn turn : possibleTurns) {
            Playground playground = expandNode.getPlayground().getCloneOfPlayground();
            environment.updatePlayground(turn, playground);
            Node child = new Node(playground, expandNode, environment.getNextPlayer(turn.getPlayerIcon()), turn, new ArrayList<Turn>(), 0);
            expandNode.getChildren().add(child);
            leafNodes.add(child);
            if (!QUIET_MODE) {
                System.out.println("Leaf Node added: " + child.getNextPlayer().getSymbol());
                child.getPlayground().printPlayground();
            }
        }
        //expandNode is expanded and can be removed from Leaf Nodes
        //expandNode is not included in UCT anymore
        leafNodes.remove(expandNode);
    }

    /********************************************************************************************************/
    /** END ********************************** WITHOUT NEURONAL NET *****************************************/
    /********************************************************************************************************/

    /**
     * counts the number of stones on the playground for the corresponding player
     * checks if we have the most stones and return the result below
     *
     * @param playground represents the current game state and holds the current playground
     * @return the game result: 0 for a draw, -1 for a loss, +1 for winning
     */

    public int rewardGame(Playground playground) {

        //initialize an array of integer for each player
        int[] stonesOfPlayer = new int[environment.getNumOfPlayers()];
        for (int i = 0; i < environment.getNumOfPlayers(); i++) {
            stonesOfPlayer[i] = 0;
        }

        //count all stones on the playground for each player
        char playgroundSymbol;
        int playerIndex;
        for (int x = 0; x < playground.getPlaygroundHeight(); x++) {
            for (int y = 0; y < playground.getPlaygroundWidth(); y++) {
                playgroundSymbol = playground.getSymbolOnPlaygroundPosition(y, x);
                playerIndex = (playgroundSymbol - 49); //Player 1 -> index 0; Player 2 -> index 1
                if (playerIndex < environment.getNumOfPlayers() && playerIndex >= 0) {
                    stonesOfPlayer[playerIndex]++;
                }
            }
        }

        //check if our stone count is the highest
        int ourIndex = (environment.getOurPlayer().getSymbol() - 49);
        int ourNumOfStones = stonesOfPlayer[ourIndex];
        int sameNumOfStones = 0; //check if a player has the same reward
        for (int j = 0; j < environment.getNumOfPlayers(); j++) {
            if (!(j == ourIndex)) {
                if (stonesOfPlayer[j] > ourNumOfStones) {
                    return AlphaGoZeroConstants.GAME_LOST;
                }
                if (stonesOfPlayer[j] == ourNumOfStones) {
                    sameNumOfStones++;
                }
            }
        }

        //no player has more stones then our player
        //so chose if we have a draw or won
        if (sameNumOfStones == 0) {
            return AlphaGoZeroConstants.GAME_WON;
        } else {
            return AlphaGoZeroConstants.GAME_DRAWN;
        }
    }

}
