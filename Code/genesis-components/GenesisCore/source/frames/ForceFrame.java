package frames;
import java.util.HashMap;

import constants.RecognizedRepresentations;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Thread;
import utils.StringUtils;

/**
 * Frame for Leonard Talmy's Force Dynamics
 * @author blamothe
 */
public class ForceFrame extends Frame {
	/* Some Examples */
	private static HashMap<String, Relation> forceMap;
	public static HashMap<String, Relation> getMap() {
		if (forceMap == null) {
			forceMap = new HashMap<String, Relation>();
			forceMap.put("The ball kept rolling because the wind blew on it.", ForceFrame.makeForceRelation(new Entity ("ball"), "notExit", "weak", "restful", new Entity("wind"), "notExit"));
			forceMap.put("The shed kept standing despite the wind blowing against it.", ForceFrame.makeForceRelation(new Entity("shed"), "notExit", "strong", "restful", new Entity("wind"), "notExit"));
			forceMap.put("The ball kept rolling despite the stiff grass", ForceFrame.makeForceRelation(new Entity("ball"), "notExit", "strong", "active", new Entity ("grass"), "notExit"));
			forceMap.put("The ball stayed on the incline because of the ridge there", ForceFrame.makeForceRelation(new Entity("ball"), "notExit", "weak", "active", new Entity("ridge"), "notExit"));
			forceMap.put("The lamp fell from the table because the ball hit it.", ForceFrame.makeForceRelation(new Entity("lamp"), "notExit", "weak", "restful", new Entity("ball"), "enter"));
			forceMap.put("The plug's coming loose let the water flow from the tank.", ForceFrame.makeForceRelation(new Entity("water"), "notExit", "weak", "active", new Entity("plug"), "exit"));
			forceMap.put("The fire died down because the water dripped onto it", ForceFrame.makeForceRelation(new Entity("fire"), "notExit", "weak", "active", new Entity("water"), "enter"));
			forceMap.put("The particles settled because the stirring rod broke.", ForceFrame.makeForceRelation(new Entity("particles"), "notExit", "weak", "restful", new Entity("rod"), "exit"));
			forceMap.put("The plug's staying loose let the water drain.", ForceFrame.makeForceRelation(new Entity("water"), "notExit", "weak", "active", new Entity("plug"), "notEnter"));
			forceMap.put("The fan's being broken let the smoke hang in the chamber.", ForceFrame.makeForceRelation(new Entity("smoke"), "notExit", "weak", "restful", new Entity("fan"), "notEnter"));
		}
		return forceMap;
	}
	
	/* Use these for type checking */
	public static final String [] shiftPatterns = {"enter", "exit", "notEnter", "notExit", "unknown"};
	public static final String [] strengths = {"strong", "weak", "grow", "fade", "unknown"};
	public static final String [] tendencies = {"active", "restful", "unknown"};
	public static final String [] roles = {"agonist", "antagonist"};
	public static final String FRAMETYPE = (String) RecognizedRepresentations.FORCE_THING;
	public static final String TENDENCY = "forceTendency";
	public static final String STRENGTH = "forceStrength";
	public static final String AGENTTYPE = "forceAgent";
	public static final String RESULT = "result";
	public static final String PATTERN = "forceShiftPattern";
	
