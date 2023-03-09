package gui;

import java.util.HashMap;

import frames.entities.Bundle;
import frames.entities.Thread;
import memory.ThreadMemory;

/*
 * Created on Jan 6, 2008 @author phw
 */

public class TaughtWords implements ThreadMemory {
	
	private HashMap<String, Bundle> threadBundles = new HashMap<String, Bundle>();
	
	private static TaughtWords taughtWords;
	
	public static TaughtWords getTaughtWords() {
		if (taughtWords == null) {
			taughtWords = new TaughtWords();
		}
		return taughtWords;		
	}
	
	private TaughtWords() {
//		Thread thread = new Thread();
//		thread.add("Thing");
//		thread.add("foo");
//		thread.add("bouvier");
//		add("Bouvier", thread);
	}

	public void add(String word, Thread thread) {
		Bundle bundle = threadBundles.get(word);
		if (bundle == null) {
			bundle = new Bundle();
			threadBundles.put(word, bundle);
		}
		// System.out.println("Adding " + word + "||" + thread);
		bundle.add(thread);
	}

	public Bundle lookup(String word) {
		Bundle bundle = threadBundles.get(word);
		if (bundle == null || bundle.isEmpty()) {
			Thread thread = new Thread("thing");
			thread.addType("unknownWord");
			thread.addType(word);
			return new Bundle(thread);
		}
        return bundle;
	}

	public Bundle lookup(String word, String pos) {
		return lookup(word);
	}

}
