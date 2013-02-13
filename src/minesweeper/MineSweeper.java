package minesweeper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MineSweeper
{
	// Model, or the game's state
	private MineSweeperSquare[][] squares;
	private int windowWidth;
	private int windowHeight;
	private int rows;
	private int cols;
	private int numberMines;
	private int numberMinesRevealed;
	private double fractionMines;
	private BufferedImage background;
	// This will just be a rectangle, but stored in memory for performance.
	private BufferedImage squareArea; 
	
	// For global use in this class
	private JFrame frame;
	private MSPanel panel;
	
	private MineSweeperSquare currentSquare;
	
	private HashSet<MineSweeperSquare> checkedBlanks;
	
	
	// Images to be set by the constructor
	private BufferedImage smiley;
	private BufferedImage winSmiley;
	private BufferedImage loseSmiley;
	private BufferedImage cleanImage;
	private BufferedImage mineImage;
	private BufferedImage cleanFlaggedImage;
	private BufferedImage cleanPressedImage;
	private BufferedImage cleanFlaggedPressedImage;
	private BufferedImage empty;
	private BufferedImage[] numberImages;
	
	public static void main(String[] args)
	{
		@SuppressWarnings("unused")
		MineSweeper game = new MineSweeper();
		
	}
	
	public MineSweeper()
	{
		rows = 20;
		cols = 30;
		windowWidth = cols * 20 + 50;
		windowHeight = rows * 20 + 50;
		fractionMines = .15;
		
		background = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D backgroundG = background.createGraphics();
		backgroundG.setColor(new Color(152, 152, 152));
		backgroundG.fillRect(0, 0, windowWidth, windowHeight);
		
		squareArea = new BufferedImage(cols * 20, rows * 20, BufferedImage.TYPE_INT_ARGB);
		Graphics2D squareAreaG = squareArea.createGraphics();
		squareAreaG.setColor(new Color(76, 76, 76));
		squareAreaG.fillRect(0, 0, cols * 20, rows * 20);
		
		empty = new BufferedImage(20,20,BufferedImage.TYPE_INT_ARGB);
		
		try {
			Class<? extends MineSweeper> c = this.getClass();
			smiley = ImageIO.read(
					new File(c.getResource("resources/smiley.png").getFile()));
			loseSmiley = ImageIO.read(
					new File(c.getResource("resources/loseSmiley.png").getFile()));
			winSmiley = ImageIO.read(
					new File(c.getResource("resources/winSmiley.png").getFile()));
			cleanImage = ImageIO.read(
					new File(c.getResource("resources/cleanImage.png").getFile()));
			mineImage = ImageIO.read(
					new File(c.getResource("resources/mineImage.png").getFile()));
			cleanFlaggedImage = ImageIO.read(
					new File(c.getResource("resources/cleanFlaggedImage.png").getFile()));
			cleanPressedImage = ImageIO.read(
					new File(c.getResource("resources/cleanPressedImage.png").getFile()));
			cleanFlaggedPressedImage = ImageIO.read(
					new File(c.getResource("resources/cleanFlaggedPressedImage.png").getFile()));
			numberImages = new BufferedImage[9];
			numberImages[0] = empty;
			for (int i = 1; i < 9; i++) {
				numberImages[i] = ImageIO.read(
						new File(c.getResource("resources/" + i + ".png").getFile()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Populate the 2D array "squares"
		squares = new MineSweeperSquare[rows][cols];
		int[][] squareNumbers = new int[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				if (Math.random() >= fractionMines) {
					squares[i][j] = new MineSweeperSquare(SquareState.CLEAN);
				} else {
					squares[i][j] = new MineSweeperSquare(SquareState.MINE);
					numberMines++;
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
		
		// Put up the window
		frame = new JFrame();
		panel = new MSPanel();
		panel.setPreferredSize(new Dimension(windowWidth,windowHeight));
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		
		// Add the listeners
		panel.addMouseListener(new MSMouseListener());
		
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
				boolean isDiagonal = (n != i && m != j);
				boolean hasBeenChecked;
				if (inBounds) {
					hasBeenChecked = checkedBlanks.contains(squares[n][m]);
				} else {
					hasBeenChecked = false;
				}
				
				if (inBounds && !isOriginal && !hasBeenChecked && !isDiagonal 
						&& squares[n][m].getNumber() == 0) {
					clearRecursiveBlanks(n, m);
				} else if (isOriginal) {
					squares[n][m].getSquareProperties().add(SquareProperty.CLEARED);
					checkedBlanks.add(squares[n][m]);
				} else if (inBounds && squares[n][m].getNumber() != 0) {
					// This is a numbered square adjacent to a blank
					squares[n][m].getSquareProperties().add(SquareProperty.CLEARED);
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
			int squareAreaY = (windowHeight - squareArea.getHeight()) / 2;
			g.drawImage(squareArea,  squareAreaX, squareAreaY, null);
			
			// Paint every square
			for (int i = 0; i < rows; i++) {
				for (int j = 0; j < cols; j++) {
					BufferedImage img;
					MineSweeperSquare sq = squares[i][j]; // shorthand
					HashSet<SquareProperty> p = sq.getSquareProperties();
					
					if (sq.getSquareState() == SquareState.MINE && p.contains(SquareProperty.CLEARED)) {
						img = mineImage;
					} else if (p.contains(SquareProperty.CLEARED)) {
						img = numberImages[sq.getNumber()];
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
		public void mouseClicked(MouseEvent e)
		{
			
		}

		public void mouseEntered(MouseEvent arg0)
		{
			
		}

		public void mouseExited(MouseEvent arg0)
		{
			
		}

		public void mousePressed(MouseEvent e)
		{
			int squareAreaX = (windowWidth - squareArea.getWidth()) / 2;
			int squareAreaY = (windowHeight - squareArea.getHeight()) / 2;
			
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

			frame.repaint();
			
		}

		public void mouseReleased(MouseEvent e)
		{
			if (currentSquare != null && e.getButton() == MouseEvent.BUTTON1) {
				
				// Make sure the mouse is still on the square.
				// If they're not on the square, they presumably did not want to click it!
				int xLower = (windowWidth - squareArea.getWidth()) / 2 + currentSquare.getCol()*20;
				int yLower = (windowHeight - squareArea.getHeight()) / 2 + currentSquare.getRow()*20;
				int xHigher = xLower + 20;
				int yHigher = yLower + 20;
				boolean stillInXBounds = (e.getX() >= xLower && e.getX() <= xHigher);
				boolean stillInYBounds = (e.getY() >= yLower && e.getY() <= yHigher);
				
				if (!stillInXBounds || !stillInYBounds) {
					currentSquare.getSquareProperties().remove(SquareProperty.PRESSED);
					currentSquare = null;
					frame.repaint();
				} else {
				
					// Process the change to make
				
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
					} else {
						// It is neither blocked nor a mine.
						if (currentSquare.getNumber() == 0) {
							clearRecursiveBlanks(currentSquare.getRow(), currentSquare.getCol());
						} else {
							p.add(SquareProperty.CLEARED);
						}
						
					}
					
					currentSquare = null;
					frame.repaint();
				}
			}
		}
		
	}
}