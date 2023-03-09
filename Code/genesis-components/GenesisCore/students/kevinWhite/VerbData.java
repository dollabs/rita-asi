package kevinWhite;

import java.util.HashMap;

/**
 * 
 * This Singleton class takes a verb and matches it to a subject and 
 * store whether the subject can perform the verb.
 * 
 */
public class VerbData {
    private String verb;
    private HashMap<String,Boolean> subjectMap = new HashMap<String,Boolean>();
    private HashMap<String,String> threadMap = new HashMap<String,String>();
    public static HashMap<String,VerbData> verbs = new HashMap<String,VerbData>();

    
    /**
     * Private construction
     * Input: verb
     * @param verb
     */
    private VerbData(String verb){
        this.verb = verb;
    }
    
    /**
     * Singleton method
     * 
     * @param verb: should be the semantic thread, ex: "action travel swim"
     * @return
     */
    public static VerbData getVerbData(String verb){
        if(!verbs.containsKey(verb)){
        	VerbData.verbs.put(verb, new VerbData(verb));
        }
        return VerbData.verbs.get(verb);
    }
    
    /**
     * This method takes in the subject (such as "cat"), subject thread and the boolean
     * indicating whether the subject can perform the action and stores in the VerbData.
     *  
     * @param subjectName
     * @param subjectThread
     * @param capability
     */
    public void addSubject(String subjectName, String subjectThread, boolean capability){
        subjectMap.put(subjectThread, capability);
        threadMap.put(subjectThread, subjectName);
    }

    /**
     * Return the list of subjects that can perform this action
     * 
     * @return
     */
    public String getSubjectThreads(){
        String threadStrings = "";
        for (String subject: subjectMap.keySet()){
            threadStrings = threadStrings.concat(subject + "\n\n");
        }
        return threadStrings;
    }

    /**
     * Get the full representation of the verb, such as "action travel swim"
     * @return
     */
    public String getVerb(){
        return verb;
    }
    
    public HashMap<String,Boolean> getSubjectMap(){
        return subjectMap;
    }
    
    public HashMap<String,String> getThreadMap(){
        return threadMap;
    }    
}
