package matthewFay.StoryAlignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import matthewFay.Demo;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.EntityHelper.MatchNode;
import matthewFay.viewers.AlignmentViewer;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.util.EdgeType;
import frames.entities.Entity;
import frames.entities.Sequence;

public class MatchTree {
	// Debugging Switches
	
	// Creates a match tree at each step of the process for debugging
	public static boolean debugPrintOutputDuringGeneration = false;//false
	
	//Switches
	//Only allows one good binding set to be returned
	public static boolean thereCanOnlyBeOne = true; //true
	
	//Debugging Node Tracking
	int nodesProcessed = 0;
	int nodesPruned = 0;
	int entitiesPruned = 0;
	
	//Variables in Algorithm
	private MatchNode root;
	public ArrayList<MatchNode> leafNodes;
	public Forest<MatchNode, Integer> graph;
	private int edgeCount = 0;
	private boolean foundLeaf = false;
	private float leafScore = Float.NEGATIVE_INFINITY;
	
	private NodeScorer scorer;
	private ArrayList<MatchNode> queue;
	
	/**
	 * Begins the generation of a MatchTree, for consistency when possible
	 * thingSetOne should be the pattern set of things and thingSetTwo should
	 * be the datum set of things.
	 * @param story1_entities Set of Things found in story (use ThingHelper)
	 * @param story2_entities Set of Things found in story (use ThingHelper)
	 */
	public MatchTree(List<Entity> story1_entities, List<Entity> story2_entities, NodeScorer scorer) {
		this.scorer = scorer;
		root = new MatchNode();
		root.story1_entities = story1_entities;
		root.story2_entities = story2_entities;
		root.score = Float.NEGATIVE_INFINITY;
		
		queue = new ArrayList<MatchNode>();
		queue.add(root);
		
		//Only needed to find all possible binding sets
		leafNodes = new ArrayList<MatchNode>();
		
		//Only needed for debugging
		graph = new DelegateTree<MatchNode, Integer>();
		graph.addVertex(root);
	}
	
	public void primeMatchTree(LList<PairOfEntities> bindings) {
		root.bindingSet = root.bindingSet.append(bindings);
		for(PairOfEntities pair : bindings) {
			Entity thing1 = pair.getPattern();
			Entity thing2 = pair.getDatum();
			root.story1_entities.remove(thing1);
			root.story2_entities.remove(thing2);
		}
	}
	
	private MatchNode createNode(MatchNode parent, Entity entity1, Entity entity2) {
		MatchNode newNode = new MatchNode();
		newNode.story1_entities = new ArrayList<Entity>(parent.story1_entities);
		newNode.story2_entities = new ArrayList<Entity>(parent.story2_entities);
		
		newNode.bindingSet = parent.bindingSet.cons(new PairOfEntities(entity1, entity2));
		newNode.story1_entities.remove(entity1);
		newNode.story2_entities.remove(entity2);
		
		newNode.setParent(parent);
		
		newNode.score = scorer.score(newNode);
		
		return newNode;
	}
	
	private MatchNode finishNode(MatchNode parent) {
		MatchNode newNode = new MatchNode();
		newNode.story1_entities = new ArrayList<Entity>(parent.story1_entities);
		newNode.story2_entities = new ArrayList<Entity>(parent.story2_entities);
		
		newNode.bindingSet = null;
		while(!newNode.story2_entities.isEmpty()) {
			if(newNode.bindingSet == null)
				newNode.bindingSet = parent.bindingSet.cons(new PairOfEntities(new Entity("null"),newNode.story2_entities.get(0)));
			else
				newNode.bindingSet = newNode.bindingSet.cons(new PairOfEntities(new Entity("null"),newNode.story2_entities.get(0)));
			newNode.story2_entities.remove(0);
		}
		
		newNode.setParent(parent);
		
		newNode.score = scorer.score(newNode);
		
		return newNode;
	}
	
