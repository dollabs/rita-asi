package matthewFay.VideoAnalysis;

import java.util.ArrayList;
import java.util.HashMap;

import frames.entities.Entity;
import frames.entities.Sequence;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import matthewFay.Demo;
import matthewFay.StoryAlignment.Aligner;
import matthewFay.StoryAlignment.Alignment;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.SortableAlignmentList;
import matthewFay.Utilities.EntityHelper;

/***
 * The EventPredictor uses collections of stories
 * and the alignment comparison algorithms in 
 * order to predict events in a target story
 * @author Matthew
 *
 */

public class EventPredictor {
	private Aligner aligner;
	
	public EventPredictor() {
		aligner = new Aligner();
	}
	
	public HashMap<Entity, Float> predictNextEvent(Sequence target, ArrayList<Sequence> patterns) {
		HashMap<Entity, Float> predictedEvents = new HashMap<Entity, Float>();
		SortableAlignmentList alignments = aligner.alignToPatterns(target, patterns);
		for(Alignment<Entity, Entity> alignment : alignments) {
			int end = alignment.size()-1;
			int predictionTime = -1;
			while(end > 0) {
				if(alignment.get(end).a == null && alignment.get(end).b == null) {
					end--;
				} else {
					if(alignment.get(end).a == null ) {
						predictionTime = end;
						end--;
					} else {
						break;
					}
				}
			}
			if(predictionTime >= 0) {
				LList<PairOfEntities> bindings = ((SequenceAlignment)alignment).bindings;
				Entity basis = (Entity)alignment.get(predictionTime).b.deepClone(false);
				//Mark.say("Prediction basis: " + basis.asString());
				basis = EntityHelper.findAndReplace(basis, bindings);
				predictedEvents.put(basis, alignment.score);
			}
		}
		return predictedEvents;
	}
	
	public Entity predictMostLikelyNextEvent(Sequence target, ArrayList<Sequence> patterns) {
		HashMap<Entity, Float> predictedEvents = predictNextEvent(target,patterns);
		Entity event = null;
		float score = Float.NEGATIVE_INFINITY;
		for(Entity t : predictedEvents.keySet()) {
			if(predictedEvents.get(t) > score) {
				event = t;
				score = predictedEvents.get(t);
			}
		}
		return event;
	}
	
	public static void main(String[] args) {
		Sequence seqA = Demo.GiveStartStory();
		Sequence seqB = Demo.GiveStory();
		
		Aligner aligner = new Aligner();
		SortableAlignmentList alignments = aligner.align(seqA, seqB); 
		Mark.say(alignments.get(0));
		Mark.say(((SequenceAlignment)alignments.get(0)).bindings);
		
		ArrayList<Sequence> patterns = new ArrayList<Sequence>();
		patterns.add(seqB);
		
		EventPredictor predictor = new EventPredictor();
		for(Entity event : predictor.predictNextEvent(seqA, patterns).keySet()) {
			Mark.say("Prediction: "+event.asString());
		}
	}
}
