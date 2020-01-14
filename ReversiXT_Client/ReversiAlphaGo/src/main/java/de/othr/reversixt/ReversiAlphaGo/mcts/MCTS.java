package de.othr.reversixt.ReversiAlphaGo.mcts;

import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;

import java.util.ArrayList;
import java.util.Random;


public class MCTS {

    private Node root;
    private ArrayList<Node> leafNodes;
    private Player myPlayer;

    public MCTS(Environment environment, Player player) {
        this.root = new Node(environment, player);
        this.leafNodes = new ArrayList<Node>();
        leafNodes.add(root);
        this.myPlayer = player;
    }

    //TODO copied from RandomTurnChoiceAlgorithm

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
        //TODO find next player
        for(int idx = 0; idx < nodeEnvironment.getNumOfPlayers(); idx++) {
            //find next player
            //if getPossibleTurns(nodeEnvironment, nextPlayer) empty --> continue
            //if not empty --> return nextPlayer
        }
        return currentPlayer;
    }

    /**
     * expands the tree with the corresponding child nodes (as possible next moves)
     * and simulates random playouts starting in each child node (which are eventually backpropagated)
     */
    public void searchBestTurn() {
        Node chosenNode = root;
        double chosenNodeUCT = double.min_value;
        double nodeUCT;
        while(!leafNodes.isEmpty()) {
            expand(chosenNode);
            traverse(chosenNode);
            //chose the next node which should be explored
            for (Node node : leafNodes) {
                nodeUCT = node.calculateUCT;
                if(nodeUCT > chosenNodeUCT) {
                    chosenNodeUCT = nodeUCT;
                    chosenNode = node;
                }
            }
        }
    }

    /**
     * from each child node random playouts (meaning choosing random moves until and end state is reached) are simulated
     * when there are no more possible moves the end state is reached and the reward for this outcome is calculated
     * eventually the simulation results are backpropagated to the root node (number how often the node was visited and the the simulation reward are updated)
     */
    //ToDo parameter root
    private void traverse(Node traverseNode) {
        for (Node child : traverseNode.getChildren()) {
            Environment nodeEnvironment;
            try {
                nodeEnvironment = (Environment) traverseNode.getEnvironment().clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return;
            }
            //Simulate one path to get a reward
            double reward = simulate(nodeEnvironment, child.getNextPlayer());
            //Backpropagate the reward anv visitedNum to all Parents (except root)
            Node nodeBP = child;
            while(nodeBP != root) {
                nodeBP.setNumVisited(nodeBP.getNumVisited() + 1);
                nodeBP.setSimulationReward(nodeBP.getSimulationReward() + reward);
                nodeBP = child.getParent();
            }
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
     * @param nodeEnv represents the environment for the node
     * @param currPlayer represents the current player
     * @return the reward of the simulated game
     */
    private double simulate(Environment nodeEnv,  Player currPlayer) {
        ArrayList<Turn> possTurns = getPossibleTurns(nodeEnv, currPlayer);
        // exit condition of recursion
        while(!possTurns.isEmpty()) {
            // determine next random turn
            int index = new Random().nextInt() % possTurns.size();
            nodeEnv.updatePlayground(possTurns.get(index));
            // create new child for the chosen turn
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
        //get all possibleTurns for expandNode
        ArrayList<Turn> possibleTurns = getPossibleTurns(expandNode.getEnvironment(), expandNode.getNextPlayer());
        //for each Turn, clone the environment and add the node as children to expandNode
        for (Turn turn : possibleTurns) {
            Environment nodeEnvironment;
            try {
                nodeEnvironment = (Environment) expandNode.getEnvironment().clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return;
            }
            nodeEnvironment.updatePlayground(turn);
            Node child = new Node(nodeEnvironment, expandNode, getNextPlayer(nodeEnvironment, expandNode.getNextPlayer()));
            expandNode.getChildren().add(child);
            leafNodes.add(child);
        }
        //this arraylist saves all nodes for uct
        leafNodes.remove(expandNode);
    }

}
