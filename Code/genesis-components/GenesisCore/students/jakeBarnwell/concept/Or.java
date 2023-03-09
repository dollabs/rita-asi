package jakeBarnwell.concept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Or<T> implements Iterable<T> {
	
	private ArrayList<T> options = new ArrayList<>();
	
	@SafeVarargs
	public Or(T... args) {
		for(T arg : args) {
			options.add(arg);
		}
	}
	
	public T peek() {
		assert options.size() == 1;
		return get(0);
	}
	
	public T get(int i) {
		assert options.size() > 0;
		return options.get(i);
	}
	
	public boolean update(int i, T t) {
		options.set(i, t);
		return true;
	}
	
	public boolean also(T other) {
		if(options.contains(other)) {
			return false;
		}
		
		options.add(other);
		return true;
	}
	
	public boolean remove(T t) {
		return options.remove(t);
	}
	
	public int count() {
		return options.size();
	}
	
	@Override
	public int hashCode() {
		int hash = 53;
		for(T t : options) {
			hash *= t.hashCode();
		}
		return hash;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other == null || !(other instanceof Or)) {
			return false;
		}
		
		Or<T> o;
		try {
			o = (Or<T>)other;
		} catch(Exception e) {
			return false;
		}
		
		if(count() != o.count()) {
			return false;
		}
		
		return this.hashCode() == o.hashCode();		
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("OR(");
		for(T o : options) {
			sb.append(o.toString() + "|");
		}
		sb.delete(sb.length() - 1, sb.length());
		sb.append(")");
		return sb.toString();
	}

	@Override
	public Iterator<T> iterator() {
		return options.iterator();
	}

}
