package connectfour;

public class Action {
    public int move;

    public Action(int move) {
        this.move = move;
    }

    public boolean sameAs(Action action) { //*** Method should be changed to convert both moves to exact row, col, then compare
        return this.move == action.move;
    }

    public String print() {
        return Integer.toString(move + 1);
    }
}
