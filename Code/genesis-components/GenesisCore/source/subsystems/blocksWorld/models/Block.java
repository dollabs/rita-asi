package subsystems.blocksWorld.models;

import java.awt.*;

import utils.Mark;

/*
 * Created on Sep 9, 2005
 * @author Patrick
 */

public class Block {

	Location position = new Location(0, 0);

	Dimension size = new Dimension(10, 10);

	Color color = Color.BLACK;

	String name = "Block";

	Brick support;

	public Block(String name, Color color, int width, int height, double x, double y) {
		this.name = name;
		this.color = color;
		size = new Dimension(width, height);
		position = new Location(x, y);
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getLocation() {
		return position;
	}

	public void setLocation(Location position) {
		this.position = position;
	}

	public Dimension getSize() {
		return size;
	}

	public boolean isLight() {
		return !isHeavy();
	}

	public boolean isHeavy() {
		if (size.getHeight() > 10 || size.getWidth() > 10) {
			return true;
		}
		return false;
	}

	public boolean isTall() {
		if (size.getHeight() > 10 || size.getWidth() < 20) {
			return true;
		}
		return false;
	}

	public void setSize(Dimension size) {
		this.size = size;
	}

	public Location getTopCenter() {
		Location location = getLocation();
		Dimension size = getSize();
		Location topCenter = new Location(location.getX() + (size.getWidth() / 2), location.getY() + size.getHeight());
		return topCenter;
	}

	public void setSupport(Brick support) {
		boolean debug = false;
		if (!support.equals(this.support)) {
			this.support = support;
			support.addSupported(this);
			Mark.say(debug, "The block", support, "supports", support.getSupported());
			Mark.say(debug, this, "is supported by", support,
			        this.isSupportedByP(support),
			        support.supportsP(this)
			);
		}
	}

	public boolean supportsP(Block supported) {
		return supported.isSupportedByP(this);
	}

	public boolean isSupportedByP(Block support) {
		return this.getSupport() == support;
	}

	public void removeSupport() {
		if (support != null) {
			support.removeSupported(this);
			support = null;
		}
	}

	public Brick getSupport() {
		return support;
	}

	public String toString() {
		return name;
	}

}
