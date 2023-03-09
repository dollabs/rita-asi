package start.transitionSpace;

import java.util.ArrayList;

/*
 * Created on May 15, 2011
 * @author phw
 */

public class Ladders extends ArrayList <Ladder> {

	public void addLadder(Ladder ladder) {
		if (this.size() > 9) {
			this.remove(0);
		}
	    this.add(ladder);
    }

}
