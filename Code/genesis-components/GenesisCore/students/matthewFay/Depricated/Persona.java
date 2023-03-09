package matthewFay.Depricated;

import java.io.Serializable;
import java.util.ArrayList;

import frames.entities.Relation;
import frames.entities.Sequence;

@Deprecated
public class Persona implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3L;
	
	private ArrayList<Relation> rules = null;
	private ArrayList<Sequence> reflections = null;
	private int version = 0;
	
	private String name = "default";
	public void setName(String newName) {
		name = newName;
	}
	public String getName() {
		return name;
	}
	
	public Persona(Sequence rules, Sequence reflections) {
		this.rules = new ArrayList<Relation>();
		this.reflections = new ArrayList<Sequence>();
		addRules(rules);
		addReflections(reflections);
	}
	
	public Persona() {
		rules = new ArrayList<Relation>();
		reflections = new ArrayList<Sequence>();
	}

	public String toString() {
		String s = rules.toString() + "\n" + reflections.toString();
		return s;
	}
	
	public Sequence getRules() {
		Sequence gotRules = new Sequence();
		gotRules.addType(name);
		for(Relation r : rules) {
			gotRules.addElement(r.cloneForResolver());
		}
		return gotRules;
	}
	
	public void addRule(Relation rule) {
		rules.add((Relation) rule.cloneForResolver());
	}
	
	public void addRules(Sequence rules) {
		for(int i=0;i<rules.getNumberOfChildren();i++) {
			addRule((Relation) rules.getElement(i));
		}
	}
	
	public Sequence getConcepts() {
		Sequence gotReflections = new Sequence();
		gotReflections.addType(name);
		for(Sequence s : reflections) {
			gotReflections.addElement(s.cloneForResolver());
		}
		return gotReflections;
	}
	
	public void addConcept(Sequence reflection) {
		reflections.add((Sequence) reflection.cloneForResolver());
	}
	
	public void addReflections(Sequence reflections) {
		for(int i=0;i<reflections.getNumberOfChildren();i++) {
			addConcept((Sequence) reflections.getElement(i));
		}
		version++;
	}

	public int getVersion() {
		return version;
	}
	
	public void markVersion() {
		version++;
	}
}
