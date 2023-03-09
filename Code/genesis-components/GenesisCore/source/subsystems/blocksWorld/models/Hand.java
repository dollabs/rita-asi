package subsystems.blocksWorld.models;

import java.awt.Color;

import utils.Mark;

/*
 * Created on Sep 9, 2005
 * @author Patrick
 */

public class Hand extends Block {
 
 Block block;

	String name;

	Location parkingLocation;

	public Location getParkingLocation() {
		return parkingLocation;
	}

	public String getName() {
		return name;
	}

	public Hand(String name, Color color, Location location) {
		super("", color, 10, 100, location.getX(), location.getY());
		this.name = name;
		this.color = color;
		setLocation(location);
		// Accounts for width of hand
		double offset = this.getSize().getWidth() / 2;
		parkingLocation = new Location(getLocation().getX(), getLocation().getY());
//		Mark.say("Parking location is", parkingLocation);
  }

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public String toString() {
		return name;
	}
}
