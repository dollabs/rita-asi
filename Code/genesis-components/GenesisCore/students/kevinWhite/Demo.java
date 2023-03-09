/**
 * 
 */
package kevinWhite;

import java.util.HashMap;

import javax.swing.JOptionPane;

import frames.entities.Bundle;
import frames.entities.Entity;
import utils.Mark;

/**
 * @author minhtuev
 *
 * This is the main demonstration for our project to resolve ambiguity
 */
public class Demo {
	
	SentenceSystem sentSys;
	AutomatedLearner automatedLearner;
	AutomatedPanel leftPanel;
	AutomatedPanel rightPanel;
	AutomatedPanel midPanel;
	
	Entity sentenceEntity;
	HashMap<String, AutomatedPanel> panelMap;

	public Demo()
	{
		this.sentSys = new SentenceSystem();
		this.automatedLearner = new AutomatedLearner(this.sentSys.getSentences(), false);
		for (FasterLLConcept flc: this.automatedLearner.getConceptList()){
			this.sentSys.conceptManager.addConcept(flc);
		}

		this.panelMap = new HashMap<String, AutomatedPanel>();

		for (VerbData data: VerbData.verbs.values()){
			AutomatedPanel panel = new AutomatedPanel(data);
			this.panelMap.put(panel.getName(), panel);
			Mark.say("Panel Name:" + panel.getName());
		}
	}
	
	/**
	 * Debugging function, show a popup message.
	 * @param msg
	 */
	public void popup(String msg)
	{
		JOptionPane.showMessageDialog(null, msg, "My awesome demo", JOptionPane.OK_CANCEL_OPTION); 
	}
	
	/**
	 * The main run method
	 */
	public void run()
	{
		String sentence = "Cats can walk.";
		try
		{
			HashMap<String,Object> sentData = FasterLLConcept.parseSimpleSentence(sentence);
			FasterLLConcept verbConcept = this.sentSys.conceptManager.getConcept((String) sentData.get("verb"));
			Mark.say("verb:" + sentData.get("verbName"));
			
			this.midPanel = this.panelMap.get(sentData.get("verbName"));
			
			DemoUI demoUI = new DemoUI(this.midPanel, sentence,
					this.sentSys.conceptManager, this.panelMap);
			
			demoUI.setRightTextAreaText(verbConcept.interpretSimpleSentence(sentData));
			demoUI.setLeftTextAreaText(((Bundle)sentData.get("noun_bundle")).toString());
			demoUI.setVisible(true);
		} catch (Exception ex)
		{
			Mark.say(ex);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Demo demo = new Demo();
		demo.run();
	}

}
