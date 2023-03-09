package jakeBarnwell.concept;

import java.util.ArrayList;
import java.util.List;

import jakeBarnwell.Tools;

/**
 * An immutable sequence of tree transformations
 * @author jb16
 *
 */
public class TransformationPath {
	private List<Transformation> transformationSequence = new ArrayList<>();
	private List<ConceptTree> treeSequence = new ArrayList<>();
	private double cost = 0;
	private int numTransformations = 0;
	private int numMergeExpand = 0;
	
	public TransformationPath(ConceptTree newNode) {
		treeSequence.add(newNode);
	}
	
	/**
	 * Registers an operation to a new tree, and returns the new
	 * mutated object.
	 * @param op the operator of the transformation
	 * @param directions the directions to get to the node upon which 
	 * the transformation takes place
	 * @param newNode the new tree attained by applying the transformation
	 * @return the new, mutated path
	 */
	public TransformationPath register(EntityOperator op, NodeLocation directions, ConceptTree newNode) {
		treeSequence.add(newNode);
		registerTransformation(new Transformation(op, directions, ((ConceptTree)newNode.follow(directions)).getProperties()));
		return this;
	}
	
	public double getCost() {
		return cost;
	}
	
	public int getNumTransformations() {
		return numTransformations;
	}
	
	public int getNumMergeExpand() {
		return numMergeExpand;
	}
	
	public Transformation getTransformation(int i) {
		return transformationSequence.get(i);
	}
	
	public ConceptTree getTreeBeforeTransformation(int i) {
		return treeSequence.get(i);
	}
	
	public ConceptTree getTreeAfterTransformation(int i) {
		return treeSequence.get(i + 1);
	}
	
	public Transformation latestTransformation() {
		return transformationSequence.get(transformationSequence.size() - 1);
	}
	
	public ConceptTree latestTree() {
		return treeSequence.get(treeSequence.size() - 1);
	}
	
	private void registerTransformation(Transformation opn) {
		transformationSequence.add(opn);
		cost += opn.type.getCost();
		numTransformations++;
		if(opn.type == EntityOperator.EXPAND_SEQ 
				|| opn.type == EntityOperator.MERGE_SEQ) {
			numMergeExpand++;
		}
	}
	
	public TransformationPath copy() {
		ConceptTree first = treeSequence.get(0);
		TransformationPath copyOpSeq = new TransformationPath(first.copy());
		for(int i = 0; i < transformationSequence.size(); i++) {
			ConceptTree etn = treeSequence.get(i);
			Transformation opn = transformationSequence.get(i);
			copyOpSeq.register(opn.type, opn.location.copy(), etn.copy());
		}
		
		return copyOpSeq;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof TransformationPath)) {
			return false;
		}
		
		TransformationPath o = (TransformationPath)other;
		return transformationSequence.equals(o.transformationSequence)
				&& treeSequence.equals(o.treeSequence);
	}
	
	@Override
	public int hashCode() {
		return 41 * Tools.listHashCode(transformationSequence) + Tools.listHashCode(treeSequence);
	}
	
	@Override
	public String toString() {
		return String.format("{%s[%d] ...%s>%s}",
				this.getClass().getSimpleName(),
				treeSequence.size(),
				latestTransformation().toString(),
				latestTree().toString());
	}
}