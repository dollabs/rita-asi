package matthewFay;

import java.util.ArrayList;

import frames.entities.Sequence;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import matthewFay.StoryAlignment.MatchTree;
import matthewFay.StoryAlignment.NWSequenceAlignmentScorer;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.SortableAlignmentList;
import matthewFay.Utilities.HashMatrix;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.EntityHelper.MatchNode;

public class SequenceClusterer extends KClusterer<Sequence> {

	HashMatrix<Sequence, Sequence, Float> matrix;
	
	public SequenceClusterer(int k) {
		super(k);
		matrix = new HashMatrix<Sequence, Sequence, Float>();
	}

	@Override
	public float sim(Sequence a, Sequence b) {
		if(matrix.contains(a, b))
			return matrix.get(a, b);
		
		LList<PairOfEntities> reflectionBindings = new LList<PairOfEntities>();
		
		NWSequenceAlignmentScorer scorer =  new NWSequenceAlignmentScorer(EntityHelper.sequenceToList(a), EntityHelper.sequenceToList(b));
		
		MatchTree matchTree = new MatchTree(EntityHelper.getAllEntities(a), EntityHelper.getAllEntities(b),scorer);
		
		matchTree.primeMatchTree(reflectionBindings);
		
		matchTree.generateMatchTree();
		
		SortableAlignmentList alignments = new SortableAlignmentList();
		
		for(MatchNode leaf : matchTree.leafNodes) {
			SequenceAlignment alignment = scorer.align(leaf);
			
			alignments.add(alignment);
		}
		
		alignments.sort();
		
		
		float score = alignments.get(0).score;
		
		//Normalize scores?//
		
		matrix.put(a, b, score);
		
		return score;
	}
	
	public static void main(String[] args)
	{
		SequenceClusterer sc = new SequenceClusterer(3);
		
		Mark.say(Demo.ComplexGiveStory().asString());
		
		ArrayList<Sequence> stories = new ArrayList<Sequence>();
		stories.add(Demo.ComplexGiveStory());
		stories.add(Demo.ComplexTakeStory());
		stories.add(Demo.ExchangeStory());
		stories.add(Demo.FleeStory());
		stories.add(Demo.FollowStory());
		stories.add(Demo.MediatedExchangeStory());
		stories.add(Demo.ThrowCatchStory());
//		stories.add(Demo.ApproachStory());
//		stories.add(Demo.KickStory());
//		stories.add(Demo.CarryStory());
		
		Mark.say("Stories Loaded");
		
		ArrayList<Cluster<Sequence>> clusters = sc.cluster(stories);
		
		Mark.say("Clustering Complete");
		
		for(Cluster<Sequence> cluster : clusters) {
			Mark.say("---");
			Mark.say("Average Sim: "+cluster.averageSim);
			Mark.say("Variance: "+cluster.variance);
			Mark.say("Centroid: "+cluster.Centroid.asString());
			for(Sequence s : cluster)
			{
				Mark.say(s.asString());
			}
		}
	}
}
