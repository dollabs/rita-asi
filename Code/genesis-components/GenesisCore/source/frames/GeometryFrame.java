package frames;
import java.util.HashMap;

import constants.RecognizedRepresentations;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.utilities.Utils;
import gui.GeometryViewer;
import utils.StringUtils;
/**
 * Frame for Talmy's spatial geometry representation.
 * 
 * @author blamothe
 */
public class GeometryFrame extends Frame {
	/* Examples */
	private static HashMap<String, Relation> map;
	public static HashMap<String, Relation> getMap() {
		if (GeometryFrame.map == null) {
			GeometryFrame.map = new HashMap<String, Relation>();
			GeometryFrame.map.put("The bike stood near the tree.", GeometryFrame.makeSchema(new Entity("bike"), "point",
					new Entity("tree"), "point", "locative"));
			GeometryFrame.map.put("The bike rolled along the walkway.", GeometryFrame.makeSchema(new Entity("bike"),
					"point", new Entity("walkway"), "line", "motional"));
			GeometryFrame.map.put("The bike stood along the fence.", GeometryFrame.makeSchema(new Entity("bike"),
					"line", new Entity("fence"), "line", "locative"));
			GeometryFrame.map.put("The board lay across the railway bed.", GeometryFrame.makeSchema(new Entity("board"),
					"line", new Entity("bed"), "plane", "locative"));
			GeometryFrame.map.put("The bird flew across the field.", GeometryFrame.makeSchema(new Entity("bird"),
					"point", new Entity("field"), "plane", "motional"));
			GeometryFrame.map.put("The trickle flowed along the ledge.", GeometryFrame.makeSchema(new Entity("trickle"),
					"line", new Entity("ledge"), "line", "motional"));
			GeometryFrame.map.put("The snake lay around the tree trunk.", GeometryFrame.makeSchema(new Entity("snake"),
					"line", new Entity("trunk"), "cylinder", "locative"));
			GeometryFrame.map.put("The tablecloth lay over the table.", GeometryFrame.makeSchema(
					new Entity("tablecloth"), "plane", new Entity("table"), "plane", "locative"));
			GeometryFrame.map.put("There was oil all along the ledge.", GeometryFrame.makeSchema(new Entity("oil"),
					"distributed", new Entity("ledge"), "line", "locative"));
			GeometryFrame.map.put("The water fell all over the floor.", GeometryFrame.makeSchema(new Entity("water"),
					"distributed", new Entity("floor"), "plane", "motional"));
			GeometryFrame.map.put("The scarecrow stood amidst the cornstalks.", GeometryFrame.makeSchema(new Entity(
					"scarecrow"), "point", new Entity("cornstalks"), "distributed", "locative"));
			GeometryFrame.map.put("The beetle crawled among the pebbles.", GeometryFrame.makeSchema(
					new Entity("beetle"), "point", new Entity("pebbles"), "aggregate", "motional"));
			GeometryFrame.map.put("The plane flew between the clouds.", GeometryFrame.makeSchema(new Entity("plane"),
					"point", new Entity("clouds"), "point-pair", "motional"));
			GeometryFrame.map.put("The boy ran into the bedroom.", GeometryFrame.makeSchema(new Entity("boy"), "point",
					new Entity("bedroom"), "enclosure", "motional"));
			GeometryFrame.map.put("The salmon swam through the water.", GeometryFrame.makeSchema(new Entity("salmon"),
					"point", new Entity("water"), "distributed", "motional"));
			GeometryFrame.map.put("The dye spread throughout the water.", GeometryFrame.makeSchema(new Entity("dye"),
					"distributed", new Entity("water"), "distributed", "motional"));
		}
		return GeometryFrame.map;
	}
	/* Static constansts to be used for type checking. */
	public static final String[] figureGeometries	= { "point", "line", "distributed" };
	public static final String[] groundMasses		= { "point", "point-pair", "point-set", "aggregate", "distributed" };
	public static final String[] groundSurfaces		= { "line", "tube", "enclosure", "plane", "cylinder" };
	public static final String[] groundGeometries	= Utils.arraycat(GeometryFrame.groundMasses, GeometryFrame.groundSurfaces);
	public static final String[] relationships		= { "motional", "locative" };
	public static final String	 FIGURETYPE			= "figureGeometry";
	public static final String	 GROUNDTYPE			= "groundGeometry";
	public static final String	 FRAMETYPE			= (String) RecognizedRepresentations.GEOMETRY_THING;
	/**
	 * Creates a figure or ground to be used in a geometry schema. Figures and grounds are derivitives 
	 * with the thing in question as the subject and the things geometry as a type on a features thread 
	 * in the derivative's bundle.
	 *
	 * @param t
	 * @param geometryType
	 * @param figure
	 */
	public static Function makeFigOrGround(Entity t, String geometryType, boolean figure) {
		if (figure) {
			if (StringUtils.testType(geometryType, GeometryFrame.figureGeometries)) {
				Function fig = new Function(GeometryFrame.FIGURETYPE, t);
				fig.addType(geometryType);
				return fig;
			}
			System.err.println("Sorry, " + geometryType + " is not a known figure geometry.");
			return null;
		} else {
			if (StringUtils.testType(geometryType, GeometryFrame.groundGeometries)) {
				Function ground = new Function(GeometryFrame.GROUNDTYPE, t);
				ground.addType(geometryType);
				return ground;
			}
			System.err.println("Sorry, " + geometryType + " is not a known ground geometry.");
			return null;
		}
	}
	public static String getGeometry(Function thing) {
		if (thing.isA(GeometryFrame.FIGURETYPE) || thing.isA(GeometryFrame.GROUNDTYPE)) {
			return thing.getType();
		} else {
			System.err.println("Sorry, " + thing + " is not a valid figure or ground.");
			return "";
		}
	}
	/**
	 * Creates a geometry schema--the thing representation of geometry frames. Schemas are relations
	 * with the figure as subject, the ground as object, and the relationship stored on a fetures thread
	 * in the schema's bundle.
	 * 
	 * @param fig
	 * @param figGeometry
	 * @param gro
	 * @param groundGeometry
	 * @param relationship
	 * @return
	 */
	public static Relation makeSchema(Entity fig, String figGeometry, Entity gro, String groundGeometry,
			String relationship) {
		if (StringUtils.testType(relationship, GeometryFrame.relationships)) {
			Function figure = GeometryFrame.makeFigOrGround(fig, figGeometry, true);
			Function ground = GeometryFrame.makeFigOrGround(gro, groundGeometry, false);
			Relation schema = new Relation(GeometryFrame.FRAMETYPE, figure, ground);
			schema.addFeature(relationship);
			return schema;
		}
		System.err.println("Sorry, " + relationship + " is not a known figure-ground relationship.");
		return null;
	}
	public static String getRelationship(Relation schema) {
		// Cannot think what  this does 9 June 2013
		return schema.getBundle().getThreadContaining("features").getType();
	}
	public static Function getFigure(Relation schema) {
		if (schema.isA(GeometryFrame.FRAMETYPE)) {
			return (Function) schema.getSubject();
		} else {
			System.err.println("Sorry, " + schema + " is not a valid Geometry schema.");
			return null;
		}
	}
	public static Function getGround(Relation schema) {
		if (schema.isA(GeometryFrame.FRAMETYPE)) {
			return (Function) schema.getObject();
		} else {
			System.err.println("Sorry, " + schema + " is not a valid Geometry schema.");
			return null;
		}
	}
	public static String getFigureGeometry(Relation schema) {
		return GeometryFrame.getGeometry(GeometryFrame.getFigure(schema));
	}
	public static String getGroundGeometry(Relation schema) {
		return GeometryFrame.getGeometry(GeometryFrame.getGround(schema));
	}
	//----------------------------------------------------------------//
	private Relation schema;
	public GeometryFrame(Entity t) {
		if (t.isA(GeometryFrame.FRAMETYPE)) {
			this.schema = (Relation) t;
		}
	}
	public GeometryFrame(GeometryFrame f) {
		this.schema = (Relation) f.getThing().clone();
	}
	public Entity getThing() {
		return this.schema;
	}
	public String toString() {
		return this.schema.toString();
	}
	public boolean isEqual(Object f) {
		if (f instanceof GeometryFrame) {
			return this.schema.isEqual(((GeometryFrame) f).getThing());
		}
		return false;
	}
	public static void main(String[] args) {
		HashMap<String, Relation> map = GeometryFrame.getMap();
		//System.out.println(PrettyPrint.prettyPrint(map.get("The plane flew between the clouds.")));
	}
/*	public WiredPanel getThingViewer() {
		return new GeometryViewer();
	}*/
}
