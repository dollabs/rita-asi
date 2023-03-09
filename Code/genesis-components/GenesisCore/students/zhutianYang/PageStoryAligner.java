package zhutianYang;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import utils.Html;
import utils.Colors;
import utils.Mark;
import utils.Z;
import zhutianYang.PageHowToLearner.MyActionListener;
import connections.Connections;
import connections.TextEntryBox;
import dictionary.WordNet;
import frames.entities.Bundle;

/*
 * Created on Apr 5, 2019 by Z
 */

public class PageStoryAligner extends JPanel {

	static String[] labels = new String[]{"Demo Story", 
		"Type/Domain", "Storyteller/Sensemaker",
		"Synopsis",
		"Knowledge"};

	static String[][] data1 = new String[][]{

		// 1
		{"Replace Battery", 	"Procedure \n" + 
				"-- Repair skills", 	"Observer",
		"Observe two events to learn the procedure of replace phone battery",
		"Abduction rules of wants", 	"corpora/Ensembles/Repair a phone.txt"},

		// 2
		{"CPR", 				"Procedure \n" + 
				"-- Life skills",		"Reporter/\n" + 
						"Newspaper readers",
		"Read two news stories to learn the emergency procedure of saving drowning people",
		"Abduction rules of wants", 	"corpora/Ensembles/Save drowning people.txt"},

		// 3
//				{"Make Martini", 		"Cooking skills", 	"Recipe readers",
//				"Read four online recipes to learn the essence of making an Old-Fashioned Martini",
//				"Rules of verb incorporation", 	"corpora/Ensembles/Repair a phone.txt"},

		// 4
		{"Two little liars", 	"Moral\n" + 
				"-- Don't lie",		"Adults/\n" + 
						"Children",
		"Watch one ad and be reminded of the product in daily life",
		"Deduction rules of outcomes", 	"corpora/Ensembles/Two little liers.txt"},

		// 5a
		{"Another Hamlet", 		"Concept formation\n" + 
				"-- Common plot",		"Writers/\n" + 
						"Readers, Watchers",
		"Read two stories to learn the pattern of prince's revenge for his father",
		"Deduction rules of outcomes", 			"corpora/Ensembles/Revenge for father.txt"},

//			// 5b
//			{"Revenge from anger", 		"Concept modification\n" + 
//					-- Common plot",			"Literature readers",
//			"Read two stories to learn the pattern of revenge",
//			"Deductions rules", 			"corpora/Ensembles/Revenge from anger.txt"}, ////////////////??!!

		// 6
		{"Boeing 737", 			"Problem\n" + 
				"-- Identify causes",			"Investigator/\n" + 
						"Public",
		"Read two stories to learn the pattern of revenge",
		"Deduction rules of reasons", 			"corpora/Ensembles/Boeing 737 Max 8 crash.txt"},

		// 7
		{"Ticket Kiosk", 		"Problem\n" + 
				"-- Identify needs",		"Product designers/\n" + 
						"User",
		"Observe two scenarios of using the subway kiosk to identify user needs",
		"Deduction rules of reasons", 			"corpora/Ensembles/Idenify needs.txt"},
		

		// 8
		{"Twice as Much", 	"Problem\n" + 
				"-- Identify needs",		"Ancient villagers/\n" + 
						"Current students",
		"Discover similar problems in real life to calculation distributions according to proportions",
		"Deduction rules of outcomes", 	"corpora/Ensembles/Geometric progressions.txt"}
	};

	static String[][] data2 = new String[][]{

		// 9
		{"Churchill predicts", 	"Solution \n" + 
				"-- Military strategy",		"Military strategists/\n" + 
						"Decision makers",
		"Discover the current situation is similar to that of the past.",
		"Deduction rules of outcomes", 	"corpora/Ensembles/Winston sees history repeat.txt"},

		// 10(a)
		{"Alex Reads Memoir", 	"Solution\n" + 
				"-- Personal strategy", 	"Memoir writers/\n" + 
						"Readers",
		"Read one memoir to learn the next action for oneself",
		"Deduction rules of outcomes", 	"corpora/Ensembles/Alex reads a memoir.txt"},

		// 10(b)
		{"I Read Memoir", 	"Solution\n" + 
				"-- Personal strategy",		"Memoir writers/\n" + 
						"Readers",
		"Read the same memoir to learn a different next action for oneself",
		"Deduction rules of outcomes", 	"corpora/Ensembles/I read a memoir.txt"},

		// 11
		{"Use GAIN", 	"Solution\n" + 
				"-- Call for action",		"Advertiser/\n" + 
						"Consumers",
		"Watch one ad and be reminded of the product in daily life",
		"Deduction rules of outcomes", 	"corpora/Ensembles/Use GAIN.txt"},

		// 12
		{"Jupiter has moons", 	"Hypothesize \n" + 
				"-- About stars",		"Astronomer/\n" + 
						"Physics community",
		"Hypothesize that the Jupoter has its moons, like the Earth, according to the movements of stars.",
		"Deduction rules of outcomes", 	"corpora/Ensembles/Galileo predicts moons of Jupiter.txt"},

		// 13
		{"Light has particles", 	"Hypothesize \n" + 
				"-- About light",		"Physicist/\n" + 
						"Physics community",
		"Hypothesize that the light has discrete particles, like ideal gas, according to similar energy equations.",
		"Deduction rules of outcomes", 	"corpora/Ensembles/Einstein predicts particles in light.txt"}

//				{"Breakup Hurts", 		"Attribute causes",		"Anyone",
//				"Compare what happened before and what is happening now to identify the cause",
//				"Abduction rules", 	"corpora/Ensembles/Breakup hurts.txt"},

		};

