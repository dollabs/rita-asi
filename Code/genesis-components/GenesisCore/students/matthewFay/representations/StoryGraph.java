package matthewFay.representations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import frames.entities.Entity;
import matthewFay.Utilities.HashMatrix;

import utils.Mark;

public class StoryGraph {
	private Map<Entity, StoryGraphNode> graphNodes;
	private List<StoryGraphEdge> graphEdges;
	
	private HashMatrix<StoryGraphNode, StoryGraphNode, Integer> distances;
	private HashMatrix<StoryGraphNode, StoryGraphNode, Integer> directedDistances;
	
	public StoryGraph() {
		graphNodes = new HashMap<Entity, StoryGraphNode>();
		graphEdges = new ArrayList<StoryGraphEdge>();
		distances = new HashMatrix<StoryGraphNode, StoryGraphNode, Integer>();
		directedDistances = new HashMatrix<StoryGraphNode, StoryGraphNode, Integer>();
	}
	
	public boolean addNode(Entity entity) {
		if(!graphNodes.containsKey(entity))
			return false;			
		graphNodes.put(entity, new StoryGraphNode(entity));
		return true;
	}
	
	public void clear() {
		graphNodes.clear();
		graphEdges.clear();
		distances.clear();
		directedDistances.clear();
	}
	
	public boolean addEdge(Entity antecedent, Entity consequent, String type) {
		if(!graphNodes.containsKey(antecedent))
			graphNodes.put(antecedent, new StoryGraphNode(antecedent));
		if(!graphNodes.containsKey(consequent))
			graphNodes.put(consequent, new StoryGraphNode(consequent));
		StoryGraphNode antecedentNode = graphNodes.get(antecedent);
		StoryGraphNode consequentNode = graphNodes.get(consequent);
		if(antecedentNode.getConsequents().contains(consequentNode)) {
			Mark.say("Rejecting:");
			Mark.say(antecedent.asString());
			Mark.say("leads to");
			Mark.say(consequent.asString());
			return false;
		}
		graphEdges.add(new StoryGraphEdge(antecedentNode, consequentNode, type));
		return true;
	}
	
	public int getNodeCount() {
		return graphNodes.size();
	}
	public List<StoryGraphNode> getAllNodes() {
		ArrayList<StoryGraphNode> nodes = new ArrayList<>();
		for(StoryGraphNode node : graphNodes.values()) {
			nodes.add(node);
		}
		return nodes;
	}
	public List<Entity> getAllEntities() {
		ArrayList<Entity> entities = new ArrayList<>();
		for(StoryGraphNode node : graphNodes.values()) {
			entities.add(node.getEntity());
		}
		return entities;
	}
	public StoryGraphNode getNode(Entity e) {
		if(graphNodes.containsKey(e))
			return graphNodes.get(e);
		return null;
	}
	public List<Entity> getAntecedents(Entity e) {
		List<Entity> antecedents = new ArrayList<Entity>();
		StoryGraphNode node = getNode(e);
		if(node != null) {
			for(StoryGraphNode ante : node.getAntecedents()) {
				antecedents.add(ante.getEntity());
			}
		}
		return antecedents;
	}
	public List<Entity> getConsequents(Entity e) {
		List<Entity> consequents = new ArrayList<Entity>();
		StoryGraphNode node = getNode(e);
		if(node != null) {
			for(StoryGraphNode conq : node.getConsequents()) {
				consequents.add(conq.getEntity());
			}
		}
		return consequents;
	}
	
	public int getEdgeCount() {
		return graphEdges.size();
	}
	public List<StoryGraphEdge> getAllEdges() {
		ArrayList<StoryGraphEdge> edges = new ArrayList<StoryGraphEdge>();
		for(StoryGraphEdge edge : graphEdges) {
			edges.add(edge);
		}
		return edges;
	}
	
	public int distance(Entity origin, Entity target) {
		updateDistances();
		if(distances.contains(getNode(origin),getNode(target))) {
			if(distances.get(getNode(origin), getNode(target)) < 0)
				return Integer.MAX_VALUE;
			return distances.get(getNode(origin),getNode(target));
		}
		return Integer.MAX_VALUE;
	}
	
