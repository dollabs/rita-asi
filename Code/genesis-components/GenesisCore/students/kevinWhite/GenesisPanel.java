package kevinWhite;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import kevinWhite.pictures.Pictures;
import utils.Mark;

/*
 * TODO: add documentation for this class
 * 
 */
@SuppressWarnings("serial")
public class GenesisPanel extends JPanel {

	private String filename = "";

	private String text = "";

	private boolean paintVert = false;

	private boolean isPositive = true;

	private int panelType = -1;

	private Dimension initDimensions;

	private String[] expertNames;

	/**
	 * This GenesisPanel constructor allows for the rendering of one image or
	 * string of text on a panel to be displayed in a parent container.
	 * 
	 * @param file
	 *            , a string of text that corresponds to a directory that
	 *            contains an image or text to be drawn on a panel.
	 */
	public GenesisPanel(String file) {
		this.setBackground(Color.WHITE);
		if (file.contains(".")) {
			filename = file;
		}

		else {
			text = file;
		}

		// debugging listener
		// this.addMouseListener(new MouseListener() {
		// @Override
		// public void mouseClicked(MouseEvent arg0) {
		// Mark.say("x:", arg0.getX(), "y:", arg0.getY());
		// displayText("We need some help in Aisle 10!");
		// }
		// @Override
		// public void mouseEntered(MouseEvent arg0) {}
		// @Override
		// public void mouseExited(MouseEvent arg0) {}
		// @Override
		// public void mousePressed(MouseEvent arg0) {}
		// @Override
		// public void mouseReleased(MouseEvent arg0) {}
		// });
	}

	public GenesisPanel(String panelName, boolean vertical) {
		this.setBackground(Color.WHITE);
		paintVert = vertical;
		expertNames = new String[] { "class", "trajectory", "path", "place", "transition", "transfer", "cause", "goal", "persuasion", "coercion",
		        "belief", "mood", "part", "property", "possession", "job", "social", "time", "comparison", "roleframe", "image" };
		Arrays.sort(expertNames);
		panelType = Arrays.binarySearch(expertNames, panelName.toLowerCase());
		// System.out.println(Arrays.toString(expertNames));

		// debugging listener
		// this.addMouseListener(new MouseListener() {
		// @Override
		// public void mouseClicked(MouseEvent arg0) {
		// Mark.say("x:", arg0.getX(), "y:", arg0.getY());
		// displayText("I haven't been the same man since I saw you comin' in!");
		// }
		// @Override
		// public void mouseEntered(MouseEvent arg0) {}
		// @Override
		// public void mouseExited(MouseEvent arg0) {}
		// @Override
		// public void mousePressed(MouseEvent arg0) {}
		// @Override
		// public void mouseReleased(MouseEvent arg0) {}
		// });
	}

	/**
	 * @return a Dimension of a fixed size or of a size corresponding to the sum
	 *         of the height or widths of the images.
	 */
	public Dimension getPreferredSize() {
		int width = 200;
		int height = 200;
		initDimensions = new Dimension(width, height);
		return initDimensions;
	}

