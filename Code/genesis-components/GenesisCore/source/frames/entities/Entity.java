package frames.entities;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

import utils.*;
import constants.Markers;
import dictionary.BundleGenerator;
import frames.memories.MemoryForget;
import generator.Generator;
import start.Start;
import translator.Translator;
import utils.ArrayUtils;
import utils.Mark;
import utils.StringUtils;
import utils.collections.IdentityHashSet;
import utils.logging.Logger;
import utils.tools.Predicates;

/*
 * Edited on 8 July 2013 by ahd
 */

/**
 * See Frame interface for API.
 * <p>
 * Printing of Entity instances is handled by static methods in the Tags class, which provides for human-readable,
 * XML-style printing. See Tags documentation for an example.
 * <p>
 * Execute main to see printed examples.
 * 
 * @author Patrick Winston
 */
@SuppressWarnings("serial")
public class Entity extends Observable implements Serializable, Frame {

	// *********************************************
	// Static fields for the class
	// *********************************************

	/**
	 * Determines whether the default toXML for Entities is compact True is compact form, false is XML.
	 */
	public static final boolean defaultToXMLIsCompact = false;

	public static final Object SAVE_STATE = new Object();

	public static final Object FIRE_NOTIFICATION = new Object();

	public static String pathElement = "pathElement";

	public static String[] placeList = { "above", "at", "below", "under", "farFrom", "in", "near", "nextTo", "rightOf", "leftOf", "on", "over", "by",
	        "top", "bottom" };

	public static String[] pathList = { "to", "from", "toward", "awayFrom", "down", "up", "via" };

	public static String[] roleList = { "with", "by", "for" };

	public static String[] changeList = { "increase", "decrease", "change", "appear", "disappear", "notIncrease", "notDecrease", "notChange",
	        "notAppear", "notDisappear", "blank" };

	public static final char TYPECHAR_ENTITY = 'E';

	public static final char TYPECHAR_FUNCTION = 'F';

	public static final char TYPECHAR_RELATION = 'R';

	public static final char TYPECHAR_SEQUENCE = 'S';

	public static final String MARKER_ENTITY = "thing";

	public static final String MARKER_ACTION = "action";

	public static final String MARKER_DESCRIPTION = "description";

	public static final String MARKER_OWNERS = "owners";

	public static final String MARKER_FEATURE = "feature";

	public static final String MARKER_DETERMINER = "determiner";

	public static final String MARKER_WORD = "word";

	public static final String MARKER_COMPLETE = "complete";

	// *********************************************
	// Entity state variables
	// *********************************************

	protected String name = null;

	protected String nameSuffix = null;

	protected Bundle bundle;

	protected Vector<Entity> modifiers = new Vector<Entity>();

	protected Vector<Function> subjectOf = new Vector<Function>();

	protected Vector<Relation> objectOf = new Vector<Relation>();

	protected Vector<Entity> elementOf = new Vector<Entity>();

	protected Vector<Entity> exampleOf = new Vector<Entity>();

	protected Vector<Observer> observers = new Vector<Observer>();

	// *********************************************
	// Identification methods
	// *********************************************

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

	public boolean sequenceP() {
		return false;
	}

	public boolean sequenceP(String type) {
		return false;
	}

	public boolean featureP() {
		return false;
	}

	// *********************************************
	// Constructors
	// *********************************************

	/**
	 * Constructs object with a unique name. Adds new object instance to an instance list for later retrieval. Creates
	 * thread bundle.
	 */
	public Entity() {
		setNameSuffix(NameGenerator.getNewName());
		setBundle(new Bundle());
		addType("thing");
	}

	/**
	 * Creates an entity, with a type added to its primed thread.
	 */
	public Entity(String type) {
		this();
		addType(type);
	}

	/**
	 * Creates an Entity, with a given thread
	 */

	public Entity(Thread t) {
		setNameSuffix(NameGenerator.getNewName());
		setBundle(new Bundle(t));
	}

	/**
	 * Creates an entity, with a given thread
	 */

	public Entity(Bundle b) {
		setNameSuffix(NameGenerator.getNewName());
		setBundle(b);
	}

	/**
	 * Constructs object with a name determined by suffix string provided; used only in reading. Adds new object
	 * instance to an instance list for later retrieval. Creates thread bundle.
	 */
	public Entity(boolean readOnly, String suffix) {
		if (!readOnly) {
			warning("Any boolean argument of an Entity is supposed to be true!");
		}
		setBundle(new Bundle());
		addType("thing");
		setNameSuffix(suffix);
	}

	// *********************************************
	// Identification methods
	// *********************************************

	public boolean entityP() {
		return true;
	}

	public boolean entityP(String type) {
		return this.isAPrimed(type);
	}

	// *********************************************
	// State methods
	// *********************************************

	public void setName(String n) {
		name = n;
	}

	/**
	 * Sets suffix, a number, which is combined with type to produce a name, such as Bird-27.
	 */
	public void setNameSuffix(String suffix) {
		nameSuffix = suffix;
	}

	/**
	 * Gets suffix, a string representing a number preceded by a hyphen, which is combined with type to produce a name,
	 * such as Bird-27.
	 */
	public String getNameSuffix() {
		return nameSuffix;
	}

	/**
	 * Gets suffix, the identifier. Same as getNameSuffix, but public.
	 */
	public String getIdentifier() {
		return nameSuffix.substring(1);
	}

	public int getID() {
		return (new Integer(getIdentifier()).intValue());
	}

	/**
	 * Helps read printed string.
	 */
	public String getName() {
		if (name != null) {
			return name;
		}
		return getType() + nameSuffix;
	}

	/**
	 * Returns the name that has been explicitly set, or Null if this Entity is using an implicit name.
	 */
	public String getExplicitName() {
		return name;
	}

	// *********************************************
	// Property lists
	// *********************************************

	/**
	 * Work on property list follows Greenblatt and Vaina, implemented 8 July 2013
	 * <p>
	 * name is always a string, but object may be, for example, an entity or something, such as in She hated John's
	 * words, which would give words an owner property with the John entity as the value.
	 * 
	 * @param name
	 * @param value
	 */

	private Vector<LabelValuePair> propertyList;

	public Vector<String> getKeys() {
		Vector<String> keys = new Vector<String>();
		for (LabelValuePair lv : getPropertyList()) {
			keys.add(lv.getLabel());
		}
		return keys;
	}

	public Vector<LabelValuePair> getPropertyList() {
		if (propertyList == null) {
			propertyList = new Vector<LabelValuePair>();
		}
		return propertyList;
	}

	public void setPropertyList(Vector<LabelValuePair> propertyList) {
		this.propertyList = propertyList;
	}

	public Vector<LabelValuePair> clonePropertyList() {
		Vector<LabelValuePair> clone = new Vector<LabelValuePair>();
		for (LabelValuePair pair : getPropertyList()) {
			clone.add(clonePair(pair));
		}
		return clone;
	}

	private LabelValuePair clonePair(LabelValuePair pair) {
		return new LabelValuePair(pair.getLabel(), pair.getValue(), pair.isIdentifier());
	}
	
	/**
	 * Adds a property to this entity. If identifier is true, this property is taken into account (used as an additional
     * hash key) during dereferencing so it's distinguished from other equivalent entities without this property. 
     * Regardless of identifier value, this property is NOT used in .equals(), .hashCode(), or other equality comparisons
     * 
     * introduced by 2016bmw 3.23.2017
     * 
     * Will NOT add duplicate properties, so this method can be used to change identifier value.
     * 
	 * @param label the label for this property
	 * @param object the value for this property
	 * @param identifier whether property identifies this entity vs. other equivalent entities without this
	 * property; i.e. whether this property is important to the identity of this entity
	 *
	 */
	public void addProperty(String label, Object object, boolean identifier) {
	    for (LabelValuePair pair : getPropertyList()) {
	        if (pair.getLabel().equals(label)) {
	            pair.setValue(object);
	            pair.setIdentifier(identifier);
	            return;
	        }
	    }
	    getPropertyList().add(new LabelValuePair(label, object, identifier));
	}

	public void addProperty(String label, Object object) {
	    // by default, property is not an identifier unless this is an entity (not subtype)
	    // and label is OWNER (agrees with legacy behavior)
	    boolean identifier = this.entityP() && Markers.OWNER_MARKER.equals(label);
	    addProperty(label, object, identifier);
	}

