package subsystems.blocksWorld.views;

import java.awt.*;

import subsystems.blocksWorld.models.Location;

/*
 * Created on Sep 9, 2005
 * @author Patrick
 */

public class Square {
 
 Location location = new Location(0, 0);
 Dimension size = new Dimension (1, 1);
 Color color = Color.BLACK;
 String name = "";

 public String getName() {
  return name;
 }
 public void setName(String name) {
  this.name = name;
 }
 public Color getColor() {
  return color;
 }
 public void setColor(Color color) {
  this.color = color;
 }
 public Location getLocation() {
  return location;
 }
 public void setLocation(Location location) {
  this.location = location;
 }
 public Dimension getSize() {
  return size;
 }
 public void setSize(Dimension size) {
  this.size = size;
 }
}
