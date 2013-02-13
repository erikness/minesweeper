package minesweeper;

import java.util.HashSet;

public class MineSweeperSquare
{
	private SquareState squareState;
	private HashSet<SquareProperty> squareProperties;
	private int number;
	private int row;
	private int col;
	
	public MineSweeperSquare(SquareState squareState)
	{
		this.squareState = squareState;
		this.squareProperties = new HashSet<SquareProperty>();
		this.number = 0;
	}
	
	public MineSweeperSquare()
	{
		squareProperties = new HashSet<SquareProperty>();
	}
	
	public SquareState getSquareState()
	{
		return squareState;
	}
	public void setSquareState(SquareState squareState)
	{
		this.squareState = squareState;
	}
	public HashSet<SquareProperty> getSquareProperties()
	{
		return squareProperties;
	}
	public void setSquareProperties(HashSet<SquareProperty> squareProperties)
	{
		this.squareProperties = squareProperties;
	}

	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}

	public int getRow()
	{
		return row;
	}

	public void setRow(int row)
	{
		this.row = row;
	}

	public int getCol()
	{
		return col;
	}

	public void setCol(int col)
	{
		this.col = col;
	}
	
	
	
	
}
