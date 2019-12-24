package de.othr.reversixt.ReversiAlphaGo.mcts;

import java.lang.Math;

public class Node {

    // TODO prior probability for a move (calculated by a nn) has to be added instead of the constant, for now an arbitrary constant is used (which will later be superfluous)
    // parameter for controlling the trade-off between exploration and exploitation in the mcts
    private static final int UCB_CONSTANT = 1.2;

    private int numVisited;
    private double simulationReward;
    private Node parent;
    private ArrayList<Node> children;
    private Environment environment;
    private Player nextPlayer;

    /**
     * public constructor for the root node
     * init: a new node is not yet visited, the inital win/loss and has no children
     */
    public Node(Environment environment, Player player) {
        this.numVisited = 0;
        this.simulationReward = 0.0;
        this.parent = null;
        this.children = new ArrayList<Node>();
        this.environment = environment;
        this.nextPlayer = player;
    }

    /**
     * public constructor
     * init: a new node is not yet visited, the inital win/loss and has no children
     * @param parent
     */
    public Node(Environment environment, Node parent, Player player) {
        this.numVisited = 0;
        this.simulationReward = 0.0;
        this.parent = parent;
        this.children = new ArrayList<Node>();
        this.environment = environment;
        this.nextPlayer = player;
    }

    /**
     * getter and setter
     * @return
     */
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

    //TODO updateSimulationReward and numVisited?

    /**
     * calculates the exploitation component for the uct
     * @return double exploitation value
     */
    private double calculateExploitation() {
        return simulationReward / numVisited;
    }

    //TODO parent.getNumVisited() correct?
    /**
     * calculates the exploration component for the uct
     * @return double exploration value
     */
    private double calculateExploration() {
        return Math.sqrt( parent.getNumVisited() (1 + numVisited));
    }

    /**
     * TODO exchange UCB_CONSTANT
     * calculates the upper confidence bound applied to trees for the given node
     * @return double uct value
     */
    protected double calculateUCT() {
        return calculateExploitation() + UCB_CONSTANT * calculateExploration();
    }
}