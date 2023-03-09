package start;

import java.util.HashMap;

import matchers.StandardMatcher;
import translator.BasicTranslator;
import utils.*;
import utils.minilisp.LList;
import connections.*;
import connections.signals.BetterSignal;
import frames.entities.Entity;

/*
 * Created on Jan 11, 20
 * @author phw
 */

public class StartPostprocessor extends AbstractWiredBox {

	boolean debug = false;

	private HashMap<String, String> objects = new HashMap<String, String>();

	private static Entity p0;

	private static StartPostprocessor startPostprocessor;

	public static StartPostprocessor getStartPostprocessor() {
		if (startPostprocessor == null) {
			startPostprocessor = new StartPostprocessor();
		}
		return startPostprocessor;
	}

	public StartPostprocessor() {
		// Preserved as public because mental models want their own copy
		super("Story processor");
		Connections.getPorts(this).addSignalProcessor(this::process);
	}

	public void process(Object object) {
		boolean debug = false;
		if (object instanceof Entity) {
			Entity t = (Entity) object;
			if (!t.getElements().isEmpty()) {
				Mark.say(debug, "Start postprocessor passthrough", t.getElements().get(0).asString());
				Connections.getPorts(this).transmit(object);
			}
			else {
				Mark.err("No elements:", t);
			}
		}
	}

	private String resolve(String s, LList<PairOfEntities> bindings) {
		for (Object object : bindings) {
			PairOfEntities pair = (PairOfEntities) object;
			Entity d = pair.getDatum();
			Entity p = pair.getPattern();
			if (s.equals(p.getType())) {
				return pair.getDatum().getType();
			}
		}
		return null;
	}

}