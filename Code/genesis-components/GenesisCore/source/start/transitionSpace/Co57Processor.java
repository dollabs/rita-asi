package start.transitionSpace;

import java.util.*;

import start.StartPreprocessor;
import utils.Mark;
import connections.*;

/*
 * Created on May 15, 2011
 * @author phw
 */

public class Co57Processor extends AbstractWiredBox {

	private static Co57Processor co57Processor;

	private ArrayList<HashMap<String, Object>> attributeTraces = new ArrayList<HashMap<String, Object>>();

	private Ladders ladders = new Ladders();

	public static Co57Processor getCo57Processor() {
		if (co57Processor == null) {
			co57Processor = new Co57Processor();
		}
		return co57Processor;
	}

	private Co57Processor() {
		super("Co57Processor");
		Connections.wire(StartPreprocessor.TRACES_PORT, StartPreprocessor.getStartPreprocessor(), this);
		Connections.wire(this, StartPreprocessor.getStartPreprocessor());
		Connections.getPorts(this).addSignalProcessor("process");
	}

	private void augmentHashMap(HashMap<String, Object> map) {
		if (attributeTraces.size() >= 5) {
			attributeTraces.remove(0);
		}
		attributeTraces.add(map);
	}

	public void process(Object o) {
		if (o instanceof HashMap) {
			processAttentionTrace((HashMap<String, HashMap>) o);
		}
	}

	private void processAttentionTrace(HashMap<String, HashMap> trace) {
		Mark.say("Hashmap", trace);
		HashMap<String, Object> attributes = trace.get("FOAAttrs");
		if (attributeTraces.size() != 0) {
			HashMap<String, Object> previousAttributes = attributeTraces.get(attributeTraces.size() - 1);
			Integer focus = (Integer) attributes.get("FocusedObject");
			Integer previousFocus = (Integer) previousAttributes.get("FocusedObject");
			Ladder ladder = new Ladder();
			String f = Ladder.O1;
			if (focus == 1) {
				f = Ladder.O2;
			}
			else if (focus == 2) {
				f = Ladder.O3;
			}
			if (focus != null && previousFocus != null) {
				Double dx = (Double) attributes.get("dxFocusedObject");
				Double previousDx = (Double) previousAttributes.get("dxFocusedObject");
				// Mark.say("Result:", focus, dx, attributeTraces.size());
				if (focus != previousFocus) {
					// Report as appear
					if (dx > 0) {
						ladder.addTransition(f, Ladder.MR, Ladder.APPEAR);
					}
					else if (dx < 0) {
						ladder.addTransition(f, Ladder.ML, Ladder.APPEAR);
					}
				}
				else {
					// Report as appear if different direction
					if ((dx < 0 && previousDx > 0) || (dx > 0 && previousDx < 0)) {
						if (dx > 0) {
							ladder.addTransition(f, Ladder.MR, Ladder.APPEAR);
							ladder.addTransition(f, Ladder.ML, Ladder.DISAPPEAR);
						}
						else {
							ladder.addTransition(f, Ladder.ML, Ladder.APPEAR);
							ladder.addTransition(f, Ladder.MR, Ladder.DISAPPEAR);
						}
					}
				}
			}
			this.ladders.addLadder(ladder);
		}
		this.augmentHashMap(attributes);
		processLadders(ladders);
	}

	int delay = 0;

	private void processLadders(Ladders event) {
		String result = Patterns.getPatterns().match(event);
		// Mark.say("Count", delay);
		if (result != null) {
			if (delay > 30) {
				delay = 0;
				Mark.say("Produced result", result);
				Connections.getPorts(this).transmit(result);
			}
			else {
				++delay;
			}
		}
	}

	public static void main(String[] ignore) {
		Ladders event = new Ladders();
		Ladder ladder = new Ladder();
		ladder.addTransition(Ladder.O2, Ladder.MR, Ladder.APPEAR);
		event.addLadder(ladder);
		new Co57Processor().process(event);

	}
}
