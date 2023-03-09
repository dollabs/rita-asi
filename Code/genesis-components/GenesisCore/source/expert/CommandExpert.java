package expert;

import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import generator.RoleFrames;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class CommandExpert extends AbstractWiredBox {

	public static final String IMAGINE = "imagine";

	public static final String PERSUADE = "persuade";

	public static final String TELL = "tell";

	public CommandExpert() {
		super("Command expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (!(object instanceof Entity)) {
			return;
		}
		Entity entity = (Entity) object;
		if (entity.functionP(Markers.IMAGINE)) {
			Function d = (Function) entity;
			Connections.getPorts(this).transmit(Markers.VIEWER, d);
			// Mark.say("Transmitting derivative", d.asString());
			Connections.getPorts(this).transmit(IMAGINE, d);
		}
		else if (entity.relationP(Markers.IMAGINE)) {
			Relation r = (Relation) entity;
			Connections.getPorts(this).transmit(Markers.VIEWER, r);
			Mark.say("Transmitting imagine relation", r.asString());
			Connections.getPorts(this).transmit(IMAGINE, r);
		}
		else if (entity.relationP(Markers.TELL)) {
			Relation r = (Relation) entity;
			Mark.say("Transmitting tell relation", r.asString());
			Connections.getPorts(this).transmit(TELL, r);
		}
		else if (isAMakeCommand(entity)) {
			Mark.say("Transmitting desired property", getDesiredProperty(entity));
			Connections.getPorts(this).transmit(PERSUADE, new BetterSignal(PERSUADE, getDesiredProperty(entity)));
		}
		else {
			// Mark.say("Transmitting from command expert", entity);

			Connections.getPorts(this).transmit(Markers.NEXT, entity);
		}

	}

	private Object getDesiredProperty(Entity entity) {
		if (isAMakeCommand(entity)) {
			return RoleFrames.getObject(entity);
		}
		return null;
	}

	private boolean isAMakeCommand(Entity entity) {
		if (entity.isA(Markers.MAKE)) {
			if (entity.getSubject().isA(Markers.YOU)) {
				if (RoleFrames.getObject(entity).isA(Markers.PROPERTY_TYPE)) {
					return true;
				}
			}
		}
		return false;
	}
}