	public Object getProperty(String label) {
		for (LabelValuePair pair : getPropertyList()) {
			if (pair.getLabel().equals(label)) {
				return pair.getValue();
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param label property label
	 * @return whether property identifies this entity vs. other equivalent entities without this
     * property; i.e. whether this property is important to the identity of this entity. If identifier is true, 
     * this property is taken into account (used as an additional hash key) during dereferencing so it's distinguished 
     * from other equivalent entities without this property. Regardless of identifier value, this property is NOT used 
     * in .equals(), .hashCode(), or other equality comparisons.
     * 
     * Returns false if this property label is not set.
     * 
     * introduced by 2016bmw 3.23.2017
	 */
	public boolean isPropertyIdentifier(String label) {
	    return getPropertyList().stream()
	            .filter(pair -> pair.getLabel().equals("label"))
	            .map(LabelValuePair::isIdentifier)
	            .findFirst()
	            .orElse(false);
	}

	public Object getIntegerProperty(String label) {
		for (LabelValuePair pair : getPropertyList()) {
			if (pair.getLabel().equals(label)) {
				return (int) pair.getValue();
			}
		}
		return -1;
	}

	public boolean getBooleanProperty(String label) {
		for (LabelValuePair pair : getPropertyList()) {
			if (pair.getLabel().equals(label)) {
				return (boolean) (pair.getValue());
			}
		}
		return false;
	}

	public void addProperName(String name) {
		addProperty(Markers.PROPER, name);
	}

	public String getProperName() {
		return (String) (getProperty(Markers.PROPER));
	}

	/**
	 * Syntactic sugar
	 */
	public void addProbability(Object object) {
		Number number = null;
		if (object instanceof String) {
			number = Double.parseDouble((String) object);
		}
		else if (object instanceof Number) {
			number = (Number) object;
		}
		if (number != null) {
			addProperty(Markers.PROBABILITY, number);
		}
		else {
			Mark.err("Tried to add a probability property that is not a number");
		}
	}

	public Number getProbability() {
		Number result = (Number) getProperty(Markers.PROBABILITY);
		if (result == null && Predicates.isCause(this)) {
			result = (Number) getObject().getProperty(Markers.PROBABILITY);
		}
		// Mark.say("getProbability returns", result, "for", this);
		return result;
	}

	/**
	 * Don't care what the value is, just get rid of it
	 * 
	 * @param label
	 * @return
	 */

	public void removeProperty(String label) {
		LabelValuePair remove = null;
		for (LabelValuePair pair : getPropertyList()) {
			if (pair.getLabel().equals(label)) {
				remove = pair;
				break;
			}
		}
		if (remove != null) {
			getPropertyList().remove(remove);
		}
	}

	/**
	 * Don't care what the value is, just whether the property has been set
	 */
	public boolean hasProperty(String label) {
		if (getProperty(label) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Do care what the value is, both key and value must match
	 */
	public boolean hasProperty(String label, Object value) {
		Object v = getProperty(label);
		if (v instanceof String) {
			return v.equals(value);
		}
		else
			return v == value;
	}

	public static String OWNER = "owner";

	public static String PROPERTY = "property";

	public static class LabelValuePair {

		private String label;

		private Object value;
		
		// if a property is an identifier, this property is taken into account (used as an additional
		// hash key) during dereferencing. See LabelValuePair(String, Object, boolean), LabelValuePair.isIdentifier(), 
		// Entity.addFeature(Object, boolean) and 
		// Entity.addProperty(String, Object, boolean) for more details
		private boolean identifier;

		public void setValue(Object value) {
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public Object getValue() {
			return value;
		}
		
		/**
		 * @return if this pair is an entity's property, 
         * whether this property is taken into account (used as an additional hash key) during dereferencing 
         * so it's distinguished from other equivalent entities without this property. 
		 */
		public boolean isIdentifier() {
		    return identifier;
		}
		
		public void setIdentifier(boolean identifier) {
		    this.identifier = identifier;
		}

		public LabelValuePair(String label, Object value) {
		    this(label, value, false);
		}
		
        /**
         * Creates a LabelValuePair. If identifier is true and this pair is an entity's property, 
         * this property is taken into account (used as an additional hash key) during dereferencing 
         * so it's distinguished from other equivalent entities without this property. 
         * Regardless of identifier value, this property is NOT used in .equals(), .hashCode(), or other equality comparisons
         * 
         * introduced by 2016bmw 3.28.2017
         * 
         * @param label the label for this pair
         * @param object the value for this pair
         * @param identifier if this pair is an entity's property, whether property identifies this entity 
         * vs. other equivalent entities without this property; i.e. whether this property is important 
         * to the identity of this entity
         *
         */
		public LabelValuePair(String label, Object value, boolean identifier) {
			this.label = label;
			this.value = value;
			this.identifier = identifier;
		}

		public LabelValuePair clone() {
			return new LabelValuePair(label, value, identifier);
		}

		public String toXML() {
			return "<" + label + ", " + value.toString() + ">";
		}

		public String toString() {
			return "<" + label + ", " + value + ">";
		}

		public boolean equals(LabelValuePair that) {
			if (label.equals(that.getLabel())) {
				if (value instanceof String && value.equals(that.getValue())) {
					return true;
				}
				else if (value == that.getValue()) {
					return true;
				}
			}
			return false;
		}

	}

	// *********************************************
	// Features, viewed as properties that can have multiple values
	// *********************************************

	// public void setQuantity(Object amount) {
	// for (LabelValuePair pair : getPropertyList()) {
	// if (pair.getLabel().equals(Markers.QUANTITY) && pair.getValue().equals(amount)){
	// return;
	// }
	// else if (pair.getLabel().equals(Markers.QUANTITY)){
	// pair.setValue(amount);
	// return;
	// }
	// }
	// getPropertyList().add(new LabelValuePair(Markers.QUANTITY, amount));
	// }
	//
	// public Object getQuantity() {
	// for (LabelValuePair pair : getPropertyList()) {
	// if (pair.getLabel().equals(Markers.QUANTITY)) {
	// return pair.getValue();
	// }
	// }
	// }

   /**
     * Adds a feature to this entity. If identifier is true, this feature is taken into account 
     * (used as an additional hash key) during dereferencing so it's distinguished from other equivalent 
     * entities without this feature. Regardless of identifier value, this feature is NOT used 
     * in .equals(), .hashCode(), or other equality comparisons 
     * 
     * Will NOT add duplicate features, so this method can be used to change identifier value.
     * 
     * CAUTION: an Entity's features affect .toEnglish() generation
     * 
     * introduced by 2016bmw 3.23.2017
     * 
     * @param object the value for this feature
     * @param identifier whether property identifies this feature vs. other equivalent entities without this
     * feature; i.e. whether this feature is important to the identity of this entity
     *
     */
	public void addFeature(Object object, boolean identifier) {
		for (LabelValuePair pair : getPropertyList()) {
			if (pair.getLabel().equals(Markers.FEATURE) && pair.getValue().equals(object)) {
			    pair.setIdentifier(identifier);
				return;
			}
		}
		// Add another property with same label
		getPropertyList().add(new LabelValuePair(Markers.FEATURE, object, identifier));

	}
	
	// Does not add duplicate features
	public void addFeature(Object object) {
	    // by default, feature is not an identifier unless this is an entity and not one of the entity subtypes
	    // (matches legacy behavior)
	    boolean identifier = this.entityP();
	    addFeature(object, identifier);
	}
	
	public boolean removeFeature(Object t) {
		ArrayList<LabelValuePair> clone = new ArrayList<LabelValuePair>();
		clone.addAll(getPropertyList());
		for (LabelValuePair pair : clone) {
			if (pair.getLabel().equals(Markers.FEATURE) && pair.getValue().equals(t)) {
				// remove
				getPropertyList().remove(pair);
			}
		}
		return false;
	}

	public boolean hasFeature(Object object) {
		for (LabelValuePair pair : getPropertyList()) {
			if (pair.getLabel().equals(Markers.FEATURE) && pair.getValue().equals(object)) {
				// Got it
				return true;
			}
		}
		return false;
	}
	
	/**
     * 
     * @param object feature value
     * @return whether feature identifies this entity vs. other equivalent entities without this
     * feature; i.e. whether this feature is important to the identity of this entity. If identifier is true, 
     * this feature is taken into account (used as an additional hash key) during dereferencing so it's distinguished 
     * from other equivalent entities without this property. Regardless of identifier value, this property is NOT used 
     * in .equals(), .hashCode(), or other equality comparisons
     * 
     * Returns false if this feature value does not exist.
     *      
     * introduced by 2016bmw 3.23.2017
     */
    public boolean isFeatureIdentifier(Object object) {
        return getPropertyList().stream()
                .filter(pair -> pair.getLabel().equals(Markers.FEATURE) && pair.getValue().equals(object))
                .map(LabelValuePair::isIdentifier)
                .findFirst()
                .orElse(false);
    }

	// public Thread getFeatures() {
	// for (int i = 0; i < bundle.size(); ++i) {
	// Thread thread = bundle.elementAt(i);
	// if (thread.contains(MARKER_FEATURE)) {
	// return thread;
	// }
	// }
	// return null;
	// }

	public ArrayList<Object> getFeatures() {
		ArrayList<Object> result = new ArrayList<Object>();
		for (LabelValuePair pair : getPropertyList()) {
			if (pair.getLabel().equals(Markers.FEATURE)) {
				// Add
				result.add(pair.getValue());
			}
		}
		return result;
	}

	// *********************************************
	// Bundle manipulation
	// *********************************************

	/**
	 * Gets object's thread bundle.
	 */
	public Bundle getBundle() {
		if (bundle == null) {
			bundle = new Bundle();
		}
		return bundle;
	}

	/**
	 * Sets object's thread bundle. [Makes sure that the previous thread bundle gets deregistered properly. --
	 * MAF.13.Jan.04]
	 */
	public void setBundle(Bundle b) {
		if (bundle == b) {
			return;
		}
		saveState();
		if (bundle != null) {
			bundle.setOwnerThingNull();
		}

		bundle = b;

		if (bundle != null) {
			bundle.setOwnerThing(this);
		}
		fireNotification();
	}

	/**
	 * Adds a new thread to thread bundle.
	 */
	public void addThread(Thread t) {
		bundle.addThread(t);
	}

	public void replacePrimedThread(Thread t) {
		if (bundle.size() != 0) {
			bundle.remove(0);
		}
		bundle.add(0, t);
	}

	/**
	 * Gets primed thread from bundle of threads.
	 */
	public Thread getPrimedThread() {
		return getBundle().getPrimedThread();
	}

	/**
	 * Sets primed thread in bundle of threads.
	 */
	public void setPrimedThread(Thread t) {
		replacePrimedThread(t);
	}

	public Thread getThread(String firstElement) {
		return getBundle().getThread(firstElement);
	}

	public Thread getThreadWith(String element) {
		for (Thread t : getBundle()) {
			if (t.contains(element)) {
				return t;
			}
		}
		return null;
	}

	public Thread getThreadWith(String first, String last) {
		for (Thread t : getBundle()) {
			if (t.firstElement().equals(first) && t.lastElement().equals(last)) {
				return t;
			}
		}
		return null;
	}

	/**
	 * Swaps primed with next thread in the bundle of threads
	 */
	public void swapPrimedThread() {
		getBundle().swapPrimedThread();
	}

	/**
	 * Makes a copy of the primed thread, and makes that copy the primed thread.
	 */
	public void pushPrimedThread() {
		getBundle().pushPrimedThread();
	}

	/**
	 * Sends primed thread to end of the thread bundle.
	 */
	public void sendPrimedThreadToEnd() {
		getBundle().sendPrimedThreadToEnd();
	}

	// *********************************************
	// Type manipulation
	// *********************************************

	private String cached_type = null;

	public String getCachedType() {
		if (cached_type == null) {
			cached_type = getType();
		}
		return cached_type;
	}

	public String getType() {
		String properName = getProperName();
		if (properName != null) {
			// Mark.say("Type returning proper name", properName);
			return properName;
		}
		if (bundle == null) {
			Mark.err("Entity has no bundle!");
			throw (new RuntimeException("What the hell!!!!"));
			// return "Entity with no type encountered!!!!!";

		}
		return bundle.getType();
	}

	/**
	 * A bit of a hack. Works only on primed thread and retains only primed thread. If name indicated, skips over that.
	 * 
	 * @author PHW
	 */
	public Entity geneneralize(Entity x) {
		Bundle b = x.getBundle();
		Thread t = b.firstElement();
		// Trim
		int l = t.size();
		if (l > 0) {
			t.remove(l - 1);
		}
		// Get final element
		--l;
		if (l > 0) {
			String f = t.get(l - 1);
			// If "name" get rid of it too
			if (Markers.NAME.equals(f)) {
				t.remove(l - 1);
			}
		}
		// Now construct new entity
		return new Entity(t);
	}

	/**
	 * Returns supertype of the Entity.
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 8, 2004; JDK 1.4.2
	 */
	public String getSupertype() {
		return bundle.getSupertype();
	}

	public void addType(String t) {
		Thread thread = getPrimedThread();
		thread.addType(t);
	}

	public void addDeterminer(String t) {
		addType(t, MARKER_DETERMINER);
	}

	public Thread getDeterminer() {
		for (int i = 0; i < bundle.size(); ++i) {
			Thread thread = bundle.elementAt(i);
			if (thread.contains(MARKER_DETERMINER)) {
				return thread;
			}
		}
		return null;
	}

	public void addType(String type, String threadType) {
		for (int i = 0; i < bundle.size(); ++i) {
			Thread thread = (Thread) (bundle.elementAt(i));
			// null check drip pan
			if (thread != null && thread.contains(threadType)) {
				thread.addType(type);
				return;
			}
		}
		Thread thread = new Thread();
		thread.addType(threadType);
		thread.addType(type);
		bundle.addThreadAtEnd(thread);
	}

	/**
	 * Adds all types given in order given
	 * 
	 * @author PHW
	 * @since 4 January 2015
	 */
	public void addTypes(String... strings) {
		Arrays.asList(strings).stream().forEachOrdered(s -> addType(s));
	}

	/**
	 * Adds all types separated by one space in types parameter
	 * 
	 * @author M.A. Finlayson
	 * @since Jun 28, 2004; JDK 1.4.2
	 */
	public void addTypes(String threadType, String types) {
		int start = 0;
		int end;
		types = types.trim();
		while ((end = types.indexOf(' ', start + 1)) > -1) {
			addType(types.substring(start, end), threadType);
			start = end + 1;
		}
		addType(types.substring(start, types.length()), threadType);
	}

	/**
	 * Asserts that object belongs to a type via thread bundle, but first checks to be sure type is allowed, that is, in
	 * the array. Obsolete?
	 */
	public void addType(String t, String[] ok) {
		if (StringUtils.testType(t, ok)) {
			addType(t);
		}
		else {
			System.err.println("Tried to add unacceptible type " + t);
		}
	}

	/**
	 * Asserts that object belongs to all types in a vector via thread bundle. First, creates a new primed thread.
	 */
	public void addTypes(Vector<?> v) {
		getBundle().pushPrimedThread();
		for (int i = 0; i < v.size(); ++i) {
			Object o = v.elementAt(i);
			if (o instanceof String) {
				addType((String) o);
			}
			else {
				System.err.println("Tryed to add a type that is not a string");
			}
		}
	}

	public boolean removeType(String type) {
		boolean result = false;
		for (int i = 0; i < bundle.size(); ++i) {
			Thread thread = (Thread) (bundle.elementAt(i));
			if (thread.contains(type)) {
				thread.remove(type);
				result = true;
			}
		}
		return result;
	}

	/**
	 * Forces a type, but first clones primed thread.
	 */
	public void imagineType(String t) {
		getBundle().pushPrimedThread();
		addType(t);
	}

	/**
	 * Gets a vector of all types on the primed thread. Most general is first.
	 */
	public Vector<String> getTypes() {
		Vector<String> v = new Vector<String>();
		// Check Threads
		if (bundle.size() == 0) {
			return v;
		}
		Thread thread = (Thread) (bundle.elementAt(0));
		for (int j = 0; j < thread.size(); ++j) {
			String s = (String) (thread.elementAt(j));
			if (!v.contains(s)) {
				v.add(s);
			}
		}
		return v;
	}

	/**
	 * Gets a vector of all types. Most general is first.
	 */
	public Vector<String> getAllTypes() {
		Vector<String> v = new Vector<String>();
		// Check Threads
		for (int i = 0; i < bundle.size(); ++i) {
			Thread thread = (Thread) (bundle.elementAt(i));
			if (thread.size() > 0) {
				for (int j = 0; j < thread.size(); ++j) {
					String s = (String) (thread.elementAt(j));
					if (!v.contains(s)) {
						v.add(s);
					}
				}
			}
		}
		return v;
	}

	/**
	 * Gets a vector of all types that the object belongs to, except for feature and tracer types. Most general is
	 * first. Uses the object's threads, but not features thread. Discovered that this method did not do what it said it
	 * did...i.e., ignore features thread. So I made it match its spec. --MAF.20.Feb.04
	 */
	public Set<String> getAllTypesForFindMatchingThing() {
		String[] ignore = { "features", "tracers" };
		return getAllTypesExcept(ignore);
	}

	public Set<String> getAllTypesExcept(String[] ignoreTheseThreads) {
		Set<String> result = new HashSet<String>();
		result.addAll(Arrays.asList(bundle.getAllTypesExcept(ignoreTheseThreads)));
		return result;
	}

	/**
	 * Gets a vector of all types that the object belongs too that seem relevant to matching. Most general is first.
	 * Uses only the object's "thing" and "description" threads.
	 */
	public Vector<String> getMatcherTypes() {
		Vector<String> v = new Vector<String>();
		// Check Threads
		for (int i = 0; i < bundle.size(); ++i) {
			Thread thread = (Thread) (bundle.elementAt(i));
			if (thread.size() > 0) {
				String top = (String) (thread.elementAt(0));
				if (top.equalsIgnoreCase("thing") || top.equalsIgnoreCase("description")) {
					for (int j = 0; j < thread.size(); ++j) {
						String s = (String) (thread.elementAt(j));
						if (!v.contains(s)) {
							v.add(s);
						}
					}
				}
			}
		}
		return v;
	}

	// *********************************************
	// Type testing
	// *********************************************

	public boolean isA(String s) {
		if (bundle == null) {
			return false;
		}
		if (bundle.size() == 0) {
			return false;
		}
		for (int i = 0; i < bundle.size(); ++i) {
			Thread thread = (Thread) (bundle.elementAt(i));
			if (thread.contains(s)) {
				return true;
			}
		}
		return false;
	}

	public boolean isA(String type, String ignoreThread) {
		String[] ignoreThreads = { ignoreThread };
		return isA(type, ignoreThreads);
	}

	public boolean isA(List<String> actions) {
		if (actions.isEmpty()) {
			return true;
		}
		for (Iterator<String> i = actions.iterator(); i.hasNext();) {

			try {
				if (isA(i.next())) {
					return true;
				}
			}
			catch (RuntimeException e) {
				System.err.println(actions);
				e.printStackTrace();
			}
		}
		return false;
	}

	public boolean isAPrimedAction(String type) {
		Bundle bundle = this.getBundle();
		for (Thread thread : bundle) {
			if (thread.isEmpty() || !thread.firstElement().equals("action")) {
				continue;
			}
			else {
				return thread.contains(type);
			}
		}
		return false;
	}

	public boolean isAPrimed(String type) {
		try {
			if (getPrimedThread().contains(type)) {
				return true;
			}
		}
		catch (Exception e) {
			Mark.say("Exception in Entity.isAPrimed", type);
			// e.printStackTrace();
		}
		return false;
	}

	public boolean isAPrimed(List<String> list) {
		if (list.isEmpty()) {
			return true;
		}
		for (Iterator<String> i = list.iterator(); i.hasNext();) {

			try {
				String test = i.next();
				if (isAPrimed(test)) {
					return true;
				}
			}
			catch (RuntimeException e) {
				System.err.println(list);
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * Returns true if a thread (which cannot be of a thread type listed in ignoreThreads) contains the specified type.
	 * 
	 * @author M.A. Finlayson
	 * @since Aug 20, 2004; JDK 1.4.2
	 */
	public boolean isA(String type, String[] ignoreThreads) {
		if (bundle.size() == 0) {
			return false;
		}
		for (int i = 0; i < bundle.size(); ++i) {
			Thread thread = (Thread) (bundle.elementAt(i));
			if (!ArrayUtils.contains(ignoreThreads, thread.getThreadType())) {
				if (thread.contains(type)) {
					// info("Found '" + type + "' on thread: " +
					// thread.toString());
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Returns true if ALL threads for this entity contain the specified type
	 * Returns false is this entity does not have any threads
	 * @author 2016bmw
	 */
	public boolean isAlwaysA(String type) {
	    int bundleSize = bundle.size();
	    if (bundleSize == 0) {
	        return false;
	    }
	    for (int i = 0; i < bundleSize; i++) {
	        Thread curThread = bundle.get(i);
	        if (!curThread.contains(type)) {
	            return false;
	        }
	    }
	    return true;
	}

	/**
	 * Returns true only if the Entity is not a member of the specified class.
	 * 
	 * @author M.A. Finlayson
	 * @since Dec 5, 2003
	 */
	public boolean isNotA(String s) {
		if (isA(s)) {
			return false;
		}
		return true;
	}

	public boolean isNotAPrimed(String type) {
		if (getPrimedThread().contains(type)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns true if the Entity is a member of any of the specified classes.
	 * 
	 * @author M.A. Finlayson
	 * @since Aug 19, 2004; JDK 1.4.2
	 */
	public boolean isAnyOf(String[] s) {
		return !isNoneOf(s);
	}

	/**
	 * Returns true if the Entity is any of the specified classes, ignore the specified threads.
	 */
	public boolean isAnyOf(String[] types, String ignoreThread) {
		String[] ignoreThreads = { ignoreThread };
		return isAnyOf(types, ignoreThreads);
	}

	/**
	 * Returns true if the Entity is any of the specified classes, ignoring the specified threads.
	 */
	public boolean isAnyOf(String[] types, String[] ignoreThreads) {
		for (int i = 0; i < types.length; i++) {
			if (isA(types[i], ignoreThreads)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the Entity is a member of all the specified classes.
	 * 
	 * @author M.A. Finlayson
	 * @since Aug 19, 2004; JDK 1.4.2
	 * @see frames.entities.Frame#isAllOf(java.lang.String[]) isA
	 */

	public boolean isAllOf(String[] s) {
		for (int i = 0; i < s.length; ++i) {
			if (!isA(s[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns true only if the Entity is not a member of any of the specified classes.
	 * 
	 * @author M.A. Finlayson
	 * @since Aug 19, 2004; JDK 1.4.2
	 * @see frames.entities.Frame#isNoneOf(java.lang.String[]) isNotA
	 */

	public boolean isNoneOf(String[] s) {
		for (int i = 0; i < s.length; ++i) {
			if (isA(s[i])) {
				return false;
			}
		}
		return true;

	}

	/**
	 * Returns true only if this Entity is a path element
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 1, 2005; JDK 1.4.2
	 */
	public boolean isPathElement() {
		return isA(pathElement);
	}

	// *********************************************
	// Getter and setters for graph links
	// *********************************************

	public void setSubject(Entity t) {
		warning("Tried to set subject, but " + this.asString() + " is not a Derivative");
	}

	public Entity getSubject() {
		return null;
	}

	public void setObject(Entity t) {
		warning("Tried to set object, but " + this.asString() + " is not a Relation");
	}

	public Entity getObject() {

		warning("Tried to get object, but " + this.asString() + " is not a Relation");
		return null;
	}

	public void addElement(Entity t) {
		warning("Tried to add an element, but " + this.asString() + " is not a Sequence");
	}

	public Vector<Entity> getElements() {
		return new Vector<>();
	}

	public Entity getElement(int i) {
		return getElements().get(i);
	}

	public Stream<Entity> stream() {
		return getElements().stream();
	}

	/**
	 * Gets vector of derivatives (or relations) in which the Entity instance is the subject.
	 */
	public Vector<Function> getSubjectOf() {
		return subjectOf;
	}

	/**
	 * Gets vector of derivatives (or relations) in which the Thing instance is the subject; restrict to named
	 * relations.
	 */
	public Vector<Function> getSubjectOf(String s) {
		Vector<Function> v = new Vector<Function>();
		for (int i = 0; i < subjectOf.size(); ++i) {
			if ((subjectOf.get(i)).isA(s)) {
				v.add(subjectOf.get(i));
			}
		}
		return v;
	}

	/**
	 * Adds to vector of derivatives (or relations) in which the Entity instance is the subject.
	 */
	public void addSubjectOf(Function d) {
		subjectOf.add(d);
	}

	public boolean removeSubjectOf(Entity t) {
		return subjectOf.remove(t);
	}

	/**
	 * Gets vector of relations in which the Entity instance is the object.
	 */
	public Vector<Relation> getObjectOf() {
		return objectOf;
	}

	/**
	 * Gets vector of relations in which the Entity instance is the object; restrict to named relations.
	 */
	public Vector<Relation> getObjectOf(String s) {
		Vector<Relation> v = new Vector<Relation>();
		for (int i = 0; i < objectOf.size(); ++i) {
			if ((objectOf.get(i)).isA(s)) {
				v.add(objectOf.get(i));
			}
		}
		return v;
	}

	public boolean removeObjectOf(Entity t) {
		return objectOf.remove(t);
	}

	/**
	 * Adds to vector of relations in which the Entity instance is the object.
	 */
	public void addObjectOf(Relation r) {
		objectOf.add(r);
	}

	/**
	 * Gets vector of sequences in which the Entity instance is an element.
	 */
	public Vector<Entity> getElementOf() {
		return elementOf;
	}

	/**
	 * Adds to vector of sequences in which the Entity instance is an element.
	 */
	public void addElementOf(Sequence s) {
		elementOf.add(s);
	}

	public boolean removeElementOf(Entity t) {
		return elementOf.remove(t);
	}

	public boolean removeParent(Entity t) {
		return removeSubjectOf(t) | removeObjectOf(t) | removeElementOf(t);
	}

	public Set<Entity> getParents() {
		Set<Entity> result = new HashSet<Entity>();
		result.addAll(getSubjectOf());
		result.addAll(getObjectOf());
		result.addAll(getElementOf());
		return result;
	}

	/**
	 * Returns all the children of the node, which for an Entity is an empty set.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 15, 2005; JDK 1.4.2
	 */
	public Set<Entity> getChildren() {
		return new HashSet<Entity>();
	}

	public int getNumberOfChildren() {
		return 0;
	}

	/**
	 * Returns all roots of the given Entity.
	 * 
	 * @author M.A. Finlayson
	 * @since Jul 22, 2004; JDK 1.4.2
	 */
	public Set<Entity> getRoots() {
		Set<Entity> result = new HashSet<Entity>();

		List<Entity> queue = new ArrayList<Entity>();
		queue.addAll(getParents());

		Entity next;
		Set<Entity> parents;
		while (!queue.isEmpty()) {
			next = queue.remove(0);
			if (!result.add(next)) {
				continue;
			}
			parents = next.getParents();
			if (parents.isEmpty()) {
				result.add(next);
			}
			else {
				queue.addAll(parents);
			}
		}

		return result;
	}

	public Set<Entity> getAncestors() {
		HashSet<Entity> result = new HashSet<Entity>();
		List<Entity> queue = new ArrayList<Entity>();
		queue.addAll(getParents());

		Entity next;
		while (!queue.isEmpty()) {
			next = queue.remove(0);
			if (!result.add(next)) {
				continue;
			}
			queue.addAll(next.getParents());
		}

		return result;
	}

	public Set<Entity> getDescendants() {
		HashSet<Entity> result = new HashSet<Entity>();
		List<Entity> queue = new LinkedList<Entity>();
		queue.addAll(getChildren());

		Entity next;
		while (!queue.isEmpty()) {
			next = queue.remove(0);
			if (!result.add(next)) {
				continue;
			}
			queue.addAll(next.getChildren());
		}

		return result;
	}

	/**
	 * Returns all Entities which can be reached by a graph walk from this node.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 27, 2005; JDK 1.4.2
	 */
	public Set<Entity> getConnected() {
		HashSet<Entity> visited = new HashSet<Entity>();

		ArrayList<Entity> queue = new ArrayList<Entity>();
		queue.add(this);

		Entity current;
		while (!queue.isEmpty()) {
			current = queue.remove(0);
			if (!visited.add(current)) {
				continue;
			}
			queue.addAll(current.getParents());
			queue.addAll(current.getChildren());
		}

		return visited;
	}

	/**
	 * Returns all trajectory spaces which have this Entity as a descendant.
	 */
	public Collection<Entity> getAncestralTrajectorySpaces() {
		Collection<Entity> result = new HashSet<Entity>();
		Collection<Entity> visited = new HashSet<Entity>();
		Vector<Entity> queue = new Vector<Entity>();
		queue.add(this);
		Entity currentThing;
		while (!queue.isEmpty()) {
			currentThing = queue.remove(0);
			if (visited.add(currentThing)) {
				queue.addAll(currentThing.getSubjectOf());
				queue.addAll(currentThing.getObjectOf());
				queue.addAll(currentThing.getElementOf());
				if (currentThing.isA("eventSpace")) {
					result.add(currentThing);
				}
			}
		}
		return result;
	}

	public boolean isRoot() {
		if (elementOf.isEmpty() & subjectOf.isEmpty() & objectOf.isEmpty()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if a downward graph walk from this Entity can reach the given Entity.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 15, 2005; JDK 1.4.2
	 */
	public boolean isAncestorOf(Entity t) {
		HashSet<Entity> visited = new HashSet<Entity>();

		List<Entity> queue = new ArrayList<Entity>();
		queue.add(this);

		Entity current;
		while (!queue.isEmpty()) {
			current = queue.remove(0);
			if (!visited.add(current)) {
				continue;
			}
			queue.addAll(current.getChildren());
			if (current.equals(t)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * Removes all backward links to this Entity from the children.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 21, 2005; JDK 1.4.2
	 */
	public void breakLinksFromChildren() {
		Entity child;
		for (Iterator<Entity> i = getChildren().iterator(); i.hasNext();) {
			child = i.next();
			child.removeParent(this);
		}
	}

	/**
	 * Adds backward links from the children if they do not already exist.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 21, 2005; JDK 1.4.2
	 */
	public void makeLinksFromChildren() {
		if (functionP()) {
			if (!getSubject().getSubjectOf().contains(this)) {
				getSubject().getSubjectOf().add((Function) this);
			}
		}
		else if (relationP()) {
			if (!getSubject().getSubjectOf().contains(this)) {
				getSubject().addSubjectOf((Function) this);
			}
			if (!getObject().getObjectOf().contains(this)) {
				getObject().addObjectOf((Relation) this);
			}
		}
		else if (relationP()) {
			Entity child;
			for (int i = 0; i < getElements().size(); i++) {
				child = (Entity) getElements().get(i);
				if (!child.getElementOf().contains(this)) {
					child.addElementOf((Sequence) this);
				}
			}
		}
	}

	/**
	 * Attempts to remove this Entity from all memories of which it is a part, and break backward graph links. Does not
	 * remove all strong references. Returns <code>true</code> if the Entity is forgotten from all registered memories.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 27, 2005; JDK 1.4.2
	 */
	public boolean delete() {
		boolean result = true;

		Vector<Observer> obs = new Vector<Observer>();
		obs.addAll(observers);
		Observer o;
		MemoryForget mf;
		for (Iterator<Observer> i = obs.iterator(); i.hasNext();) {
			o = i.next();
			if (o instanceof MemoryForget) {
				mf = (MemoryForget) o;
				result = result & mf.forget(this);
			}
		}
		breakLinksFromChildren();
		return result;
	}

	/**
	 * Returns the order of the thing. Entity = order 0 Anything else = maximum of the order of its arguments + 1;
	 * 
	 * @author M.A. Finlayson
	 * @since Apr 24, 2004; JDK 1.4.2
	 */
	public int order() {
		if (entityP()) {
			return 0;
		}
		else if (functionP()) {
			return getSubject().order() + 1;
		}
		else if (relationP()) {
			return Math.max(getObject().order(), getSubject().order()) + 1;
		}
		else if (sequenceP()) {
			int result = 0;
			Vector<Entity> elements = getElements();
			for (int i = 0; i < elements.size(); i++) {
				result = Math.max(result, elements.get(i).order());
			}
			return result + 1;
		}
		else {
			warning("Thing type not recognized.");
			return -1;
		}
	}

	/**
	 * Returns true if the Entity is a function, relation or sequence of first order, -or- it is a function, relation,
	 * or sequence which is first order not considering any paths or path elements it contains.
	 * 
	 * @author M.A. Finlayson
	 * @since Aug 30, 2004; JDK 1.4.2
	 */
	public boolean isFirstOrderThing() {
		if (order() == 1) {
			return true;
		}

		Entity child;
		for (Iterator<Entity> i = getChildren().iterator(); i.hasNext();) {
			child = i.next();
			if (!child.isZeroOrderThing()) {
				return false;
			}
		}

		return true;
	}

	public boolean isZeroOrderThing() {
		if (order() == 0) {
			return true;
		}
		if (isA("path") | isA("pathElement")) {
			return true;
		}
		return false;
	}

	/*
	 * Adds given Entity to the "example" thread of this.
	 */
	public void addExampleOf(Entity example) {
		exampleOf.add(example);
		this.addType(example.getName(), "example");
	}

	/*
	 * oana's addition; adds an Entity to the examplesOf thread of this Entity. Used mainly to store "bird-38" and
	 * "rock-219" as examples of unique objects.
	 */
	public Vector<Entity> getExampleOf() {
		return exampleOf;
	}

	public void addModifier(int index, Entity modifier) {
		modifiers.add(index, modifier);
	}

	public Vector<Entity> getModifiers() {
		return modifiers;
	}

	public void addModifier(Entity modifier) {
		modifiers.add(modifier);
	}

	public void setModifiers(Vector<Entity> v) {
		// Make sure all the elements are Entities; defensive
		// programming
		for (int i = 0; i < v.size(); ++i) {
			if (!(v.elementAt(i) instanceof Frame)) {
				System.err.println("Oops, trying to put something into a modifier slot that is not a Frame instance!");
			}
		}
		modifiers = v;
	}

	public void clearModifiers() {
		modifiers.clear();
	}

	/**
	 * @see frames.entities.Frame#getAllComponents()
	 */
	public List<Entity> getAllComponents() {
		return new LinkedList<Entity>();
	}

	public IdentityHashSet<Entity> getAllFramesShallow() {
		IdentityHashSet<Entity> result = new IdentityHashSet<Entity>();
		result.addAll(getAllComponents());
		result.addAll(getModifiers());
		return result;
	}

	public IdentityHashSet<Entity> getAllFramesDeep() {
		IdentityHashSet<Entity> result = new IdentityHashSet<Entity>();
		getAllFramesDeepHelper(result);
		return result;
	}

	protected void getAllFramesDeepHelper(Set<Entity> result) {
		if (!result.contains(this)) {
			result.add(this);

			Iterator<Entity> iComponents = getAllComponents().iterator();
			while (iComponents.hasNext()) {
				Entity component = iComponents.next();
				if (component != null) {
					result.addAll(component.getAllFramesDeep());
				}
			}

			Iterator<Entity> iModifiers = getModifiers().iterator();
			while (iModifiers.hasNext()) {
				Entity modifier = iModifiers.next();
				if (modifier != null) {
					result.addAll(modifier.getAllFramesDeep());
				}
			}
		}
	}

	/**
	 * A convenience alias for resolvePath(List). Instead of taking a List of strings, this function takes a single
	 * string in dotted (javalike) notation. For example, the path string "subject.subject.element3" would return:
	 * <p>
	 * <code>(Entity) this.getSubject().getSubject().getElements().get(3);</code>
	 * 
	 * @param path
	 * @return
	 */
	public Entity resolvePath(String path) {
		List<String> splitList;
		if (path.trim().equals("")) {
			splitList = new ArrayList<String>();
		}
		else {
			String[] split = path.split("\\.");
			splitList = Arrays.asList(split);
		}
		return resolvePath(splitList);
	}

	/**
	 * Given a list such as <code>[subject subject element3]</code>, this routine will attempt to walk down the Entity
	 * tree, following the given directions. that is, it will try to return <code>(Thing)
	 * this.getSubject().getSubject().getElements().get(3)</code>. Passing the empty list as the path simply returns
	 * this. If the path list points outside the tree (for example, if this is a derivative, but the path list is
	 * <code>[object]</code>, then null is returned. <br>
	 * Valid path elements include:
	 * <ul>
	 * <li>subject
	 * <li>object
	 * <li>element <i>n </i>, where <i>n </i> is an optional element index
	 * <li>elt <i>n </i>, where <i>n </i> is an optional element index
	 * <li>modifier <i>n </i>, where <i>n </i> is an optional modifier index
	 * <li>mod <i>n </i>, where <i>n </i> is an optional modifier index
	 * <li><i>type </i>, where <i>type </i> is a type appearing on the thread of one of this thing's subcomponents or
	 * modifiers.
	 * </ul>
	 * <br>
	 * <b>Resolving ambiguous paths </b>: <br>
	 * If the optional indices are omitted, or if the <i>type </i> style path elements are used, then the path may be
	 * ambiguous. In ambiguous paths, the path elements are interpretted first as a structural indicator (eg, subject,
	 * object, etc), and only if that fails are they treated as a <i>type </i> indicated. If the optional index is left
	 * off of element or modifier, then the first element/modifier is returned. If more than one subcomponent/modifier
	 * matches a <i>type </i> request, then the first to match is returned.
	 * 
	 * @param path
	 *            a List of strings
	 * @return an Entity, or null if the path cannot be successfully resolved against this thing.
	 */
	public Entity resolvePath(List<String> path) {
		if (path.isEmpty()) return this;

		String pathHead = path.get(0).toLowerCase();
		List<String> pathRest = path.subList(1, path.size());

		if (functionP() || relationP()) {
			if (pathHead.startsWith("subject")) {
				Entity next = getSubject();
				if (next == null) return null;
				return next.resolvePath(pathRest);
			}
		}

		if (relationP()) {
			if (pathHead.startsWith("object")) {
				Entity next = getObject();
				if (next == null) return null;
				return next.resolvePath(pathRest);
			}
		}

		if (sequenceP()) {
			if (pathHead.startsWith("element")) {
				int which;
				if (pathHead.equals("element")) {
					which = 0;
				}
				else {
					String whichStr = pathHead.substring(7);
					which = Integer.parseInt(whichStr);
				}

				if (which >= 0 && getElements().size() > which) {
					Entity next = (Entity) getElements().get(which);
					if (next == null) return null;
					return next.resolvePath(pathRest);
				}
			}

			if (pathHead.startsWith("elt")) {
				int which;
				if (pathHead.equals("elt")) {
					which = 0;
				}
				else {
					String whichStr = pathHead.substring(3);
					which = Integer.parseInt(whichStr);
				}

				if (which >= 0 && getElements().size() > which) {
					Entity next = (Entity) getElements().get(which);
					if (next == null) return null;
					return next.resolvePath(pathRest);
				}
			}
		}

		if (pathHead.startsWith("modifier")) {
			int which;
			if (pathHead.equals("modifier")) {
				which = 0;
			}
			else {
				String whichStr = pathHead.substring(8);
				which = Integer.parseInt(whichStr);
			}

			if (which >= 0 && modifiers.size() > which) {
				Entity next = modifiers.get(which);
				if (next == null) return null;
				return next.resolvePath(pathRest);
			}
		}

		if (pathHead.startsWith("mod")) {
			int which;
			if (pathHead.equals("mod")) {
				which = 0;
			}
			else {
				String whichStr = pathHead.substring(3);
				which = Integer.parseInt(whichStr);
			}

			if (which >= 0 && modifiers.size() > which) {
				Entity next = modifiers.get(which);
				if (next == null) return null;
				return next.resolvePath(pathRest);
			}
		}

		List<Entity> components = getAllComponents();
		Iterator<Entity> iComponents = components.iterator();
		while (iComponents.hasNext()) {
			Entity component = iComponents.next();
			if (component != null && component.isA(pathHead)) {
				return component.resolvePath(pathRest);
			}
		}

		List<Entity> modifiers = getModifiers();
		Iterator<Entity> iModifiers = modifiers.iterator();
		while (iModifiers.hasNext()) {
			Entity modifier = iModifiers.next();
			if (modifier != null && modifier.isA(pathHead)) {
				return modifier.resolvePath(pathRest);
			}
		}

		return null;
	}

	/**
	 * Returns the identity without calling getName().
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 14, 2004; JDK 1.4.2
	 */
	protected String identitySansName(boolean compact) {
		String id = "";
		String b = "";
		if (getNameSuffix() != null) {
			id = (new Integer(getID())).toString();
		}
		if (getBundle() != null) {
			b = getBundle().toString(compact);
		}

		if (compact) {
			return id + "\n" + b + "\n";
		}
		else {
			return Tags.tagNoLine("id", id) + b;
		}
	}

	/**
	 * Helps produce print string.
	 */
	protected String identity(boolean compact) {
		if (compact) {
			return getName() + "\n" + getBundle().toString(compact) + "\n";
		}
		else {
			return Tags.tagNoLine("name", getName()) + getBundle().toString(compact);
		}
	}

	/**
	 * Assures no calls to getName().
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 14, 2004; JDK 1.4.2
	 */
	protected String fillerSansName(boolean compact) {
		Class<? extends Entity> c = this.getClass();
		if (c.equals(Function.class)) {
			return ((Function) this).fillerSansName(compact);
		}
		else if (c.equals(Relation.class)) {
			return ((Relation) this).fillerSansName(compact);
		}
		else
			return "";
	}

	/**
	 * Helps produce print string.
	 */
	protected String filler(boolean compact) {
		Class<? extends Entity> c = this.getClass();
		if (c.equals(Function.class)) {
			return ((Function) this).filler(compact);
		}
		else if (c.equals(Relation.class)) {
			return ((Relation) this).filler(compact);
		}
		else
			return "";
	}

	/**
	 * Helps produce print string.
	 */
	protected String marker(boolean compact) {
		return "entity";
	}

	/**
	 * Produces a string from which the Entity can be reconstructed without triggering changes to the Entity. The
	 * getName() method will trigger an addThread() call if the Entity's bundle is empty.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 14, 2004; JDK 1.4.2
	 */
	public String toXMLSansName(boolean compact) {
		if (compact) {
			String s = "";
			s += marker(compact) + ": " + identitySansName(compact);
			String sf = " " + filler(compact).replaceAll("\n", "\n ");
			s += sf;
			return s.trim();
		}
		else {
			return Tags.tag(marker(compact), identitySansName(compact) + fillerSansName(compact));
		}
	}

	/**
	 * Produces print string. If compact is true, the string is an easily human-readable form. Otherwise, the string is
	 * an XML version suitable for writing to files.
	 */
	public String toXML(boolean compact) {
		if (compact) {
			String s = "";
			s += marker(compact) + ": " + identity(compact);
			String sf = " " + filler(compact).replaceAll("\n", "\n ");
			s += sf;
			return s.trim();
		}
		else {
			return Tags.tag(marker(compact), identity(compact) + filler(compact));
		}
	}

	/*
	 * (non-Javadoc)
	 * @see bridge.reps.entities.Frame#getPrettyPrintType()
	 */
	public char getPrettyPrintType() {
		return TYPECHAR_ENTITY;
	}
	
	// print the details of an entity
	public void details() {
		boolean debug = true;
		Entity entity = this;
		if(entity.getType().equals(Markers.SEMANTIC_INTERPRETATION)) {
			for(Entity entity1:entity.getElements()) {
				entity1.details();
			}
		} else {
//			entity = entity.getElement(0);
			Mark.say(debug, "  Entity:", entity);
			Mark.say(debug, "     Subject:", entity.getSubject());
			Mark.say(debug, "     Object:", entity.getObject());
			Mark.say(debug, "     Features:", entity.getFeatures());
			Mark.say(debug, "     Properties", entity.getPropertyList().toString());
			Mark.say(debug, "     Types", entity.getTypes().toString());
			Mark.say(debug, "     Thread", entity.getPrimedThread());
			// Mark.say(debug, " Elements", entity.getElements().toString());
		}

	}

	protected void bundleModified(Bundle b, String oldState, String newState) {
		finest("\nBundle modified from:\n-- " + oldState + "\n--(to:) " + newState + "\n--");
		fireNotification();
	}

	/**
	 * Indicates whether the string form should be saved when firing notifications.
	 */
	boolean saveStringForm = false;

	public void setSaveStringForm(boolean b) {
		saveStringForm = b;
	}

	public void setSaveStringFormOnDependents(boolean b) {
		setSaveStringForm(b);
		getBundle().setSaveStringFormOnDependents(b);
	}

	public boolean getSaveStringForm() {
		return saveStringForm;
	}

	/**
	 * Used to suppress notification. Notification starts turned off -- when this thread (via it's owner bundle) is
	 * added to a memory which cares about notification, that memory turns it on.
	 */
	boolean notification = false;

	/**
	 * Sets the notification flag.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 14, 2004; JDK 1.4.2
	 */
	public void setNotification(boolean b) {
		notification = b;
	}

	/**
	 * Sets the notification flag on this object, and all of the object's descendents, overriding any individual
	 * settings.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 15, 2004; JDK 1.4.2
	 */
	public void setNotificationOnDependents(boolean b) {
		setNotification(b);
		getBundle().setNotificationOnDependents(b);
	}

	/**
	 * Used to store a string representation of the previous state.
	 */
	protected String previousState;

	/**
	 * Saves the state associated with this Entity in string form
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	protected void saveState() {
		if (notification) {
			if (saveStringForm) {
				previousState = this.toXMLSansName(Entity.defaultToXMLIsCompact);
			}
			changed(SAVE_STATE);
		}

	}

	protected void fireNotification() {
		if (notification) {
			changed(FIRE_NOTIFICATION);
		}
	}

	/**
	 * Adds functionality to status as Observable.
	 */
	public void changed() {
		changed(null);
	}

	/**
	 * Adds functionality to status as Observable.
	 */
	public void changed(Object o) {
		setChanged();
		notifyObservers(o);
	}

	public Entity getCopy() {
		return getCopy(EntityFactoryDefault.getInstance());
	}

	public Entity getCopy(EntityFactory factory) {
		Entity thing = factory.newThing();
		Bundle bundle = (Bundle) getBundle().clone();
		thing.setBundle(bundle);
		return thing;
	}

	public Object clone() {
		Entity t = (Entity) (clone(EntityFactoryDefault.getInstance()));
		t.setPropertyList(clonePropertyList());
		return t;
	}

	/**
	 * Clones, cloning threads
	 */
	public Object clone(EntityFactory factory) {
		Entity thing = factory.newThing();

		thing.setPropertyList(clonePropertyList());

		Bundle bundle = (Bundle) getBundle().clone();
		thing.setBundle(bundle);

		List<Entity> modifiers = getModifiers();
		for (int i = 0; i < modifiers.size(); i++) {
			Entity t = modifiers.get(i);
			thing.addModifier(t);
		}
		return thing;
	}

	/**
	 * Clones, recursively.
	 */
	public Entity deepClone() {
		return deepClone(EntityFactoryDefault.getInstance());
	}

	public Entity deepClone(boolean newId) {
		return deepClone(EntityFactoryDefault.getInstance(), newId);
	}

	public Entity deepClone(EntityFactory factory) {
		return deepClone(factory, true);
	}

	public Entity deepClone(EntityFactory factory, boolean newId) {
		return deepClone(factory, new IdentityHashMap<Entity, Entity>(), newId);
	}

	/**
	 * Clones, recursively.
	 */
	protected Entity deepClone(EntityFactory factory, IdentityHashMap<Entity, Entity> cloneMap, boolean newId) {
		if (cloneMap.containsKey(this)) {
			return cloneMap.get(this);
		}

		Entity clone = factory.newThing();
		if (!newId) {
			clone.setNameSuffix(getNameSuffix());
		}

		Bundle bundleClone = (Bundle) (getBundle().clone());
		clone.setBundle(bundleClone);

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
	 * Clones, recursively, for benefit of resolver. Note that structure is cloned, but not Entity instances. Not sure
	 * which is right. KAB 2004.09.27: No no no. cloneForResolver() just returns this?
	 */
	public Entity cloneForResolver() {
		Mark.say("J");
		return cloneForResolver(EntityFactoryDefault.getInstance());
	}

	public Entity cloneForResolver(EntityFactory factory) {
		Mark.say("K");
		// Version two
		// Entity clone = (Entity)(this.clone());
		// clone.forget();
		// return clone;
		// Version one
		return this;
	}

	public int hashCode() {
		// Name should be the object name + uniqueID
		String hashname = this.getName();
		// mpfay jan2014 - removing unique id from hashcode to make it consistent with isEqual/isDeepEqual
		// if(name != null)
		// hashname = name;
		// else
		// hashname = this.getType();
		return hashname.hashCode() + 17;
	}

	public int hashCodeSansID() {
		return getBundle().hashCode();
	}

	// Tests for equality without regard to ID number.
	// MAF.14.Feb.04
	public boolean isEqual(Object o) {
		if (o instanceof Entity) {
			Entity t = (Entity) o;
			Bundle b = t.getBundle();
			return (this.entityP() == t.entityP()) && (this.functionP() == t.functionP()) && (this.relationP() == t.relationP())
			        && (this.sequenceP() == t.sequenceP()) && b.equals(this.getBundle());
		}
		return false;
	}

	// Tests for equality without regard to ID number. Recursively
	// checks heirarchy rooted at this thing -- will go on violent
	// murderous rampage if passed something with cycles!
	public boolean isDeepEqual(Object o) {
		if (!(o instanceof Entity)) return false;
		Entity t = (Entity) o;
		Bundle b = t.getBundle();
		if ((this.entityP() != t.entityP()) || (this.functionP() != t.functionP()) || (this.relationP() != t.relationP())
		        || (this.sequenceP() != t.sequenceP())) {
			return false;
		}
		if (!getBundle().equals(b)) {
			return false;
		}

		List<Entity> components = getAllComponents();
		List<Entity> oComponents = t.getAllComponents();
		if (components.size() != oComponents.size()) {
			return false;
		}
		Iterator<Entity> iComponents = components.iterator();
		Iterator<Entity> iOComponents = oComponents.iterator();
		while (iComponents.hasNext()) {
			Entity component = iComponents.next();
			Entity oComponent = iOComponents.next();
			if (!component.isDeepEqual(oComponent)) {
				return false;
			}
		}

		List<Entity> modifiers = getModifiers();
		List<Entity> oModifiers = t.getModifiers();
		if (modifiers.size() != oModifiers.size()) {
			return false;
		}
		Iterator<Entity> imodifiers = modifiers.iterator();
		Iterator<Entity> iOModifiers = oModifiers.iterator();
		while (imodifiers.hasNext()) {
			Entity modifier = imodifiers.next();
			Entity oModifier = iOModifiers.next();
			if (!modifier.isDeepEqual(oModifier)) {
				return false;
			}
		}
		
		List<Object> features = getFeatures();
		List<Object> oFeatures = t.getFeatures();
		if (features.size() != oFeatures.size()) {
			return false;
		}
		Iterator<Object> ifeatures = features.iterator();
		Iterator<Object> iOFeatures = oFeatures.iterator();
		while (ifeatures.hasNext()) {
			Object feature = ifeatures.next();
			Object oFeature = iOFeatures.next();
			if (!feature.equals(oFeature)) {
				return false;
			}
		}

		return true;
	}

	// Added jan2014 to make entities work better in sets/hashsets/etc.
	@Override
	public boolean equals(Object o) {
		if (super.equals(o))
			return true;
		else
			return (isDeepEqual(o) && this.getName().equals(((Entity) o).getName()));
	}

	/**
	 * Produces print string.
	 */
	public String toXML() {
		return this.toXML(Entity.defaultToXMLIsCompact);
	}

	// *********************************************
	// Overridden observerable methods
	// *********************************************

	public void deleteObservers() {
		observers.removeAllElements();
		super.deleteObservers();
	}

	public void addObserver(Observer o) {
		observers.addElement(o);
		super.addObserver(o);
	}

	public void deleteObserver(Observer o) {
		observers.removeElement(o);
		super.deleteObserver(o);
	}

	// *********************************************
	// Static methods
	// *********************************************

	public static String toXMLNamesOnly(Collection<Entity> entities) {
		StringBuffer result = new StringBuffer();
		result.append("[");
		Entity thing;
		for (Iterator<Entity> i = entities.iterator(); i.hasNext();) {
			thing = i.next();
			if (thing != null) {
				result.append(thing.getName());
			}
			else {
				result.append("null");
			}
			result.append(", ");

		}
		if (result.length() > 2) {
			result.deleteCharAt(result.length() - 1);
			result.deleteCharAt(result.length() - 1);
		}
		result.append("]");
		return result.toString();
	}

	/**
	 * Returns a vector of classes to which an Entity belongs, exctracted from a string representation of that Entity.
	 * Ignores features thread.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 16, 2004; JDK 1.4.2
	 */
	public static Vector<String> getClassesFromString(String s) {
		Vector<String> result = new Vector<String>();
		if (s == null) {
			return result;
		}

		s = Tags.untagString("bundle", s);
		String t;
		int i = 1;
		int begin, end;
		String sub;
		// Get each thread
		while ((t = Tags.untagString("thread", s, i)) != null) {
			// Iterate on each thread
			begin = 0;
			t = t.trim();
			while (true) {
				if (t.indexOf("features") >= 0) {
					break;
				}
				end = t.indexOf(' ', begin);
				if (end < 0) {
					end = t.length();
				}
				// Get the string demarked by spaces.
				sub = t.substring(begin, end);
				sub = sub.trim();
				// If the class is not empty, add it to the vector.
				if (sub != "") {
					result.add(sub);
				}
				// If we've reached the end of the string, break;
				if (end == t.length()) {
					break;
				}
				begin = end + 1;
			}
			i++;
		}

		return result;
	}

	public static String getPrintStringFromCollection(Collection<Entity> things) {
		StringBuffer result = new StringBuffer();
		result.append("[");
		Entity thing;
		for (Iterator<Entity> i = things.iterator(); i.hasNext();) {
			thing = i.next();
			result.append(thing.getName());
			if (i.hasNext()) result.append(", ");
		}
		result.append("]");
		return result.toString();
	}

	public static void printNamesFromCollection(Collection<Entity> things) {
		System.out.println(getPrintStringFromCollection(things));

	}

	public static String showDifferences(Entity first, Entity second) {
		String result = null;

		String firstClass = first.getClass().toString();
		String secondClass = second.getClass().toString();
		String diff;
		if (!firstClass.equals(secondClass)) {
			if (result == null) {
				result = "";
			}
			result = "Entity diff: " + firstClass + " != " + secondClass + "\n";
		}
		else {
			if (first.relationP()) {
				Relation f = (Relation) first;
				Relation s = (Relation) second;

				diff = showDifferences(f.getSubject(), s.getSubject());
				if (diff != null) {
					if (result == null) {
						result = "";
					}
					result = result + "Entity diff: " + diff + "\n";
				}

				diff = showDifferences(f.getObject(), s.getObject());
				if (diff != null) {
					if (result == null) {
						result = "";
					}
					result = result + "Entity diff: " + diff + "\n";
				}

			}
			else if (first.functionP()) {
				Function f = (Function) first;
				Function s = (Function) second;
				diff = showDifferences(f.getSubject(), s.getSubject());
				if (diff != null) {
					if (result == null) {
						result = "";
					}
					result = result + "Entity diff: " + diff + "\n";
				}
			}
			else if (first.sequenceP()) {
				Sequence f = (Sequence) first;
				Sequence s = (Sequence) second;
				if (f.getElements().size() != s.getElements().size()) {
					if (result == null) {
						result = "";
					}
					result = result + "Entity diff: " + f.getName() + " and " + s.getName() + "\nare of different sizes\n";
				}
				else {
					for (int i = 0; i < first.getElements().size(); i++) {
						diff = showDifferences((Entity) first.getElements().get(i), (Entity) second.getElements().get(i));
						if (diff != null) {
							if (result == null) {
								result = "";
							}
							result = result + "Entity diff: " + diff + "\n";
						}
					}
				}

			}

			diff = Bundle.showDifferences(first.getBundle(), second.getBundle());
			if (diff != null) {
				if (result == null) {
					result = "";
				}
				result = result + "Entity diff: " + diff + "\n";
			}
		}
		return result;
	}

	public Entity limitTo(String type) {
		Bundle b = getBundle();
		b = b.filterFor(type);
		setBundle(b);
		return this;
	}

	public Entity limitToRoot(String type) {
		Bundle b = getBundle();
		b = b.filterForRoot(type);
		setBundle(b);
		return this;
	}

	// ///***** Moving above functions to memory

	// Debugging section
	public static final String LOGGER_GROUP = "frames";

	public static final String LOGGER_INSTANCE = "Entity";

	public static final String LOGGER = LOGGER_GROUP + "." + LOGGER_INSTANCE;

	public static Logger getLogger() {
		return Logger.getLogger(LOGGER);
	}

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

	public String asString() {
		boolean not = hasFeature(Markers.NOT);
		if (this.relationP()) {
			return "(rel " + (not ? "not " : "") + this.getType() + " " + this.getSubject().asString() + " " + this.getObject().asString() + ")";
		}
		else if (this.functionP()) {
			return "(fun " + (not ? "not " : "") + this.getType() + " " + this.getSubject().asString() + ")";
		}
		else if (this.sequenceP()) {
			String result = "(" + (not ? "not " : "") + "seq ";
			result += this.getType();
			for (Entity t : ((Sequence) this).getElements()) {
				result += " " + t.asString();
			}
			result += ")";
			return result;
		}
		else {
			return "(ent " + (not ? "not " : "") + this.getName() + ")";
		}
	}

	/**
	 * Produces intented string
	 */
	public String prettyPrint() {
		return prettyPrint(0, this);
	}

	/**
	 * Produces intented string
	 */
	public String prettyPrint(int i, Entity entity) {
		String result = "";
		boolean not = hasFeature(Markers.NOT);
		if (entity.relationP()) {
			result += indent(i) + "(rel " + (not ? "not " : "") + entity.getType();
			result += prettyPrint(i + 1, entity.getSubject());
			result += prettyPrint(i + 1, entity.getObject());
			result += ")";
		}
		else if (entity.functionP()) {
			result += indent(i) + "(fun " + (not ? "not " : "") + entity.getType();
			result += prettyPrint(i + 1, entity.getSubject());
			result += ")";
		}
		else if (entity.sequenceP()) {
			result += indent(i) + "(" + (not ? "not " : "") + "seq ";
			result += entity.getType();
			for (Entity t : ((Sequence) entity).getElements()) {
				result += prettyPrint(i + 1, t);
			}
			result += ")";
		}
		else {
			result += indent(i) + "(ent " + (not ? "not " : "") + entity.getName() + ")";
		}
		return result;
	}

	private String indent(int x) {
		String result = "\n";
		for (int i = 0; i < x; ++i) {
			result += " ";
		}
		return result;
	}

	public String asStringSansIndexes() {
		boolean not = hasFeature(Markers.NOT);
		if (this.relationP()) {
			return "(rel " + (not ? "not " : "") + this.getType() + " " + this.getSubject().asStringSansIndexes() + " "
			        + this.getObject().asStringSansIndexes() + ")";
		}
		else if (this.functionP()) {
			return "(fun " + (not ? "not " : "") + this.getType() + " " + this.getSubject().asStringSansIndexes() + ")";
		}
		else if (this.sequenceP()) {
			String result = "(" + (not ? "not " : "") + "seq ";
			result += this.getType();
			for (Entity t : ((Sequence) this).getElements()) {
				result += " " + t.asStringSansIndexes();
			}
			result += ")";
			return result;
		}
		else {
			return "(ent " + (not ? "not " : "") + this.getType() + ")";
		}
	}

	public String toString() {
		return this.asString();
	}

	/**
	 * Ok to edit; ensures generator gets passive and past tense right
	 * 
	 * @return
	 */
	public String hashForGenerator() {

		String result = "";

		// Include features

		for (Object feature : this.getFeatures()) {
			result += feature.toString() + " ";
		}

		if (this.functionP()) {
			result += this.getType() + " " + this.getSubject().hash();
		}
		else if (this.relationP()) {
			result += this.getType() + " " + this.getSubject().hash() + " " + this.getObject().hash();
		}
		else if (this.sequenceP()) {
			result += "sequence ";
			result += this.getType();
			for (Entity t : ((Sequence) this).getElements()) {
				result += " " + t.hash();
			}
		}
		else if (this.entityP()) {
			// Changed 30 Apr 2013 to do post story reading dereferencing of things
			// result += (not ? "not " : "") + (isA (Markers.NAME) ? this.getName() : this.getType());

			result += this.getType();

			if (result.indexOf('-') >= 0) {
				// Mark.say("Type has hyphen", result);
			}

		}

		// property list contains features as well
		for (LabelValuePair property : getPropertyList()) {
			if (property.isIdentifier()) {
				Object value = property.getValue();
				if (value instanceof Entity) {
					result += ((Entity) (value)).hash() + " ";
				}
				else {
					result += value.toString() + "";
				}
			}
		}

		return result;
	}

	/*
	 * Do not edit; dereferencing of story elements depends delicately on this method.
	 */
	public String hash() {

		String result = "";

		boolean not = hasFeature(Markers.NOT);

		if (this.relationP()) {
		    result = "(" + (not ? "not " : "") + this.getType() + " " + this.getSubject().hash() + " " + this.getObject().hash() + ")";
		}
		else if (this.functionP()) {
		    result = "(" + (not ? "not " : "") + this.getType() + " " + this.getSubject().hash() + ")";
		}
		else if (this.sequenceP()) {
		    result = "(" + (not ? "not " : "") + "sequence ";
		    result += this.getType();
		    for (Entity t : ((Sequence) this).getElements()) {
		        result += " " + t.hash();
		    }
		    result += ")";
		}
		else if (this.entityP()) {
			// Changed 30 Apr 2013 to do post story reading dereferencing of things
			// result += (not ? "not " : "") + (isA (Markers.NAME) ? this.getName() : this.getType());

			result += (not ? "not " : "") + this.getType();

			if (result.indexOf('-') >= 0) {
				// Mark.say("Type has hyphen", result);
			}

		}
		
		// property list contains features as well
		for (LabelValuePair property : getPropertyList()) {
		    if (property.isIdentifier()) {
		        Object value = property.getValue();
		        result += " " + property.getLabel()+":";
		        if (value instanceof Entity) {
		            result += ((Entity) (value)).hash();
		        }
		        else {
		            result += value.toString();
		        }
		    }
		}
		
		return result;
	}

	/*
	 * Do not edit; dereferencing of story elements depends delicately on this method.
	 */
	public String hashIncludingThings() {
		boolean not = hasFeature(Markers.NOT);

		if (this.relationP()) {
			return "(" + (not ? "not " : "") + this.getType() + " " + this.getSubject().hashIncludingThings() + " "
			        + this.getObject().hashIncludingThings() + ")";
		}
		else if (this.functionP()) {
			return "(" + (not ? "not " : "") + this.getType() + " " + this.getSubject().hashIncludingThings() + ")";
		}
		else if (this.sequenceP()) {
			String result = "(" + (not ? "not " : "") + "sequence ";
			result += this.getType();
			for (Entity t : ((Sequence) this).getElements()) {
				result += " " + t.hashIncludingThings();
			}
			result += ")";
			return result;
		}
		else {
			return (not ? "not " : "") + this.getType();
		}
	}

	public String asStringWithoutIndexes() {
		// Ok to change, but do not change hash
		return hash();
	}

	public String asStringWithIndexes() {
		if (this.relationP()) {
			return "(rel " + this.getName() + " " + this.getSubject().asStringWithIndexes() + " " + this.getObject().asStringWithIndexes() + ")";
		}
		else if (this.functionP()) {
			return "(fun " + this.getName() + " " + this.getSubject().asStringWithIndexes() + ")";
		}
		else if (this.sequenceP()) {
			String result = "(seq ";
			result += this.getName();
			for (Entity t : ((Sequence) this).getElements()) {
				result += " " + t.asStringWithIndexes();
			}
			result += ")";
			return result;
		}
		else {
			return "(ent " + this.getName() + ")";
		}
	}

	/*
	 * Reads Entity structure cast as s-expression
	 */
	public static Entity reader(String s) {
		if (s.isEmpty()) {
			Mark.say("Sexp bug 1");
			return null;
		}

		if (s.startsWith("(t ")) {
			// Mark.say("Working on t", s);

			int match = indexOfMatchingParenthesis(0, s);
			Thread thread = makeThread(s.substring(2, match).trim());
			Entity t = new Entity(thread);
			// Mark.say("Finished", t.asString());
			return t;
		}
		else if (s.startsWith("(d ")) {
			// Mark.say("Working on d", s);
			int first = indexOfNextParenthesis(1, s);
			Thread thread = makeThread(s.substring(2, first).trim());
			Function d = new Function(thread, reader(s.substring(first, 1 + indexOfMatchingParenthesis(first, s)).trim()));
			// Mark.say("Finished", d.asString());
			return d;
		}
		else if (s.startsWith("(r ")) {
			// Mark.say("Working on r", s);
			int first = indexOfNextParenthesis(1, s);
			int match1 = indexOfMatchingParenthesis(first, s);
			int second = indexOfNextParenthesis(match1 + 1, s);
			int match2 = indexOfMatchingParenthesis(second, s);
			Thread thread = makeThread(s.substring(2, first).trim());
			Relation r = new Relation(thread, reader(s.substring(first, match1 + 1)), reader(s.substring(second, match2 + 1)));
			// Mark.say("Finished", r.asString());
			return r;
		}
		else if (s.startsWith("(s ")) {
			// Mark.say("Working on s", s);
			int first = indexOfNextParenthesis(1, s);
			Thread thread = makeThread(s.substring(2, first).trim());
			Sequence sequence = new Sequence(thread);
			int next = first;
			int match = indexOfMatchingParenthesis(next, s);
			while (true) {
				if (first < 0) {
					// Mark.say("Finished s", s);
					return sequence;
				}
				Entity t = reader(s.substring(first, match));
				sequence.addElement(t);
				first = indexOfNextParenthesis(match + 1, s);
				if (first < 0) {
					// Mark.say("Finished s", s);
					return sequence;
				}
				match = indexOfMatchingParenthesis(first, s);
			}
		}
		return null;
	}

	public static Thread makeThread(String s) {
		String[] split = s.split(" ");
		Thread thread = new Thread();
		for (String x : split) {
			thread.add(x);
		}
		return thread;
	}

	private static int indexOfNextParenthesis(int start, String s) {
		StringBuffer b = new StringBuffer(s);
		int count = 0;
		for (int i = start; i < b.length(); ++i) {
			if (b.charAt(i) == '(') {
				return i;
			}
			else if (b.charAt(i) == ')') {
				--count;
			}
			else if (count < 0) {
				Mark.say("Sexp bug 4");
				return -1;
			}
		}
		// Mark.say("Sexp bug 5");
		return -1;
	}

	private static int indexOfMatchingParenthesis(int start, String s) {
		StringBuffer b = new StringBuffer(s);
		int count = 0;
		for (int i = start; i < b.length(); ++i) {
			if (b.charAt(i) == '(') {
				++count;
			}
			else if (b.charAt(i) == ')') {
				--count;
			}
			if (count == 0) {
				return i;
			}
			else if (count < 0) {
				Mark.say("Sexp bug 3");
				return -1;
			}
		}
		Mark.say("Sexp bug 3");
		return -1;
	}

	public String toEnglish() {
		String english = "";
		english = Generator.getGenerator().generate(this);
		if (english == null) return toString();
		return english.trim();
	}


	public Entity get(int i) {
		if (this.sequenceP()) {
			Vector<Entity> v = getElements();
			if (!v.isEmpty()) {
				return v.get(i);
			}

		}
		return null;
	}

	public void set(int i, Entity x) {
		if (this.sequenceP()) {
			Vector<Entity> v = getElements();
			v.set(i, x);
		}
	}

	public void transferThreadsFeaturesAndProperties(Entity target) {

		if (this == null || target == null) {
			Mark.err("Source or target null", this, target);
			return;
		}

		Bundle bundle = this.getBundle().copy();
		if (bundle == null) {
			Mark.err("Bundle is null for source", this);
		}
		if (bundle.isEmpty()) {
			Mark.err("Bundle is empty for source", this);
		}
		target.setBundle(bundle);

		// Test

		bundle = target.getBundle();

		if (bundle == null) {
			Mark.err("Bundle is null for target", target);
		}
		if (bundle.isEmpty()) {
			Mark.err("Bundle is empty for target", target);
		}
		Vector<LabelValuePair> propertyList = clonePropertyList();
		// Mark.say("Property list", propertyList);
		target.setPropertyList(propertyList);
		this.getFeatures().stream().forEach(f -> {
			// Mark.say("Adding", f, "to", this.getType());
			target.addFeature(f);
		});
	}

	/////////////////// New 20 Apr 2017 Entities hashed, implemented for use with Jason connections

	private static HashMap<String, Entity> classifiedThings = new HashMap<>();

	public static Entity getClassifiedThing(String x) {

		if (x.equalsIgnoreCase("null")) {
			return Markers.NULL;
		}

		Entity value = classifiedThings.get(x);

		if (value != null) {
			// Already in hashmap, so just return it.
			return value;
		}

		// Not in hashmap, have to make entity and put it into map

		// First, see if it has a suffix (part after +)

		int index = x.lastIndexOf('+');

		String word = x;
		String suffix = null;
		Entity entity = null;

		if (index > 0) {
			// Suffix found
			word = x.substring(0, index);
			suffix = x.substring(index + 1);
		}
		Bundle bundle = null;
		try {
			Integer.parseInt(word);
			bundle = new Bundle();
			Thread thread = new Thread();
			thread.add("number");
			thread.add("integer");
			thread.add(word);
			bundle.add(thread);

		}
		catch (NumberFormatException e) {
			// Should move this static method here in Entity from START
			bundle = Start.restrict(word, BundleGenerator.getBundle(word));
		}
		if (suffix != null) {
			entity = new Entity(bundle, "-" + suffix);
			// Hash only if it has a suffix
			classifiedThings.put(x, entity);
		}
		else {
			entity = new Entity(bundle);
		}

		return entity;
	}

	private Entity(Bundle b, String suffix) {
		setNameSuffix(suffix);
		setBundle(b);
	}

	public static void clearCache() {
		classifiedThings.clear();
	}

	/**
	 * Demonstrates behavior of constructors and getters, that is, how to construct and take apart the basic building
	 * blocks.
	 */
	public static void main(String argv[]) {
		// // Make example with constructors.
		// Relation r = new Relation("kill", new Entity("John"), new Entity("Mary"));
		// r.addType("shot");
		// // Mark.say(r.toXML());
		// Mark.say("Result of construction:", r);
		// Mark.say("Result of getSubject:", r.getSubject());
		// Mark.say("Result of getObject:", r.getObject());
		// // Next returns null because not a sequence; in general, if the entity doesn't have the kind of thing that
		// the
		// // getter looks for naturally, the getter returns null.
		// Mark.say("Result of getElements:", r.getElements());
		// Mark.say("Result of getType:", r.getType());
		// // Result has html like markers when printed, but it is just a vector of threads, in this case, just one
		// thread.
		// Mark.say("Result of getBundle:", r.getBundle());
		// // Result has html like markers when printed, but it is just a vector of strings.
		// Mark.say("Result of getPrimedThread:", r.getPrimedThread());
		//
		// // Give relation a property
		// r.addProperty("test", "hello world");
		// Mark.say("The stored 'test' property is", r.getProperty("test"));
		// r.addProperty("number", 3.14);
		// Mark.say("The stored 'number' property is", r.getProperty("number"));
		//

		Entity z = Translator.getTranslator().translate("John loves cake with frosting");

		Mark.say(z.asString());
		Mark.say(z);
		Mark.say(z.toXML());
		Mark.say(z.toEnglish());

	}

	/**
	 * @param what
	 * @param vv
	 * @param vv2
	 */
	public static Entity replace(Entity pattern, Entity what, Entity vv) {
		if (pattern.entityP(what.getType())) {
			return vv;
		}
		else if (pattern.entityP()) {
			return pattern;
		}
		else if (pattern.functionP()) {
			pattern.setSubject(replace(pattern.getSubject(), what, vv));
		}
		else if (pattern.relationP()) {
			pattern.setSubject(replace(pattern.getSubject(), what, vv));
			pattern.setObject(replace(pattern.getObject(), what, vv));
		}
		else if (pattern.sequenceP()) {
			Vector<Entity> v = pattern.getElements();
			for (int i = 0; i < v.size(); ++i) {
				v.set(i, replace(v.get(i), what, vv));
			}
		}
		return pattern;
	}

}
