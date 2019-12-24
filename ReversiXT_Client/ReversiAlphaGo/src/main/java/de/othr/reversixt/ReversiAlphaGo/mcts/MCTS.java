package de.othr.reversixt.ReversiAlphaGo.mcts;

public class MCTS {

    private Node root;
    private Arraylist<Node> leafNodes;
    private Player myPlayer;

    public MCTS(Evironment environment, Player player) {
        this.root = new Node(environment, myPlayer);
        this.leafNodes = new ArrayList<Node>;
        this.myPlayer = player;
        //get possible moves and store them as children
    }

    //TODO copied from RandomTurnChoiceAlgorithm
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
                    setBestTurn(turn);
                }
            }
        }
        return validTurns;
    }

    private int rewardGameState(Environment environment) {
        //reward = count all our stones
        int reward = 0;
        for (int x = 0; x < environment.getPlayground().getPlaygroundHeight(); x++) {
            for (int y = 0; y < environment.getPlayground().getPlaygroundWidth(); y++) {
                //print gameboard
                //System.out.print(game[x][y]);
                if (environment.getPlayground().getSymbolOnPlaygroundPosition(y, x) == myPlayer.getPlayerIcon()) {
                    reward++;
                }
            }
            //print gameboard
            //System.out.println('|');
        }
        System.out.println("The reward is " + reward);
        return reward;
    }

    private Player getNextPlayer(Player currentPlayer) {
        //TODO find next player
        return currentPlayer;
    }

    protected void searchBestTurn() {
        //clone map
        ArrayList<Turn> possibleTurns = getPossibleTurns(root.environment, myPlayer);
        for (Turn turn : possibleTurns) {
            //TODO clone env --> nodeEnvironment
            nodeEnvironment.updatePlayground(turn);
            Player nextPlayer = getNextPlayer(root.nextPlayer);
            Node child = new Node(nodeEnvironment, root, nextPlayer);
            root.children.add(child);
            leafNodes.add(child);
        }

        for (Node child : root.children) {
            //clone env --> nodeEnvironment
            Player nextPlayer = child.nextPlayer;
            double reward;
            while (reward != null) {
                ArrayList<Turn> possibleTurns = getPossibleTurns(nodeEnvironment, nextPlayer);
                Random random = new Random();
                if (possibleTurns.size() > 0) {
                    int index = random.nextInt() % possibleTurns.size();
                    turn = possibleTurns.get(index);
                    nodeEnvironment.updatePlayground(turn);
                    nextPlayer = getNextPlayer();
                } else {
                    reward = rewardGameState(nodeEnvironment);
                    //path traversed --> game ended
                }
            }
            //TODO backpropagate tree
        }

    }
}
