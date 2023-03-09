package frames.entities;

import java.util.*;

/**
 * Implements Derivatives.
 * 
 * @author Patrick Winston
 */

/*
 * Edited on 8 July 2013 by ahd
 */

@SuppressWarnings("serial")
public class Function extends Entity {

	/**
	 * Key methods, specified in Frame interface.
	 */

	public boolean entityP() {
		return false;
	}

	public boolean entityP(String type) {
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

	public Function() {
	}

	public Function(boolean readOnly, String suffix) {
		super(readOnly, suffix);
	}

	/**
	 * Key methods, specified in interface.
	 */

	public boolean functionP() {
		return true;
	}

	public boolean functionP(String type) {
		return this.isAPrimed(type);
	}

	public Entity getSubject() {
		return subject;
	}

	public Set<Entity> getChildren() {
		Set<Entity> result = new HashSet<Entity>();
		result.add(getSubject());
		return result;
	}

	public int getNumberOfChildren() {
		return 1;
	}

	public void setSubject(Entity t) {
		saveState();
		subject = t;
		if (t != null) ((Entity) t).addSubjectOf(this);
		fireNotification();
	}

	public boolean isEqual(Object o) {
		if (o instanceof Function) {
			Function d = (Function) o;
			if (d.getSubject().isEqual(this.getSubject())) {
				return super.isEqual(d);
			}
		}
		return false;
	}

	/**
	 * End of key methods, specified in interface.
	 */

	protected Entity subject;

	public Function(Entity t) {
		setSubject(t);
	}

	public Function(String string, Entity t) {
		this(t);
		addType(string);
	}

	public Function(Thread thread, Entity t) {
		this(t);
		setBundle(new Bundle(new Thread(thread)));
	}

	public Function(Bundle b, Entity t) {
		this(t);
		setBundle(b);
	}

	/**
	 * Constructs object with a name determined by suffix string provided; used only in reading. Adds new object
	 * instance to an instance list for later retrieval. Creates thread bundle.
	 */
	public Function(boolean readOnly, String suffix, Entity subject) {
		super(readOnly, suffix);
		setSubject(subject);
	}

	/*
	 * (non-Javadoc)
	 * @see bridge.reps.entities.Frame#getAllComponents()
	 */
	public List<Entity> getAllComponents() {
		List<Entity> result = super.getAllComponents();
		result.add(getSubject());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see bridge.reps.entities.Frame#getPrettyPrintType()
	 */
	public char getPrettyPrintType() {
		return TYPECHAR_FUNCTION;
	}

	/**
	 * Helps produce print string.
	 */
	// protected String filler () {return
	// filler(Thing.defaultToStringIsCompact);}
	protected String filler(boolean compact) {
		if (compact) {
			return ((Entity) subject).toXML(compact).replaceFirst("\n", " (subject)\n") + "\n";
		}
		else {
			return Tags.tag("subject", ((Entity) subject).toXML(compact));
		}
	}

	/**
	 * Assures no calls to getName().
	 * 
	 * @author M.A. Finlayson
	 * @since 14 Jan, 2003; JDK 1.4.2
	 */
	protected String fillerSansName(boolean compact) {
		if (subject != null) {
			if (compact) {
				return ((Entity) subject).toXMLSansName(compact).replaceFirst("\n", " (subject)\n") + "\n";
			}
			else {
				return Tags.tag("subject", ((Entity) subject).toXMLSansName(compact));
			}
		}
		return "";
	}

	/**
	 * Copies class and modifiers, leaves rest alone
	 */

	public Function rebuild() {
		Function result = new Function(getSubject());
		this.transferThreadsFeaturesAndProperties(result);
		return result;
	}

	/**
	 * Clones (shallow).
	 */
	public Object clone(EntityFactory factory) {
		Function derivative = factory.newDerivative(getSubject());
		Bundle bundle = (Bundle) (getBundle().clone());
		derivative.setBundle(bundle);

		Vector<Entity> v = getModifiers();
		for (int i = 0; i < v.size(); i++) {
			Entity t = (Entity) v.elementAt(i);
			derivative.addModifier(t);
		}

		return derivative;
	}

	/**
	 * Clones, recursively.
	 */
	protected Entity deepClone(EntityFactory factory, IdentityHashMap<Entity, Entity> cloneMap, boolean newId) {
		if (cloneMap.containsKey(this)) {
			return (Entity) cloneMap.get(this);
		}

		Entity subjectClone = (Entity) (getSubject().deepClone(factory, cloneMap, newId));

		Function clone = factory.newDerivative(subjectClone);
		if (!newId) {
			clone.setNameSuffix(getNameSuffix());
		}
		Bundle bundleClone = (Bundle) (getBundle().clone());
		clone.setBundle(bundleClone);

		Vector<Entity> modifiers = getModifiers();
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
		Function newDerivative = (Function) (clone(factory));
		Entity newSubject = (Entity) (getSubject().cloneForResolver(factory));
		newDerivative.setSubject(newSubject);

		Vector<Entity> v = getModifiers();
		for (int i = 0; i < v.size(); i++) {
			Entity t = (Entity) v.elementAt(i);
			Entity tClone = (Entity) t.cloneForResolver(factory);
			newDerivative.addModifier(tClone);
		}

		// newDerivative.forget();
		return newDerivative;
	}

	/**
	 * Tests behavior.
	 */
	public static void main(String argv[]) {
		EntityFactory factory = EntityFactoryDefault.getInstance();
		Entity t = factory.newThing();
		t.addType("Mark");
		Function d = factory.newDerivative(t);
		d.addType("was here");
		System.out.println(d);
		Function d2 = (Function) d.clone();
		System.out.println(d2);
		System.out.println("Equal? " + d.isEqual(d2));
		System.out.println(d.cloneForResolver());
	}
}
