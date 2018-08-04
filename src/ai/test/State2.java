package ai.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import connectfour.Action;
import connectfour.State;

public class State2 {
    public long[] bitboard = new long[2];
    public long[] bitboardMirror = new long[2];
    public int moves[], ply;
    public byte height[], heightMirror[]; // holds bit index of lowest free square
    public double baseScore = 2;

    static final Random random = new Random();
    static final int WIDTH = 7;
    static final int HEIGHT = 6;

    // bitmask corresponds to board as follows in 7x6 case:
//  .  .  .  .  .  .  .  TOP
//  5 12 19 26 33 40 47
//  4 11 18 25 32 39 46
//  3 10 17 24 31 38 45
//  2  9 16 23 30 37 44
//  1  8 15 22 29 36 43
//  0  7 14 21 28 35 42  BOTTOM
    static final int H1 = HEIGHT + 1;
    static final int H2 = HEIGHT + 2;
    static final int SIZE = HEIGHT * WIDTH;
    static final int SIZE1 = H1 * WIDTH;
    static final long ALL1 = (1L << SIZE1) - 1L; // assumes SIZE1 < 63
    static final int COL1 = (1 << H1) - 1;
    static final long BOTTOM = ALL1 / COL1; // has bits i*H1 set
    static final long TOP = BOTTOM << HEIGHT;
    static final long MAP1 = ALL1 ^ TOP;
    static final long EVEN1 = 186172425540906L; //1 on even rows
    static final long ODD1 = 93086212770453L; //1 on odd rows
    public State2() {
        bitboard = new long[2];
        bitboardMirror = new long[2];
        height = new byte[WIDTH];
        heightMirror = new byte[WIDTH];
        moves = new int[SIZE];
        reset();
    }

    public State2(State state) { //Creates a copy
        System.arraycopy(state.bitboard, 0, bitboard, 0, 2);
        System.arraycopy(state.bitboardMirror, 0, bitboardMirror, 0, 2);
        moves = new int[state.moves.length];
        System.arraycopy(state.moves, 0, moves, 0, moves.length);
        height = new byte[state.height.length];
        System.arraycopy(state.height, 0, height, 0, height.length);
        heightMirror = new byte[state.heightMirror.length];
        System.arraycopy(state.heightMirror, 0, heightMirror, 0, heightMirror.length);
        ply = state.ply;
    }

    public State2(State2 state) { //Creates a copy
        System.arraycopy(state.bitboard, 0, bitboard, 0, 2);
        System.arraycopy(state.bitboardMirror, 0, bitboardMirror, 0, 2);
        moves = new int[state.moves.length];
        System.arraycopy(state.moves, 0, moves, 0, moves.length);
        height = new byte[state.height.length];
        System.arraycopy(state.height, 0, height, 0, height.length);
        heightMirror = new byte[state.heightMirror.length];
        System.arraycopy(state.heightMirror, 0, heightMirror, 0, heightMirror.length);
        ply = state.ply;
    }

    void reset() {
        ply = 0;
        bitboard[0] = bitboard[1] = 0L;
        for (int i = 0; i < WIDTH; i++) {
            height[i] = heightMirror[WIDTH - 1 - i] = (byte) (H1 * i);
        }
    }

    public long positionCode() {
        return 2 * bitboard[0] + bitboard[1] + BOTTOM;
        // bitboard[0] + bitboard[1] + BOTTOM forms bitmap of heights
        // so that positioncode() is a complete board encoding
    }

    // return whether columns col has room
    final boolean isPlayable(int col) {
        return isLegal(bitboard[ply & 1] | (1L << height[col]));
    }

    // return whether newBoard lacks overflowing column
    final boolean isLegal(long newBoard) {
        return (newBoard & TOP) == 0;
    }

    // return whether newBoard is legal and includes a win
    final boolean isLegalHasWon(long newBoard) {
        return isLegal(newBoard) && hasWon(newBoard);
    }

