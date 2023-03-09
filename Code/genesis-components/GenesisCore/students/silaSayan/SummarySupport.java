package silaSayan;

import java.util.ArrayList;
import java.util.LinkedList;

import frames.entities.Entity;
import frames.entities.Sequence;

public class SummarySupport {
	public SummarySupport(){
		
	}
	
public void extractSummaryFromStory(Sequence explicitStory, Sequence reflectionElements, LinkedList<ArrayList<Object>> storyCompilation){
		
		for (ArrayList<Object> recordedElement : storyCompilation){
			if (reflectionElements.containsDeprecated((Entity) recordedElement.get(0))){
				
			}
		}
		
	}
	
}
