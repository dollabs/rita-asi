package connections;

/*
 * Like a QueueingWiredBox, but runs with low priority and no persistance.
 * Created on Nov 18, 2007 @author phw
 */

public class BackgroundWiredBox extends QueuingWiredBox {

	public BackgroundWiredBox() {
		this.getTimingThread().setPriority(Thread.MIN_PRIORITY);
		// this.setPersistance(5000);
	}

}
