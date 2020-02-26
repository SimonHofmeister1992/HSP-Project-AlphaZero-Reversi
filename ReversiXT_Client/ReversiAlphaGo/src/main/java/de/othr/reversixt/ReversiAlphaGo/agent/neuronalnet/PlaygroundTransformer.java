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

        INDArray player1Board = convertPlaygroundBySymbol(playground, '1');
        INDArray player2Board = convertPlaygroundBySymbol(playground, '2');
        INDArray holesBoard = convertPlaygroundBySymbol(playground, '-');
        INDArray playerTurnBoard = Nd4j.zeros(DataType.INT,1,1,PlaygroundDimension,PlaygroundDimension);

        playerTurnBoard = playerTurnBoard.add(player.getSymbol() - 49);

        INDArray transformedPlayground = Nd4j.concat(1, player1Board, player2Board, holesBoard, playerTurnBoard);
        return transformedPlayground;
    }

    private INDArray convertPlaygroundBySymbol(Playground playground, char symbol){

        int[][] intPlayground = new int[PlaygroundDimension][PlaygroundDimension];

        for(int row = 0; row < PlaygroundDimension; row++){
            for(int col = 0; col < PlaygroundDimension; col++){
                if(row >= playground.getPlaygroundHeight() || col >= playground.getPlaygroundWidth()) {
                    if(symbol == '-') intPlayground[row][col] = 1;
                    else intPlayground[row][col] = 0;
                }
                else if(playground.getSymbolOnPlaygroundPosition(row,col) == symbol){
                    intPlayground[row][col] = 1;
                }
                else {
                    intPlayground[row][col] = 0;
                }
            }
        }

        return Nd4j.createFromArray(intPlayground).reshape(1,1,PlaygroundDimension,PlaygroundDimension);
    }

}
