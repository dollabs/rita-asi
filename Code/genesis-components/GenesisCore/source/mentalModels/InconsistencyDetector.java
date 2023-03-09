package mentalModels;

import java.util.*;

import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.RoleFrames;
import start.StartPreprocessor;
import utils.Mark;

/*
 * Created on Apr 14, 2016
 * @author phw
 */

public class InconsistencyDetector extends AbstractWiredBox {

	private static InconsistencyDetector inconsistencyDetector;

	public static InconsistencyDetector getInconsistencyDetector() {
		if (inconsistencyDetector == null) {
			inconsistencyDetector = new InconsistencyDetector("Inconsistency detector");
		}
		return inconsistencyDetector;
	}

	private static HashMap<String, List<String>> memory;

	public List<EntityPair> findInconsistencies(Sequence story) {
		List<EntityPair> result = new ArrayList<>();
		List<Entity> candidates = new ArrayList<>();
		// First gather up mental state changes
		for (Entity element : story.getElements()) {
			if (element.isA(Markers.APPEAR_MARKER)) {
				candidates.add(element);
				
			}
		}
		// For each mental state change
		for (int i = 0; i < candidates.size(); ++i) {
			Entity e1 = candidates.get(i);
			// See if there is one that is incompatible
			for (int j = i + 1; j < candidates.size(); ++j) {
				Entity e2 = candidates.get(j);
				if (e1.getSubject().getSubject() == e2.getSubject().getSubject()) {
					Entity o1 = RoleFrames.getObject(e1.getSubject());
					Entity o2 = RoleFrames.getObject(e2.getSubject());
					String s1 = o1.getType();
					String s2 = o2.getType();
					List<String> opposites = getMemory().get(s1);
					//Mark.say(s1, s2, opposites);
					//Mark.say("Checking", s1, s2);
					if (opposites != null && opposites.contains(s2)) {
						//Mark.say("Bingo\n", s1, "\n", s2);
						//Mark.say(e1.toEnglish());
						//Mark.say(e2.toEnglish());
						result.add(new EntityPair(e1, e2));
					}
				}
			}
		}
		
		
		return result;
	}
	
	// added by Sayeri
	public List<EntityPair> findInconsistentProperties(Sequence story) {
		List<EntityPair> result = new ArrayList<>();
		List<Entity> candidates = new ArrayList<>();
		// First gather up properties
		for (Entity element: story.getElements()) {
			if (element.isA(Markers.PROPERTY_TYPE)) {
				candidates.add(element);
				//Mark.say(element);
			}
		}
		
		
		
		// For each property change
		for (int i = 0; i < candidates.size(); ++i) {
			Entity e1 = candidates.get(i);
			// See if there is one that is incompatible
			for (int j = i + 1; j < candidates.size(); ++j) {
				Entity e2 = candidates.get(j);
				if (e1.getSubject() == e2.getSubject()) {
					Entity o1 = RoleFrames.getObject(e1);
					Entity o2 = RoleFrames.getObject(e2);
					
					String s1 = o1.getType();
					String s2 = o2.getType();
					List<String> opposites = getMemory().get(s1);
					//Mark.say(s1, s2, opposites);
					//Mark.say("Checking", s1, s2);
					if (opposites != null && opposites.contains(s2)) {
						//Mark.say("Bingo\n", s1, "\n", s2);
						//Mark.say(e1.toEnglish());
						//Mark.say(e2.toEnglish());
						result.add(new EntityPair(e1, e2));
					}
				}
			}
		}
		
		
		return result;
	}

	private InconsistencyDetector(String name) {
		super(name);
		Connections.getPorts(this).addSignalProcessor(StartPreprocessor.INCONSISTENCY, this::addOpposites);
	}

	public static HashMap<String, List<String>> getMemory() {
		if (memory == null) {
			memory = new HashMap<>();
		}
		return memory;
	}

	public void addOpposites(Object o) {
		if (o instanceof BetterSignal) {
			BetterSignal bs = (BetterSignal)o;
			if (bs.get(0, String.class)== Markers.OPPOSITES) {
				String x = bs.get(1, String.class);
				String y = bs.get(2, String.class);
				// Mark.say("Hello incompatibles", x, y);
				add(x, y);
				add(y, x);
			}
			
		}
	}

	private void add(String a, String b) {
		List<String> list = getMemory().get(b);
		if (list == null) {
			list = new ArrayList<String>();
			getMemory().put(b, list);
		}
		list.add(a);
	}

}
