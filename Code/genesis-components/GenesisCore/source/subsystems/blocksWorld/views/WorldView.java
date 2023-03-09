package subsystems.blocksWorld.views;

import java.awt.*;
import java.util.Vector;

import javax.swing.*;

import subsystems.blocksWorld.models.Location;
import utils.Mark;


/*
 * Created on Sep 9, 2005
 * @author Patrick
 */

public class WorldView extends JPanel {
  
  // Viewing space
  
  Dimension space = new Dimension(100, 100);
  double spacer = 0.9;
  
  
  Vector squares = new Vector();
  
  TrafficLight stopLight;
  
  public WorldView () {
    setBackground(Color.WHITE);
    
      
    setMinimumSize(new Dimension(0, 0));
    setPreferredSize(new Dimension(400, 400));
    setLayout(new MyLayoutManager());
  }
  
    
  public TrafficLight getStopLight() {
    if (stopLight == null) {
      stopLight = new TrafficLight();
      add(stopLight);
    }
    return stopLight;
  }

  /*
  * Get rid of existing squares.
  */
  public void clear() {squares.clear();}
  
  /*
  * Add a square to the list of squares to be shown.
  */
  public void add(Square square) {
    squares.add(square);
    repaint();
  }
  
  public void setSquares(Vector squares) {
    this.squares = squares;
    repaint();
  }
  
  
	/*
	 * Paint the squares. Use Graphics2D to move squares around and adjust to frame size.
	 */
	public void paintComponent(Graphics g) {


    super.paintComponent(g);
    int width = getWidth();
    int height = getHeight();
    
    // Paint the squares
    double wScale = 0.9 * width / space.getWidth();
    double hScale = 0.9 * height / space.getHeight();
    double scale = Math.min(wScale, hScale);
    int xOffset = (int)((width - (scale * space.getWidth())) / 2);
    int yOffset = (int)((height - (scale * space.getHeight())) / 2);
    
    for (int i = 0; i < squares.size(); ++i) {
      
      Square square = (Square)(squares.get(i));
			Location p = square.getLocation();
      Dimension d = square.getSize();
      Color c = square.getColor();
      g.setColor(c);
      
			// This part draws the hand
      
      if (square instanceof T) {

				// First the arm

        int xLocation = (int)(p.getX() * scale);
        int yLocation = (int)(p.getY() * scale);
        int xSize = (int)(d.getWidth() * scale);
        int ySize = (int)(d.getHeight() * scale);
        yLocation = height - yLocation - ySize;
        xLocation += xOffset;
        
        // Make it a little skinnier
        int sideSpace = xSize * 4 / 10;
        int xLocation1 = xLocation + sideSpace;
        int xSize1 = xSize - 2 * sideSpace;

				g.fillRect(xLocation1, yLocation, xSize1, ySize);
        
        // Draw the hand part
        sideSpace = xSize * 2 / 10;
        int xLocation2 = xLocation + sideSpace;
        int xSize2 = xSize - 2 * sideSpace;
        int ySize2 = xSize1;
        int yLocation2 = yLocation + ySize - ySize2;

				g.fillRect(xLocation2, yLocation2, xSize2, ySize2);
      }
      else {

				// Draw block
        
        int xLocation = (int)(p.getX() * scale);
        int yLocation = (int)(p.getY() * scale);
        int xSize = (int)(d.getWidth() * scale);
        int ySize = (int)(d.getHeight() * scale);
        yLocation = height - yLocation - ySize;
        xLocation += xOffset;

				if (square.getLocation().getFill() != 0) {

					xLocation += 3 * xSize / 4;

					Graphics2D g2 = (Graphics2D) g.create();

					int fill = (int) (square.getLocation().getFill() * ySize);
					int offset = (int) (ySize * (1 - square.getLocation().getFill()));

					// Mark.say("Drawing", square.getName(), fill, offset);
					g2.rotate(Math.toRadians(-45), xLocation + xSize / 2, yLocation + ySize / 2);
					g2.fillRect(xLocation, yLocation + offset, xSize, fill);
					// g2.fillRect(0, 0 + offset, xSize, fill);
					g2.setColor(Color.BLACK);
					g2.drawRect(xLocation, yLocation, xSize, ySize);
					g2.dispose();
				}
				else {
					g.fillRect(xLocation, yLocation, xSize, ySize);
					g.setColor(Color.BLACK);
					g.drawRect(xLocation, yLocation, xSize, ySize);
				}

        if (square.getName() != null && !square.getName().equals("")) {
					g.setFont(new Font("helvetica", Font.BOLD, 40));
          FontMetrics metrics = g.getFontMetrics();
          int xText = getCenterStringOffset(metrics, square.getName(), xSize);
          int yText = getCenterBaseline(metrics, ySize);
          
          if (false && c.equals(Color.BLACK)) {g.setColor(Color.WHITE);}
          else {g.setColor(Color.WHITE);}
          g.drawString(square.getName(), xText + xLocation, yText + yLocation);
          g.setColor(Color.BLACK);
        }
        
      }
    }
  }
  
  public static int getCenterStringOffset(FontMetrics metrics, String string, int width) {
    return (width - metrics.stringWidth(string)) / 2;
  }
  
  public static int getCenterBaseline(FontMetrics metrics, int height) {
    return getBaselineOffset(metrics, height);
  }
  
  public static int getBaselineOffset(FontMetrics metrics, int height) {
    int fontHeight = metrics.getAscent();
    return (height + fontHeight) / 2;
  }
  
  class MyLayoutManager implements LayoutManager {
    public void removeLayoutComponent(Component component) {
    }
    
    public void addLayoutComponent(String string, Component arg1) {
    }
    
    public Dimension minimumLayoutSize(Container container) {
      return null;
    }
    
    public Dimension preferredLayoutSize(Container container) {
      return null;
    }
    
    public void layoutContainer(Container container) {
      if (stopLight != null) {
       stopLight.setBounds(getWidth() - 30, 10, 20, 60);
      }
    }
  }
  
  /*
  * Create test method for early testing of painting method.
  */
  public static void main(String[] args) {
    WorldView view = new WorldView();
    Square table = new Square();
    table.setSize(new Dimension(10, 1));
		table.setLocation(new Location(1, 0));
    view.add(table);
    Square B1 = new Square();
    B1.setColor(Color.RED);
    B1.setSize(new Dimension(2, 2));
		B1.setLocation(new Location(3, 1));
    view.add(B1);
    JFrame frame = new JFrame();
    frame.getContentPane().add(view);
    frame.pack();
    frame.show();
  }
}
