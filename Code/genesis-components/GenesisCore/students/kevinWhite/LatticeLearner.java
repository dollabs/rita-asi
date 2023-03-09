package kevinWhite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import translator.BasicTranslator;
import utils.Mark;
import connections.AbstractWiredBox;
import frames.entities.Entity;

public class LatticeLearner extends AbstractWiredBox {
    
    private static BasicTranslator interpreter = BasicTranslator.getTranslator();
    private ArrayList<Entity> sentences = new ArrayList<Entity>();
    private HashMap<Integer,String> nounThreads = new HashMap<Integer,String>();
    private HashMap<Integer,String> verbThreads = new HashMap<Integer,String>();
    private static Set<String> impliedPositives = new HashSet<String>();
    private static Set<String> impliedNegatives = new HashSet<String>();
    private UI nounUI;
    private UI verbUI;

    public LatticeLearner() throws Exception{
        interpreter = BasicTranslator.getTranslator();
        translateFlyers();
        translateSwimmers();
        translateLandLovers();
        translateFurry();
//        for(Entity sentence: sentences){
//            Mark.say(sentences.indexOf(sentence),sentence.asString());
//        }
        parse();
        nounUI = new UI(nounThreads.values().toArray());
        nounUI.setTitle("Nouns");
        nounUI.setVisible(true);
//        parseVerbs();
//        verbUI = new UI(verbThreads.values().toArray());
//        verbUI.setTitle("Verbs");
//        verbUI.setVisible(true);
    }
    
    /**
     * Build the verb lattice structure for "fly"
     * TODO: should we refactor this code so that it returns list of entities?
     * @throws Exception
     */
    public void translateFlyers() throws Exception{
        Entity birds = interpreter.translate("Birds can fly.");
        Entity fish = interpreter.translate("Fish cannot fly.");
        Entity airplane = interpreter.translate("Airplanes can fly.");
        Entity cars = interpreter.translate("Cars can't fly.");
        Entity bats = interpreter.translate("Bats can't fly.");
        sentences.add(birds);
        sentences.add(fish);
        sentences.add(airplane);
        sentences.add(cars);
        sentences.add(bats);
    }
    
    /**
     * Build the verb lattice structure for "swim"
     * @throws Exception
     */
    public void translateSwimmers() throws Exception{
        Entity fish = interpreter.translate("Fish can swim.");
        Entity plants = interpreter.translate("Plants can't swim.");
        Entity boats = interpreter.translate("Boats can swim.");
        Entity cars = interpreter.translate("Cars can't swim.");
        Entity ducks = interpreter.translate("Ducks can swim.");
        Entity sparrow = interpreter.translate("Sparrows cannot swim.");
        Entity dogs = interpreter.translate("Dogs can swim.");
        Entity bats = interpreter.translate("Bats cannot swim.");
        sentences.add(fish);
        sentences.add(plants);
        sentences.add(boats);
        sentences.add(cars);
        sentences.add(ducks);
        sentences.add(sparrow);
        sentences.add(dogs);
        sentences.add(bats);
    }
    
    /**
     * Build the lattice structure for land entities
     * @throws Exception
     */
    public void translateLandLovers() throws Exception{
        Entity plants = interpreter.translate("Plants prefer land.");
        Entity fish = interpreter.translate("Fish do not prefer land.");
        Entity cars = interpreter.translate("Cars prefer land.");
        Entity airplane = interpreter.translate("Airplanes don't prefer land.");
        Entity dogs = interpreter.translate("Dogs prefer land.");
        Entity bats = interpreter.translate("Bats don't prefer land.");
        sentences.add(plants);
        sentences.add(fish);
        sentences.add(cars);
        sentences.add(airplane);
        sentences.add(dogs);
        sentences.add(bats);
    }
    
    /**
     * Build a lattice structure for furry aniamls
     * @throws Exception
     */
    public void translateFurry() throws Exception{
        Entity mammals = interpreter.translate("Mammals are furry.");
        Entity birds = interpreter.translate("Birds are not furry.");
        Entity rugs = interpreter.translate ("Rugs are furry.");
        Entity cars = interpreter.translate("Cars aren't furry.");
        sentences.add(mammals);
        sentences.add(birds);
        sentences.add(rugs);
        sentences.add(cars);
    }

