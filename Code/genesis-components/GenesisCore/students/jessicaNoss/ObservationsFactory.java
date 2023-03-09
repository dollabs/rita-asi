package jessicaNoss;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import frames.entities.Entity;
import mentalModels.MentalModel;
import storyProcessor.StoryProcessor;
import utils.Mark;

/*
 * Created on June 22, 2015; refactored July 27, 2015; improved June 2016
 * @author jmn
 */
public class ObservationsFactory { //todo rename to something like ActorContainer or ObservationContainer

	private final boolean debug = false;
	
	private StoryProcessor storyProcessor;

	private List<Entity> allEvents;
	private Set<Actor> allActors;
	private Set<Entity> actorEntitiesCreated;
	private Set<Entity> actorEntitiesPresentInScene;
	private Set<Entity> actorEntitiesObservingInScene; //subset of actorEntitiesPresentInScene

	public ObservationsFactory(StoryProcessor storyProcessor) {
		this.storyProcessor = storyProcessor;
		allEvents = new ArrayList<Entity>();
		allActors = new HashSet<Actor>();
		actorEntitiesCreated = new HashSet<Entity>();
		actorEntitiesPresentInScene = new HashSet<Entity>();
		actorEntitiesObservingInScene = new HashSet<Entity>();
	}

	public void processEntry(Entity subject){
		processAwaken(subject);
		actorEntitiesPresentInScene.add(subject);
		if (!actorEntitiesCreated.contains(subject)) {
			MentalModel mentalModel = storyProcessor.getMentalModel().getLocalMentalModel(subject.getName());
			allActors.add(new Actor(subject, mentalModel));
			actorEntitiesCreated.add(subject);
		}
		Mark.say(debug, "Added to scene and opened mental model:", subject);
	}

	public void processAwaken(Entity subject) {
		storyProcessor.openMentalModel(subject);
		actorEntitiesObservingInScene.add(subject);
	}

	public void processExit(Entity subject) {
		// special case for "Everyone exits"
		if ("everyone".equals(subject.getProperName()) && getActorByName("everyone") == null) {
			// Note (6/3/2016): This magically started working again with no changes.
			// But if subject.getProperName() starts returning null again, try: "Everyone".equals(subject.toEnglish())
			processExitEveryone();
			return;
		}
		
		processLoseConsciousness(subject);
		actorEntitiesPresentInScene.remove(subject);
		Mark.say(debug, "Removed from scene and closed mental model:", subject);
	}
	
	public void processExitEveryone() {
		Set<Entity> actorsToRemove = new HashSet<Entity>(actorEntitiesPresentInScene);
		for (Entity actor : actorsToRemove) {
			processExit(actor);
		}
	}

	public void processLoseConsciousness(Entity subject) {
		storyProcessor.closeMentalModel(subject);
		actorEntitiesObservingInScene.remove(subject);
	}

	public void addEvent(Entity increment) {
		allEvents.add(increment);
		int eventID = allEvents.size() - 1;
		recordObservationOfEvent(eventID, allActors, actorEntitiesObservingInScene);
		Mark.say(debug, "Event:", increment);
	}

	public Observations getObservations() {
		return new Observations(allEvents, hashsetToAlphabetizedList(allActors));
	}
	
	public Set<Actor> getAllActors() {
		return allActors;
	}
	
	public boolean isPresentInScene(Entity character) {
		return actorEntitiesPresentInScene.contains(character);
	}

	public boolean isObservingInScene(Entity character) { //todo rm if not used
		return actorEntitiesObservingInScene.contains(character);
	}

//	private boolean isEntry(Entity e) { //TODO remove commented code
//		return e.isA("enter") && RoleFrames.getObject(e).isA("scene");
//	}
//
//	private boolean isExit(Entity e) {
//		return e.isA("exit") && RoleFrames.getObject(e).isA("scene");
//	}

	private void recordObservationOfEvent(Integer eventID, Set<Actor> allActors, Set<Entity> actorsAwakeInScene) {
		for (Actor currentActor : allActors) {
			boolean didActorObserveEvent = actorsAwakeInScene.contains(currentActor.getEntity());
			if (didActorObserveEvent) {
				currentActor.addObservation(eventID);
			}
		}
	}

	private List<Actor> hashsetToAlphabetizedList(Set<Actor> actorSet) {
		List<Actor> actorList = new ArrayList<Actor>(actorSet);
		Collections.sort(actorList, new Actor.ActorComparator());
		return actorList;
	}

	public Actor getActorByName(String name) {
		// TODO make this more efficient; maybe compare Entitys instead of Strings?
		for (Actor actor : allActors) {
			if (actor.getName().equalsIgnoreCase(name)) {
				return actor;
			}
		}
		return null; //TODO don't return null
	}
}

//Version that uses entire story at once instead of increments:
/*
public class ObservationsFactoryNonincremental {

	private Sequence story;

	private final boolean debug = false;

	public ObservationsFactory(Sequence story) {
		this.story = story;
	}

	public Observations getObservations() {
		List<Entity> allEvents = new ArrayList<Entity>();
		Set<Actor> allActors = new HashSet<Actor>();
		Set<Entity> actorsCreated = new HashSet<Entity>();
		Set<Entity> actorsInScene = new HashSet<Entity>();
		Vector<Entity> elements = story.getElements();
		boolean afterFirstEntrance = false;
		for (Entity event : elements) {
			Entity subject = event.getSubject();
			if (isEntry(event)) {
				afterFirstEntrance = true;
				actorsInScene.add(subject);
				if (!actorsCreated.contains(subject)) {
					allActors.add(new Actor(subject));
					actorsCreated.add(subject);
				}
				Mark.say(debug, "Noted entry:", subject);
			}
			else if (isExit(event)) {
				actorsInScene.remove(subject);
				Mark.say(debug, "Noted exit:", subject);
			}
			else if (afterFirstEntrance) { //because we don't want to count events such as "Macbeth is a person"
				allEvents.add(event);
				int eventID = allEvents.size() - 1;
				recordObservationOfEvent(eventID, allActors, actorsInScene);
				Mark.say(debug, "Event:", event);
			}
		}
		return new Observations(allEvents, hashsetToAlphabetizedList(allActors));
	}

	private boolean isEntry(Entity e) {
		return e.isA("enter") && RoleFrames.getObject(e).isA("scene");
	}

	private boolean isExit(Entity e) {
		return e.isA("exit") && RoleFrames.getObject(e).isA("scene");
	}

	private void recordObservationOfEvent(Integer eventID, Set<Actor> allActors, Set<Entity> actorsInScene) {
		for (Actor currentActor : allActors) {
			boolean didActorObserveEvent = actorsInScene.contains(currentActor.getEntity());
			if (didActorObserveEvent) {
				currentActor.addObservation(eventID);
			}
		}
	}

	private List<Actor> hashsetToAlphabetizedList(Set<Actor> actorSet) {
		List<Actor> actorList = new ArrayList<Actor>(actorSet);
		Collections.sort(actorList, new Actor.ActorComparator());
		return actorList;
	}
}
*/
