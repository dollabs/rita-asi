package connections.views;

import java.awt.Component;
import java.util.*;

import javax.swing.event.*;

import connections.*;

/*
 * Created on Feb 26, 2009
 * @author phw
 */

public class Adapter implements Observer {

	private ArrayList<Integer> levelList;

	private HashMap<WiredBox, ViewerBox> boxToBoxViewerMap;

	private boolean depth = false;

	private ConnectionViewer viewer;

	private Network<WiredBox> network;

	private static Adapter networkAdapter;

	private static Adapter connectionAdapter;

	public static Adapter makeConnectionAdapter() {
		if (connectionAdapter == null) {
			connectionAdapter = new Adapter();
			Connections.getInstance().addObserver(connectionAdapter);
			Connections.getInstance().changed();
			connectionAdapter.network = Connections.getInstance();
		}
		return connectionAdapter;
	}

	/*
	 * For use in other contexts, without use of observable
	 */
	public static Adapter makeNetworkAdapter(BasicNetwork<? extends WiredBox> network) {
		if (networkAdapter == null) {
			networkAdapter = new Adapter();
			network.addObserver(networkAdapter);
		}
		return networkAdapter;
	}

	public ConnectionViewer getViewer() {
		if (viewer == null) {
			viewer = new ConnectionViewer();
		}
		return viewer;
	}

	public void setNetwork(Network<WiredBox> network) {
		this.network = network;
		processNetwork();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object ignore) {
		network = (Network<WiredBox>) o;
		processNetwork();
	}

	private void processNetwork() {
		getLevelList().clear();
		getBoxToBoxViewerMap().clear();
		getViewer().clear();
		ArrayList<WiredBox> boxes = new ArrayList<WiredBox>();
		ArrayList<WiredBox> targets = new ArrayList<WiredBox>();
		ArrayList<WiredBox> roots = new ArrayList<WiredBox>();
		if (network == null) {
			return;
		}
		for (WiredBox box : network.getBoxes()) {
			boxes.add(box);
			targets.addAll(network.getTargets(box));
		}
		roots.addAll(boxes);
		for (WiredBox target : targets) {
			roots.remove(target);
		}
		// System.out.println("Boxes: " + boxes);
		// System.out.println("Targets: " + targets);
		// System.out.println("Roots: " + roots);
		for (WiredBox root : roots) {
			search(root, boxes, network);
		}
		// At this point, there may be some circularly connected boxes
		while (!boxes.isEmpty()) {
			// Awkward, but need to get my hands on one of the elements of the
			// set
			ArrayList<WiredBox> remainder = new ArrayList<WiredBox>();
			remainder.addAll(boxes);
			WiredBox box = remainder.get(0);
			search(box, boxes, network);
		}
		// Now all boxes are in place, so time to install wires
		// for (WiredBox box : network.getBoxes()) {
		// ViewerBox viewerSource = getBoxToBoxViewerMap().get(box);
		// for (WiredBox target : network.getTargets(box)) {
		// ViewerBox viewerTarget = getBoxToBoxViewerMap().get(target);
		// getViewer().addWire(new ViewerWire(viewerSource, viewerTarget));
		// }
		// }
		for (WiredBox box : network.getBoxes()) {
			ViewerBox viewerSource = getBoxToBoxViewerMap().get(box);
			for (Port port : Connections.getPorts(box).getPorts()) {
				Set<String> destinationNames = port.getDestinationNames();
				viewerSource.getOutputPortNames().addAll(destinationNames);
				for (String name : destinationNames) {
					for (WiredBox destination : port.getDestinations(name)) {
						ViewerBox viewerTarget = getBoxToBoxViewerMap().get(destination);
						viewerTarget.getInputPortNames().add(name);
						getViewer().addWire(new ViewerWire(port, viewerSource, name, viewerTarget));	
					}
				}
			}
		}
	}

	private void search(WiredBox root, ArrayList<WiredBox> boxes, Network<WiredBox> network) {
		// Initialize set of boxes to search forward from
		ArrayList<WiredBox> targets = new ArrayList<WiredBox>();
		targets.add(root);

		// Remove box already handled
		targets = intersection(targets, boxes);

		int column = 0;

		while (!targets.isEmpty()) {
			// Handle current targets, then remove from set, so not handled
			// twice; depth gives depth-first arrangement, but seems screwed up,
			// so use depth = false
			if (depth) {
				process(targets.get(0), column);
				boxes.remove(targets.get(0));
			}
			else {
				process(targets, column);
				boxes.removeAll(targets);
			}
			// Find all boxes on next tier
			ArrayList<WiredBox> nextTargets = new ArrayList<WiredBox>();
			for (WiredBox target : targets) {
				// intersection rearranges targets in order boxes were created
				nextTargets = union(nextTargets, intersection(boxes, network.getTargets(target)));
			}
			// Remove boxes already handled
			targets = intersection(nextTargets, boxes);
			++column;
		}
		this.setMaximumLevelInColumn(0, getMaximumLevel());
	}

