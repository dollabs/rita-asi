package matthewFay.Depricated;

import java.util.ArrayList;

import frames.entities.Entity;
import frames.entities.Sequence;
import matchers.StandardMatcher;
import utils.PairOfEntities;
import utils.minilisp.LList;
@Deprecated
public class SequenceSanitizer {
	/**
	 * Compares two things and returns if they are equal
	 * @param t1 - Thing 1 to compare
	 * @param t2 - Thing 2 to compare
	 * @return - Returns true if the things are equal false otherwise
	 */
	public static boolean compareThings(Entity t1, Entity t2) {
		StandardMatcher matcher = StandardMatcher.getBasicMatcher();
		//Mark.say("Comparing: ", t1.asString(), t2.asString());
		LList<PairOfEntities> t1t2 = matcher.match(t1, t2);
		//Mark.say("t1t2:", t1t2);
		LList<PairOfEntities> t2t1 = matcher.match(t2, t1);
		//Mark.say("t1t2:", t2t1);
		if(t1t2 == null || t2t1 == null)
			return false;
		if(t1t2.size() != t1t2.size())
			return false;
		if(t1t2.toString().equals(t2t1.toString()))
			return true;
		return false;
	}
	
	private static Sequence stripMilestones(Sequence s) {
		for(int i=0; i<s.getNumberOfChildren();i++) {
			if(s.getElement(i).functionP("milestone")) {
				s.removeElement(s.getElement(i));
				i--;
			}
		}
		return s;
	}
	
	private static boolean compareThings(ArrayList<Entity> l1, ArrayList<Entity> l2) {
		boolean allMatched = true;
		for(int i=0;i<l1.size()&&allMatched;i++) {
			allMatched = compareThings(l1.get(i), l2.get(i));
		}
		return allMatched;
	}
	
	private static Sequence sanitize(int pass, Sequence s) {
		if(s.getNumberOfChildren() < pass*2)
			return s;
		
		ArrayList<Entity> setOne = new ArrayList<Entity>();
		ArrayList<Entity> setTwo = new ArrayList<Entity>();
		
		for(int i=0;i<=s.getNumberOfChildren()-pass*2;i++) {
			setOne.clear();
			setTwo.clear();
			for(int iAdder=0;iAdder<pass;iAdder++) {
				setOne.add(s.getElement(i+iAdder));
			}
			for(int jAdder=0;jAdder<pass;jAdder++) {
				setTwo.add(s.getElement(i+pass+jAdder));
			}
			if(compareThings(setOne, setTwo)) {
				for(Entity t : setTwo) {
					s.removeElement(t);
				}
				i--;
			}
		}
		
		return s;
	}
	
	public static Sequence sanitize(Sequence s, int numberOfPasses) {
		Sequence sanitized = (Sequence) s.deepClone();
		sanitized = stripMilestones(sanitized);
		for(int pass=1;pass<=numberOfPasses;pass++) {
			sanitized = sanitize(pass, sanitized);
		}
		return sanitized;
	}
	
	public static Sequence sanitize(Sequence s) {
		return sanitize(s, 2);
	}
}
