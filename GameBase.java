import java.util.Scanner;
import org.apache.commons.math3.special.Erf; //For Likelihood of Superiority test. Test to see how LOS behaves, then decide how to use it.

public class GameBase {

	static int pitNum = 6; //Number of boards per side
	static int stoneNum = 6; //Number of starting stones per board
	static int[] board = new int[pitNum * 2 + 2];
	/* Board representation
	 * 13 | 12 11 10 9 8 7
	 *       0  1  2 3 4 5 | 6
	 */
	static boolean player = true;
	static boolean startingPlayer = true; //Used to determine which player starts
	static boolean repeatMove;

	//Statistics in the perspective of the South player
	static int wins = 0;
	static int losses = 0;
	static int draws = 0;

	public static void main(String[] args) {
		//Control Options
		int[] options = new int[5];
		options[0] = 0; //Print board
		options[1] = 1; //Switch sides after every game
		//Print board & stats at the end of the game
		options[2] = 1;
		//Computer One. Computer One always plays the south side
		options[3] = 1;
		//Computer Two. Computer Two always plays the north side
		options[4] = 1;

		//Time control settings
		long[] timeControl = new long[2];
		//beginning time in milliseconds (1000 milliseconds = 1 second)
		timeControl[0] = 6000;
		//incremental time in milliseconds
		timeControl[1] = 600;

		//2. Match with n games
		int n = 100000; //# of games
		for (int i = 1; i <= n; i++) game(n, options, timeControl, i);
	}

	public static void game(int n, int[] options, long[] timeControl, int i) { //Need to implement n, options, timeControl
		setBoard();

		if (options[0] == 1) printBoard();
		while (!terminal()) {
			repeatMove = false;
			int move;
			if (player && options[3] == 1) move = AI.findMove();
			else if (!player && options[4] == 1) move = AI.findMove();
			else move = playerMove();

			updateBoard(move);
			if (options[0] == 1) printBoard();
			if (repeatMove) continue;
			player = player ? false : true;
		}

		if (board[pitNum] > board[2 * pitNum + 1]) wins++;
		else if (board[pitNum] == board[2 * pitNum + 1]) draws++;
		else losses++;

		captureRemainingPieces();
		if (options[2] == 1) {
			printBoard();
			System.out.print("Game over. ");
			if (board[pitNum] > board[2 * pitNum + 1]) System.out.println("Player South wins by " + (board[pitNum] - board[2 * pitNum + 1]) + "!");
			else if (board[pitNum] == board[2 * pitNum + 1]) System.out.println("Draw!");
			else System.out.println("Player North wins by " + (board[2 * pitNum + 1] - board[pitNum]) + "!");

			System.out.println("W-L-D " + wins + "-" + losses + "-" + draws + ". " + i + " out of " + n + " games completed.");
			System.out.println("Elo difference: " + (-400.0 * Math.log((1.0 / ((wins + 0.5 * draws) / i)) - 1) / Math.log(10.0)));
			//https://chessprogramming.wikispaces.com/Match+Statistics
			System.out.println("LOS: " + (0.5 + 0.5 * Erf.erf((wins - losses) / Math.sqrt(2.0 * (wins + losses)))));

		}

		//Determine the starting player for the next game
		if (options[1] == 0) player = true; //true for the south player, false for the north player
		else {
			startingPlayer = startingPlayer ? false : true;
			player = startingPlayer;
		}
	}

	public static boolean terminal() {
		//Inefficient as it checks both sides
		for (int i = 0; i < pitNum; i++) {
			if (board[i] != 0) break;
			if (i == pitNum - 1) return true;
		}

		for (int i = pitNum + 1; i < 2 * pitNum + 1; i++) {
			if (board[i] != 0) break;
			if (i == 2 * pitNum) return true;
		}
		return false;
	}

	public static void setBoard() {
		for (int i = 0; i < pitNum; i++) {
			board[i] = stoneNum;
			board[2 * pitNum - i] = stoneNum;
		}

		board[pitNum] = 0;
		board[2 * pitNum + 1] = 0;
	}

