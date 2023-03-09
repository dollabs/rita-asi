package silaSayan;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;


import storyProcessor.ConceptDescription;
import utils.Mark;
import connections.AbstractWiredBox;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

public class SummaryHelper extends AbstractWiredBox{
	
	public static String TO_STORY_TELLER = "to story teller"; 
	 
	
	public SummaryHelper(){
		
	}
	
	public Sequence filterSummary(ConceptDescription concept){
		
		Mark.say("  In FILTER SUMMARY!!!");
		Sequence quickSummaryForConcept = new Sequence();
		for (Entity t: concept.getStoryElementsInvolved().getAllComponents()){
			quickSummaryForConcept.addElement(t);
		}
		return quickSummaryForConcept;
	}
	
	public Map<Entity,ArrayList<Entity>> rootMapper(Sequence storyLine){		
		Mark.say("  IN  ROOTMAPPER  !!!!!!");
		Map<Entity,ArrayList<Entity>> rootMap = new TreeMap<Entity,ArrayList<Entity>>();
		for (Entity t: storyLine.getAllComponents()){
			if (t.isA(Markers.EXPLANATION_RULE)||t.isA(Markers.PREDICTION_RULE)){
				Mark.say("CONSEQUENCE : ", t.asString());
				if (!(t.getObject()==null||t.getSubject()==null)){
					ArrayList<Entity> values = new ArrayList<Entity>();
					Mark.say("ANTECEDENTS : " );
					for (Entity f: t.getSubject().getAllComponents()){
						Mark.say("    ", f.asString());
						values.add(f);
					}
//					if (rootMap.get(t)!=null){
//						rootMap.get(t).addAll(values);
//					}
//					else if(rootMap.get(t)==null){
					rootMap.put(t, values);
//					}
//					if (rootMap.containsKey(t)){
//						rootMap.get(t).addAll(values);
//					}
//					else if (!(rootMap.containsKey(t))){
//						rootMap.put(t, values);
//					}	
					}		
				}
			}
		return rootMap;		
	}
	
	public Sequence extendRootsTree(Entity consequent, Map<Entity, ArrayList<Entity>> rootMap){
		Sequence relevantElements = new Sequence();

		//Base case: there is no going further back 
		if (!rootMap.containsKey(consequent)){
			if (relevantElements.getAllComponents().isEmpty()){
				Mark.say("EMPTY: relevantElements!!!!!!!!!!");
			}else{
				for (Entity t: relevantElements.getAllComponents()){
					Mark.say("Found relevant: ", t.asString());
				}
			}
			return relevantElements;
		}
		if(rootMap.containsKey(consequent)){
			for (Entity t: rootMap.get(consequent)){
				if (!relevantElements.containsDeprecated(t)){
					relevantElements.addElement(t);
					extendRootsTree(t, rootMap);
				}
			}
		}
			Mark.say("DOING WEIRD RETURN!!!!!!");
			return relevantElements;
	}
	
	public Sequence populateConceptSummary(Sequence quickSummary, Map<Entity,ArrayList<Entity>> rootMap){
		Mark.say(" POPULATING CONCEPT SUMMARY!!!!!");
		Sequence fullConceptSummary = new Sequence(); 
		for (Entity t: quickSummary.getAllComponents()){
			Sequence s = extendRootsTree(t, rootMap);
			fullConceptSummary.addAll(s);
		}
		Mark.say("FULL CONCEPT SUMMARRYYYYYY: ");
		for (Entity f: fullConceptSummary.getAllComponents()){
			Mark.say(f.asString());
		}
		return fullConceptSummary;
	}
	
	@SuppressWarnings({ "unused" })
	public LinkedList<ArrayList<Object>> summarySorter(ArrayList<ArrayList<Object>> unsorted){
		
		Map<Double, ArrayList<Object>> map = new HashMap<Double, ArrayList<Object>>();
		int attempt = 0;
		int done = 0;
		
		for (ArrayList<Object> l : unsorted){
			if (l.get(0).getClass()==Double.class){
				double key = (Double) l.get(0);
				ArrayList<Object> value = new ArrayList<Object>();
				
				value.add(0, l.get(1)); // Adding story element or rule
				value.add(1, l.get(2)); // Adding integer
				attempt = attempt + 1;

				map.put(key, value);

			}
		}
//		Mark.say("ATTEMPED : ", attempt);
//		Mark.say("ACTUALLY INCLUDED : ", map.size());
		
		LinkedList<ArrayList<Object>> finalSummary = new LinkedList<ArrayList<Object>>();
		
		TreeSet<Double> keySet = new TreeSet<Double>(map.keySet());

		for (Double key : keySet) {
			
			ArrayList<Object> seqToAdd = map.get(key);

			finalSummary.add(seqToAdd);
		}
		if (finalSummary.isEmpty()) {
			Mark.say("FINAL SUMMARY EMPTY!");
		}
		return finalSummary;
	}
}
