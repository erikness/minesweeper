package minesweeper;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class MineSweeperTheme
{
	private HashMap<String, Image> images;
	private ArrayList<Image> numberedImages;
	private HashMap<String, Color> colors;
	
	private Class<? extends MineSweeperTheme> c;
	
	public MineSweeperTheme(String resourceString)
	{
		c = this.getClass();
		BufferedReader reader;
		numberedImages = new ArrayList<Image>();
		images = new HashMap<String, Image>();
		colors = new HashMap<String, Color>();
		
		try {
			reader = new BufferedReader(new FileReader(c.getResource(resourceString).getFile()));
			setThemeValues(reader);
		} catch (IOException ioex) {
			// Theme file could not be read. Oh well, load the default!
			try {
				reader = new BufferedReader(new FileReader(c.getResource("resources/themes/classic.txt").getFile()));
				setThemeValues(reader);
			} catch (IOException ioex2) {
				// Default couldn't be loaded. Someone screwed up, and we can't fix that here.
				throw new Error("The default theme file could not be loaded.");
			}
		}
	}
	
	private void setThemeValues(BufferedReader reader) throws IOException
	{
		String line;
		
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("#") || "".equals(line) || line.startsWith(" ") || line.startsWith("\t")) {
				
				// The line is either a comment or whitespace. Leave it be.
				
			} else {
				
				String[] parts = line.split("=");
				
				// There might be = signs in the values if they are strings, 
				// so merge all but the first one
				for (int i = 2; i < parts.length; i++) {
					parts[1] = parts[1] + parts[i];
				}
				
				if (parts[0].equals("numberedImagesExpression")) {
					
					
					// All numbered images "1.png", "2.png", etc. will have the same format.
					// This specifies the resource string for each, where "*" can be substituted
					// for the image.
					
					String expression = parts[1].substring(1, parts[1].length() - 1); // Cut off ends
					
					numberedImages.add(null); // Null reference for index 0
					
					for (int n = 1; n <= 8; n++) {
						String resourceString = expression.replace("*", Integer.toString(n));
						numberedImages.add(ImageIO.read(
							new File(c.getResource(resourceString).getFile())));
					}
					
				} else if (parts[1].startsWith("\"")) {
					
					
					// String. In this file, that means a resource string of format "resources/foo.bar"
					String resourceString = parts[1].substring(1, parts[1].length() - 1); // Cut off ends
					BufferedImage resourceImage = ImageIO.read(
							new File(c.getResource(resourceString).getFile()));
					images.put(parts[0], resourceImage);
					
				} else if (parts[1].startsWith("0x")) {

					// This is a hex number, representing a color
					String redString = parts[1].substring(2,4);
					String greenString = parts[1].substring(4,6);
					String blueString = parts[1].substring(6,8);
					
					int red = hexStringToDecimal(redString);
					int green = hexStringToDecimal(greenString);
					int blue = hexStringToDecimal(blueString);
					
					colors.put(parts[0], new Color(red, green, blue));
					
				}
			}
		}
	}
	
	private int hexStringToDecimal(String hexString)
	{
		int result = 0;
		
		for (int i = 0; i < hexString.length(); i++) {
			int intDigit;
			String digit = hexString.substring(i,i+1);
			
			if (digit.toLowerCase().equals("f"))
				intDigit = 15;
			else if (digit.toLowerCase().equals("e"))
				intDigit = 14;
			else if (digit.toLowerCase().equals("d"))
				intDigit = 13;
			else if (digit.toLowerCase().equals("c"))
				intDigit = 12;
			else if (digit.toLowerCase().equals("b"))
				intDigit = 11;
			else if (digit.toLowerCase().equals("a"))
				intDigit = 10;
			else {
				// The digit is a single digit number
				intDigit = Integer.parseInt(digit);
			}
			
			result += intDigit * Math.pow(16, hexString.length() - 1 - i);
		}
		
		return result;
	}

	public HashMap<String, Image> getImages()
	{
		return images;
	}

	public ArrayList<Image> getNumberedImages()
	{
		return numberedImages;
	}

	public HashMap<String, Color> getColors()
	{
		return colors;
	}
	
	public Color getColor(String key)
	{
		return colors.get(key);
	}
	
	public Image getImage(String key)
	{
		return images.get(key);
	}
}
