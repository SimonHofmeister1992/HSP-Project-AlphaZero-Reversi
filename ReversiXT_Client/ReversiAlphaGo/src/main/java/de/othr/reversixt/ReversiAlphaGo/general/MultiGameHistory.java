package de.othr.reversixt.ReversiAlphaGo.general;

import java.io.*;

public class MultiGameHistory {

    private int numberOfGames;
    private int numberOfWonGames;

    private static final File statisticsFile = new File("learning/statistics.txt");

    public MultiGameHistory(){
        numberOfGames=0;
        numberOfWonGames=0;
        readStatisticFile();
    }

    public int getNumberOfGames() {
        return numberOfGames;
    }

    public int getNumberOfWonGames() {
        return numberOfWonGames;
    }

    public void declareGameAsWon(){
        this.numberOfGames++;
        this.numberOfWonGames++;

        saveStatisticToFile();
    }

    public void declareGameAsLost(){
        this.numberOfGames++;

        saveStatisticToFile();
    }

    private void saveStatisticToFile(){
        FileWriter fw;
        BufferedWriter bw;

        try {
            fw = new FileWriter(statisticsFile);
            bw = new BufferedWriter(fw);
            if(this.getNumberOfGames() >= AlphaGoZeroConstants.NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE){
                this.numberOfGames=0;
                this.numberOfWonGames=0;
            }
            bw.write(this.getNumberOfGames() + "," + this.getNumberOfWonGames());

            bw.close();

        } catch (IOException e) {
        }
    }

    private void readStatisticFile(){
        try {
            FileReader fr = new FileReader(statisticsFile);
            BufferedReader br = new BufferedReader(fr);
            String[] contents = br.readLine().split(",");
            this.numberOfGames = Integer.valueOf(contents[0]);
            this.numberOfWonGames = Integer.valueOf(contents[1]);
        } catch (IOException e) {
        }
    }

}
