package dictionary;

import java.io.*;
import java.util.*;

import frames.entities.Bundle;
import frames.entities.Thread;
import utils.*;
import utils.NewTimer;

/*
 * Created on Apr 21, 2009
 * @author phw Modified Oct 4, 2010 by ADK to use Singleton pattern
 */

public class BundleGenerator {

	private static BundleGenerator.Implementation instance;



	@SuppressWarnings("unchecked")
	private static Class clazz = BundleGenerator.Implementation.class;

	@SuppressWarnings("unchecked")
	public static void setSingletonClass(Class c) {
		instance = null;
		clazz = c;
	}

	public static BundleGenerator.Implementation getInstance() {
		if (instance == null || instance.getClass() != clazz) {
			try {
				instance = (BundleGenerator.Implementation) clazz.newInstance();
			}
			catch (Exception e) {
				Mark.err("Blow out in BundelGnerator.Implementation.getInstance");
			}
		}
		return instance;
	}

	private BundleGenerator() {
	}// make sure nobody calls this ADK

	public static class Implementation {
		private HashMap<String, Bundle> bundleMap;

		private WordNet wordNet = null;// new WordNet();

		private int cacheSize = 0;

		public WordNet getWordNet() {
			if (wordNet == null) {
				wordNet = new WordNet();
			}
			return wordNet;
		}

		public Bundle getRawBundle(String word) {
			Bundle bundle = getBundleMap().get(word);
			if (bundle == null) {
				if (false && word.indexOf('_') >= 0 || word.indexOf('-') >= 0) {
					bundle = new Bundle();
					// Mark.say("Making empty bundle for", word);
				}
				else {
					try {

						// Mark.say("Direct on");
						// Mark.say("Doing local lookup to look up " + word);
						NewTimer.bundleGeneratorTimer.reset();
						bundle = getWordNet().lookup(word);
						// Special case check
						// For thing
						if ("thing".equals(word)) {
							Thread thread = new Thread();
							thread.add("thing");
							bundle.add(0, thread);
						}
						NewTimer.bundleGeneratorTimer.report(false, "Time for" + word);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					finally {
						// Mark.say("Direct off");
					}
				}
			}

			getBundleMap().put(word, bundle);
			return bundle;
		}

		public Bundle getBundle(String word) {
			// Mark.say("Getting bundle for word", word);
			Bundle bundle = getRawBundle(word).getThingClones();
			// Mark.say("Returning", bundle);
			return bundle;
		}

		public HashMap<String, Bundle> getBundleMap() {
			if (bundleMap == null) {
				bundleMap = new HashMap<String, Bundle>();
				cacheSize = 0;
			}
			return bundleMap;
		}

		private static boolean alreadyRead = false;

		@SuppressWarnings("unchecked")
		public HashMap<String, Bundle> readWordnetCache() {
			// Drip pan
			if (alreadyRead) {
				Mark.say("Wordnet cache already read");
				return null;
			}
			alreadyRead = true;

			File file = new File(System.getProperty("user.home") + File.separator + "wordnet.data");
			if (!file.exists()) {
				Mark.say("No wordnet cache,", file, "to load");
				return getBundleMap();
			}
			Mark.say(true, "Loading wordnet cache");
			FileInputStream fileInputStream;
			ObjectInputStream objectInputStream;
			try {
				fileInputStream = new FileInputStream(System.getProperty("user.home") + File.separator + "wordnet.data");
				objectInputStream = new ObjectInputStream(fileInputStream);
				Object object = objectInputStream.readObject();
				if (object != null) {
					bundleMap = (HashMap<String, Bundle>) object;
				}
				objectInputStream.close();
				fileInputStream.close();
			}
			catch (Exception e) {
				Mark.err("Harmless blow out in BundleGenerator.readWordnetCache");
				// e.printStackTrace();
			}
			cacheSize = getBundleMap().size();
			Mark.say(true, "Number of cached items read: " + cacheSize);
			return getBundleMap();
		}

		public void purgeWordnetCache() {
			Boolean debug = false;
			File file = new File(System.getProperty("user.home") + File.separator + "wordnet.data");
			if (!file.exists()) {
				Mark.say(debug, "Wordnet cache", file, "already purged");
			}
			else if (file.delete()) {
				Mark.say(debug, "Purged wordnet cache,", file, ", of " + cacheSize, "items");
			}
			else {
				Mark.say("Unable to purge wordnet cache file,", file);
			}
			getBundleMap().clear();
			Mark.say(debug, "Cache size is", getBundleMap().size());
			cacheSize = 0;
		}

		public void writeWordnetCache() {
			Mark.say(true, "Writing wordnet cache");
			FileOutputStream fileOutputStream;
			try {
				fileOutputStream = new FileOutputStream(System.getProperty("user.home") + File.separator + "wordnet.data");
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
				HashMap<String, Bundle> newMap = new HashMap<String, Bundle>();
				for (String key : getBundleMap().keySet()) {
					purge(key, getBundleMap().get(key));
				}
				objectOutputStream.writeObject(getBundleMap());
				objectOutputStream.close();

			}
			catch (Exception e) {
				e.printStackTrace();
			}
			cacheSize = getBundleMap().size();
			Mark.say("Number of Wordnet cached items written:", cacheSize);
		}

	}

