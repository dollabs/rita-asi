package start.portico;


import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class Combinator extends AbstractWiredBox {

	public final static String LEFT = "left";

	public final static String RIGHT = "right";

	public final static String COMBINATOR = "combinator";

	private Entity left;

	private Entity right;

	public Combinator() {
		super("Combinator");
		Connections.getPorts(this).addSignalProcessor(LEFT, "processLeft");
		Connections.getPorts(this).addSignalProcessor(RIGHT, "processRight");
		Connections.getPorts(this).addSignalProcessor(COMBINATOR, "processCombinator");
	}

	public void processLeft(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		left = (Entity) object;
	}

	public void processRight(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		right = (Entity) object;
	}

	public void processCombinator(Object object) {
		// System.out.println("Processing combinator " + object + ", " +
		// object.getClass());
		if (!(object instanceof String)) {
			return;
		}
		String combinator = (String) object;
		if (IdiomSplitter.CAUSE.equals(combinator) || IdiomSplitter.BEFORE.equals(combinator) || IdiomSplitter.WHILE.equals(combinator)
		        || IdiomSplitter.AFTER.equals(combinator)) {
			transmitRelation(combinator);
		}
		else if (IdiomSplitter.NONE.equals(combinator)) {
			Entity result = left;
			left = right = null;
			Connections.getPorts(this).transmit(embed(result));
		}
		System.out.println("Link parser transmitting");
	}

	private void transmitRelation(String relation) {
		Relation result = null;
		if (IdiomSplitter.BEFORE.equals(relation) || IdiomSplitter.WHILE.equals(relation) || IdiomSplitter.AFTER.equals(relation)) {
			result = new Relation("action", left, right);
			result.addType(Markers.TIME_TYPE);
		}
		else if (IdiomSplitter.CAUSE.equals(relation)) {
			result = new Relation("action", right, left);
		}
		left = right = null;
		if (result != null) {
			result.addType(relation);
			Connections.getPorts(this).transmit(embed(result));
		}
	}

	private Entity embed(Entity t) {
		Sequence result = new Sequence("semantic-interpretation");
		result.addElement(t);
		return result;
	}

}
