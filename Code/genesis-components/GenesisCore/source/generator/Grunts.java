package generator;

import start.Start;
import utils.Mark;
import utils.Z;
import utils.tools.Predicates;
import constants.Markers;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

/**
 * Serves as the root of the mechanisms that provide syntactically easy was to construct Innerese structures.
 * <p>
 * Created on Mar 14, 2015
 * 
 * @author phw
 */

public class Grunts {

	/**
	 * Constructs an entity using wordnet
	 */
	public static Entity makeEntity(String word) {
		return Entity.getClassifiedThing(word);
	}

	/**
	 * Creates an entity, but then uses bundle attached to another word
	 * 
	 * @param word
	 * @return
	 */
	public static Entity makeEntity(String word, String type) {
		Entity source = makeEntity(type);
		Bundle b = (Bundle) (source.getBundle().clone());
		b.stream().forEach(t -> t.add(word));
		Entity result = Entity.getClassifiedThing(word);
		result.setBundle(b);
		return result;
	}

	/**
	 * Constructs a function using wordnet
	 */
	public static Function makeFunction(String word, Entity subject) {
		Function f = new Function("Temporary", subject);
		f.setBundle(Start.restrict(word, BundleGenerator.getBundle(word)));
		return f;
	}

	/**
	 * Constructs a relation using wordnet
	 */
	public static Relation makeRelation(String word, Entity subject, Entity object) {
		Relation r = new Relation(word, subject, object);
		
		Bundle bundle = Start.restrict(word, BundleGenerator.getBundle(word));
		
		if (bundle.size() > 0) {
			r.setBundle(bundle);
		}
		return r;
	}

	/**
	 * Constructs a sequence using wordnet
	 */
	public static Sequence makeSequence(String word) {
		Sequence s = new Sequence("Temporary");
		s.setBundle(Start.restrict(word, BundleGenerator.getBundle(word)));
		return s;
	}

	/**
	 * Negates
	 */
	public static Entity negate(Entity e) {
		if (Predicates.isCause(e)) {
			negate(e.getObject());
		}
		else {
			e.addFeature(Markers.NOT);
		}
		return e;
	}

}
