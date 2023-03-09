package connections;

import java.util.ArrayList;

/*
 * Enables wire viewer to be used for other networks
 * Created on May 12, 2009
 * @author phw
 */
public interface Network<B extends WiredBox> {
	
	public ArrayList<B> getBoxes();
	
	public ArrayList<B> getTargets(B box);
	
	// public ArrayList<B> getSources(B box);
}
