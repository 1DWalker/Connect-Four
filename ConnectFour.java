import java.util.Scanner;
import java.util.Random;
import org.apache.commons.math3.special.Erf; //For Likelihood of Superiority test

public class ConnectFour {
	
	public static void main(String[] args) {
		//Options. 1 for YES, 0 for NO, unless a measure size
		int options[] = new int[8];
		//Width of board
		options[0] = 7;
		//Height of board
		options[1] = 6;
		//Computer One
		options[2] = 1;
		//Computer Two:
		options[3] = 1;
		//Switch sides after every game
		options[4] = 1;
		//Print board every move
		options[5] = 1;
		//Print board at the end of the game
		options[6] = 1;
		//Calculate elo difference? Information on elo can be found here: https://en.wikipedia.org/wiki/Elo_rating_system , https://en.wikipedia.org/wiki/Chess_rating_system
		options[7] = 1;	
		
		//Time control settings	
		double[] beginningTimeControl = new double[2];
		//beginning time in seconds
		beginningTimeControl[0] = 30.0;
		//incremental time in seconds
		beginningTimeControl[1] = 3.0;
		
		//Match Type. Un-comment the one you want to use and their parameters.
		//1. 1 game
		oneGame(options, beginningTimeControl);
		
		//2. Match with n games
		//int n = 100; //# of games
		//match(n, options);
		
		//3. Likelihood of Superiority Test (LOS). Must have the package Apache commons math. Used to determine the probability that a player is stronger than the other. 
		//int n = 100; //# of games
		//likelihoodOfSuperiority(n, options); 
	}
	
	public static void oneGame(int[] options, double[] beginningTimeControl) {
		Scanner keyboard = new Scanner(System.in);
		
		int[][] board = new int[options[0]][options[1]];
		int sentinel = 1;
		
		do {
			int gameLength = 0;
			String player = "X";
			
			printBoard(board);
			while (gameLength < options[0] * options[1]) {
				int[] moveChoiceCoordinates = new int[2];
				if (player.equals("X") & options[2] == 1) moveChoiceCoordinates = computerOneMoveCoordinates(board);
				else if (player.equals("O") & options[3] == 1) moveChoiceCoordinates = computerTwoMoveCoordinates(board);
				else moveChoiceCoordinates = userMoveCoordinates(board, player);
				
				changeBoard(board, moveChoiceCoordinates, player);
				printBoard(board);
				if (winningConditionCheck(board, moveChoiceCoordinates, player)) {
					System.out.println("Player " + player + " wins!");
					System.out.println();
					break;
				}
				
				player = player.equals("X") ? "O" : "X";
				gameLength++;
				
				if (gameLength == options[0] * options[1]) {
					System.out.println("Draw!");
				}
			}
			
			clearBoard(board);
		} while (playAnotherGame());
	}
	
	public static int[] computerOneMoveCoordinates (int[][] board) {
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
	
	public static int[] computerTwoMoveCoordinates (int[][] board) {
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
	
	public static void printBoard(int[][] board) {
		for (int num = 1; num <= board.length; num++) {
			System.out.print(num + " ");
		} 
		
		System.out.println();
		
		for (int row = 0; row < board[0].length; row++) {
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
	
	public static void clearBoard(int[][] board) {
		//For when another game is to be played
		for (int row = 0; row < board[0].length; row++) {
			for (int col = 0; col < board.length; col++) {
				board[col][row] = 0;
			}
		}
	}
	
	public static int[] userMoveCoordinates(int[][] board, String player) {
		//Take user input and convert into coordinates on the array board
		int[] userMoveCoordinates = new int[2];
		Scanner keyboard = new Scanner(System.in);
		
		int playerNumber = player.equals("X") ? 1 : 2;
		
		legalMoveCheck:
		while (true) {
			System.out.print("Player " + player + "'s move: ");
			int playerMove = keyboard.nextInt();
			
			if (playerMove < 1 | playerMove > board.length) {
				System.out.println("Out of bounds.");
				continue;
			}
			
			//Is the column filled?
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
	
	public static void changeBoard(int[][] board, int[] moveChoiceCoordinates, String player) {
		board[moveChoiceCoordinates[0]][moveChoiceCoordinates[1]] = player.equals("X") ? 1 : 2;
	}
	
	//If you have a faster version, include it in your engine! This is just something makes sense
	public static boolean winningConditionCheck(int[][] board, int[] moveChoiceCoordinates, String player) {
		int numberInARow;
		int playerNumber = player.equals("X") ? 1 : 2;
		int coordinatesSum = moveChoiceCoordinates[0] + moveChoiceCoordinates[1]; //Used in checking for diagonals
		
		//Vertical check
		numberInARow = 1;
		for (int row = moveChoiceCoordinates[1] + 1; row <= moveChoiceCoordinates[1] + 3; row++) {
			if (row > board[0].length - 1) break;
			if (board[moveChoiceCoordinates[0]][row] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//Horizontal check (left to right)
		numberInARow = 1;
		for (int col = moveChoiceCoordinates[0] + 1; col <= moveChoiceCoordinates[0] + 3; col++) {
			if (col > board.length - 1) break;
			if (board[col][moveChoiceCoordinates[1]] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//(right to left)
		for (int col = moveChoiceCoordinates[0] - 1; col >= moveChoiceCoordinates[0] - 3; col--) {
			if (col < 0) break;
			if (board[col][moveChoiceCoordinates[1]] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//Diagonal check top right to bottom left (center to top right)
		numberInARow = 1;
		for (int col = moveChoiceCoordinates[0] + 1; col <= moveChoiceCoordinates[0] + 3; col++) {
			if (col > board.length - 1 | coordinatesSum - col < 0) break;
			if (board[col][coordinatesSum - col] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//(center to bottom left)
		for (int col = moveChoiceCoordinates[0] - 1; col >= moveChoiceCoordinates[0] - 3; col--) {
			if (col < 0 | coordinatesSum - col > board[0].length - 1) break;
			if (board[col][coordinatesSum - col] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//Diagonal check top left to bottom right (center to top left) 
		numberInARow = 1;
		for (int i = 1; i < 4; i++) {
			if (moveChoiceCoordinates[0] - i < 0 | moveChoiceCoordinates[1] - i < 0) break;
			if (board[moveChoiceCoordinates[0] - i][moveChoiceCoordinates[1] - i] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//(center to bottom right)
		for (int i = 1; i < 4; i++) {
			if (moveChoiceCoordinates[0] + i > board.length - 1| moveChoiceCoordinates[1] + i > board[0].length - 1) break;
			if (board[moveChoiceCoordinates[0] + i][moveChoiceCoordinates[1] + i] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		return false;
	}	
		
	public static boolean playAnotherGame() {
		Scanner keyboard = new Scanner(System.in);
			
		System.out.print("1 - play again, 2 - exit: ");
		int choice = keyboard.nextInt();
		
		switch (choice) {
			case 1:
				return true;
			default:
				System.out.println("Program terminated.");
				return false;
		}
	}
}