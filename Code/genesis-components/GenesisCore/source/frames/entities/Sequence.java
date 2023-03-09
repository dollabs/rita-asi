package frames.entities;

import java.util.*;
import java.util.stream.Stream;

import utils.logging.Logger;

/**
 * Implements Sequences.
 * 
 * @author Patrick Winston
 */

/*
 * Edited on 8 July 2013 by ahd
 */

@SuppressWarnings("serial")
public class Sequence extends Entity {

	public static String COMMUTATIVE = "commutativesequence";

	protected Vector<Entity> elements = new Vector<Entity>();

	// Key predicates, specified in interface.

	public boolean entityP() {
		return false;
	}

	public boolean entityP(String type) {
		return false;
	}

	public boolean functionP() {
		return false;
	}

	public boolean functionP(String type) {
		return false;
	}

	public boolean relationP() {
		return false;
	}

	public boolean relationP(String type) {
		return false;
	}

	public boolean relationP(List<String> types) {
		return false;
	}

	// public static String COMMUTATIVE = "commutativesequence";

	// private Vector<Entity> elements = new Vector<Entity>();

	// Type predicates

	public boolean sequenceP() {
		return true;
	}

	public boolean sequenceP(String type) {
		return this.isAPrimed(type);
	}

	public void addElement(Entity element) {
		saveState();
		elements.add(element);
		element.addElementOf(this);
		fireNotification();
	}

	public boolean removeElement(Entity element) {
		saveState();
		boolean b = elements.remove(element);
		if (b) {
			element.removeElementOf(this);
			fireNotification();
		}
		return b;
	}

	public Vector<Entity> getElements() {
		return elements;
	}

	public int size() {
		return getElements().size();
	}

	public Stream<Entity> stream() {
		return getElements().stream();
	}

	public Set<Entity> getChildren() {
		Set<Entity> result = new HashSet<Entity>();
		result.addAll(getElements());
		return result;
	}

	public int getNumberOfChildren() {
		return getElements().size();
	}

	public Entity getElement(int i) {
		return (Entity) elements.get(i);
	}

	/**
	 * End of key methods, specified in interface.
	 */

	public boolean isEqual(Object o) {
		if (o instanceof Sequence) {
			Sequence s = (Sequence) o;
			if (s.getElements().size() == this.getElements().size()) {
				Entity t1, t2;
				Vector<Entity> e1, e2;
				e1 = s.getElements();
				e2 = this.getElements();
				for (int i = 0; i < e1.size(); i++) {
					t1 = e1.get(i);
					t2 = e2.get(i);
					if (!(t1.isEqual(t2))) {
						return false;
					}
				}
				return super.isEqual(s);
			}
		}
		return false;
	}

	/**
	 * Constructs element-free sequence.
	 */
	public Sequence() {
		super();
	}

	public Sequence(Thread thread, Collection<Entity> elements) {
		this();
		this.elements.addAll(elements);
		setBundle(new Bundle(thread));
	}

	/**
	 * Constructs element-free sequence.
	 */
	public Sequence(String type) {
		this();
		addType(type);
	}

	public Sequence(Thread thread) {
		this();
		setBundle(new Bundle(new Thread(thread)));
	}

	public Sequence(Bundle b) {
		this();
		setBundle(b);
	}

	/**
	 * Constructs object with a name determined by suffix string provided; used only in reading. Adds new object
	 * instance to an instance list for later retrieval. Creates thread bundle.
	 */
	public Sequence(boolean readOnly, String suffix) {
		super(readOnly, suffix);
	}

	public Sequence(String label, List<Entity> elements) {
		this(label);
		for (Entity t : elements) {
			addElement(t);
		}
	}

	/**
	 * Adds an element to the sequence at the specified index. [Also fires a change of state. -- MAF.14.Jan.04]
	 */
	public void addElement(int index, Entity element) {
		saveState();
		elements.add(index, element);
		fireNotification();
	}