	public void updateDephts() {
		updateDistances();
		updateDirectedDistances();
		
		Set<StoryGraphNode> currentNodes = new HashSet<>();
		Set<StoryGraphNode> processedNodes = new HashSet<>();
		
		for(StoryGraphNode node : graphNodes.values()) {
			node.depth = -1;
			if(node.getAntecedents().isEmpty())
			{
				node.depth = 0;
				currentNodes.add(node);
			} 
		}

		int i=0;
		int max_depth = 0;
		//Simple depths, max distance from core antecedent
		while(!currentNodes.isEmpty()) {
			for(StoryGraphNode c_node : currentNodes) {
				for(StoryGraphNode node : graphNodes.values()) {
					if(node.depth != 0) {
						int distance = directedDistances.get(c_node, node);
						if(distance > 0) {
							node.depth = Math.max(node.depth,distance+i);
							max_depth = Math.max(max_depth, node.depth);
						}
					}
				}
			}
			processedNodes.addAll(currentNodes);
			currentNodes.clear();
			for(StoryGraphNode node : graphNodes.values()) {
				if(node.depth == i+1 && !processedNodes.contains(node)) {
					currentNodes.add(node);
				}
			}
			i++;
		}
		
		//Now do priorities
		currentNodes.clear();
		processedNodes.clear();
		i=1;
		int p=0;
		for(StoryGraphNode node : graphNodes.values()) {
			if(node.depth == i)
				node.height = ++p;
		}
		while(i<max_depth){
			for(StoryGraphNode node : graphNodes.values()) {
				if(node.depth == i) {
					node.height = Integer.MAX_VALUE;
					for(StoryGraphNode ante : node.getAntecedents()) {
						node.height = Math.min(node.height, ante.height);
					}
				}
			}
			i++;
		}
	}
	
	public void updateDistances() {
		boolean doUpdate = false;
		//First purge out the unused nodes
		ArrayList<StoryGraphNode> purgeNodes = new ArrayList<StoryGraphNode>();
		for(StoryGraphNode node : distances.keySetRows()) {
			if(!graphNodes.values().contains(node)) {
				purgeNodes.add(node);
				doUpdate = true;
			}
		}
		
		//Make sure we still have all the nodes in the graph
		for(StoryGraphNode node_origin : graphNodes.values()) {
			if(!distances.keySetRows().contains(node_origin)) {
				doUpdate = true;
			}
		}
		
		if(!doUpdate)
			return;
		
		distances.clear();
		
		//Initialize any new nodes
		for(StoryGraphNode node_origin : graphNodes.values()) {
			for(StoryGraphNode node_target : graphNodes.values()) {
				if(node_origin == node_target) {
					distances.put(node_origin, node_target, 0);
				} else {
					distances.put(node_origin, node_target, -1);
					distances.put(node_target, node_origin, -1);
				}
			}
		}
		
		//Loop until stable
		while(doUpdate) {
			doUpdate=false;
			for(StoryGraphNode node_origin : graphNodes.values()) {
				for(StoryGraphNode node_target : graphNodes.values()) {
					for(StoryGraphNode node_neighbor : node_origin.getAllNeighbors()) {
						Integer cur_distance = distances.get(node_origin, node_target);
						Integer new_distance = distances.get(node_neighbor, node_target);
						if(cur_distance == null || cur_distance < 0) {
							cur_distance = Integer.MAX_VALUE;
						}
						if(new_distance == null || new_distance < 0) {
							new_distance = Integer.MAX_VALUE;
						}
						if(new_distance != cur_distance) {
							if(new_distance < cur_distance-1) {
								//Shorter path found!
								distances.put(node_origin, node_target, new_distance+1);
								distances.put(node_target, node_origin, new_distance+1);
								doUpdate = true;
							}
						}
					}
				}
			}
		}
	}
	
	public void updateDirectedDistances() {
		boolean doUpdate = false;
		//First purge out the unused nodes
		ArrayList<StoryGraphNode> purgeNodes = new ArrayList<StoryGraphNode>();
		for(StoryGraphNode node : directedDistances.keySetRows()) {
			if(!graphNodes.values().contains(node)) {
				purgeNodes.add(node);
				doUpdate = true;
			}
		}
		
		//Make sure we still have all the nodes in the graph
		for(StoryGraphNode node_origin : graphNodes.values()) {
			if(!directedDistances.keySetRows().contains(node_origin)) {
				doUpdate = true;
			}
		}
		
		if(!doUpdate)
			return;
		
		directedDistances.clear();
		
		//Initialize any new nodes
		for(StoryGraphNode node_origin : graphNodes.values()) {
			for(StoryGraphNode node_target : graphNodes.values()) {
				if(node_origin == node_target) {
					directedDistances.put(node_origin, node_target, 0);
				} else {
					directedDistances.put(node_origin, node_target, -1);
					directedDistances.put(node_target, node_origin, -1);
				}
			}
		}
		
		//Loop until stable
		while(doUpdate) {
			doUpdate=false;
			for(StoryGraphNode node_origin : graphNodes.values()) {
				for(StoryGraphNode node_target : graphNodes.values()) {
					for(StoryGraphNode node_neighbor : node_origin.getConsequents()) {
						Integer cur_distance = directedDistances.get(node_origin, node_target);
						Integer new_distance = directedDistances.get(node_neighbor, node_target);
						if(cur_distance == null || cur_distance < 0) {
							cur_distance = Integer.MAX_VALUE;
						}
						if(new_distance == null || new_distance < 0) {
							new_distance = Integer.MAX_VALUE;
						}
						if(new_distance != cur_distance) {
							if(new_distance < cur_distance-1) {
								//Shorter path found!
								directedDistances.put(node_origin, node_target, new_distance+1);
								doUpdate = true;
							}
						}
					}
				}
			}
		}
	}
}
