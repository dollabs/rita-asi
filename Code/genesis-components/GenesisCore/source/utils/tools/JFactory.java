package utils.tools;

import java.util.logging.Logger;

import utils.StringUtils;

import constants.Markers;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;

/**
 * This entire class is deprecated Utility class for generating Jackendoff-style frames.
 * 
 * @author Patrick Winston
 */
public class JFactory {

	/**
	 * Creates a Jackendoff place; type argument must be one of Jackendoff's place modifiers.
	 */
	public static Function createPlace(String type, Entity thing) {
		if (!StringUtils.testType(type, Entity.placeList)) {
			System.err.println("Sorry, " + type + " is not a known place type.");
			return null;
		}
		Function result = new Function(thing);
		result.addType("place");
		result.addType(type);
		return result;
	}

	/**
	 * Creates a Jackendoff path element; type argument must be one of Jackendoff's path-element modifiers.
	 */
	public static Function createPathElement(String type, Entity thing) {
		if (!StringUtils.testType(type, Entity.pathList)) {
			System.err.println("Sorry, " + type + " is not a known path element type.");
			return null;
		}
		Function result = new Function(thing);
		result.addType("pathElement");
		result.addType(type);
		return result;
	}

	/**
	 * Creates an empty path.
	 */
	public static Sequence createPath() {
		Sequence result = new Sequence();
		result.addType("path");
		// result.addType("empty", "features");
		return result;
	}

	public static Entity createPath(String indicator, Entity reference) {
		Sequence path = createPath();
		addPathElement(path, createPathElement(indicator, reference));
		return path;
	}

	/**
	 * Adds a path element
	 */

	public static void addPathElement(Sequence path, Function pathElement) {
		path.addElement(pathElement);
	}

	/**
	 * Create a trajectory
	 */
	public static Relation createTrajectory(Entity mover, String moveType, Entity path) {
		Sequence roles = new Sequence(Markers.ROLE_MARKER);
		roles.addElement(new Function(Markers.OBJECT_MARKER, path));
		Relation r = new Relation(Markers.ACTION_MARKER, mover, roles);
		Bundle bundle = BundleGenerator.getBundle(moveType);
		r.setBundle(bundle);
		return r;
	}

}
