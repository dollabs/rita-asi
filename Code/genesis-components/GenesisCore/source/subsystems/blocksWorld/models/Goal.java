package subsystems.blocksWorld.models;

import java.util.Vector;

import utils.Mark;

/*
 * Created on Sep 11, 2005
 * @author Patrick
 */

public class Goal {

	String[] keyWords = { "put", "make", "grasp", "ungrasp", "clear", "rid", "move", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "table" };

	String description;

	Goal supergoal;

	Vector subgoals = new Vector();

	public Goal(Goal superGoal, String description) {
		this.supergoal = superGoal;
		if (supergoal != null) {
			supergoal.addSubgoal(this);
		}
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Vector getSubgoals() {
		return subgoals;
	}

	public void setSubgoals(Vector subgoals) {
		this.subgoals = subgoals;
	}

	public void addSubgoal(Goal g) {
		subgoals.add(g);
		g.setSupergoal(this);
	}

	public String toString() {
		return description;
	}

	public String toStringComplete() {
		return toString(0);
	}

	public String toString(int level) {
		String indent = pad(level);
		String result = indent + description;
		for (int i = 0; i < subgoals.size(); ++i) {
			Goal subgoal = (Goal) (subgoals.get(i));
			result += '\n' + subgoal.toString(level + 1);
		}
		return result;
	}

	private String pad(int n) {
		String result = "";
		for (int i = 0; i < n; ++i) {
			result += " ";
		}
		return result;
	}

	public Goal getSupergoal() {
		return supergoal;
	}

	public void setSupergoal(Goal supergoal) {
		this.supergoal = supergoal;
	}

	public Vector findHow(String question) {
		return findHow(this, question);
	}

	public Vector findHow(Goal goal, String question) {
		if (test(question, goal.getDescription())) {
			return goal.getSubgoals();
		}
		Vector v = goal.getSubgoals();
		for (int i = 0; i < v.size(); ++i) {
			Goal subgoal = (Goal) (v.get(i));
			Vector result = findHow(subgoal, question);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public Goal find(String question) {
		return find(this, question);
	}

	public Goal find(Goal goal, String question) {
		if (test(question, goal.getDescription())) {
			return goal;
		}
		Vector v = goal.getSubgoals();
		for (int i = 0; i < v.size(); ++i) {
			Goal subgoal = (Goal) (v.get(i));
			Goal result = find(subgoal, question);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public Goal findWhy(String question) {
		return findWhy(this, question);
	}

	public Goal findWhy(Goal goal, String question) {
		if (test(question, goal.getDescription())) {
			return goal.getSupergoal();
		}
		Vector v = goal.getSubgoals();
		for (int i = 0; i < v.size(); ++i) {
			Goal subgoal = (Goal) (v.get(i));
			Goal result = findWhy(subgoal, question);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	private boolean test(String question, String history) {
		return match(filter(question), history.toLowerCase());
	}

	private boolean match(Vector words, String string) {
		Mark.say("Matching", words, string);
		for (int i = 0; i < words.size(); ++i) {
			String word = (String) (words.get(i));
			int index = string.indexOf(word);
			if (index >= 0) {
				string = string.substring(word.length()).trim();
			}
			else {
				Mark.say("false");
				return false;
			}
		}
		Mark.say("True");
		return true;
	}

	private Vector filter(String text) {
		Vector output = new Vector();
		Vector input = vectorize(text);
		for (int i = 0; i < input.size(); ++i) {
			String word = (String) (input.get(i));
			for (int j = 0; j < keyWords.length; ++j) {
				if (keyWords[j].equalsIgnoreCase(word)) {
					output.add(word.toLowerCase());
				}
			}
		}
		return output;
	}

	private Vector vectorize(String text) {

		Vector result = new Vector();
		text = text.trim();
		while (text.length() > 0) {
			int index = text.indexOf(" ");
			if (index >= 0) {
				result.add(text.substring(0, index));
				text = text.substring(index + 1).trim();

			}
			else {
				result.add(text);
				text = "";
			}
		}
		return result;

	}

}
