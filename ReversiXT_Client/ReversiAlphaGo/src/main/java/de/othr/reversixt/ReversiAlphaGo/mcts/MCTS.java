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
                //print gameboard
                //System.out.print(game[x][y]);
                if (environment.getPlayground().getSymbolOnPlaygroundPosition(y, x) == myPlayer.getSymbol()) {
                    reward++;
                }
            }
            //print gameboard
            //System.out.println('|');
        }
        System.out.println("The reward is " + reward);
        return reward;
    }

    /**
     * determines whose turn is next
     *
     * @param currentPlayer implies whose turn it was now
     * @return player: specifies which player is next
     */
    private Player getNextPlayer(Player currentPlayer) {
        //TODO find next player
        return currentPlayer;
    }

    /**
     * expands the tree with the corresponding child nodes (as possible next moves)
     * and simulates random playouts starting in each child node (which are eventually backpropagated)
     */
    public void searchBestTurn() {
        expand();
        traverse();
    }

    /**
     * from each child node random playouts (meaning choosing random moves until and end state is reached) are simulated
     * when there are no more possible moves the end state is reached and the reward for this outcome is calculated
     * eventually the simulation results are backpropagated to the root node (number how often the node was visited and the the simulation reward are updated)
     */
    private void traverse() {
        for (Node child : root.getChildren()) {
            Environment nodeEnvironment;
            try {
                nodeEnvironment = (Environment) root.getEnvironment().clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return;
            }
            Player nextPlayer = child.getNextPlayer();
            root.setSimulationReward(root.getSimulationReward()
                    + simulate(child, getPossibleTurns(nodeEnvironment, nextPlayer), nodeEnvironment, nextPlayer));
            root.setNumVisited(root.getNumVisited() + 1);
        }
    }

    /**
     * recursive function to simulate game by playing random moves
     * @param currNode represents the current node
     * @param possTurns represents all possible turns in the current game state
     * @param nodeEnv represents the environment for the node
     * @param currPlayer represents the current player
     * @return the reward of the simulated game
     */
    private double simulate(
            Node currNode,
            ArrayList<Turn> possTurns,
            Environment nodeEnv,
            Player currPlayer) {
        // exit condition of recursion
        if (possTurns.isEmpty()) {
            currNode.setNumVisited(currNode.getNumVisited() + 1);
            currNode.setSimulationReward(currNode.getSimulationReward() + rewardGameState(nodeEnv));
            return (double) rewardGameState(nodeEnv);
        }
        // determine next random turn
        int index = new Random().nextInt() % possTurns.size();
        nodeEnv.updatePlayground(possTurns.get(index));
        // create new child for the chosen turn
        Player nextPlayer = getNextPlayer(currPlayer);
        Node simulationNode = new Node(nodeEnv, currNode, nextPlayer);
        // update the values of the current node
        currNode.setNumVisited(currNode.getNumVisited() + 1);
        currNode.setSimulationReward(
                simulationNode.getSimulationReward() +
                        // get here into next level and get the reward returned
                        simulate(
                                simulationNode,
                                getPossibleTurns(nodeEnv, nextPlayer),
                                nodeEnv,
                                nextPlayer)
        );
        // backpropagate the reward to nodes on the higher level
        return (double) rewardGameState(nodeEnv);
    }

    /**
     * expand the tree such that all possible next moves are added as child nodes for the current node
     * each child receives their own deep copy of an environment
     * the map is updated according to the possible move
     * also the unvisited child nodes are added to the list of leaf nodes
     */
    private void expand() {
        ArrayList<Turn> possibleTurns = getPossibleTurns(root.getEnvironment(), myPlayer);
        for (Turn turn : possibleTurns) {
            Environment nodeEnvironment;
            try {
                nodeEnvironment = (Environment) root.getEnvironment().clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
                return;
            }
            nodeEnvironment.updatePlayground(turn);
            Player nextPlayer = getNextPlayer(root.getNextPlayer());
            Node child = new Node(nodeEnvironment, root, nextPlayer);
            root.getChildren().add(child);
            leafNodes.add(child);
        }
    }

}
