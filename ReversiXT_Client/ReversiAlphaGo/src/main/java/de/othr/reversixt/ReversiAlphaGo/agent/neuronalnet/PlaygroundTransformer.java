package de.othr.reversixt.ReversiAlphaGo.agent.neuronalnet;

import de.othr.reversixt.ReversiAlphaGo.environment.Player;
import de.othr.reversixt.ReversiAlphaGo.environment.Playground;
import de.othr.reversixt.ReversiAlphaGo.general.AlphaGoZeroConstants;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class PlaygroundTransformer {

    public static final int PlaygroundDimension = AlphaGoZeroConstants.DIMENSION_PLAYGROUND;

    public PlaygroundTransformer(){

    }


    public INDArray transform(Playground playground, Player player){
        return convertPlaygroundBySymbol(playground, player);
    }

    private INDArray convertPlaygroundBySymbol(Playground playground, Player player){

        int[][] intPlaygroundPlayer1 = new int[PlaygroundDimension][PlaygroundDimension];
        int[][] intPlaygroundPlayer2 = new int[PlaygroundDimension][PlaygroundDimension];
        int[][] intHolesMap = new int[PlaygroundDimension][PlaygroundDimension];
        int[][] intTurnOfPlayerMap = new int[PlaygroundDimension][PlaygroundDimension];


        // build the 3 planes containing the stones of player1, player2 and the holes
        for(int row = 0; row < PlaygroundDimension; row++){
            for(int col = 0; col < PlaygroundDimension; col++){
                intPlaygroundPlayer1[row][col] = 0;
                intPlaygroundPlayer2[row][col] = 0;
                intHolesMap[row][col] = 0;
                intTurnOfPlayerMap[row][col] = 0;

                if(row >= playground.getPlaygroundHeight() || col >= playground.getPlaygroundWidth()) {
                    intHolesMap[row][col] = 1;
                }
                else {
                    switch (playground.getSymbolOnPlaygroundPosition(row,col)){
                        case '1': intPlaygroundPlayer1[row][col] = 1; break;
                        case '2': intPlaygroundPlayer1[row][col] = 2; break;
                        case '3': intHolesMap[row][col] = 1; break;
                        default: break;
                    }
                }
            }
        }

        // Map plane which contains the actual player turn
        INDArray playerTurnBoard = Nd4j.zeros(DataType.INT,1,1,PlaygroundDimension,PlaygroundDimension);
        playerTurnBoard = playerTurnBoard.add(player.getSymbol() - 49);


        // Return an array containing all 4 planes
        return Nd4j.concat(1,
                Nd4j.createFromArray(intPlaygroundPlayer1).reshape(1,1,PlaygroundDimension,PlaygroundDimension),
                Nd4j.createFromArray(intPlaygroundPlayer2).reshape(1,1,PlaygroundDimension,PlaygroundDimension),
                Nd4j.createFromArray(intHolesMap).reshape(1,1,PlaygroundDimension,PlaygroundDimension),
                playerTurnBoard);
    }

}
