package matthewFay.StoryThreading;

import generator.Generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;
import matthewFay.StoryAlignment.AlignmentProcessor;
import matthewFay.Utilities.HashMatrix;
import matthewFay.Utilities.EntityHelper;

public class StoryThreadingProcessor extends AbstractWiredBox {
	public static final String STORY_INPUT_PORT = "story input port";

	public static final String STORY_SITCHED_OUTPUT_PORT = "story stitched output port";

	public static final String COMPARISON_PORT = "comparison port";

	private static String THREADING_PROCESSOR = "Story Threading Processor";

	public StoryThreadingProcessor() {
		super(THREADING_PROCESSOR);

		Connections.getPorts(this).addSignalProcessor(STORY_INPUT_PORT, "processStory");
	}

	public void processStory(Object input) {
		Sequence story = (Sequence) input;

		String story_title = EntityHelper.getStoryTitle(story);
		HashMap<Entity, Sequence> threads = getStoryThreads(story);

		Sequence stitchedStory = stitchStory(threads);

		Connections.getPorts(this).transmit(new BetterSignal(threads));
		Connections.getPorts(this).transmit(STORY_SITCHED_OUTPUT_PORT, new BetterSignal(stitchedStory));

		stitchedStory.getElements().stream().forEach(f -> Mark.say("Element", Generator.getGenerator().generate(f)));

		// This is used for comparing all characters, should probably be moved externally eventually
		for (Entity entity : threads.keySet()) {
			entityToStory.put(entity, story_title);
			allEntities.add(entity);
		}
		if (storyThreads.containsKey(story_title)) storyThreads.remove(story_title);
		storyThreads.put(story_title, threads);

		if (StoryThreadingViewer.doMinimumSpanningStory.isSelected()) {
			MinimumSpanningCharaterSet mscs = new MinimumSpanningCharaterSet(threads);
			mscs.constructStoryGraph();
			Mark.say("Done with MSCS");
		}
		if (StoryThreadingViewer.doCompareAllEntities.isSelected()) compareAllEntities();
	}

	public ArrayList<Entity> allEntities = new ArrayList<Entity>();

	public HashMap<Entity, String> entityToStory = new HashMap<Entity, String>();

	public HashMap<String, HashMap<Entity, Sequence>> storyThreads = new HashMap<String, HashMap<Entity, Sequence>>();

	// The goal of this function is to compute how similar each character is to each other character using alignment
	// This should probably be moved externally eventually
	public void compareAllEntities() {
		HashMatrix<Entity, Entity, Float> similarity = new HashMatrix<Entity, Entity, Float>();
		for (int i = 0; i < allEntities.size(); i++) {
			for (int j = i; j < allEntities.size(); j++) {
				Entity entity_i = allEntities.get(i);
				Entity entity_j = allEntities.get(j);
				String story_title_i = entityToStory.get(entity_i);
				String story_title_j = entityToStory.get(entity_j);
				Sequence thread_i = storyThreads.get(story_title_i).get(entity_i);
				Sequence thread_j = storyThreads.get(story_title_j).get(entity_j);
				// Do alignment
				// TODO: Should constrain the alignment to require entity_i and entity_j match
				AlignmentProcessor ap = new AlignmentProcessor();
				Mark.say("Aligning: ", entity_i.asString(), " and ", entity_j.asString());
				LList<PairOfEntities> bindings = new LList<PairOfEntities>();
				bindings = bindings.cons(new PairOfEntities(entity_i, entity_j));
				float score = ap.alignStories(thread_i, thread_j, bindings).score;
				similarity.put(entity_i, entity_j, score);
				if (!similarity.contains(entity_j, entity_i)) similarity.put(entity_j, entity_i, score);
			}
		}
		Connections.getPorts(this).transmit(COMPARISON_PORT, new BetterSignal(similarity));

		String csv = "";
		for (int i = 0; i < allEntities.size(); i++) {
			Entity entity_i = allEntities.get(i);
			if (csv.isEmpty()) {
				csv = entity_i.asString();
			}
			else {
				csv = csv + "," + entity_i.asString();
			}
		}
		for (int j = 0; j < allEntities.size(); j++) {
			Entity entity_j = allEntities.get(j);
			csv = csv + "\n" + entity_j.asString();
			for (int i = 0; i < allEntities.size(); i++) {
				Entity entity_i = allEntities.get(i);
				if (similarity.contains(entity_i, entity_j)) {
					float score = similarity.get(entity_j, entity_i);
					csv = csv + "," + score;
					Mark.say(entity_j.asString(), " : ", entity_i.asString(), " = ", score);
				}
			}
		}
		Mark.say(csv);
	}

