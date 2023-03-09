package matthewFay.StoryThreading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import frames.entities.Entity;
import frames.entities.Sequence;
import matthewFay.Utilities.EntityHelper;

/*
 *Goal of this class is to find the minimum set of characters
 *for which the entire story is completed
 */

public class MinimumSpanningCharaterSet {
	private HashMap<Entity, Sequence> threads;
	private List<Entity> entities;
	
	public MinimumSpanningCharaterSet(HashMap<Entity, Sequence> threads) {
		this.threads = threads;
		
		graph = new ArrayList<EventNode>();
		eventToGraph = new HashMap<Entity, EventNode>();
		
		entities = new ArrayList<Entity>();
		for(Object entity : threads.keySet().toArray()) {
			entities.add((Entity)entity);
		}
	}
	
	Map<Entity, EventNode> eventToGraph;
	List<EventNode> graph;	
	
	public List<EventNode> constructStoryGraph() {
		//Construct Story Graph
		for(Entity entity : entities) {
			Sequence thread = threads.get(entity);
			List<Entity> events = thread.getAllComponents();
			for(int i=0;i<events.size();i++) {
				Entity event = events.get(i);
				EventNode node;
				if(eventToGraph.containsKey(event)) {
					node = eventToGraph.get(event);
				} else {
					node = new EventNode(event);
					graph.add(node);
					eventToGraph.put(event, node);
				}
				node.entities.add(entity);
				if (i > 0) {
					Entity lastEvent = events.get(i-1);
					EventNode lastNode = eventToGraph.get(lastEvent);
					node.pastEventNodes.add(lastNode);
					lastNode.futureEventNodes.add(node);
				}
			}
		}
		return graph;
	}
	
	class EventNode {
		public Entity event;
		public List<EventNode> futureEventNodes;
		public List<EventNode> pastEventNodes;
		public List<Entity> entities;
		
		public EventNode(Entity event) {
			this.event = event;
			futureEventNodes = new ArrayList<EventNode>();
			pastEventNodes = new ArrayList<EventNode>();
			entities = new ArrayList<Entity>();
		}
	}
	
	public int countEntities(Entity story) {
		List<Entity> entities = EntityHelper.getAllEntities(story);
		return entities.size();
	}
}
