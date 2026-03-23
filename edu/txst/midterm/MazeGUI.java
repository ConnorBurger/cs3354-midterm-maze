package edu.txst.midterm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Main graphical user interface for the Maze Game.
 *
 * <p>Displays a {@link GamePanel} that renders the current board and an
 * {@link InfoPanel} that tracks the player's step and coin counts.
 * Keyboard arrow keys move the player; a menu bar provides Open and Reset
 * actions.  When the player reaches the exit the game computes a score
 * using the formula {@code steps * -1 + coins * 5} and displays it in a
 * dialog.
 */
public class MazeGUI extends JFrame {
	private Board originalBoard;
	private Board currentBoard;
	private GameEngine engine;
	private GamePanel gamePanel;
	private InfoPanel infoPanel;
	private JMenuItem resetItem;

	/**
	 * Constructs the MazeGUI window, initialises the menu bar, the info
	 * panel, and the game panel, and registers the keyboard listener.
	 */
	public MazeGUI() {
		setTitle("16-Bit Maze");
		setSize(640, 480);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		initMenu();

		infoPanel = new InfoPanel();
		gamePanel = new GamePanel();
		add(infoPanel, BorderLayout.NORTH);
		add(gamePanel, BorderLayout.CENTER);

		// Handle Keyboard Input
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (engine == null)
					return;

				switch (e.getKeyCode()) {
					case KeyEvent.VK_UP    -> engine.movePlayer(-1, 0);
					case KeyEvent.VK_DOWN  -> engine.movePlayer(1, 0);
					case KeyEvent.VK_LEFT  -> engine.movePlayer(0, -1);
					case KeyEvent.VK_RIGHT -> engine.movePlayer(0, 1);
				}

				// Update counters in the info panel after every move
				infoPanel.setInfoSteps(engine.getStepCount());
				infoPanel.setInfoCoins(engine.getCoinCount());

				gamePanel.repaint();

				// Check for victory
				if (engine.playerWins()) {
					int steps = engine.getStepCount();
					int coins = engine.getCoinCount();
					int score = steps * -1 + coins * 5;

					JOptionPane.showMessageDialog(MazeGUI.this,
							"Congratulations! You found the exit.\nYou got "
									+ score + " points",
							"Level Complete", JOptionPane.INFORMATION_MESSAGE);

					engine = null;
					resetItem.setEnabled(false);
				}
			}
		});
	}

	/**
	 * Builds and attaches the menu bar with "Open" and "Reset" items under
	 * a "Game" menu.
	 */
	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu gameMenu = new JMenu("Game");

		JMenuItem openItem = new JMenuItem("Open");
		resetItem = new JMenuItem("Reset");
		resetItem.setEnabled(false);

		openItem.addActionListener(e -> openFile());
		resetItem.addActionListener(e -> resetGame());

		gameMenu.add(openItem);
		gameMenu.add(resetItem);
		menuBar.add(gameMenu);
		setJMenuBar(menuBar);
	}

	/**
	 * Opens a file-chooser dialog and loads the selected CSV file as a new
	 * board.  Resets the step and coin counters in the info panel.
	 */
	private void openFile() {
		JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
		int result = fileChooser.showOpenDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			CSVBoardLoader loader = new CSVBoardLoader();

			originalBoard = loader.load(selectedFile.getAbsolutePath());
			currentBoard = originalBoard.clone();
			engine = new GameEngine(currentBoard);

			infoPanel.setInfoSteps(0);
			infoPanel.setInfoCoins(0);

			resetItem.setEnabled(true);
			gamePanel.setBoard(currentBoard);
			gamePanel.repaint();
		}
	}

	/**
	 * Resets the current game to the original board state and clears the
	 * step and coin counters in the info panel.
	 */
	private void resetGame() {
		if (originalBoard != null) {
			currentBoard = originalBoard.clone();
			engine = new GameEngine(currentBoard);

			infoPanel.setInfoSteps(0);
			infoPanel.setInfoCoins(0);

			resetItem.setEnabled(true);
			gamePanel.setBoard(currentBoard);
			gamePanel.repaint();
		}
	}

	/**
	 * Panel that displays the current step and coin counts.
	 *
	 * <p>Provides setter and getter methods so the game loop can update
	 * the displayed values after each player move.
	 */
	private class InfoPanel extends JPanel {
		private JLabel infoSteps;
		private JLabel infoCoins;

		/**
		 * Constructs the InfoPanel and lays out the step and coin labels
		 * using a {@link FlowLayout}.
		 */
		public InfoPanel() {
			this.setLayout(new FlowLayout());
			this.add(new JLabel("Steps: "));
			infoSteps = new JLabel("0");
			this.add(infoSteps);
			this.add(new JLabel("Coins: "));
			infoCoins = new JLabel("0");
			this.add(infoCoins);
		}

		/**
		 * Updates the displayed step count.
		 *
		 * @param steps the new step count to display
		 */
		public void setInfoSteps(int steps) {
			this.infoSteps.setText(Integer.toString(steps));
		}

		/**
		 * Returns the step count currently displayed.
		 *
		 * @return current step count
		 */
		public int getInfoSteps() {
			return Integer.parseInt(this.infoSteps.getText());
		}

		/**
		 * Updates the displayed coin count.
		 *
		 * @param coins the new coin count to display
		 */
		public void setInfoCoins(int coins) {
			this.infoCoins.setText(Integer.toString(coins));
		}

		/**
		 * Returns the coin count currently displayed.
		 *
		 * @return current coin count
		 */
		public int getInfoCoins() {
			return Integer.parseInt(this.infoCoins.getText());
		}
	}

	/**
	 * Panel responsible for rendering the maze board.
	 *
	 * <p>Each cell is drawn as a filled coloured square scaled to
	 * {@code TILE_SIZE} pixels per side.
	 */
	private class GamePanel extends JPanel {
		private Board board;
		private final int TILE_SIZE = 64;

		/**
		 * Sets the board that this panel will render on the next repaint.
		 *
		 * @param board the board to render
		 */
		public void setBoard(Board board) {
			this.board = board;
		}

		/**
		 * Paints every cell of the board onto the panel using
		 * {@link #drawTile(Graphics, int, int, int)}.
		 *
		 * @param g the {@link Graphics} context provided by Swing
		 */
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (board == null)
				return;

			for (int r = 0; r < 6; r++) {
				for (int c = 0; c < 10; c++) {
					int cell = board.getCell(r, c);
					drawTile(g, cell, c * TILE_SIZE, r * TILE_SIZE);
				}
			}
		}

		/**
		 * Draws a single tile at the given pixel coordinates.
		 *
		 * <p>Colour mapping:
		 * <ul>
		 *   <li>0 (Floor) – light gray</li>
		 *   <li>1 (Wall)  – dark gray</li>
		 *   <li>2 (Coin)  – yellow</li>
		 *   <li>5 (Exit)  – magenta</li>
		 *   <li>6 (Player)– blue</li>
		 * </ul>
		 *
		 * @param g    the {@link Graphics} context
		 * @param type the cell type integer value
		 * @param x    the left pixel coordinate of the tile
		 * @param y    the top pixel coordinate of the tile
		 */
		private void drawTile(Graphics g, int type, int x, int y) {
			switch (type) {
				case 0 -> g.setColor(Color.LIGHT_GRAY); // Floor
				case 1 -> g.setColor(Color.DARK_GRAY);  // Wall
				case 2 -> g.setColor(Color.YELLOW);     // Coin
				case 5 -> g.setColor(Color.MAGENTA);    // Exit
				case 6 -> g.setColor(Color.BLUE);       // Player
				default -> g.setColor(Color.BLACK);
			}
			g.fillRect(x, y, TILE_SIZE, TILE_SIZE);
			g.setColor(Color.WHITE);
			g.drawRect(x, y, TILE_SIZE, TILE_SIZE);
		}
	}

	/**
	 * Application entry point.  Creates and displays the MazeGUI window on
	 * the Swing event-dispatch thread.
	 *
	 * @param args command-line arguments (not used)
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new MazeGUI().setVisible(true));
	}
}
