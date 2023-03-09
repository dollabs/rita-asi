package frames.entities;

import java.util.*;

import com.thoughtworks.xstream.mapper.CGLIBMapper.Marker;

import constants.Markers;
import utils.logging.Level;
import utils.logging.Logger;
import utils.logging.*;

/**
 * Implements behavior of a bundle of threads. The first thread is the "primed" thread, the one returned by the
 * getPrimedThread method. At construction time, the primed thread is the one and only thread created. [When adding or
 * deleting threads from bundles it is important to use the addThread() or removeThread() methods, rather than the
 * standard Vector methods such as add() or remove(), to ensure that all threads are properly registered to the bundles
 * of which they are a part. -- MAF.13.Jan.04]
 * 
 * @author Patrick Winston
 */

/*
 * Edited on 8 July 2013 by ahd
 */

public class Bundle extends Vector<Thread> {

	/**
	 * The thing to which this bundle belongs.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	private Entity ownerThing = null;

	/**
	 * Constructs Bundle.
	 */
	public Bundle() {
	}

	public Bundle(Thread t) {
		this();
		addThread(t);
	}

	public Bundle copy() {
		Bundle result = new Bundle();
		result.addAll(this);
		return result;
	}

	/**
	 * Clones the bundle deeply (including all threads).
	 */
	@Override
	public Object clone() {
		Bundle b = new Bundle();
		for (int i = 0; i < size(); i++) {
			b.add(((Thread) get(i)).copyThread());
		}
		return b;
	}

	public Bundle getClone() {
		return (Bundle) (this.clone());
	}

	public Bundle getThingClones() {
		Bundle bundle = new Bundle();
		for (Thread t : this) {
			if (t.contains(Entity.MARKER_ENTITY) || t.contains(Entity.MARKER_ACTION) || t.contains(Markers.DESCRIPTOR)) {
				bundle.add((Thread) (t.clone()));
			}
		}
		return bundle;
	}

	public Bundle getAllClones() {
		Bundle bundle = new Bundle();
		for (Thread t : this) {
			bundle.add((Thread) (t.clone()));
		}
		return bundle;
	}

	/**
	 * Returns the thing which owns this bundle.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	public Entity getOwnerThing() {
		return ownerThing;
	}

	public void setOwnerThing(Entity t) {
		if (ownerThing == t) {
			return;
		}

		if (ownerThing != null) {
			ownerThing.setBundle(null);
		}

		ownerThing = t;
	}

	public void setOwnerThingNull() {
		ownerThing = null;
	}

	/**
	 * Adds thread to bundle, and makes that thread the "primed" thread. [Moreover, takes control of the thread by
	 * setting the owner bundle of the thread. (MAF.13.Jan.04)]
	 */
	public boolean addThread(Thread t) {
		add(0, t);
		t.setOwnerBundle(this);
		return true;
	}

	/**
	 * Removes the named thread from the bundle. If the named thread is not on the bundle, returns <b>false</b>. Sets
	 * the owner bundle of the named thread to <b>null</b>.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	public boolean removeThread(Thread t) {
		boolean val = remove(t);
		if (val) {
			t.setOwnerBundleNull();
		}
		return val;
	}

	/**
	 * Adds thread to bundle, but does not make that thread the "primed" thread. [Moreover, takes control of the thread
	 * by setting the owner bundle of the thread. (MAF.13.Jan.04)]
	 */
	public boolean addThreadAtEnd(Thread t) {
		add(t);
		t.setOwnerBundle(this);
		return true;
	}

	/**
	 * Copies primed thread and makes that new thread the new primed thread. Used for creating contexts.
	 */
	public void pushPrimedThread() {
		addThread((Thread) (getPrimedThread().clone()));
	}

	/**
	 * Swaps primed thread with next thread in the bundle.
	 */
	public void swapPrimedThread() {
		if (size() < 2) {
			return;
		}
		Thread object = elementAt(1);
		removeElementAt(1);
		add(0, object);
	}

	/**
	 * Sends primed thread to end of the bundle.
	 */
	public void sendPrimedThreadToEnd() {
		if (size() > 0) {
			Thread thread = (Thread) (elementAt(0));
			add(thread);
			remove(0);
		}
	}

	/**
	 * Gets prime thread. Adds a thread, if none so far.
	 */
	public Thread getPrimedThread() {
		if (size() == 0) {
			addThread(new Thread());
		}
		return (Thread) firstElement();
	}

	/**
	 * Sets "primed" thread.
	 */
	public void setPrimedThread(int i) {
		if (i >= 0 || i < size()) {
			Thread t = (Thread) elementAt(i);
			removeElementAt(i);
			add(0, t);
		}
	}

