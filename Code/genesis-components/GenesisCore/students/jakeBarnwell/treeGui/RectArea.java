package jakeBarnwell.treeGui;

import java.util.HashMap;

/**
 * Represents the notion of a geometric rectangular area with four corners.
 * @author jb16
 *
 */
public class RectArea {
	
	/**
	 * (x1, y1) are the coordinates of the upper-left corner, whereas
	 * (x2, y2) are the coordinates of the lower-right corner.
	 */
	public int x1, x2, y1, y2;
	
	/**
	 * Construct a rectangle whose corners have the given coordinates
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public RectArea(int x1, int y1, int x2, int y2) {
		assert x1 <= x2;
		assert y1 <= y2;
		
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	
	/**
	 * Checks if this {@link RectArea} contains the given (x, y) coords
	 * within it.
	 * @param x
	 * @param y
	 * @return true if (x, y) is inside this rectangle, otherwise false
	 */
	public boolean contains(int x, int y) {
		return this.x1 <= x && x <= this.x2 && this.y1 <= y && y <= this.y2;
	}
	
	/**
	 * Given many rectangles, returns one that contains the supplied
	 * (x, y) coordinates.
	 * @param rects
	 * @param x
	 * @param y
	 * @return the {@link RectArea} containing the (x, y) point, if there is
	 * one; otherwise null. If more than one meet the criteria, an arbitrary one
	 * is returned.
	 */
	public static DrawableSpool which(HashMap<DrawableSpool, RectArea> rects, int x, int y) {
		RectArea nodeArea;
		for(DrawableSpool node : rects.keySet()) {
			nodeArea = rects.get(node);
			if(nodeArea != null && nodeArea.contains(x, y)) {
				return node;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return String.format("Rect(%d,%d,%d,%d)", x1, y1, x2, y2);
	}

}
