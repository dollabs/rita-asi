package matthewFay.Depricated;

import java.awt.Dimension;
import java.util.*;

import javax.swing.JFrame;

import matchers.StandardMatcher;
import matthewFay.Demo;
import matthewFay.StoryAlignment.NWAligner;
import matthewFay.StoryAlignment.Alignment;
import matthewFay.StoryAlignment.RankedSequenceAlignmentSet;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.Utilities.Pair;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.EntityHelper.MatchNode;
import utils.*;
import utils.minilisp.LList;
import constants.Markers;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;

/*
 * Needs a Code Clean up
 */
@Deprecated
public class SequenceAligner extends NWAligner<Entity, Entity> {

	//Debugging Switches
	public static boolean debugCosts = false;
	
	
	//Utility Switches
	//////////////////
	//Generate English Output
	public static boolean generateNiceOutput = false;
	
	//Lock Level affects how to handle bindings during alignment
	//Normal allows new bindings to be created and added to the set of bindings
	//Locked forces the binding list to remain unchaged, matches requiring additions are failed
	//Loose forces the binding list to remain unchanged, matches requireing additions are allowed but not added to list
	//Unlocked allows as long as their is a match, does not use binding list
	public static enum LockLevel { NORMAL, LOCKED, LOOSE, UNLOCKED };
	public LockLevel lockLevel = LockLevel.NORMAL;
	public static enum AlignmentType { NORMAL, COMPLETE, FASTER };
	
	//Alignment Matrix Scoring Metrics
	public static float matchReward = 100;
	public static float gapFactor = 0.8f;
	public static float mismatchPenalty = -0.5f;
	public final static float gapPenaltyFactor = -0.2f;


	private static final boolean debugBest = true;

	private static final boolean restrictMatches = false;


	private static final boolean pruneSearchTree = false; 
	
	//If code is not running in parallel these can be reused
	public OneToOneMatcher matcher;
	public static Generator generator;
	
	//Binding List used for 
	public LList<PairOfEntities> bindings;
	
	//The internal aligner uses a slightly different scoring system
	//The constructor and align() functions make sure they are consistent
	public SequenceAligner()
	{
		setGapPenalty((int)(gapPenaltyFactor*matchReward));	
		matcher = OneToOneMatcher.getOneToOneMatcher();
		generator = Generator.getGenerator();
	} 
	
	@Override
	public float sim(Entity pattern, Entity datum) {
		float cost = mismatchPenalty*matchReward;
		
		if(datum.getType().equals("appear") &&
			  datum.functionP() &&
			  datum.getSubject().isA("gap")) {
			return (matchReward*gapFactor);
		}
		
		//Bindings Match Function
		//Normal allows additions to be made to the binding set during scoring
		if(lockLevel == LockLevel.NORMAL) {
			if(bindings == null)
			{
				bindings = matcher.match(pattern,datum);
				if(bindings != null)
					cost = matchReward;
			} else {
				LList<PairOfEntities> newBindings = matcher.match(pattern,datum,bindings);
				if(newBindings != null) {
					bindings = newBindings;
					cost = matchReward;
				}
			}
		//Locked forces the binding set to remain unchanged during scoring, and changes result in a failed match
		} else if(lockLevel == LockLevel.LOCKED) {
			if(bindings != null)
				try {
					LList<PairOfEntities> match = matcher.match(pattern,datum,bindings);
					if(match != null && match.size() == bindings.size())
						cost = matchReward;
				} catch(Exception e) {
					Mark.say("pattern", pattern);
					Mark.say("datum", datum);
					Mark.say("matcher", matcher);
					e.printStackTrace();
					System.exit(0);
				}
		//Loose forces the binding set to remain unchanged, but changes count as a match
		} else if(lockLevel == LockLevel.LOOSE) {
			if(bindings == null) {
				cost = matchReward;
			} else {
				try {
					LList<PairOfEntities> match = matcher.match(pattern,datum,bindings);
					if(match != null)
						cost = matchReward;
//					cost = matchReward * PartialMatcher.match(pattern, datum, bindings);
					
				} catch(Exception e) {
					Mark.say("pattern", pattern);
					Mark.say("datum", datum);
					Mark.say("matcher", matcher);
					e.printStackTrace();
					System.exit(0);
				}
			}
		//As long as a match is found the match is accepted
		} else if(lockLevel == LockLevel.UNLOCKED) {
			LList<PairOfEntities> match = matcher.match(pattern,datum);
			if(match != null)
				cost = matchReward;
		}
	
		// Simple Cost Function - exact match only//
		//cost = a.isEqual(b) ? matchReward : mismatchPenalty*matchReward;
		
		if(debugCosts) {
			Mark.say(debugCosts, pattern.asString());
			Mark.say(debugCosts, datum.asString());
			Mark.say(debugCosts, "Cost: ", cost);
		}
		
		return cost;
	}

