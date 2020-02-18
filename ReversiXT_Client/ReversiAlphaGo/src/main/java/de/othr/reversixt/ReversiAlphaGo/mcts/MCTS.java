package de.othr.reversixt.ReversiAlphaGo.mcts;

import de.othr.reversixt.ReversiAlphaGo.agent.ITurnChoiceAlgorithm;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


public class MCTS implements ITurnChoiceAlgorithm {

    private Node root;
    private ArrayList<Node> leafNodes;
    private Player myPlayer;
    private Node bestNode;

    public MCTS(Environment environment, Player player) {
        this.root = new Node(environment, player);
        this.leafNodes = new ArrayList<Node>();
        leafNodes.add(root);
        this.myPlayer = player;
    }

    /**
     * getter
     */
    @Override
    public Turn getBestTurn() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        System.out.println("Ende: " + formatter.format(date));
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
     * @param environment represents the current game state and holds the current playground
     * @param player      specifies whose turn it is
     * @return an ArrayList of Turns that holds all possible/valid moves that can be played from this game state by this player,
     * if there are no possible moves an empty ArrayList is returned
     */
    private ArrayList<Turn> getPossibleTurns(Environment environment, Player player) {
        Turn turn;
        Turn turnToCheck = new Turn(player.getSymbol(), 0, 0, 0);
        ArrayList<Turn> validTurns = new ArrayList<>();
        for (int row = 0; row < environment.getPlayground().getPlaygroundHeight(); row++) {
            for (int col = 0; col < environment.getPlayground().getPlaygroundWidth(); col++) {
                turnToCheck.setRow(row);
                turnToCheck.setColumn(col);

                if (environment.validateTurnPhase1(turnToCheck)) {
                    //System.out.println("Valid Turn: row " + row + " col " + col);
                    turn = new Turn(player.getSymbol(), row, col, 0);
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
     * determines whose turn is next
     *
     * @param currentPlayer implies whose turn it was now
     * @return player: specifies which player is next
     */
    private Player getNextPlayer(Environment nodeEnvironment, Player currentPlayer) {
        int curSymbol = currentPlayer.getSymbol();
        int nextSymbol = ((curSymbol - 49 + 1) % nodeEnvironment.getNumOfPlayers() ) + 49;
        char nextSymbolChar = (char) nextSymbol;
        for(Player player : nodeEnvironment.getPlayers()) {
            if(player.getSymbol() == nextSymbolChar) {
                //System.out.println("Nex Player Symbol: " + nextSymbolChar);
                return player;
            }
        }
        return currentPlayer; //should not occur
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
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        System.out.println("Start: " + formatter.format(date));
        Node chosenNode = root;
        double chosenNodeUCT;
        double nodeUCT;
        while (!leafNodes.isEmpty()) {
            System.out.println("LeafNodeSize before expand: " + leafNodes.size());
            chosenNodeUCT = Double.MIN_VALUE;
            expand(chosenNode);
            System.out.println("LeafNodeSize after expand: " + leafNodes.size());
            traverse(chosenNode);
            System.out.println("LeafNodeSize after traverse: " + leafNodes.size());
            setBestTurn();
            System.out.println("SetBestTurn finished!");
            //chose the next node which should be explored
            for (Node node : leafNodes) {
                nodeUCT = node.calculateUCT();
                if (nodeUCT > chosenNodeUCT) {
                    chosenNodeUCT = nodeUCT;
                    chosenNode = node;
                }
            }
            //break;
        }
    }

    /**
     * from each child node random playouts (meaning choosing random moves until and end state is reached) are simulated
     * when there are no more possible moves the end state is reached and the reward for this outcome is calculated
     * eventually the simulation results are backpropagated to the root node (number how often the node was visited and the the simulation reward are updated)
     */
    //ToDo parameter root
    private void traverse(Node traverseNode) {
        System.out.println("Start Traverse");
        System.out.println("Node children size: " + traverseNode.getChildren().size());
        for (Node child : traverseNode.getChildren()) {
            Environment nodeEnvironment;
            try {
                nodeEnvironment = (Environment) child.getEnvironment().clone();
                nodeEnvironment.setPlayground(child.getEnvironment().getPlayground().getCloneOfPlayground());
                System.out.println("Map cloned");
                //nodeEnvironment.getPlayground().printPlayground();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                System.out.println("Clone Exception");
                return;
            }
            //Simulate one path to get a reward
            System.out.println("Simulate started");
            double reward = simulate(nodeEnvironment, child.getNextPlayer());
            System.out.println("Simulate finished");
            //Backpropagate the reward anv visitedNum to all Parents (except root)
            System.out.println("Backpropagation started");
            Node nodeBP = child;
            while (nodeBP != root) {
                nodeBP.setNumVisited(nodeBP.getNumVisited() + 1);
                nodeBP.setSimulationReward(nodeBP.getSimulationReward() + reward);
                nodeBP = nodeBP.getParent();
            }
            System.out.println("Backpropagation finished");
        }
    }

    /**
     * calculates the reward as counting the stones one the playground from the corresponding player
     *
     * @param environment represents the current game state and holds the current playground
     * @return the calculated reward (int)
     */
    private int rewardGameState(Environment environment) {
        //reward = count all our stones
        int reward = 0;
        for (int x = 0; x < environment.getPlayground().getPlaygroundHeight(); x++) {
            for (int y = 0; y < environment.getPlayground().getPlaygroundWidth(); y++) {
                if (environment.getPlayground().getSymbolOnPlaygroundPosition(y, x) == myPlayer.getSymbol()) {
                    reward++;
                }
            }
        }
        System.out.println("The reward is " + reward);
        return reward;
    }

    /**
     * function to simulate the full game by playing random moves till the end
     *
     * @param nodeEnv    represents the environment for the node
     * @param currPlayer represents the current player
     * @return the reward of the simulated game
     */
    private double simulate(Environment nodeEnv, Player currPlayer) {
        ArrayList<Turn> possTurns = getPossibleTurns(nodeEnv, currPlayer);
        // exit condition of recursion
        while (!possTurns.isEmpty()) {
            // determine next random turn
            int index = new Random().nextInt(possTurns.size());
            nodeEnv.updatePlayground(possTurns.get(index));
            currPlayer = getNextPlayer(nodeEnv, currPlayer);
            possTurns = getPossibleTurns(nodeEnv, currPlayer);
        }
        return (double) rewardGameState(nodeEnv);
    }

    /**
     * expand the tree such that all possible next moves are added as child nodes for the current node
     * each child receives their own deep copy of an environment
     * the map is updated according to the possible move
     * also the unvisited child nodes are added to the list of leaf nodes
     */
    private void expand(Node expandNode) {
        System.out.println("Start Expand");
        //get all possibleTurns for expandNode
        ArrayList<Turn> possibleTurns = getPossibleTurns(expandNode.getEnvironment(), expandNode.getNextPlayer());
        //for each Turn, clone the environment and add the node as children to expandNode
        for (Turn turn : possibleTurns) {
            System.out.println("-- new Node --");
            Environment nodeEnvironment;
            try {
                nodeEnvironment = (Environment) expandNode.getEnvironment().clone();
                nodeEnvironment.setPlayground(expandNode.getEnvironment().getPlayground().getCloneOfPlayground());
                System.out.println("Map cloned");
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                System.out.println("Clone Exception");
                return;
            }
            nodeEnvironment.updatePlayground(turn);
            Node child = new Node(nodeEnvironment, expandNode, getNextPlayer(nodeEnvironment, expandNode.getNextPlayer()), turn);
            expandNode.getChildren().add(child);
            leafNodes.add(child);
        }
        //this arraylist saves all nodes for uct
        leafNodes.remove(expandNode);
        System.out.println("Removed Node form leaf Nodes");
    }


}
