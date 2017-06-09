import java.lang.System; //Time
import java.util.Scanner;
import org.apache.commons.math3.special.Erf; //For Likelihood of Superiority test. Test to see how LOS behaves, then decide how to use it.
//If using apache commons math, un-comment the last line in method matchStatement

public class ConnectFour {
	
	static long timeBegin;
	static long totalTime;
	static long gameLength = 0; 
	static long totalGameLength = 0;
	
	public static void main(String[] args) {
		
		AlexAI.initialize();
		AlexAI2.initialize();
		
		//Options. 1 for YES, 0 for NO, unless a numerical value is required
		int options[] = new int[7];
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
		options[5] = 0;
		//Print board & stats at the end of the game
		options[6] = 1;
		
		//Time control settings	
		long[] timeControl = new long[2];
		//beginning time in milliseconds (1000 milliseconds = 1 second)
		timeControl[0] = 500;
		//incremental time in milliseconds
		timeControl[1] = 50;
		
		//Match Type. Un-comment the one you want to use and their parameters.
		//1. 1 game
	//	int switchSides = 0; //Switch sides? Useful for human vs computer or computer vs computer fun
	//	oneGame(options, timeControl, switchSides);
		
		//2. Match with n games
		int n = 10000; //# of games
		match(n, options, timeControl);
	}
	
	public static void oneGame(int[] options, long[] timeControl, int switchSides) {
		int[][] board = new int[options[0]][options[1]];

		do {
			boolean newGame = true; //For the AI to initialize and clear memory for the purposes of playing a new game
			gameLength = 0;
			String player = "X";
			int[] lastMoveCoordinates = {board.length, board[0].length}; //MAYBE YOUR PROGRAM NEEDS THIS. Make a move based on your opponent's move!
			
			long[] playerTime = new long[2];
			playerTime[0] = timeControl[0];
			playerTime[1] = timeControl[0];
			long timeStart;
			long timeEnd;
			
			if (options[5] == 1)  {
				timeStatement(options, playerTime, timeControl, switchSides);
				printBoard(board);
			}
			
			//Play game
			while (gameLength < options[0] * options[1]) {
				int[] moveChoiceCoordinates = new int[2];
				
				//Take move from human/computer
				timeStart = System.currentTimeMillis();
				if (switchSides == 0) {
					if (player.equals("X") & options[2] == 1) moveChoiceCoordinates =computerOneMoveCoordinates(board, lastMoveCoordinates, timeControl, playerTime[0], newGame);
					else if (player.equals("O") & options[3] == 1) moveChoiceCoordinates = computerTwoMoveCoordinates(board, lastMoveCoordinates, timeControl, playerTime[1], newGame);
					else moveChoiceCoordinates = userMoveCoordinates(board, player);
				} else {
					if (player.equals("O") & options[2] == 1) moveChoiceCoordinates = computerOneMoveCoordinates(board, lastMoveCoordinates, timeControl, playerTime[1], newGame);
					else if (player.equals("X") & options[3] == 1) moveChoiceCoordinates = computerTwoMoveCoordinates(board, lastMoveCoordinates, timeControl, playerTime[0], newGame);
					else moveChoiceCoordinates = userMoveCoordinates(board, player);
				}
				
				lastMoveCoordinates = moveChoiceCoordinates;
				
				//end time
				timeEnd = System.currentTimeMillis();
				if (player.equals("X")) playerTime[0] -= timeEnd - timeStart;
				else playerTime[1] -= timeEnd - timeStart;
				if (playerTime[0] < 0 | playerTime[1] < 0) {
					timeStatement(options, playerTime, timeControl, switchSides);
					printBoard(board);

					player = player.equals("X") ? "O" : "X";
					System.out.println("Player " + player + " wins on time!");
					System.out.println();
					break;
				}
				
				changeBoard(board, moveChoiceCoordinates, player);
				if (options[5] == 1)  {
					timeStatement(options, playerTime, timeControl, switchSides);
					printBoard(board);
				}
				
				//Check win conditions
				if (winningConditionCheck(board, moveChoiceCoordinates, player)) {
					if (options[5] == 0 & options[6] == 1) {
						timeStatement(options, playerTime, timeControl, switchSides);
						printBoard(board);
					}
					System.out.println("Player " + player + " wins!");
					System.out.println();
					break;
				}
				
				//give time increment
				if (player.equals("X")) playerTime[0] += timeControl[1];
				else playerTime[1] += timeControl[1];
				
				//switch players, check for draw
				player = player.equals("X") ? "O" : "X";
				gameLength++;
				if (gameLength == 2) newGame = false; //2, since the first AI plays a new game and the 2nd AI sees it as a new game as well.
				
				if (gameLength == options[0] * options[1]) {
					System.out.println("Draw!");
				}
			}
			
			clearBoard(board);
		} while (playAnotherGame());
	}
	
