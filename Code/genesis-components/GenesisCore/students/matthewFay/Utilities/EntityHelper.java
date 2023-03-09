package matthewFay.Utilities;

import java.util.*;

import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.StoryAlignment.SequenceAlignment;
import translator.BasicTranslator;
import utils.*;
import utils.minilisp.LList;
import constants.Markers;
import edu.uci.ics.jung.graph.Forest;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

public class EntityHelper {

	public static Entity createStartStoryStructure(String name) {
		Relation root = null;
		Entity title = new Entity(name);
		Function story = new Function("story", title);
		Function object = new Function("object", story);
		Sequence roles = new Sequence("roles");
		roles.addElement(object);
		Entity you = new Entity("you");
		root = new Relation("start", you, roles);
		
		return root;
	}
	
	private static int genericCount = 0;

	public static Entity getGenericEntity() {
		Entity generic = new Entity("GEN_" + genericCount);
		generic.getPrimedThread().add(generic.getPrimedThread().size()-1, "name");
		genericCount++;
		return generic;
	}

	public static boolean isGeneric(Entity e) {
		if (e.getName().startsWith("GEN_")) return true;
		return false;
	}

	public static List<Entity> generalizeListOfEntities(List<Entity> story, Entity focus) {
		List<Entity> generalized_story = new ArrayList<Entity>();
		
		Set<Entity> all_entities_from_story = new HashSet<Entity>();
		for(Entity event : story) {
			all_entities_from_story.addAll(EntityHelper.getAllEntities(event));
		}
		List<PairOfEntities> replacements = new ArrayList<>();
		replacements.add(new PairOfEntities(focus, Generalizer.getFocus()));
		for(Entity entity : all_entities_from_story) {
			if(!entity.equals(focus)) {
				if(  CharacterProcessor.getCharacterLibrary().keySet().contains(entity) || CharacterProcessor.getGenericsLibrary().contains(entity) ) {
					Entity generic_entity = EntityHelper.getGenericEntity();
					replacements.add(new PairOfEntities(entity, generic_entity));
				}
			}
		}
		for(Entity event : story) {
			Entity new_event = event.deepClone(false);
			new_event = EntityHelper.findAndReplace(new_event, replacements, true);
			generalized_story.add(new_event);
		}
		
		return generalized_story;
	}
	
	public static List<Entity> sequenceToList(Sequence sequence) {
		ArrayList<Entity> list = new ArrayList<Entity>();

		for (int i = 0; i < sequence.getNumberOfChildren(); i++) {
			Entity t = sequence.getElement(i);
			// if(!t.isA("classification"))
			list.add(t);
		}

		return list;
	}

	public static String getStoryTitle(Sequence story) {
		try {
			Relation r = (Relation) story.getElement(0);
			Sequence s = (Sequence) r.getObject();
			Function d = (Function) s.getElement(0);
			d = (Function) d.getSubject();
			Entity t = d.getSubject();
			return t.getName();
		}
		catch (Exception e) {
			return story.getElement(0).asString();
		}
	}

	private static void getEntities(Entity thing, List<Entity> currentEntities) {
		if (thing.getType().equals("appear") && thing.functionP() && thing.getSubject().isA("gap")) {
			return;
		}
		if (thing.functionP()) {
			Entity subject = thing.getSubject();
			getEntities(subject, currentEntities);
		}
		else if (thing.featureP()) {
			Mark.say("Features not handled yet...");
		}
		else if (thing.relationP()) {
			if (thing.getSubject().entityP("you")) {
				if (thing.getObject().functionP(Markers.STORY_MARKER) || thing.getObject().functionP(Markers.CONCEPT_MARKER)) {
					// Do Nothing
				}
			}
			else if (thing.getType().equals("appear") && thing.functionP() && thing.getSubject().isA("gap")) {
				// Do Nothing
			}
			else {
				// } else if(!thing.isA("classification")) {
				Entity subject = thing.getSubject();
				Entity object = thing.getObject();
				getEntities(subject, currentEntities);
				getEntities(object, currentEntities);
			}
		}
		else if (thing.sequenceP()) {
			for (int i = 0; i < thing.getNumberOfChildren(); i++) {
				getEntities(thing.getElement(i), currentEntities);
			}
		}
		else if (thing.entityP()) {
			if (!currentEntities.contains(thing)) {
				currentEntities.add(thing);
			}
		}
	}