	public static void setBundle(String word, Bundle bundle) {
		// System.out.println("Associating " + word + " with\n" + bundle);
		getBundleMap().put(word, bundle.getClone());
		// Mark.say("Now word |" + word + "| is associated with\n" + getBundle(word));
	}

	public static Bundle getBundle(String word) {
		return getInstance().getBundle(word);
	}

	@SuppressWarnings("unchecked")
	private static HashMap<String, Bundle> getBundleMap() {
		return getInstance().getBundleMap();
	}

	@SuppressWarnings("unchecked")
	public static HashMap<String, Bundle> readWordnetCache() {
		return getInstance().readWordnetCache();
	}

	public static void purgeWordnetCache() {
		getInstance().purgeWordnetCache();
	}

	public static void writeWordnetCache() {
		getInstance().writeWordnetCache();
	}

	private static Bundle purge(String key, Bundle input) {
		ArrayList<Thread> bundle = new ArrayList<Thread>();
		HashSet<Thread> set = new HashSet<Thread>();
		if (input != null && !input.isEmpty()) {
			for (Thread t : input) {
				if (!bundle.contains(t)) {
					bundle.add(t);
				}
			}
		}
		input.clear();
		input.addAll(bundle);
		if (bundle.size() > 50) {
			Mark.say("Bundle size for", key, "is " + bundle.size());
		}
		if (bundle.size() < input.size()) {
			Mark.say("Bundle for " + key, "reduced from", input.size() + " to " + bundle.size());
		}
		return input;
	}

	public static ArrayList<String> getSiblings(String word) {
		// First, get primed thread
		Bundle bundle = BundleGenerator.getBundle(word);
		if (bundle.size() == 0) {
			return null;
		}

		Thread primedThread = bundle.get(0);
		int length = primedThread.size();
		ArrayList<String> result = new ArrayList<String>();
		for (Thread t : getSiblingThreads(primedThread)) {
			String category = t.get(length - 1);
			if (result.contains(category) || category.equalsIgnoreCase(word)) {
				continue;
			}
			result.add(category);
		}
		return result;
	}

	public static ArrayList<Thread> getSiblingThreads(String word) {
		// First, get primed thread
		Bundle bundle = BundleGenerator.getBundle(word);
		if (bundle.size() == 0) {
			return null;
		}
		Thread primedThread = bundle.get(0);
		return getSiblingThreads(primedThread);
	}

	public static ArrayList<Thread> getSiblingThreads(Thread primedThread) {
		// Mark.say("Working to find siblings using", primedThread);
		// Note word
		String word = primedThread.lastElement();
		// Note length
		int length = primedThread.size();
		// Generate a big array of threads from all bundles
		ArrayList<Thread> allPrimedThreads = new ArrayList<Thread>();
		for (Bundle b : getBundleMap().values()) {
			if (b.size() != 0) {
				allPrimedThreads.addAll(b);
			}
		}
		// Get rid of the threads that are too short
		ArrayList<Thread> testList = new ArrayList<Thread>();
		testList.addAll(allPrimedThreads);
		allPrimedThreads.clear();
		for (Thread t : testList) {
			// If too short, reject
			if (t.size() < length) {
				continue;
			}
			else {
				allPrimedThreads.add(t);
			}
		}
		// Mark.say("Candidates = ", allPrimedThreads.size());
		// Walk down primed thread, using each class to shrink list of all
		// threads
		for (int i = 0; i < primedThread.size() - 1; ++i) {
			String nextClass = primedThread.get(i);
			testList = new ArrayList<Thread>();
			testList.addAll(allPrimedThreads);
			allPrimedThreads.clear();
			for (Thread t : testList) {
				if (nextClass.equalsIgnoreCase(t.get(i))) {
					allPrimedThreads.add(t);
				}
			}
			// Mark.say("Candidates = ", allPrimedThreads.size());
		}
		// Now collect siblings
		ArrayList<Thread> result = new ArrayList<Thread>();

		for (Thread t : allPrimedThreads) {
			String aClass = t.get(length - 1);
			// Mark.say("Word candidate =", aClass);
			if (result.contains(aClass) || aClass.equalsIgnoreCase(word)) {
				continue;
			}
			else {
				// Mark.say("Exploiting", t);
				result.add(t);
			}
		}
		return result;
	}

	public static void main(String[] ignore) {
		for (Thread t : (BundleGenerator.getBundle("run"))) {
			Mark.say(true, t);
		}
		System.out.println(BundleGenerator.getSiblingThreads("bird"));
		System.out.println(BundleGenerator.getSiblings("bird"));
	}
}