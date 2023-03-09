package dictionary;

import java.util.HashMap;

import utils.Mark;
import connections.Connections.NetWireError;
import frames.entities.Bundle;

public class FallbackBundleGenerator extends RemoteBundleGenerator {
	private BundleGenerator.Implementation fallback = null;

	public FallbackBundleGenerator() {
		super(false);

		// Following commented out pending recompiling of wordnet server; prevents ugly blowout

		// if(bundleGeneratorProxy==null){
		// Mark.say("Initialization of remote bundle generator failed. Falling back permanently on local WordNet");
		// System.err.flush();
		// initFallback();
		// }

		// Following one line added pending recompiling of wordnet server; prevents ugly blowout

		bundleGeneratorProxy = null;
		initFallback();
	}

	protected void initFallback() {
		fallback = new BundleGenerator.Implementation();
		try {
			fallback.readWordnetCache();
		}
		catch (Exception e) {
			fallback.purgeWordnetCache();
			fallback.writeWordnetCache();
		}
		// copy items from old cache into new one:
		for (String word : super.getBundleMap().keySet()) {
			fallback.getBundleMap().put(word, super.getBundleMap().get(word));
		}
	}

	@Override
	public Bundle getRawBundle(String word) {

		if (bundleGeneratorProxy != null && fallback == null) { // there is a cache coherence problem with falling back.
																// it is probably a bad idea to
			// keep retrying after we notice that the server has failed
			try {
				return super.getRawBundle(word);

			}
			catch (NetWireError e) {
				System.out.println(e.getStackTrace());
				e.printStackTrace();
				if (fallback == null) {
					System.err.println(e.toString());
					System.err.println("Remote error while looking up bundle of " + word + ". Falling back");// temporarily
																											 // on local
																											 // WordNet");
					System.err.flush();
					initFallback();
				}
				else {
					System.err.println("RPC failed. falling back on local");
					System.err.flush();
				}
				return fallback.getRawBundle(word);
			}
		}
		else {
			return fallback.getRawBundle(word);
		}
	}

	@Override
	public HashMap<String, Bundle> getBundleMap() {
		if (fallback != null) {// we've fallen back on local Wordnet
			return fallback.getBundleMap();
		}
		else {
			return super.getBundleMap();
		}
	}
}
