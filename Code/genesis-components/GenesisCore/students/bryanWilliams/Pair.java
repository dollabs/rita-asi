package bryanWilliams;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Pair<T> implements Iterable<T>, Serializable {
    
    private static final long serialVersionUID = 1L;
    private final T obj1;
    private final T obj2;
    
    public Pair(T obj1, T obj2) {
        this.obj1 = obj1;
        this.obj2 = obj2;
    }
    
    public List<T> getObjs() {
        return Collections.unmodifiableList(Arrays.asList(obj1, obj2));
    }
    
    public T other(T obj) {
        if (obj1.equals(obj)) {
            return obj2;
        }
        if (obj2.equals(obj)) {
            return obj1;
        }
        throw new IllegalArgumentException("Given obj must be one of the elements of this pair");
    }

    @Override
    public Iterator<T> iterator() {
        return getObjs().iterator();
    }

    @Override
    public int hashCode() {
        return 31*obj1.hashCode()*obj2.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Pair)) {
            return false;
        }
        Pair<?> that = (Pair<?>) other;
        return (this.obj1.equals(that.obj1) && this.obj2.equals(that.obj2)) ||
               (this.obj2.equals(that.obj1) && this.obj1.equals(that.obj2));
    }
}
