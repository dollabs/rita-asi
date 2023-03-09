package connections;

import utils.Mark;

/*
 * See Connections for documentation.
 */
public abstract class AbstractWiredBox implements WiredBox {

	private CheckBoxWithMemory gateKeeper;

	/**
	 * @deprecated
	 */
	public AbstractWiredBox() {
		super();
	}

	public AbstractWiredBox(String name) {
		super();
		getGateKeeper(name);
	}

	public String getName() {
		if (getGateKeeper() != null) {
			return getGateKeeper().getText();
		}
		else {
			return "No name given to instance of " + this.getClass().getName();
		}
	}

	public void setName(String name) {
		if (getGateKeeper() != null) {
			getGateKeeper().setText(name);
		}
		else {
			getGateKeeper(name);
		}
	}

	public String toString() {
		return getName();
	}

	public CheckBoxWithMemory getGateKeeper(String name) {
		if (gateKeeper == null) {
			gateKeeper = new CheckBoxWithMemory(name, true);
		}
		return gateKeeper;
	}

	public CheckBoxWithMemory getGateKeeper() {
		if (gateKeeper == null) {
			gateKeeper = new CheckBoxWithMemory("Unnamed box", true);
		}
		return gateKeeper;
	}

	// private void initializeCheckBox(String name) {
	// // Mark.say("Initializing gatekeeper for", name);
	//
	// }

	public void setGateKeeper(CheckBoxWithMemory gateKeeper) {
		this.gateKeeper = gateKeeper;
	}

}