    // return whether newBoard includes a win
    final boolean hasWon(long newBoard) {
        long y = newBoard & (newBoard >> HEIGHT);
        if ((y & (y >> 2 * HEIGHT)) != 0) { // check diagonal \
            return true;
        }
        y = newBoard & (newBoard >> H1);
        if ((y & (y >> 2 * H1)) != 0) { // check horizontal -
            return true;
        }
        y = newBoard & (newBoard >> H2);
        if ((y & (y >> 2 * H2)) != 0) { // check diagonal /
            return true;
        }
        y = newBoard & (newBoard >> 1);
        return (y & (y >> 2)) != 0; // check vertical |
    }

    final boolean isDecisive(long bitboard, int col) { //Checks if a move is a winning move
        bitboard ^= 1L << height[col];
        return hasWon(bitboard);
    }

    final boolean multiThreat(long bitboard, int col) { //Checks if a move creates a multithreat
        int threats = 0;
        bitboard ^= 1L << height[col]++;
        for (int i = Math.max(col - 3, 0); i <= Math.min(col + 3, 6); i++) {
            if (!isPlayable(i)) continue;
            bitboard ^= 1L << height[i];
            if (hasWon(bitboard)) {
                if (++threats > 1) {
                    height[col]--;
                    return true;
                }
            }
            bitboard ^= 1L << height[i];
        }
        height[col]--;
        return false;
    }

    final boolean doubleThreat(long us, long them) { //Returns whether a doubleThreat exists
        long majorThreats = majorThreatsMap(us, them);
        return (majorThreats & (majorThreats << 1)) != 0;
    }

    final boolean doubleThreatAbove(long us, long them, int col) {
        long col1 = 0;
        for (int i = 7 * col; i < 7 * (col + 1); i++) {
            col1 |= 1L << i;
        }
        long majorThreats = majorThreatsMap(us, them);
        return (col1 & majorThreats & (majorThreats << 1)) != 0;
    }

    final boolean doubleThreatAbove(int col) {
        byte startHeight = height[col];

        while (true) {
            if (!isPlayable(col)) {
                break;
            }

            if (isDecisive(bitboard[(ply + 1) & 1], col)) {
                break;
            }

            if (!isDecisive(bitboard[ply & 1], col)) {
                height[col]++;
                continue;
            }

            height[col]++;
            if (!isPlayable(col)) {
                break;
            }

            if (isDecisive(bitboard[ply & 1], col)) {
                height[col] = startHeight;
                return true;
            }
        }

        height[col] = startHeight;
        return false;
    }

    final long majorThreatsMap(long us, long them) {
        long majorThreats = (us << 1) & (us << 2) & (us << 3); //vertical
        majorThreats |= (us >> 7) & (us >> 14) & (us >> 21) | (us >> 7) & (us >> 14) & (us << 7) | (us >> 7) & (us << 7) & (us << 24) | (us << 7) & (us << 14) & (us << 21); //horizontal
        majorThreats |= (us >> 8) & (us >> 16) & (us >> 24) | (us >> 8) & (us >> 16) & (us << 8) | (us >> 8) & (us << 8) & (us << 16) | (us << 8) & (us << 16) & (us << 24); //diagonal up
        majorThreats |= (us >> 6) & (us >> 12) & (us >> 18) | (us >> 6) & (us >> 12) & (us << 6) | (us >> 6) & (us << 6) & (us << 12) | (us << 6) & (us << 12) & (us << 18); //diagonal down
        return (~us & ~them & MAP1) & (majorThreats);
    }

