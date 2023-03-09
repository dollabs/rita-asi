package matthewFay.Depricated;

import utils.PairOfEntities;
import utils.minilisp.LList;
import frames.entities.Entity;
import frames.entities.Function;
import matchers.*;
import matchers.original.BasicMatcherOriginal;

@Deprecated
public class OneToOneMatcher extends BasicMatcherOriginal {

	private static OneToOneMatcher matcher;
	
	public static OneToOneMatcher getOneToOneMatcher () {
		if (matcher == null) {
			matcher = new OneToOneMatcher();
		}
		return matcher;
	}
	
	// Used for inheirted classes to disallow matches
	// based on criteria
	@Override
	public boolean allowMatch(LList<PairOfEntities> matches, Entity datum, Entity pattern) {
		for (Object o : matches) {
			PairOfEntities pairOfThings = (PairOfEntities) o;
			//IsEqual may not be good enough...//
			if (pairOfThings.getPattern().isEqual(pattern) || pairOfThings.getDatum().isEqual(datum))
			{
				return false;
			}
		}
		return true;
	}

	public static void main(String[] ignore) {
		Entity t1 = new Entity("foo");
		t1.addType("name");
		t1.addType("john");
		Entity t2 = new Entity("foo");
		t2.addType("name");
		t2.addType("john");
		Function d1 = new Function("d", t1);
		Function d2 = new Function("d", t2);
		OneToOneMatcher matcher = OneToOneMatcher.getOneToOneMatcher();
		System.out.println("Match: " + matcher.match(d1, d2));
		System.out.println("Match: " + matcher.match(t1, t2));
	}

}
