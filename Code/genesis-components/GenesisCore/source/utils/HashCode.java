package utils;

/**
 * @author bonawitz
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class HashCode {
	public static final int trueHash = Boolean.TRUE.hashCode();
	
	private HashCode() {}
	public static final int hash(Object o) {
		if (o == null) return 0; else return o.hashCode();
	}
	public static final int hash(int i) { return i;	}
	public static final int hash(byte b) { return b; }
	public static final int hash(short s) { return s; }
	public static final int hash(char c) { return c; }
	public static final int hash(long l) { return (new Long(l)).hashCode(); }
	public static final int hash(double d) {return (new Double(d)).hashCode(); }
	public static final int hash(float f) {return (new Float(f)).hashCode(); }
	public static final int hash(boolean b) {
		if (b) 
			return Boolean.TRUE.hashCode();
		else 
			return Boolean.FALSE.hashCode();
	}

	public static boolean areEqual(Object a, Object b) {
		if (a == b) { 
			return true;
		} else if ((a == null) || (b == null)) { 
			return false;
		} else {
			return a.equals(b);
		}
	}
	
	public static final int identityHash(Object o) {
		if (o == null) return 0; else return System.identityHashCode(o);
	}	
}
