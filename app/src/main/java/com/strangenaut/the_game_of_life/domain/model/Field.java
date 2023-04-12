package com.strangenaut.the_game_of_life.domain.model;

public class Field {

    private boolean[][] state;
    private boolean[][] previousState;

    public Field(int width, int height) {
        state = new boolean[width][height];
        previousState = new boolean[width][height];
    }

    public int getWidth () {
        return state.length;
    }

    public int getHeight () {
        return state[0].length;
    }

    public boolean getCell(int i, int j) {
        return state[i][j];
    }

    public boolean getCellPreviousState(int i, int j) {
        return previousState[i][j];
    }

    public void setCell(int i, int j, boolean isAlive) {
        state[i][j] = isAlive;
    }

    public void updateGeneration(boolean isTorus) {
        previousState = state.clone();
        boolean[][] tempMatrix = new boolean[getWidth()][getHeight()];
        for (int i = 0; i < getWidth(); i++)
        {
            for (int j = 0; j < getHeight(); j++)
            {
                tempMatrix[i][j] = state[i][j];
                int localsCount = state[i][j] ? -1 : 0;

                // Here the algorithm checks element's locals
                // If the element is the "border" element, we don't check it's non - existent locals
                if (isTorus) {
                    for (int horOffset = -1; horOffset < 2; horOffset++)
                        for (int verOffset = -1; verOffset < 2; verOffset++)
                        {
                            int horIndex = i + horOffset;
                            int verIndex = j + verOffset;
                            if (i == 0 && horOffset == -1) {
                                horIndex = getWidth() - 1;
                            }
                            if (i == getWidth() - 1 && horOffset == 1) {
                                horIndex = 0;
                            }
                            if (j == 0 && verOffset == -1) {
                                verIndex = getHeight() - 1;
                            }
                            if (j == getHeight() - 1 && verOffset == 1) {
                                verIndex = 0;
                            }
                            localsCount += (state[horIndex][verIndex]) ? 1 : 0;
                        }
                } else {
                    for (int horOffset = (i == 0 ? 0 : -1); horOffset < (i == getWidth() - 1 ? 1 : 2); horOffset++) {
                        for (int verOffset = (j == 0 ? 0 : -1); verOffset < (j == getHeight() - 1 ? 1 : 2); verOffset++) {
                            localsCount += state[i + horOffset][j + verOffset] ? 1 : 0;
                        }
                    }
                }
                tempMatrix[i][j] = (localsCount == 2 && tempMatrix[i][j]) || localsCount == 3;
            }
        }
        state = tempMatrix;
    }

    public void clearField() {
        state = new boolean[getWidth()][getHeight()];
    }
}
