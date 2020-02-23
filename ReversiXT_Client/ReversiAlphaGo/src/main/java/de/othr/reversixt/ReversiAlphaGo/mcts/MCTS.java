package de.othr.reversixt.ReversiAlphaGo.mcts;

import de.othr.reversixt.ReversiAlphaGo.agent.ITurnChoiceAlgorithm;
import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;


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
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z");
        Date date = new Date(System.currentTimeMillis());
        System.out.println("Start: " + formatter.format(date));
        Node chosenNode = root;
        double chosenNodeUCT;
        double nodeUCT;
        while (!leafNodes.isEmpty()) {
            //search best UCT in every loop of while
            chosenNodeUCT = Double.MIN_VALUE;
            System.out.println("LeafNodeSize before expand: " + leafNodes.size());
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
    private void traverse(Node traverseNode) {
        System.out.println("Start Traverse");
        System.out.println("Node children size: " + traverseNode.getChildren().size());
        for (Node child : traverseNode.getChildren()) {
            //
            //clone map to "complete" the map
            Playground playground = child.getPlayground().getCloneOfPlayground();
            //
            //Simulate one path to get a reward
            System.out.println("Simulate started");
            double reward = simulate(playground, child.getNextPlayer());
            System.out.println("Simulate finished");
            //
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
        System.out.println("The reward is " + reward);
        return reward;
    }

    /**
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
     * expand the tree such that all possible next moves are added as child nodes for the current node
     * each child receives their own deep copy of an environment
     * the map is updated according to the possible move
     * also the unvisited child nodes are added to the list of leaf nodes
     */
    private void expand(Node expandNode) {
        System.out.println("Start Expand");
        ArrayList<Turn> possibleTurns = getPossibleTurns(expandNode.getPlayground(), expandNode.getNextPlayer());
        System.out.println("Possbile Turn Size: " + possibleTurns.size());
        //for each Turn:
        // clone the playground and make a single turn -> create newNode with updated map
        // insert newNode in children of expandNode and in leafNodes
        for (Turn turn : possibleTurns) {
            Playground playground = expandNode.getPlayground().getCloneOfPlayground();
            environment.updatePlayground(turn, playground);
            Node child = new Node(playground, expandNode, environment.getNextPlayer(turn.getPlayerIcon()), turn);
            System.out.println("-- new Node created--");
            expandNode.getChildren().add(child);
            leafNodes.add(child);
        }
        //expandNode is expanded and can be removed from Leaf Nodes
        //expandNode is not included in UCT anymore
        leafNodes.remove(expandNode);
        System.out.println("Removed Node form leaf Nodes");
    }


}
