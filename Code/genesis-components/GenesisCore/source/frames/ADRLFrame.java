package frames;
import java.util.Set;

import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;

import java.util.Iterator;

import utils.StringUtils;
import utils.groups.Graph;
/**
 * Frame for the Augmented Decision Rationale Language.  Scenarios are implemented
 * as a relation who's subject is a sequence of Derivatives with type one of ADRL's
 * element type and with a frame as subject.  The object of the relation is a sequence
 * of ADRL relations between frames.
 * @author blamothe
 *
 */
public class ADRLFrame extends Frame{
	public static String FRAMETYPE = "adrlScenario";
	public static String ELEMENTNAME = "adrlElement";
	public static String RELATIONNAME = "adrlRelation";
	
	public static String [] elementTypes = {"state", "goal", "action", "actor action", "actor goal"};
	public static String [] stateTypes = {"claim", "result"};
	public static String [] relationSupertypes = {"causal", "state", "action", "goal", "temporal"};
	public static String [] causalRelationTypes = {"causes", "enables", "gates"};
	public static String [] stateRelationTypes = {"strengthens", "weakens", "invalidates", "validates", "presupposes", "substate"};
	public static String [] actionRelationTypes = {"prevents", "accomplishes", "hinders", "aids"};
	public static String [] temporalRelationTypes = {"parallel to", "after", "before"};
	
	public static Graph makeNewScenario() {
		return new Graph(FRAMETYPE);
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
	
	public static String elementType(Function element) {
		if (element.isA(ELEMENTNAME)) {
			return element.getType();
		} else {
			System.err.println("Sorry, " + element + " is not an ADRL element.");
			return null;
		}
	}
	
	public static String relationType(Relation relation) {
		if (relation.isA(RELATIONNAME)) {
			return relation.getType();
		} else {
			System.err.println("Sorry, " + relation + " is not an ADRL relation.");
			return null;
		}
	}
	
	public static String relationSupertype(Relation relation) {
		if (relation.isA(RELATIONNAME)) {
			return relation.getSupertype();
		} else {
			System.err.println("Sorry, " + relation + " is not an ADRL relation.");
			return null;
		}
	}
	
	public static Relation mkeADRLRelation(String type, Entity subject, Entity object) {
		Relation result = new Relation(subject, object);
		
		if (StringUtils.testType(type, causalRelationTypes)) {
			result.addType("causal");
		}
		else if (StringUtils.testType(type, stateRelationTypes)) {
			result.addType("state");
		}
		else if (StringUtils.testType(type, actionRelationTypes)) {
			result.addType("action");
		}
		else if (StringUtils.testType(type, temporalRelationTypes)) {
			result.addType("temporal");
		}
		else if (type == "subgoal of") {
			result.addType("goal");
		}
		else {
			System.err.println("Type, " + type + ", provided to makeADRLRelation is not valid.");
			return null;
		}
		result.addType(type);
		return result;
	}
	
	public static boolean elementCheck(Set<Function> elements) {
		Iterator<Function> i = elements.iterator();
		while (i.hasNext()) {
			Function current = i.next();
			if (!StringUtils.testType(elementType(current), elementTypes)) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean relationCheck(Set<Relation> relations) {
		Iterator<Relation> i = relations.iterator();
		while (i.hasNext()) {
			Relation current = i.next();
			if (!StringUtils.testType(relationSupertype(current), relationSupertypes)) {
				return false;
			}
		}
		return true;
	}
	/******************************************************************************/
	
	private Graph scenario = ADRLFrame.makeNewScenario();
	
	public ADRLFrame (Entity t) {
		if (t.isA(ADRLFrame.FRAMETYPE)) {
			this.scenario = (Graph) scenario;
		}
	}
	
	public ADRLFrame (ADRLFrame f) {
		this((Graph) f.getThing().clone());
	}
	
	public Entity getThing() {
		return scenario;
	}
	
	public String toString() {
		return scenario.toString();
	}
}
