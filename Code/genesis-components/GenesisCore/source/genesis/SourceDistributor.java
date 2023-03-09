package genesis;

import gui.*;
import utils.Mark;
import connections.*;

/*
 * Created on Jan 23, 2010
 * @author phw
 */

public class SourceDistributor extends AbstractWiredBox {

	public static final String CONTROL = "control";

	public static final String CASE = "case";

	public static final String REFLECTIVE_KNOWLEDGE = "reflex";

	public static final String COMMONSENSE_KNOWLEDGE = "reflect";

	public static final String GENERAL_KNOWLEDGE = "general";

	String mode = GENERAL_KNOWLEDGE;

	public SourceDistributor() {
		super("Source distributor");
		Connections.getPorts(this).addSignalProcessor(CONTROL, "switchMode");
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void switchMode(Object o) {
		if (mode == o) {
			// Do nothing
		}
		else if (o == CASE) {
			mode = CASE;
			Connections.getPorts(this).transmit(TabbedTextViewer.TAB, "Story");
		}
		else if (o == COMMONSENSE_KNOWLEDGE) {
			mode = COMMONSENSE_KNOWLEDGE;
		}
		else if (o == REFLECTIVE_KNOWLEDGE) {
			mode = REFLECTIVE_KNOWLEDGE;
		}
		else if (o == GENERAL_KNOWLEDGE) {
			mode = GENERAL_KNOWLEDGE;
		}
	}

	public void process(Object o) {
		if (mode == CASE) {
			Connections.getPorts(this).transmit(CASE, o);
		}
		else if (mode == COMMONSENSE_KNOWLEDGE) {
			Connections.getPorts(this).transmit(COMMONSENSE_KNOWLEDGE, o);
		}
		else if (mode == REFLECTIVE_KNOWLEDGE) {
			Connections.getPorts(this).transmit(REFLECTIVE_KNOWLEDGE, o);
		}
		else if (mode == GENERAL_KNOWLEDGE) {
			Connections.getPorts(this).transmit(GENERAL_KNOWLEDGE, o);
		}
	}
}