	public RankedSequenceAlignmentSet<Entity, Entity> align(Sequence pattern, Sequence datum) {
		return align(pattern,datum,AlignmentType.NORMAL);
	}

	@SuppressWarnings("serial")
	public static class ProcessedSequence extends ArrayList<Entity> {
		public String name;
	}
	

	public static ProcessedSequence sequenceToList(Sequence sequence) {
		return sequenceToList(sequence, "sequence");
	}
	
	public static ProcessedSequence sequenceToList(Sequence sequence, String defaultName) {
		ProcessedSequence listOfThings = new ProcessedSequence();
		listOfThings.name = defaultName;
		for(int i=0;i<sequence.getNumberOfChildren(); i++) {
			if (sequence.getElement(i).relationP() && sequence.getElement(i).getSubject().entityP("you")) {
				if (sequence.getElement(i).getObject().functionP(Markers.STORY_MARKER) || sequence.getElement(i).getObject().functionP(Markers.CONCEPT_MARKER)) {
					listOfThings.name = sequence.getElement(i).getObject().getSubject().getType();
				}
			} else if(sequence.getElement(i).isA("classification")) {
				continue;
				//listOfThings.add(sequence.getElement(i));
			}  else {
				listOfThings.add(sequence.getElement(i));
			}
		}
		return listOfThings;
	}
	
	public RankedSequenceAlignmentSet<Entity, Entity> align(Sequence pattern, Sequence datum, AlignmentType type) {	
		setGapPenalty((int)(gapPenaltyFactor*matchReward));
		
		//Convert Sequences to Lists of Things//
		ProcessedSequence patternList = sequenceToList(pattern, "pattern");
		ProcessedSequence datumList = sequenceToList(datum, "datum");
		
		return align(patternList, datumList, type);
	}
	
