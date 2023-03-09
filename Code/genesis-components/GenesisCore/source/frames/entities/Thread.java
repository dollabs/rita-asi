package frames.entities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import utils.logging.Logger;

/**
 * Helps to implement thread behavior, derived from Greenblatt and Vaina.
 * 
 * @author Patrick Winston
 */

/*
 * Edited on 8 July 2013 by ahd
 */

@SuppressWarnings("serial")
public class Thread extends Vector<String> {

	private Bundle ownerBundle = null;

	public static Thread parse(String s) {
		Thread th = new Thread();
		List<String> types = Arrays.asList(s.split("\\s+"));
		for (String type : types) {
			th.addType(type);
		}
		return th;
	}

	public static Thread constructThread(String... s) {
		Thread th = new Thread();
		for (String x : s) {
			th.addType(x);
		}
		return th;
	}

	public static Thread constructThread(Vector<String> v) {
		Thread th = new Thread();
		for (String x : v) {
			th.addType(x);
		}
		return th;
	}
	
	/**
	 * Constructs a new thread from an old thread with start and 
	 * end offsets.
	 * @author jb
	 * @param t the thread to copy from
	 * @param start start index, inclusive
	 * @param end end index, exclusive
	 * @return 
	 */
	public static Thread constructThread(Thread t, int start, int end) {
		Thread th = new Thread();
		for(int i = start; i < end; i++) {
			// th.addType removes duplicate class names which is stupid. Don't use that.
			th.add(t.get(i));
		}
		return th;
	}

	/**
	 * The bundle to which this thread belongs.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */

	/** Constructs a thread with no specified type. */
	public Thread() {
		super();
	}

	public Thread(Thread t) {
		super(t);
	}

	public Object clone() {
		return new Thread(this);
	}

	public Thread copyThread() {
		Thread result = (Thread) (clone());
		return result;
	}

	/**
	 * Constructs a thread with starting type as specified. --MAF.14.Feb.04
	 */
	public Thread(String type) {
		this();
		addType(type);
	}

	/**
	 * Returns the first type on the thread.
	 * 
	 * @author M.A. Finlayson
	 * @since Aug 20, 2004; JDK 1.4.2
	 */
	public String getThreadType() {
		if (size() > 0) {
			return (String) get(0);
		}
		else {
			return null;
		}
	}

	/**
	 * Adds class to thread; makes this class the class returned by getType. If present, deletes and adds at the bottom.
	 */
	public void addType(String t) {
		if (contains(t)) {
			finest("Type " + t + "is contained.");
			remove(t);
		}
		finest("Adding type: " + t);
		add(t);
	}

	/**
	 * Adds class to the top of the thread. If already present on the thread, it is deleted first.
	 */
	public void addTypeFront(String t) {
		if (contains(t)) {
			finest("Type " + t + "is contained.");
			remove(t);
		}
		finest("Adding type: " + t);
		add(0, t);
	}

	/** Gets the final class associated with the thread. */
	public String getType() {
		if (size() == 0) {
			return "no type";
		}
		else {
			return (String) lastElement();
		}
	}

	/**
	 * Gets the second to final class associated with the thread.
	 * 
	 * @author M.A. Finlayson
	 * @since Mar 8, 2004; JDK 1.4.2
	 */
	public String getSupertype() {
		if (size() < 2) {
			return null;
		}
		else {
			String supertype = (String) get(size() - 2);
			return supertype;
		}
	}

	/**
	 * Returns the bundle to which this thread belongs.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	public Bundle getOwnerBundle() {
		return ownerBundle;
	}

	/**
	 * Sets the bundle to which this thread belongs. This methods should only be called via calls to a Bundle's
	 * addThread() or removeThread() methods. This will ensure that Threads are properly registered or deregistered from
	 * bundles.
	 * 
	 * @param b
	 *            Bundle to which the thread will now belong.
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	public void setOwnerBundle(Bundle b) {
		if (ownerBundle == b) {
			return;
		}

		if (ownerBundle != null) {
			if (!ownerBundle.removeThread(this)) {
				warning("Unable to remove thread from bundle!");
				return;
			}
		}

		ownerBundle = b;
	}

	public void setOwnerBundleNull() {
		ownerBundle = null;
	}

	/** Checks for containment ignoring case. */
	public boolean contains(Object object) {
		try {
			if (!(object instanceof String)) {
				return false;
			}
			String type = (String) object;
			return super.contains(type);
			// for (int i = 0; i < size(); ++i) {
			// String string = (String) (elementAt(i));
			// if (string.equals(type)) {
			// return true;
			// }
			// }
		}
		catch (Exception e) {
			System.err.println("Blew out in Thread.contains");
			// System.err.println("Blew out in contains: " + this + ", " + object);
			// e.printStackTrace();
		}
		return false;
	}

