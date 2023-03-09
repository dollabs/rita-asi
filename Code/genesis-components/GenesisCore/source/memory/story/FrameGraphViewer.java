package memory.story;

import java.util.HashSet;
import java.util.Hashtable;
import javax.swing.JFrame;

import connections.WiredBox;
import frames.entities.Entity;
import frames.entities.Relation;

/**
 * Custom Built Frame Graph Viewer
 * Just laid the framework out, but didn't actually start writing code
 * 
 * @author ryscheng
 * @date 2008/10/22
 */
public class FrameGraphViewer extends JFrame implements WiredBox{
	private static final long serialVersionUID = 1L;
	private FrameGraph data;
	private Hashtable<Entity,Integer> nodeMap;
	
	public FrameGraphViewer() {
		//this.getContentPane().add(new JScrollPane(graph));
		//this.pack();
		this.fillNodeMap();
		repaint();
	}
	
	/**
	 * @return the data
	 */
	private FrameGraph getData() {
		return this.data;
	}
	/**
	 * @param data the data to set
	 */
	private void setData(FrameGraph data) {
		this.data = data;
	}
	/**
	 * @return the nodeMap
	 */
	private Hashtable<Entity, Integer> getNodeMap() {
		return this.nodeMap;
	}
	/**
	 * @param nodeMap the nodeMap to set
	 */
	private void setNodeMap(Hashtable<Entity, Integer> nodeMap) {
		this.nodeMap = nodeMap;
	}
	
	private void fillNodeMap(){
		this.setNodeMap(new Hashtable<Entity,Integer>());
		HashSet<Entity> nodeSet = this.getData().getNodes();
		int count=0;
		
		for (Entity node : nodeSet){
			this.getNodeMap().put(node, count);
			count++;
		}
	}
	
	public void setParameters(FrameGraph input){
		this.setData(input);
		this.fillNodeMap();
		this.setVisible(true);
		repaint();
	}
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		FrameGraphViewer view = new FrameGraphViewer();
		FrameGraph graph = new FrameGraph();
		//FrameGraph newGraph;
		//List<String> resultListStr;
		//List<Thing> resultListThing;
		//Sequence graphSeq;
		Entity t1 = new Entity("Ray");
		Entity t2 = new Entity("Patrick");
		Entity t3 = new Entity("Mike");
		Entity t4 = new Entity("Mark");

		graph.add(t3, t1, "older");
		graph.add(t3, t1, "not as cool");
		graph.add(t4, t3, new Relation("older", t4, t3));
		graph.add(new Relation("older", t2, t4));
		
		view.setBounds(0,0,500,500);
		view.setVisible(true);
		view.setParameters(graph);
		  /**
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 300, 300);
		frame.setVisible(true);
		**/
	}



}
