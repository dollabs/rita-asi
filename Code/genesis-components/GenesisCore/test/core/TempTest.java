package core;

import java.io.IOException;
import java.io.File; // ???? is this rightyes
import org.junit.Assert;

import connections.signals.BetterSignal;
import frames.entities.Sequence;
import genesis.HeadlessGenesis;
import utils.Mark;

public class TempTest {

	public final static String testStory = "stories/Shakespeare/Macbeth1.txt";
	

	
	public static String getParse(String storyPath) {
		try {
			// RLM: This sleep here is to handle a nasty concurrency bug.
			Thread.sleep(1000);
			BetterSignal parsedStory = 
					HeadlessGenesis.getHeadlessGenesis().processStoryFile(testStory);
		
			Sequence concepts = parsedStory.get(3, Sequence.class);
		
			return concepts.asStringWithoutIndexes();
			
		} catch (IOException | InterruptedException e){
			e.printStackTrace();
			return null;
		}
	}
	public static void main(String[] args) {
		System.out.println("1 " + getParse(testStory));
		System.out.println("2 " + getParse(testStory));
	}
	
}
