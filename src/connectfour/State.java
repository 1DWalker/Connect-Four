package connectfour;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class State {
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
    static final long EVEN1 = 186172425540906L; //1 on even rows
    static final long ODD1 = 93086212770453L; //1 on odd rows

    public State() {
        bitboard = new long[2];
        bitboardMirror = new long[2];
        height = new byte[WIDTH];
        heightMirror = new byte[WIDTH];
        moves = new int[SIZE];
        reset();
    }

    public State(State state) { //Creates a copy
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
        for (int i = 0; i < WIDTH; i++) {
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

        //Check if antidecisive
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

        //Block first multi threat seen that isn't won by playing above
        for (i = 0; i < goodMoves.size(); i++) {
            col = goodMoves.get(i);
            if (isPlayable(col)) {
                if (multiThreat(bitboard[(ply + 1) & 1], col)) {
                    return new Action(col);
                }
            }
        }

        //Play on double threats column
        for (i = 0; i < goodMoves.size(); i++) {
            height[i]++;
            if (doubleThreatAbove(goodMoves.get(i))) {
                height[i]--;
                return new Action(goodMoves.get(i));
            }
            height[i]--;
        }
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

    public void flip() { //Not very robust, but gets the job done for data collection
        bitboard[0] = bitboardMirror[0];
        bitboard[1] = bitboardMirror[1];

        byte[] h = new byte[7];
        System.arraycopy(heightMirror, 0, h, 0, heightMirror.length);

        for (int i = 0; i < 7; i++) {
            height[i] = h[6 - i];
        }
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

    public String getStringCode() { //Prints the state as a string. For data collection
        //Write the player, data for X, then finally data for O
        //Start topleft to topright, then go down by row
        String line = getPlayer() ? "1 " : "0 "; //Write the player

        for (int i = 0; i <= 1; i++) {
            for (int row = 5; row >= 0; row--) {
                for (int col = 0; col <= 6; col++) {
                    line +=((bitboard[i] & (1L << (7*col + row))) != 0) ? "1 " : "0 ";
                }
            }
        }
        return line;
    }
}

