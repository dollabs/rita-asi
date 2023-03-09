package utils.minilisp;

import java.util.*;

import utils.Mark;

/*
 * Created on Apr 2, 2010
 * @author phw
 */

public class LList<A> extends Cons implements Iterable<A>, Iterator<A> {

	public LList() {
		super();
	}

	public LList(A x) {
		super(x, new LList());
	}

	public LList(A x, Object y) {
		super(x, new LList(y));
	}

	public LList(A x, LList l) {
		super(x, l);
	}

	public A first() {
		return (A) car();
	}

	public boolean endP() {
		if (eol == true) {
			return true;
		}
		return false;
	}

	public LList<A> rest() {
		if (endP()) {
			return this;
		}
		return (LList) cdr();
	}

	public LList<A> cons(A x, LList l) {
		return new LList(x, l);
	}

	public LList<A> cons(A x) {
		return new LList<A>(x, this);
	}

	public LList<A> append(LList<A> a) {
		return append(a, this);
	}

	public LList<A> append(LList<A> a, LList b) {
		LList result = b;
		for (Object o : a) {
			A x = (A) o;
			result = cons(x, result);
		}
		return result;
	}
	
	public LList<A> copy() {
	    LList<A> copy = new LList<>();
	    ArrayList<A> reversedElements = toList();
	    Collections.reverse(reversedElements);
	    for (A element : reversedElements) {
	        copy = copy.cons(element);
	    }
	    return copy;
	}

	public ArrayList<A> toList() {
		ArrayList<A> result = new ArrayList<>();
		for (A x : this) {
			result.add(x);
		}
		return result;
	}

	// Not checked out
	// public boolean equals(LList<A> a) {
	// if (endP() && a.endP()) {
	// return true;
	// }
	// if (endP() || a.endP()) {
	// return false;
	// }
	// if (first().equals(a.first())) {
	// return rest().equals(a.rest());
	// }
	// return false;
	// }
	//
	// public boolean member(Object x) {
	// boolean result = false;
	// if(this.endP()) {
	// }
	// else if (first().equals(x)) {
	// result = true;
	// }
	// else {
	// result = (rest().member(x));
	// }
	// return result;
	// }

	public int size() {
		if (endP()) {
			return 0;
		}
		return 1 + rest().size();
	}

	public static void main(String[] ignore) {
		LList<Integer> l = new LList<Integer>(new Integer(0), new Integer(10));
		Mark.say("Result", l);
		Mark.say("Result", l.first());
		Mark.say("Result", l.rest());
		LList<Integer> m = l.cons(new Integer(15));
		Mark.say("Result", m);
		Mark.say("Result", m.rest());
		Mark.say("Result", m.rest().rest());
		Mark.say("Result", m.rest().rest().rest());
		Mark.say("Empty", new LList());
		// while (true) {
		// if (m.endP()) {
		// break;
		// }
		// Mark.say("Next:", m);
		// m = m.rest();
		// }
		for (Object o : m) {
			Mark.say("Next element:", o);
		}
		for (Object o : m) {
			Mark.say("Next element again:", o);
		}
		Mark.say("Size", m.size());
		// Mark.say(true, m.member(11));
		Mark.say("Copy:", m.copy());
	}

	private LList<A> pointer;

	@Override
	public Iterator<A> iterator() {
		pointer = this;
		return this;
	}

	@Override
	public boolean hasNext() {
		if (pointer.endP()) {
			return false;
		}
		return true;
	}

	@Override
	public A next() {
		A result = pointer.first();
		pointer = pointer.rest();
		return result;
	}

	@Override
	public void remove() {
		rest();
	}

}
