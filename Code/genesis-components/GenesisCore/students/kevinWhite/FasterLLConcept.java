package kevinWhite;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import translator.BasicTranslator;
import utils.Mark;
import cern.colt.Arrays;
import dictionary.BundleGenerator;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Thread;

/**
 * 
 * @author Michael Klein
 * @author Kevin White
 * @author Vo Thanh Minh Tue
 * 
 * This class sits on top of lattice (raw data).
 * 
 * In this case, the concept is a verb ("throw", "swim", "grab", ...) and uses
 * the concept on incoming data to make inferences about the data.
 * 
 * Input: any lattice
 *
 */
public class FasterLLConcept implements Concept<String> {

    private TypeLattice lattice;
    
    private Set<String> positiveAncestors = new HashSet<String>();
    private Set<String> negativeAncestors = new HashSet<String>();
    private String conceptName;
    public boolean empty = true;

    public FasterLLConcept(TypeLattice lattice) {
        this.lattice = lattice;
    }
    
    /**
     * @param lattice: raw lattice data
     * @param name: the associated name of the concept; most likely are verbs ("throw", "swim", ...). 
     */
    public FasterLLConcept(TypeLattice lattice, String name){
    	this.lattice = lattice;
    	this.conceptName = name;
    }
    
    /**
     * responsible for providing negative samples into the lattice
     */
    public void learnNegative(String negative) {
    	negativeAncestors.addAll(lattice.getAncestors(negative));
    	this.empty = false;
    }

    /**
     * responsible for providing positive samples into the lattice
     */
    public void learnPositive(String positive) {
    	positiveAncestors.addAll(lattice.getAncestors(positive));
    	this.empty = false;
    }

    public boolean contains(String node) {
    	if (negativeAncestors.contains(node)) {
    		return false;
    	}
    	if (positiveAncestors.contains(node)) {
    		return true;
    	}
    	boolean result = false;
    	for (String parent : lattice.getParents(node)) {
    		result = result || contains(parent);
    	}
    	return result;
    }
    
    
    
    public Set<String> maximalElements() {
        Set<String> maxes = new HashSet<String>();
        for (String node : positiveAncestors) {
    		if (contains(node)) {
    			maxes.add(node);
    		}
        }

        Set<String> toRemove = new HashSet<String>();
        for (String a : maxes) {
            for (String b : maxes) {
            	if (!a.equals(b) && lattice.leq(a,b)) {
                    toRemove.add(a);
                }
            }
        }
        maxes.removeAll(toRemove);
        return maxes;
    }
    
    /**
     * The inference method is used to answer questions and evaluate statements.
     * In addition, it also increases the size of the lattice by 1 semantic thread.
     * @param word, the word that is being predicted in the lattice
     * @return whether the given inference is true or false
     * @throws Exception
     */
    protected boolean infer(String word) throws Exception{
    	Bundle wBundle = BundleGenerator.getBundle(word.toString());
    	Thread wThread = wBundle.getPrimedThread();
    	this.lattice.updateAncestry(wThread);
    	for (int i = wThread.size() - 1; i >= 0; i--){
    		String ele = wThread.elementAt(i);
    		if (negativeAncestors.contains(wThread.elementAt(i))){
    			this.learnNegative(ele);
    			return false;
    		}
    		else if (positiveAncestors.contains(wThread.elementAt(i))){
    			this.learnPositive(ele);
    			return true;
    		}
    	}
    	return false;
    }
    
    
    /**
     * The inference method is used to answer questions and evaluate statements.
     * In addition, it also increases the size of the lattice by 1 semantic thread.
     * @param wThread, the topic of inference
     * @return whether the inference is true or false
     */
    protected boolean infer(Thread wThread){
    	this.lattice.updateAncestry(wThread);
    	for (int i = wThread.size() - 1; i >= 0; i--){
    		String ele = wThread.elementAt(i);
    		if (negativeAncestors.contains(wThread.elementAt(i))){
    			this.learnNegative(ele);
    			return false;
    		}
    		else if (positiveAncestors.contains(wThread.elementAt(i))){
    			this.learnPositive(ele);
    			return true;
    		}
    	}
    	return false;
    }
    
    public String interpretSimpleSentence(HashMap<String,Object> sentData){
    	return this.reduceBundle((Bundle)sentData.get("noun_bundle"),(String)sentData.get("verb"),(Boolean)sentData.get("feature"));
    }
    
    /**
     * Parse simple sentence (Subject + Verb) and return a hash map of four objects:
     *     String noune = the subject of sentence
     *     Bundle noun_bundle = Bundle of all threads related to fish
     *     String verb = Prime thread in the String, which is often a verb. Ex: "action travel swim"
     *     Thread verbThread = the actual verb thread
     *     String verbName = the actual verb name, ex: "swim"
     *     Boolean feature = whether the [noun] can perform the [verb]
     *     
     * Example: sentence = "Fish can swim."
     * 
     * @param sentence
     * @return
     * @throws Exception
     */
    public static HashMap<String, Object> parseSimpleSentence(String sentence) throws Exception{
    	HashMap map = new HashMap<String, Object>();
    	BasicTranslator basicTranslator = new BasicTranslator();
    	Entity entity = basicTranslator.translate(sentence);
    	Mark.say(entity);
    	Entity subject = entity.getElement(0).getSubject();
    	Bundle noun_bundle = subject.getBundle();
    	String verb = entity.getElement(0).getPrimedThread().toString(true);
    	ArrayList features = entity.getElement(0).getFeatures();
    	Boolean feature = !entity.getElement(0).hasFeature("not");
    	map.put("noun_bundle", noun_bundle);
    	map.put("verb", verb);
    	map.put("verbThread", entity.getElement(0).getPrimedThread());
    	map.put("verbName", entity.getElement(0).getPrimedThread().lastElement());
    	map.put("feature", feature);
    	map.put("noun", subject);
//    	Mark.say(noun_bundle);
//    	Mark.say(interpretSentence(noun_bundle,verb,feature));
    	return map;
    }
    
    /**
     * Take the bundle from the noun, and take in a verb, and return a String as an array list
     * of the meaning of the nouns
     * 
     * @param noun_bundle = bundle of all threads of this noun
     * @param verb = the action verb
     * @param feature = whether the [noun] can perform this [verb]
     * @return an array list of valid noun semantic threads
     */
	public String reduceBundle(Bundle noun_bundle, String verb, boolean feature){
		ArrayList<String> goodMeanings = new ArrayList<String>();
		Bundle finBundle = noun_bundle.copy();
		Enumeration<Thread> threads = noun_bundle.elements();
    	while (threads.hasMoreElements()){
    		Thread wThread = threads.nextElement();
    		if (feature && this.infer(wThread) || !feature && !this.infer(wThread)){
    			goodMeanings.add(wThread.toString(true));
    		} else{
    			finBundle.removeThread(wThread);
    		}
    	}
		return goodMeanings.toString();
	}
    
    public String toString() {
    	return maximalElements().toString();
    }
    
    public TypeLattice getLattice(){
    	return this.lattice;
    }

    public Set<String> getPositives() {
        return positiveAncestors;
    }

    public Set<String> getNegatives() {
        return negativeAncestors;
    }
    
    public String getName(){
    	return this.conceptName;
    }
    
    /**
     * Main function that runs the show
     * @param args
     */
    public static void main(String[] args){
    }
}