	//Can combine method oneGame and method match but I'm too lazy and it doesn't really matter
	public static void match(int n, int[] options, long[] timeControl) {
		timeBegin = System.currentTimeMillis();
		
		int[][] board = new int[options[0]][options[1]];
		int i = 0; //counting unit
		//These in perspective of player 1: the one that plays first in the match, or computer # one if computers are playing
		int wins = 0;
		int draws = 0;
		int losses = 0;
		int numberOfGames = 0;
		
		do {
			numberOfGames += n;
			int switchSides = 0;
//			int i = 0; //counting unit
//			//These in perspective of player 1: the one that plays first in the match, or computer # one if computers are playing
//			int wins = 0;
//			int draws = 0;
//			int losses = 0;
	
			do {
				boolean newGame = true; //For the AI to initialize and clear memory for the purposes of playing a new game
				gameLength = 0;
				String player = "X";
				int[] lastMoveCoordinates = {board.length, board[0].length}; //MAYBE YOUR PROGRAM NEEDS THIS. Make a move based on your opponent's move!
				
				long[] playerTime = new long[2];
				playerTime[0] = timeControl[0];
				playerTime[1] = timeControl[0];
				long timeStart;
				long timeEnd;
				
				if (options[5] == 1)  {
					timeStatement(options, playerTime, timeControl, switchSides);
					printBoard(board);
				}
				
				//Play game
				while (gameLength < options[0] * options[1]) {
					int[] moveChoiceCoordinates = new int[2];
					
					//Take move from human/computer
					timeStart = System.currentTimeMillis();
					if (switchSides == 0) {
						if (player.equals("X") & options[2] == 1) moveChoiceCoordinates = computerOneMoveCoordinates(board, lastMoveCoordinates, timeControl, playerTime[0], newGame);
						else if (player.equals("O") & options[3] == 1) moveChoiceCoordinates = computerTwoMoveCoordinates(board, lastMoveCoordinates, timeControl, playerTime[1], newGame);
						else moveChoiceCoordinates = userMoveCoordinates(board, player);
					} else {
						if (player.equals("O") & options[2] == 1) moveChoiceCoordinates = computerOneMoveCoordinates(board, lastMoveCoordinates, timeControl, playerTime[1], newGame);
						else if (player.equals("X") & options[3] == 1) moveChoiceCoordinates = computerTwoMoveCoordinates(board, lastMoveCoordinates, timeControl, playerTime[0], newGame);
						else moveChoiceCoordinates = userMoveCoordinates(board, player);
					}
					
					lastMoveCoordinates = moveChoiceCoordinates; 
					
					//end time
					timeEnd = System.currentTimeMillis();
					if (player.equals("X")) playerTime[0] -= timeEnd - timeStart;
					else playerTime[1] -= timeEnd - timeStart;
					
					if (playerTime[0] < 0 | playerTime[1] < 0) {	
						player = player.equals("X") ? "O" : "X";
						
						if (player.equals("X") & switchSides == 0) wins++;
						else if (player.equals("O") & switchSides == 1) wins++;
						else losses++;
						i++;
						
						if (options[5] == 0 & options[6] == 1) {
							timeStatement(options, playerTime, timeControl, switchSides);
							printBoard(board);
						}			
							
						if (options[6] == 1) {
							totalGameLength += gameLength;
							System.out.println("Player " + player + " wins on time!");
							matchStatement(wins, draws, losses, i, numberOfGames);
							System.out.println();
						}	
						break;
					}
					
					changeBoard(board, moveChoiceCoordinates, player);
					if (options[5] == 1)  {
						timeStatement(options, playerTime, timeControl, switchSides);
						printBoard(board);
					}
					
					//Check win conditions
					if (winningConditionCheck(board, moveChoiceCoordinates, player)) {
						if (player.equals("X") & switchSides == 0) wins++;
						else if (player.equals("O") & switchSides == 1) wins++;
						else losses++;
						
						i++;
						totalGameLength += gameLength;
						
						if (options[5] == 0 & options[6] == 1) {
							timeStatement(options, playerTime, timeControl, switchSides);
							printBoard(board);
						}
						
						if (options[6] == 1) {
							System.out.println("Player " + player + " wins!");
							matchStatement(wins, draws, losses, i, numberOfGames);
							System.out.println();
						}
						
						break;
					}
					
					//give time increment
					if (player.equals("X")) playerTime[0] += timeControl[1];
					else playerTime[1] += timeControl[1];
					
					//switch players, check for draw
					player = player.equals("X") ? "O" : "X";
					gameLength++;
					if (gameLength == 2) newGame = false; //2, since the first AI plays a new game and the 2nd AI sees it as a new game as well.
					
					if (gameLength == options[0] * options[1]) {
						totalGameLength += gameLength;
						draws++;
						i++;
						
						if (options[6] == 1) {
							printBoard(board);
							System.out.println("Draw!");
							matchStatement(wins, draws, losses, i, numberOfGames);
						}
					}
				}
				
				clearBoard(board);
				//Switch Sides if option 4 is on
				if (options[4] == 1) switchSides = switchSides == 0 ? 1 : 0;
				
			} while (i < numberOfGames);
			
			if (options[6] == 0) matchStatement(wins, draws, losses, i, numberOfGames);
			
			totalTime = System.currentTimeMillis();
			System.out.println("Total time: " + (totalTime - timeBegin));
		} while (playAnotherGame());
	}
	
