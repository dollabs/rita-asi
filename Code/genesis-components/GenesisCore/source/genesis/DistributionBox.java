package genesis;

import connections.*;

public class DistributionBox extends AbstractWiredBox {

	GenesisGetters gauntlet;

	public DistributionBox(GenesisGetters genesisGetters) {
		super("Distribution box");
		this.gauntlet = genesisGetters;
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object input) {
		Connections.getPorts(this).transmit(input);
	}
}
