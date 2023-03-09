package silaSayan;

import connections.AbstractWiredBox;
import connections.Connections;
import constants.Markers;
import frames.entities.Sequence;

/** This class simulates narrator's own reading and understanding of a story.  **/
public class NarrateToSelf extends AbstractWiredBox {

	// FIELDS
	
	public static Sequence plot;
	public static String readablePlot;
	public static Sequence storyUnderstood;
	
	// INPUT PORTS	
	public static String RAW_STORY_INPUT_PORT = "unprocessed story coming in as representations"; // from StoryProcessor.
	public static String RAW_STORY_READABLE_INPUT_PORT = "unprocessed story coming in as string"; // from StoryProcessor. broadcasts plot of story (e.g. "Macbeth and Duncan are persons")
	public static String STORY_UNDERSTANDING_INPUT_PORT = "story and inferences"; //from StoryProcessor. broadcasts internal representation. i.e. the elaboration graph of narrator
		
	// OUTPUT PORTS
	public static String NARRATOR_STORY_OUTPUT_PORT = "narrator's understanding of the story"; // to ExtractRelevance.
	
	
	public NarrateToSelf(){
		this.setName("Narrator");
		Connections.getPorts(this).addSignalProcessor(RAW_STORY_INPUT_PORT, "savePlot");
		Connections.getPorts(this).addSignalProcessor(RAW_STORY_READABLE_INPUT_PORT, "saveReadablePlot"); //saves readable plot of story as a sequence.
		Connections.getPorts(this).addSignalProcessor(STORY_UNDERSTANDING_INPUT_PORT, "narrateToSelf"); // saves story understanding (plot and inferences) as a sequence.  
	}
	
	public void savePlot(Object o){
		if (o instanceof Sequence){
			plot = (Sequence) o;
		}
	}
	
	public void saveReadablePlot(Object o){
		if (o instanceof String){
			readablePlot = (String) o;
		}
	}
	
	//Q: How can I tell end of story??? Markers.THE_END_TEXT
	public void narrateToSelf(Object o){
		if (o instanceof Sequence){
			storyUnderstood = (Sequence) o;
		}
		//POSSIBLE PROBLEM HERE!!
		if (storyUnderstood.getObject().functionP(Markers.THE_END_TEXT)){
			//The story understood needs to be transmitted to updateStory and AudienceTestRun. (or maybe just AudienceTestRun)
			Connections.getPorts(this).transmit(NARRATOR_STORY_OUTPUT_PORT,storyUnderstood);
		}
	}
}
