package kevinWhite;

import java.util.Set;

public interface Concept<T> {

    public boolean contains(T node);

    public void learnPositive(T positive);

    public void learnNegative(T negative);
    
    abstract Set<T> getPositives();
    
    abstract Set<T> getNegatives();
    
    public Set<T> maximalElements();
}
