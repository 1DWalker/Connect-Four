package ai.test;

import connectfour.Action;
import connectfour.State;

import java.util.ArrayList;
import java.util.List;

public class MC_RAVE2 {
    //Constants
    private double bias = 200;
    private double c = 0.5;

    private Node node;
    private boolean maximumPlayer;
    private int simulations;

    private final boolean output = false;
    private int playouts = -1; //default

    double baseScore; //Used when traversing tree and keeping track of zugzwang winner

    public MC_RAVE2(double bias, double c) {
//        node = new Node(5000000);
        node = new Node(1000000);
        this.bias = bias;
        this.c = c;
    }

    public MC_RAVE2(int playouts, double bias, double c) {
//        node = new Node(15000000);
        node = new Node(1000000);
        this.bias = bias;
        this.c = c;
        this.playouts = playouts;
    }

    public void reset() {
        node.reset();
    }

    public Action makeMove(State state, long time, long increment, Action action) {
        //Convert to state2, action2
        State2 state2 = new State2(state);

//        return state2.defaultPolicy();
//        return state2.makeRandomMoves();

        long timeTarget = System.currentTimeMillis() + time - Math.max(time * 4 / 5 - increment, 200);

        //Update the current node
        if (output) System.out.println("Space: " + node.space);
//        node.toRootNode(); //necessary?
        if (action == null) {
            if (node.hasChildren()) reset();
        } else {
            node.pruneNot(action);
        }

        return MC_RAVE(state2, timeTarget);
    }

    public Action MC_RAVE (State2 state2, long timeTarget){
        simulations = -1;
        long timeStart = System.nanoTime();
        while (timeRemaining(timeTarget)) {
            simulate(new State2(state2));
        }
        long timeEnd = System.nanoTime();
        if (output) System.out.println("MC_RAVE2 Speed: " + (double) simulations * 1000000000 / (timeEnd - timeStart) + " nps");
        return node.bestMove(state2.getPlayer(), bias, output);
    }

    public boolean timeRemaining(long timeTarget) {
        simulations++;

        if (simulations < 1) return true; //force at least one simulation

        if (playouts == -1) return System.currentTimeMillis() < timeTarget;

//        if (System.currentTimeMillis() >= timeTarget) {
//            System.out.println("MC_RAVE2: " + simulations);
//            return false;
//        } else {
//            return true;
//        }
//        return true;

        if (simulations == playouts) return false;

        return true;
    }

    public void simulate(State2 state2) {
        State2 leafState = simTree(state2);
        double z = simDefault(state2);
        backup(leafState, state2, z);
    }

    public State2 simTree(State2 state2) {
        maximumPlayer = state2.getPlayer();
        baseScore = 2;

        while (!state2.terminal()) {
            if (node.index != node.root && state2.getPlayer()) { // Check zugzwang if player X
                if (state2.allColumnsEven()) {
                    double score = state2.zugzwang();
                    if (score == 0) return new State2(state2);
                    else if (score == 0.5) baseScore = score;
                }
            }

            if (!node.hasChildren()) {
                addChildren(state2);
                Action action = state2.defaultPolicy(node.getChildActions());
                node.nextNode(action);
                state2.makeMove(action);
                return new State2(state2);
            }

            node.nextNode(node.bestChild(maximumPlayer, bias, c));
            state2.makeMove(node.getAction());

            maximumPlayer = !maximumPlayer;
        }

        return new State2(state2);
    }

    public double simDefault(State2 state2) {
//        if (node.isForcedWin()) return maximumPlayer ? 1 : 0;
        if (baseScore == 0) return 0;

        double score;
        while (!state2.terminal()) {
            if (state2.getPlayer()) { // Check zugzwang if player X
                if (state2.allColumnsEven()) {
                    score = state2.zugzwang();
                    if (score == 0) return 0;
                    else if (score == 0.5) baseScore = score;
                }
            }
            state2.makeMove(state2.defaultPolicy());
        }
        score = state2.score();
        return baseScore > score ? score : baseScore;
    }

    public void backup(State2 leafState, State2 endState, double z) {
        node.backup(leafState, endState, z);
    }

    public void addChildren(State2 state2) {
        List<Action> possibleActions = state2.getActions();
        for (Action action : possibleActions) {
            node.addNode(action);
        }
    }
}