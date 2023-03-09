package matthewFay.representations;

public class StoryGraphEdge {
	private String type;
	public String getEdgeType() {
		return type;
	}
	
	private StoryGraphNode antecedent;
	public StoryGraphNode getAntecedent() {
		return antecedent;
	}
	private StoryGraphNode consequent;
	public StoryGraphNode getConsequent() {
		return consequent;
	}
	
	public StoryGraphEdge(StoryGraphNode antecedent, StoryGraphNode consequent, String type) {
		this.antecedent = antecedent;
		this.consequent = consequent;
		this.type = type;
		antecedent.addConsequentEdge(this);
		consequent.addAntecedentEdge(this);
	}
}