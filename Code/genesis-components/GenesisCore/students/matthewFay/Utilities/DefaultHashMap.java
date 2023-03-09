package matthewFay.Utilities;

import java.util.HashMap;

public class DefaultHashMap<K, V> extends HashMap<K, V> {
	protected V defaultValue;
	  public DefaultHashMap(V defaultValue) {
	    this.defaultValue = defaultValue;
	  }
	  
	  @Override
	  public V get(Object k) {
	    V v = super.get(k);
	    return ((v == null) && !this.containsKey(k)) ? this.defaultValue : v;
	  }
}
