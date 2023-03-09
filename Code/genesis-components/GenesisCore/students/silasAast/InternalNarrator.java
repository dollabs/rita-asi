package silasAast;

import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import frames.entities.Sequence;

public class InternalNarrator extends AbstractWiredBox {

	// FIELDS
	public static Sequence text = new Sequence(); // this is the story that reaches the narrator

	public static Sequence narratorStory = new Sequence(); // this is the whole story as the narrator understands it
	public static Sequence narratorReflections = new Sequence();
	private boolean communicatedWithSpecifier = false;
	
	// INPUT PORTS
	public static String TEXT_IN; // from StartPreprocessor?
	public static String STORY_IN; //from StoryProcessor via MentalModel
	public static String REFLECTIONS_IN; //form StoryProcessor via MentalModel
	
	// OUTPUT PORTS
	public static String TEXT_OUT; // to StoryModifier
	public static String STORY_OUT; // to GoalSpecifier 
	
	public InternalNarrator(){

		Connections.getPorts(this).addSignalProcessor(TEXT_IN,"storeText");
		Connections.getPorts(this).addSignalProcessor(STORY_IN,"storeUnderstanding");
		Connections.getPorts(this).addSignalProcessor(REFLECTIONS_IN,"storeReflections");

	}
	
	public void storeText(Object o){
		if (o instanceof Sequence){
			text = (Sequence) o;
			Connections.getPorts(this).transmit(TEXT_OUT, text);
		}
	}
	
	public void storeUnderstanding(Object o){
		if (o instanceof Sequence){
			narratorStory = (Sequence) o;
			if (narratorStory!=null && narratorReflections!=null && !communicatedWithSpecifier){
				wrapStoryAndReflections(narratorStory, narratorReflections);
				communicatedWithSpecifier = true;
			}
		}
	}
	
	public void storeReflections(Object o){
		if (o instanceof Sequence){
			narratorReflections = (Sequence) o;
			if (narratorStory!=null && narratorReflections!=null && !communicatedWithSpecifier){
				wrapStoryAndReflections(narratorStory, narratorReflections);
				communicatedWithSpecifier = true;
			}
		}
	}	
	
	public void wrapStoryAndReflections(Sequence story, Sequence reflections){
		BetterSignal wrap = new BetterSignal(story, reflections);
		Connections.getPorts(this).transmit(STORY_OUT, wrap);
	}

}