	@SuppressWarnings("unused")
	public RankedSequenceAlignmentSet<Entity, Entity> align(ProcessedSequence patternList, ProcessedSequence datumList, AlignmentType type) {
		RankedSequenceAlignmentSet<Entity, Entity> alignments = new RankedSequenceAlignmentSet<Entity, Entity>();
		if(type == AlignmentType.NORMAL) {
			lockLevel = LockLevel.NORMAL;
			setBasicBindings(patternList,datumList);

			SequenceAlignment alignment = new SequenceAlignment(align(patternList, datumList)); 
			
			alignment.bindings = bindings;
			alignment.aName = patternList.name;
			alignment.bName = datumList.name;

			alignments.add(alignment);
			
		} else if(type == AlignmentType.COMPLETE || type == AlignmentType.FASTER) {
			List<Entity> patternThings = EntityHelper.getAllEntities(patternList);
			List<Entity> datumThings = EntityHelper.getAllEntities(datumList);
			
			List<LList<PairOfEntities>> allBindingPairSets = new ArrayList<LList<PairOfEntities>>();
			
			MatchNode root = new MatchNode();
			root.story1_entities = patternThings;
			root.story2_entities = datumThings;
			root.score = Float.NEGATIVE_INFINITY;
			lockLevel = LockLevel.LOOSE;
			
			ArrayList<MatchNode> leafNodes = new ArrayList<MatchNode>();
			
			Forest<MatchNode, Integer> graph = new DelegateTree<MatchNode, Integer>();
			int edgeCount = 0;
			graph.addVertex(root);
			
			int debugNodesProcessed = 0;
			int debugNodesInQueue = 0;
			
			PriorityQueue<MatchNode> queue = new PriorityQueue<EntityHelper.MatchNode>();
			queue.add(root);
			boolean foundLeaf = false;
			float leafScore = Float.NEGATIVE_INFINITY;
			while(!queue.isEmpty()) {
				debugNodesProcessed++;
				if(debugNodesProcessed%200 == 0 && debugBest) {
					Mark.say(debugBest, "-----------------------------");
					Mark.say(debugBest, "Nodes Processed: ", debugNodesProcessed);
					Mark.say(debugBest, "Nodes in queue: ", debugNodesInQueue = queue.size());
					Mark.say(debugBest, "Best Score: ", queue.peek().score > leafScore ? queue.peek().score : leafScore);
					Mark.say(debugBest, "-----------------------------");
					//generateVisualTree(graph);
				}
				if(foundLeaf && queue.peek().score < leafScore && type == AlignmentType.FASTER) {
					break;
				} else {
					MatchNode parent = queue.poll();
					if(!parent.story1_entities.isEmpty()) {
						Entity thingOne = parent.story1_entities.get(0);
						if(!parent.story2_entities.isEmpty()) {
							for(int i=0;i<parent.story2_entities.size();i++) {
								MatchNode newNode = new MatchNode();
								newNode.story1_entities = new ArrayList<Entity>(parent.story1_entities);
								newNode.story1_entities.remove(0);
								Entity thingTwo = parent.story2_entities.get(i);
								newNode.story2_entities = new ArrayList<Entity>(parent.story2_entities);
								newNode.story2_entities.remove(i);
								if(!restrictMatches || (StandardMatcher.getBasicMatcher().match(thingOne, thingTwo) != null && StandardMatcher.getBasicMatcher().match(thingTwo, thingOne) != null)) {
									newNode.bindingSet = parent.bindingSet.cons(new PairOfEntities(thingOne, thingTwo));
									newNode.setParent(parent);
									graph.addEdge(edgeCount++, parent, newNode, EdgeType.DIRECTED);
									// Score Node
									bindings = newNode.bindingSet;
									Alignment<Entity, Entity> newAlignment = align(patternList, datumList);
									newNode.score = newAlignment.score;
									// New scorer
									//newLeaf.score = PartialMatcher.match(pattern, datum, bindings);
									//
									queue.add(newNode);
								}
							}
						}
						MatchNode newNode = new MatchNode();
						newNode.story1_entities = new ArrayList<Entity>(parent.story1_entities);
						newNode.story1_entities.remove(0);
						newNode.story2_entities = new ArrayList<Entity>(parent.story2_entities);
						newNode.bindingSet = parent.bindingSet;
						newNode.bindingSet = parent.bindingSet.cons(new PairOfEntities(thingOne, new Entity()));
						newNode.setParent(parent);
						graph.addEdge(edgeCount++, parent, newNode, EdgeType.DIRECTED);
						// Score Node
						bindings = newNode.bindingSet;
						Alignment<Entity, Entity> newAlignment = align(patternList, datumList);
						newNode.score = newAlignment.score;
						queue.add(newNode);
					} else {
						if(!parent.story2_entities.isEmpty()) {
							MatchNode newNode = new MatchNode();
							newNode.story1_entities = new ArrayList<Entity>();
							Entity thingTwo = parent.story2_entities.get(0);
							newNode.story2_entities = new ArrayList<Entity>(parent.story2_entities);
							newNode.story2_entities.remove(0);
							newNode.bindingSet = parent.bindingSet;
							newNode.bindingSet = parent.bindingSet.cons(new PairOfEntities(new Entity(), thingTwo));
							newNode.setParent(parent);
							graph.addEdge(edgeCount++, parent, newNode, EdgeType.DIRECTED);
							// Score Node
							bindings = newNode.bindingSet;
							Alignment<Entity, Entity> newAlignment = align(patternList, datumList);
							newNode.score = newAlignment.score;
							queue.add(newNode);
						} else {
							leafNodes.add(parent);
							foundLeaf = true;
							leafScore = parent.score;
						}
					}
				}
				if(pruneSearchTree) {
					ArrayList<MatchNode> tempList = new ArrayList<EntityHelper.MatchNode>();
					if(!queue.isEmpty()) {
						float maxScore = queue.peek().score;
						if(leafScore >= maxScore)
							maxScore = leafScore;
						while(!queue.isEmpty() && queue.peek().score >= maxScore)
							tempList.add(queue.poll());
						queue.clear();
						while(!tempList.isEmpty()) {
							queue.add(tempList.get(0));
							tempList.remove(0);
						}
					}
				}
			}
			//generateVisualTree(graph);
			
			//Mark.say("Leaf Nodes: ", leafNodes.size());
			for(MatchNode leaf : leafNodes) {			
				LList<PairOfEntities> bindingPairSet = leaf.bindingSet;
				allBindingPairSets.add(bindingPairSet);
			}
			
			lockLevel = LockLevel.LOCKED;
			
			for(LList<PairOfEntities> bindings : allBindingPairSets) {
				this.bindings = bindings;
				SequenceAlignment newAlignment = new SequenceAlignment(align(patternList, datumList));
				newAlignment.bindings = bindings;
				newAlignment.aName = patternList.name;
				newAlignment.bName = datumList.name;
				alignments.add(newAlignment);
			}
		}
		return alignments;
	}

	@SuppressWarnings({ "unused", "rawtypes" })
	private void generateVisualTree(Forest<MatchNode, Integer> graph) {
		//Make Some Objects for Tree Layout
		TreeLayout<MatchNode, Integer> layout = new TreeLayout<MatchNode, Integer>(graph);
		VisualizationViewer<MatchNode, Integer> vv =
		new VisualizationViewer<MatchNode, Integer>(layout);
		vv.setPreferredSize(new Dimension(800,800));
		// Draw each vertex
		ToStringLabeller<MatchNode> vertexPaint = new ToStringLabeller<MatchNode>() {
			public String transform(MatchNode t) {
				if(t.bindingSet != null && t.bindingSet.size() > 0)
					return "Score: "+Float.toString(t.score)+"\n"+t.bindingSet.first().toString();
				return "root";
			}
		};
		vv.getRenderContext().setVertexLabelTransformer(vertexPaint);
		
		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		
		boolean external = true;
		if(external ) {
			JFrame frame = new JFrame("Simple Graph View");
			//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().add(vv);
			frame.pack();
			frame.setVisible(true);
		} else {		
//				treePanel.removeAll();
//				treePanel.add(vv);
//				tabbedPane.repaint();
		}
	}
	
