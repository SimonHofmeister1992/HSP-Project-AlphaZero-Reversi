package de.othr.reversixt.ReversiAlphaGo.mcts;

import de.othr.reversixt.ReversiAlphaGo.agent.ITurnChoiceAlgorithm;
import de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.OutputNeuronalNet;
import de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet.PolicyValuePredictor;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;
import de.othr.reversixt.ReversiAlphaGo.general.AlphaGoZeroConstants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import static de.othr.reversixt.ReversiAlphaGo.general.Main.QUIET_MODE;


public class MCTS implements ITurnChoiceAlgorithm {

    private Environment environment;
    private char ourPlayerSymbol;
    private Node root;
    private ArrayList<Node> leafNodes;
    private Node bestNode;
    private ArrayList<Node> turnHistory = new ArrayList<>();

    public MCTS(Environment environment) {
        this.environment = environment;
        this.root = new Node(environment.getPlayground().getCloneOfPlayground(), environment.getOurPlayer());
        this.leafNodes = new ArrayList<Node>();
        this.ourPlayerSymbol = environment.getOurPlayer().getSymbol();
        leafNodes.add(root);
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
        System.out.println("bestTurn: " + bestNode.getCurTurn().getColumn() + ", " + bestNode.getCurTurn().getRow());
        turnHistory.add(bestNode);
        return bestNode.getCurTurn();
    }

    @Override
    public void chooseTurnPhase1() {
        searchBestTurn();
    }

    @Override
    public void chooseTurnPhase2() {

    }

    /**
     * @param playground represents the current game state and holds the current playground
     * @param player     specifies whose turn it is
     * @return an ArrayList of Turns that holds all possible/valid moves that can be played from this game state by this player,
     * if there are no possible moves an empty ArrayList is returned
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
        Node chosenNode = root;
        double chosenNodeUCT;
        double nodeUCT;
        double reward;

        if (!QUIET_MODE) {
            System.out.println("-- evaluateRootLeaf --");
        }
        evaluateLeaf(chosenNode); //expand
        while (!leafNodes.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            chosenNodeUCT = Double.MIN_VALUE;

            for (Node child : chosenNode.getChildren()) {
                if (!QUIET_MODE) {
                    System.out.println("-- evaluateLeaf --");
                }
                reward = evaluateLeaf(child); //call NeuronalNet
                backpropagate(child, reward);
                setBestTurn();
            }

            for (Node node : leafNodes) {
                nodeUCT = node.calculateUCT();
                if (nodeUCT > chosenNodeUCT) {
                    chosenNodeUCT = nodeUCT;
                    chosenNode = node;
                }
            }
            if (!QUIET_MODE) {
                System.out.println("Next chosen Turn; row: " + chosenNode.getCurTurn().getRow() + " col: " + chosenNode.getCurTurn().getColumn());
            }
        }

    }

    private void backpropagate(Node node, double reward) {
        //Backpropagate the reward and numVisited to all parents
        Node nodeBP = node;
        while (nodeBP != root) {
            nodeBP.setNumVisited(nodeBP.getNumVisited() + 1);
            nodeBP.setSimulationReward(nodeBP.getSimulationReward() + reward);
            nodeBP = nodeBP.getParent();
        }
    }

    /**
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
        //System.out.println("priors: " + Arrays.toString(priors));
        //System.out.println("priors length: " + priors.length);
        ArrayList<Turn> validTurns = getPossibleTurns(chosenNode.getPlayground(), chosenNode.getNextPlayer()); //row col

        for (Turn turn : validTurns) {
            int i = turn.getColumn() + turn.getRow() * AlphaGoZeroConstants.DIMENSION_PLAYGROUND;
            playground = chosenNode.getPlayground().getCloneOfPlayground();
            environment.updatePlayground(turn, playground);
            Node child = new Node(playground, chosenNode, environment.getNextPlayer(turn.getPlayerIcon()), turn, priors[i]);
            chosenNode.getChildren().add(child);
            leafNodes.add(child);
        }

        leafNodes.remove(chosenNode);
        return reward;
    }

    /** DEPRECATED
     *
     * from each child node random playouts (meaning choosing random moves until and end state is reached) are simulated
     * when there are no more possible moves the end state is reached and the reward for this outcome is calculated
     * eventually the simulation results are backpropagated to the root node (number how often the node was visited and the the simulation reward are updated)
     */
    private void traverse(Node traverseNode) {
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

    /** DEPRECATED
     *
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

    /** DEPRECATED
     *
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

    /** DEPRECATED
     *
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
            Node child = new Node(playground, expandNode, environment.getNextPlayer(turn.getPlayerIcon()), turn, 1);
            expandNode.getChildren().add(child);
            leafNodes.add(child);
        }
        //expandNode is expanded and can be removed from Leaf Nodes
        //expandNode is not included in UCT anymore
        leafNodes.remove(expandNode);
    }


}
