package expert;

import utils.Html;

import connections.*;
import constants.Markers;

/*
 * Created on Mar 14, 2009
 * @author phw
 */

public class ActionExpert extends AbstractWiredBox {

	public ActionExpert() {
		super("Action expert");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object object) {
		if (Recognizers.action(object)) {
			Connections.getPorts(this).transmit(Markers.VIEWER, object);
			Connections.getPorts(this).transmit(Markers.TEXT, Html.p(Recognizers.theThing(object).asString()));
		}
		else {
			Connections.getPorts(this).transmit(Markers.NEXT, object);
		}

	}
}
