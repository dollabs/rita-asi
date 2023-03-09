/**
 * 
 */
package kevinWhite;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import frames.entities.Bundle;
import utils.Mark;
/**
 * @author minhtuev
 *
 */
public class DemoUI extends JFrame {

	JTextArea leftTextArea;
	JTextArea rightTextArea;
    JLabel instructLbl;
    JTextField questionField;
    JButton clearButton;
    private final JPanel lp;
    private AutomatedPanel midPanel;
    private JPanel rightPanel;
    private ConceptManager conceptManager;
    // TODO: refactor and remove the hashmap
    HashMap<String, AutomatedPanel> panelMap;
    JSplitPane split1;
    JSplitPane split2; 
    
	public DemoUI(AutomatedPanel midPanel, String windowTitle, ConceptManager conceptManager,
			HashMap<String, AutomatedPanel> panelMap)
	{
		// Set windows size
        setSize(1024, 768);
        
        this.midPanel = midPanel;
        this.panelMap = panelMap;
        this.conceptManager = conceptManager;
        this.lp = this.getLeftPanel();
        this.rightPanel = this.getRightPanel();
        this.setTitle(windowTitle);
        
          
        // Splitting the window into three panels
        split1 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.lp, this.midPanel);
        split1.setResizeWeight(0);
        split1.setContinuousLayout(true);
        split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, split1, this.rightPanel);
        split2.setResizeWeight(1);
        split2.setContinuousLayout(true);

        // getContentPane gets the reference to the base pane
        getContentPane().setLayout(new GridLayout());
        getContentPane().add(split2);
        setDefaultCloseOperation(EXIT_ON_CLOSE);       
    }
	
	/**
	 * Return the text area
	 * @param threadList
	 * @return
	 */
	private JTextArea getTextArea(String threadList){
		JTextArea textInput = new JTextArea(threadList);
        textInput.setLineWrap(true);
        textInput.setWrapStyleWord(true);
        textInput.setEditable(false);
        textInput.setPreferredSize(new Dimension(this.getWidth()/3,this.getWidth()/2));
        return textInput;
	}
	
	private void setQuestionField(){
	    this.questionField = new JTextField();
        this.questionField.setMaximumSize(new Dimension(this.getWidth()/3,50));
        this.questionField.addKeyListener(new KeyListener(){
            @Override
            public void keyPressed(KeyEvent arg0) {
            }
            @Override
            public void keyReleased(KeyEvent arg0) {
            }
            @Override
            public void keyTyped(KeyEvent arg0) {
                if(KeyEvent.getKeyText(arg0.getKeyChar()).equalsIgnoreCase("Enter")){
                    String line = questionField.getText();
                    try {
        				HashMap<String,Object> sentData = FasterLLConcept.parseSimpleSentence(line);
        				// display the initial bundle
        				setLeftTextAreaText(((Bundle)sentData.get("noun_bundle")).toString());
        				// get the verb concept
        				FasterLLConcept verbConcept = conceptManager.getConcept((String) sentData.get("verb"));
        				// display the reduced bundle
        				setRightTextAreaText(verbConcept.interpretSimpleSentence(sentData));
        				setTitle(line);
        				midPanel = panelMap.get((String)sentData.get("verbName"));
        				Mark.say("panel name:" + midPanel.getName());
        				split1.setRightComponent(midPanel);
        				
        				repaint();
        				revalidate();

                    	Mark.say(line);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    instructLbl.setText(line);
                    questionField.setText("");
                    repaint();
                    revalidate();
                }
                
            }
        });	
	}
	
	private JPanel getLeftPanel()
	{
        this.leftTextArea = this.getTextArea("");
        this.instructLbl = new JLabel("Please input a statement.");
        this.instructLbl.setAlignmentX(CENTER_ALIGNMENT);        
        this.setQuestionField();

		JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.add(this.leftTextArea);
        panel.add(this.instructLbl);
        panel.add(this.questionField);
        return panel;
	}
	
	private JPanel getRightPanel()
	{
        this.rightTextArea = this.getTextArea("");

		JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.add(this.rightTextArea);
        return panel;
	}
	
	public void setRightTextAreaText(String text)
	{
		this.rightTextArea.setText(text);
	}
	
	public void setLeftTextAreaText(String text)
	{
		this.leftTextArea.setText(text);
	}
}
