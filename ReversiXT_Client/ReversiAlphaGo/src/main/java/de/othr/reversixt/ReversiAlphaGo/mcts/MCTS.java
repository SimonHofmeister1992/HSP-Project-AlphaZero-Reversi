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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static de.othr.reversixt.ReversiAlphaGo.general.Main.LEARNER_MODE;
import static de.othr.reversixt.ReversiAlphaGo.general.Main.QUIET_MODE;


public class MCTS implements ITurnChoiceAlgorithm {

    private Environment environment;
    private char ourPlayerSymbol;
    private Node root;
    private Node bestNode;
    private boolean firstCall = Boolean.TRUE;

    // Global Variable to save next Node to explore
    private Node bestUCTNode;

    // Training
    private ArrayList<Node> turnHistory = new ArrayList<>();


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
        setNewRootNode(bestNode);
        Turn bestTurn = bestNode.getCurTurn();
        System.out.println("bestTurn: " + bestTurn.getColumn() + ", " + bestTurn.getRow());
        return bestTurn;
    }

    @Override
    public void chooseTurnPhase1() {
        searchBestTurn();
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
        int newRootNodeFound = 0;
        for (Node node : root.getChildren()) {
            if (node.getCurTurn().getRow() == turn.getRow() && node.getCurTurn().getColumn() == turn.getColumn()) {
                System.out.println("New rootNode found");
                setNewRootNode(node);
                newRootNodeFound = 1;
            }
        }
        //enemy turn was not explored -> new root node
        if (newRootNodeFound == 0) {
            System.out.println("New rootNode was not found -> create new Tree");
            root = new Node(environment.getPlayground().getCloneOfPlayground(), environment.getOurPlayer());
            firstCall = Boolean.TRUE;
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
        if (!QUIET_MODE) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            System.out.println("Start: " + formatter.format(date));
        }

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
        while (bestUCTNode != null) {

            //needed to interrupt thread
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (!QUIET_MODE) {
                System.out.println("-- create next Node and evaluate --");
            }

            newNode = createNextNodeAndEvaluate(bestUCTNode);
            backpropagate(newNode);
            setBestTurn();
            searchBestUCT(root);
        }
    }

    /**
     * DEPRECATED
     * <p>
     * expands the tree with the corresponding child nodes (as possible next moves)
     * and simulates random playouts starting in each child node (which are eventually backpropagated)
     */
    public void searchBestTurn_deprecated() {
        if (!QUIET_MODE) {
            SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
            Date date = new Date(System.currentTimeMillis());
            System.out.println("Start: " + formatter.format(date));
        }
        int countExpands = 0;
        Node chosenNode = root;
        double chosenNodeUCT;
        double nodeUCT;
        double reward;

        if (!QUIET_MODE) {
            System.out.println("-- evaluateRootLeaf --");
        }
        evaluateLeaf(chosenNode); //expand
        //while (!leafNodes.isEmpty()) {
        if (Thread.currentThread().isInterrupted()) {
            return;
        }
        chosenNodeUCT = Double.MIN_VALUE;

        for (Node child : chosenNode.getChildren()) {
            if (!QUIET_MODE) {
                System.out.println("-- evaluateLeaf --");
            }
            reward = evaluateLeaf(child); //call NeuronalNet
            //backpropagate(child, reward);
            setBestTurn();
        }

        //set bestUCTNode
            /*for (Node node : leafNodes) {
                nodeUCT = node.calculateUCT();
                if (nodeUCT > chosenNodeUCT) {
                    chosenNodeUCT = nodeUCT;
                    chosenNode = node;
                }
            }*/

        if (!QUIET_MODE) {
            System.out.println("Next chosen Turn; row: " + chosenNode.getCurTurn().getRow() + " col: " + chosenNode.getCurTurn().getColumn());
        }
        //}
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
        OutputNeuronalNet outputNN = PolicyValuePredictor.getInstance().evaluate(playground, environment.getNextPlayer(nextTurn.getPlayerIcon()));
        double reward = outputNN.getOutputValueHead().toDoubleVector()[0];
        double[] priors = outputNN.getOutputPolicyHead().toDoubleVector();

        if (!QUIET_MODE) {
            System.out.println("reward: " + reward);
            playground.printPlayground();
        }

        ArrayList<Turn> validTurns = getPossibleTurns(chosenNode.getPlayground(), chosenNode.getNextPlayer()); //row col
        for (Turn turn : validTurns) {
            int i = turn.getColumn() + turn.getRow() * AlphaGoZeroConstants.DIMENSION_PLAYGROUND;
            turn.setPrior(priors[i]);
        }

        Node newNode = new Node(playground, chosenNode, environment.getNextPlayer(nextTurn.getPlayerIcon()), nextTurn, validTurns, reward);

        if (LEARNER_MODE) {
            newNode.setPriorsOfNN(priors);
            newNode.setUnchangedRewardNN(reward);
        }

        chosenNode.getNextTurns().remove(nextTurn);
        chosenNode.getChildren().add(newNode);
        return newNode;
    }

    /**
     * DEPRECATED
     * <p>
     * hand over the leaf node to the neuronal net to evaluate game state
     * reward represents the evaluation of the current game state, i.e. the probability of winning being in the current
     * state
     * to consider only valid turns (instead of all playground positions) the possible turns are obtained
     * priors array holds the prior probabilities for every move/for every position in the playground,
     * if it is a valid turn a child node is created and the corresponding prior is saved
     * otherwise it is not a valid move, therefore it is not added to the list of child nodes
     * priors is a one dimensional array representing the playground (a two dimensional array), therefore row and col
     * are calculated using the value for the dimension of the playground deposited in AlphaGoZeroConstants
     * <p>
     * eventually the new child node is added to the array of leaf nodes and the node received as a parameter
     * (chosenNode) is removed from it
     *
     * @param chosenNode is the node to be evaluated
     */
    private double evaluateLeaf(Node chosenNode) {
        Playground playground = chosenNode.getPlayground().getCloneOfPlayground();
        OutputNeuronalNet outputNN = PolicyValuePredictor.getInstance().evaluate(playground, chosenNode.getNextPlayer());
        double reward = outputNN.getOutputValueHead().toDoubleVector()[0];
        double[] priors = outputNN.getOutputPolicyHead().toDoubleVector();

        if (!QUIET_MODE) {
            System.out.println("reward: " + reward);
            playground.printPlayground();
        }

        ArrayList<Turn> validTurns = getPossibleTurns(chosenNode.getPlayground(), chosenNode.getNextPlayer()); //row col
        for (Turn turn : validTurns) {
            int i = turn.getColumn() + turn.getRow() * AlphaGoZeroConstants.DIMENSION_PLAYGROUND;
            turn.setPrior(priors[i]);
        }

        for (Turn turn : validTurns) {
            int i = turn.getColumn() + turn.getRow() * AlphaGoZeroConstants.DIMENSION_PLAYGROUND;
            playground = chosenNode.getPlayground().getCloneOfPlayground();
            environment.updatePlayground(turn, playground);
            //Node child = new Node(playground, chosenNode, environment.getNextPlayer(turn.getPlayerIcon()), turn);
            //chosenNode.getChildren().add(child);
            //leafNodes.add(child);
        }

        //leafNodes.remove(chosenNode);
        return reward;
    }

    /**
     * DEPRECATED
     * <p>
     * from each child node random playouts (meaning choosing random moves until and end state is reached) are simulated
     * when there are no more possible moves the end state is reached and the reward for this outcome is calculated
     * eventually the simulation results are backpropagated to the root node (number how often the node was visited and the the simulation reward are updated)
     */
    private void traverse(Node traverseNode) {

        if(LEARNER_MODE){  // parallel execution on all cores
            traverseNode.getChildren().parallelStream().forEach(child -> {

                //clone map to "complete" the map
                Playground playground = child.getPlayground().getCloneOfPlayground();
                //Simulate one path to get a reward
                double reward = simulate(playground, child.getNextPlayer());

                //Backpropagate the reward and numVisited to all parents
                Node nodeBP = child;
                while (nodeBP != root) {
                    nodeBP.setNumVisited(nodeBP.getNumVisited() + 1);
                    //TODO save current player?
                    if (nodeBP.getParent().getNextPlayer() == child.getParent().getNextPlayer()) {
                        nodeBP.setSimulationReward(nodeBP.getSimulationReward() + reward);
                    } else {
                        nodeBP.setSimulationReward(nodeBP.getSimulationReward() - reward);
                    }
                    nodeBP = nodeBP.getParent();
                }

            });
        }

        else{  // iterative execution
            for (Node child : traverseNode.getChildren()) {
                //clone map to "complete" the map
                Playground playground = child.getPlayground().getCloneOfPlayground();
                //Simulate one path to get a reward
                double reward = simulate(playground, child.getNextPlayer());

                //Backpropagate the reward and numVisited to all parents
                Node nodeBP = child;
                while (nodeBP != root) {
                    nodeBP.setNumVisited(nodeBP.getNumVisited() + 1);
                    //TODO save current player?
                    if (nodeBP.getParent().getNextPlayer() == child.getParent().getNextPlayer()) {
                        nodeBP.setSimulationReward(nodeBP.getSimulationReward() + reward);
                    } else {
                        nodeBP.setSimulationReward(nodeBP.getSimulationReward() - reward);
                    }
                    nodeBP = nodeBP.getParent();
                }
            }
        }

    }

    /**
     * DEPRECATED
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
     * DEPRECATED
     * <p>
     * function to simulate the full game by playing random moves till the end
     *
     * @param playground represents a cloned map which will calculated till end
     * @param currPlayer represents the current player
     * @return the reward of the simulated game
     */
    private double simulate(Playground playground, Player currPlayer) {
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
     * DEPRECATED
     * <p>
     * expand the tree such that all possible next moves are added as child nodes for the current node
     * each child receives their own deep copy of an environment
     * the map is updated according to the possible move
     * also the unvisited child nodes are added to the list of leaf nodes
     */
    private void expand(Node expandNode) {
        ArrayList<Turn> possibleTurns = getPossibleTurns(expandNode.getPlayground(), expandNode.getNextPlayer());
        //for each Turn:
        // clone the playground and make a single turn -> create newNode with updated map
        // insert newNode in children of expandNode and in leafNodes
        for (Turn turn : possibleTurns) {
            Playground playground = expandNode.getPlayground().getCloneOfPlayground();
            environment.updatePlayground(turn, playground);
            //Node child = new Node(playground, expandNode, environment.getNextPlayer(turn.getPlayerIcon()), turn, 1);
            //expandNode.getChildren().add(child);
            //leafNodes.add(child);
        }
        //expandNode is expanded and can be removed from Leaf Nodes
        //expandNode is not included in UCT anymore
        //leafNodes.remove(expandNode);
    }

    public ArrayList<Node> getTurnHistory() {
        return turnHistory;
    }
}
