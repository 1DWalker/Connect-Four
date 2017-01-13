import java.util.Scanner;

public class ConnectFour {
	
	public static void printBoard(byte[][] board) {
		//System.out.print("  ");
		for (int num = 1; num <= board.length; num++) {
			System.out.print(num + " ");
		} 
		
		System.out.println();
		
		for (int row = 0; row < board[0].length; row++) {
			//System.out.print((row + 1) + " ");
			
			for (int col = 0; col < board.length; col++) {
				switch (board[col][row]) {
					case 0: 
						System.out.print(". ");
						break;
					case 1: 
						System.out.print("X ");
						break;
					default:
						System.out.print("O ");
						break;
				}
			}
			
			System.out.println();
		}
		
		System.out.println();
	}
	
	public static int[] userMoveCoordinates(byte[][] board, String player) {
		int[] userMoveCoordinates = new int[2];
		Scanner keyboard = new Scanner(System.in);
		
		int playerNumber = player.equals("X") ? 1 : 2; //1 if X, 2 if O
		
		legalMoveCheck:
		while (true) {
			System.out.print("Player " + player + "'s move: ");
			int playerMove = keyboard.nextInt();
			
			if (playerMove < 1 | playerMove > board.length) {
				System.out.println("Out of bounds.");
				continue;
			}
			
			for (int i = board[0].length - 1; i >= 0; i--) {
				if (board[playerMove - 1][i] == 0) {
					userMoveCoordinates[0] = playerMove - 1;
					userMoveCoordinates[1] = i;
					break legalMoveCheck;
				}
			}
			
			System.out.println("Column filled.");
		}
		
		return userMoveCoordinates;
	}
	
	public static void changeBoard(byte[][] board, int[] moveChoiceCoordinates, String player) {
		board[moveChoiceCoordinates[0]][moveChoiceCoordinates[1]] = player.equals("X") ? (byte) 1 : (byte) 2;
	}
		
	public static boolean winningConditionCheck(byte[][] board, byte[][][] groupTable, int[] moveChoiceCoordinates) { //FINISH THIS. Arrays idea, for each square on board.
		return false;
	}	
		
	public static void main(String[] args) {
		Scanner keyboard = new Scanner(System.in);
		
		int rowLength = 7;
		int columnLength = 6;
		byte[][] board = new byte[rowLength][columnLength]; //row length * column length
		byte[][][] groupTable = new byte[rowLength][columnLength][4]; //For use of winningConditionCheck
		int sentinel = 1;
		
		while (sentinel == 1) {
			int gameLength = 0;
			String player = "X";
			
			while (gameLength < rowLength * columnLength) {
				printBoard(board);
				int[] moveChoiceCoordinates = userMoveCoordinates(board, player);
				changeBoard(board, moveChoiceCoordinates, player);
				if (winningConditionCheck(board, groupTable, moveChoiceCoordinates)) {
					System.out.println("Player " + player + " has won!");
					System.out.println();
					break;
				}
				
				player = player.equals("X") ? "O" : "X";
				gameLength++;
				
				if (gameLength == rowLength * columnLength) {
					System.out.println("Draw!");
				}
			}
			
			System.out.print("1 - play again, 2 - exit: ");
			sentinel = keyboard.nextInt();
		}
	}
}