    //Return true if a move in column col creates a major threat
    final List<Integer> majorThreats(long us, long them, List<Integer> goodMoves) {
        // check | down. Basically checking if there are two of our pieces below
//        if (row > 1) {
//            if ((us & (1L << (row - 1))) != 0 && (us & (1L << (row - 2))) != 0) return true;
//        }
        List<Integer> majorThreats = new ArrayList(7);
        int i;

        long possibleMoves = 0;
        for (i = 0; i < 7; i++) {
            if (isPlayable(i)) {
                possibleMoves |= 1L << height[i];
            }
        }

        long notThem = ~them & MAP1;

        long beforeHor = (us >> 7) & (us >> 14) & (us >> 21);
        beforeHor |= (us >> 7) & (us >> 14) & (us << 7);
        beforeHor |= (us >> 7) & (us << 7) & (us << 24);
        beforeHor |= (us << 7) & (us << 14) & (us << 21);
        beforeHor &= notThem;

        long beforeDiaUp = (us >> 8) & (us >> 16) & (us >> 24);
        beforeDiaUp |= (us >> 8) & (us >> 16) & (us << 8);
        beforeDiaUp |= (us >> 8) & (us << 8) & (us << 16);
        beforeDiaUp |= (us << 8) & (us << 16) & (us << 24);
        beforeDiaUp &= notThem;

        long beforeDiaDown = (us >> 6) & (us >> 12) & (us >> 18);
        beforeDiaDown |= (us >> 6) & (us >> 12) & (us << 6);
        beforeDiaDown |= (us >> 6) & (us << 6) & (us << 12);
        beforeDiaDown |= (us << 6) & (us << 12) & (us << 18);
        beforeDiaDown &= notThem;

        long after;

        for (i = 0; i < goodMoves.size(); i++) {
            us ^= 1L << height[goodMoves.get(i)];
            //Horizontal
            after = (us >> 7) & (us >> 14) & (us >> 21);
            after |= (us >> 7) & (us >> 14) & (us << 7);
            after |= (us >> 7) & (us << 7) & (us << 24);
            after |= (us << 7) & (us << 14) & (us << 21);
            after &= notThem;

            if (((beforeHor ^ after) & ~possibleMoves) != 0) {
                majorThreats.add(goodMoves.get(i));
                us ^= 1L << height[goodMoves.get(i)];
                continue;
            }

            //Diagonal up
            after = (us >> 8) & (us >> 16) & (us >> 24);
            after |= (us >> 8) & (us >> 16) & (us << 8);
            after |= (us >> 8) & (us << 8) & (us << 16);
            after |= (us << 8) & (us << 16) & (us << 24);
            after &= notThem;

            if (((beforeDiaUp ^ after) & ~possibleMoves) != 0) {
                majorThreats.add(goodMoves.get(i));
                us ^= 1L << height[goodMoves.get(i)];
                continue;
            }

            //Diagonal Down
            after = (us >> 6) & (us >> 12) & (us >> 18);
            after |= (us >> 6) & (us >> 12) & (us << 6);
            after |= (us >> 6) & (us << 6) & (us << 12);
            after |= (us << 6) & (us << 12) & (us << 18);
            after &= notThem;

            if (((beforeDiaDown ^ after) & ~possibleMoves) != 0) majorThreats.add(goodMoves.get(i));

            us ^= 1L << height[goodMoves.get(i)];
        }

        return majorThreats;

//        //Check horizontal -
//        if (horizontalMajorThreat(us, them, col, possibleMoves)) return true;
//
//        //Check diagonal /
//        if (diagonalUpMajorThreat(us, them, col, possibleMoves)) return true;
//
//        //Check diagonal \
//        if (diagonalDownMajorThreat(us, them, col, possibleMoves)) return true;
//
//        return false;
    }

    private boolean horizontalMajorThreat(long us, long them, int col, long possibleMoves) {
        long notThem = ~them & MAP1;
        long before = notThem & (us >> 7) & (us >> 14) & (us >> 21);
        before |= notThem & (us >> 7) & (us >> 14) & (us << 7);
        before |= notThem & (us >> 7) & (us << 7) & (us << 24);
        before |= notThem & (us << 7) & (us << 14) & (us << 21);

        us ^= 1L << height[col];
        long after = notThem & (us >> 7) & (us >> 14) & (us >> 21);
        after |= notThem & (us >> 7) & (us >> 14) & (us << 7);
        after |= notThem & (us >> 7) & (us << 7) & (us << 24);
        after |= notThem & (us << 7) & (us << 14) & (us << 21);

        return ((before ^ after) & ~possibleMoves) != 0;
//        return (before ^ after) != 0;
    }

