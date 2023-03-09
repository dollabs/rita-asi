package kevinWhite;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

import constants.Markers;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Thread;

/**
 * 
 * @author minhtuev
 * The concept manager (corresponding to the Controller in VMC) updates the concept model.
 */
public class ConceptManager {
	
	private HashMap<String,FasterLLConcept> conceptMap = new HashMap<String,FasterLLConcept>();
	
	public ConceptManager(){
		
	}
	
	/**
	 * 
	 * @param name, the name of the concept and the lattice to be associated with it
	 */
	private void addConcept(String name){
		TypeLattice tl = new TypeLattice();
		FasterLLConcept flc = new FasterLLConcept(tl,name);
		this.conceptMap.put(name, flc);
	}
	
	public void addConcept(FasterLLConcept concept){
		this.conceptMap.put(concept.getName(), concept);
	}
	
	/**
	 * 
	 * @param name, the name of the concept
	 * @param th, the thread being added to the concept
	 * @param positive, is this a positive or negative contribution to the concept
	 */
	public void updateConcept(String name, Thread th, boolean positive){
		FasterLLConcept flc = this.conceptMap.get(name);
		TypeLattice tl = flc.getLattice();
		tl.updateAncestry(th);
		if (positive){
			flc.learnPositive(th.lastElement());
		}
		else{
			flc.learnNegative(th.lastElement());
		}
	}
	
	public FasterLLConcept removeConcept(String name){
		return this.conceptMap.remove(name);
	}
	
	public FasterLLConcept getConcept(String name){
		if (!this.conceptMap.containsKey(name)){
			this.addConcept(name);
		}
		return this.conceptMap.get(name);
	}
	
	public HashMap<String,FasterLLConcept> getConcepts(){
		return this.conceptMap;
	}

}