	public boolean containsEntity(Entity story, Entity entity) {
		if (story.getType().equals("appear") && story.functionP() && story.getSubject().isA("gap")) {
			return false;
		}
		if (story.functionP()) {
			Entity subject = story.getSubject();
			if (containsEntity(subject, entity)) return true;
		}
		else if (story.featureP()) {
			Mark.say("Features not handled yet...");
		}
		else if (story.relationP()) {
			if (story.getSubject().entityP("you")) {
				if (story.getObject().functionP(Markers.STORY_MARKER) || story.getObject().functionP(Markers.CONCEPT_MARKER)) {
					// Do Nothing
				}
			}
			else if (!story.isA("classification")) {
				Entity subject = story.getSubject();
				Entity object = story.getObject();
				if (containsEntity(subject, entity)) return true;
				if (containsEntity(object, entity)) return true;
			}
		}
		else if (story.sequenceP()) {
			for (int i = 0; i < story.getNumberOfChildren(); i++) {
				if (containsEntity(story.getElement(i), entity)) return true;
			}
		}
		else if (story.entityP()) {
			if (story.equals(entity)) return true;
		}
		return false;
	}

	public int countEntities(Entity story) {
		List<Entity> entities = EntityHelper.getAllEntities(story);
		return entities.size();
	}

	public Sequence stitchStory(HashMap<Entity, Sequence> threads) {
		Sequence story = new Sequence();
		ArrayList<Entity> entities = new ArrayList<Entity>();
		HashMap<Entity, Integer> thread_its = new HashMap<Entity, Integer>();
		for (Object entity : threads.keySet().toArray()) {
			entities.add((Entity) entity);
			thread_its.put((Entity) entity, 0);
		}
		Collections.sort(entities, new Comparator<Entity>() {

			@Override
			public int compare(Entity o1, Entity o2) {
				return o1.asString().compareTo(o2.asString());
			}

		});

		int e_it = 0;
		HashMap<Entity, Integer> event_occurances = new HashMap<Entity, Integer>();
		while (entities.size() > 0) {
			if (e_it >= entities.size()) e_it = 0;
			Entity entity = entities.get(e_it);
			int thread_it = thread_its.get(entity);
			if (thread_it >= threads.get(entity).getNumberOfChildren()) {
				entities.remove(e_it);
				continue;
			}
			Entity event = threads.get(entity).getElement(thread_it);
			int e_count = countEntities(event);

			int occurances = 0;
			if (event_occurances.containsKey(event)) {
				occurances = event_occurances.get(event);
			}
			occurances++;
			if (occurances == e_count) {
				// All entities are at this event, add to story and continue
				story.addElement(event);
				thread_it++;
				thread_its.remove(entity);
				thread_its.put(entity, thread_it);
				event_occurances.remove(event);
				event_occurances.put(event, occurances);
				continue;
			}
			else if (occurances > e_count) {
				// The event has already been added, move along
				thread_it++;
				thread_its.remove(entity);
				thread_its.put(entity, thread_it);
				event_occurances.remove(event);
				event_occurances.put(event, occurances);
				continue;
			}
			else {
				// The event hasn't been seen often enough yet, please continue with other entities
				event_occurances.remove(event);
				event_occurances.put(event, occurances);
				e_it++;
				continue;
			}
		}
		return story;
	}

	public HashMap<Entity, Sequence> getStoryThreads(Sequence story) {
		// First find all of the Entities in the story
		// Mark.say("Finding Entities...");
		List<Entity> entities = EntityHelper.getAllEntities(story);

		// For each actor, find story thread
		// Mark.say("Finding Story Threads...");
		HashMap<Entity, Sequence> threads = new HashMap<Entity, Sequence>();
		for (Entity entity : entities) {
			Sequence st = new Sequence();
			for (int i = 0; i < story.getNumberOfChildren(); i++) {
				if (containsEntity(story.getElement(i), entity)) {
					st.addElement(story.getElement(i));
				}
			}
			threads.put(entity, st);
		}

		return threads;
	}
}
