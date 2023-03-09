package jakeBarnwell.concept;

public class Pair<T, U> {
	
	public T left;
	public U right;
	
	public Pair(T t, U u) {
		left = t;
		right = u;
	}
	
	public static Pair<Object, Object> of(Object a, Object b) {
		return new Pair<Object, Object>(a, b);
	}

}
