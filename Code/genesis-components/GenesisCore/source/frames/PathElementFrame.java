package frames;
import constants.RecognizedRepresentations;
import frames.entities.Entity;
import frames.entities.Function;
import gui.PathElementViewer;
import utils.StringUtils;
/*
 * Created on Jul 7, 2006 @author phw
 */
public class PathElementFrame extends Frame {
	public static String	FRAMETYPE	= (String) RecognizedRepresentations.PATH_ELEMENT_THING;
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
	private Entity		reference;
	private Function	pathElement;	// how all of the things are linked
	/*
	 * This constructor is used when you specify fully a place.
	 */
	public PathElementFrame(String preposition, Entity reference) {
		super();
		this.reference = reference;
		this.pathElement = PathElementFrame.createPathElement(preposition, reference);
	}
	public PathElementFrame(Entity t) {
		super();
		if (t.isA(PathElementFrame.FRAMETYPE)) {
			this.pathElement = (Function) t;
		}
	}
	public Function getPathElement() {
		return this.pathElement;
	}
	@Override
	public Entity getThing() {
		return this.pathElement;
	}
	//@Override
/*	public gui.WiredPanel getThingViewer() {
		return new PathElementViewer();
	}*/
}