	/**
	 * Impliments force agents (agonists and antagonists) as double threaded derivatives with the thing as subject
	 * and the specific force dynamic role and shift patterns as types on a features thread.
	 * @param agent         : The agonist or antagonist thing.
	 * @param role          : A string which is either "agonist" or "antagonist".
	 * @param shiftPattern  : A string which must be an element of the static array ForceFrame.shiftPatterns.
	 * @return              : Derivative force agent.
	 */
	public static Function makeForceAgent(Entity agent, String role, String shiftPattern, String tendency, String strength) {
		if (!StringUtils.testType(role, roles)){
			System.err.println("Sorry, " + role + " is not a valid force dynamic role--must be agonist or antagonist.");
			return null;
		}
		if (!StringUtils.testType(shiftPattern, shiftPatterns)) {
			System.err.println("Sorry, " + shiftPattern + " is not a valid force dynamic shift pattern.");
			return null;
		}
		if (!StringUtils.testType(tendency, tendencies)) {
			System.err.println("Sorry, " + shiftPattern + " is not a valid force dynamic shift pattern.");
			return null;
		}
		if (!StringUtils.testType(strength, strengths)) {
			System.err.println("Sorry, " + strength + " is not a valid force dynamic strength.");
			return null;
		}
		Function result = new Function(AGENTTYPE, agent);
		result.addType(role);
		result.addType(shiftPattern, PATTERN);
		result.addType(strength, STRENGTH);
		result.addType(tendency, TENDENCY);
		
		return result;
	}
	
	/**
	 * Implements force frames as a relation between two froce agent derivatives.
	 * Information regarding the resulting action is stored in a fetures thread.
	 * @param agonist     : Derivative, agonist force agent.
	 * @param antagonist  : Derivative, antagonist force agent
	 * @param agoStrength : String, must be an element of the static array ForceFrame.strengths.
	 * @param agoTendency : String, must be an element of the static array ForceFrame.tendencies.
	 * @return            : Two threaded relation between agonist and antagonist.
	 */
	public static Relation makeForceRelation (Entity agonist, String agoShift, String agoStrength, String agoTend, Entity antagonist, String antShift) {
		Function ago = makeForceAgent(agonist, "agonist", agoShift, agoTend, agoStrength);
		Function ant = makeForceAgent(antagonist, "antagonist", antShift, oppositeTendency(agoTend), oppositeStrength(agoStrength));
		
		Relation result = new Relation(FRAMETYPE, ago, ant);
		result.addType(ForceFrame.evaluateResult(result), RESULT);
		return result;
	}
	
	public static Entity getAgonistThing (Relation forceRelation) {
		if (forceRelation.isA(FRAMETYPE)) {
			return forceRelation.getSubject().getSubject();
		}
		System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
		return null;
	}
	
	public static Entity getAntagonistThing (Relation forceRelation) {
		if (forceRelation.isA(FRAMETYPE)) {
			return forceRelation.getObject().getSubject();
		}
		System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
		return null;
	}
	
	public static Function getAgonist (Relation forceRelation) {
		if (forceRelation.isA(FRAMETYPE)) {
			return (Function) forceRelation.getSubject();
		}
		System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
		return null;
	}
	
	public static Function getAntagonist (Relation forceRelation) {
		if (forceRelation.isA(FRAMETYPE)) {
			return (Function) forceRelation.getObject();
		}
		System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
		return null;
	}
	
	public static String getAgonistName(Relation forceRelation){
		if(!forceRelation.isA(FRAMETYPE)){
			System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
			return "";
		}
		return getAgonist(forceRelation).getSubject().getBundle().getType();
	}
	
	public static String getAntagonistName(Relation forceRelation){
		if(!forceRelation.isA(FRAMETYPE)){
			System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
			return "";
		}
		return getAntagonist(forceRelation).getSubject().getBundle().getType();
	}
	
	public static String getAgonistStrength(Relation forceRelation) {
		if(!forceRelation.isA(FRAMETYPE)){
			System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
			return "";
		}
		return getAgonist(forceRelation).getBundle().getThreadContaining(STRENGTH).getType();
	}
	
	public static String getAntagonistStrength(Relation forceRelation) {
		if(!forceRelation.isA(FRAMETYPE)){
			System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
			return "";
		}
		return getAntagonist(forceRelation).getBundle().getThreadContaining(STRENGTH).getType();
	}
	
	public static String getAgonistTendency(Relation forceRelation) {
		if (!forceRelation.isA(FRAMETYPE)) {
			System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
			return "";
		}
		return getAgonist(forceRelation).getBundle().getThreadContaining(TENDENCY).getType();
	}
	
