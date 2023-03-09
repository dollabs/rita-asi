package utils.minilisp;

import utils.Mark;

/*
 * Created on Dec 2, 2009
 * @author phw
 */

public class L {

	public static Cons list(Object a) {
		return cons(a, null);
	}

	public static Cons list(Object a, Object b) {
		return cons(a, list(b));
	}

	public static Object first(Cons a) {
		return a.first();
	}

	public static Object second(Cons a) {
		return a.second();
	}

	public static Object car(Cons a) {
		return a.car();
	}

	public static Object cdr(Cons a) {
		return a.cdr();
	}

	public static Cons cons(Object a, Cons b) {
		return new Cons(a, b);
	}

	public static Object rest(Cons cell) {
		if (cell == null) {
			return null;
		}
		Object cdr = cdr(cell);
		if (cdr == null) {
			return null;
		}
		else {
			return cdr;
		}
	}

	public static Cons cons(Object a, Object b) {
		return new Cons(a, b);
	}

	public static Cons append(Cons a, Cons b) {
		// Finally, a recursion!
		if (a == null) {
			return b;
		}
		else if (cdr(a) == null) {
			return cons(first(a), b);
		}
		else if (cdr(a) instanceof Cons) {
			return cons(first(a), append((Cons) cdr(a), b));
		}
		Mark.say("Error in appending two lists", a, b);
		return null;
	}

	public static void main(String[] ignore) {
		Cons c = list("patrick", "winston");
		Cons d = list("karen", "prendergast");
		Mark.say(true, c);
		Mark.say(true, d);
		Mark.say(true, append(c, d));
		Mark.say(true, rest(append(c, d)));
		Mark.say(true, second(append(c, d)));
		Mark.say(true, cons("karen", "prendergast"));
		Mark.say(true, append(c, cons("karen", (list("prendergast")))));
		Cons x = append(c, d);
		Cons y = cons("Hello", rest(x));
		Cons z = cons("World", rest(x));
		Mark.say(true, x);
		Mark.say(true, y);
		Mark.say(true, z);
	}

}
