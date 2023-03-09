package subsystems.blocksWorld.models;


/*
 * Created on Sep 10, 2005
 * @author Patrick
 */

public class Location {
 
	double x = 0, y = 0, fill = 0;
 
	public double getFill() {
		return fill;
	}

	public Location(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Location(double x, double y, double fill) {
		this(x, y);
		this.fill = fill;
	}

	public void fill(double fill) {
		this.fill = fill;
	}

	public void unfill() {
		fill = 0;
	}

	public void translate(double deltaX, double deltaY) {
		x += deltaX;
		y += deltaY;
	}
  
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
  
  public String toString() {return "<" + x + ", " + y + ">";}
 
}