	public static int[] computerOneMoveCoordinates(int[][] board, int[] lastMoveCoordinates, long[] timeControl, long playerTime, boolean newGame) {
		return AlexAI.computerMove(board, lastMoveCoordinates, timeControl, playerTime, newGame);
//		return AlexAI2.computerMove(board, lastMoveCoordinates, timeControl, playerTime, newGame);
//		return AlexAITest.computerMove(board, lastMoveCoordinates, timeControl, playerTime, newGame);
	}
	
	public static int[] computerTwoMoveCoordinates(int[][] board, int[] lastMoveCoordinates, long[] timeControl, long playerTime, boolean newGame) {
//		return AlexAI.computerMove(board, lastMoveCoordinates, timeControl, playerTime, newGame);

		return AlexAI2.computerMove(board, lastMoveCoordinates, timeControl, playerTime, newGame);
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
	
	public static void matchStatement (int wins, int draws, int losses, int i, int n) {
		System.out.println("W-L-D " + wins + "-" + losses + "-" + draws + ". " + i + " out of " + n + " games completed.");
		//Information on elo can be found here: https://en.wikipedia.org/wiki/Elo_rating_system , https://en.wikipedia.org/wiki/Chess_rating_system
		System.out.println("Elo difference: " + (-400.0 * Math.log((1.0 / ((wins + 0.5 * draws) / i)) - 1) / Math.log(10.0)));
		//https://chessprogramming.wikispaces.com/Match+Statistics
		System.out.println("LOS: " + (0.5 + 0.5 * Erf.erf((wins - losses) / Math.sqrt(2.0 * (wins + losses))))); 
		System.out.println("Game length: " + gameLength); 
		System.out.println("Average game length: " + (double) totalGameLength / i); 
	}
	
	public static void timeStatement (int[] options, long[] playerTime, long[] timeControl, int switchSides) {
		if (options[3] == 1 & switchSides == 0) System.out.print("C1: ");
		else if (options[4] == 1 & switchSides == 1) System.out.print("C2: ");
		else System.out.print("H: ");		
		System.out.print((double) playerTime[0] / 1000 + "s +" + timeControl[1] / 1000 + "s, ");
		
		if (options[3] == 1 & switchSides == 1) System.out.print("C1: ");
		else if (options[4] == 1 & switchSides == 0) System.out.print("C2: ");
		else System.out.print("H: ");	
		System.out.println((double) playerTime[1] / 1000 + "s +" + timeControl[1] / 1000 + "s. Incre: " + (double) timeControl[1] / 1000);
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
		System.out.println();
		
		switch (choice) {
			case 1:
				return true;
			default:
				System.out.println("Program terminated.");
				return false;
		}
	}
}