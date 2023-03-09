package matthewFay.StoryGeneration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import matthewFay.representations.StoryGraph;
import matthewFay.viewers.StoryGraphViewerFX;
import utils.Mark;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

public class RuleGraphProcessor extends AbstractWiredBox {
	private static RuleGraphProcessor ruleGraphProcessor = null;

	public static RuleGraphProcessor getRuleGraphProcessor() {
		if (ruleGraphProcessor == null) {
			ruleGraphProcessor = new RuleGraphProcessor();
		}
		return ruleGraphProcessor;
	}

	private StoryGraph ruleGraph;

	public RuleGraphProcessor() {
		super("Rule graph processor");

		Connections.getPorts(this).addSignalProcessor("processRules");

		ruleGraph = new StoryGraph();
	}

	/**
	 * Given a sequence of rules, creates a rule graph
	 * 
	 * @param o
	 *            Sequence of Rules
	 */
	public void processRules(Object o) {
		BetterSignal s = BetterSignal.isSignal(o);
		if (o == null) {
			Mark.err("Not a good Signal!");
			return;
		}
		Sequence rules = s.get(2, Sequence.class);
		Mark.say("Intantiations: " + rules.asString());
		Sequence plotUnits = s.get(3, Sequence.class);
		Mark.say("Plot Units: " + plotUnits);

		for (Entity newRule : rules.getElements()) {
			String type = newRule.getType();
			List<Entity> antecedents = getAntecedents(newRule);
			Entity consequent = getConsequent(newRule);
			for (Entity antecedent : antecedents) {
				ruleGraph.addEdge(antecedent, consequent, type);
			}
			// Do settings for predictions, explanations, etc.
			if (newRule.isAPrimed(Markers.PREDICTION_RULE)) {
				ruleGraph.getNode(consequent).setPrediction(true);
			}
		}

		// Commented out for Commit
		StoryGraphViewerFX viewer = new StoryGraphViewerFX();
		viewer.init_from_swing(ruleGraph, plotUnits, "Rule Graph Viewer");
	}

	Random r = new Random();

	public Sequence generateRandomConnectedStory(StoryGraph graph) {
		return this.generateRandomConnectedStory(graph, 3);
	}

	public double scoreStory(StoryGraph graph, Sequence story) {
		double story_score = 0;

		for (Entity elt_origin : story.getElements()) {
			for (Entity elt_target : story.getElements()) {
				int distance = graph.distance(elt_origin, elt_target);
				story_score += distance;
			}
		}

		return story_score / 2;
	}

	public Sequence generateRandomStory(StoryGraph graph, int length) {
		Sequence story = new Sequence();

		graph.updateDephts();

		int max = graph.getNodeCount();

		int index = r.nextInt(max);
		Entity root = graph.getAllEntities().get(index);
		story.addElement(root);

		while (story.getNumberOfChildren() < length) {
			// Grab a random new element
			index = r.nextInt(max);
			Entity newElt = graph.getAllEntities().get(index);
			while (story.containsDeprecated(newElt)) {
				index = r.nextInt(max);
				newElt = graph.getAllEntities().get(index);
			}
			// Place the event at a reasonable place in the story
			for (int i = 0; i < story.getNumberOfChildren(); i++) {
				Entity elt = story.getElement(i);
				if (graph.getNode(newElt).depth <= graph.getNode(elt).depth) {
					story.addElement(i, newElt);
					break;
				}
			}
			if (!story.getElements().contains(newElt)) {
				// If we haven't put it in yet put it at the end
				story.addElement(newElt);
			}
		}
		return story;
	}

	public Sequence generateRandomConnectedStory(StoryGraph graph, int length) {
		Sequence story = new Sequence();

		graph.updateDephts();

		int max = graph.getNodeCount();

		int index = r.nextInt(max);
		Entity root = graph.getAllEntities().get(index);
		story.addElement(root);

		while (story.getNumberOfChildren() < length) {
			List<Entity> pool = new ArrayList<Entity>();
			// Get all current story event antecedents and consequents
			for (Entity elt : story.getElements()) {
				pool.addAll(graph.getAntecedents(elt));
				pool.addAll(graph.getConsequents(elt));
			}
			// Grab a random story event that is not in the story
			int failures = 0;
			index = r.nextInt(pool.size());
			Entity newElt = pool.get(index);
			while (story.containsDeprecated(newElt)) {
				failures++;
				if (failures > 50) break;
				index = r.nextInt(pool.size());
				newElt = pool.get(index);
			}
			// Place the event at a reasonable place in the story
			for (int i = 0; i < story.getNumberOfChildren(); i++) {
				Entity elt = story.getElement(i);
				if (graph.getNode(newElt).depth <= graph.getNode(elt).depth) {
					story.addElement(i, newElt);
					break;
				}
			}
			if (!story.getElements().contains(newElt)) {
				// If we haven't put it in yet put it at the end
				story.addElement(newElt);
			}
		}

		return story;
	}

	public void saveStories(StoryGraph graph, List<Sequence> stories) {
		int max = 0;
		for (Sequence story : stories) {
			max = Math.max(story.getNumberOfChildren(), max);
		}
		String csv = "";
		for (int i = 0; i < max; i++) {
			csv = csv + "Element " + i;
			if (i + 1 != max) csv = csv + ",";
		}
		csv = csv + ", Score";
		for (Sequence story : stories) {
			csv = csv + "\n";
			for (int i = 0; i < story.getNumberOfChildren(); i++) {
				Entity elt = story.getElement(i);
				csv = csv + elt.toEnglish();
				if (i + 1 != max) csv = csv + ",";
			}
			csv = csv + ", " + this.scoreStory(graph, story);
		}
		Mark.say(csv);
		try {
			File file = new File("stories.csv");
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(csv);
			bw.close();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private List<Entity> getAntecedents(Entity rule) {
		Entity s = rule.getSubject();
		List<Entity> antecedents = new ArrayList<Entity>();
		if (s.sequenceP()) {
			for (Entity a : s.getElements()) {
				antecedents.add(a);
			}
		}
		return antecedents;
	}

	private Entity getConsequent(Entity rule) {
		return rule.getObject();
	}
}
