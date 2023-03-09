package utils;

import java.awt.Point;

/**
 * A Rectangle represents a rectangular area 
 * @author TAL
 *
 */

public class Rectangle {
	
	/**
	 * @specfield upperLeft : the lower left corner of the rectangle
	 * @specfield width : the width of the rectangle
	 * @specfield height : the height of the rectangle
	 * 
	 */
	
	private int upperleftx;
	private int upperlefty;
	private int width;
	private int height; 
	
	/**
	 * Create a new Rectangle 
	 * @param lowerleftx
	 * @param lowerlefty
	 * @param width
	 * @param height
	 */
	public Rectangle(int upperleftx, int upperlefty, int width, int height) {
		this.upperleftx = upperleftx; 
		this.upperlefty = upperlefty;
		this.width = width;
		this.height = height;
	}
	
	
	/**
	 * Tests whether a given point is contained within a rectangle 
	 * @param x : the x-coordinate of the point
	 * @param y : the y-coordinate of the point
	 * @return True if and only if point is contained within the rectangle
	 */
	public boolean contains(int x, int y) {
		return (x >= upperleftx &&
				x <= upperleftx + width &&
				y >= upperlefty &&
				y <= upperlefty + height);
	}
	
	public Point getCenter() {
		return new Point(upperleftx+width/2, upperlefty+height/2);
	}
	
	/**
	 * 
	 * @param rect
	 * @return True iff rect has the same coordinates as this rectangle
	 */
	public boolean equals(Rectangle rect) {
		return (upperleftx == rect.upperleftx &&
				upperlefty == rect.upperlefty &&
				width == rect.width &&
				height == rect.height);
	}
	
	public String toString() {
		String result = "";
		result = result + "upper left corner: (" + upperleftx + "," + upperlefty + ")\n";
		result = result + "width: " + width + ", height: " + height;
		return result;
	}
}