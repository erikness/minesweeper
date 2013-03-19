package minesweeper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

public class MineSweeper
{
	// Configuration settings
	private MineSweeperConfig config;
	// These are populated with populateState() based on config
	private int rows;
	private int cols;
	private double fractionMines;
	
	private MineSweeperSquare[][] squares;
	private int windowWidth;
	private int windowHeight;
	private final int SMILEY_AREA_HEIGHT;
	private final int SMILEY_Y;
	private int numberOfMines;
	private int numberOfRevealed;
	private BufferedImage background;
	// This will just be a rectangle, but stored in memory for performance.
	private BufferedImage squareArea; 
	
	// For global use in this class
	private JFrame frame;
	private MSPanel panel;
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem fileMenuItem;
	
	private MineSweeperSquare currentSquare;
	private SmileyState smileyState;
	private boolean isSmileyPressed;
	private HashSet<MineSweeperSquare> checkedBlanks;
	
	
	// Theme settings
	private MineSweeperTheme theme;
	// The values below are derived from theme through populateTheme()
	private Image smiley;
	private Image smileyPressed;
	private Image winSmiley;
	private Image winSmileyPressed;
	private Image loseSmiley;
	private Image loseSmileyPressed;
	private Image cleanImage;
	private Image mineImage;
	private Image cleanFlaggedImage;
	private Image cleanPressedImage;
	private Image cleanFlaggedPressedImage;
	private Image empty;
	private ArrayList<Image> numberImages;
	
	private Color backgroundColor;
	private Color squareAreaColor;
	
	public static void main(String[] args)
	{
		@SuppressWarnings("unused")
		MineSweeper game = new MineSweeper();
		
	}
	
	public MineSweeper()
	{
		config = new MineSweeperConfig("resources/config.txt");
		populateState();
		
		theme = new MineSweeperTheme("resources/themes/classic/classic.txt");
		populateTheme();
		
		// 30 for the smiley icon, 20 for top buffer
		SMILEY_Y = 20;
		SMILEY_AREA_HEIGHT = 30 + SMILEY_Y;
		windowWidth = cols * 20 + 50;
		windowHeight = rows * 20 + 50 + SMILEY_AREA_HEIGHT;
				
		background = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D backgroundG = background.createGraphics();
		backgroundG.setColor(backgroundColor);
		backgroundG.fillRect(0, 0, windowWidth, windowHeight);
				
		squareArea = new BufferedImage(cols * 20, rows * 20, BufferedImage.TYPE_INT_ARGB);
		Graphics2D squareAreaG = squareArea.createGraphics();
		squareAreaG.setColor(squareAreaColor);
		squareAreaG.fillRect(0, 0, cols * 20, rows * 20);
				
		empty = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
		
		newGame();
		
		// Put up the window
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		fileMenuItem = new JMenuItem("Hello Erik!");
		fileMenu.add(fileMenuItem);
		menuBar.add(fileMenu);
		
		frame = new JFrame();
		panel = new MSPanel();
		panel.setPreferredSize(new Dimension(windowWidth,windowHeight));
		frame.setJMenuBar(menuBar);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		// Add the listeners
		panel.addMouseListener(new MSMouseListener());
		
	}
	
