package de.othr.reversixt.ReversiAlphaGo.mcts;

import de.othr.reversixt.ReversiAlphaGo.environment.Environment;
import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.environment.Turn;


import java.lang.Math;
import java.util.ArrayList;

public class Node {

    // TODO prior probability for a move (calculated by a nn) has to be added instead of the constant, for now an arbitrary constant is used (which will later be superfluous)
    // parameter for controlling the trade-off between exploration and exploitation in the mcts
    private static final double UCB_CONSTANT = 1.2;

    private int numVisited;
    private double simulationReward;
    private Node parent;
    private ArrayList<Node> children;
    private Playground playground;
    private Player nextPlayer;
    private Turn curTurn;

    /**
     * public constructor for the root node
     * init: a new node is not yet visited, the inital win/loss and has no children
     *
     * @param playground for the node
     * @param player      specifying whose turn it is (for the root node it is the current user not the opponent)
     */
    public Node(Playground playground, Player player) {
        this.numVisited = 0;
        this.simulationReward = 0.0;
        this.parent = null;
        this.children = new ArrayList<Node>();
        this.playground = playground;
        this.nextPlayer = player;
    }

    /**
     * public constructor
     * init: a new node is not yet visited, the initial win/loss and has no children
     *
     * @param playground for the node
     * @param parent      of the node
     * @param nextPlayer      specifying whose turn it is
     */
    public Node(Playground playground, Node parent, Player nextPlayer, Turn turn) {
        this.numVisited = 0;
        this.simulationReward = 0.0;
        this.parent = parent;
        this.children = new ArrayList<Node>();
        this.playground = playground;
        this.nextPlayer = nextPlayer;
        this.curTurn = turn;
    }

    /**
     * getter and setter
     */

    public Turn getCurTurn() {
        return curTurn;
    }

    public void setCurTurn(Turn curTurn) {
        this.curTurn = curTurn;
    }

    public int getNumVisited() {
        return numVisited;
    }

    public void setNumVisited(int numVisited) {
        this.numVisited = numVisited;
    }

    public double getSimulationReward() {
        return simulationReward;
    }

    public void setSimulationReward(double simulationReward) {
        this.simulationReward = simulationReward;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public ArrayList<Node> getChildren() {
        return children;
    }

    public Playground getPlayground() {
        return playground;
    }

    public void setPlayground(Playground playground) {
        this.playground = playground;
    }

    public Player getNextPlayer() {
        return nextPlayer;
    }

    public void setNextPlayer(Player nextPlayer) {
        this.nextPlayer = nextPlayer;
    }


    /**
     * calculates the exploitation component for the uct
     *
     * @return double exploitation value
     */
    private double calculateExploitation() {
        return simulationReward / numVisited;
    }

    /**
     * calculates the exploration component for the uct
     *
     * @return double exploration value
     */
    private double calculateExploration() {
        return Math.sqrt(((double) parent.getNumVisited()) / (1 + numVisited));
    }

    /**
     * TODO exchange UCB_CONSTANT
     * calculates the upper confidence bound applied to trees for the given node
     *
     * @return double uct value
     */
    public double calculateUCT() {
        return calculateExploitation() + UCB_CONSTANT * calculateExploration();
    }
}