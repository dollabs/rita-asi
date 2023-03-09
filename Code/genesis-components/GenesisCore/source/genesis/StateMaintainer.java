package genesis;

import connections.*;

/*
 * Created on Mar 27, 2009
 * @author phw
 */

public class StateMaintainer extends AbstractWiredBox {
	GenesisGetters genesisGetters;

	public StateMaintainer(GenesisGetters genesisGetters) {
		super("State maintainer");
		this.genesisGetters = genesisGetters;
		Connections.getPorts(this).addSignalProcessor("setState");
	}

	public void setState(Object o) {
		if (genesisGetters != null) {
			genesisGetters.changeState(o);
		}
	}

}