    private boolean diagonalUpMajorThreat(long us, long them, int col, long possibleMoves) {
        long notThem = ~them & MAP1;
        long before = notThem & (us >> 8) & (us >> 16) & (us >> 24);
        before |= notThem & (us >> 8) & (us >> 16) & (us << 8);
        before |= notThem & (us >> 8) & (us << 8) & (us << 16);
        before |= notThem & (us << 8) & (us << 16) & (us << 24);

        us ^= 1L << height[col];
        long after = notThem & (us >> 8) & (us >> 16) & (us >> 24);
        after |= notThem & (us >> 8) & (us >> 16) & (us << 8);
        after |= notThem & (us >> 8) & (us << 8) & (us << 16);
        after |= notThem & (us << 8) & (us << 16) & (us << 24);

        return ((before ^ after) & ~possibleMoves) != 0;
//        return (before ^ after) != 0;
    }

    private boolean diagonalDownMajorThreat(long us, long them, int col, long possibleMoves) {
        long notThem = ~them & MAP1;
        long before = notThem & (us >> 6) & (us >> 12) & (us >> 18);
        before |= notThem & (us >> 6) & (us >> 12) & (us << 6);
        before |= notThem & (us >> 6) & (us << 6) & (us << 12);
        before |= notThem & (us << 6) & (us << 12) & (us << 18);

        us ^= 1L << height[col];
        long after = notThem & (us >> 6) & (us >> 12) & (us >> 18);
        after |= notThem & (us >> 6) & (us >> 12) & (us << 6);
        after |= notThem & (us >> 6) & (us << 6) & (us << 12);
        after |= notThem & (us << 6) & (us << 12) & (us << 18);

        return ((before ^ after) & ~possibleMoves) != 0;
//        return (before ^ after) != 0;
    }

    public boolean allColumnsEven() {
        for (int i = 0; i < 7; i++) {
            int k = height[i] % 7;
            if (!(k == 0 || k == 2 || k == 4 || k == 6)) return false;
        }

        return true;
    }

    public double zugzwang() { //Returns the result if player O follows a zugzwang strategy, in the simplest case of zugzwangs. Score in X's perspective. 1 may mean unsure
        //Assumes the allColumnsEven() condition has been satisfied
        //X gets all odd squares
        long X = bitboard[0] | (ODD1 & ~bitboard[1]);
        long O = bitboard[1] | (EVEN1 & ~bitboard[0]);

        if (hasWon(X)) return 1;
        if (hasWon(O)) return 0;
        return 0.5;
    }

    void backMove() {
        int n;
        n = moves[--ply];
        bitboard[ply & 1] ^= 1L << --height[n];
        bitboardMirror[ply & 1] ^= 1L << --heightMirror[n];
    }

    public void makeMove(int n) {
        bitboard[ply & 1] ^= 1L << height[n]++;
        bitboardMirror[ply & 1] ^= 1L << heightMirror[n]++;
        moves[ply++] = n;
    }

    public void makeMove(Action action) {
        makeMove(action.move);
    }

    public long getMoveLong(int n) {
        return 1L << height[n];
    }

    public int getPly() {
        return ply;
    }

    public boolean movePlayed(int ply, long moveLong) { //Returns if action in the form of ply and a move as a long is in this board state
        return (bitboard[ply & 1] & moveLong) != 0;
    }

    public boolean terminal() {
        return ply == 42 || hasWon(bitboard[(ply + 1) & 1]);
    }

    public boolean getPlayer() {
        return (ply & 1) == 0;
    }

    public boolean isSymmetric() {
        return bitboard[0] == bitboardMirror[0] & bitboard[1] == bitboardMirror[1];
    }

