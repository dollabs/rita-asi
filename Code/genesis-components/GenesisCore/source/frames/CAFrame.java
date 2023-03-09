package frames;
import constants.RecognizedRepresentations;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.utilities.CAFactory;
import utils.groups.Graph;
/**
 * Frame for Chuck Rieger's Commonsense Algorithmic Knowledge representation.
 * @author blamothe
 */
public class CAFrame extends Frame {
	public Graph caGraph = new Graph(CAFactory.graphType);
	
	public static String FRAMETYPE = (String) RecognizedRepresentations.CA;
	public CAFrame() {
		super();
	}
	
	public CAFrame(Entity t) {
		this();
		if (t.isA(CAFrame.FRAMETYPE)) {
			this.caGraph = (Graph) t;
		}
	}
	
	public Graph getCAGraph() {
		return caGraph;
	}
	
	public Entity getThing() {
		return getCAGraph();
	}
	
	public boolean addEvent(String type, Entity e) {
		Function event = CAFactory.makeNewEvent(type, e);
		if (event != null) {
			return caGraph.addElt(event);
		}
		return false;
	}
	
	public boolean addEvent(Function e) {
		if (!e.isA(CAFactory.eventType)) {
			System.err.println("Sorry, argument supplied to CAFrame.addEvent was not a valid ca event.");
			return false;
		}
		return caGraph.addElt(e);
	}
	
	public boolean addRelation(String type, String subtype, Function subject, Function object) {
		return caGraph.addRel(CAFactory.makeNewLink(type, subtype, subject, object));
	}
	
	public boolean addRelation(String type, Function subject, Relation object) {
		if ((type != "gates") && (type != "concurrent") && (type != "motivates")) {
			System.err.println("Sorry, " + type + " is not a valid ca relation with relation object.");
			return false;
		}
		return caGraph.addRel(CAFactory.makeNewLink(type, "", subject, object));
	}
	
	public boolean addRelation(Relation r) {
		if (!r.isA(CAFactory.relationType)) {
			System.err.println("Sorry, argument supplied to CAFrame.addRelaiton was not a valid ca relation.");
			return false;
		}
		return caGraph.addRel(r);
	}
	public String toString() {
		return caGraph.toString();
	}
}