	public static void printBoard() {
		//Find the largest value.
		int max = -1;
		for (int i = 0; i < board.length; i++) {
			if (i == pitNum || i == 2 * pitNum + 1) continue;
			if (board[i] > max) {
				max = board[i];
			}
		}

		for (int i = 0; i < pitNum; i++) {
			if (i + 1 > max) {
				max = i + 1;
			}
		}

		String boardString = " ";
		for (int i = 2 * pitNum; i >= pitNum + 1; i--) {
			for (int k = 0; k < Math.floor(Math.log(Math.max(1, max)) / Math.log(10)) - Math.floor(Math.log(Math.max(1, board[i])) / Math.log(10)); k++) boardString += " ";
			boardString += "  " + board[i];
		}
		boardString += " ";

		//Print "<-- North"
		if ((boardString.length() / 2 - 8) > 0) {
			for (int i = 0; i < (boardString.length() - 9) / 2; i++) System.out.print(" ");
			System.out.print("<-- North");
			for (int i = 0; i < (boardString.length() - 9) / 2; i++) System.out.print(" ");
		}
		System.out.println();

		for (int i = 0; i < boardString.length() + 2; i++) System.out.print("-");
		System.out.println();
		System.out.println(boardString);
		System.out.println();

		String storeString = "   ";
		for (int i = 0; i < Math.floor(Math.log(Math.max(1, max)) / Math.log(10)) - Math.floor(Math.log(Math.max(1, board[2 * pitNum + 1])) / Math.log(10)); i++) storeString += " ";
		storeString += board[2 * pitNum + 1];
		int storeStringLength = storeString.length();
		for (int i = 0; i < boardString.length() - storeStringLength - Math.floor(Math.log(Math.max(1, board[pitNum])) / Math.log(10)) - 2; i++) storeString += " ";
		storeString += board[pitNum];
		System.out.println(storeString);
		System.out.println();

		boardString = " ";
		for (int i = 0; i < pitNum; i++) {
			for (int k = 0; k < Math.floor(Math.log(Math.max(1, max)) / Math.log(10)) - Math.floor(Math.log(Math.max(1, board[i])) / Math.log(10)); k++) boardString += " ";
			boardString += "  " + board[i];
		}
		boardString += " ";
		System.out.println(boardString);

		for (int i = 0; i < boardString.length() + 2; i++) System.out.print("-");
		System.out.println();

		String columnString = " ";
		for (int i = 0; i < pitNum; i++) {
			for (int k = 0; k < Math.floor(Math.log(Math.max(1, max)) / Math.log(10)) - Math.floor(Math.log(Math.max(1, i + 1)) / Math.log(10)); k++) columnString += " ";
			columnString += "  " + (i + 1);
		}
		columnString += " ";
		System.out.println(columnString);

		//Print "South -->"
		if ((boardString.length() / 2 - 8) > 0) {
			for (int i = 0; i < (columnString.length() - 9) / 2; i++) System.out.print(" ");
			System.out.print("South -->");
			for (int i = 0; i < (columnString.length() - 9) / 2; i++) System.out.print(" ");
		}
		System.out.println();

		System.out.println();
		System.out.println();

	}

	public static int playerMove() {
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Player " + (player ? "South's" : "North's") + " move: ");
		while (true) {
			int move = keyboard.nextInt();
			if (move < 1 || move > pitNum) {
				System.out.print("Invalid move! Player " + (player ? "South's" : "North's") + " move: ");
				continue;
			}

			if (player) {
				if (board[move - 1] == 0) {
					System.out.print("Invalid move! Player " + (player ? "South's" : "North's") + " move: ");
					continue;
				}
			} else {
				if (board[2 * pitNum - move + 1] == 0) {
					System.out.print("Invalid move! Player "  + (player ? "South's" : "North's") + " move: ");
					continue;
				}
			}

			System.out.println();
//			keyboard.close();
			return move;
		}
	}

	public static void updateBoard(int move) {
		int sowLocation;
		if (player) sowLocation = move - 1;
		else sowLocation = 2 * pitNum - move + 1;

		int numberOfStones = board[sowLocation];
		board[sowLocation] = 0;
		sowLocation++;

		for (int i = 0; i < numberOfStones; i++) {
			if ((player && sowLocation == 2 * pitNum + 1) || (!player && sowLocation == pitNum)) { //Cannot sow the opponent's store
				i--;
				sowLocation++;
				continue;
			}
			if (sowLocation == board.length) sowLocation = 0;
			//Capture
			if (i == numberOfStones - 1) {
				if (sowLocation == pitNum || sowLocation == 2 * pitNum + 1) repeatMove = true;
				else if (board[sowLocation] == 0 && (player && (sowLocation < pitNum) || !player && (sowLocation > pitNum) )) {
					board[sowLocation < pitNum ? pitNum : 2 * pitNum + 1] += 1 + board[Math.abs(2 * pitNum - sowLocation)];
					board[Math.abs(2 * pitNum - sowLocation)] = 0;
					break;
				}
			}

			board[sowLocation]++;
			sowLocation++;
		}
	}

	public static void captureRemainingPieces() { //At the end of the game, all the pieces belonging to one's side are captured.
		for (int i = 0; i < board.length; i++) {
			if (i == pitNum || i == 2 * pitNum + 1) continue;
			if (i < pitNum) {
				board[pitNum] += board[i];
				board[i] = 0;
			} else {
				board[2 * pitNum + 1] += board[i];
				board[i] = 0;
			}
		}
	}
}