	/**
	 * Checks for containment of an array of objects. If the thread contains any one of the objects, returns true.
	 */
	public boolean contains(Object[] objects) {
		if (objects == null) return false;
		for (int i = 0; i < objects.length; i++) {
			if (contains(objects[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true only if the thread contains all of the indicated objects.
	 * 
	 * @author M.A. Finlayson
	 * @since Aug 20, 2004; JDK 1.4.2
	 */
	public boolean containsAll(Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (!contains(objects[i])) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Provides print string.
	 */
	public String toString() {
		return toString(Entity.defaultToXMLIsCompact);
	}

	public String toString(boolean compact) {
		if (compact) {
			String s = "";
			Iterator<String> i = iterator();
			while (i.hasNext()) {
				s += i.next();
				if (i.hasNext()) s += " ";
			}
			return s;
		}
		else {
			return Tags.tagNoLine("thread", Tags.tag(this));
		}
	}

	/**
	 * Get's string representation of thread.
	 */
	public String getString() {
		String result = "";
		for (int i = 0; i < this.size(); ++i) {
			result += (String) (this.elementAt(i));
			if (i < this.size() - 1) {
				result += " ";
			}
		}
		return result;
	}

	/**
	 * Yet another option
	 * 
	 * @return
	 */
	public String asString() {
		if (this.isEmpty()) {
			return "<Thread: Empty>";
		}
		String result = "<Thread: " + this.get(0);
		for (int i = 1; i < this.size(); ++i) {
			result += "---" + this.get(i);
		}
		return result + ">";
	}

	/**
	 * Extracts Thread from printed form.
	 */
	public static Thread extractInstance(String s) {
		Thread result = new Thread();
		String x = Tags.untagString("thread", s).trim();
		while (x.length() > 0) {
			int index = x.indexOf(' ');
			if (index < 0) {
				result.add(x);
				break;
			}
			else {
				result.add(x.substring(0, index));
				x = x.substring(index + 1).trim();
			}
		}
		return result;
	}

	public static String showDifferences(Thread f, Thread s) {
		String result = null;

		if (f.size() != s.size()) {
			if (result == null) {
				result = "";
			}
			result = result + f + " and " + s + "\nare of different sizes\n";
		}
		else {
			String ft, st;
			for (int i = 0; i < f.size(); i++) {
				ft = (String) f.get(i);
				st = (String) s.get(i);
				if (!ft.equals(st)) {
					if (result == null) {
						result = "";
					}
					result = result + ft + " and " + st + "\nare not equal\n";
				}
			}
		}

		return result;
	}

	/** Used in splicing. */
	public void remove(int i, int j) {
		removeRange(i, j);
	}

	/** Tests behavior. */
	public static void main(String argv[]) {
		Thread t = new Thread();
		t.add("Thing");
		t.add("Animal");
		System.out.println(t);
		System.out.println(Thread.extractInstance(t.toString()));

		System.out.println("\n\n -----------Testing showDifferences()");
		Thread s = new Thread();
		s.add("Thing");
		s.add("Person");
		System.out.println(showDifferences(t, s));

		System.out.println("\n\n----------Testing bundle membership code (MAF.13.Jan.04");

		Bundle b1 = new Bundle();
		Bundle b2 = new Bundle();
		b1.addType("Mark");
		b2.addType("Steph");
		Thread t1 = b1.getPrimedThread();
		Thread t2 = b2.getPrimedThread();
		System.out.println("\nBundle 1: " + b1);
		System.out.println("Thread 1: " + t1);
		System.out.println("\nBundle 2: " + b2);
		System.out.println("Thread 2: " + t2);

		b1.addType("was");
		b1.addType("here");
		b2.addType("was not here");

		System.out.println("\nSwitching thread 2 to bundle 1.");
		b1.addThread(t2);

		System.out.println("\nBundle 1: " + b1);
		System.out.println("\nBundle 2: " + b2);

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
	}

	public boolean getSaveStringForm() {
		return saveStringForm;
	}

	/**
	 * Used to suppress notification. Notification starts turned off -- when this thread (via its owner bundle) is added
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
	}

	String previousState = null;

	/**
	 * Saves the current state of the thread in string form. Also notifies the owning bundle to save its state as well.
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
		if (getOwnerBundle() != null) {
			getOwnerBundle().saveState();
		}
	}

	/**
	 * Notifies the owner bundle that the thread has been changed.
	 * 
	 * @author M.A. Finlayson
	 * @since Jan 13, 2004; JDK 1.4.2
	 */
	protected void fireNotification() {
		if (!notification) {
			return;
		}
		if (getOwnerBundle() != null) {
			if (saveStringForm) {
				getOwnerBundle().threadModified(this, previousState, this.toString());
			}
			else {
				getOwnerBundle().threadModified(this, null, null);
			}
		}
	}

	/*
	 * The methods below have been overridden so that the thread can notify it's owner bundle if it has been changed. --
	 * MAF.13.Jan.04
	 */

	public void add(int index, String element) {
		// This method does not require overriding, since it calls
		// insertElementAt(Object, int)
		// internally
		super.add(index, element);
	}

	public boolean add(String o) {
		saveState();
		boolean val = super.add(o);
		if (val) {
			fireNotification();
		}
		return val;
	}

	public boolean addAll(Collection<? extends String> c) {
		saveState();
		boolean val = super.addAll(c);
		if (val) {
			fireNotification();
		}
		return val;
	}

	public boolean addAll(int index, Collection<? extends String> c) {
		saveState();
		boolean val = super.addAll(index, c);
		if (val) {
			fireNotification();
		}
		return val;
	}

	public void addElement(String obj) {
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

	public void insertElementAt(String obj, int index) {
		saveState();
		super.insertElementAt(obj, index);
		fireNotification();
	}

	public String remove(int index) {
		saveState();
		String obj = super.remove(index);
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

	public boolean removeAll(Collection<?> c) {
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

	public boolean retainAll(Collection<?> c) {
		saveState();
		boolean val = super.retainAll(c);
		if (val) {
			fireNotification();
		}
		return val;
	}

	public String set(int index, String element) {
		saveState();
		String obj = super.set(index, element);
		if (obj != null) {
			fireNotification();
		}
		return obj;
	}

	public void setElementAt(String obj, int index) {
		saveState();
		super.setElementAt(obj, index);
		fireNotification();
	}

	// /* (non-Javadoc)
	// * @see java.util.Vector#clone()
	// */
	// public synchronized Thread clone() {
	// return new Thread(this);
	// }

	// Debugging section
	public static final String LOGGER_GROUP = "things";

	public static final String LOGGER_INSTANCE = "Thread";

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

	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Thread)) {
			return false;
		}
		Thread t = (Thread) object;
		if (this.size() != t.size()) {
			return false;
		}
		for (int i = 0; i < t.size(); ++i) {
			if (!this.get(i).equalsIgnoreCase(t.get(i))) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		// Order matters, case does not
		int hash = -13; 
		for (int i = 0; i < this.size(); ++i) {
			// May want to hash the int itself, instead of doing i+23
			hash *= (i + 23) * this.get(i).toLowerCase().hashCode();
		}
		return hash;
	}

	public static Vector<String> getClassPairsFromString(String s) {
		Vector<String> result = new Vector<String>();

		s = Tags.untagString("bundle", s);
		String t;
		int i = 1;
		int begin, middle, end;
		String upper, lower;
		// Get each thread
		while ((t = Tags.untagString("thread", s, i)) != null) {
			// Iterate on each thread
			begin = 0;
			t = t.trim();
			while (true) {
				if (t.indexOf("features") >= 0) {
					break;
				}
				if (t.indexOf("tracers") >= 0) {
					break;
				}
				middle = t.indexOf(' ', begin);
				if (middle >= 0) {
					end = t.indexOf(' ', middle + 1);
					if (end >= 0) {
						upper = t.substring(begin, middle).trim();
						lower = t.substring(middle + 1, end).trim();
					}
					else {
						end = t.length();
						upper = t.substring(begin, middle).trim();
						lower = t.substring(middle + 1, end).trim();
					}
				}
				else {
					middle = t.length();
					end = t.length();
					upper = "";
					lower = null;
				}

				// If the class is not empty, add it to the vector.
				if (upper != "") {
					result.add(ClassPair.makeClassPair(upper, lower));
				}
				// If we've reached the end of the string, break;
				if (end == t.length() && middle == t.length()) {
					break;
				}
				begin = middle + 1;
			}
			i++;
		}

		return result;
	}

	public int indexOf(String s) {
		if (s == null) {
			return -1;
		}
		for (int i = 0; i < this.size(); ++i) {
			String x = this.get(i);
			if (s.equals(x)) {
				return i;
			}
		}
		return -1;
	}

	public boolean startsWith(Thread littleThread) {
		if (littleThread.size() > this.size()) {
			return false;
		}
		for (int i = 0; i < littleThread.size(); ++i) {
			if (!get(i).equals(littleThread.get(i))) {
				return false;
			}
		}
		return true;
	}
}
