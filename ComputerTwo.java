import java.util.Random;
//computer one makes a move, given the board, time controls, current time, and others. Tell me what parameters you need! Ex: last player's move?

public class ComputerTwo {

    public static int[] makeMove(int[][] board, int[] lastMoveCoordinates, long[] timeControl, long playerTime) {
		int[] computerMoveCoordinates = new int[2];
		Random randomInt = new Random();
	
		//Select move to play randomly
		moveSelection:
		while (true) {
			int columnRandom =  randomInt.nextInt(board.length);
			
			//Is the column filled?
			for (int i = board[0].length - 1; i >= 0; i--) {
				if (board[columnRandom][i] == 0) {
					computerMoveCoordinates[0] = columnRandom;
					computerMoveCoordinates[1] = i;
					break;
				}
				
				if (i == 0) continue moveSelection;
			}
			
			break;
		}
		
		return computerMoveCoordinates;
    }
}
    
    