package jakeBarnwell.concept;

import java.util.Collection;
import java.util.function.Function;

/**
 * Semantically, a group of elements that should hold a similar or 
 * same property. Each element must be unique as defined by some
 * custom function supplied by the user (or, defaults to the
 * <code>hashCode</code> function). A {@link Group} is backed by
 * a {@link AssocSet}.
 * @author jb16
 *
 */
public class Group<E> extends AssocSet<E> {
	
	public Group() {
		super(E::hashCode);
	}
	
	public Group(Function<? super E, Integer> customHashFn) {
		super(customHashFn);
	}
	
	public Group(Collection<E> things) {
		this();
		for(E thing : things) {
			this.add(thing);
		}
	}
	
	public Group(Collection<E> things, Function<E, Integer> customHashFn) {
		this(customHashFn);
		for(E thing : things) {
			this.add(thing);
		}
	}
	
	/**
	 * Gets a particular item from this group. There is no guarantee
	 * on the order of items.
	 * @param i
	 * @return
	 */
	public E get(int i) {
		return map.keyList().get(i);
	}
	
}