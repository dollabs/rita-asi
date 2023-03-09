package generator;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Thread;


/*
 * Created on Mar 14, 2015
 * @author phw
 */

public class TransitionFrames extends RoleFrames {

	public static Function makeTransition(String type, Entity property) {
		Thread thread = new Thread();

		Function result = new Function(Markers.ACTION_WORD, property);
		result.addType(Markers.TRANSITION_MARKER);
		result.addType(type);
		return result;
	}

	public static Function makeAppear(Entity property) {
		return makeTransition(Markers.APPEAR_MARKER, property);
	}

	public static Function makeDisappear(Entity property) {
		return makeTransition(Markers.DISAPPEAR_MARKER, property);
	}

	public static Function makeIncrease(Entity property) {
		return makeTransition(Markers.INCREASE, property);
	}

	public static Function makeDecrease(Entity property) {
		return makeTransition(Markers.DECREASE, property);
	}

	public static Function makeChange(Entity property) {
		return makeTransition(Markers.CHANGE, property);
	}

	public static Function makeNotAppear(Entity property) {
		Function result = makeTransition(Markers.APPEAR_MARKER, property);
		negate(result);
		return result;
	}

	public static Function makeNotDisappear(Entity property) {
		Function result = makeTransition(Markers.DISAPPEAR_MARKER, property);
		negate(result);
		return result;
	}

	public static Function makeNotIncrease(Entity property) {
		Function result = makeTransition(Markers.INCREASE, property);
		negate(result);
		return result;
	}

	public static Function makeNotDecrease(Entity property) {
		Function result = makeTransition(Markers.DECREASE, property);
		negate(result);
		return result;
	}

	public static Function makeNotChange(Entity property) {
		Function result = makeTransition(Markers.CHANGE, property);
		negate(result);
		return result;
	}

}