	public static List<Entity> getAllEntities(Entity thing) {
		List<Entity> entityList = new ArrayList<Entity>();
		getEntities(thing, entityList);
		return entityList;
	}
	
	/**
	 * Gets all the agents in a particular inner-ese structure
	 * Agents must be an entity of special status
	 * For example: characters, persons, countries, etc.
	 * For now, just using presence of a name as the indicator 
	 * @param entity
	 * @return
	 */
	public static List<Entity> getAllAgents(Entity entity) {
		List<Entity> entityList = getAllEntities(entity);
		List<Entity> agentList = new ArrayList<>();
		for(Entity e : entityList) {
			if(e.isA("name")
					|| e.isA("character")
					|| e.isA("person")
					|| e.isA("country")
					|| EntityHelper.isGeneric(e)
					|| e.isA("null")) {
				agentList.add(e);
			}
		}
		return agentList;
	}

	public static List<Entity> getAllEntities(List<Entity> listOfElements) {
		List<Entity> entityList = new ArrayList<Entity>();
		for (Entity element : listOfElements) {
			List<Entity> entities = getAllEntities(element);
			for (Entity entity : entities)
				if (!entityList.contains(entity) && entity != null) entityList.add(entity);
		}
		return entityList;
	}

	public static boolean containsEntity(Entity element, Entity entity) {
		if (element.equals(entity)) return true;
		if (element.functionP()) return EntityHelper.containsEntity(element.getSubject(), entity);
		if (element.relationP())
		    return EntityHelper.containsEntity(element.getSubject(), entity) || EntityHelper.containsEntity(element.getObject(), entity);
		if (element.sequenceP()) {
			for (Entity e : element.getChildren()) {
				if (EntityHelper.containsEntity(e, entity)) return true;
			}
		}
		return false;
	}

	public static List<PairOfEntities> bindingsToList(LList<PairOfEntities> bindings) {
		List<PairOfEntities> new_bindings = new ArrayList<>();
		for (PairOfEntities binding : bindings) {
			new_bindings.add(binding);
		}
		return new_bindings;
	}

	public static Entity instantiate(Entity element, LList<PairOfEntities> bindings) {
		return findAndReplace(element.deepClone(), bindings, true);
	}

	public static Entity instantiate(Entity element, List<PairOfEntities> bindings) {
		return findAndReplace(element.deepClone(), bindings, true, false);
	}

	public static Entity findAndReplace(Entity element, List<PairOfEntities> bindings) {
		return findAndReplace(element, bindings, false, false);
	}
	
	public static Entity findAndReplace(Entity element, List<PairOfEntities> bindings, boolean preserveUnknowns) {
		return findAndReplace(element, bindings, preserveUnknowns, false);
	}

