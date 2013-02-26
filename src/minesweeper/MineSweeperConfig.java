package minesweeper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MineSweeperConfig
{
	/* There are these values which must be loaded:
	 * - rows
	 * - cols
	 *
	 * Should any of these values not be included in the config file,
	 * this wil throw a DataFormatException
	 */
	
	private int rows;
	private int cols;
	private double fractionMines;
	
	public MineSweeperConfig(String resourceString)
	{
		Class<? extends MineSweeperConfig> c = this.getClass();
		
		try {
			BufferedReader reader = 
					new BufferedReader(new FileReader(c.getResource(resourceString).getFile()));
			setConfigValues(reader);
		} catch (IOException ioex) {
			throw new Error("The config file could not be loaded.");
			// There is really no way to recover from no config file.
		}
	}
	
	private void setConfigValues(BufferedReader reader) throws IOException
	{
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#") || line.startsWith("\n") || line.startsWith(" ")) {
				// The line is either a comment or whitespace. Leave it be.
			} else {
				String[] parts = line.split("=");
				// There might be = signs in the values if they are strings, 
				// so merge all but the first one
				for (int i = 2; i < parts.length; i++) {
					parts[1] = parts[1] + parts[i];
				}
				
				switch(parts[0]) {
					case "rows":
						this.rows = Integer.parseInt(parts[1]);
						break;
					case "cols":
						this.cols = Integer.parseInt(parts[1]);
						break;
					case "fractionMines":
						this.fractionMines = Double.parseDouble(parts[1]);
						break;
				}
			}
		}
	}
	
	// For conciseness and clarity, each accessor is shortened from getField() to field()

	public int rows()
	{
		return rows;
	}

	public void setRows(int rows)
	{
		this.rows = rows;
	}

	public int cols()
	{
		return cols;
	}

	public void setCols(int cols)
	{
		this.cols = cols;
	}

	public double fractionMines()
	{
		return fractionMines;
	}

	public void setFractionMines(double fractionMines)
	{
		this.fractionMines = fractionMines;
	}
	
	
	
	
}
