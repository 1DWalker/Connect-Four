package connectfour;

import ai.MC_RAVE;
import ai.test.MC_RAVE2;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Experiment {

    static MC_RAVE AI1;
    static MC_RAVE AI2;

    static Statistics statistics;

    static int numberOfGames = 40000;
    static final boolean P1 = true; //False if AI1 is playing X
    static final boolean P2 = false; //False if AI2 is playing O
    static final boolean PRINT_BOARD = true; //Print board after every move
    static final boolean PRINT_STATS = true; //Print statistics after every game
    static final long TIME_START = 60000000; //Testing: 600, 60
    static final long INCREMENT = 6000000;
    static boolean switchSides = true;
    static boolean mirror;

    static long[] timeControl;

    //Keep track of time
    static long start;
    static long end;

    public static void main(String[] args) {
        statistics = new Statistics(numberOfGames);

        if (!P1) AI1 = new MC_RAVE(10000,0, 0.5);
        if (!P2) AI2 = new MC_RAVE(10000,0, 0.5);

        timeControl = new long[]{TIME_START, INCREMENT};

        while (true) {
            match(numberOfGames);
        }
    }

    public static void match(int n) {
        for (int i = 0; i < n; i++) {
            State state = new State();
            List<Action> actionList = new ArrayList();

            boolean playerX = true;
            int gameLength = 0;
            long[] playerTime = new long[]{TIME_START, TIME_START};
            double score;
            mirror = false;
            Action action = null;

            if (PRINT_BOARD) printGameState(state, playerTime, timeControl, actionList);

            while(true) {
                if (state.terminal()) {
                    score = state.score();
                    if (score == 1) System.out.println("Player X wins!");
                    else if (score == 0) System.out.println("Player O wins!");
                    else System.out.println("Draw!");
                    score = switchSides ? 1 - score : score;
                    break;
                }

                //Find move to play
                start = System.currentTimeMillis();
                if ((playerX && !switchSides) || (!playerX && switchSides)) { //P1
                    action = player1Move(state, playerTime[0], INCREMENT, action);
                } else {
                    action = player2Move(state, playerTime[1], INCREMENT, action);
                }
                end = System.currentTimeMillis();

                //Determine if there is a loser based on time
                if ((playerX && !switchSides) || (!playerX && switchSides)) { //P1
                    playerTime[0] += start - end + INCREMENT;
                    if (playerTime[0] <= 0) {
                        score = 0;
                        System.out.println("Player " + (playerX ? "X" : "O") + " loses on time!");
                        break;
                    }
                } else {
                    playerTime[1] += start - end + INCREMENT;
                    if (playerTime[1] <= 0) {
                        score = 1;
                        System.out.println("Player " + (playerX ? "X" : "O") + " loses on time!");
                        break;
                    }
                }

                if (mirror) action = new Action(6 - action.move);

                if (state.isSymmetric()) {
                    if (action.move > 3) {
                        action = new Action(6 - action.move);
                        mirror = !mirror;
                    }
                }

                actionList.add(action);
                state.makeMove(action);

                playerX = !playerX;
                gameLength++;

                if (PRINT_BOARD) {
                    printGameState(state, playerTime, timeControl, actionList);
                    System.out.println();
                }
            }

            statistics.addStatistic(score, switchSides, gameLength);

            if (!PRINT_BOARD) printGameState(state, playerTime, timeControl, actionList); //In case isn't printed
            if (PRINT_STATS) {
                statistics.print(gameLength);
                System.out.println();
            }

            switchSides = !switchSides;

            if (!P1) AI1.reset();
            if (!P2) AI2.reset();
        }

        if (!PRINT_STATS) statistics.print();
    }

    public static void printGameState(State state, long[] playerTime, long[] timeControl, List<Action> actionList) {
        System.out.print("Sequence: ");
        for (Action action : actionList) {
            System.out.print(action.move + 1);
        }
        System.out.println();

        if (!switchSides) {
            System.out.println("P1: " + (double) playerTime[0] / 1000 + " P2 " + (double) playerTime[1] / 1000 + " with " + (double) timeControl[1] / 1000 + " increment");
        } else {
            System.out.println("P2: " + (double) playerTime[1] / 1000 + " P1 " + (double) playerTime[0] / 1000 + " with " + (double) timeControl[1] / 1000 + " increment");
        }
        state.print(mirror);
//        if (state.hasWon(state.bitboard[0])) {
//            System.out.println("Player X has won!");
//        } else if (state.hasWon(state.bitboard[1])) {
//            System.out.println("Player O has won!");
//        } else if (state.ply == 42) {
//            System.out.println("Draw!");
//        }
    }

    public static Action player1Move(State state, long time, long increment, Action action) {
        if (P1) {
            Scanner keyboard = new Scanner(System.in);

            while (true) {
                try {
                    int col = keyboard.nextInt() - 1;
                    if (col < 0 || col > State.WIDTH - 1 || !state.isPlayable(col)) {
                        System.out.println("Illegal move.");
                        continue;
                    } else {
                        return new Action(col);
                    }
                } catch (Exception e) {
                    System.out.println("error");
                }
            }
        } else {
//            return AI1.makeMove(state, time, increment, action);
            return AI1.makeMove(state, time, increment, action, 10000,0);
        }
    }

    public static Action player2Move(State state, long time, long increment, Action action) {
        if (P2) {
            Scanner keyboard = new Scanner(System.in);

            while (true) {
                try {
                    int col = keyboard.nextInt() - 1;
                    if (col < 0 || col > State.WIDTH - 1 || !state.isPlayable(col)) {
                        System.out.println("Illegal move.");
                        continue;
                    } else {
                        return new Action(col);
                    }
                } catch (Exception e) {
                    System.out.println("error");
                }
            }
        } else {
            return AI2.makeMove(state, time, increment, action, 10000,0);
        }
    }
}
