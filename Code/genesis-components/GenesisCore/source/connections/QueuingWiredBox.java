package connections;


import java.util.ArrayList;

/*
 * Like a wired box, but keeps a queue of inputs, and spaces them out in time on
 * the output wire. Created on Nov 18, 2007 @author phw
 */

public class QueuingWiredBox extends AbstractWiredBox {

	TimingThread timingThread;

	ArrayList<Object> queue = new ArrayList<Object>();

	int persistance = 250;
	
	int maxQueueLength = 50;
	
	public QueuingWiredBox() {
		Connections.getPorts(this).addSignalProcessor("process");
	}
	
	public QueuingWiredBox(int p, int q) {
		this(p);
		maxQueueLength = q;
	}
	
	public QueuingWiredBox(int p) {
		this();
		persistance = p;
	}

	public void process(Object o) {
		queue.add(o);
		getTimingThread();
	}

	protected synchronized TimingThread getTimingThread() {
		if (timingThread == null) {
			timingThread = new TimingThread();
			timingThread.start();
		}
		return timingThread;
	}

	class TimingThread extends Thread {
		public void run() {
			int i = 0;
			while (true) {
				if (++i % 10 == 0) {
					i = 0;
				}
				while (queue.size() > maxQueueLength) {
					queue.remove(0);
				}
				try {
					if (queue.size() > 0) {
						Object input = queue.remove(0);
						Connections.getPorts(QueuingWiredBox.this).transmit(input);
						Thread.sleep(persistance);
					}
					else {
						Thread.sleep(persistance);
					}
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public int getPersistance() {
		return persistance;
	}

	public void setPersistance(int persistance) {
		this.persistance = persistance;
	}

}
