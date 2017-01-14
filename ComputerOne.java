import java.util.Random;
//computer one makes a move, given the board, time controls, current time, and others. Tell me what parameters you need! Ex: last player's move?

public class ComputerOne {

    public static int[] makeMove(int[][] board, long[] timeControl, long playerTime) {
		//Un-comment those comments standing out below & delete 
		//"int columnRandom =  randomInt.nextInt(board.length);" for an awesome increase in playing strength ;) 
		//+50 elo, which is the amount of elo a top chess engine gains per year of development. 
		//Can you figure out what causes the increase in strength?
		int[] computerMoveCoordinates = new int[2];
		Random randomInt = new Random();
		int l = 1;
		
		//Select move to play randomly
		moveSelection:
		while (true) {
			//int columnRandom =  randomInt.nextInt(board.length);
			int columnRandom =  randomInt.nextInt(board.length - 1);
			l++;
			if (l == 50) columnRandom = 6;
			
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