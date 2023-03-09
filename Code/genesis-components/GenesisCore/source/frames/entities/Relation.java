package frames.entities;

import java.util.*;

import bryanWilliams.Util;

/**
 * Implements relations.
 * 
 * @author Patrick Winston
 */

/*
 * Edited on 8 July 2013 by ahd
 */

public class Relation extends Function {

	public static final String PARTICLE = "particle";

	public static final String MODAL = "modal";

	public static final String NOT = "not";

	public static final String SHOULD = "should";
	
	public static enum Transitivity {
	    TRANSITIVE,
	    INTRANSITIVE;	    
	}

	/**
	 * Key methods, specified in interface.
	 */

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
		return true;
	}

	public boolean relationP(String type) {
		return this.isAPrimed(type);
	}

	public boolean relationP(List<String> types) {
		return types.stream().anyMatch(t -> this.isAPrimed(t));
	}

	public boolean sequenceP() {
		return false;
	}

	public boolean sequenceP(String type) {
		return false;
	}

	public void setObject(Entity t) {
		saveState();
		object = t;
		if (t != null) t.addObjectOf(this);
		fireNotification();
	}

	public Entity getObject() {
		return object;
	}

	public Set<Entity> getChildren() {
		Set<Entity> result = super.getChildren();
		result.add(getObject());
		return result;
	}

	public int getNumberOfChildren() {
		return 2;
	}

	/**
	 * Used to test equality without regard to ID numbers. Does not override equals(Object) so it won't interfere with
	 * hashing.
	 * 
	 * @see frames.entities.Entity#isEqual(java.lang.Object) isEqual
	 * @author M.A. Finlayson
	 * @since Feb 15, 2004; JDK 1.4.2
	 */

	public boolean isEqual(Object o) {
		if (o instanceof Relation) {
			Relation r = (Relation) o;
			if (r.getObject().isEqual(this.getObject())) {
				return super.isEqual(r);
			}
		}
		return false;
	}

	/**
	 * End of key methods, specified in interface.
	 */

	protected Entity object;

	/**
	 * Constructs a new relation, given a subject and an object.
	 */
	public Relation(Entity subject, Entity object) {
		super(subject);
		setObject(object);
	}

	public Relation(Thread thread, Entity subject, Entity object) {
		this(subject, object);
		setBundle(new Bundle(new Thread(thread)));
	}

	public Relation(Bundle b, Entity subject, Entity object) {
		this(subject, object);
		setBundle(b);
	}

	/**
	 * Constructs a new relation, given a type, a subject and an object.
	 */
	public Relation(String type, Entity subject, Entity object) {
		this(subject, object);
		addType(type);
	}

	/**
	 * Constructs object with a name determined by suffix string provided; used only in reading. Adds new object
	 * instance to an instance list for later retrieval. Creates thread bundle.
	 */
	public Relation(boolean readOnly, String suffix, Entity subject, Entity object) {
		super(readOnly, suffix, subject);
		setObject(object);
	}

	/*
	 * (non-Javadoc)
	 * @see bridge.reps.entities.Frame#getAllComponents()
	 */
	public List<Entity> getAllComponents() {
		List<Entity> result = super.getAllComponents();
		result.add(getObject());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see bridge.reps.entities.Frame#getPrettyPrintType()
	 */
	public char getPrettyPrintType() {
		return TYPECHAR_RELATION;
	}

	/**
	 * Helps produce print string.
	 */
	// protected String filler () {return
	// filler(Thing.defaultToStringIsCompact);}
	protected String filler(boolean compact) {
		if (compact) {
			String s = ((Entity) subject).toXML(compact).replaceFirst("\n", " (subject)\n");
			String o = ((Entity) object).toXML(compact).replaceFirst("\n", " (object)\n");
			return s + "\n" + o;
		}
		else {
			return Tags.tag("subject", ((Entity) subject).toXML(compact)) + Tags.tag("object", object.toXML(compact));
		}
	}

	/**
	 * 
	 */
	protected String fillerSansName(boolean compact) {
		if (compact) {
			String s = "";
			String o = "";
			if (subject != null) {
				s = ((Entity) subject).toXMLSansName(compact).replaceFirst("\n", " (subject)\n");
			}
			if (object != null) {
				o = ((Entity) object).toXMLSansName(compact).replaceFirst("\n", " (object)\n");
			}
			return s + "\n" + o;
		}
		else {
			String s = "";
			String o = "";
			if (subject != null) {
				s = Tags.tag("subject", ((Entity) subject).toXMLSansName(compact));
			}
			if (object != null) {
				o = Tags.tag("object", ((Entity) object).toXMLSansName(compact));
			}
			return s + o;
		}
	}

	/**
	 * Copies class and modifiers, leaves rest alone
	 */
	public Relation rebuild() {

		Relation result = new Relation(getSubject(), getObject());
		this.transferThreadsFeaturesAndProperties(result);

		// Mark.say("Before rebuilding relation\n", this.getPropertyList(), "\n", this);
		// Mark.say("After rebuilding relation\n", result.getPropertyList(), "\n", result);

		return result;
	}

	/**
	 * Clones (shallow).
	 */
	public Object clone(EntityFactory factory) {
		Relation relation = factory.newRelation(getSubject(), getObject());
		Bundle bundle = (Bundle) (getBundle().clone());
		relation.setBundle(bundle);

		Vector v = getModifiers();
		for (int i = 0; i < v.size(); i++) {
			Entity t = (Entity) v.elementAt(i);
			relation.addModifier(t);
		}
		return relation;
	}

	/**
	 * Clones, recursively.
	 */
	protected Entity deepClone(EntityFactory factory, IdentityHashMap<Entity, Entity> cloneMap, boolean newId) {
		if (cloneMap.containsKey(this)) {
			return (Entity) cloneMap.get(this);
		}

		Entity subjectClone = (Entity) (getSubject().deepClone(factory, cloneMap, newId));
		Entity objectClone = (Entity) (getObject().deepClone(factory, cloneMap, newId));

		Relation clone = factory.newRelation(subjectClone, objectClone);
		if (!newId) {
			clone.setNameSuffix(getNameSuffix());
		}

		Bundle bundleClone = (Bundle) (getBundle().clone());
		clone.setBundle(bundleClone);

		Vector modifiers = getModifiers();
		for (int i = 0; i < modifiers.size(); i++) {
			Entity modifier = (Entity) modifiers.elementAt(i);
			Entity modifierClone = (Entity) modifier.deepClone(factory, cloneMap, newId);
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
		Relation newRelation = (Relation) (clone(factory));
		Entity newSubject = (Entity) (getSubject().cloneForResolver(factory));
		Entity newObject = (Entity) (getObject().cloneForResolver(factory));
		newRelation.setSubject(newSubject);
		newRelation.setObject(newObject);

		Vector v = getModifiers();
		for (int i = 0; i < v.size(); i++) {
			Entity t = (Entity) v.elementAt(i);
			Entity tClone = (Entity) t.cloneForResolver(factory);
			newRelation.addModifier(tClone);
		}

		// newRelation.forget();
		return newRelation;
	}

	/**
	 * The Modal Thread will store all modals and particles such as "not" or "should", with respect to this Relation The
	 * following methods allow you to manipulate the thread
	 */

	public Thread getModalThread() {
		return getThread(Relation.MODAL);
	}

	public void setModalThread(Thread t) {
		if (getModalThread() != null) {
			getBundle().removeThread(getModalThread());
		}
		addThread(t);
	}

	public boolean isInModalThread(String s) {
		if (getModalThread() == null) {
			return false;
		}
		return getModalThread().contains(s);
	}

	public void addToModalThread(String s) {
		if (getModalThread() == null) {
			addThread(new Thread(Relation.MODAL));
		}
		getModalThread().addType(s);
	}

	public void removeFromModalThread(String s) {
		if (getModalThread() != null) {
			getModalThread().remove(s);
		}
	}

	public void negate() {
		this.addToModalThread(Relation.NOT);
	}

	public boolean isNegated() {
		return this.isInModalThread(Relation.NOT);
	}
	
	public Transitivity getTransitivity() {
	    return Util.getTransitiveRelationDirectObjects(this).size() > 0 ? Transitivity.TRANSITIVE : Transitivity.INTRANSITIVE;
	}

	/**
	 * Tests behavior.
	 */
	public static void main(String argv[]) {

		EntityFactory factory = EntityFactoryDefault.getInstance();
		Entity t1 = factory.newThing();
		t1.addType("Mark");
		Entity t2 = factory.newThing();
		t2.addType("Steph");
		Relation r = factory.newRelation(t1, t2);
		r.addType("Siblings");

		Relation r2 = (Relation) r.clone();
		System.out.println(r);
		System.out.println(r.clone());
		System.out.println("Equals? " + r.isEqual(r2));
		System.out.println(r.cloneForResolver());

		t1 = new Entity("John");
		t2 = new Entity("Mary");
		r = new Relation("kissed", t1, t2);

		r.addToModalThread("able");

		if (r.isInModalThread("able")) {
			System.out.println("Can't even do it");
		}
		r.removeFromModalThread("able");

		if (r.isInModalThread("able")) {
			System.out.println("Can't even do it");
		}

	}

}
