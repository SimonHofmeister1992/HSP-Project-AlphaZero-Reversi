package de.othr.reversixt.ReversiAlphaGo.general;

public interface AlphaGoZeroConstants {
    int DIMENSION_PLAYGROUND = 15; // max width and height of Playground
    int NUMBER_OF_FEATURE_PLANES_INPUT_NEURONAL_NET = 4; // number of planes which are input to the neuronal net
    int NUMBER_OF_TRAINING_GAMES_UNTIL_UPDATE=100;
    double NEEDED_WIN_RATE = 0.55;
}
