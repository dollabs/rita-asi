package frames.entities;

import java.util.List;
import java.util.Vector;

/**
 * Provides methods for manipulating Frames, which come in four types:
 * <ul>
 * <li>A Entity instance is an instance that belongs to various classes, which are implemented via a Thread memory (see
 * Vaina and Greenblatt, MIT AI Laboratory working paper 195). These are used, for example, to describe all the things
 * you can think about, from rocks and dogs to ideas and spirits.
 * <li>A Function instance is an Entity instance with the addition of a slot containing another Entity instance called
 * the subject of the Function. You can think of this as a unary relation, but it is probably easier to think in terms
 * of a predicate, the Function, and an argument, the subject. The existence of the Function relation indicates that the
 * predicate applied to the argument is true. Functions are used, for example, to describe places such as the place
 * "above the tree," which can be thought of as produced by the function <i>above</i> applied to the tree.
 * <li>A Relation instance is a Function instance with the addition of a second slot containing another Entity instance
 * called the object of the Relation. You can think of this as an ordinary binary relation between two Entities, or you
 * can think in terms of a predicate, the relation, and two arguments. These are used, for example, to describe
 * connections such as the connection between a dog and a path the dog runs along.
 * <li>A Sequence instance is an Entity with a slot containing a vector of Entities. These are used, for example, to
 * list the elements of a path, which might include its starting point, intermediate points, and end point.
 * </ul>
 * Each Frame has a Bundle, which is a collection of Threads which together
 * 
 * @author Patrick Winston
 */
public interface Frame {

	/**
	 * Determines if the object is an Entity.
	 */
	public boolean entityP();

	/**
	 * Determine if the object is an Entity, AND has the given type in its primal thread.
	 */
	public boolean entityP(String type);

	/**
	 * Determines if the object is a Function.
	 */
	public boolean functionP();

	/**
	 * Determine if the object is a Function, AND has the given type in its primal thread.
	 */
	public boolean functionP(String type);

	/**
	 * Determines if the object is a Relation.
	 */
	public boolean relationP();

	/**
	 * Determine if the object is a Relation, AND has the given type in its primal thread.
	 */
	public boolean relationP(String type);

	/**
	 * Determines if the object is a Sequence.
	 */
	public boolean sequenceP();

	/**
	 * Determine if the object is a Sequence, AND has the given type in its primal thread.
	 */
	public boolean sequenceP(String type);

	/**
	 * Determines if the object belongs to the specified class.
	 */
	public boolean isA(String className);

	/**
	 * Determines if the object belongs to the classes specified in the array.
	 */
	public boolean isAllOf(String[] className);

	/**
	 * Determines if the object does not belong to the classes specified in the array.
	 */
	public boolean isNoneOf(String[] className);

	/**
	 * Gets the most specific class on the object's primed thread. A thread is a vector of class names. The primed
	 * thread is the most recently created thread or some other thread identified as the primed thread.
	 */
	public String getType();

	/**
	 * Adds a type to the object's primed thread. This time is then considered the most specific type. See isA for
	 * discussion of primed thread.
	 */
	public void addType(String newClass);

	/**
	 * Adds a type to the first thread containing the specified class. Used, for example, to add to description threads
	 * of various sorts.
	 */
	public void addType(String newClass, String threadClass);

	/**
	 * Sets subject. Complains unless object is a Function or Relation.
	 */
	public void setSubject(Entity thing);

	/**
	 * Gets subject. Complains unless object is a Function or Relation.
	 */
	public Entity getSubject();

	/**
	 * Sets object. Complains unless object is a Relation.
	 */
	public void setObject(Entity thing);

	/**
	 * Gets object. Complains unless object is a Relation.
	 */
	public Entity getObject();

	/**
	 * Adds an element to a sequence. Complains unless object is a sequence.
	 */
	public void addElement(Entity element);

	/**
	 * Gets element in a sequence. Complains unless object is a sequence.
	 */
	public Vector<Entity> getElements();

	/**
	 * Returns a specific Entity from the element list.
	 * 
	 * @author M.A. Finlayson
	 * @since Sep 13, 2004; JDK 1.4.2
	 */
	public Entity getElement(int i);

	/**
	 * Gets thread headed by string provided.
	 */
	public Thread getThread(String firstElement);

	/**
	 * Returns an ordered list of all Entities that are subcomponents: - for a Entity: empty list - for a Function:
	 * subject - for a Relation: subject, object - for a Sequence: getElements()
	 */
	public List<Entity> getAllComponents();

	/**
	 * Returns the bundle.
	 */
	public Bundle getBundle();

	/**
	 * Returns a list of all modifiers. Modifiers are Frames that are not counted among the components of a thing. Any
	 * frame can have any number of modifiers.
	 */
	public Vector<Entity> getModifiers();

	public void addModifier(Entity modifier);

	public void addModifier(int index, Entity modifier);

	public void setModifiers(Vector<Entity> v);

	public void clearModifiers();

	/**
	 * @return a single character description of this thing type: - 'E' for entity - 'F' for function - 'R' for relation
	 *         - 'S' for sequence Other frame types may choose their own description character.
	 */
	public char getPrettyPrintType();
}