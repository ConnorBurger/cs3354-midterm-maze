package edu.txst.midterm;

/**
 * Core logic engine for the Maze Game.
 *
 * <p>Manages player movement across the board, enforces wall collisions,
 * tracks step and coin counts, and determines the win condition.
 *
 * <p>Cell type constants used by the board:
 * <ul>
 *   <li>0 – Floor</li>
 *   <li>1 – Wall</li>
 *   <li>2 – Coin</li>
 *   <li>5 – Exit</li>
 *   <li>6 – Player</li>
 * </ul>
 */
public class GameEngine {
	private Board board;
	private int playerRow;
	private int playerCol;
	private int exitRow;
	private int exitCol;
	private int coinCount;

	// Cell Type Constants
	private static final int FLOOR = 0;
	private static final int WALL = 1;
	private static final int COIN = 2;
	private static final int EXIT = 5;
	private static final int PLAYER = 6;

	/**
	 * Constructs a GameEngine bound to the given board.
	 * Scans the board to locate the initial player and exit positions.
	 *
	 * @param board the game board that this engine will operate on
	 */
	public GameEngine(Board board) {
		this.board = board;
		this.coinCount = 0;
		findPlayer();
		findExit();
	}

	/**
	 * Determines whether the player has reached the exit.
	 *
	 * @return {@code true} if the player's current position matches
	 *         the exit position; {@code false} otherwise
	 */
	public boolean playerWins() {
		return playerRow == exitRow && playerCol == exitCol;
	}

	/**
	 * Scans every cell of the board to locate the player (cell value {@code 6})
	 * and stores its row and column coordinates.
	 */
	private void findPlayer() {
		for (int r = 0; r < 6; r++) {
			for (int c = 0; c < 10; c++) {
				if (board.getCell(r, c) == PLAYER) {
					playerRow = r;
					playerCol = c;
					return;
				}
			}
		}
	}

	/**
	 * Scans every cell of the board to locate the exit (cell value {@code 5})
	 * and stores its row and column coordinates.
	 */
	private void findExit() {
		for (int r = 0; r < 6; r++) {
			for (int c = 0; c < 10; c++) {
				if (board.getCell(r, c) == EXIT) {
					exitRow = r;
					exitCol = c;
					return;
				}
			}
		}
	}

	/**
	 * Attempts to move the player in the specified direction.
	 *
	 * <p>The move is blocked if the target cell is a wall or out of bounds.
	 * Otherwise the player advances, the step counter is incremented, and
	 * any coin at the destination is collected (coin counter incremented).</p>
	 *
	 * @param dRow change in row: {@code -1} (up), {@code 0} (none), or {@code 1} (down)
	 * @param dCol change in column: {@code -1} (left), {@code 0} (none), or {@code 1} (right)
	 */
	public void movePlayer(int dRow, int dCol) {
		int targetRow = playerRow + dRow;
		int targetCol = playerCol + dCol;
		int targetCell = board.getCell(targetRow, targetCol);

		// Block movement into walls or out-of-bounds cells
		if (targetCell == WALL || targetCell == -1) {
			return;
		}

		// Collect coin if present at the destination
		if (targetCell == COIN) {
			coinCount++;
		}

		// Count the step
		board.stepCounter.increaseSteps();

		// Move the player on the board
		board.setCell(playerRow, playerCol, FLOOR);
		playerRow = targetRow;
		playerCol = targetCol;
		board.setCell(playerRow, playerCol, PLAYER);
	}

	/**
	 * Returns the total number of steps the player has taken since the
	 * engine was created.
	 *
	 * @return number of steps taken
	 */
	public int getStepCount() {
		return board.stepCounter.getSteps();
	}

	/**
	 * Returns the total number of coins the player has collected since the
	 * engine was created.
	 *
	 * @return number of coins collected
	 */
	public int getCoinCount() {
		return coinCount;
	}
}
