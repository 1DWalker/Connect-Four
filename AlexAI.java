
import java.lang.System;
import java.util.Random;

public class AlexAI {
	
	static Random randomInt = new Random();
	
//	static int maxMemory = 536870912; //In bytes. 512 mb 
	static int maxMemory = 50000000;
	static int boardWidth = 7;
	static int boardHeight = 6;
	
	static int rootPlayer;
	static int currentPlayer;
	static int savedPlayer; //for default policy
	//static int rootGameLength;
	//static int currentGameLength;
	
	static int[][] rootBoard = new int[boardWidth][boardHeight];
	static int[][] currentBoard = new int[boardWidth][boardHeight];
	static int[] rootColumnCount = new int[boardWidth]; //To increase node expansion speed
	static int[] currentColumnCount = new int[boardWidth];
	static int currentColumn = -1;
	
	//Time
	static int simulations;
	static long timeBegin;
	static long timeEnd;
	static long nanoTimeBegin;
	static long tolerance = 10; //Minimum time remaining after move is played MILLISECONDS
	static long timeTarget;
	static long timeTest;
	static int gameLength = 0;
	static int gameDepth;
	
	//Memory. [position of node][storage value]
	static int size = maxMemory / ((boardWidth + 5) * 8);
	static int rootMemoryPosition; //Need to implement this for turn based play. Used in backup
	static int currentMemoryPosition; //Change to be the memory of the root node
	static int memoryCursor;
	static boolean memoryUsed = false;
    static int[][] childNodes = new int[size][boardWidth];
	static int[] parentNodes = new int[size];
	static double[] totalScore = new double[size];
	static int[] totalVisits = new int[size];
	static boolean[] terminalNodes = new boolean[size];
	static boolean[] flagNodes = new boolean[size];
	//static int[] gameLengthNodes = new int[size]; //Use for evaluation?
	
	static boolean[] symmetry = new boolean[size];
	
	//Extras
//	static boolean output = true;
	static boolean output = false;
	
	public static void initialize() {}
	