	public void newGame()
	{
		smileyState = SmileyState.NORMAL;
		isSmileyPressed = false;
		numberOfRevealed = 0;
		numberOfMines = 0;
		
		// Populate the 2D array "squares"
		squares = new MineSweeperSquare[rows][cols];
		int[][] squareNumbers = new int[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (Math.random() >= fractionMines) {
					squares[i][j] = new MineSweeperSquare(SquareState.CLEAN);
				} else {
					squares[i][j] = new MineSweeperSquare(SquareState.MINE);
					numberOfMines++;
					for (int n = i - 1; n <= i + 1; n++) {
						for (int m = j - 1; m <= j + 1; m++) {
							if (n >= 0 && m >= 0 && n < rows && m < cols) {
								squareNumbers[n][m]++;
							}
						}
					}
				}
			
				squares[i][j].setRow(i);
				squares[i][j].setCol(j);
			}
		}
		
				
		// Apply squareNumbers to the squares
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				squares[i][j].setNumber(squareNumbers[i][j]);
			}
		}

		currentSquare = null;
		checkedBlanks = new HashSet<MineSweeperSquare>();
	}
	
	private void populateState()
	{
		rows = config.rows();
		cols = config.cols();
		// Not exact fraction, as this is over a random variable. See numberOfMines
		fractionMines = config.fractionMines();
	}
	
	private void populateTheme()
	{
		smiley = theme.getImage("smiley");
		smileyPressed = theme.getImage("smileyPressed");
		winSmiley = theme.getImage("winSmiley");
		winSmileyPressed = theme.getImage("winSmileyPressed");
		loseSmiley = theme.getImage("loseSmiley");
		loseSmileyPressed = theme.getImage("loseSmileyPressed");
		cleanImage = theme.getImage("cleanImage");
		mineImage = theme.getImage("mineImage");
		cleanFlaggedImage = theme.getImage("cleanFlaggedImage");
		cleanPressedImage = theme.getImage("cleanPressedImage");
		cleanFlaggedPressedImage = theme.getImage("cleanFlaggedPressedImage");
		empty = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
		numberImages = theme.getNumberedImages();
		backgroundColor = theme.getColor("backgroundColor");
		squareAreaColor = theme.getColor("squareAreaColor");
	}
	
	/** 
	 * Preconditions: i, and j have to be between 0 and rows-1 or cols-1
	 *                The square at squares[i][j] must be blank
	 */
	private void clearRecursiveBlanks(int i, int j)
	{
		for (int n = i - 1; n <= i + 1; n++) {
			for (int m = j - 1; m <= j + 1; m++) {
				boolean inBounds = (n >= 0 && m >= 0 && n < rows && m < cols);
				boolean isOriginal = (n == i && m == j);
				boolean hasBeenChecked;
				if (inBounds) {
					hasBeenChecked = checkedBlanks.contains(squares[n][m]);
				} else {
					hasBeenChecked = false;
				}
				
				if (inBounds && !isOriginal && !hasBeenChecked 
						&& squares[n][m].getNumber() == 0) {
					clearRecursiveBlanks(n, m);
				} else if (isOriginal) {
					if (squares[n][m].getSquareProperties().add(SquareProperty.CLEARED)) {
						// This little conditional increments numberOfRevealed only when
						// that square has not been revealed before.
						numberOfRevealed++;
					}
					checkedBlanks.add(squares[n][m]);
				} else if (inBounds && squares[n][m].getNumber() != 0) {
					// This is a numbered square adjacent to a blank
					if (squares[n][m].getSquareProperties().add(SquareProperty.CLEARED)) {
						// This little conditional increments numberOfRevealed only when
						// that square has not been revealed before.
						numberOfRevealed++;
					}
				}
			}
		}
	}
	
	public class MSPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;

		// View. Remember, this can be called at any time,
		// so it is important to be able to render the graphics
		// based on the current state.
		public void paintComponent(Graphics g)
		{
			// Paint the background
			g.drawImage(background, 0, 0, null);
			
			
			// Paint the area for the square area
			int squareAreaX = (windowWidth - squareArea.getWidth()) / 2;
			int squareAreaY = (windowHeight - squareArea.getHeight() + SMILEY_AREA_HEIGHT) / 2;
			g.drawImage(squareArea,  squareAreaX, squareAreaY, null);
			
			// Paint the smiley
			Image currentSmiley;
			switch(smileyState) {
				case WIN:
					if (isSmileyPressed) {
						currentSmiley = winSmileyPressed;
					} else {
						currentSmiley = winSmiley;
					}
					break;
				case LOSE:
					if (isSmileyPressed) {
						currentSmiley = loseSmileyPressed;
					} else {
						currentSmiley = loseSmiley;
					}
					break;
				default:
					if (isSmileyPressed) {
						currentSmiley = smileyPressed;
					} else {
						currentSmiley = smiley;
					}
					break;
			}
					
			g.drawImage(currentSmiley, (windowWidth - 30)/2, 20, null);
			
			// Paint every square
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					Image img;
					MineSweeperSquare sq = squares[i][j]; // shorthand
					HashSet<SquareProperty> p = sq.getSquareProperties();
					
					if (sq.getSquareState() == SquareState.MINE && p.contains(SquareProperty.CLEARED)) {
						img = mineImage;
					} else if (p.contains(SquareProperty.CLEARED)) {
						img = numberImages.get(sq.getNumber());
					} else if (p.contains(SquareProperty.FLAGGED) && p.contains(SquareProperty.PRESSED)) {
						img = cleanFlaggedPressedImage;
					} else if (p.contains(SquareProperty.FLAGGED)) {
						img = cleanFlaggedImage;
					} else if (p.contains(SquareProperty.PRESSED)) {
						img = cleanPressedImage;
					} else {
						img = cleanImage;
					}
					
					g.drawImage(img, squareAreaX + j*20, squareAreaY + i*20, null);
				}
			}
			
			
		}
		
	}
	
	public class MSMouseListener implements MouseListener
	{
		public void mouseClicked(MouseEvent e) {}

		public void mouseEntered(MouseEvent e) {}

		public void mouseExited(MouseEvent e) {}

		public void mousePressed(MouseEvent e)
		{
			boolean inSmileyBounds = e.getX() >= (windowWidth - 30)/2 &&
					e.getX() <= (windowWidth + 30)/2 &&
					e.getY() >= SMILEY_Y &&
					e.getY() <= SMILEY_Y + 30;

			if (inSmileyBounds) {
				// They clicked on the smiley
				isSmileyPressed = true;
			} else {
				// They clicked on either a square or nothing
				int squareAreaX = (windowWidth - squareArea.getWidth()) / 2;
				int squareAreaY = (windowHeight - squareArea.getHeight() + SMILEY_AREA_HEIGHT) / 2;
				
				if (e.getX() >= squareAreaX && e.getX() <= (squareAreaX + cols*20) &&
					e.getY() >= squareAreaY && e.getY() <= (squareAreaY + rows*20)) {
	
					int squareCol = (e.getX() - squareAreaX) / 20;
					int squareRow = (e.getY() - squareAreaY) / 20;
					currentSquare = squares[squareRow][squareCol];
					HashSet<SquareProperty> p = currentSquare.getSquareProperties();
					
					if (e.getButton() == MouseEvent.BUTTON1) {
						p.add(SquareProperty.PRESSED);
					} else if (e.getButton() == MouseEvent.BUTTON3) {
						if (p.contains(SquareProperty.FLAGGED)) {
							p.remove(SquareProperty.FLAGGED);
						} else {
							p.add(SquareProperty.FLAGGED);
						}
					}
				}
			}
			
			frame.repaint();
		}

		public void mouseReleased(MouseEvent e)
		{
			/*
			 * The handler for clicking on the smiley (which logically starts a new game)
			 */
			
			boolean inSmileyBounds = e.getX() >= (windowWidth - 30)/2 &&
					e.getX() <= (windowWidth + 30)/2 &&
					e.getY() >= SMILEY_Y &&
					e.getY() <= SMILEY_Y + 30;
			if (isSmileyPressed) {
				if (inSmileyBounds) {
					// They're not kidding!
					newGame();
				}
				isSmileyPressed = false;
				frame.repaint();
			}
			
			/*
			 * The handler for square clicking
			 * This is only executed if the mouse is released on the same square it
			 * that was clicked on immediately before.
			 */
			
			if (currentSquare != null && e.getButton() == MouseEvent.BUTTON1) {
				
				// Make sure the mouse is still on the square.
				// If they're not on the square, they presumably did not want to click it!
				int xLower = (windowWidth - squareArea.getWidth()) / 2 + currentSquare.getCol()*20;
				int yLower = (windowHeight - squareArea.getHeight() + SMILEY_AREA_HEIGHT) / 2 + currentSquare.getRow()*20;
				int xHigher = xLower + 20;
				int yHigher = yLower + 20;
				boolean stillInXBounds = (e.getX() >= xLower && e.getX() <= xHigher);
				boolean stillInYBounds = (e.getY() >= yLower && e.getY() <= yHigher);
				
				if (!stillInXBounds || !stillInYBounds) {
					currentSquare.getSquareProperties().remove(SquareProperty.PRESSED);
					currentSquare = null;
					frame.repaint();
				} else {
				
					// Something's gotta happen
				
					HashSet<SquareProperty> p = currentSquare.getSquareProperties();
					currentSquare.getSquareProperties().remove(SquareProperty.PRESSED);
					
					if (p.contains(SquareProperty.FLAGGED) || p.contains(SquareProperty.CLEARED)) {
						// They either clicked a flag or a square 
						// that has already been revealed.
						// For their own safety, do nothing.
					} else if (currentSquare.getSquareState() == SquareState.MINE) {
						for (int i = 0; i < rows; i++) {
							for (int j = 0; j < cols; j++) {
								// Iterate over all squares
								if (squares[i][j].getSquareState() == SquareState.MINE) {
									squares[i][j].getSquareProperties().add(SquareProperty.CLEARED);
								}
							}
						}
						smileyState = SmileyState.LOSE;
					} else {
						// It is neither blocked nor a mine.
						if (currentSquare.getNumber() == 0) {
							clearRecursiveBlanks(currentSquare.getRow(), currentSquare.getCol());
						} else {
							p.add(SquareProperty.CLEARED);
							numberOfRevealed++;
						}
						
						// Win condition check
						if (numberOfRevealed + numberOfMines == rows*cols) {
							smileyState = SmileyState.WIN;
						}
						
						
					}
					
					currentSquare = null;
					frame.repaint();
				}
			} // End square clicking handler
		}
	}
}