	/**
	 * Paints images and scales them accordingly by calling the paintAux method.
	 * Draws text at a location proportional to the size of the panel.
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int height = this.getHeight();
		int width = this.getWidth();
		if (filename.equals("") && !text.equals("")) {
			Font stdFont = g.getFont().deriveFont((float) 18.0);
			g.setFont(stdFont);
			FontMetrics fm = g.getFontMetrics();
			int txtWidth = fm.stringWidth(text);
			int widthOffset = width - txtWidth;
			float fontSize = g.getFont().getSize();

			if (txtWidth > width) {
				while (txtWidth > width) {
					fontSize -= (float) 1.0;
					Font tempFont = g.getFont().deriveFont(fontSize);
					g.setFont(tempFont);
					txtWidth = g.getFontMetrics().stringWidth(text);
					widthOffset = width - txtWidth;
				}
			}

			g.drawString(text, widthOffset / 2, height / 2);
		}

		else {
			if (!filename.equals("")) {
				try {
					BufferedImage img = ImageIO.read(new File(filename));
					paintAux(g, img, width, height, 0, 0);
				}
				catch (IOException e) {
					String tempTxt = "No image or text available.";
					FontMetrics fm = g.getFontMetrics();
					int txtWidth = fm.stringWidth(tempTxt);
					int offset = width - txtWidth;
					g.drawString(tempTxt, offset / 2, height / 2);
				}
			}

			else if (panelType != -1) {
				switch (panelType) {

				case 5:
					paintAux(g, getSign(isPositive), width, height, 0, 0);
					break;
					
				case 7:
				    paintAux(g, getBriefcase(isPositive), width,height,0,0);
				    break;

				case 9:
					paintAux(g, getPiece(isPositive), width, height, 0, 0);
					break;

				case 13:
					paintAux(g, getDeed(isPositive), width, height, 0, 0);
					break;

				default:
					Mark.say(expertNames[panelType] + "expert does not use a GenesisPanel.");
					String tempTxt = "No image or text available.";
					FontMetrics fm = g.getFontMetrics();
					int txtWidth = fm.stringWidth(tempTxt);
					int offset = width - txtWidth;
					g.drawString(tempTxt, offset / 2, height / 2);
				}
			}
		}
	}

	/**
	 * Paints and scales the image according to its proportion to the size of
	 * the panel.
	 * 
	 * @param g
	 * @param image
	 *            , the image to be painted
	 * @param width
	 *            , the width of the current panel
	 * @param height
	 *            , the height of the current panel
	 * @param globalXOffset
	 *            , the offset applied to the image along the x axis
	 * @param globalYOffset
	 *            , the offset applied to the image along the y axis
	 */
	private void paintAux(Graphics g, BufferedImage image, int width, int height, int globalXOffset, int globalYOffset) {
		double rPanel = (double) width / height;
		double rImage = (double) image.getWidth() / image.getHeight();
		double scale = 0.0;
		if (!paintVert) {
			scale = (double) width / image.getWidth();
			if (rImage < rPanel) {
				scale = (double) height / image.getHeight();
			}
		}

		else {
			scale = (double) height / image.getHeight();
			if (rImage > rPanel) {
				scale = (double) width / image.getWidth();
			}
		}
		int imageWidth = (int) (scale * image.getWidth());
		int imageHeight = (int) (scale * image.getHeight());

		int localXOffset = (width - imageWidth) / 2;
		int localYOffset = (height - imageHeight) / 2;

		g.drawImage(image, globalXOffset + localXOffset, globalYOffset + localYOffset, imageWidth, imageHeight, null);
	}

	protected Dimension getInitDimensions() {
		return this.initDimensions;
	}

	/**
	 * Allows for the drawing of text on the GenesisPanel
	 * 
	 * @param chars
	 *            , the text to be displayed
	 */
	protected void displayText(String chars) {
		filename = "";
		text = chars;
		repaint();
	}

	/**
	 * Changes the desirability of a goal to pursuit or evasion. Repaints the
	 * panel with the corresponding image. Used within the Genesis system only.
	 * 
	 * @param want
	 *            , a boolean determining whether or not to pursue a particular
	 *            goal
	 */
	protected void setDesirability(boolean want) {
		isPositive = want;
		repaint();
	}

	/**
	 * @param want
	 *            , a boolean determining whether or to pursue a goal
	 * @return a BufferedImage corresponding to the desire to pursue a goal
	 */
	private BufferedImage getSign(boolean want) {
		if (want) {
			return getGo_Sign();
		}
		else {
			return getStop_Sign();
		}
	}

	private BufferedImage getPiece(boolean want) {
		if (want) {
			return getPuzzle_Piece();
		}
		else {
			return getBroken_Piece();
		}
	}

	private BufferedImage getDeed(boolean want) {
		if (want) {
			return getTitle_Deed();
		}
		else {
			return getInvalid_Deed();
		}
	}
	
	private BufferedImage getBriefcase(boolean want){
	    if (want){
	        return getMy_Briefcase();
	    }
	    else{
	        return getYour_Briefcase();
	    }
	}

	private BufferedImage go_sign;

