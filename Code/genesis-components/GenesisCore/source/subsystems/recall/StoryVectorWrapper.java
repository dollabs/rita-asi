package subsystems.recall;

import java.util.HashMap;

import storyProcessor.*;
import utils.Mark;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

/*
 * Created on Jul 12, 2010
 * @author phw
 */

public class StoryVectorWrapper {

	private String title;

	private Sequence story;

	HashMap<String, Integer> conceptVector = new HashMap();

	HashMap<String, Integer> entityVector = new HashMap();

	public StoryVectorWrapper(Sequence story, ConceptAnalysis analysis) {
		setTitle(analysis.getStoryName());

		computeConceptMap(analysis);
		computeEntityMap(story);
	}

	private void computeEntityMap(Sequence story) {
		extractEntities(story);
	}

	private boolean exclude(String s) {

		if (s.equalsIgnoreCase(Markers.CAUSE_MARKER) || s.equalsIgnoreCase(Markers.CLASSIFICATION_MARKER)) {
			return true;
		}
		else if (s.equalsIgnoreCase("person") || s.equalsIgnoreCase("property")) {
			return true;
		}
		return false;
	}

	private void extractEntities(Entity entity) {
		if (entity.sequenceP()) {
			entity.getElements().stream().forEach(f -> extractEntities(f));
		}
		else if (entity.relationP()) {
			incrementValue(entity.getType(), getEntityMap());
			extractEntities(entity.getSubject());
			extractEntities(entity.getObject());
		}
		else if (entity.functionP()) {
			extractEntities(entity.getSubject());
		}
		else if (entity.entityP()) {
			incrementValue(entity.getType(), getEntityMap());
		}
	}

	private void computeConceptMap(ConceptAnalysis analysis) {
		for (ConceptDescription completion : analysis.getConceptDescriptions()) {
			incrementValue(completion.getName(), getConceptMap());
		}
	}

	/*
	 * For debugging gui only
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	public boolean equals(Object o) {
		if (o instanceof StoryVectorWrapper) {
			StoryVectorWrapper s = (StoryVectorWrapper) o;
			if (this.getTitle().equalsIgnoreCase(s.getTitle())) {
				return true;
			}
		}
		return false;
	}

	public String getTitle() {
		return title;
	}

	public HashMap<String, Integer> getConceptMap() {
		return conceptVector;
	}

	public HashMap<String, Integer> getEntityMap() {
		return entityVector;
	}

	public void incrementValue(String word, HashMap<String, Integer> map) {
		if (exclude(word)) {
			return;
		}

		Integer i = map.get(word);
		if (i == null) {
			map.put(word, 1);
			// length2 += 1;
		}
		else {
			// length2 -= Math.pow(i, 2);
			map.put(word, i + 1);
			// length2 += Math.pow(i + 1, 2);
		}
		// length = Math.sqrt(length2);
	}

	public int getValue(String word, HashMap<String, Integer> map) {
		Integer i = map.get(word);
		if (i == null) {
			return 0;
		}
		else {
			return i;
		}
	}

	// public double getLength() {
	// return length;
	// }

	public Sequence getStory() {
		return story;
	}

	public String toString() {
		String result = "<" + getTitle().toString();
		for (String s : conceptVector.keySet()) {
			result += " (" + s + ", " + conceptVector.get(s) + ")";
		}
		result += ">";
		return result;
	}

}
