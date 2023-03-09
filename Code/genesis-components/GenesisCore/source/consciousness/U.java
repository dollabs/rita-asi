package consciousness;


import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import utils.PairOfEntities;
import utils.minilisp.LList;

/*
 * Created on Feb 15, 2016
 * @author phw
 */

public class U {

	public static Entity substitute(Entity rememberedConsequence, LList<PairOfEntities> bindings) {
		if (rememberedConsequence.entityP()) {
			Entity match = getReverseMatch(rememberedConsequence, bindings);
			if (match != null) {
				return match;
			}
			return rememberedConsequence;
		}
		else if (rememberedConsequence.functionP()) {
			return new Function(rememberedConsequence.getBundle().copy(), substitute(((Function) rememberedConsequence).getSubject(), bindings));
		}
		else if (rememberedConsequence.relationP()) {
			return new Relation(rememberedConsequence.getBundle().copy(), substitute(((Relation) rememberedConsequence).getSubject(), bindings),
			        substitute(((Relation) rememberedConsequence).getObject(), bindings));
		}
		// Now the hard part, sequences
		// Assumes only one element in sequence of each type
		else if (rememberedConsequence.sequenceP()) {
			Sequence result = new Sequence(rememberedConsequence.getBundle().copy());
			for (Entity element : ((Sequence) rememberedConsequence).getElements()) {
				result.addElement(substitute(element, bindings));
			}
			return result;
		}
		return null;
	}

	/*
	 * Should be identity match, rather than type match, but memory does not use same objects!
	 */
	private static Entity getReverseMatch(Entity thing, LList<PairOfEntities> matches) {
		for (Object object : matches) {
			PairOfEntities pairOfThings = (PairOfEntities) object;
			// if (pairOfThings.getPattern().getType().equals(thing.getType())) {
			// return pairOfThings.getDatum();
			// }
			if (pairOfThings.getPattern().getType().equals(thing.getType())) {
				return pairOfThings.getDatum();
			}
		}
		return null;
	}

}