	public void setPrimedThread(Thread thread) {
		int index = this.indexOf(thread);
		if (index >= 0) {
			setPrimedThread(index);
		}
		else {
			this.add(0, thread);
		}
	}

	/**
	 * Gets a particular thread, named by string.
	 */
	public Thread getThread(String firstElement) {
		for (int i = 0; i < size(); ++i) {
			Thread thread = (Thread) (elementAt(i));
			if (!thread.isEmpty()) {
				String first = (String) (thread.firstElement());
				if (firstElement.equals(first)) {
					return thread;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the first thread which contains the specified element.
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 8, 2004; JDK 1.4.2
	 */
	public Thread getThreadContaining(String element) {
		Thread result = null, thread;
		for (int i = 0; i < size(); ++i) {
			thread = (Thread) (elementAt(i));
			if (thread.contains(element)) {
				result = thread;
				break;
			}
		}
		return result;
	}

	public Bundle filterFor(String element) {
		Bundle b = new Bundle();
		for (int i = 0; i < size(); ++i) {
			Thread thread = (Thread) (elementAt(i));
			if (thread.contains(element)) {
				b.add(thread);
			}
		}
		return b;
	}

	public Bundle filterForRoot(String element) {
		Bundle b = new Bundle();
		for (int i = 0; i < size(); ++i) {
			Thread thread = (Thread) (elementAt(i));
			if (thread.size() > 0 && thread.get(0).equals(element)) {
				b.add(thread);
			}
		}
		return b;
	}

	public Bundle filterFor(List<String> elements) {
		Bundle b = new Bundle();
		for (String element : elements) {
			for (int i = 0; i < size(); ++i) {
				Thread thread = (Thread) (elementAt(i));
				if (thread.contains(element) && !b.contains(thread)) {
					b.add(thread);
				}
			}
		}
		return b;
	}

	public Bundle filterForNot(String element) {
		Bundle b = new Bundle();
		for (int i = 0; i < size(); ++i) {
			Thread thread = (Thread) (elementAt(i));
			if (!thread.contains(element)) {
				b.add(thread);
			}
		}
		return b;
	}

	/**
	 * Adds type to end of primed thread.
	 */
	public void addType(String t) {
		getPrimedThread().addType(t);
	}

	/**
	 * Gets type from primed thread.
	 */
	public String getType() {
		return getPrimedThread().getType();
	}

	/**
	 * Returns all types in the bundle
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 1, 2005; JDK 1.4.2
	 */
	public String[] getAllTypes() {
		return getAllTypesExcept(null);
	}

	/**
	 * Returns all types in the bundle except those on the specified Threads
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 1, 2005; JDK 1.4.2
	 */
	public String[] getAllTypesExcept(String[] ignoreTheseThreads) {
		Set<String> types = new HashSet<String>();
		Thread thread;
		for (Iterator i = iterator(); i.hasNext();) {
			thread = (Thread) i.next();
			if (thread.contains(ignoreTheseThreads)) continue;
			types.addAll(thread);
		}
		return (String[]) types.toArray(new String[types.size()]);
	}

	/**
	 * Gets the supertype from the primed thread.
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 8, 2004; JDK 1.4.2
	 */
	public String getSupertype() {
		return getPrimedThread().getSupertype();
	}

	/**
	 * Prunes out redundant threads. For example, <br>
	 * thing animal person man, thing animal person man, thing person man, becomes <br>
	 * thing animal person man
	 */
	public void prune() {
		// System.out.println("Pruning " + this);
		Vector<Thread> result = new Vector<Thread>();
		for (int i = 0; i < size(); ++i) {
			Thread t = (Thread) (elementAt(i));
			if (!bottomSubThread(t)) {
				if (!result.contains(t)) {
					result.add(t);
				}
			}
		}
		clear();
		addAll(result);
		// System.out.println("...to " + this);
	}

	/**
	 * Indicates that a thread in the bundle has been modified.
	 * 
	 * @param t
	 *            The thread calling the method.
	 * @param oldState
	 *            String representation of the old state of the thread.
	 * @param newState
	 *            String representation of the new state of the thread.
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	protected void threadModified(Thread t, String oldState, String newState) {
		// finest("\nThread modified from:\n-- " + oldState + "\n--(to:) " +
		// newState + "\n--");
		fireNotification();
	}

	/**
	 * Indicates whether the string form should be save when firing notifications
	 */
	boolean saveStringForm = false;

	public void setSaveStringForm(boolean b) {
		saveStringForm = b;
	}

	public void setSaveStringFormOnDependents(boolean b) {
		setSaveStringForm(b);
		for (int i = 0; i < size(); i++) {
			((Thread) get(i)).setSaveStringFormOnDependents(b);
		}
	}

	public boolean getSaveStringForm() {
		return saveStringForm;
	}

	/**
	 * Used to suppress notification. Notification starts turned off -- when this bundle (via its owner thing) is added
	 * to a memory which cares about notification, that memory turns it on.
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
		for (int i = 0; i < size(); i++) {
			((Thread) get(i)).setNotificationOnDependents(b);
		}
	}

	/**
	 * Contains the last saved state of the bundle in string form.
	 */
	protected String previousState = null;

	/**
	 * Saves the state of the bundle in string form
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	protected void saveState() {
		if (!notification) {
			return;
		}
		if (saveStringForm) {
			previousState = this.toString();
		}
		if (getOwnerThing() != null) {
			getOwnerThing().saveState();
		}
	}

	/**
	 * Notifies the owning thing that the bundle has changed.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	protected void fireNotification() {
		if (getOwnerThing() != null) {
			if (saveStringForm) {
				getOwnerThing().bundleModified(this, previousState, this.toString());
			}
			else {
				getOwnerThing().bundleModified(this, null, null);
			}
		}
	}

	private boolean bottomSubThread(Thread t) {
		// System.out.println("Checking " + t);
		Vector v = (Vector) this;
		for (int i = 0; i < v.size(); ++i) {
			Thread t2 = (Thread) (v.elementAt(i));
			// Cannot be bottomSubTread of self
			if (t.equals(t2)) {
			}
			else if (bottomSubThread(t, t2)) {
				return true;
			}
		}
		return false;
	}

	private static boolean bottomSubThread(Thread t1, Thread t2) {
		// System.out.println("Checking " + t1 + " versus " + t2);
		int i1 = t1.size();
		int i2 = t2.size();
		if (i2 < i1) {
			return false;
		}
		else if (i1 == 0 && i2 == 0) {
			return false;
		}
		int delta = i2 - i1;
		String zero1 = (String) (t1.elementAt(0));
		String zero2 = (String) (t2.elementAt(0));
		if (!zero1.equalsIgnoreCase(zero2)) {
			return false;
		}
		for (int i = 1; i < t1.size(); ++i) {
			String s1 = (String) (t1.elementAt(i));
			String s2 = (String) (t2.elementAt(i + delta));
			if (!s1.equalsIgnoreCase(s2)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Produces print string.
	 */
	public String toString() {
		return toString(Entity.defaultToXMLIsCompact);
	}

	public String toString(boolean compact) {
		if (compact) {
			String s = "(";
			Iterator i = iterator();
			while (i.hasNext()) {
				s += ((Thread) i.next()).toString(compact);
				if (i.hasNext()) s += ", ";
			}
			s += ")";
			return s;
		}
		else {
			return Tags.tagNoLine("bundle", Tags.tag(this));
		}
	}

	public static String showDifferences(Bundle f, Bundle s) {
		String result = null;

		if (f.size() != s.size()) {
			if (result == null) {
				result = "";
			}
			result = result + f + " and " + s + "\nare of different sizes\n";
		}
		else {
			Thread ft, st;
			String diff;
			for (int i = 0; i < f.size(); i++) {
				ft = (Thread) f.get(i);
				st = (Thread) s.get(i);
				diff = Thread.showDifferences(ft, st);
				if (diff != null) {
					if (result == null) {
						result = "";
					}
					result = result + diff + "\n";
				}
			}
		}

		return result;
	}

	/**
	 * Extracts Thread from printed form.
	 */
	public static Bundle extractInstance(String s) {
		Bundle result = new Bundle();
		String x = Tags.untagString("bundle", s).trim();
		for (IteratorForXML iterator = new IteratorForXML(x, "thread"); iterator.hasNext();) {
			Thread thread = Thread.extractInstance(iterator.next());
			result.add(thread);
		}
		return result;
	}

	/*
	 * The methods below have been overridden so that the bundle can notify it's owner thing if it has been changed. --
	 * MAF.13.Jan.04
	 */

	public void add(int index, Thread element) {
		// This method does not require overriding, since it calls
		// insertElementAt(Object, int)
		// internally
		super.add(index, element);
	}

	public boolean add(Thread o) {
		saveState();
		boolean val = super.add(o);
		if (val) {
			fireNotification();
		}
		return val;
	}

	public boolean addAll(Collection<? extends Thread> c) {
		saveState();
		boolean val = super.addAll(c);
		if (val) {
			fireNotification();
		}
		return val;
	}

	public boolean addAll(int index, Collection<? extends Thread> c) {
		saveState();
		boolean val = super.addAll(index, c);
		if (val) {
			fireNotification();
		}
		return val;
	}

	public void addElement(Thread obj) {
		saveState();
		super.addElement(obj);
		fireNotification();
	}

	public void clear() {
		// This method does not require overriding, since it calls
		// removeAllElements()
		// internally
		super.clear();
	}

	public void insertElementAt(Thread obj, int index) {
		saveState();
		super.insertElementAt(obj, index);
		fireNotification();
	}

	public Thread remove(int index) {
		saveState();
		Thread obj = super.remove(index);
		if (obj != null) {
			fireNotification();
		}
		return obj;
	}

	public boolean remove(Object o) {
		// This method does not require overriding, since it calls
		// removeElement(Object)
		// internally
		return super.remove(o);
	}

	public boolean removeAll(Collection c) {
		saveState();
		boolean val = super.removeAll(c);
		if (val) {
			fireNotification();
		}
		return val;
	}

	public void removeAllElements() {
		saveState();
		super.removeAllElements();
		fireNotification();
	}

	public boolean removeElement(Object obj) {
		// This method does not require overriding, since it calls
		// removeElementAt(Object, int)
		// internally
		return super.removeElement(obj);
	}

	public void removeElementAt(int index) {
		saveState();
		super.removeElementAt(index);
		fireNotification();
	}

	protected void removeRange(int fromIndex, int toIndex) {
		saveState();
		super.removeRange(fromIndex, toIndex);
		fireNotification();
	}

	public boolean retainAll(Collection c) {
		saveState();
		boolean val = super.retainAll(c);
		if (val) {
			fireNotification();
		}
		return val;
	}

	public Thread set(int index, Thread element) {
		saveState();
		Thread obj = super.set(index, element);
		if (obj != null) {
			fireNotification();
		}
		return obj;
	}

	public void setElementAt(Thread obj, int index) {
		saveState();
		super.setElementAt(obj, index);
		fireNotification();
	}

	/**
	 * Tests behavior.
	 */
	public static void main(String argv[]) {

		System.out.println("Show differences");
		Bundle b = new Bundle();
		b.addType("Thing");
		b.addType("Animal");
		b.addType("Person");
		b.addType("Jerk");
		// System.out.println(b);
		Thread t = new Thread();
		t.addType("Thing");
		t.addType("Person");
		t.addType("Jerk");
		b.addThread(t);
		// b.prune();

		Bundle c = new Bundle();
		c.addType("Thing");
		c.addType("Animal");
		c.addType("Person");
		c.addType("Jerk");
		// System.out.println(b);
		Thread s = new Thread();
		s.addType("Thing");
		s.addType("Person");
		s.addType("Asshole");
		c.addThread(s);
		c.prune();
		System.out.println(showDifferences(b, c));
		// System.out.println(b);
		// System.out.println(Bundle.extractInstance(b.toString()));

		System.out.println("\n\n---------------Testing Thing/Bundle ownership. (MAF.13.Jan.04)");
		Entity.getLogger().setLevel(Level.All);

		Entity g1 = new Entity("Mark");
		Entity g2 = new Entity("Steph");
		Bundle b1 = g1.getBundle();
		Bundle b2 = g2.getBundle();
		System.out.println("\nThing 1: " + g1);
		System.out.println("Bundle 1: " + b1);
		System.out.println("Bundle 1 owner id: " + b1.getOwnerThing().getID());
		System.out.println("\nThing 2: " + g2);
		System.out.println("Bundle 2: " + b2);
		System.out.println("Bundle 2 owner id: " + b2.getOwnerThing().getID());

		System.out.println("\nSwitching bundle 2 to Thing 1");
		g1.setBundle(b2);
		System.out.println("\nThing 1: " + g1);

		System.out.println("\nPutting bundle 1 on Thing 2");
		g2.setBundle(b1);
		System.out.println("\nThing 2: " + g2);

		System.out.println("\n\n---------------Testing Derivative/Relation. (MAF.14.Jan.04)");

		System.out.println("Creating derivative...");
		Function d1 = new Function("born", g1);
		System.out.println("Creating relation...");
		Relation r1 = new Relation("sibling", g1, g2);
		System.out.println("Creating sequence...");
		Sequence s1 = new Sequence("birth");
		s1.addElement(d1);
		s1.addElement(r1);
	}

	// Debugging section
	public static final String LOGGER_GROUP = "things";

	public static final String LOGGER_INSTANCE = "Bundle";

	public static final String LOGGER = LOGGER_GROUP + "." + LOGGER_INSTANCE;

	protected static Logger getLogger() {
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

	public boolean hasThread(Thread t) {
		for (Thread x : this) {
			if (x.equals(t)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof Bundle)) {
			return false;
		}
		
		Bundle b = (Bundle)other;
		if (this.size() != b.size()) {
			return false;
		}
		for (int i = 0; i < b.size(); ++i) {
			if (!this.get(i).equals(b.get(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		// Order matters
		int hash = -17; 
		for(int i = 0; i < this.size(); i++) {
			// May want to hash the int itself, instead of doing i+29
			hash *= (i + 29) * this.get(i).hashCode();
		}
		return hash;
	}

}