	public void setBasicBindings(List<Entity> pattern, List<Entity> datum)
	{
		bindings = null;
		int aIteratorDown = pattern.size()-1;
		int bIteratorDown = datum.size()-1;
		int aIteratorUp = 0;
		int bIteratorUp = 0;
		while(aIteratorDown >= aIteratorUp && bIteratorDown >= bIteratorUp)
		{
			if(bindings == null)
			{
				bindings = matcher.match(pattern.get(aIteratorUp), datum.get(bIteratorUp));				
			} else {
				LList<PairOfEntities> newBindings = matcher.match(pattern.get(aIteratorUp), datum.get(bIteratorUp),bindings);
				if(newBindings != null)
					bindings = newBindings;
			}
			aIteratorUp++;
			bIteratorUp++;
		}
	}
	
	public static void outputAlignment(RankedSequenceAlignmentSet<Entity, Entity> alignments) {
		int i = 0;
		for(Alignment<Entity, Entity> alignment : alignments) {
			i++;
			Mark.say("\n Alignment "+i+":");
			outputAlignment(alignment);
		}
	}
	
	public static void outputAlignment(Alignment<Entity, Entity> alignment)
	{		
		int i=0;
		for(Pair<Entity, Entity> pair : alignment) {
			i++;
			Mark.say("Plot Element "+i+":");
			if(pair.a != null)
			{
				if(generateNiceOutput)
					try {
						Mark.say(generator.generate(pair.a), " ",pair.a.asString());
					} catch(Exception e) {
						
					}
				else
					Mark.say(pair.a.asString());
			}
			else
				Mark.say("---");
			if(pair.b != null)
			{
				if(generateNiceOutput)
					try {
						Mark.say(generator.generate(pair.b)," ",pair.b.asString());
					} catch(Exception e) {
						Mark.say(pair.b.asString());
					}
				else
					Mark.say(pair.b.asString());
			}
			else
				Mark.say("---");
		}
		Mark.say(debugCosts, "Alignment Score: ", alignment.score);
	}
	
//	/**
//	 * Given a list of patterns to match against finds the best alignment. 
//	 * @param datum The sequence to search for the best match of
//	 * @param patterns The patterns to search for the best match of the datum from
//	 * @return Returns a list of alignments matching the datum best from the set of patterns
//	 * most often should only contain one pattern.
//	 */
//	public List<Alignment<Thing, Thing>> findBestAlignments(List<Sequence> patterns, Sequence datum)
//	{
//		List<Alignment<Thing, Thing>> alignments = new ArrayList<Alignment<Thing, Thing>>();
//		
//		int bestScore = Integer.MIN_VALUE;
//		for(Sequence pattern : patterns)
//		{
//			Alignment<Thing, Thing> alignment = align(pattern, datum);
//			if(alignment.score > bestScore)
//			{
//				bestScore = alignment.score;
//				alignments.clear();
//				alignments.add(alignment);
//			} else if (alignment.score == bestScore) {
//				alignments.add(alignment);
//			}
//		}
//		
//		return alignments;
//	}
	
	/**
	 * Given a set of pattern sequences and a datum sequence,
	 * rankAlignments retrieves an ordered set of matching
	 * sequences to the datum
	 * @param patterns
	 * @param datum
	 * @return
	 */
	public RankedSequenceAlignmentSet<Entity, Entity> findBestAlignments(List<Sequence> patterns, Sequence datum, AlignmentType type)
	{
		RankedSequenceAlignmentSet<Entity, Entity> alignments = new RankedSequenceAlignmentSet<Entity, Entity>();
		
		for(Sequence pattern : patterns)
		{
			alignments.addAll(align(pattern, datum, type));
		}
		
		alignments.sort();
		//alignments.globalAlignment();
		
		return alignments;
	}
	
	public RankedSequenceAlignmentSet<Entity, Entity> findBestAlignments(List<Sequence> patterns, Sequence datum) {
		return findBestAlignments(patterns, datum, AlignmentType.FASTER);
	}

	public static void main(String[] args) {
		Sequence exchangeStory = Demo.MediatedExchangeStory();
		Sequence giveStory = Demo.GiveStory();
		
		SequenceAligner aligner = new SequenceAligner();
		
		outputAlignment(aligner.align(exchangeStory, giveStory, AlignmentType.COMPLETE));
		
		
	}
}