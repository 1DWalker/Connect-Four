//Tree of size 15 million is approximately the maximum
package ai;

import connectfour.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Node { //Stores the entire tree of nodes
    static Random random = new Random();
    public static double noise = 0;

    int[] parent; //Stores the parent index
    List<Integer>[] children; //Stores children indices
    Action[] action; //Stores the action that led from parent index to this node

    int index; //Index of current node
    int root; //Index of root node
    int size; //Size of tree
    int writingIndex; //The current spot in the node writer, for speed purposes
    int space; //The number of nodes that are filled
    boolean flag[]; //To mark nodes to erase

    public int[] MC_count, MC_wins, MC_draws, AMAF_count, AMAF_wins, AMAF_draws;

    public Node(int size) {
        this.size = size;

        parent = new int[size];
        for (int i = 0; i < size; i++) {
            parent[i] = -1;
        }

        children = new ArrayList[size];
        for (int i = 0; i < children.length; i++) {
            children[i] = new ArrayList(7);
        }

        action = new Action[size];

        index = 0;
        root = 0;
        writingIndex = 1; //0 is already filled, start writing at 1
        space = size - 1; //1 node is already filled
        flag = new boolean[size];

        MC_count = new int[size];
        MC_wins = new int[size];
        MC_draws = new int[size];
        AMAF_count = new int[size];
        AMAF_wins = new int[size];
        AMAF_draws = new int[size];
    }

    public void fullReset() {
        for (int i = 0; i < size; i++) {
            parent[i] = -1;
        }

        for (int i = 0; i < children.length; i++) {
            children[i].clear();
        }

        index = 0;
        root = 0;
        writingIndex = 1; //0 is already filled, start writing at 1
        space = size - 1; //1 node is already filled

        for (int i = 0; i < size; i++) {
            action[i] = null;
            MC_count[i] = 0;
            MC_wins[i] = 0;
            MC_draws[i] = 0;
            AMAF_count[i] = 0;
            AMAF_wins[i] = 0;
            AMAF_draws[i] = 0;
        }
    }

    public void reset() {
        int remove;

        while (children[root].size() > 0) {
            int start = children[root].get(0);

            index = start;

            boolean childFound;
            do {
                do {
                    childFound = false;
                    for (int i = 0; i < children[index].size(); i++) {
                        index = children[index].get(i);
                        childFound = true;
                        break;
                    }
                } while (childFound);

                if (index == start) break;

                remove = index;
                index = parent[index];
                removeNode(remove);
                children[index].remove(0);
            } while (true);

            removeNode(start);
            children[root].remove(0);
        }

        removeNode(root);

        index = 0;
        root = 0;
        writingIndex = 1; //0 is already filled, start writing at 1
        space = size - 1; //1 node is already filled
    }

    public void pruneNot(int child) {
        index = root;
        int childIndex = children[root].get(child);
        int remove;

        for (int k = 0; k < children[root].size(); k++) {
            int start = children[root].get(k);
            if (start == childIndex) continue;

            index = start;

            boolean childFound;
            do {
                do {
                    childFound = false;
                    for (int i = 0; i < children[index].size(); i++) {
                        index = children[index].get(i);
                        childFound = true;
                        break;
                    }
                } while (childFound);

                if (index == start) break;

                remove = index;
                index = parent[index];
                removeNode(remove);
                children[index].remove(0);
            } while (true);

            removeNode(start);
            children[root].remove(k);
            k--;
        }

        index = children[root].get(0);
        children[root].remove(0);
        removeNode(root);
        root = index;
        parent[root] = root;
        writingIndex = 0;
    }

    public void pruneNot(Action action) {
        for (int i = 0; i < children[index].size(); i++) {
            nextNode(i);
            if (this.action[index].sameAs(action)) {
                previousNode();
                pruneNot(i);
                return;
            }
            previousNode();
        }
    }

    public void removeNode(int index) {
        parent[index] = -1;
        children[index].clear();
        action[index] = null;
        MC_count[index] = 0;
        MC_wins[index] = 0;
        MC_draws[index] = 0;
        AMAF_count[index] = 0;
        AMAF_wins[index] = 0;
        AMAF_draws[index] = 0;

        space++;
    }

    public boolean addNode(Action action) {
        while (writingIndex < size) {
            if (parent[writingIndex] == -1) {
                parent[writingIndex] = index;
                children[index].add(writingIndex);
                this.action[writingIndex] = action;
                space--;
                writingIndex++;
                return true;
            }
            writingIndex++;
        }
        System.exit(5);
        return false;
    }

    public void nextNode(Action action) { //Finds the node that corresponds to action and returns with action played
        for (int i = 0; i < children[index].size(); i++) {
            nextNode(i);
            if (this.action[index].sameAs(action)) return;
            previousNode();
        }

//        System.exit(10); //Test
        if (hasChildren()) reset();
    }

    public void nextNode(int child) {
        index = children[index].get(child);
    }

    public void previousNode() {
        index = parent[index];
    }

    public void toRootNode() {
        index = root;
    }

    public int bestChild(boolean maximumPlayer, double bias, double c) { //Selects the best child. Maximizes if is maximumPlayer, minimizes if is minimumPlayer.
        int bestIndex = -1;
        int childIndex;
        double bestScore, childScore, schedule, child_MC_score, child_AMAF_score;

        if (maximumPlayer) { //Two cases for speed purposes. Practically the same except for score comparison
            bestScore = -1;

            for (int i = 0; i < children[index].size(); i++) {
                childIndex = children[index].get(i);
                if (MC_count[childIndex] == 0) return i;

//                schedule = (double) AMAF_count[childIndex] / (MC_count[childIndex] + AMAF_count[childIndex] + bias * MC_count[childIndex] * AMAF_count[childIndex]);
//                schedule = Math.sqrt(bias / (3 * MC_count[childIndex] + bias));
                schedule = 0;

                child_MC_score = (MC_wins[childIndex] + 0.5 * MC_draws[childIndex]) / MC_count[childIndex];
                child_AMAF_score = (AMAF_wins[childIndex] + 0.5 * AMAF_draws[childIndex]) / AMAF_count[childIndex];
                childScore = (1 - schedule) * child_MC_score + schedule * child_AMAF_score + c * Math.sqrt(Math.log(MC_count[index]) / MC_count[childIndex]);

                if (childScore > bestScore) {
                    bestIndex = i;
                    bestScore = childScore;
                }
            }
        } else {
            bestScore = 2;

            for (int i = 0; i < children[index].size(); i++) {
                childIndex = children[index].get(i);
                if (MC_count[childIndex] == 0) return i;

//                schedule = (double) AMAF_count[childIndex] / (MC_count[childIndex] + AMAF_count[childIndex] + bias * MC_count[childIndex] * AMAF_count[childIndex]);
//                schedule = Math.sqrt(bias / (3 * MC_count[childIndex] + bias));
                schedule = 0;

                child_MC_score = (MC_wins[childIndex] + 0.5 * MC_draws[childIndex]) / MC_count[childIndex];
                child_AMAF_score = (AMAF_wins[childIndex] + 0.5 * AMAF_draws[childIndex]) / AMAF_count[childIndex];
                childScore = (1 - schedule) * child_MC_score + schedule * child_AMAF_score - c * Math.sqrt(Math.log(MC_count[index]) / MC_count[childIndex]);

                if (childScore < bestScore) {
                    bestIndex = i;
                    bestScore = childScore;
                }
            }
        }

        return bestIndex;
    }

    public Action bestMove(boolean maximumPlayer, double bias, boolean output) { //Returns the best move from root
        int bestIndex = -1;
        double bestScore = maximumPlayer ? -1 : 2;
        int childIndex;
        double childScore, schedule, child_MC_score, child_AMAF_score;

        for (int i = 0; i < children[root].size(); i++) {
            childIndex = children[root].get(i);
            if (MC_count[childIndex] == 0) continue;

//          schedule = (double) AMAF_count[childIndex] / (MC_count[childIndex] + AMAF_count[childIndex] + bias * MC_count[childIndex] * AMAF_count[childIndex]);
//            schedule = Math.sqrt(bias / (3 * MC_count[childIndex] + bias));
            schedule = 0;

            child_MC_score = (MC_wins[childIndex] + 0.5 * MC_draws[childIndex]) / MC_count[childIndex];
            child_AMAF_score = (AMAF_wins[childIndex] + 0.5 * AMAF_draws[childIndex]) / AMAF_count[childIndex];
            childScore = (1 - schedule) * child_MC_score + schedule * child_AMAF_score;

            //Add Random
            if (noise != 0) childScore += (random.nextDouble() - 0.5) * (noise - 2 * noise * Math.abs(0.5 - childScore));

            if (maximumPlayer) {
                if (childScore > bestScore) {
                    bestIndex = i;
                    bestScore = childScore;
                }
            } else {
                if (childScore < bestScore) {
                    bestIndex = i;
                    bestScore = childScore;
                }
            }

            //TEST CODE
            if (output) System.out.println("Column: " + action[childIndex].print() + " Score: " + childScore + " Visits: " + MC_count[childIndex]);
        }

        pruneNot(bestIndex);
//        nextNode(bestIndex);
//        root = index;

        //TEST CODE
        if (output) System.out.println("Best Move: " + getAction().print() + " Score: " + bestScore);
        return getAction();
    }

    public void backup(double z) {
        if (z == 1) {
            do {
                MC_count[index]++;
                MC_wins[index]++;
                AMAF_count[index]++;
                AMAF_wins[index]++;
                index = parent[index];
            } while (index != root);
        } else if (z == 0.5) {
            do {
                MC_count[index]++;
                MC_draws[index]++;
                AMAF_count[index]++;
                AMAF_draws[index]++;
                index = parent[index];
            } while (index != root);
        } else {
            do {
                MC_count[index]++;
                AMAF_count[index]++;
                index = parent[index];
            } while (index != root);
        }

        MC_count[index]++;
        AMAF_count[index]++;
    }

    public List<Action> getChildActions() {
        List<Action> actions = new ArrayList();
        for (int i = 0; i < children[index].size(); i++) {
            actions.add(action[children[index].get(i)]);
        }
        return actions;
    }

    public boolean hasChildren() {
        return children[index].size() != 0 ? true : false;
    }

    public Action getAction() {
        return action[index];
    }

//    public int MC_count() {
//        return MC_count[index];
//    }
//
//    public int MC_wins() {
//        return MC_wins[index];
//    }
//
//    public int MC_draws() {
//        return MC_draws[index];
//    }
//
//    public int AMAF_count() {
//        return AMAF_count[index];
//    }
//
//    public int AMAF_wins() {
//        return AMAF_wins[index];
//    }
//
//    public int AMAF_draws() {
//        return AMAF_draws[index];
//    }

    public static void main(String[] args) {
        Node node = new Node(15000000);
    }
}
