package ai;

import connectfour.*;

import java.util.List;

public class MC_RAVE {
    //Constants
    private double bias = 200;
    private double c = 0.5;

    private Node node;
    private boolean maximumPlayer;
    private int simulations;

    private final boolean output = false;
    private int playouts = -1;

    double baseScore; //Used when traversing tree and keeping track of zugzwang winner

    public MC_RAVE(double bias, double c) {
//        node = new Node(5000000);
        node = new Node(1000000);
        this.bias = bias;
        this.c = c;
    }

    public MC_RAVE(int playouts, double bias, double c) {
//        node = new Node(15000000);
        node = new Node(1000000);
        this.bias = bias;
        this.c = c;
        this.playouts = playouts;
    }

    public void reset() {
        node.reset();
//        node.fullReset();
    }

    public Action makeMove(State state, long time, long increment, Action action, int playouts, double noise) {
        if (this.playouts == 0) {
            return state.makeRandomMoves();
        }

//        this.playouts = playouts;
//        node.noise = noise;

//        return state.defaultPolicy();
//        return state.makeRandomMoves();


        long timeTarget = System.currentTimeMillis() + time - Math.max(time * 4 / 5 - increment, 200);
//
        //Update the current node
        if (output) System.out.println("Space: " + node.space);
        if (action == null) {
            if (node.hasChildren()) reset();
        } else {
            node.pruneNot(action);
        }

        return MC_RAVE(state, timeTarget);
    }

    public Action MC_RAVE (State state, long timeTarget){
        simulations = -1;
        long timeStart = System.nanoTime();
        while (timeRemaining(timeTarget)) {
            simulate(new State(state));
        }
        long timeEnd = System.nanoTime();
        if (output) System.out.println("MC_RAVE1 Speed: " + (double) simulations * 1000000000 / (timeEnd - timeStart) + " nps");
        return node.bestMove(state.getPlayer(), bias, output);
    }

    public boolean timeRemaining(long timeTarget) {
        simulations++;

        if (simulations < 1) return true; //force at least one simulation
//
        if (playouts == -1) return System.currentTimeMillis() < timeTarget;

//        if (System.currentTimeMillis() >= timeTarget) {
//            System.out.println("MC_RAVE " + simulations);
//            return false;
//        } else {
//            return true;
//        }
//        return true;

        if (simulations == playouts) return false;

        return true;
    }

    public void simulate(State state) {
        simTree(state);
        double z = simDefault(state);
        backup(z);
    }

    public void simTree(State state) {
        maximumPlayer = state.getPlayer();
        baseScore = 2;

        while (!state.terminal()) {
            if (node.index != node.root && state.getPlayer()) { // Check zugzwang if player X
                if (state.allColumnsEven()) {
                    double score = state.zugzwang();
                    if (score == 0) return;
                    else if (score == 0.5) baseScore = score;
                }
            }

            if (!node.hasChildren()) {
                addChildren(state);
                Action action = state.defaultPolicy(node.getChildActions());
                node.nextNode(action);
                state.makeMove(action);
                return;
            }

            node.nextNode(node.bestChild(maximumPlayer, bias, c));
            state.makeMove(node.getAction());

            maximumPlayer = !maximumPlayer;
        }
    }

    public double simDefault(State state) {
        if (baseScore == 0) return 0;

        double score;
        while (!state.terminal()) {
            if (state.getPlayer()) { // Check zugzwang if player X
                if (state.allColumnsEven()) {
                    score = state.zugzwang();
                    if (score == 0) return 0;
                    else if (score == 0.5) baseScore = score;
                }
            }
            state.makeMove(state.defaultPolicy());
        }
        score = state.score();
        return baseScore > score ? score : baseScore;
    }

    public void backup(double z) {
        node.backup(z);
    }

    public void addChildren(State state) {
        List<Action> possibleActions = state.getActions();
        for (Action action : possibleActions) {
            node.addNode(action);
        }
    }
}