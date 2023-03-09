package memory.distancemetrics;
import frames.entities.Thread;
public class ThreadWithSimilarityDistance extends Point<Thread> {
	protected double getDistance(Thread a, Thread b) {
		return topDownCompare(a,b);
	}
	public static double topDownCompare(Thread t1, Thread t2){
		double overlap = 0.0;
		for (int i=0;i<t1.size()&&i<t2.size();i++){
			if(t1.get(i).equals(t2.get(i))) overlap +=1.0;
			else break;
		}
		if(Math.min(t1.size(),t2.size())==0)return 1.0;
		return 1.0 - overlap/Math.min(t1.size(),t2.size());
		//return 1.0 - 2.0*overlap/(t1.size()+t2.size());
	}
	
	
	public Thread getWrapped() {
		return myThread;
	}
	
	private Thread myThread;
	public ThreadWithSimilarityDistance(Thread t){
		myThread = t;
	}
}
