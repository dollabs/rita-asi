package kevinWhite;

import java.util.HashSet;
import java.util.Set;

import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Thread;

public class SlowerLLConcept<T> implements Concept<T> {

    private Lattice<T>   lattice;
    private Set<T> positives = new HashSet<T>();
    private Set<T> negatives = new HashSet<T>();

    public SlowerLLConcept(Lattice<T> lattice) {
        this.lattice = lattice;
    }
    
    public void learnNegative(T negative) {
    	negatives.add(negative);
    }


    public void learnPositive(T positive) {
    	positives.add(positive);
    }

    public boolean contains(T node) {
        // if leq negative, false
        for (T negative : negatives) {
            if (lattice.leq(negative, node)) {
                return false;
            }
        }

        // if leq positive, true
        for (T positive : positives) {
            if (lattice.leq(positive, node)) {
                return true;
            }
        }
       
        boolean result = false;
        for (T parent : lattice.getParents(node)) {
        	result = result || contains(parent);
        }
        return result;
    }

    public Set<T> maximalElements() {
        Set<T> maxes = new HashSet<T>();
        for (T positive : positives) {
        	for (T node : lattice.getAncestors(positive)) {
        		if (contains(node)) {
        			maxes.add(node);
        			break;
        		}
        	}
        }

        Set<T> toRemove = new HashSet<T>();
        for (T a : maxes) {
            for (T b : maxes) {
                if (!a.equals(b) && lattice.leq(a,b)) {
                    toRemove.add(a);
                }
            }
        }
        maxes.removeAll(toRemove);
        return maxes;
    }
    
    protected boolean infer(String word) throws Exception{
    	Bundle wBundle = BundleGenerator.getBundle(word);
    	Thread wThread = wBundle.getPrimedThread();
    	for (int i = wThread.size() - 1; i >= 0; i--){
    		if (positives.contains(wThread.elementAt(i))){
    			return true;
    		}
    		
    		else if (negatives.contains(wThread.elementAt(i))){
    			return false;
    		}
    		System.out.println(wThread.elementAt(i));
    	}
    	return false;
    }

    public String toString() {
    	return maximalElements().toString();
    }

    public Set<T> getPositives() {
        return positives;
    }

    public Set<T> getNegatives() {
        return negatives;
    }
}
