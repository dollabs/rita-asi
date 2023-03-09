package frames.utilities;
import java.util.Set;

import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import utils.StringUtils;
import utils.groups.Graph;
/**
 * Utility class for the Augmented Decision Rationale Language.
 * @author blamothe
 */
public class ADRLFactory {
	public static String [] elementTypes = {"state", "goal", "action", "actor action", "actor goal"};
	public static String [] stateTypes = {"claim", "result"};
	public static String [] relationTypes = {"causal", "state", "action", "goal", "temporal"};
	public static String [] causalRelationTypes = {"causes", "enables", "gates"};
	public static String [] stateRelationTypes = {"strengthens", "weakens", "invalidates", "validates", "presupposes", "sub-state"};
	public static String [] actionRelationTypes = {"prevents", "accomplishes", "hinders", "aids"};
	public static String [] temporalRelationTypes = {"parallel to", "after", "before"};
	public static String subgoalType = "subgoal of";
	public static String ELEMENTNAME = "adrl element";
	public static String RELATIONNAME = "adrl relation";
	public static String SCENARIONAME = "adrl scneario";
	
	public static Graph makeNewScenario() {
		Graph scenario = new Graph(SCENARIONAME);
		return scenario;
	}
	
	public static Function makeADRLElement(String type, Entity frame) {
		if(!StringUtils.testType(type, elementTypes)) {
			System.err.println("Type " + type + " provided to makeADRLElement is not a valid element type.");
			return null;
		}
		Function result = new Function(ELEMENTNAME, frame);
		result.addType(type);
		return result;
	}
	
	public static Relation makeADRLRelation(String subType, Entity subject, Entity object) {
		Relation result = new Relation(subject, object);
		String type;
		if (StringUtils.testType(subType, causalRelationTypes)) {
			type = "causal";
		}
		else if (StringUtils.testType(subType, stateRelationTypes)) {
			type = "state";
		}
		else if (StringUtils.testType(subType, actionRelationTypes)) {
			type = "action";
		}
		else if (StringUtils.testType(subType, temporalRelationTypes)) {
			type = "temporal";
		}
		else if (subType == subgoalType) {
			type = "goal";
		}
		else {
			System.err.println("Type " + subType + " provided to makeADRLRelation is not valid.");
			return null;
		}
		result.addType(RELATIONNAME);
		result.addType(type);
		result.addType(subType);
		return result;
	}
	
	public static boolean elementCheck (Set<Function> elts) {
		for (Function e : elts) {
			if (!e.isA(ELEMENTNAME)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean relationCheck (Set<Relation> rels) {
		for (Relation r : rels) {
			if (!r.isA(RELATIONNAME)) {
				return false;
			}
		}
		return true;
	}
}