    public List<Action> getActions() {
        final int END = isSymmetric() ? 4 : 7;
        List<Action> playableMoves = new ArrayList(WIDTH);
        int i;
        for (i = 0; i < END; i++) {
            if (isPlayable(i)) {
                playableMoves.add(new Action(i));
            }
        }

        //Check if decisive
        for (i = 0; i < playableMoves.size(); i++) {
            if (isDecisive(bitboard[ply & 1], playableMoves.get(i).move)) {
                return playableMoves.subList(i, i + 1);
            }
        }

        //Check if antidecisive
        for (i = 0; i < playableMoves.size(); i++) {
            if (isDecisive(bitboard[(ply + 1) & 1], playableMoves.get(i).move)) {
                return playableMoves.subList(i, i + 1);
            }
        }

        return playableMoves;
    }

    public Action defaultPolicy(List<Action> availableActions) { //Might replace later with heuristic + select best child
        return availableActions.get(random.nextInt(availableActions.size()));
    }

    public Action defaultPolicy() { //Select a strong action
        final int END = isSymmetric() ? 4 : 7;
        int i;
        List<Integer> playableMoves = new ArrayList(WIDTH);
        for (i = 0; i < END; i++) {
            if (isPlayable(i)) {
                playableMoves.add(i);
            }
        }

        //Check if decisive
        for (i = 0; i < playableMoves.size(); i++) {
            if (isDecisive(bitboard[ply & 1], playableMoves.get(i))) {
                return new Action(playableMoves.get(i));
            }
        }

//        Check if antidecisive
        for (i = 0; i < playableMoves.size(); i++) {
            if (isDecisive(bitboard[(ply + 1) & 1], playableMoves.get(i))) {
                return new Action(playableMoves.get(i));
            }
        }

        List<Integer> goodMoves = new ArrayList(playableMoves);
//        //Zugzwang
        if ((ply & 1) == 1) {
            for (Integer col : goodMoves) {
                if (!isPlayable(col)) continue;
                makeMove(col);
                if (allColumnsEven()) {
                    double score = zugzwang();
                    if (score == 0) {
                        backMove();
                        return new Action(col);
                    } else if (score == 0.5) {
                        baseScore = score;
                    }
                }
                backMove();
            }
        }

        int col;
        //If X, remove moves that allow O to play zugzwang and win
        if ((ply & 1) == 0) {
            for (i = 0; i < goodMoves.size(); i++) {
                col = goodMoves.get(i);
                if (!isPlayable(col)) continue;
                makeMove(col);
                for (int k = 0; k < goodMoves.size(); k++) {
                    int col2 = goodMoves.get(k);
                    if (!isPlayable(col2)) continue;
                    makeMove(col2);
                    if (allColumnsEven()) {
                        double score = zugzwang();
                        if (score == 0) {
                            goodMoves.remove(i);
                            i--;
                            backMove();
                            break;
                        }
                    }
                    backMove();
                }
                backMove();
            }
        }

        //Remove moves that allow the opponent to win by playing above
        for (i = 0; i < goodMoves.size(); i++) {
            col = goodMoves.get(i);
            height[col]++;
            if (!isPlayable(col)) {
                height[col]--;
                continue;
            }
            if (isDecisive(bitboard[(ply + 1) & 1], col)) {
                height[col]--;
                goodMoves.remove(i);
                i--;
                continue;
            }
            height[col]--;
        }

        //Play multi threat if possible
        for (i = 0; i < goodMoves.size(); i++) {
            col = goodMoves.get(i);
            if (isPlayable(col)) {
                if (multiThreat(bitboard[ply & 1], col)) {
                    return new Action(col);
                }
            }
        }

//        //Remove moves that allow the opponent to play a multi threat in the vicinity
//        for (i = 0; i < goodMoves.size(); i++) {
//            col = goodMoves.get(i);
//            if (isPlayable(col)) {
//                makeMove(col);
//                for (int k = Math.max(col - 3, 0); k <= Math.min(col + 3, 6); k++) {
//                    if (isPlayable(k)) {
//                        if (multiThreat(bitboard[ply & 1], k)) {
//                            goodMoves.remove(i);
//                            i--;
//                            break;
//                        }
//                    }
//                }
//                backMove();
//            }
//        }

        //Block first multi threat seen
        for (i = 0; i < goodMoves.size(); i++) {
            col = goodMoves.get(i);
            if (isPlayable(col)) {
                if (multiThreat(bitboard[(ply + 1) & 1], col)) {
                    return new Action(col);
                }
            }
        }

        //Play on double threats column
        if (doubleThreat(bitboard[ply & 1], bitboard[(ply + 1) & 1])) {
            for (i = 0; i < goodMoves.size(); i++) {
                if (doubleThreatAbove(bitboard[ply & 1], bitboard[(ply + 1) & 1], goodMoves.get(i))) {
                    return new Action(goodMoves.get(i));
                }
            }
        }

//        //Create double threats
//        for (i = 0; i < goodMoves.size(); i++) {
//            col = goodMoves.get(i);
//            if (isPlayable(col)) {
//                makeMove(col);
//                if (doubleThreat(bitboard[(ply + 1) & 1], bitboard[ply & 1])) {
//                    backMove();
//                    return new Action(col);
//                }
//                backMove();
//            }
//        }
//
//        //Remove moves that allow the opponent to play a double threat in the vicinity
//        for (i = 0; i < goodMoves.size(); i++) {
//            col = goodMoves.get(i);
//            if (isPlayable(col)) {
//                makeMove(col);
//                for (int k = Math.max(col - 3, 0); k <= Math.min(col + 3, 6); k++) {
//                    if (isPlayable(k)) {
//                        makeMove(k);
//                        if (doubleThreat(bitboard[(ply + 1) & 1], bitboard[ply & 1])) {
//                            goodMoves.remove(i);
//                            i--;
//                            backMove();
//                            break;
//                        }
//                        backMove();
//                    }
//                }
//                backMove();
//            }
//        }

//        //Play on double threats column
//        for (i = 0; i < goodMoves.size(); i++) {
//            height[i]++;
//            if (doubleThreatAbove(goodMoves.get(i))) {
//                if (!found) {
//                    System.out.println("Col: " + (goodMoves.get(i) + 1) + " Player: " + getPlayer());
//                    print();
//
//                    height[i]--;
//                    doubleThreatAbove(bitboard[ply  & 1], bitboard[(ply + 1) & 1], goodMoves.get(i));
//                    System.exit(1);
//                }
//                height[i]--;
//                return new Action(goodMoves.get(i));
//            }
//            height[i]--;
//        }

        //Create columns with consecutive threats
//        for (i = 0; i < goodMoves.size(); i++) {
//            col = goodMoves.get(i);
//            bitboard[ply & 1] ^= 1L << height[col]++;
//
//            int end = Math.min(col + 3, WIDTH);
//            for (int k = Math.max(col - 3, 0); k < end; k++) {
//                if (doubleThreatAbove(k)) {
//                    bitboard[ply & 1] ^= 1L << --height[col];
//                    System.out.println("col " + (col + 1));
//                    print();
//                    return new Action(col);
//                }
//            }
//
//            bitboard[ply & 1] ^= 1L << --height[col];
//        }

////        Block moves that create consecutive threats
//        ply++;
//        for (i = 0; i < goodMoves.size(); i++) {
//            col = goodMoves.get(i);
//            bitboard[ply & 1] ^= 1L << height[col]++;
//            int end = Math.min(col + 3, WIDTH);
//
//            for (int k = Math.max(col - 3, 0); k < end; k++) {
//                if (doubleThreatAbove(k)) {
//                    bitboard[ply & 1] ^= 1L << --height[col];
//                    ply--;
//                    return new Action(col);
//                }
//            }
//            bitboard[ply & 1] ^= 1L << --height[col];
//        }
//        ply--;

        //Remove moves that lose a win threat on above row
        for (i = 0; i < goodMoves.size(); i++) {
            col = goodMoves.get(i);
            height[col]++;
            if (!isPlayable(col)) {
                height[col]--;
                continue;
            }
            if (isDecisive(bitboard[ply & 1], col)) {
                height[col]--;
                goodMoves.remove(i);
                i--;
                continue;
            }
            height[col]--;
        }

////      Play a good move
////      Play moves that create a major threat
//        List<Integer> majorThreats;
//        majorThreats = majorThreats(bitboard[ply & 1], bitboard[(ply + 1) & 1], goodMoves);
//        if (majorThreats.size() > 0) {
//            return new Action(majorThreats.get(random.nextInt(majorThreats.size())));
//        }
//
////      Play on first square seen in which the opponent can make a major threat
//        majorThreats = majorThreats(bitboard[(ply + 1) & 1], bitboard[ply & 1], goodMoves);
//        if (majorThreats.size() > 0) {
//            return new Action(majorThreats.get(random.nextInt(majorThreats.size())));
//        }
//
////        Play in the center column if possible
//        if (height[3] != 27) {
//            for (Integer move : goodMoves) {
//                if (move == 3) return new Action(move);
//            }
//        }

        if (goodMoves.size() != 0) {
            return new Action(goodMoves.get(random.nextInt(goodMoves.size())));
        } else {
            return new Action(playableMoves.get(random.nextInt(playableMoves.size())));
        }
    }

