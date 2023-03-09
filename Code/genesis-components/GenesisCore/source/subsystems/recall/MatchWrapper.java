package subsystems.recall;

import java.util.TreeSet;

import frames.entities.Sequence;

/*
 * Created on Jul 15, 2010
 * @author phw
 */

public class MatchWrapper implements Comparable {

	private double similarity;

	private StoryVectorWrapper probe;

	private StoryVectorWrapper precedent;

	private TreeSet<MatchContribution> contributions;

	public MatchWrapper(double s, TreeSet<MatchContribution> contributions, StoryVectorWrapper p, StoryVectorWrapper c) {
		similarity = s;
		probe = p;
		precedent = c;
		this.contributions = contributions;
	}

	public String toString() {
		return "<" + similarity + "\n" + contributions + "\n" + precedent.toString() + "\n>";
	}

	public double getSimilarity() {
		return similarity;
	}

	public StoryVectorWrapper getProbe() {
		return probe;
	}

	public StoryVectorWrapper getPrecedent() {
		return precedent;
	}

	public Sequence getStory() {
		return getProbe().getStory();
	}

	public TreeSet<MatchContribution> getContributions() {
		return contributions;
	}

	public int compareTo(Object p) {
		MatchWrapper that = (MatchWrapper) p;
		if (this.similarity < that.similarity) {
			return 1;
		}
		else if (this.similarity > that.similarity) {
			return -1;
		}
		else
			return this.getPrecedent().getTitle().compareTo(that.getPrecedent().getTitle());
	}
}
