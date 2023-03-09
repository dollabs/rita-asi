package matthewFay.StoryAlignment;

import java.util.ArrayList;
import java.util.List;

import frames.entities.Entity;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import matchers.BindingValidator;
import matchers.EntityMatcher;
import matchers.representations.EntityMatchResult;
import matthewFay.ScoreMatcher;
import matthewFay.Utilities.EntityHelper.MatchNode;
import matthewFay.Utilities.HashMatrix;
import matthewFay.viewers.AlignmentViewer;

public class NWSequenceAlignmentScorer extends NWAligner<Entity, Entity> implements NodeScorer {

	/*
	//Alignment Matrix Scoring Metrics
	public static float matchReward = 1f;
	public static float gapFactor = 0.8f;
	public static float mismatchPenalty = -0.5f;
	//public final static float gapPenaltyFactor = -0.2f;
	public final static float gapPenaltyFactor = -0.1f;
	boolean debugCosts = false;
	*/
	
	//Alignment Matrix Scoring Metrics
	public static float matchReward = 1f;
	public static float gapFactor = 0.0f;
	public static float mismatchPenalty = -2f;
	//public final static float gapPenaltyFactor = -0.2f;
	public static float gapPenaltyFactor = -1.0f;
	boolean debugCosts = false;
	
	public static void usePenalizedScoring() {
		matchReward = 1f;
		gapFactor = 0.8f;
		mismatchPenalty = -0.5f;
		gapPenaltyFactor = -0.1f;
		NWSequenceAlignmentScorer.usePenalizedScoring();

	}
	
	public static void useSimpleScoring() {
		matchReward = 1f;
		gapFactor = 0.0f;
		mismatchPenalty = -2f;
		gapPenaltyFactor = -1f;
		NWSequenceAlignmentScorer.useSimpleScoring();

	}
	
	private LList<PairOfEntities> bindings;
	
	ScoreMatcher scoreMatcher = new ScoreMatcher(); 
	EntityMatcher em = new EntityMatcher();
	BindingValidator bv = new BindingValidator();
	
	//These are the stories being aligned against in List<Thing> form rather than Sequences
	List<Entity> A;
	List<Entity> B;
	
	public NWSequenceAlignmentScorer(List<Entity> A, List<Entity> B) {
		setGapPenalty(gapPenaltyFactor*matchReward);
		this.A = A;
		this.B = B;
	}
	
	
	
	@Override
	public float score(MatchNode node) {
		SequenceAlignment alignment = this.align(node);
		node.alignment = alignment;
		return alignment.score;
	}
	
	public SequenceAlignment align(List<PairOfEntities> bindings) {
		// PHW changed to false to get diagnosis to work.
		ScoreMatcher.useBindingHashes = false;
		ScoreMatcher.bindingHashMatrix.clear();
		
		if(ScoreMatcher.useBindingHashes) {
			for(PairOfEntities pair : bindings) {
				ScoreMatcher.bindingHashMatrix.put(pair.getPattern().getNameSuffix(), pair.getDatum().getNameSuffix(), true);
				//ScoreMatcher.bindingHashMap.put(pair.getPattern().getNameSuffix(), pair.getDatum().getNameSuffix());
			}
		}
		
		Alignment<Entity, Entity> alignment = align(A, B);
		SequenceAlignment seqAlignment = new SequenceAlignment(alignment);
		seqAlignment.bindings = new LList<>();
		for(PairOfEntities binding : bindings) {
			seqAlignment.bindings = seqAlignment.bindings.cons(binding);
		}
		seqAlignment.aName = "Story A";
		seqAlignment.bName = "Story B";
		
		
		ScoreMatcher.bindingHashMatrix.clear();
		ScoreMatcher.useBindingHashes = false;
		
		return seqAlignment;
	}
	
	public SequenceAlignment align(MatchNode node) {
		bindings = node.bindingSet;
//		score_cache.clear();
		// PHW changed to false to get diagnosis to work.
		ScoreMatcher.useBindingHashes = false;
		ScoreMatcher.bindingHashMatrix.clear();
		
		if(ScoreMatcher.useBindingHashes) {
			for(PairOfEntities pair : node.bindingSet) {
				ScoreMatcher.bindingHashMatrix.put(pair.getPattern().getNameSuffix(), pair.getDatum().getNameSuffix(), true);
				//ScoreMatcher.bindingHashMap.put(pair.getPattern().getNameSuffix(), pair.getDatum().getNameSuffix());
			}
		}
		
		Alignment<Entity, Entity> alignment = align(A, B);
		SequenceAlignment seqAlignment = new SequenceAlignment(alignment);
		seqAlignment.bindings= bindings;
		seqAlignment.aName = "Story A";
		seqAlignment.bName = "Story B";
		
		
		ScoreMatcher.bindingHashMatrix.clear();
		ScoreMatcher.useBindingHashes = false;
		
		return seqAlignment;
	}

//	private HashMatrix<Entity, Entity, Float> score_cache = new HashMatrix<>();
	
	@Override
	public float sim(Entity pattern, Entity datum) {	
//		if(datum.getType().equals("appear") &&
//			  datum.functionP() &&
//			  datum.getSubject().isA("gap")) {
//			return (matchReward*gapFactor);
//		}
		
//		if(score_cache.contains(pattern, datum)) {
//			float old_score = score_cache.get(pattern, datum);
//			return old_score;
//		}
		
		float score = matchReward*scoreMatcher.scoreMatch(pattern, datum, bindings);
		
		if(score > 0)
			score = 1;
		
//		em.useScoreMatching();
//		bv.setPatternMatch(false);
//		EntityMatchResult emr = em.match(pattern, datum);
//		emr.bindings = bv.validateBindings(emr.bindings, bindings);
//		double scored = 0;
//		if(emr.toLList() != null)
//			scored = matchReward*emr.score;
//		if(scored > 0)
//			scored = 1;
//		score = (float)scored;
		
		if(score == 0) {
			score = matchReward*mismatchPenalty;
		}
		
//		score_cache.put(pattern, datum, score);
		return (float)score;
	}

}