	public static int[] computerMove(int[][] board, int[] lastMoveCoordinates, long[] timeControl, long playerTime, boolean newGame) {
    	timeBegin = System.currentTimeMillis();
    	timeTarget = Math.max(playerTime * 4 / 5 - timeControl[1], tolerance);
		gameLength += 2;
    	
    	if (newGame) {
    		clearAllMemory();
    		memoryUsed = false;
    		rootMemoryPosition = 0;
    		memoryCursor = 0;
    	} else {
    		if (childNodes[rootMemoryPosition][lastMoveCoordinates[0]] != -1) {
    			rootMemoryPosition = childNodes[rootMemoryPosition][lastMoveCoordinates[0]];
    			filterMemory();
        		memoryUsed = false;
        		memoryCursor = 0;
        		
//            	for (int i = 0; i < boardWidth; i++) {
//            		if (childNodes[rootMemoryPosition][i] != -1) System.out.println((i + 1) + "	Visits: " + totalVisits[childNodes[rootMemoryPosition][i]] + "	 Score: " + totalScore[childNodes[rootMemoryPosition][i]] / totalVisits[childNodes[rootMemoryPosition][i]] + "	 Wins: "  + totalScore[childNodes[rootMemoryPosition][i]]);
//            	}
    		} else {
        		clearAllMemory();
        		memoryUsed = false;
        		rootMemoryPosition = 0;
        		memoryCursor = 0;
    		}
    	}
		
		if (lastMoveCoordinates[0] == board.length) rootPlayer = 1;
		else {
			if (board[lastMoveCoordinates[0]][lastMoveCoordinates[1]] == 1) rootPlayer = 2;
			else rootPlayer = 1;
		} 
		
		setRootBoard(board);
		setRootColumnCount();
		
		//timeEnd = System.currentTimeMillis();
		//System.out.println(timeEnd - timeBegin);
		return search(timeControl, playerTime);
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
	
    public static void clearAllMemory() {
    	for (int i = 0; i < size; i++) {
    		if (parentNodes[i] == -1) break;
    		
    		for (int k = 0; k < boardWidth; k++) {
    			childNodes[i][k] = -1;
    		}
    		
    		parentNodes[i] = -1;
    		totalScore[i] = 0;
    		totalVisits[i] = 0;
    		terminalNodes[i] = false;
    		symmetry[i] = false;
    	}
    	
		parentNodes[0] = 0;
    	memoryUsed = false;
    	gameLength = 1;
    }
    
    public static void filterMemory() {
		for (int i = 0; i < size; i++) {
			flagNodes[i] = false;
		}
		
		flagNodes[rootMemoryPosition] = true;
		
		int memoryPosition = rootMemoryPosition;
    	do {
    		
    		boolean childFound;
    		do {
    			childFound = false;
        		for (int i = 0; i < boardWidth; i++) {
        			if (childNodes[memoryPosition][i] != -1) {
        				if (!flagNodes[childNodes[memoryPosition][i]]) {
            				memoryPosition = childNodes[memoryPosition][i];
            				childFound = true;
            				break;
        				}
        			}
        		}
    		} while (childFound);
    		
    		if (memoryPosition == rootMemoryPosition) break;
    		
    		flagNodes[memoryPosition] = true;
    		memoryPosition = parentNodes[memoryPosition];
    	} while (true);
		
		for (int i = 0; i < size; i++) {
    		if (parentNodes[i] == -1) break;
			if (flagNodes[i] == false) {
	    		for (int k = 0; k < boardWidth; k++) {
	    			childNodes[i][k] = -1;
	    		}
	    		
	    		parentNodes[i] = -1;
	    		totalScore[i] = 0;
	    		totalVisits[i] = 0;
	    		terminalNodes[i] = false;
	    		symmetry[i] = false;
			}
		}
		
		parentNodes[rootMemoryPosition] = rootMemoryPosition;
    }
    
    public static int[] search(long[] timeControl, long playerTime) {
    	simulations = 0; //testing purposes
    	
    	while (computationalBudget(timeControl, playerTime)) {
    		int newMemoryPosition = treePolicy();
    		double result = defaultPolicy(newMemoryPosition);
    		backup(newMemoryPosition, result);
    	}
    	
    	return bestNode();
    }
    
    public static boolean computationalBudget(long[] timeControl, long playerTime) {
    	timeEnd = System.currentTimeMillis();
    	simulations++;
    	
    	if (playerTime - (timeEnd - timeBegin) <= timeTarget) return false;
//    	if (timeEnd - timeBegin >= 1000) return false; //Time per move.
    	
    	return true;
    	
//    	if (simulations <= 1000) return true;
//    	return false;
    }
    
    public static int treePolicy() {
    	gameDepth = gameLength;
    	setCurrentBoard();
    	setCurrentColumnCount();
    	currentMemoryPosition = rootMemoryPosition;
    	currentPlayer = rootPlayer == 1 ? 2 : 1; //Start as the opposing player
    	
    	int bestChild = 0;
    	while (!terminal()) {
    		currentPlayer = currentPlayer == 1 ? 2 : 1;
    				
    		if (!fullyExpanded()) {
    			return expand();
    		} else {
    			bestChild = bestChild();
    		}
    		
    		gameDepth++;
    	}
    	
    	return bestChild;
    }
    
    public static double defaultPolicy(int newMemoryPosition) {
    	double winScore = 1;
    	double drawScore = 0.5;
    	double lossScore = 0;
    	
 		if (currentColumnCount[currentColumn] > 0) {
	    	if (winningConditionCheck(currentPlayer, currentColumn, boardHeight - currentColumnCount[currentColumn])) {
	    		terminalNodes[newMemoryPosition] = true;
	    		return winScore;
	    	}
 		}
 		
 		savedPlayer = currentPlayer;
		currentPlayer = currentPlayer == 1 ? 2 : 1;
		
    	while (!fullBoard()) {
     	  	int[] possibleMoves = new int[boardWidth];
     	  	int numberOfPossibleMoves = 0;
     	  	
     	  	//Play a move randomly
    		for (int i = 0; i < boardWidth; i++) {
    			if (currentColumnCount[i] < boardHeight) {
    				if (winningConditionCheck(currentPlayer, i, boardHeight - currentColumnCount[i] - 1)) {
    	    			if (currentPlayer == savedPlayer) return winScore;
    	    			else return lossScore;
    				}
    				
    				possibleMoves[numberOfPossibleMoves] = i;
    				numberOfPossibleMoves++;
    			}
    		}
    		
    		currentColumn = possibleMoves[randomInt.nextInt(numberOfPossibleMoves)];
    		
    		//Don't give the opponent a forced win
    		for (int i = 0; i < boardWidth; i++) {
    			if (currentColumnCount[i] < boardHeight) {
    				if (winningConditionCheck(currentPlayer == 1 ? 2 : 1, i, boardHeight - currentColumnCount[i] - 1)) {
    	    			currentColumn = i;
    	    			break;
    				}
    			}
    		}
    		
    		currentColumnCount[currentColumn] = currentColumnCount[currentColumn] + 1;
    		currentBoard[currentColumn][boardHeight - currentColumnCount[currentColumn]] = currentPlayer;
    		currentPlayer = currentPlayer == 1 ? 2 : 1;
    	}
    	
    	return drawScore;
    }
    
    public static void backup(int newMemoryPosition, double result) {
    	do {
    		totalScore[newMemoryPosition] += result;
    		totalVisits[newMemoryPosition] = totalVisits[newMemoryPosition] + 1;
        	result = 1 - result;
    		newMemoryPosition = parentNodes[newMemoryPosition];
    	} while (newMemoryPosition != rootMemoryPosition);
    	
    	//One last time for root node
    	totalVisits[newMemoryPosition]++; 	
    }
    
    public static int[] bestNode() {
    	double mostVisits = -1;
    	int bestNode = 0; 
    		
    	for (int i = 0; i < boardWidth; i++) {
    		if (childNodes[rootMemoryPosition][i] > -1 ) {
	      		if (totalScore[childNodes[rootMemoryPosition][i]] / totalVisits[childNodes[rootMemoryPosition][i]] > mostVisits) {
	    			mostVisits = totalScore[childNodes[rootMemoryPosition][i]] / totalVisits[childNodes[rootMemoryPosition][i]];
	    			bestNode = i;
	    		}
    		}
    	}
    	
    	if (output) {
        	System.out.println("Player: " + rootPlayer + "	 Move played: " + (bestNode + 1) + " 	Score: " + (totalScore[childNodes[rootMemoryPosition][bestNode]] / totalVisits[childNodes[rootMemoryPosition][bestNode]]));
        	
        	for (int i = 0; i < boardWidth; i++) {
        		if (childNodes[rootMemoryPosition][i] != -1) System.out.println((i + 1) + "	Visits: " + totalVisits[childNodes[rootMemoryPosition][i]] + "	 Score: " + totalScore[childNodes[rootMemoryPosition][i]] / totalVisits[childNodes[rootMemoryPosition][i]] + "	 Wins: "  + totalScore[childNodes[rootMemoryPosition][i]]);
        	}
        	
        	System.out.println((double) simulations / (timeEnd - timeBegin) + " kN/s");
        	System.out.println("Memory filled? " + memoryUsed);
    	}
    	
    	int[] coordinates = new int[2];
    	coordinates[0] = bestNode;
    	coordinates[1] = boardHeight - 1 - rootColumnCount[bestNode];
    	
    	rootMemoryPosition = childNodes[rootMemoryPosition][bestNode]; //Update for memory
    	return coordinates;
    }

    public static boolean terminal() {
    	if (terminalNodes[currentMemoryPosition] == true) {
    		//printBoard(currentBoard);
    		return true;
    	}
    	
    	for (int i = 0; i < boardWidth; i++) {
    		if (currentColumnCount[i] < boardHeight) {
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    public static boolean fullBoard() {
    	for (int i = 0; i < boardWidth; i++) {
    		if (currentColumnCount[i] < boardHeight) {
    			return false;
    		}
    	}
    	
    	return true;
    }
    
    public static boolean fullyExpanded() {
    	int[] possibleMoves = new int[boardWidth];
    	int numberOfPossibleMoves = 0;
    	
    	for (int i = 0; i < boardWidth; i++) {
    		if (childNodes[currentMemoryPosition][i] != -1) {
    			if (symmetry[childNodes[currentMemoryPosition][i]] == true) continue;
    		}
    		
    		if (currentColumnCount[i] < boardHeight & childNodes[currentMemoryPosition][i] == -1) {
    			if (winningConditionCheck(currentPlayer, i, boardHeight - currentColumnCount[i] - 1) || winningConditionCheck(currentPlayer == 1 ? 0 : 1, i, boardHeight - currentColumnCount[i] - 1)) {
    				currentColumn = i;
    				return false;
    			}
    			
    			possibleMoves[numberOfPossibleMoves] = i;
    			numberOfPossibleMoves++;
    		}
    	}
		
		if (numberOfPossibleMoves > 0) {
			currentColumn = possibleMoves[randomInt.nextInt(numberOfPossibleMoves)];
			return false;
		}
		
    	return true;
    }
    
    public static int expand() {
    	//Playing move from fullyExpanded()
    	boolean isSymmetrical = false;
    	if (symmetry[currentMemoryPosition] == false & gameDepth <= 8) { //Check symmetrical states
    		if (childNodes[currentMemoryPosition][boardWidth - 1 - currentColumn] != -1) {
        		if (symmetrical()) {
        			isSymmetrical = true;
        		}
    		}
    	}
    	
      	currentColumnCount[currentColumn] += 1;
    	currentBoard[currentColumn][boardHeight - currentColumnCount[currentColumn]] = currentPlayer;
    	
    	if (memoryUsed) { //Do not expand and keep the node as is. 	
    		currentPlayer = currentPlayer == 1 ? 2 : 1; //Since no child is created, player stays the same
    		return currentMemoryPosition;
    	} 
    	
    	int newMemoryPosition = 0; //Position for new memory 
    	
    	//Is the for loop necessary? Use memoryCursor + 1 instead?
    	for (int i = memoryCursor; i < size; i++) {
    		memoryCursor++;
    		
    		if (i == size - 1) memoryUsed = true; //No need to return currentMemoryPosition since size - 1 must be available
    		
    		if (parentNodes[i] == -1) {
    			newMemoryPosition = i;
    			break;
    		}
    	}
    	
    	childNodes[currentMemoryPosition][currentColumn] = newMemoryPosition;
    	parentNodes[newMemoryPosition] = currentMemoryPosition;

    	if (isSymmetrical) {
			symmetry[newMemoryPosition] = true; //WRITE A NEW SPOT FOR NEW NODE
			return childNodes[parentNodes[newMemoryPosition]][boardWidth - 1 - currentColumn];
    	}
    	
    	return newMemoryPosition;
    }
    
    public static int bestChild() {
		double bestChildScore = -1; 
		int bestChild = 0;
		
		for(int i = 0; i < boardWidth; i++) {
			if (childNodes[currentMemoryPosition][i] == -1 | currentColumnCount[i] == boardHeight) continue; //If the board is filled, childNodes[currentMemoryPosition][i] = -1, and doesn't work
			if (symmetry[childNodes[currentMemoryPosition][i]]) continue;
			
			double score = totalScore[childNodes[currentMemoryPosition][i]];
			int visits = totalVisits[childNodes[currentMemoryPosition][i]];
			double percentScore = score / visits;
			int parentVisits = totalVisits[currentMemoryPosition];
			double exploration = Math.log(parentVisits) / visits;
			double childScore = percentScore + Math.sqrt(exploration * Math.min(0.25, visits * percentScore * (1 - percentScore) + 2 * exploration));
					
			if (childScore > bestChildScore) {
				bestChildScore = childScore; 
				bestChild = i;
			}
		}
		
		currentColumn = bestChild; //Replace bestChild with currentColumn?
      	currentColumnCount[bestChild] += 1;
    	currentBoard[bestChild][boardHeight - currentColumnCount[bestChild]] = currentPlayer;
    	currentMemoryPosition = childNodes[currentMemoryPosition][bestChild];
    	
    	return currentMemoryPosition;
    }
    
    public static void setRootBoard(int[][] board) {
    	for (int col = 0; col < boardWidth; col++) {
    		for (int row = 0; row < boardHeight; row++) {
    			rootBoard[col][row] = board[col][row];
    		}
    	}
    }
    
    public static void setRootColumnCount() {
    	for (int col = 0; col < boardWidth; col++) {
    		int counter = 0;
    		
    		for (int row = boardHeight - 1; row >= 0; row--) {
    			if (rootBoard[col][row] != 0) counter++;
    			else break;
    		}
    		
    		rootColumnCount[col] = counter;
    	}
    }
    
    public static void setCurrentBoard() {
    	for (int col = 0; col < boardWidth; col++) {
    		for (int row = 0; row < boardHeight; row++) {
    			currentBoard[col][row] = rootBoard[col][row];
    		}
    	}
    }
    
    public static void setCurrentColumnCount() {
    	for (int col = 0; col < boardWidth; col++) {
    		currentColumnCount[col] = rootColumnCount[col];
    	}	
    }
    
    public static boolean symmetrical() {
    	for (int row = boardHeight - 1; row >= 0; row--) {
    		for (int col = 0; col < boardWidth; col++) {
    			if (currentBoard[col][row] != currentBoard[boardWidth - 1 - col][row]) return false;
     		}
    	}
    	
    	return true;
    }
    
    public static boolean winningConditionCheck(int playerNumber, int coordinateX, int coordinateY) {
		int numberInARow;
		int coordinatesSum = coordinateX + coordinateY; //Used in checking for diagonals
		
		//Vertical check
//		if (coordinateY < currentBoard[0].length - 3) {
		numberInARow = 1;
		for (int row = coordinateY + 1; row <= coordinateY + 3; row++) {
			if (row > currentBoard[0].length - 1) break;
			if (currentBoard[coordinateX][row] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}	
//		}
		
		//Horizontal check (left to right)
		numberInARow = 1;
		for (int col = coordinateX + 1; col <= coordinateX + 3; col++) {
			//System.out.println(coordinateY);
			if (col > currentBoard.length - 1) break;
			if (currentBoard[col][coordinateY] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//(right to left)
		for (int col = coordinateX - 1; col >= coordinateX - 3; col--) {
			if (col < 0) break;
			if (currentBoard[col][coordinateY] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//Diagonal check top right to bottom left (center to top right)
		numberInARow = 1;
		for (int col = coordinateX + 1; col <= coordinateX + 3; col++) {
			if (col > currentBoard.length - 1 | coordinatesSum - col < 0) break;
			if (currentBoard[col][coordinatesSum - col] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//(center to bottom left)
		for (int col = coordinateX - 1; col >= coordinateX - 3; col--) {
			if (col < 0 | coordinatesSum - col > currentBoard[0].length - 1) break;
			if (currentBoard[col][coordinatesSum - col] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//Diagonal check top left to bottom right (center to top left) 
		numberInARow = 1;
		for (int i = 1; i < 4; i++) {
			if (coordinateX - i < 0 | coordinateY - i < 0) break;
			if (currentBoard[coordinateX - i][coordinateY - i] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		//(center to bottom right)
		for (int i = 1; i < 4; i++) {
			if (coordinateX + i > currentBoard.length - 1| coordinateY + i > currentBoard[0].length - 1) break;
			if (currentBoard[coordinateX + i][coordinateY + i] == playerNumber) numberInARow++;
			else break;
			if (numberInARow == 4) return true;
		}
		
		return false;
	}	
}