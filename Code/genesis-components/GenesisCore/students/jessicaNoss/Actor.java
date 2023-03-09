package jessicaNoss;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import frames.entities.Entity;
import frames.entities.Sequence;
import mentalModels.MentalModel;

/*
 * Created on Jan 15, 2015; rewritten June 22, 2015
 * @author jmn
 */

public class Actor {

	private Entity entity;
	private MentalModel mentalModel;
	private List<Integer> observedEventIDs;

	public Actor(Entity entity, MentalModel mentalModel) {
		this.entity = entity;
		this.mentalModel = mentalModel;
		observedEventIDs = new ArrayList<Integer>();
	}

	public String getName() {
		return JmnUtils.entityToText(entity);
	}

	public Entity getEntity() {
		return entity;
	}
	
	public String getStoryString() {
		Sequence story = mentalModel.getStoryProcessor().getStory();
		return JmnUtils.sequenceToString(story);
	}

	public List<Integer> getObservedEventIDs() {
		return observedEventIDs;
	}

	public Boolean didObserveEventID(Integer eventID) {
		return observedEventIDs.contains(eventID);
	}

	public void addObservation(int eventID) {
		observedEventIDs.add(eventID);
	}

	@Override
	public String toString() {
		String s = "Actor";
		s = s + " with entity " + getEntity();
		s = s + " and observed eventIDs " + observedEventIDs;
		return s;
	}
	
	public static class ActorComparator implements Comparator<Actor> {
		@Override
		public int compare(Actor actor1, Actor actor2) {
			return actor1.getName().compareTo(actor2.getName());
		}
	}
}