	public static String getAntagonistTendency(Relation forceRelation) {
		if (!forceRelation.isA(FRAMETYPE)) {
			System.err.println("Sorry, " + forceRelation + " is not a valid force relation.");
			return "";
		}
		return getAntagonist(forceRelation).getBundle().getThreadContaining(TENDENCY).getType();
	}
	
	public static String getAgonistShiftPattern(Relation forceRelation) {
		if (!forceRelation.isA(FRAMETYPE)) {
			System.err.println("Sorry, " + forceRelation + " is not a force relation.");
			return "";
		} 
		return getAgonist(forceRelation).getBundle().getThreadContaining(PATTERN).getType();
	}
	
	public static String getAntagonistShiftPattern(Relation forceRelation) {
		if (!forceRelation.isA(FRAMETYPE)) {
			System.err.println("Sorry, " + forceRelation + " is not a force relation.");
			return "";
		}
		return getAntagonist(forceRelation).getBundle().getThreadContaining(PATTERN).getType();
	}
	
	/**
	 * Returns the tendency, either action or rest, of the dominant force agent in the force relation provided.
	 * @param forceRelation
	 * @return  String tendency.
	 */
	public static String evaluateResult(Relation forceRelation) {
		if ((getAntagonistShiftPattern(forceRelation) == "exit") || (getAntagonistShiftPattern(forceRelation) == "notEnter")) {
			return getAgonistTendency(forceRelation);
		} else if ((getAgonistStrength(forceRelation) == "strong") || (getAgonistStrength(forceRelation) == "grow")) {
			return getAgonistTendency(forceRelation);
		} else if ((getAgonistTendency(forceRelation) == "unknown") || (getAgonistStrength(forceRelation) == "unknown")){
			return "unknown";
		} else {
			return getAntagonistTendency(forceRelation);
			
		}
	}
	
	/**
	 * Returns the opposite tendency of the tendency provided.  Mainly used for methods involving
	 * antagonists because the antagonist tendency must be the opposite of the antagonist tendency.
	 * @param tendency : String; element of the array ForceFrame.tendencies.
	 * @return : String opposite of tendency.
	 */
	public static String oppositeTendency(String tendency) {
		if (tendency == "active") {
			return "restful";
		} else if (tendency == "restful") {
			return "active";
		} else if (tendency == "unknown"){
			return tendency;
		} else {
			System.err.println("Sorry, " + tendency + " is not a valid force dynamic tendency");
			return "";
		}
	}
	
	/**
	 * Returns the opposite strength of the strength provided.
	 * @param strength : String; must be an elemnt of the static array ForceFrame.strengths
	 * @return : String, opposite of strength.
	 */	
	public static String oppositeStrength(String strength) {
		if (strength == "strong") {
			return "weak";
		} else if (strength == "weak") {
			return "strong";
		} else if (strength == "grow") {
			return "fade";
		} else if (strength == "fade") {
			return "grow";
		} else if (strength == "unknown"){
			return strength;
		} else {
			System.err.println("Sorry, " + strength + " is not a valid force dynamic strength.");
			return "";
		}
	}
	
	/* End static methods */
	
	private Relation forceRelation;
	
	/**
	 * Main constructor for force frames.
	 */
	public ForceFrame(Entity t) {
		if (t.isA(ForceFrame.FRAMETYPE)) {
			this.forceRelation = (Relation) t;
		} else {
			System.err.println("Error, called ForceFrame constructor with non force relation " + t + ".");
		}
	}
	
	public ForceFrame (ForceFrame s) {
		this((Entity) s.getThing().clone());
	}
	
	public Entity getThing() {
		return forceRelation;
	}
	
	public String toString() {
		if (forceRelation != null) {
			return forceRelation.toString();
		}
		return "";
	}
	
	public static void main(String[] args) {
		HashMap<String, Relation> map = getMap();
		//System.out.println(PrettyPrint.prettyPrint(map.get("The ball stayed on the incline because of the ridge there")));
	}
}
