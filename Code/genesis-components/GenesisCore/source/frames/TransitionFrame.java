package frames;
import constants.RecognizedRepresentations;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import utils.StringUtils;
/*
 * Created on Jul 14, 2006 @author phw
 */
public class TransitionFrame extends Frame {
	public static String FRAMETYPE = (String) RecognizedRepresentations.TRANSITION_REPRESENTATION;
	public static String[] changeList = { "increase", "decrease", "change",
        "appear", "disappear", "notIncrease", "notDecrease", "notChange",
        "notAppear", "notDisappear", "blank" };
	
	public static Function createVariableElement(String type, Entity thing) {
		Function result = new Function(thing);
		result.addType(type);
		return result;
	}
	/**
	 * Creates a Borchardt place; type argument must be AT.
	 */
	public static Function createPlaceElement (Entity thing) {
		Function result = new Function (thing);
		result.addType("placeElement"); result.addType("at");
		return result;
	}
	
	
	/**
	 * Creates a BETWEEN path.
	 */
	public static Sequence createPath () {
		Sequence result = new Sequence();
		result.addType("path");
		result.addType("between");
		// result.addFeature("empty"");
		return result;
	}
	
	/**
	 * Extends a path. Warns if types wrong.
	 */
	public static Sequence extendPath(Sequence path, Function placeElement) {
		Sequence result = path;
		if (!placeElement.isA("placeElement")) {System.err.println("Sorry, " + placeElement + " is not a place element."); return null;}
		if (!path.isA("path")) {System.err.println("Sorry " + path + " is not a path."); return null;}
		return extend(result, placeElement);
	}
	/**
	 * Creates a Borchardt transition element; type argument must be one of
	 * Borchardt's basic ten.
	 */
	public static Function createTransitionElement (String type, Entity thing) {
		if (!StringUtils.testType(type, TransitionFrame.changeList)){System.err.println("Sorry, " + type + " is not a known transition element type."); return null;}
		Function result = new Function(thing);
		result.addType(TransitionFrame.FRAMETYPE);
		result.addType(type);
		return result;
	}
	
	/**
	 * Creates an empty transition ladder.
	 */
	public static Sequence createTransitionLadder () {
		Sequence result = new Sequence();
		result.addType("transitionLadder");
		// result.addFeature("empty");
		return result;
	}
	
	/**
	 * Extends a transitionLadder. Warns if types wrong.
	 */
	public static Sequence extendTransitionLadder(Sequence transitionLadder, Function transition) {
		Sequence result = transitionLadder;
		if (!transition.isA("transitionElement")) {System.err.println("Sorry, " + transition + " is not a transition element."); return null;}
		if (!transitionLadder.isA("transitionLadder")) {System.err.println("Sorry " + transitionLadder + " is not a transitionLadder."); return null;}
		return extend(result, transition);
	}
	
	/**
	 * Creates an empty transitionSpace.
	 */
	public static Sequence createTransitionSpace () {
		Sequence result = new Sequence();
		result.addType("transitionSpace");
		// result.addFeature("empty");
		return result;
	}
	
	/**
	 * Extends a transitionSpace. Warns if types wrong.
	 */
	public static Sequence extendTransitionSpace(Sequence transitionSpace, Sequence transitionLadder) {
		Sequence result = transitionSpace;
		if (!transitionLadder.isA("transitionLadder")) {
			System.err.println("Sorry, " + transitionLadder + " is not a transitionLadder element."); 
			System.err.println("Cannot add to " + transitionSpace); 
			return null;
		}
		if (!transitionSpace.isA("transitionSpace")) {System.err.println("Sorry " + transitionSpace + " is not a transitionSpace."); return null;}
		return extend(result, transitionLadder);
	}
	
	// This section for private helpers
	private static Sequence extend (Sequence result, Entity thing) {
		result.addElement(thing);
		result.removeType("empty");
		return result;
	}
	//end static methods.
	
	private Function transition = null;
	
	public TransitionFrame(Entity t) {
		if (t.isA(TransitionFrame.FRAMETYPE)) {
			this.transition = (Function) t;
		}
	}
	public TransitionFrame(Entity transitioner, String type) {
		transition = TransitionFrame.createTransitionElement(type, transitioner);
	}
    
	private String mapChange (String word) {
		if ("increased".equalsIgnoreCase(word)) {return "increase";}
		else if ("decreased".equalsIgnoreCase(word)) {return "decrease";}
		else if ("changed".equalsIgnoreCase(word)) {return "change";}
		else if ("appeared".equalsIgnoreCase(word)) {return "appear";}
		else if ("disappeared".equalsIgnoreCase(word)) {return "disappear";}
		return word;
	}
 
	public TransitionFrame(TransitionFrame t) {
		this.transition = (Function) t.getTransition().clone();
	}
	public String toString() {
		if (transition != null) {
			return transition.toString();
		}
		return "";
	}
	public Function getTransition() {
		return this.transition;
	}
	
	public Entity getThing() {
		return getTransition();
	}
}
	
