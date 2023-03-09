package connections.views;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import javax.swing.SwingUtilities;

/*
 * Created on Apr 23, 2010
 * @author phw
 */

public class ColorTracker {

	private long delay = 250;

	private boolean running = false;
	
	private static int maxRunning = 0;
	private static long min_runner = 0;

	private static ColorTracker tracker;

	private HashMap<ColoredBox, ColorTrackerPackage> tracker_map = new HashMap<>();
	private PriorityQueue<ColorTrackerPackage> tracker_queue = new PriorityQueue<ColorTrackerPackage>(100, new Comparator<ColorTrackerPackage>() {

		@Override
		public int compare(ColorTrackerPackage o1, ColorTrackerPackage o2) {
			// TODO Auto-generated method stub
			long a=o1.getQuitTime(),b=o2.getQuitTime();
			if(a==b)
				return 0;
			else
				return a>b?1:-1;
		}
		
	});
	
	public static ColorTracker getTracker() {
		if (tracker == null) {
			tracker = new ColorTracker();
		}
		return tracker;
	}

	public synchronized ArrayList<ColorTrackerPackage> process(Object x) {
		if (x instanceof ColorTrackerPackage) {
			ColorTrackerPackage tracker = (ColorTrackerPackage) x;
			boolean found = false;
			if(tracker_map.containsKey(tracker.getColoredBox())) {
				ColorTrackerPackage existingTracker = tracker_map.get(tracker.getColoredBox());
				existingTracker.setQuitTime(tracker.getQuitTime());
				tracker_queue.remove(existingTracker);
				tracker_queue.add(existingTracker);
				found = true;
			}
			if (!found) {
				tracker_map.put(tracker.getColoredBox(), tracker);
				tracker_queue.add(tracker);
				tracker.getColoredBox().setColor(tracker.getTemporaryColor());
			}
			if (running == false) {
				new TrackerThread().start();
			}
		}
//		else if (x instanceof ArrayList<?>) {
//			for (Object o : (ArrayList<?>) x) {
//				ColorTrackerPackage tracker = (ColorTrackerPackage) o;
//				tracker_map.values().remove(tracker);
//				tracker_queue.remove(tracker);
//				SwingUtilities.invokeLater(new PaintingThread(tracker.getColoredBox(), tracker.getPermanentColor()));
//			}
//		}
		else if (x == null) {
			long now = System.currentTimeMillis();
			min_runner = 0;
			ArrayList<ColorTrackerPackage> result = new ArrayList<ColorTrackerPackage>();
			while(tracker_queue.peek() != null && tracker_queue.peek().getQuitTime() < now) {
				ColorTrackerPackage tracker = tracker_queue.poll();
				tracker_map.values().remove(tracker);
				SwingUtilities.invokeLater(new PaintingThread(tracker.getColoredBox(), tracker.getPermanentColor()));
			}
			if(tracker_queue.peek() != null)
				min_runner = tracker_queue.peek().getQuitTime()-now;
//			for (Object o : tracker_map.values()) {
//				ColorTrackerPackage tracker = (ColorTrackerPackage) o;
//				if (tracker.getQuitTime() < now) {
//					result.add(tracker);
//				} else {
//					min_runner = Math.min(tracker.getQuitTime()-now, min_runner);
//				}
//			}
//			if (result.size() > 0) {
//				return result;
//			}
		}
		return null;
	}

	private class PaintingThread implements Runnable {

		private ColoredBox box;

		private Color color;

		public PaintingThread(ColoredBox box, Color color) {
			this.box = box;
			this.color = color;
		}

		public void run() {
			box.setColor(color);
		}

	}

	private class TrackerThread extends Thread {
		public TrackerThread() {
			running = true;
			maxRunning = 0;
		}

		public void run() {
			try {
				while (true) {
//					Thread.sleep(delay);
					Thread.sleep(10);
//					ArrayList<ColorTrackerPackage> purgeList = ColorTracker.this.process(null);
//					ColorTracker.this.process(purgeList);
					ColorTracker.this.process(null);
					maxRunning = Math.max(maxRunning, tracker_map.size());
					// System.out.println("Tracker count: " + trackers.size() + "/" + maxRunning); 
					if (tracker_map.isEmpty()) {
						// System.out.println("Maximum trackers: " + maxRunning); 
						break;
					}
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			finally {
				ColorTracker.this.running = false;
			}
		}
	}

}
