package frames;
import constants.RecognizedRepresentations;
import frames.entities.Entity;
import frames.entities.Relation;
import gui.BlockViewer;
import utils.StringUtils;
/**
 * Frame to represent blockage and containment.
 * 
 * @author blamothe
 */
public class BlockFrame extends Frame {
	public static String[]	blockTypes	= { "obstructs", "contains" };
	public static String[]	magTypes	= { "partial", "complete" };
	public static String	FRAMETYPE	= (String) RecognizedRepresentations.BLOCK_THING;
	/**
	 * Returns a block relation with the blocker as subject and the blocked thing as object. The block type is stored as
	 * a type on the thread of the returned block relation, and must be an element of BlockFrame.blockTypes.
	 * 
	 * @param blocker
	 * @param blockedThing
	 * @param blockType
	 * @return Relation
	 */
	public static Relation makeBlockRelation(Entity blocker, Entity blockedThing, String blockType, String mag) {
		if (!StringUtils.testType(blockType, BlockFrame.blockTypes)) {
			System.err.println("Sorry, " + blockType + " is not a valid block relation.");
			return null;
		}
		if (!StringUtils.testType(mag, BlockFrame.magTypes)) {
			System.err.println("Sorry, " + mag + " is not a valid blockage magnitude.");
			return null;
		}
		Relation result = new Relation(BlockFrame.FRAMETYPE, blocker, blockedThing);
		result.addType(blockType);
		result.addFeature(mag);
		return result;
	}
	public static void setBlockedThing(Relation blockRelation, Entity blockedThing) {
		if (blockRelation.isA(BlockFrame.FRAMETYPE)) {
			blockRelation.setObject(blockedThing);
			return;
		}
		System.err.println("Sorry, " + blockRelation + " is not a valid block relation.");
		return;
	}
	public static Entity getBlockedThing(Relation blockRelation) {
		if (blockRelation.isA(BlockFrame.FRAMETYPE)) {
			return blockRelation.getObject();
		}
		System.err.println("Sorry, " + blockRelation + " is not a valid block relation.");
		return null;
	}
	public static void setBlocker(Relation blockRelation, Entity blocker) {
		if (blockRelation.isA(BlockFrame.FRAMETYPE)) {
			blockRelation.setSubject(blocker);
			return;
		}
		System.err.println("Sorry, " + blockRelation + " is not a valid block relation.");
		return;
	}
	public static Entity getBlocker(Relation blockRelation) {
		if (blockRelation.isA(BlockFrame.FRAMETYPE)) {
			return blockRelation.getSubject();
		}
		System.err.println("Sorry, " + blockRelation + " is not a valid block relation.");
		return null;
	}
	public static String getBlockType(Relation blockRelation) {
		if (blockRelation.isA(BlockFrame.FRAMETYPE)) {
			return blockRelation.getType();
		}
		System.err.println("Sorry, " + blockRelation + " is not a valid block relation.");
		return "";
	}
	public static String getMag(Relation blockRelation) {
		if (blockRelation.isA(BlockFrame.FRAMETYPE)) {
			// Cannot think what this does 9 June 2013
			return blockRelation.getBundle().getThreadContaining("features").getType();
		}
		System.err.println("Sorry, " + blockRelation + " is not a valid block relation.");
		return "";
	}
	/**
	 * Returns both the magnitude of blockage and the type of blockage, separated by "--".
	 * 
	 * @param blockRelation
	 * @return String
	 */
	public static String getFullBlockType(Relation blockRelation) {
		if (blockRelation.isA(BlockFrame.FRAMETYPE)) {
			return BlockFrame.getMag(blockRelation) + "--" + BlockFrame.getBlockType(blockRelation);
		}
		System.err.println("Sorry, " + blockRelation + " is not a valid block relation.");
		return "";
	}
	// End static methods.
	private Relation	blockRelation;
	public BlockFrame(Entity t) {
		if (t.isA(BlockFrame.FRAMETYPE)) {
			if (t instanceof Relation) {
			this.blockRelation = (Relation) t;
			}
			else {
				System.err.println("BlockFrame construction handed thing, not relation: " + t);
			}
		}
	}
	public BlockFrame(BlockFrame f) {
		this.blockRelation = (Relation) f.getThing().clone();
	}
	@Override
	public Entity getThing() {
		return this.blockRelation;
	}
	public boolean isEqual(Object f) {
		if (f instanceof BlockFrame) {
			return this.blockRelation.isEqual(((BlockFrame) f).getThing());
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		if (this.blockRelation != null) {
			return this.blockRelation.toString();
		}
		return "";
	}
/*	@Override
	public WiredPanel getThingViewer() {
		return new BlockViewer();
	}*/
	public static void main(String[] args) {
		Relation rel = BlockFrame.makeBlockRelation(new Entity("airplane"), new Entity("man"), "contains", "complete");
		//System.out.println(PrettyPrint.prettyPrint(rel));
	}
}
