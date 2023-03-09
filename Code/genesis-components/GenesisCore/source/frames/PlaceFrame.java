package frames;

import utils.StringUtils;
// import gui.WiredPanel;
import utils.tools.JFactory;
import constants.RecognizedRepresentations;
import frames.entities.Entity;
import frames.entities.Function;

/*
 * Created on Jul 7, 2006 @author phw
 */
public class PlaceFrame extends Frame {
	public static String FRAMETYPE = (String) RecognizedRepresentations.PLACE_REPRESENTATION;

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

	private Entity reference;

	private Function place; // how all of the things are linked

	/*
	 * This constructor is used when you specify fully a place.
	 */
	public PlaceFrame(String preposition, Entity reference) {
		super();
		this.reference = reference;
		this.place = JFactory.createPlace(preposition, reference);
	}

	public PlaceFrame(Entity t) {
		super();
		if (t.isA(PlaceFrame.FRAMETYPE)) {
			this.place = this.place;
		}
	}

	public Function getPlace() {
		return this.place;
	}

	@Override
	public Entity getThing() {
		return this.getPlace();
	}
	/*
	 * @Override public WiredPanel getThingViewer() { return new PlaceViewer(); }
	 */
}
