package genesis;

import utils.Mark;

import constants.Markers;
import frames.CauseFrame;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Jan 9, 2008 @author phw
 */

public class Quantum {

	Entity thing;

	boolean truth;

	public Entity getThing() {
		return thing;
	}

	public boolean isTruth() {
		return truth;
	}

	public Quantum(Entity statement, Entity question, boolean truth) {
		super();
			Sequence conjunction = new Sequence(Markers.CONJUNCTION);
			conjunction.addElement(statement.getSubject());
			this.thing = new Relation("action", conjunction, question);
			this.thing.addType(CauseFrame.FRAMETYPE);
		if (!truth) {
			question.addFeature(Markers.NOT);
		}
		this.truth = truth;
		Mark.say("Learned quantum", truth);
	}

}
