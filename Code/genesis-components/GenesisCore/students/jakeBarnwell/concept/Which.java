package jakeBarnwell.concept;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Refers to some particular collection of things that can be
 * specified, by rule, by the user. Uses either a whitelist or blacklist
 * paradigm, depending on which methods are invoked by the user.
 * 
 * @author jb16
 *
 */
public class Which {
	
	private enum Paradigm {
		BLACKLIST,
		WHITELIST;
	}
	
	public static final Which ALL = new Which(Paradigm.BLACKLIST);
	public static final Which NONE = new Which(Paradigm.WHITELIST);
	
	private Set<Object> exceptions = new HashSet<>();
	
	private Paradigm paradigm;
	
	private Which(Paradigm p) {
		this.paradigm = p;
	}
	
	private Which(Paradigm p, Set<Object> exceptions) {
		this.paradigm = p;
		this.exceptions = exceptions;
	}
	
	public Which except(Object... exceptions) {
		if(!(this == ALL || this == NONE)) {
			throw new RuntimeException("Invalid constant.");
		}
		
		Which copy = this.copy();
		for(Object exception : exceptions) {
			copy.exceptions.add(exception);
		}
		return copy;
	}
	
	public static Which only(Object... allowed) {
		return NONE.except(allowed);
	}
	
	private Which copy() {
		return new Which(paradigm, exceptions);
	}
	
	public boolean permits(Object o) {
		if(paradigm == Paradigm.WHITELIST && exceptions.contains(o)) {
			return true;
		}
		
		if(paradigm == Paradigm.BLACKLIST && !exceptions.contains(o)) {
			return true;
		}
		
		return false;
	}
	
	public boolean rejects(Object o) {
		return !permits(o);
	}
	
	@Override
	public int hashCode() {
		return 283 * paradigm.hashCode() + 293 * exceptions.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Which)) {
			return false;
		}
		
		Which oth = (Which)o;
		return paradigm == oth.paradigm && exceptions.equals(oth.exceptions);
	}
	
	@Override
	public String toString() {
		String open = "[", close = "]";
		if(exceptions.size() == 0) {
			return open + (paradigm == Paradigm.WHITELIST ? "NONE" : "ALL") + close;
		}
		
		StringBuilder exceptsSb = new StringBuilder();
		for(Object except : exceptions) {
			exceptsSb.append(except.toString() + ", ");
		}
		exceptsSb.delete(exceptsSb.length() - 2, exceptsSb.length());
		String exceptsStr = exceptsSb.toString();
		
		return open + (paradigm == Paradigm.WHITELIST ? "only: " : "all-except: ") + exceptsStr + close;
	}
}