    /**
     * Main parsing function
     * Iterate over every sentence and parse 
     */
    public void parse(){
        for (Entity sentence: sentences){
            int nounKey = sentence.getElement(0).getSubject().getPrimedThread().toString(true).hashCode();
            if (!nounThreads.containsKey(nounKey)){
                nounThreads.put(nounKey, sentence.getElement(0).getSubject().getPrimedThread().toString(true));
            }
            
            String tempString = "";
            int verbKey = 0;
            if (sentence.getElement(0).hasFeature("not")){
                tempString = "not " + sentence.getElement(0).getPrimedThread().toString(true);
                verbKey = tempString.hashCode();
            }
            
            else{
                tempString = sentence.getElement(0).getPrimedThread().toString(true);
                verbKey = tempString.hashCode();
            }
            
            if (!verbThreads.containsKey(verbKey)){
                verbThreads.put(verbKey, tempString);
            }
        }
    }
    
    /**
     * Answer the input question given the set of affirmatives & negatives
     * 
     * @param question
     * @param affirmatives
     * @param negatives
     * @return
     * @throws Exception
     */
    public static String answerQuestion(String question, Set<String> affirmatives, Set<String> negatives) throws Exception{
        Entity ansEntity = interpreter.translate(question);
//        String sentBrkdwn = ansEntity.toString();
//        Mark.say(sentBrkdwn);
        if (question.contains("?")){
            String ansSubject = "";
            try{
                ansSubject = ansEntity.getElement(0).getSubject().getSubject().asStringWithoutIndexes().split("-")[0];
                Mark.say(Arrays.toString(impliedPositives.toArray()),Arrays.toString(impliedNegatives.toArray()));
                Mark.say(ansSubject);
            }
            
            catch (Exception e){
                ansSubject = ansEntity.getElement(0).getSubject().getFeatures().toString().split(" ")[1];
            }
            if (affirmatives.contains(ansSubject)||impliedPositives.contains(ansSubject)){
                return "Yes.";
            }
            else if (negatives.contains(ansSubject)||impliedNegatives.contains(ansSubject)){
                return "No.";
            }
            else {
                return "This lattice does not contain the information requested.";
            }
        }
        
        else{
            String ansSubject = ansEntity.getElement(0).getSubject().asStringWithoutIndexes().split("-")[0];
            if (affirmatives.contains(ansSubject)||impliedPositives.contains(ansSubject)){
                return "True.";
            }
            else if (negatives.contains(ansSubject)||impliedNegatives.contains(ansSubject)){
                return "False.";
            }
            else {
                ansSubject = ansEntity.getElement(0).getObject().getFeatures().toString().split(" ")[1];
                if (affirmatives.contains(ansSubject)||impliedPositives.contains(ansSubject)){
                    return "True.";
                }
                else if (negatives.contains(ansSubject)||impliedNegatives.contains(ansSubject)){
                    return "False.";
                }
                else{
                    return "This lattice does not contain the information requested.";
                }
            }
        }
    }
    
    public static void getImplication(boolean isGreen, String thread,Set<String> positives, Set<String> negatives){
        if (isGreen){
            if (impliedNegatives.contains(thread)){
                impliedNegatives.remove(thread);
            }
            impliedPositives.add(thread);
        }

        else{
            if (impliedPositives.contains(thread)){
                impliedPositives.remove(thread);
            }
            impliedNegatives.add(thread);
        }

        impliedPositives.removeAll(positives);
        impliedNegatives.removeAll(negatives);
    }
    
    public static Set<String> getImpliedPositives(){
        return impliedPositives;
    }
    
    public static Set<String> getImpliedNegatives(){
        return impliedNegatives;
    }
    
    public UI getNounUI(){
        return nounUI;
    }
    
    public UI getVerbUI(){
        return verbUI;
    }
    
    /**
     * Main function that runs the show
     * @param args
     */
    public static void main(String[] args){
        try {
            LatticeLearner test = new LatticeLearner();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
