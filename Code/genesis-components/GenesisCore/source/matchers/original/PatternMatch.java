package matchers.original;

import java.util.Collection;

import frames.entities.Entity;

/*
 * Created on Jan 13, 2011
 * @author phw
 */

public class PatternMatch {
	
	public static boolean match(Entity newThing, Entity oldThing) {
		if (newThing.entityP() && oldThing.entityP() &&  matchTypesAndSign(newThing, oldThing)) {
			return newThing == oldThing;
		}
		else if (newThing.functionP() && oldThing.functionP() &&  matchTypesAndSign(newThing, oldThing)) {
			return match(newThing.getSubject(), oldThing.getSubject());
		}
		else if (newThing.relationP() && oldThing.relationP() &&  matchTypesAndSign(newThing, oldThing)) {
			return match(newThing.getSubject(), oldThing.getSubject()) && match(newThing.getObject(), oldThing.getObject());
		}
		else if (newThing.sequenceP() && oldThing.sequenceP() &&  matchTypesAndSign(newThing, oldThing)) {
			Collection<Entity> newElements = newThing.getElements();
			Collection<Entity> oldElements = newThing.getElements();
			if (newElements.size() != oldElements.size()) {
				return false;
			}
			for (Entity newElement : newElements) {
				boolean result = false;
				for (Entity oldElement : oldElements) {
					if (match(newElement, oldElement)) {
						result = true;
						break;
					}
				}
				if (!result) {
					return false;
				}

			}
			return true;
		}
		return false;
	}

	private static boolean matchTypesAndSign(Entity newThing, Entity oldThing) {
	    
	    return false;
    }

}
