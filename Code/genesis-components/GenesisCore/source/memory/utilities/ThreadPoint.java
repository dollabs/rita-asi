package memory.utilities;
import frames.entities.Thread;
import memory.distancemetrics.Point;
/**
 * Simply implements Adam's Point class so I can
 * use his Hungarian algorithm.  Don't use this for
 * anything else: you should use Distances.distance(...)
 * directly.
 * 
 * @author sglidden
 *
 */
public class ThreadPoint extends Point<Thread> {
	
	@Override
	protected double getDistance(Thread a, Thread b) {
		return Distances.distance(a, b);
	}
	private Thread myThread;
	public ThreadPoint(Thread t){
		myThread = t;
	}
	@Override
	public Thread getWrapped() {
		return myThread;
	}
}
