package dylanHolmes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Observable;

import connections.Connections;
import connections.WiredBox;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import frames.entities.Entity;

public class NetworkViewer<V extends WiredBox> extends Observable implements WiredBox {
	// map : sends string to localwiredbox
	// boxlist : array of localwiredboxes
	// plotunits: sends string to array of localwiredboxes
	
	// localwiredbox might be called an "EntityBox"
	// n.b. each localwiredbox is named after the Entity it displays.
	
	protected String name;
	protected Graph<V, String> network;
	protected HashMap<String, ArrayList<V>> boxGroups;
	
	public String getName(){
		return name;
	}
	public Graph<V, String> getNetwork(){
		return network;
	}
	
	
	/**
	 * Fetch a box based on its name.
	 * 
	 * Performs a linear search through the network's list of vertices for a box with 
	 * the given name. If the box is found, returns it. Otherwise, returns null.
	 * 
	 * @param name
	 * @return
	 */
	public V getBoxByName(String name) {
		Collection<V> boxes = this.network.getVertices();
		for(V b : boxes){
				if(b.getName() == name) {
					return b;
				}
		}
		return null;
	}
	
	
	public void changed() {
		setChanged();
		notifyObservers();
	}

	protected void changed(Object o) {
		setChanged();
		notifyObservers(o);
	}
	
	
	
	public NetworkViewer(String name) {
		this.name = name;
		this.network = new DirectedSparseGraph<V, String>();
	}
	
	
	
}
