package subsystems.blocksWorld.models;

import java.awt.Color;
import java.util.Vector;

/*
 * Created on Sep 9, 2005
 * @author Patrick
 */

public class Brick extends Block {
  
  Vector supported = new Vector();
  
  public Brick(String name, Color color, int width, int height, int x, int y) {
    super(name, color, width, height, x, y);
  }
  
  public void addSupported(Block b) {
    if (!supported.contains(b)) {
      b.setSupport(this);
      supported.add(b);
    }
  }
  
  public void removeSupported(Block b) {
    if (supported.contains(b)) {
      supported.remove(b);
      b.removeSupport();
    }
  }
  
  public Vector getSupported() {return supported;}
  
	public Block getTopSupported() {
		if (!supported.isEmpty()) {
			((Brick) supported.get(0)).getTopSupported();
		}
		return this;
	}
  
  
}


