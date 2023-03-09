package subsystems.rashi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import conceptNet.conceptNetNetwork.ConceptNetClient;
import conceptNet.conceptNetNetwork.ConceptNetQueryResult;
import frames.entities.Entity;
import utils.Mark;

public class Sentiment {

	private static ArrayList<String> negativePersonDescriptors = new ArrayList<String>(Arrays.asList("bad-person", "wrongdoer", "terrorist", "unpleasant-person"));
	private static ArrayList<String> negativeActions = new ArrayList<String>(Arrays.asList("kill", "hurt", "perpetrate"));
	private static ArrayList<String> positiveActions = new ArrayList<String>(Arrays.asList("help", "save", "support", "comfort"));
	
	
	public static ArrayList<String> getNegativePersonDescriptors() {
		return negativePersonDescriptors;
	}
	
	/**
	 * Determine if an entity is negative (inherently, by implication) 
	 * TODO: extend using concept net
	 * 
	 * @param entity
	 * @param entityDescriptions
	 * @param useConceptNet
	 * @return
	 */
	public static boolean isEntityNegative(Entity entity, Vector<String> entityDescriptions, boolean useConceptNet) {
		
		
		Mark.say("Object testing for negativity", entity);
		ArrayList<String> negativePersonDescriptions = Sentiment.getNegativePersonDescriptors();
		for(String negativeDescriptor : negativePersonDescriptions) {
			if(entityDescriptions!= null && entityDescriptions.contains(negativeDescriptor)) {
				return true;
			}	
		}
		
		return false;
	}
	
	
	public static boolean actionDrawsBlood(List<String> actionThreads, Boolean useConceptNet, Boolean printAll) {
		
		return actionThreads.contains("kill");
		
		
	}
	
	/**
	 * Determine if an action is Positive, Negative or neutral 
	 * 
	 * Params: 
	 * 	ArrayList<String> actionThreads wordNet list of threads associated with the original action 
	 * 
	 * Returns: String "positive", "negative", "neutral" indicated result
	 * 
	 * TODO: Keep improving with the use of conceptNet, determine thresholding
	 */
	public static String getPosNegNeutral(List<String> actionThreads, Boolean useConceptNet, Boolean printAll){ 
		
		for(String actionThread : actionThreads ) {
			if(Sentiment.negativeActions.contains(actionThread)) return "negative";
		}
		
		for(String actionThread : actionThreads ) {
			if(Sentiment.positiveActions.contains(actionThread)) return "positive";
		}
	
		//if(actionThreads.contains("save") || actionThreads.contains("win")){return "positive";}
		
		// Now, use ConceptNet: (TODO: Look into perpetrate)
		if(!actionThreads.isEmpty() && useConceptNet){
			
			String action = actionThreads.get(actionThreads.size()-1);
			
			ConceptNetQueryResult<Double> badRes = ConceptNetClient.getSimilarityScore("bad", action);
			ConceptNetQueryResult<Double> goodRes = ConceptNetClient.getSimilarityScore("good", action);
			
			ConceptNetQueryResult<Double> harmRes = ConceptNetClient.getSimilarityScore("harm", action);
			ConceptNetQueryResult<Double> helpRes = ConceptNetClient.getSimilarityScore("help", action);
			
			//TODO: Solidify this logic! NaNs. Thresholds between values too, maye want to do AND not OR.
			if( (badRes.getResult() > goodRes.getResult()) || (harmRes.getResult() > helpRes.getResult())){
				
				if(printAll) Mark.say("ACTION, CONCL", action, "negative");
				
				return "negative";
			}
			
			if((badRes.getResult() < goodRes.getResult()) || (harmRes.getResult() < helpRes.getResult())){
				
				if(printAll) Mark.say("ACTION, CONCL", action, "positive");
				
				return "positive";
			}
			
			if(printAll) Mark.say("ACTION, CONCL", actionThreads, "neutral");
			return "neutral";
			
		}
		
		if(printAll) Mark.say("ACTION, CONCL (LAST)", actionThreads, "neutral");
		return "neutral";
		
	}
	
	
	
}