	private void process(ArrayList<WiredBox> targets, int column) {
		for (WiredBox box : targets) {
			process(box, column);
		}
	}

	private void process(WiredBox box, int column) {
		int level = getMaximumLevelInColumn(column);
		String name = "---";
		if (box instanceof Component) {
			name = ((Component) box).getName();
		}
		else if (box instanceof WiredBox) {
			name = ((WiredBox) box).getName();
		}
		if (name == null) {
			name = "---";
		}
		// System.out.println("Processing " + name + " at row/column = " +
		// getMaximumLevelInColumn(column) + "/" + column);
		ViewerBox viewerBox = new ViewerBox(level, column, name, box);
		if (box instanceof WiredOnOffSwitch) {
			WiredOnOffSwitch theBox = (WiredOnOffSwitch) box;
			theBox.addChangeListener(new SwitchStateListener(theBox, viewerBox));
			if (theBox.isSelected()) {
				viewerBox.setSwitchState(ViewerBox.ON_SWITCH);
			}
			else {
				viewerBox.setSwitchState(ViewerBox.OFF_SWITCH);
			}
		}
		if (box instanceof WiredToggleSwitch) {
			viewerBox.setToggleSwitch(true);
		}
		viewerBox.addObserver(getViewer());
		this.getBoxToBoxViewerMap().put(box, viewerBox);
		getViewer().addBox(viewerBox);
		incrementMaximumLevelInColumn(column);
	}

	private class SwitchStateListener implements ChangeListener {
		WiredOnOffSwitch switchBox;

		ViewerBox viewerBox;

		public SwitchStateListener(WiredOnOffSwitch switchBox, ViewerBox viewerBox) {
			this.switchBox = switchBox;
			this.viewerBox = viewerBox;
		}

		public void stateChanged(ChangeEvent e) {
			if (switchBox.isSelected()) {
				viewerBox.setSwitchState(ViewerBox.ON_SWITCH);
			}
			else {
				viewerBox.setSwitchState(ViewerBox.OFF_SWITCH);
			}
			getViewer().repaint();
		}
	}

	private ArrayList<WiredBox> intersection(ArrayList<WiredBox> targets, ArrayList<WiredBox> boxes) {
		ArrayList<WiredBox> result = new ArrayList<WiredBox>();
		for (WiredBox target : targets) {
			if (boxes.contains(target) && !result.contains(target)) {
				result.add(target);
			}
		}
		return result;
	}

	private ArrayList<WiredBox> union(ArrayList<WiredBox> listA, ArrayList<WiredBox> listB) {
		ArrayList<WiredBox> result = new ArrayList<WiredBox>();
		for (WiredBox candidate : listA) {
			if (!result.contains(candidate)) {
				result.add(candidate);
			}
		}
		for (WiredBox candidate : listB) {
			if (!result.contains(candidate)) {
				result.add(candidate);
			}
		}
		return result;
	}

	private void incrementMaximumLevelInColumn(int column) {
		int current = getMaximumLevelInColumn(column);
		++current;
		getLevelList().set(column, current);
	}

	private void setMaximumLevelInColumn(int column, int max) {
		getMaximumLevelInColumn(column);
		getLevelList().set(column, max);
	}

	private int getMaximumLevelInColumn(int column) {
		ArrayList<Integer> list = getLevelList();
		int currentSize = list.size();
		if (currentSize < column + 1) {
			for (int i = 0; i < column + 1 - currentSize; ++i) {
				list.add(0);
			}
		}
		return list.get(column);
	}

	private int getMaximumLevel() {
		int result = 0;
		ArrayList<Integer> list = getLevelList();
		for (int i = 0; i < list.size(); ++i) {
			result = Math.max(result, list.get(i));
		}
		return result;
	}

	private ArrayList<Integer> getLevelList() {
		if (levelList == null) {
			levelList = new ArrayList<Integer>();
		}
		return levelList;
	}

	public HashMap<WiredBox, ViewerBox> getBoxToBoxViewerMap() {
		if (boxToBoxViewerMap == null) {
			boxToBoxViewerMap = new HashMap<WiredBox, ViewerBox>();
		}
		return boxToBoxViewerMap;
	}

	public ViewerBox getViewerBox(WiredBox box) {
		return getBoxToBoxViewerMap().get(box);
	}

	public static void main(String[] args) {
		Test.main(args);
	}
}
