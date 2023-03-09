package jakeBarnwell.concept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * An association map.
 * <br>
 * Like a {@link HashMap}, but instead of using the pre-defined hash
 * function for an object, keys entries on a custom ("faux-hash")
 * association function defined by the user.
 * @author jb16
 *
 * @param <K> type of key
 * @param <V> type of value
 */
public class AssocMap<K, V> {
	
	private final HashMap<Integer, V> valMap = new HashMap<>();
	private final List<K> keyList = new ArrayList<K>();
	private final Function<? super K, Integer> hashFn;
	
	public AssocMap(Function<? super K, Integer> customHashFn) {
		hashFn = customHashFn;
	}
	
	/**
	 * Uses the custom hash function of this map to turn the 
	 * given key into an integer, which is itself used as  
	 * the true key into the map. If the input object is null,
	 * or is not of type {@code <K>}, {@link null} is instead returned.
	 * @param key
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	private Integer hash(Object key) {
		if(key == null) {
			return null;
		}
		
		try {
			return hashFn.apply((K)key);
		} catch(RuntimeException e) {
			return null;
		}
	}
	
	public V get(Object key) {
		return valMap.get(hash(key));
	}
	
	/**
	 * Puts the <key,value> pair in the map. If there was already a 
	 * value associated with the key, returns the old value. Otherwise,
	 * returns null.
	 * <br>
	 * Null key or value not allowed.
	 */
	public V put(K key, V val) {
		if(key == null) {
			throw new RuntimeException("Null keys are not allowed!");
		}
		if(val == null) {
			throw new RuntimeException("Null values are not allowed!");
		}
		
		int h = hash(key);
		V previousVal = valMap.put(h, val);
		
		// Only add the key to the keyList if there was nothing in the map before
		if(previousVal == null) {
			keyList.add(key);
		}
		
		return previousVal;
	}
	
	/**
	 * Removes the key and the associated object from the map, if 
	 * they exist. 
	 * 
	 * @param key
	 * @return The value removed, if the key existed in the map. Otherwise, null.
	 */
	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		int h = hash(key);
		
		V previousVal = valMap.remove(h);
		
		// Only remove the key from keyList if there was something in the map
		if(previousVal != null) {
			K realKey = (K)key;
			// Can't rely on keyList.remove because that uses strict .equals
			for(int k = 0; k < keyList.size(); k++) {
				if(hashFn.apply(keyList.get(k)) == hashFn.apply(realKey)) {
					keyList.remove(k);
					break;
				}
			}
		}
		
		return previousVal;
	}
	
	/**
	 * Clears all entries from the map.
	 */
	public void clear() {
		valMap.clear();
		keyList.clear();
	}
	
	public int size() {
		return valMap.size();
	}
	
	public boolean isEmpty() {
		return size() == 0;
	}
	
	public boolean containsValue(Object val) {
		return valMap.containsValue(val);
	}
	
	public List<K> keyList() {
		return keyList;
	}
	
	@SuppressWarnings("unchecked")
	public boolean containsKey(Object key) {
		// Can't use keyList.contains because it uses .equals
		
		K realKey;
		try {
			realKey = (K)key;
		} catch(Exception e) {
			return false;
		}
		
		for(int k = 0; k < keyList.size(); k++) {
			if(hashFn.apply(keyList.get(k)) == hashFn.apply(realKey)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return 3 * valMap.hashCode();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object oth) {
		if(oth == null || !(oth instanceof AssocMap)) {
			return false;
		}
		
		AssocMap<K,V> o;
		try {
			o = (AssocMap<K,V>)oth;
		} catch(Exception e) {
			return false;
		}
		
		return valMap.equals(o.valMap);
	}
	
	@Override
	public String toString() {
		if(valMap.isEmpty()) {
			return "{}";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for(K key : keyList) {
			sb.append(String.format("%s=%s, ", key, valMap.get(hash(key))));
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append("}");
		return sb.toString();
	}

}
