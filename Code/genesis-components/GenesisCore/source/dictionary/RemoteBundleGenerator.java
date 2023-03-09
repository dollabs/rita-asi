package dictionary;

import java.util.Date;

import utils.*;
import connections.*;
import connections.Connections.NetWireError;
import connections.Connections.NetWireException;
import connections.signals.BetterSignal;
import frames.entities.Bundle;

public class RemoteBundleGenerator extends BundleGenerator.Implementation implements WiredBox {

	public static String WORDNET_SERVICE = "Wordnet Bundle Generator";

	RPCBox bundleGeneratorProxy;

	public RemoteBundleGenerator() throws NetWireException {
		bundleGeneratorProxy = (RPCBox) Connections.subscribe(WORDNET_SERVICE);
	}

	protected RemoteBundleGenerator(boolean ignoreMe) {
		// Commented out 11 May 2018 because netwire no longer works and this causes webstart to fail. PHW
		// try {
		// bundleGeneratorProxy = (RPCBox) Connections.subscribe(WORDNET_SERVICE);
		// }
		// catch (NetWireException e) {
		// Mark.err("Encountered NetWire exception");
		// // e.printStackTrace();
		// }
		// catch (NetWireError e) {
		// }
	}

	public Bundle getRawBundle(String word) {
		// System.out.println("getBundle called! "+word);
		Bundle bundle = getBundleMap().get(word);
		if (bundle == null) {
			if (false && word.indexOf('_') >= 0 || word.indexOf('-') >= 0) {
				bundle = new Bundle();
				Mark.say("Making empty bundle for", word);
			}
			else {
				try {
					// To do, convert this to a wired connection or a ordinary
					// observer-observable combination, to ActivityMonitor, but
					// right now, not obviously easy to find where class is
					// instantiated, hence not easy to see where to connect a a
					// wire or observer.

					NewTimer.bundleGeneratorTimer.reset();
					Mark.say("Doing RPC to look up " + word);
					bundle = (Bundle) bundleGeneratorProxy.rpc("getBundle", new Object[] { word });
					NewTimer.bundleGeneratorTimer.report(false, "Word timer time for " + word);
				}
				catch (Exception e) {
					Mark.err("Harmless exception thrown in RemoteBundleGenerator.getRawBundle");
					// e.printStackTrace();
				}
				finally {
				}
			}

		}
		getBundleMap().put(word, bundle);
		return bundle;
	}

	public static class BundleServer extends BundleGenerator.Implementation implements WiredBox {
		@Override
		public String getName() {
			return "Bundle Generator Server Box";
		}

		public Bundle getRawBundle(String word) {
			System.out.println("On " + new Date() + ", got request for bundle of word " + word + "!");
			return super.getRawBundle(word);
		}
	}

	public static void main(String[] args) {
		System.out.println("Starting Bundle Generator Service!");
		BundleGenerator.setSingletonClass(BundleServer.class);
		try {
			Connections.useWireServer(DefaultSettings.WIRE_SERVER);
			Connections.publish((WiredBox) BundleGenerator.getInstance(), WORDNET_SERVICE);
			try {
				BundleGenerator.readWordnetCache();
			}
			catch (Exception e) {
				BundleGenerator.purgeWordnetCache();
				BundleGenerator.writeWordnetCache();
			}
			System.out.println("Serving forever!");
			while (true) {
				try {
					Thread.sleep(10000);
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		catch (NetWireException e) {
			e.printStackTrace();
		}
		catch (NetWireError e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "Remote bundle generator";
	}
}
