package memory.multithreading;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Worker Thread deisgn pattern. Borrowed from 6.170.
 * 
 * @author sglidden
 *
 */
public class Worker {
	BlockingQueue<Task> q = new LinkedBlockingQueue<Task>();
	public Worker() {
		new Thread() { public void run() { 
			while (true) {
				try { 
					Task t = q.take(); 
					System.out.println("Executing++++++++++++++++++++++");
					t.execute(); 
					System.out.println("Not Executing------------------");
				} 
				catch (InterruptedException e) {
					System.err.println("[MEMORY] Worker Thread Interrupted");
				}
			}
		}}.start();
	}
	public void put(Task t) {
		try { 
			q.put(t); 
		} 
		catch (InterruptedException e) {
			System.err.println("[MEMORY] Worker Thread Interrupted");
		}
	}
}
