package utils.minilisp;

import utils.Mark;

/*
 * Created on Dec 2, 2009
 * @author phw
 */

public class Cons<A, D> {

	private A car;

	private D cdr;

	public boolean eol;

	public Cons() {
		eol = true;
	}

	public Cons(A first, D second) {
		super();
		this.car = first;
		this.cdr = second;
	}

	public A car() {
		return car;
	}

	public D cdr() {
		return cdr;
	}

	public A first() {
		return car();
	}

	public A second() {
		D cdr = cdr();
		if (cdr instanceof Cons) {
			return ((Cons<A, D>) cdr).first();
		}
		return null;
	}

	public boolean listP() {
		if (this instanceof LList) {
			return true;
		}
		return false;
	}

	public String toString() {
		if (listP()) {
			return printList((LList) this);
		}
		return "(" + car + " . " + cdr + ")";
	}

	private String printList(LList x) {
		// return "(" + car + " " + printListAux(cdr) + ")";
		return "(" + printListAux(x) + ")";
	}

	private String printListAux(Object o) {
		if (o == null) {
			return "";
		}
		if (o instanceof LList) {
			LList x = (LList) o;
			if (x.endP()) {
				return "";
			}
			else if (x.cdr() instanceof LList && ((LList)(x.cdr())).endP()) {
				return x.car().toString();
			}
			else if (x instanceof LList) {
				return x.car() + " " + printListAux(x.cdr());
			}
		}
		return o.toString();
	}

	public static void main(String[] ignore) {
		LList.main(ignore);
	}

}
