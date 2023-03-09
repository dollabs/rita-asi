package utils.tools;

import java.util.ArrayList;

import frames.entities.Entity;
import frames.entities.Sequence;
import utils.Mark;

/*
 * Created on Jul 1, 2014
 * @author phw
 */

public class Search {

	private static ArrayList<Entity> findConsequents(Entity element, Sequence story) {
		ArrayList<Entity> consequents = new ArrayList<>();
		for (Entity x : story.getElements()) {
			if (Predicates.isCause(x)) {
				if (x.getSubject().getElements().contains(element)) {
					consequents.add(x.getObject());
				}
			}
		}
		return consequents;
	}

	private static boolean isConsequent(Entity element, Sequence story) {
		for (Entity x : story.getElements()) {
			if (Predicates.isCause(x)) {
				if (x.getObject().equals(element)) {
					return true;
				}
			}
		}
		return false;
	}

	public static ArrayList<Entity> findLongestPath(Entity start, Sequence story) {

		ArrayList<Entity> initialPath = new ArrayList<>();
		ArrayList<Entity> winningPath = initialPath;

		// Dont bother if a cause or consequent; irrelevant in first case; something longer in second case
		if (Predicates.isCause(start) || isConsequent(start, story)) {
			return winningPath;
		}

		ArrayList<ArrayList<Entity>> pathQueue = new ArrayList<>();

		initialPath.add(start);

		pathQueue.add(initialPath);

		while (!pathQueue.isEmpty()) {

			ArrayList<Entity> firstElement = pathQueue.remove(0);

			if (winningPath.size() < firstElement.size()) {
				winningPath = firstElement;
			}

			ArrayList<Entity> consequents = findConsequents(firstElement.get(firstElement.size() - 1), story);

			for (Entity extension : consequents) {
				ArrayList<Entity> clone = (ArrayList) (firstElement.clone());
				clone.add(extension);
				pathQueue.add(clone);
			}

		}

		return winningPath;

	}

	public static ArrayList<Entity> findLongestPath(Sequence story) {
		ArrayList<Entity> winningPath = new ArrayList<>();

		for (Entity e : story.getElements()) {
			ArrayList<Entity> candidate = findLongestPath(e, story);
			if (winningPath.size() < candidate.size()) {
				winningPath = candidate;
			}

		}

		return winningPath;

	}
}
