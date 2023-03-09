package jessicaNoss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import frames.entities.Entity;
import utils.Mark;

/*
 * Created on Jan 21, 2015; rewritten June 22, 2015
 * @author jmn
 */
public class Observations {

	private final List<Entity> events;
	private final List<Actor> actors;
	private final Map<Entity, List<Integer>> eventToEventIDs;

	private final boolean debug = false;

	public Observations(List<Entity> events, List<Actor> actors) {
		this.events = events;
		this.actors = actors;
		this.eventToEventIDs = new HashMap<Entity, List<Integer>>();
		for (int id=0; id < events.size(); id++) {
			// This HashMap supports repeating events, although the support is
			// currently irrelevant because Genesis ignores repeating events.
			Entity event = events.get(id);
			if (eventToEventIDs.containsKey(event)) {
				List<Integer> idList = eventToEventIDs.get(event);
				idList.add(id);
				eventToEventIDs.put(event, idList);
			}
			eventToEventIDs.put(event, Arrays.asList(id));
		}
	}

	public List<Entity> getEvents() {
		return events;
	}

	public List<Actor> getActors() {
		return actors;
	}

	public Boolean didEventHappen(Entity event) {
		return (events.contains(event));
	}

	public Boolean didSubjectObserveEvent(Entity subject, Entity event) {
		try {
			Actor actor = entityToActor(subject);
			return didActorObserveEvent(actor, event);
		} catch (NonexistentActorException exception) {
			Mark.say(debug, exception);
			return false;
		}
	}

	private Actor entityToActor(Entity e) throws NonexistentActorException {
		for (Actor actor : actors) {
			if (actor.getEntity().equals(e)) {
				return actor;
			}
		}
		throw new NonexistentActorException("Entity " + e + " is not an actor in this story.");
	}

	private Boolean didActorObserveEvent(Actor actor, Entity event) {
		for (Integer eventID : getEventIDs(event)) {
			if (!eventID.equals(null) && actor.didObserveEventID(eventID)) {
				return true;
			}
		}
		return false;
	}

	private List<Integer> getEventIDs(Entity event) {
		return eventToEventIDs.get(event);
	}

	public List<String> getColumnHeadersForTable() {
		List<String> columnHeaders = new ArrayList<String>();
		columnHeaders.add("Actor Name");
		columnHeaders.addAll(JmnUtils.convertEntitiesToStrings(events));
		return columnHeaders;
	}

	public List<List<String>> getDataForTable() {
		int eventCount = events.size();
		List<List<String>> data = new ArrayList<List<String>>();
		for (Actor actor : getActors()) {
			List<String> row = new ArrayList<String>();
			row.add(actor.getName());
			row.addAll(JmnUtils.convertBooleansToStrings(
					JmnUtils.convertPresenceIDsToBooleans(actor.getObservedEventIDs(), eventCount),
					"OBSERVED", "-"));
			data.add(row);
		}
		return data;
	}

	private List<String> getActorsNames() {
		return getActors().stream().map(actor -> actor.getName()).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		String s = "Observations object with ";
		s = s + "Actors: " + getActorsNames() + " and ";
		s = s + "Events: " + events;
		return s;
	}

	@SuppressWarnings("serial")
	private class NonexistentActorException extends Exception {

		private String message;

		public NonexistentActorException(String message) {
			super(message);
			this.message = message;
		}

		@Override
		public String toString() {
			return message;
		}

		@Override
		public String getMessage() {
			return message;
		}
	}
}
