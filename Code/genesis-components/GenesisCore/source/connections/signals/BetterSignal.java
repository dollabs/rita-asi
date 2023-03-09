package connections.signals;

import java.io.Serializable;
import java.util.*;

import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import genesis.Genesis;
import utils.Mark;

/**
 * Wrapper of List, which can be used to transmit multiple args in the wire pattern. provides convenience getters to
 * save a lot of instanceof checks.
 * 
 * @author adk
 */
@SuppressWarnings("serial")
public class BetterSignal implements Serializable, Iterable<Object> { // as long as everything you put in it is
                                                                      // serializable...

	public static Integer YES = 1, NO = 2, NO_OPINION = 3, NO_ANSWER = 4;

	// Exception classes:
	public static class NoSuchElementException extends RuntimeException {
		public NoSuchElementException(int idx) {
			super("Signal has no element at position " + idx);
		}
	}

	public static class WrongTypeException extends RuntimeException {
		public WrongTypeException(int idx, Class<?> asked, Class<?> actual) {
			super("Element at position " + idx + " is not a " + asked + ", it is a " + actual);
		}
	}

	private List<Object> l = new ArrayList<Object>();

	public BetterSignal() {
	}

	public BetterSignal(Object... args) {
		for (Object wrapped : args) {
			l.add(wrapped);
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T get(int n, Class<T> type) {
		if (n < l.size()) {
			if (type.isInstance(l.get(n))) {
				return (T) (l.get(n));
			}
			else {
				throw new WrongTypeException(n, type, l.get(n).getClass());
			}
		}
		else {
			throw new NoSuchElementException(n);
		}
	}

	public synchronized void add(Object o) {
		l.add(o);
	}

	public synchronized void replace(int n, Object o) {
		l.set(n, o);
	}

	public synchronized void put(int n, Object o) {
		while (l.size() <= n) {
			l.add(null);
		}
		l.set(n, o);
	}

	public synchronized <T> boolean elementIsType(int n, Class<T> type) {
		if (n < l.size()) {
			if (l.get(n) != null) {
				// return l.get(n).getClass()==type; //for strictly same class eg. ArrayList.class==List.class returns
				// false
				return type.isInstance(l.get(n));// returns true iff the Nth elt is assignable to a variable declared as
				                                 // type
			}
			else {
				return false;
			}
		}
		else {
			throw new NoSuchElementException(n);
		}
	}

	public synchronized int size() {
		return l.size();
	}

	public static BetterSignal isSignal(Object object) {
		if (object instanceof BetterSignal) {
			return (BetterSignal) object;
		}
		return null;
	}

	public static void main(String args[]) {
		BetterSignal s = new BetterSignal(new Entity(), new Function(new Entity()), new Sequence(), new ArrayList<Integer>()); // note
		Entity first = s.get(0, Entity.class);
		Function second = s.get(1, Function.class);
		Sequence third = s.get(2, Sequence.class);
		Collection<?> fourth = s.get(3, Collection.class);

		System.out.println("Got: " + first + ", " + second + ", " + third + ", " + fourth);
		System.out.println("elementIsType(3,Collection.class): " + s.elementIsType(3, Collection.class));
		System.out.println("elementIsType(3,ArrayList.class): " + s.elementIsType(3, ArrayList.class));
		s.add(null);
		System.out.println("null behaves correctly: " + !s.elementIsType(4, Genesis.class));

		Mark.say("Opinion result", new BetterSignal(YES).isNoOpinion());

	}

	@Override
	public Iterator<Object> iterator() {
		return l.iterator();
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> List<T> getAll(Class<T> type) {
		ArrayList<T> tempList = new ArrayList<T>();
		for (Object o : l) {
			if (type.isInstance(o)) tempList.add((T) o);
		}
		return tempList;
	}

	public boolean isTrue(String location) {
		if (l.size() > 0 && l.get(0) instanceof Boolean) {
			return (boolean) (l.get(0));
		}
		Mark.err("First argument of BetterSignal is not Boolean:", location, l.get(0).getClass());
		return false;
	}

	public boolean isTrue() {
		if (l.size() > 0 && l.get(0) instanceof Boolean) {
			return (boolean) (l.get(0));
		}
		Mark.err("First argument of BetterSignal is not Boolean:", l.get(0).getClass());
		return false;
	}

	public boolean isFalse() {
		return !isTrue();
	}

	public boolean isYes() {
		if (l.size() > 1 && l.get(1) instanceof Integer) {
			return l.get(1) == YES;
		}
		Mark.err("First argument of BetterSignal is not opinion:", l.get(1).getClass());
		return false;
	}

	public boolean isNo() {
		if (l.size() > 1 && l.get(1) instanceof Integer) {
			return l.get(1) == NO;
		}
		Mark.err("First argument of BetterSignal is not opinion:", l.get(1).getClass());
		return false;
	}

	public boolean isNoOpinion() {
		if (l.size() > 1 && l.get(1) instanceof Integer) {
			return l.get(1) == NO_OPINION;
		}
		Mark.err("First argument of BetterSignal is not opinion:", l.get(1).getClass());
		return false;
	}

	public boolean isNoAnswer() {
		if (l.size() > 1 && l.get(1) instanceof Integer) {
			return l.get(1) == NO_ANSWER;
		}
		Mark.err("First argument of BetterSignal is not opinion:", l.get(1).getClass());
		return false;
	}

}