	public static Entity findAndReplace(Entity element, List<PairOfEntities> bindings, boolean preserveUnknowns, boolean reverseBindings) {
		if (element.entityP()) {
			// Find Replacement and return it
			if(reverseBindings) {
				for(PairOfEntities pair : bindings) {
					if(pair.getDatum().isDeepEqual(element)) {
						if(pair.getDatum().getID() == element.getID()) {
							if(preserveUnknowns && pair.getPattern().getType().equals("null")) {
								return element;
							}
							return pair.getPattern();
						}
					}
				}
			} else {
				for (PairOfEntities pair : bindings) {
					if (pair.getPattern().isDeepEqual(element)) {
						if (pair.getPattern().getID() == element.getID()) {
							if(preserveUnknowns && pair.getDatum().getType().equals("null")) {
								return element;
							}
							return pair.getDatum();
						}
					}
				}
			}
			if (preserveUnknowns)
				return element;
			else
				return new Entity();
		}
		if (element.relationP()) {
			element.setSubject(findAndReplace(element.getSubject(), bindings, preserveUnknowns, reverseBindings));
			element.setObject(findAndReplace(element.getObject(), bindings, preserveUnknowns, reverseBindings));
			return element;
		}
		if (element.functionP()) {
			element.setSubject(findAndReplace(element.getSubject(), bindings, preserveUnknowns, reverseBindings));
			return element;
		}
		if (element.sequenceP()) {
			int i = 0;
			Sequence s = (Sequence) element;
			while (i < element.getNumberOfChildren()) {
				Entity child = element.getElement(i);
				child = findAndReplace(child, bindings, preserveUnknowns, reverseBindings);
				s.setElementAt(child, i);
				i++;
			}
			return element;
		}

		return element;
	}

	public static Entity findAndReplace(Entity element, LList<PairOfEntities> bindings, boolean preserveUnknowns, boolean reverseBindings) {
		return findAndReplace(element, bindingsToList(bindings), preserveUnknowns, reverseBindings);
	}
	
	public static Entity findAndReplace(Entity element, LList<PairOfEntities> bindings, boolean preserveUnknowns) {
		return findAndReplace(element, bindingsToList(bindings), preserveUnknowns, false);
	}

	public static Entity findAndReplace(Entity element, LList<PairOfEntities> bindings) {
		return findAndReplace(element, bindingsToList(bindings));
	}
	
	public static boolean contains(Entity pattern, Entity container) {
		boolean ret = false;
		if(container.isEqual(pattern))
			return true;
		for(Entity inner_container : container.getAllComponents())
			ret = ret || contains(pattern, inner_container);
		return ret;
	}
	
	static Forest<MatchNode, Integer> graph;

	static int edgeCount = 0;

	static List<MatchNode> leafNodes;

	public static class MatchNode implements Comparable<MatchNode> {
		private MatchNode parent;

		private Vector<MatchNode> children;

		public List<Entity> story1_entities;

		public List<Entity> story2_entities;

		public LList<PairOfEntities> bindingSet;
		
		public SequenceAlignment alignment = null;

		public float score;

		public void setParent(MatchNode node) {
			if (parent == node) return;

			if (parent != null) {
				parent.children.remove(this);
			}

			node.children.add(this);
			parent = node;
		}

		public MatchNode getParent() {
			return parent;
		}

		public Vector<MatchNode> getChildren() {
			return new Vector<MatchNode>(children);
		}

		public void addChild(MatchNode node) {
			if (node.parent != null) node.parent.children.remove(node);
			children.add(node);
		}

		public MatchNode() {
			parent = null;
			children = new Vector<MatchNode>();
			story1_entities = null;
			story2_entities = null;
			bindingSet = new LList<PairOfEntities>();
			score = Float.NEGATIVE_INFINITY;
		}

		/**
		 * Used for sorting highest to lowest
		 */
		@Override
		public int compareTo(MatchNode o) {
			if (score > o.score) return -1;
			if (score < o.score) return 1;
			return 0;
		}

	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		MatchNode node1 = new MatchNode();
		MatchNode node2 = new MatchNode();
		node1.score = 3.1934361f;
		node2.score = 2.9879107f;
		Mark.say(node1.compareTo(node2));

		Entity things = BasicTranslator.getTranslator().translate("Mark went to the store.");
		Mark.say(things.asString());
		List<Entity> list = getAllEntities(things);
		Mark.say(list.size());
		for (Entity thing : EntityHelper.getAllEntities(things)) {
			Mark.say(thing.asString(), thing.getType());
		}
		Entity thing2 = new Entity();
		Entity thing3 = new Entity("ACK");
		Mark.say(thing2.asString(), thing2.getType());
		Mark.say(thing3.asString(), thing3.getType());
	}

}
