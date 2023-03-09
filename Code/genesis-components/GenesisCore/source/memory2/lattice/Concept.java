package memory2.lattice;

import java.util.Set;

public interface Concept<T> {

    public boolean contains(T node);

    public void learnPositive(T positive);

    public void learnNegative(T negative);
    
    public Set<T> maximalElements();
}
