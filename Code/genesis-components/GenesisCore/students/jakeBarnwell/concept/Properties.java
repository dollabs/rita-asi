package jakeBarnwell.concept;

import java.util.HashMap;
import java.util.Set;

public class Properties {
	
	private HashMap<String, Object> properties = new HashMap<>();
	
	private Properties() {
		;
	}
	
	/**
	 * Readies this object to assign properties by wiping out 
	 * all existing properties, then returns itself;
	 * @return itself
	 */
	public Properties ready() {
		clear();
		return this;
	}
	
	public void clear() {
		properties.clear();
	}
	
	public void set(String key, Object val) {
		properties.put(key, val);
	}
	
	public Object get(String key) {
		return properties.get(key);
	}
	
	public void remove(String key) {
		properties.remove(key);
	}
	
	public Set<String> keys() {
		return properties.keySet();
	}
	
	public Properties copy() {
		Properties copy = Properties.create();
		for(String key : this.properties.keySet()) {
			copy.set(key, this.get(key));
		}
		
		return copy;
	}
	
	public static Properties create(Object... args) {
		Properties p = new Properties();
		for(int i = 0; i < args.length; i += 2) {
			String key = (String)args[i];
			Object val = args[i + 1];
			p.set(key, val);
		}
		
		return p;
	}
	
	@Override
	public String toString() {
		return properties.toString();
	}
}
