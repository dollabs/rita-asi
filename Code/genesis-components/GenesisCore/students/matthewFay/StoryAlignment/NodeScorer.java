package matthewFay.StoryAlignment;

import matthewFay.Utilities.EntityHelper.MatchNode;

public interface NodeScorer {
	public float score(MatchNode node);
}
