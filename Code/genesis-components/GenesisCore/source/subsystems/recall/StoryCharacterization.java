package subsystems.recall;

import java.util.TreeSet;

/*
 * Created on Jul 16, 2010
 * @author phw
 */

public class StoryCharacterization {
	TreeSet<MatchContribution> contributions;

	String name;

	public StoryCharacterization(String name, TreeSet<MatchContribution> contributions) {
		super();
		this.name = name;
		this.contributions = contributions;
	}

}
