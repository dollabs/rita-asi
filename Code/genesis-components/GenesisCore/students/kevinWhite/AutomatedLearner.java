package kevinWhite;

import java.util.ArrayList;
import java.util.List;

import frames.entities.Entity;
import frames.entities.Thread;
import translator.BasicTranslator;
import utils.Mark;

/**
 * 
 * @author Kevin
 *
 * TODO: add documentation for class
 */
public class AutomatedLearner{
	private ArrayList<AutomatedPanel> latticeList = new ArrayList<AutomatedPanel>();
	private ArrayList<Entity> sentences = new ArrayList<Entity>();
	private static BasicTranslator interpreter = new BasicTranslator();
	private ArrayList<FasterLLConcept> conceptList = new ArrayList<FasterLLConcept>();
	// the actual lattice data structure
	private TypeLattice lattice;
	private FasterLLConcept concept;

	/**
	 * 
	 * @param statements, the list of test data that are used to create the inital lattices 
	 * @param withUI, determines whether or not to display the user interface associated with t
	 * created lattices
	 */
	public AutomatedLearner(ArrayList<String> statements, boolean withUI) {
		interpretSentences(statements);
		parse();
		// If we have UI option, we build a window for each verb
		if (withUI){
			buildLatticePanels();
			buildUI();
		}
		else{
			for (VerbData vd: VerbData.verbs.values()){
				String threadList = vd.getSubjectThreads();

				List<Thread> threads = new ArrayList<Thread>();
				String[] threadArray = threadList.split("\n\n");
				for (String string : threadArray) {
					threads.add(Thread.parse(string));
				}	
				lattice = new TypeLattice(threads);
				concept = new FasterLLConcept(lattice,vd.getVerb());
				teachLattice(vd,threadArray);
				this.conceptList.add(concept);
//				if (vd.getVerb().equals("action transfer move propel throw")){
//					Mark.say("Testing...");
//					try {
//						concept.parseSimpleSentence("A cat can throw a ball.");
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
			}
		}
	}
	
	private void interpretSentences(ArrayList<String> statements)
	{
		for (String sentence: statements){
			try {
				Entity tempEntity = interpreter.translate(sentence);
				sentences.add(tempEntity);
			} catch (Exception e) { 
				Mark.say("This sentence could not be parsed. Moving on to the next sentence...");
				continue;
			}
		}
	}

	public AutomatedLearner(ArrayList<String> statements) {
		interpretSentences(statements);
		parse();
		buildLatticePanels();
		buildUI();
	}

	/**
	 * Responsible for parsing sentences and generating the verb data that will
	 * be used to build lattices
	 * TODO: might consider refactoring this into VerbData
	 */
	public void parse(){
		for (Entity sent: sentences){
			String subjectThread = sent.getElement(0).getSubject().getPrimedThread().toString(true);
			String subjectName = sent.getElement(0).getSubject().asStringWithoutIndexes().split("-")[0];

			String verbName = sent.getElement(0).getPrimedThread().toString(true);
			VerbData tempVerb = VerbData.getVerbData(verbName);

			if (sent.getElement(0).hasFeature("not")){
				tempVerb.addSubject(subjectName, subjectThread, false);
			}
			else{
				tempVerb.addSubject(subjectName, subjectThread, true);
			}    
		}
	}
	
	/**
	 * Static method to parse a single sentence and add the data to VerbData
	 * 
	 * TODO: might consider refactoring this into VerbData
	 * @param sentence
	 * @throws Exception 
	 */
	public static void parse(String sentence) throws Exception{
		Entity ent = interpreter.translate(sentence);
		String subjectThread = ent.getElement(0).getSubject().getPrimedThread().toString(true);
		String subjectName = ent.getElement(0).getSubject().asStringWithoutIndexes().split("-")[0];

		String verbName = ent.getElement(0).getPrimedThread().toString(true);
		VerbData tempVerb = VerbData.getVerbData(verbName);

		if (ent.getElement(0).hasFeature("not")){
			tempVerb.addSubject(subjectName, subjectThread, false);
		}
		else{
			tempVerb.addSubject(subjectName, subjectThread, true);
		}    
	}
	
	/**
	 * Debugging function, take entity as input instead
	 * @param ent
	 */
	public static void parse(Entity ent){
		String subjectThread = ent.getElement(0).getSubject().getPrimedThread().toString(true);
		String subjectName = ent.getElement(0).getSubject().asStringWithoutIndexes().split("-")[0];

		String verbName = ent.getElement(0).getPrimedThread().toString(true);
		VerbData tempVerb = VerbData.getVerbData(verbName);

		if (ent.getElement(0).hasFeature("not")){
			tempVerb.addSubject(subjectName, subjectThread, false);
		}
		else{
			tempVerb.addSubject(subjectName, subjectThread, true);
		}    
	}

	/**
	 * TODO: add documentation
	 */
	private void buildLatticePanels(){
		for (VerbData data: VerbData.verbs.values()){
			AutomatedPanel tempPanel = new AutomatedPanel(data);
			latticeList.add(tempPanel);
		}
	}

	/**
	 * Iterate through all verb panels and build a separate window for each verb
	 */
	private void buildUI(){
		for (AutomatedPanel leftPanel: latticeList){
			AutomatedUI tempUI = new AutomatedUI(leftPanel,leftPanel.getName());
			tempUI.setVisible(true);
		}
	}

	/**
	 * 
	 * @param vd, the verb data associated with the lattice
	 * @param threads, the list of threads being used to build
	 * the lattice with positive and negative samples
	 */
	private void teachLattice(VerbData vd,String[] threads){
		for (String thread : threads){
			if(vd.getSubjectMap().get(thread)){
//				Mark.say(vd.getVerb(),": ", thread, "\n is positive.");
				concept.learnPositive(vd.getThreadMap().get(thread));
			}
			else{
//				Mark.say(vd.getVerb(),": ", thread, "\n is negative.");
				concept.learnNegative(vd.getThreadMap().get(thread));
			}
		}
	}

	public static String answerQuestion(String question, AutomatedPanel lp) throws Exception{
		Entity ansEntity = interpreter.translate(question);
		if (question.contains("?")){
			String ansSubject = "";
			try{
				ansSubject = ansEntity.getElement(0).getSubject().getSubject().asStringWithoutIndexes().split("-")[0];
			}

			catch (Exception e){
				ansSubject = ansEntity.getElement(0).getSubject().getFeatures().toString().split(" ")[1];
			}
			if (lp.getConcept().infer(ansSubject)){
				return "Yes.";
			}
			else{
				return "No.";
			}
		}

		else{
			String ansSubject = ansEntity.getElement(0).getSubject().asStringWithoutIndexes().split("-")[0];
			if (lp.getConcept().infer(ansSubject)){
				return "True.";
			}
			else if (!lp.getConcept().infer(ansSubject)){
				return "False.";
			}
			else {
				ansSubject = ansEntity.getElement(0).getObject().getFeatures().toString().split(" ")[1];
				if (lp.getConcept().infer(ansSubject)){
					return "True.";
				}
				else if (!lp.getConcept().infer(ansSubject)){
					return "False.";
				}
				else{
					return "This lattice does not contain the information requested.";
				}
			}
		}
	}
	
	public ArrayList<FasterLLConcept> getConceptList(){
		return this.conceptList;
	}
}
