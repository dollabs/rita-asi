package subsystems.rashi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import conceptNet.conceptNetNetwork.ConceptNetClient;
import conceptNet.conceptNetNetwork.ConceptNetQueryResult;
import frames.entities.Entity;
import generator.RoleFrames;
import utils.Mark;

public class RashiHelpers {
	
	/**
	 * Get action thread
	 * @param sequence
	 * @return
	 */
	public static List<String> getActionThreadString(Entity sequence){
		if(sequence.getBundle().getThreadContaining("action")!=null){
			Object[] actionThreadObject =  sequence.getBundle().getThreadContaining("action").toArray();
			ArrayList<String> actionThreadString = new ArrayList<String>();
			for( Object actionInThread :actionThreadObject){
				actionThreadString.add(actionInThread.toString());
			}
			return actionThreadString;
		}
		return new ArrayList<String>();
		
	}
	
	/** 
	 * 
	 * Get the subject and object of a sequence if it is a relation
	 * 
	 */
	public static Map<String, Entity> getSeqSubjectAndObjectForRelation(Entity seq){
	
		Map<String, Entity> results = new HashMap<String, Entity>();
		
		
		Entity s = seq.getSubject();
		Entity o = seq.getObject();
		//Set<Entity> children = o.getChildren(); 
		//List<Entity> components = o.getAllComponents();
		//Entity first = o.get(0);
		
		if(o.sequenceP()) {
			o = o.getElement(0).getSubject();
		}
		
		/*
		if(first.functionP()) {
			// if its a function, then want the subject
			Entity subject = o.getSubject();
			o = o.getAllComponents().get(0);
		}
		*/
		
		results.put("subject", s);
		results.put("object", o);
		/*
		Mark.say("LOOKING AT SEQUENCE", seq.toEnglish());
		Mark.say("SEQUENCE IS A RELATION");
		Mark.say("Seq type", seq.getType());
		Mark.say("Found subject: ", s);
		Mark.say("found object: ", o);
		
		
		Entity s2 = RoleFrames.getSubject(seq);
		Entity o2 = RoleFrames.getObject(seq);
		
		Mark.say("ALTERNATIVE UNIVERSE AKA ROLE FRAME");
		Mark.say("SUBJECT ROLE:", s2);
		Mark.say("OBJECT ROLE:" , o2);
		*/
		return results;
		
		
		
	}
	
	
	/** 
	 * 
	 * Get the subject and object of a sequence
	 * 
	 */
	public static Map<String, Entity> getSeqSubjectAndObject(Entity seq){
		
		Entity originalObject = seq.getObject();
		Entity originalSubject = seq.getSubject();
		List<Entity> originals = new ArrayList<Entity>();
		if(originalObject!=null){
			originals.add(originalObject);
		}
		if(originalSubject!=null){
			originals.add(originalSubject);
		}
		Map<String, Entity> results = new HashMap<String, Entity>();
		
		String current = "object";
		for(Entity original : originals){
			List<Entity> children = new ArrayList<Entity>();
			children.addAll(original.getChildren());
		
			Entity firstChild = original; 
			
			while(children.size() > 0){
				firstChild = children.get(0);
				List<Entity> nextChildren = new ArrayList<Entity>();
				//firstChild = children.get(0);
				
				nextChildren.addAll(children.get(0).getChildren());
				
				children = nextChildren;
			}
			Entity finalChild = firstChild;//(Entity) children.get(0);
			//Mark.say("final", current, finalCurrent, "features", finalCurrent.getFeatures());
			results.put(current, finalChild);
			//Mark.say(finalObject.geneneralize(finalObject));
			current = "subject";
		}

		return results; //entity to Features, while here
		
	}

	
	/** 
	 * Store the subject and object features and matcher types respectively.
	 * @param seq
	 * @param subjects
	 * @param objects
	 * @param entityToSequence
	 * @param entityToFeatures
	 * @return
	 */
	public static Map<String, Entity> storeSubjectObject(Entity seq, Map<Entity, Vector<String>> subjects, Map<Entity, Vector<String>> objects,
			Map<Entity, List<Entity>> entityToSequence, Map<Entity, ArrayList<Object>> entityToFeatures, Boolean printAll){
			
			Map<String, Entity> result = new HashMap<String, Entity>();
			
			// TODO: Capitalize on the type of thing we're looking at --rel, etc., to improve the method of getsubject/object 
			Map<String, Entity> finalizedRoles;
			
			if(seq.relationP()) finalizedRoles = RashiHelpers.getSeqSubjectAndObjectForRelation(seq);
			else finalizedRoles = RashiHelpers.getSeqSubjectAndObject(seq);
			
			if(printAll) Mark.say("FINALIZED ROLES", finalizedRoles);
			
			Entity object = finalizedRoles.get("object");
			Entity subject = finalizedRoles.get("subject");
						
			for(String role: finalizedRoles.keySet()){
				Entity currentRole = finalizedRoles.get(role);
				entityToSequence.putIfAbsent(currentRole, new ArrayList<Entity>());
				entityToSequence.get(currentRole).add(seq);
							
				entityToFeatures.put(currentRole, currentRole.getFeatures());
				//Vector<String> allTypes = currentRole.getAllTypes();
				//Vector<String> matcherTypes = currentRole.getMatcherTypes();
				
			}
			if(subject!=null) subjects.put(subject, subject.getMatcherTypes());
			
			if(object!=null) objects.put(object, object.getMatcherTypes());
			
			
			result.put("subject", subject);
			result.put("object", object);
			return result;
			
			
		}

	
	public static String getEntityNameCleaned(Entity ent) {
		
		int endEntNameIndex = ent.getName().indexOf("-");
		String entNameCleaned = ent.getName().substring(0, endEntNameIndex);
		
		return entNameCleaned;
		
		
	}
	
	/**
	 * Get the official "main" character if the provided entity is considered a variant.
	 * @param e
	 * @param variantToMain
	 * @return
	 */
	public static Entity getVariant(Entity e, Map<Entity, Entity> variantToMain) {
		
		return variantToMain.getOrDefault(e, e);
		
	}
	
	/*
	public static void putInDictKeytoList(Map<String, List<Entity>> dict, String key, Entity value) {
		
		List<Entity> currentValues = dict.getOrDefault(key, new ArrayList<Entity>());
		currentValues.add(value);
		dict.put(key, currentValues);
	}
	*/
	
	public static void putInDictKeytoList(Map<Entity, List<Entity>> dict, Entity key, Entity value) {
		
		List<Entity> currentValues = dict.getOrDefault(key, new ArrayList<Entity>());
		currentValues.add(value);
		dict.put(key, currentValues);
	}
	
}