	private void removeBadBindings() {
		float bad_threshold = 0.25f;
		float worstScore = queue.get(queue.size()-1).score;
		int worstCount = 0;
		for(int i=queue.size()-1;i>0;i--) {
			if(queue.get(i).score == worstScore) {
				worstCount++;
			} else {
				break;
			}
		}
		if( ((float)worstCount)/((float)queue.size()) > bad_threshold ) {
			while(worstCount>0) {
				queue.remove(queue.size()-1);
				nodesPruned++;
				worstCount--;
			}
		}
	}
	
	public void generateMatchTree() {
		AlignmentViewer.firstPopout = true;
		
		//Set the root/max score
		queue.get(0).score = scorer.score(queue.get(0));
		
		//note these are sadly deadly optimizations...
		findUnmatchableEntities1(queue.get(0));
		findUnmatchableEntities2(queue.get(0));
		
		while(!queue.isEmpty()) {
			if(debugPrintOutputDuringGeneration) {
				//AlignmentViewer.popoutVisualTree(graph);
				outputProgress();
				AlignmentViewer.popoutVisualTree(graph);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			MatchNode bestNode;
			bestNode = queue.get(0);
			queue.remove(0);
			
			
			
			if(!bestNode.story1_entities.isEmpty()) {
				Entity entity1 = bestNode.story1_entities.get(0);
				
				MatchNode newNode;
				
				newNode = this.createNode(bestNode, entity1, new Entity("null"));
				queue.add(newNode);
				
				graph.addEdge(edgeCount++, bestNode, newNode, EdgeType.DIRECTED);
				
				if(!bestNode.story2_entities.isEmpty()) {
					for(int i=0;i<bestNode.story2_entities.size();i++) {
						Entity entity2 = bestNode.story2_entities.get(i);
						newNode = this.createNode(bestNode, entity1, entity2);
						queue.add(newNode);
						
						graph.addEdge(edgeCount++, bestNode, newNode, EdgeType.DIRECTED);
					}
				}
				
				
				
			} else {
				if(!bestNode.story2_entities.isEmpty()) {
					
					MatchNode newNode = this.finishNode(bestNode);
					leafNodes.add(newNode);
					foundLeaf = true;
					leafScore = newNode.score;
					
					graph.addEdge(edgeCount++, bestNode, newNode, EdgeType.DIRECTED);
					
				} else {
					leafNodes.add(bestNode);
					foundLeaf = true;
					leafScore = bestNode.score;
				}
			}
			
			Collections.sort(queue);
			nodesProcessed++;
			if(queue.size()<1)
				continue;
			
			if(foundLeaf) {
				while(queue.get(queue.size()-1).score < queue.get(0).score) {
					queue.remove(queue.size()-1);
					nodesPruned++;
				}
				if(thereCanOnlyBeOne) {
					queue.clear();
				}
			} else {
				removeBadBindings();
				
				float bestThreshold = 0.50f;
				float bestScore = queue.get(0).score;
				int bestCount = 0;
				for(int i=0;i<queue.size()-2;i++) {
					if(queue.get(i).score == bestScore) {
						bestCount++;
					} else {
						break;
					}
				}
				if( ((float)bestCount)/((float)queue.size()) > bestThreshold ) {
					//Prune all but the best
					while(queue.size() > bestCount) {
						queue.remove(queue.size()-1);
						nodesPruned++;
					}
				}
			}
			
			
		}
		//
		//generateVisualTree(graph);
		if(debugPrintOutputDuringGeneration) {
			outputProgress();
			AlignmentViewer.popoutVisualTree(graph);
		}
	}

	private void findUnmatchableEntities2(MatchNode bestNode) {
		// Go through and find all entities for which nulling them does not affect alignment
		if(!bestNode.story2_entities.isEmpty()) {
			List<Entity> badEntities = new ArrayList<Entity>();
			for(Entity e2 : bestNode.story2_entities) {
				MatchNode tempNode = this.createNode(bestNode, new Entity("null"), e2);
				if(tempNode.score == bestNode.score) {
					badEntities.add(e2);
				}
			}
			if(badEntities.size() > 0) {
				MatchNode verifierNode = new MatchNode();
				verifierNode.story1_entities = new ArrayList<Entity>(bestNode.story1_entities);
				verifierNode.story2_entities = new ArrayList<Entity>(bestNode.story2_entities);
				verifierNode.bindingSet = null;
				for(Entity e2 : badEntities) {	
					if(verifierNode.bindingSet == null) {
						verifierNode.bindingSet = bestNode.bindingSet.cons(new PairOfEntities(new Entity("null"),e2));
					} else {
						verifierNode.bindingSet = verifierNode.bindingSet.cons(new PairOfEntities(new Entity("null"),e2));
					}
					verifierNode.story2_entities.remove(e2);
				}
				verifierNode.score = scorer.score(verifierNode);
				if(verifierNode.score == bestNode.score){
					bestNode.story2_entities = verifierNode.story2_entities;
					bestNode.bindingSet = verifierNode.bindingSet;
					entitiesPruned += badEntities.size();
				}
			}
		}
	}

	private void findUnmatchableEntities1(MatchNode bestNode) {
		// Go through and find all entities for which nulling them does not affect alignment
		if(!bestNode.story1_entities.isEmpty()) {
			List<Entity> badEntities = new ArrayList<Entity>();
			for(Entity e1 : bestNode.story1_entities) {
				MatchNode tempNode = this.createNode(bestNode, e1, new Entity("null"));
				if(tempNode.score == bestNode.score) {
					badEntities.add(e1);
				}
			}
			if(badEntities.size() > 0) {
				MatchNode verifierNode = new MatchNode();
				verifierNode.story1_entities = new ArrayList<Entity>(bestNode.story1_entities);
				verifierNode.story2_entities = new ArrayList<Entity>(bestNode.story2_entities);
				verifierNode.bindingSet = null;
				for(Entity e1 : badEntities) {	
					if(verifierNode.bindingSet == null) {
						verifierNode.bindingSet = bestNode.bindingSet.cons(new PairOfEntities(e1,new Entity("null")));
					} else {
						verifierNode.bindingSet = verifierNode.bindingSet.cons(new PairOfEntities(e1,new Entity("null")));
					}
					verifierNode.story1_entities.remove(e1);
				}
				verifierNode.score = scorer.score(verifierNode);
				if(verifierNode.score == bestNode.score){
					bestNode.story1_entities = verifierNode.story1_entities;
					bestNode.bindingSet = verifierNode.bindingSet;
					entitiesPruned += badEntities.size();
				}
			}
		}
	}

	public void outputProgress() {
		Mark.say("-----------------------------");
		Mark.say("Nodes Processed: ", nodesProcessed);
		Mark.say("Nodes in queue: ", queue.size());
		Mark.say("Nodes Pruned: ", nodesPruned);
		Mark.say("Entities Pruned: ", entitiesPruned);
		if(queue.size() > 0)
			Mark.say("Best Score: ", queue.get(0).score > leafScore ? queue.get(0).score : leafScore);
		else {
			Mark.say("Best Score: ", leafScore);
		}
		Mark.say("-----------------------------");
	}
	
	
	public static void main(String[] args) {
		Sequence story1 = Demo.MediatedExchangeStory();
		Sequence story2 = Demo.MediatedExchangeStory();

		
		List<Entity> list1 = EntityHelper.getAllEntities(story1);
		Entity mark1 = null;
		for(Entity thing : list1) {
			if(thing.getName().toLowerCase().contains("mark"))
				mark1 = thing;
		}
		List<Entity> list2 = EntityHelper.getAllEntities(story2);
		Entity mark2 = null;
		for(Entity thing : list2) {
			if(thing.getName().toLowerCase().contains("mark"))
				mark2 = thing;
		}
		LList<PairOfEntities> bindings = new LList<PairOfEntities>();
		bindings = bindings.cons(new PairOfEntities(mark1, mark2));
		
		MatchTree matchTree = new MatchTree(EntityHelper.getAllEntities(story1), EntityHelper.getAllEntities(story2), new NWSequenceAlignmentScorer(story1.getElements(), story2.getElements()));
		matchTree.primeMatchTree(bindings);
		matchTree.generateMatchTree();
		AlignmentViewer.popoutVisualTree(matchTree.graph);
		Mark.say(story1.asString());
		Mark.say(story2.asString());
	}
}
