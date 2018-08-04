package connectfour;

import ai.MC_RAVE;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class CreateData {

    static MC_RAVE AI1;
    static MC_RAVE AI2;

    static Statistics statistics;

    static String file = "train_data17.txt";
    static int numberOfGames = 10000;
    static final boolean P1 = false; //False if AI1 is playing X
    static final boolean P2 = false; //False if AI2 is playing O
    static final boolean PRINT_BOARD = false; //Print board after every move
    static final boolean PRINT_STATS = true; //Print statistics after every game
    static final long TIME_START = 6000000; //Testing: 600, 60
    static final long INCREMENT = 600000;
    static boolean switchSides = false;
    static boolean mirror;
    static State startState;
    static double finalScore;
    static Random random = new Random();

    static long[] timeControl;

    //Keep track of time
    static long start;
    static long end;

    public static void main(String[] args) {
        statistics = new Statistics(numberOfGames);

        if (!P1) AI1 = new MC_RAVE(10000, 0, 0.5);
        if (!P2) AI2 = new MC_RAVE(10000, 0, 0.5);

        timeControl = new long[]{TIME_START, INCREMENT};

        writeData();
    }

    public static List<Action> game() {
        List<Action> actionList = new ArrayList(42);

        State state = new State();
        boolean playerX = true;
        long[] playerTime = new long[]{TIME_START, TIME_START};
        int gameLength = 0;
        double score;
        mirror = false;
        Action action = null;

        //Play a bunch of bad moves
        int randomMoves = random.nextInt(21); //20 maximum random moves
        if (PRINT_STATS) System.out.println("Number of random moves: " + randomMoves);
        for (int i = 0; i < randomMoves; i++) {
            if (state.terminal()) {
                AI1.reset();
                AI2.reset();
                return actionList;
            }
            if ((playerX && !switchSides) || (!playerX && switchSides)) { //P1
                action = player1Move(state, playerTime[0], INCREMENT, action, 1000, 2);
            } else {
                action = player2Move(state, playerTime[1], INCREMENT, action, 1000, 2);
            }
            state.makeMove(action);
            playerX = !playerX;
            gameLength++;
        }

        startState = new State(state);

        if (PRINT_BOARD) printGameState(state, playerTime, timeControl, actionList);

        while(true) {
            if (state.terminal()) {
                score = state.score();
                if (score == 1) if (PRINT_STATS)  System.out.println("Player X wins!");
                else if (score == 0) if (PRINT_STATS) System.out.println("Player O wins!");
                else System.out.println("Draw!");
                score = switchSides ? 1 - score : score;
                break;
            }

            //Find move to play
            start = System.currentTimeMillis();
            if ((playerX && !switchSides) || (!playerX && switchSides)) { //P1
                action = player1Move(state, playerTime[0], INCREMENT, action, 10000, 0);
            } else {
                action = player2Move(state, playerTime[1], INCREMENT, action, 10000, 0);
            }
            end = System.currentTimeMillis();

            //Determine if there is a loser based on time
            if ((playerX && !switchSides) || (!playerX && switchSides)) { //P1
                playerTime[0] += start - end + INCREMENT;
                if (playerTime[0] <= 0) {
                    score = 0;
                    if (PRINT_STATS) System.out.println("Player " + (playerX ? "X" : "O") + " loses on time!");
                    break;
                }
            } else {
                playerTime[1] += start - end + INCREMENT;
                if (playerTime[1] <= 0) {
                    score = 1;
                    if (PRINT_STATS) System.out.println("Player " + (playerX ? "X" : "O") + " loses on time!");
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

        //Code runs when game is over

        statistics.addStatistic(score, switchSides, gameLength);

        if (!PRINT_BOARD) {
            if (PRINT_STATS) printGameState(state, playerTime, timeControl, actionList); //In case isn't printed
        }
        if (PRINT_STATS) {
            statistics.print(gameLength);
            System.out.println();
        }

        switchSides = !switchSides;

        if (!P1) AI1.reset();
        if (!P2) AI2.reset();

        finalScore = state.score();
        return actionList;
//        if (!PRINT_STATS) statistics.print();
    }

    public static void writeData() {
        try(FileWriter fw = new FileWriter(file, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            for (int i = 0; i < numberOfGames; i++) {
                System.out.println(i);
                List<Action> actionList = game();
                if (actionList.size() == 0) { //Game terminated before data collection
                    i--;
                    continue;
                }
                List<Action> actionListFlip = new ArrayList<>(actionList.size()); //Create another copy of the game with reversed moves
                for (Action action : actionList) {
                    actionListFlip.add(new Action(6 - action.move));
                }

                writeGame(out, actionList, finalScore);

                startState.flip();
                writeGame(out, actionListFlip, finalScore);
            }
        } catch (IOException e) {

        }
    }

    public static void writeGame(PrintWriter out, List<Action> actionList, double score) {
        State state = new State(startState);
        String line;
        for (Action action : actionList) {
            line = state.getStringCode();
            switch(action.move) {
                case 0:
                    line += "1 0 0 0 0 0 0 ";
                    break;
                case 1:
                    line += "0 1 0 0 0 0 0 ";
                    break;
                case 2:
                    line += "0 0 1 0 0 0 0 ";
                    break;
                case 3:
                    line += "0 0 0 1 0 0 0 ";
                    break;
                case 4:
                    line += "0 0 0 0 1 0 0 ";
                    break;
                case 5:
                    line += "0 0 0 0 0 1 0 ";
                    break;
                case 6:
                    line += "0 0 0 0 0 0 1 ";
                    break;
            }
            if (score == 0) line += "-1";
            else if (score == 0.5) line += "0";
            else line += "1";

//            state.print();
//            System.out.println(line);
//            System.out.println(action.move + 1);
            out.println(line);
            state.makeMove(action);
        }
    }

    public static void printGameState(State state, long[] playerTime, long[] timeControl, List<Action> actionList) {
        System.out.print("Sequence: ");
        for (Action action : actionList) {
            System.out.print(action.move + 1);
//            state.makeMove(action);
        }
        System.out.println();

        if (!switchSides) {
            System.out.println("P1: " + (double) playerTime[0] / 1000 + " P2 " + (double) playerTime[1] / 1000 + " with " + (double) timeControl[1] / 1000 + " increment");
        } else {
            System.out.println("P2: " + (double) playerTime[1] / 1000 + " P1 " + (double) playerTime[0] / 1000 + " with " + (double) timeControl[1] / 1000 + " increment");
        }
        state.print(mirror);
    }

    public static Action player1Move(State state, long time, long increment, Action action, int playouts, double noise) {
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
            return AI1.makeMove(state, time, increment, action, playouts, noise);
        }
    }

    public static Action player2Move(State state, long time, long increment, Action action, int playouts, double noise) {
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
            return AI2.makeMove(state, time, increment, action, playouts, noise);
        }
    }
}
