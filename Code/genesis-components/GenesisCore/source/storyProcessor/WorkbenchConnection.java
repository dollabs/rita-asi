package storyProcessor;

import connections.*;
import frames.entities.Entity;
import translator.Translator;
import utils.Mark;

/*
 * Created on Feb 13, 2014
 * @author phw
 */

public class WorkbenchConnection extends AbstractWiredBox {

	private WorkbenchConnection() {
		super("Workbench connection");

	}

	private static WorkbenchConnection instance;

	public static WorkbenchConnection getWorkbenchConnection() {
		if (instance == null) {
			instance = new WorkbenchConnection();
		}
		return instance;
	}

	public void transmit(Entity x) {
		Connections.getPorts(this).transmit(x);
	}

	public void test() {
		Mark.say("Running insertion test");
		try {
			transmit(Translator.getTranslator().translate("Start story titled \"Unnamed\".").getElements().get(0));
			transmit(Translator.getTranslator().translate("John loves Mary.").getElements().get(0));
			transmit(Translator.getTranslator().translate("John marries Mary.").getElements().get(0));
			transmit(Translator.getTranslator().translate("John marries Mary because John likes money.").getElements().get(0));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
