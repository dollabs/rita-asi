package expert;

import utils.Mark;
import connections.*;
import constants.Markers;
import frames.entities.Function;

/*
 * Created on Apr 26, 2010
 * @author phw
 */

public class StoryExpert extends AbstractWiredBox {

	public StoryExpert() {
		super("Story expert");
		Connections.getPorts(this).addSignalProcessor("answerQuestion");
	}

	public void answerQuestion(Object o) {
		if (o instanceof Function) {
			Function d = (Function) o;
			if (d.isA(Markers.WHY_QUESTION)) {
				Mark.say("Received question", d.asString());
			}
		}
	}

}
