package jakeBarnwell.concept;

import frames.entities.Entity;

/**
 * Represents something new learned. Conceptually, the combination of an
 * example and its corresponding sign (i.e. positive or negative).
 * 
 * Ideally this would be immutable...
 * 
 * @author jb16
 *
 */
public class Example {
	
	private final Entity pattern;
	private final EntityTree tree;
	private final Charge charge;
	
	public Example(final Entity pattern, final Charge sign) {
		this.pattern = pattern;
		this.tree = new EntityTree(pattern, null); // TODO
		this.charge = sign;
	}
	
	public Entity pattern() {
		return pattern;
	}
	
	public Charge charge() {
		return charge;
	}
	
	public EntityTree tree() {
		return tree;
	}
	
	@Override
	public String toString() {
		String symbol = charge == Charge.POSITIVE ? Symbol.PLUS : Symbol.MINUS;
		return "(" + symbol + ") \"" + pattern.toString() + "\"";
	}
}