    public double score() {
        if (hasWon(bitboard[0])) {
            if (baseScore < 1) {
                baseScore = 2;
                return baseScore;
            }
            return 1;
        }
        if (hasWon(bitboard[1])) return 0;
        return 0.5; //draw case
    }

    public void print() {
        for (int w=0; w<WIDTH; w++)
            System.out.print(" "+(w+1));
        System.out.print("\n");
        for (int h=HEIGHT-1; h>=0; h--) {
            for (int w = h; w < SIZE1; w += H1) {
                long mask = 1L << w;
                System.out.print((bitboard[0] & mask) != 0 ? " X" :
                        (bitboard[1] & mask) != 0 ? " O" : " .");
            }
            System.out.print("\n");
        }
    }

    public void print(boolean mirror) {
        if (!mirror) print();
        else {
            for (int w=0; w<WIDTH; w++)
                System.out.print(" "+(w+1));
            System.out.print("\n");
            for (int h=HEIGHT-1; h>=0; h--) {
                for (int w = h; w < SIZE1; w += H1) {
                    long mask = 1L << w;
                    System.out.print((bitboardMirror[0] & mask) != 0 ? " X" :
                            (bitboardMirror[1] & mask) != 0 ? " O" : " .");
                }
                System.out.print("\n");
            }
        }
    }

    public void print(long bitboard) {
        for (int w=0; w<WIDTH; w++)
            System.out.print(" "+(w+1));
        System.out.print("\n");
        for (int h=HEIGHT-1; h>=0; h--) {
            for (int w = h; w < SIZE1; w += H1) {
                long mask = 1L << w;
                System.out.print((bitboard & mask) != 0 ? (ply % 2 == 0 ? " X" : " O") : " .");
            }
            System.out.print("\n");
        }
    }

    //Testing
    public Action makeRandomMoves() {
        final int END = isSymmetric() ? 4 : 7;
        int i;
        List<Integer> playableMoves = new ArrayList(WIDTH);
        for (i = 0; i < END; i++) {
            if (isPlayable(i)) {
                playableMoves.add(i);
            }
        }

        return new Action(playableMoves.get(random.nextInt(playableMoves.size())));
    }

    public static void printLong(long num) {
        for(int i = 0; i < Long.numberOfLeadingZeros(num); i++) {
            System.out.print('0');
        }
        System.out.println(Long.toBinaryString(num));
    }
}

