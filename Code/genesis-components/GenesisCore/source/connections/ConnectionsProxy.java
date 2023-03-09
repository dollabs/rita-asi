package connections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// needed to tease apart Connections from the GUI code it has been intertwined with
// stupidly inefficient
public class ConnectionsProxy {
	public ConnectionsProxy() {
		initialize();
	}

	private Map<String, WiredBox> allNodes = new HashMap<String, WiredBox>();

	private Map<String, WiredBox> justMyNodes = new HashMap<String, WiredBox>();

	private void initialize() {
		for (WiredBox box : Connections.getInstance().getBoxes()) {
			if (box != null) {
				String id = WireClientEndpoint.getInstance().getUUID(box);
				allNodes.put(id, box);
				if (box instanceof WireClientEndpoint) {
					justMyNodes.put(id, box);
				}
			}
		}
	}

	public Set<WiredBox> getProxyNodesWithConnectionsOut() {
		Set<WiredBox> result = new HashSet<WiredBox>();
		for (WiredBox node : justMyNodes.values()) {
			for (Port p : Connections.getPorts(node).getPorts()) {
				if (p.getSourceBox() == node) {
					result.add(node);
				}
			}
		}
		return result;
	}

	public Set<String> getConnectedOutPorts(WiredBox b) {
		Set<String> result = new HashSet<String>();
		for (Port p : Connections.getPorts(b).getPorts()) {
			if (p.getSourceBox() == b) {
				result.add(p.getSourceName());
			}
		}
		return result;
	}

}