	public BufferedImage getGo_Sign() {
		if (go_sign == null) {
			URL location = Pictures.class.getResource("go_sign.jpg");
			try {
				go_sign = ImageIO.read(location);
			}
			catch (IOException e) {
				Mark.say("go_sign.jpg was not found.");
				e.printStackTrace();
			}
		}
		return go_sign;
	}

	private BufferedImage stop_sign;

	public BufferedImage getStop_Sign() {
		if (stop_sign == null) {
			URL location = Pictures.class.getResource("stop_sign.jpg");
			try {
				stop_sign = ImageIO.read(location);
			}
			catch (IOException e) {
				Mark.say("stop_sign.jpg was not found.");
				e.printStackTrace();
			}
		}
		return stop_sign;
	}

	private BufferedImage puzzle_piece;

	public BufferedImage getPuzzle_Piece() {
		if (puzzle_piece == null) {
			URL location = Pictures.class.getResource("puzzle_piece.jpg");
			try {
				puzzle_piece = ImageIO.read(location);
			}
			catch (IOException e) {
				Mark.say("puzzle_piece.jpg was not found.");
				e.printStackTrace();
			}
		}
		return puzzle_piece;
	}

	private BufferedImage broken_piece;

	public BufferedImage getBroken_Piece() {
		if (broken_piece == null) {
			URL location = Pictures.class.getResource("broken_puzzle_piece.jpg");
			try {
				broken_piece = ImageIO.read(location);
			}
			catch (IOException e) {
				Mark.say("broken_puzzle_piece.jpg was not found.");
				e.printStackTrace();
			}
		}
		return broken_piece;
	}

	private BufferedImage title_deed;

	public BufferedImage getTitle_Deed() {
		if (title_deed == null) {
			URL location = Pictures.class.getResource("title_deed.jpg");
			try {
				title_deed = ImageIO.read(location);
			}
			catch (IOException e) {
				Mark.say("title_deed.jpg was not found.");
				e.printStackTrace();
			}
		}
		return title_deed;
	}

	private BufferedImage invalid_deed;

	public BufferedImage getInvalid_Deed() {
	    if (invalid_deed == null) {
	        URL location = Pictures.class.getResource("invalid_deed.jpg");
	        try {
	            invalid_deed = ImageIO.read(location);
	        }
	        catch (IOException e) {
	            Mark.say("invalid_deed.jpg was not found.");
	            e.printStackTrace();
	        }
	    }
	    return invalid_deed;
	}
	
	private BufferedImage my_briefcase;
	
	public BufferedImage getMy_Briefcase() {
	    if (my_briefcase == null) {
	        URL location = Pictures.class.getResource("my_briefcase.jpg");
	        try {
	            my_briefcase = ImageIO.read(location);
	        }
	        catch (IOException e) {
	            Mark.say("my_briefcase.jpg was not found.");
	            e.printStackTrace();
	        }
	    }
	    return my_briefcase;
	}
	
	private BufferedImage your_briefcase;
	
	public BufferedImage getYour_Briefcase() {
	    if (your_briefcase == null) {
	        URL location = Pictures.class.getResource("your_briefcase.jpg");
	        try {
	            your_briefcase = ImageIO.read(location);
	        }
	        catch (IOException e) {
	            Mark.say("your_briefcase.jpg was not found.");
	            e.printStackTrace();
	        }
	    }
	    return your_briefcase;
	}

	/**
	 * @param args
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {

		final GenesisPanel horizPanel = new GenesisPanel("goal", false);
		horizPanel.setDesirability(true);
		final JFrame hf = new JFrame("hDemo");
		hf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		hf.add(horizPanel);
		hf.pack();
		hf.setVisible(true);

		String file = "source/kevinWhite/pictures/title_deed.jpg";
		String invalidFile = "source/kevinWhite/pictures/Corn.jpg";
		String text = "Aisle 10";
		final GenesisPanel vertPanel = new GenesisPanel(invalidFile);
		final JFrame vf = new JFrame("vDemo");
		vf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		vf.add(vertPanel);
		vf.pack();
		vf.setVisible(true);
	}

}