	/**
	 * Checks if an Element already exists in the Sequence Deprecated because of strange equal test.
	 */
	public boolean containsDeprecated(Entity element) {
		for (int j = 0; j < getNumberOfChildren(); j++) {
			if (getElement(j).isEqual(element)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if an Element already exists in the Sequence
	 */
	public boolean contains(Entity element) {
		return getElements().contains(element);
	}
	
	/**
     * Returns the index of the first occurrence of the specified Entity 
     * in this Sequence, or -1 if this sequence does not contain the Entity.
     */
    public int indexOf(Entity element) {
        return getElements().indexOf(element);
    }

	/**
	 * Set elements. [Also fires a change of state. -- MAF.14.Jan.04]
	 */
	public void setElements(Vector<Entity> v) {
		// Make sure all the elements are things; defensive programming
		for (int i = 0; i < v.size(); ++i) {
			if (!(v.elementAt(i) instanceof Entity)) {
				System.err.println("Oops, trying to put something into a sequence that is not an Entity instance!");
				return;
			}
		}
		saveState();
		elements = v;
		fireNotification();
	}

	/* Sets the element at a specific index. BL 06-8-1 */
	public void setElementAt(Entity elt, int index) {
		saveState();
		elements.setElementAt(elt, index);
		fireNotification();
	}

	/**
	 * Clears all elements from the sequence.
	 */
	public void clearElements() {
		elements.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see bridge.reps.entities.Frame#getAllComponents()
	 */
	public List<Entity> getAllComponents() {
		List<Entity> result = super.getAllComponents();
		result.addAll(getElements());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see bridge.reps.entities.Frame#getPrettyPrintType()
	 */
	public char getPrettyPrintType() {
		return TYPECHAR_SEQUENCE;
	}

	/**
	 * Helps produce print string.
	 */
	public String filler(boolean compact) {
		if (compact) {
			String result = "";
			result += "sequence (" + elements.size() + " elements)\n";
			for (int i = 0; i < elements.size(); i++) {
				String s = ((Entity) elements.elementAt(i)).toXML(compact);
				result += s.replaceFirst("\n", " (element " + i + ")\n") + "\n";
			}
			return result;
		}
		else {
			String result = "";
			for (int i = 0; i < elements.size(); ++i) {
				result += Tags.tag("element", ((Entity) elements.elementAt(i)).toXML(compact));
			}
			return Tags.tag("sequence", result);
		}
	}

	/**
	 * Helps produce print string without calling getName(); RLM -- why do we care about not calling getName()?
	 */
	public String fillerSansName(boolean compact) {
		String result = "";
		if (elements != null) {
			if (compact) {

				result += "sequence (" + elements.size() + " elements)\n";
				for (int i = 0; i < elements.size(); i++) {
					String s = ((Entity) elements.elementAt(i)).toXMLSansName(compact);
					result += s.replaceFirst("\n", " (element " + i + ")\n") + "\n";
				}
				return result;
			}
			else {
				for (int i = 0; i < elements.size(); ++i) {
					result += Tags.tag("element", ((Entity) elements.elementAt(i)).toXMLSansName(compact));
				}
				return Tags.tag("sequence", result);
			}
		}
		else {
			return result;
		}

	}

	/**
	 * Copies class and modifiers, leaves rest alone
	 */

	public Sequence rebuildWithoutElements() {
		Sequence result = new Sequence();
		this.transferThreadsFeaturesAndProperties(result);
		return result;
	}

	/**
	 * Clones.
	 */
	public Object clone(EntityFactory factory) {
		Sequence sequence = factory.newSequence();
		Bundle bundle = (Bundle) (getBundle().clone());
		sequence.setBundle(bundle);
		Vector<Entity> v = getElements();
		for (int i = 0; i < v.size(); ++i) {
			Entity t = v.elementAt(i);
			sequence.addElement(t);
		}

		v = getModifiers();
		for (int i = 0; i < v.size(); i++) {
			Entity t = (Entity) v.elementAt(i);
			sequence.addModifier(t);
		}

		return sequence;
	}

	/**
	 * Clones, recursively.
	 */
	protected Entity deepClone(EntityFactory factory, IdentityHashMap<Entity, Entity> cloneMap, boolean newId) {
		if (cloneMap.containsKey(this)) {
			return (Entity) cloneMap.get(this);
		}

		Sequence clone = factory.newSequence();
		if (!newId) {
			clone.setNameSuffix(getNameSuffix());
		}

		Bundle bundleClone = (Bundle) (getBundle().clone());
		clone.setBundle(bundleClone);
		Vector<Entity> elements = getElements();
		for (int i = 0; i < elements.size(); ++i) {
			Entity element = elements.elementAt(i);
			Entity elementClone = element.deepClone(factory, cloneMap, newId);
			clone.addElement(elementClone);
		}

		Vector<Entity> modifiers = getModifiers();
		for (int i = 0; i < modifiers.size(); i++) {
			Entity modifier = modifiers.elementAt(i);
			Entity modifierClone = modifier.deepClone(factory, cloneMap, newId);
			clone.addModifier(modifierClone);
		}

		cloneMap.put(this, clone);
		return clone;
	}

	/**
	 * Clones, recursively.
	 */
	public Entity cloneForResolver() {
		return cloneForResolver(EntityFactoryDefault.getInstance());
	}

	public Entity cloneForResolver(EntityFactory factory) {

		boolean cloneRelations = false;

		Sequence sequence = factory.newSequence();
		Bundle bundle = (Bundle) (getBundle().clone());
		sequence.setBundle(bundle);
		Vector<Entity> v = getElements();
		for (int i = 0; i < v.size(); ++i) {
			Entity t = v.elementAt(i);
			Entity clone = t.cloneForResolver(factory);
			sequence.addElement(clone);
		}

		v = getModifiers();
		for (int i = 0; i < v.size(); i++) {
			Entity t = (Entity) v.elementAt(i);
			Entity tClone = (Entity) t.cloneForResolver(factory);
			sequence.addModifier(tClone);
		}

		// MAF.11.19.2003
		if (cloneRelations) {
			// Clone relations
			Relation relation;
			Relation newRelation;

			// Clone subject relations
			Vector<Function> subjectRelations = new Vector<Function>();
			subjectRelations.addAll(getSubjectOf());
			for (Iterator<Function> i = subjectRelations.iterator(); i.hasNext();) {
				relation = (Relation) i.next();
				newRelation = factory.newRelation(sequence, relation.getObject());
				// newRelation.forget();
				bundle = (Bundle) (relation.getBundle().clone());
				newRelation.setBundle(bundle);
				fine("Cloning subjectOf() relation " + relation.getName() + " to " + newRelation.getName());
			}

			// Clone object relations
			Vector<Relation> objectRelations = new Vector<Relation>();
			objectRelations.addAll(getObjectOf());
			for (Iterator<Relation> i = objectRelations.iterator(); i.hasNext();) {
				relation = i.next();
				newRelation = factory.newRelation(relation.getSubject(), sequence);
				// newRelation.forget();
				bundle = (Bundle) (relation.getBundle().clone());
				newRelation.setBundle(bundle);
				fine("Cloning objectOf() relation " + relation.getName() + " to " + newRelation.getName());
			}
		}
		// sequence.forget();
		return sequence;
	}

	// Debugging section
	public static final String LOGGER_GROUP = "things";

	public static final String LOGGER_INSTANCE = "Sequence";

	public static final String LOGGER = LOGGER_GROUP + "." + LOGGER_INSTANCE;

	protected static void finest(Object s) {
		Logger.getLogger(LOGGER).finest(LOGGER_INSTANCE + ": " + s);
	}

	protected static void finer(Object s) {
		Logger.getLogger(LOGGER).finer(LOGGER_INSTANCE + ": " + s);
	}

	protected static void fine(Object s) {
		Logger.getLogger(LOGGER).fine(LOGGER_INSTANCE + ": " + s);
	}

	protected static void config(Object s) {
		Logger.getLogger(LOGGER).config(LOGGER_INSTANCE + ": " + s);
	}

	protected static void info(Object s) {
		Logger.getLogger(LOGGER).info(LOGGER_INSTANCE + ": " + s);
	}

	protected static void warning(Object s) {
		Logger.getLogger(LOGGER).warning(LOGGER_INSTANCE + ": " + s);
	}

	protected static void severe(Object s) {
		Logger.getLogger(LOGGER).severe(LOGGER_INSTANCE + ": " + s);
	}

	/**
	 * Tests behavior.
	 */
	public static void main(String argv[]) {
	}

	public void addAll(Sequence sequence) {
		Vector<Entity> v = sequence.getElements();
		for (Iterator<Entity> i = v.iterator(); i.hasNext();) {
			this.addElement(i.next());
		}

	}
}
