package basics;

import genesis.FileSourceReader;
import genesis.GenesisPlugBoardUpper;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import storyProcessor.StoryProcessor;
import utils.Mark;
import connections.Connections;
import connections.WiredBox;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Sequence;


@SuppressWarnings("serial")
public class T7_StoryProcessor extends GenesisPlugBoardUpper {

	private static T7_StoryProcessor headlessGenesis;

	private QueueBox collectCompleteStory = new QueueBox();

	public static void main(String[] ignore) {
		
		// to show you the trace of the story processing:
		FileSourceReader.debug = true;
		StoryProcessor.debug = true;
		
		// create a new instance of Genesis system without interface and wire up the components
		if (headlessGenesis == null) {
			headlessGenesis = new T7_StoryProcessor();
			headlessGenesis.initializeWiring(); 
		}
		
		try {
			BetterSignal signal;
			signal = headlessGenesis.processStoryFile("Align Annotate.txt");
			demoProcessSignal(signal);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor inserts connection to story processor via MentalModel class 
	 * and says what to do when a complete story is available, namely forward to the AntiBox.
	 */
	private T7_StoryProcessor() {
		super();
		this.setName("Headless Genesis");

		Connections.wire(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, getMentalModel1(), collectCompleteStory);
	}

	/**
	 * Initiate reading of a story file and (of course) the files it calls upon. This method will return immediately;
	 * the result is available via a blocking call to <code>getProcessedStory()</code>.
	 * 
	 * @param storyPath
	 *            a path to the story resource, relative to any resource on the classpath. Works in jar files, webstart,
	 *            and for regular old files.
	 * @throws IOException
	 */
	public void submitStory(final String storyPathOrName) {
		new Thread() {
			public void run() {
				getFileSourceReader().readTheWholeStoryWithThread(storyPathOrName);
			}
		}.start();
	}

	public void submitStory(final File story) {
		new Thread() {
			public void run() {
				getFileSourceReader().readTheWholeStoryWithThread(story.getName());
			}
		}.start();
	}

	/**
	 * Get the processed story from Genesis once processing has been completed. 
	 * You must call <code>submitStory</code> before calling this method or the call will block forever.
	 * 
	 * @return a detailed analysis of the story
	 */
	public BetterSignal getProcessedStory() {
		Object signal;
		try {
			signal = Executors.newFixedThreadPool(1).submit(collectCompleteStory).get();
			if (signal instanceof BetterSignal) {
				return (BetterSignal) signal;
			}
			else {
				throw new ClassCastException();
			}
		}
		catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * This is the main function of HeadlessGenesis --- call this when you just want to input a story and get a detailed
	 * analysis of its plot. The method comes in three different flavors, depending on the type of input you have:
	 * <ul>
	 * <li>You can use a String path relative to any resource on the classpath. Works in jar files, webstart, and for
	 * regular old files. This is most commonly used internally in Genesis.
	 * <li>File -- for use if the file is on a local disk
	 * <li>URL -- access stories on the Internet, external jar files, whatever.
	 * </ul>
	 * 
	 * @param storyPath
	 *            a path to the story resource as described above.
	 * @return a detailed analysis of the story.
	 * @throws IOException
	 *             if the story is unreachable.
	 */
	public BetterSignal processStoryFile(String storyPathOrName) throws IOException {
		submitStory(storyPathOrName);
		return getProcessedStory();
	}

	public BetterSignal processStoryFile(File storyFile) throws IOException {
		submitStory(storyFile);
		return getProcessedStory();
	}

	/**
	 * Print results of story processing. This shows how to take BetterSignal instance apart, the one coming in on
	 * COMPLETE_STORY_ANALYSIS_PORT.
	 * 
	 * @param signal
	 */
	public static void demoProcessSignal(BetterSignal s) {
		Mark.yellow("Getting back to T7_StoryProcessor");

		Sequence story = s.get(0, Sequence.class);
		Sequence explicitElements = s.get(1, Sequence.class);
		Sequence inferences = s.get(2, Sequence.class);
		Sequence concepts = s.get(3, Sequence.class);
		Mark.say("\n\n\nStory elements");

		// original story elements and inferred story elements
		Mark.say("\n\n\nStory: get(0, Sequence.class)");
		for (Entity e : story.getElements()) {
			Mark.say(e.asString());
		}
		
		// original story elements only
		Mark.say("\n\n\nExplicit story elements: get(1, Sequence.class)");
		for (Entity e : explicitElements.getElements()) {
			Mark.say(e.asString());
		}
		
		// the rules used for making inference
		Mark.say("\n\n\n" + "Instantiated commonsense rules: get(2, Sequence.class)");
		for (Entity e : inferences.getElements()) {
			Mark.say(e.asString());
		}
		
		// the concept patterns identified in the story
		Mark.say("\n\n\n" + "Instantiated concept patterns: get(3, Sequence.class)");
		for (Entity e : concepts.getElements()) {
			Mark.say(e.asString());
		}
		
		Mark.say("\n\n\n");
	}
	
	public class QueueBox implements WiredBox, Callable<Object>{
		
		public BlockingDeque<Object> results = 
				new LinkedBlockingDeque<Object>();
		
		public String getName(){
			return "Anti Box";
		}
		
		public void process(Object input){
			results.offer(input);
			Connections.getPorts(this).transmit(input);
		}
		
		public QueueBox(){
			Connections.getPorts(this).addSignalProcessor("process"); 
		}

		public Object call() throws InterruptedException {
			return results.take();
		}
	}


}