package expert;

import utils.Mark;

import connections.*;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class CauseExpert extends AbstractWiredBox {

	public static final String FROM_STORY_PORT = "from story port";

	public static final String RULE = "rule";

	public static final String RULES = "rules";

	public static final String CLEAR = "clear memory";

	private Sequence rules = new Sequence();

	public CauseExpert() {
		super("Cause expert");
		Connections.getPorts(this).addSignalProcessor(this::processFromParser);
		Connections.getPorts(this).addSignalProcessor(FROM_STORY_PORT, this::processFromStory);
		Connections.getPorts(this).addSignalProcessor(CLEAR, this::clearRuleMemory);
	}

	public void processFromParser(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		processCausalRelation(t);
		if (!Switch.showBackgroundElements.isSelected()) {
			sendDownstream(t);
		}
	}

	public void processFromStory(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		processCausalRelation(t);
		sendDownstream(t);
	}

	private boolean processCausalRelation(Entity t) {
		if (t.isAPrimed(Markers.CAUSE_MARKER) && t instanceof Relation) {
			Relation r = (Relation) t;
			record(r);
			return true;
		}
		return false;
	}

	public void sendDownstream(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity t = (Entity) object;
		if (t.isAPrimed(Markers.CAUSE_MARKER) && t instanceof Relation) {
			Relation r = (Relation) t;
			Entity subject = r.getSubject();
			if (subject.sequenceP() && subject.isAPrimed(Markers.CONJUNCTION)) {
				for (Entity element : ((Sequence) subject).getElements()) {
					Connections.getPorts(this).transmit(Markers.LOOP, element);
				}
			}
			Connections.getPorts(this).transmit(Markers.LOOP, r.getObject());
			Connections.getPorts(this).transmit(Markers.VIEWER, t);
		}
		else {
			// Mark.say("Transmitting from cause expert", t);
			Connections.getPorts(this).transmit(Markers.NEXT, t);
		}
	}

	public void record(Relation rule) {
		Connections.getPorts(this).transmit(RULE, rule);
		rules.addElement(rule);
		Connections.getPorts(this).transmit(RULES, rules);
	}

	public void clearRuleMemory(Object o) {
		clearRuleMemory();
	}

	public void clearRuleMemory() {
		rules.clearElements();
	}

}
