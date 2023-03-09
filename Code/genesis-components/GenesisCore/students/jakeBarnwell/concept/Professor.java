package jakeBarnwell.concept;

import java.util.HashMap;

import frames.entities.Entity;
import jakeBarnwell.concept.Example;
import translator.Translator;
import utils.Mark;

public class Professor {
	
	private HashMap<String, ConceptModel> concepts = new HashMap<>();
	
	private Translator t;
	
	/**
	 * The name of the most recent concept that the client was using. This is largely
	 * used as a convenience field, allowing some operations to be called without
	 * having to keep specifying the name of the concept in question.
	 */
	private String activeConceptName;
	
	/**
	 * The model for the most recent concept that the client was using. Corresponds
	 * to {@link #activeConceptName}.
	 */	
	private ConceptModel activeModel;
	
	/**
	 * Creates a new instance. If a client wants to create a new instance, 
	 * try using the static builder {@link #hire()} instead.
	 */
	private Professor() {
		t = Translator.getTranslator();
	}
	
	/** 
	 * Hires a new professor to learn and teach concepts.
	 * @return
	 */
	public static Professor hire() {
		return new Professor();
	}
	
	/**
	 * Fires the professor, making him forget every concept he's ever learned or taught.
	 */
	public void fire() {
		concepts.clear();
		activeConceptName = null;
		activeModel = null;
	}
	
	/**
	 * Introduces a new concept by name to the system. This is the only way 
	 * to introduce a new concept to the system. This method must be called
	 * to instantiate the model for a particular concept, prior to ever referencing
	 * a concept by that name.
	 * @param conceptName The name of the concept, like "retaliation" or "harm"
	 */
	public void newConcept(String conceptName) {
		if(concepts.containsKey(conceptName)) {
			throw new RuntimeException("A concept by the name " + conceptName + " is already registered!");
		}
		concepts.put(conceptName, new ConceptModel());
		setActive(conceptName);
	}
	
	/**
	 * Updates the active concept and/or concept name as applicable.
	 * @param cn
	 */
	private void setActive(String cn) {
		assert cn != null;
		if(activeConceptName != null && activeConceptName.equals(cn)) {
			return;
		}
		activeConceptName = cn;
		activeModel = concepts.get(cn);
	}
	
	/**
	 * Declares to the system that these names should be considered as persons.
	 * @param persons
	 */
	public void isPerson(String... persons) {
		for(String person : persons) {
			t.internalize(String.format("%s is a person.", person));
			Mark.say(String.format("%s is a person.", person));
		}
	}
	
	/**
	 * Updates the internal model of the most recent concept based on a new example
	 * @param s
	 * @param sign
	 */
	public void learn(String s, Charge sign) {
		learn(s, sign, activeConceptName);
	}
	
	/**
	 * Updates the internal model of a concept based on a new example
	 * @param s
	 * @param sign
	 * @param cn
	 */
	public void learn(String s, Charge sign, String cn) {
		setActive(cn);
		Example ex = new Example(t.internalize(s), sign);
		concepts.get(cn).update(ex);
	}
	
	/**
	 * Asks if this is an instance of the most recent concept
	 * @param s
	 * @return
	 */
	public boolean ask(String s) {
		return ask(s, activeConceptName);
	}
	
	/**
	 * Asks if this is an instance of the given concept.
	 * @param s The potential sentence in question
	 * @param cn The concept to check
	 * @return
	 */
	public boolean ask(String s, String cn) {
		setActive(cn);
		Entity ent = t.internalize(s);
		Mark.say("Asking about", cn + ":", ent);
		return concepts.get(cn).query(ent);
	}
	
	public static void main(String[] args) {
		Professor prof = new Professor();
		prof.isPerson("John", "Mary", "Bob", "Sue");
		prof.newConcept("testing");
		
		
	}

}
