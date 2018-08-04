package ai.test;

import connectfour.Action;

public class Data { //Action, but with more information
    Action action;
    boolean forcedWin, forcedDraw, forcedLoss;

    public Data(int move) {
        action = new Action(move);
        forcedWin = forcedDraw = forcedLoss = false;
    }

    public int getMove() {
        return action.move;
    }
}