	static List<String> buttonDisabled = Arrays.asList("Breakup Hurts", "Two little liars");

	public PageStoryAligner(){
	    super();
	    JPanel one = new JPanel();
	    JPanel two = new JPanel();
//	    JPanel three = new JPanel();

	    this.setLayout(new GridLayout(2,1));
	    Border border = BorderFactory.createLineBorder(Color.LIGHT_GRAY);


	    this.add(one);
	    one.setLayout(new BorderLayout());

	    JLabel label1 = new JLabel("<html>Learn from <br/>Similarity</html>");
	    label1.setVerticalAlignment(JLabel.CENTER);
	    label1.setHorizontalAlignment(JLabel.CENTER);
	    label1.setBorder(new EmptyBorder(0,20,0,20));
	    label1.setFont(ZPage.largeFont);
	    one.add(label1, BorderLayout.WEST);

	    JPanel table1 = new JPanel();
	    table1.setLayout(new GridLayout(data1.length + 1,labels.length));

	    for(String data: labels) {
	    	JLabel text = new JLabel(data);
	    	text.setVerticalAlignment(JLabel.CENTER);
	    	text.setHorizontalAlignment(JLabel.CENTER);
	    	text.setBorder(new EmptyBorder(0,20,0,20));
	    	if (data.contains("/")) text.setFont(ZPage.medianFont);
	    	else text.setFont(ZPage.largeFont);
    		table1.add(text);
	    }

	    for(String[] data: data1) {
	    	JButton button = new JButton(data[0]);
	    	button.setFont(ZPage.medianFont);
	    	button.addActionListener(new MyActionListener());
	    	if(buttonDisabled.contains(data[0])) button.setEnabled(false);
	    	table1.add(button);
	    	for(int i=1; i<data.length-1; i++) {
	    		JTextArea text = new JTextArea(data[i]);
	    		text.setLineWrap(true); text.setWrapStyleWord(true);
//	    		text.setEditable(false);
	    		text.setFont(ZPage.medianFont);
	    		if(buttonDisabled.contains(data[0])) text.setBackground(Z.UI_GREY);
	    		if(i==3) text.setFont(ZPage.smallFont);
	    		text.setBorder(BorderFactory.createCompoundBorder(border,
	    		            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
	    		table1.add(text);
	    	}
	    }
	    table1.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
	    one.add(table1, BorderLayout.CENTER);



	    this.add(two);
	    two.setLayout(new BorderLayout());

	    JLabel label2 = new JLabel("<html>Learn from <br/>Difference</html>");
	    label2.setVerticalAlignment(JLabel.CENTER);
	    label2.setHorizontalAlignment(JLabel.CENTER);
	    label2.setBorder(new EmptyBorder(0,20,0,20));
	    label2.setFont(ZPage.largeFont);
	    two.add(label2, BorderLayout.WEST);

	    JPanel table2 = new JPanel();
	    table2.setLayout(new GridLayout(data2.length,labels.length));

	    for(String[] data: data2) {
	    	JButton button = new JButton(data[0]);
	    	button.setFont(ZPage.medianFont);
	    	button.addActionListener(new MyActionListener());
	    	if(buttonDisabled.contains(data[0])) button.setEnabled(false);
	    	table2.add(button);
	    	for(int i=1; i<data.length-1; i++) {
	    		JTextArea text = new JTextArea(data[i]);
	    		text.setLineWrap(true); text.setWrapStyleWord(true);
//	    		text.setEditable(false);
	    		text.setFont(ZPage.medianFont);
	    		if(buttonDisabled.contains(data[0])) text.setBackground(Z.UI_GREY);
	    		if(i==3) text.setFont(ZPage.smallFont);
	    		text.setBorder(BorderFactory.createCompoundBorder(border,
	    		            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
	    		table2.add(text);
	    	}
	    }

	    table2.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
	    two.add(table2, BorderLayout.CENTER);



	  }

	class MyActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			String buttonName = button.getText();

			for(String[] data: data1) {
				if(buttonName.equals(data[0])) {
					RecipeExpert.runProblemSolving(data[5]);
				}
			}
			for(String[] data: data2) {
				if(buttonName.equals(data[0])) {
					RecipeExpert.runProblemSolving(data[5]);
				}
			}
		}
	}


	  public static void main(String[] args) {
		  PageStoryAligner page = new PageStoryAligner();
		  JFrame frame = new JFrame();
		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  frame.getContentPane().add(page);
		  frame.setBounds(ZPage.windowSize);
		  frame.setVisible(true);
	  }
}
