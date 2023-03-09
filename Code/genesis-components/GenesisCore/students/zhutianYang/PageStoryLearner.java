package zhutianYang;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import utils.Html;
import utils.Colors;
import utils.Mark;
import utils.Z;
import connections.Connections;
import connections.TextEntryBox;
import dictionary.WordNet;
import frames.entities.Bundle;

/*
 * Created on Mar 10, 2008 @author phw
 */

public class PageStoryLearner extends JPanel implements ActionListener {
	
	String storyPath = "corpora/zStoriesForPS/";
	
	public static String stepsFileName = "";
	public static String testFileName = "";
	
	int commonGoalsHeight = 200;
	String stringNext = "Next Sentence";
	
	// top: story buttons
	JPanel top = new JPanel();
	JButton readingMode;
	List<String> sentences;
	int sentenceCount = 0;
	
	// right side bar: history of relations
	JPanel sidebar = new JPanel();
	
	JPanel historyPane = new JPanel();
	JPanel history = new JPanel();
	JLabel historyLabel = new JLabel("Story & State Changes");
	JPanel historyPane1 = new JPanel();
	JPanel historyPane2 = new JPanel();
	JPanel historyPane3 = new JPanel();
	JPanel historyPane4 = new JPanel();
	List<JPanel> historyPanes = Arrays.asList(historyPane1, historyPane2 ,historyPane3 ,historyPane4);//, commonGoalsPane);
	
	String[] storyNames = {"Add story ..."};
	
	List<JButton> haveReadStories = new ArrayList<>();
	JComboBox combo1 = new JComboBox(storyNames);
	JComboBox combo2 = new JComboBox(storyNames);
	JComboBox combo3 = new JComboBox(storyNames);
	JComboBox combo4 = new JComboBox(storyNames);
	List<JComboBox> combos = Arrays.asList(combo1, combo2 ,combo3 ,combo4);
	
	JTextPane story1 = new JTextPane();
	JTextPane story2 = new JTextPane();
	JTextPane story3 = new JTextPane();
	JTextPane story4 = new JTextPane();
	List<JTextPane> printStories = Arrays.asList(story1, story2, story3, story4);// , commonGoals);
	
	JTextPane history1 = new JTextPane();
	JTextPane history2 = new JTextPane();
	JTextPane history3 = new JTextPane();
	JTextPane history4 = new JTextPane();
	List<JTextPane> histories = Arrays.asList(history1, history2, history3, history4);// , commonGoals);
	int noOfHistories = 2;
	
	JTextPane currentHistory;
	List<List<RGoal>> rGoalLists = new ArrayList<>();
	List<RGoal> commonRGoals = new ArrayList<>();
	
	JButton fromStory1 = new JButton();
	JButton fromStory2 = new JButton();
	JButton fromStory3 = new JButton();
	JButton fromStory4 = new JButton();
	List<JButton> buttons = Arrays.asList(fromStory1, fromStory2, fromStory3, fromStory4);//, getCommon);
	String[] buttonNames = {stringNext, stringNext, stringNext, stringNext};
	JButton addMoreStories = new JButton("Add more stories");
	
	JPanel rightPane = new JPanel();
	JPanel commonGoalsPane = new JPanel();
	JTextPane commonGoals = new JTextPane();
	JLabel commonGoalsLabel = new JLabel("Common State Changes");
	JButton getCommon = new JButton("Get"); 
	JButton generateRecipe = new JButton("Remember"); 
	JButton clearCommon = new JButton("Clear"); 
	
	JPanel microstoriesPane = new JPanel();
	JTextPane microstories = new JTextPane();
	JLabel microstoriesLabel = new JLabel("Remembered Knowledge");
	JButton microstoriesUpdate = new JButton("Update");
	JButton microstoriesTest = new JButton("Test");
	JButton microstoriesDelete = new JButton("Delete");
	
	// center main text area
	JPanel definitionPane = new JPanel();
	JTextPane definition = new JTextPane();
	JLabel definitionLabel = new JLabel("Sentence Translator");
	
	// bottom input area
	JTextField word = new JTextField();
	Boolean firstInput = true;
	String defaultText = "Input a sentence here";

	public PageStoryLearner() {
		super();
		initiateBooks();
		this.setLayout(new BorderLayout());
		this.add(word, BorderLayout.SOUTH);
		this.add(sidebar, BorderLayout.EAST);
		this.add(definitionPane);
		
		// center
		definitionPane.setPreferredSize(new Dimension(500, 500));
		definitionPane.setLayout(new BorderLayout());
		definitionPane.add(new JScrollPane(definition), BorderLayout.CENTER);
		definitionPane.setBorder(ZPage.defaultBorder);
		
		definitionPane.add(top, BorderLayout.NORTH);
		top.setLayout(new GridLayout(0, 1));
		top.setPreferredSize(ZPage.headerSize);
		top.add(ZPage.headerStoryLearner);
		top.setBorder(ZPage.bottomBorder);
		
//		definitionPane.add(definitionLabel, BorderLayout.NORTH);
//		definitionLabel.setFont(ZPage.labelFont);
//		definitionLabel.setBorder(ZPage.bottomBorder);
//		definitionLabel.setHorizontalAlignment(JLabel.CENTER);
		ZPage.appendToPane(definition,  "\n    Input a sentence and I will guess the state changes in the world."
				+ "\n       by showing Engish -> Semantics -> State Changes");
		
		// -------------- middle panel of rGoals
		sidebar.setLayout(new BorderLayout());
//		sidebar.setPreferredSize(new Dimension(ZPage.doubleSidebarPaneWidth, 120));
		sidebar.add(historyPane);
		historyPane.setPreferredSize(new Dimension(ZPage.doubleSidebarPaneWidth, 1000));
		historyPane.setBorder(ZPage.defaultBorder);
		historyPane.setLayout(new BorderLayout());
		historyPane.add(historyLabel, BorderLayout.NORTH);
		historyLabel.setFont(ZPage.labelFont);
		historyLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
		historyLabel.setHorizontalAlignment(JLabel.CENTER);
		
		historyPane.add(history);
		history.setLayout(new GridLayout(noOfHistories, 1));
		history.setFont(ZPage.smallFont);
		
		for (int i = 0;i<combos.size();i++) {
			if(i<noOfHistories) history.add(historyPanes.get(i));  // so we can add this window later
			
			historyPanes.get(i).setLayout(new BorderLayout());
			historyPanes.get(i).setBorder(ZPage.bottomBorder);
			
			historyPanes.get(i).add(combos.get(i), BorderLayout.NORTH);
			combos.get(i).addActionListener(this);
			
			JPanel box = new JPanel();
			box.setLayout(new GridLayout());
			box.add(printStories.get(i));
			printStories.get(i).setFont(ZPage.smallFont);
			box.add(new JScrollPane(printStories.get(i)));
			
			box.add(histories.get(i));
			box.add(new JScrollPane(histories.get(i)));
			historyPanes.get(i).add(box);
		}
		
		historyPane.add(addMoreStories, BorderLayout.SOUTH);
		
		addMoreStories.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if(noOfHistories<combos.size()) {
					history.setLayout(new GridLayout(noOfHistories+1, 1));
					history.add(historyPanes.get(noOfHistories++));
					updateInterface();
					
					if(noOfHistories==combos.size()) {
						addMoreStories.setEnabled(false);
					}
				}
			}
		});
		
		
//		for (int i = 0;i<combos.size();i++) {
//			history.add(historyPanes.get(i));
//			historyPanes.get(i).setLayout(new BorderLayout());
//			historyPanes.get(i).add(combos.get(i), BorderLayout.NORTH);
//			combos.get(i).addActionListener(this);
//			historyPanes.get(i).add(histories.get(i));
//			historyPanes.get(i).setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
//			historyPanes.get(i).add(new JScrollPane(histories.get(i)));
//		}
		
	
		sidebar.add(rightPane, BorderLayout.EAST);
		rightPane.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, 1000));
		rightPane.setLayout(new BorderLayout());
		rightPane.setBorder(ZPage.defaultBorder);
		
		// ---- buttom common goals
		rightPane.add(commonGoalsPane, BorderLayout.NORTH);
		commonGoalsPane.setLayout(new BorderLayout());
		commonGoalsPane.setPreferredSize(new Dimension(0, commonGoalsHeight));
		
		// top histories
		commonGoalsPane.add(commonGoalsLabel, BorderLayout.NORTH);
		commonGoalsLabel.setFont(ZPage.labelFont);
		commonGoalsLabel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));
		commonGoalsLabel.setHorizontalAlignment(JLabel.CENTER);
		
		commonGoalsPane.add(commonGoals);
		commonGoalsPane.add(new JScrollPane(commonGoals));
		commonGoals.setFont(ZPage.smallFont);		
		
		JPanel threeButtons = new JPanel();
		threeButtons.setLayout(new GridLayout(1, 3));
		threeButtons.setPreferredSize(new Dimension(0, ZPage.buttonHeight));
		
		threeButtons.add(getCommon);
		getCommon.setEnabled(false);
		getCommon.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if(rGoalLists.size()>=2) {
					commonRGoals = RGoal.getCommonRGoals(rGoalLists);
					String toPrint = RGoal.printRGoals(commonRGoals);
					commonGoals.setText("");
					ZPage.appendToPane(commonGoals, toPrint, Color.red, ZPage.smallFontSize);
					generateRecipe.setEnabled(true);
					clearCommon.setEnabled(true);
				}
			}
		});
		
		threeButtons.add(generateRecipe);
		generateRecipe.setEnabled(false);
		generateRecipe.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				List<String> returns = RecipeLearner.writeKnowledgeFile("Make an old fashioned",commonRGoals);
				stepsFileName = returns.get(0);
				testFileName = returns.get(1);
				microstoriesTest.setEnabled(true);
				microstoriesDelete.setEnabled(true);
				microstories.setText(Z.printTXTFile(stepsFileName));
				microstories.selectAll();
				int x = microstories.getSelectionEnd();
				microstories.select(x, x);
			}
		});
		threeButtons.add(clearCommon);
		clearCommon.setEnabled(false);
		clearCommon.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				commonGoals.setText("");
			}
		});
		commonGoalsPane.add(threeButtons, BorderLayout.SOUTH);
		
		rightPane.add(microstoriesPane);
		microstoriesPane.setLayout(new BorderLayout());
		microstoriesPane.add(microstoriesLabel, BorderLayout.NORTH);
		microstoriesLabel.setFont(ZPage.labelFont);
		microstoriesLabel.setBorder(BorderFactory.createEmptyBorder(16, 0, 5, 0));
		microstoriesLabel.setHorizontalAlignment(JLabel.CENTER);
		
		microstoriesPane.add(microstories);
		microstoriesPane.add(new JScrollPane(microstories));
		microstories.setFont(ZPage.smallFont);
		microstories.setText("microstories will be printed here");
		microstories.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				microstoriesUpdate.setEnabled(true);
			}
		});
		
		// buttons
		JPanel twoButtons = new JPanel();
		twoButtons.setLayout(new GridLayout(1, 3));
		twoButtons.setPreferredSize(new Dimension(0, ZPage.buttonHeight));
		
		twoButtons.add(microstoriesUpdate);
		microstoriesUpdate.setEnabled(false);
		microstoriesUpdate.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				FileWriter writer;
				try {
					writer = new FileWriter(stepsFileName, false);
					writer.write(microstories.getText());
					writer.close();
				}catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		twoButtons.add(microstoriesTest);
		microstoriesTest.setEnabled(false);
		microstoriesTest.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				Mark.night(stepsFileName);
				Mark.night(testFileName);
				RecipeExpert.runProblemSolving(testFileName);
			}
		});
		
		twoButtons.add(microstoriesDelete);
		microstoriesDelete.setEnabled(false);
		microstoriesDelete.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				Mark.night(stepsFileName);
				Mark.night(testFileName);
				Z.deleteFile(stepsFileName);
				Z.deleteFile(testFileName);
				microstories.setText("");
			}
		});
		microstoriesPane.add(twoButtons, BorderLayout.SOUTH);
		
		// bottom
		word.setFont(ZPage.largeInput);
		word.setText(defaultText);
		word.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent event) {
				char key = event.getKeyChar();
				if (ZPage.terminators.indexOf(key) >= 0) {
					// take in input and clear text entry field
					String sentence = word.getText().trim();
					String result = "";
					if(firstInput) {
						definition.setText(result);
						firstInput = false;
					}
					// output the tree and the RGoals
					if (sentence.length() != 0) printSentence(sentence);
				}
			}
		});
		word.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Mark.say(word.getText().trim());
				if (word.getText().trim().equals(defaultText)) {
					word.setText("");
				}
			}
		});
	}
	
	
	// ------------------
	//
	//     listeners
	//
	// ------------------

	public void actionPerformed(ActionEvent e) {
		for(int i=0;i<combos.size();i++) {
			JComboBox cb = combos.get(i);
			String selectedStory = (String)cb.getSelectedItem();
	        
	        // initiate buttons among the four
	        if(!selectedStory.equals(storyNames[0]) && !haveReadStories.contains(buttons.get(i))) {
	        		
	        	historyPanes.get(i).remove(combos.get(i));
				historyPanes.get(i).add(buttons.get(i), BorderLayout.NORTH);
				
				buttons.get(i).addMouseListener(new MouseAdapter() {
					public void mouseReleased(MouseEvent e) {
						if (readingMode!=null) printSentence(sentences.get(sentenceCount));
					}
				});
				buttons.get(i).setPreferredSize(new Dimension(0, ZPage.buttonHeight));
				buttons.get(i).setText(buttonNames[i]);
				buttonNames[i] = selectedStory;
	        	
				readingMode = buttons.get(i);
				Mark.say(readingMode);
				onlyButton(buttons.get(i));
				currentHistory=histories.get(i);
				this.revalidate();
				this.repaint();
				// read the story among the six
		        for(int j =0; j<storyNames.length;j++) {
					if (selectedStory.equals(storyNames[j])) {
						String storyFile = Z.storiesPath + storyNames[j] + ".txt";
						printStories.get(i).setText(Z.listToStory(new ArrayList(Z.getStoryText(storyFile))));
						readStory(storyNames[j]);
					}
				}
	        }
		}
    }
	
	
	// ------------------
	//
	//     tools
	//
	// ------------------
	
	public void initiateBooks() {
		List<String> results = new ArrayList<String>();

		// read files in folder so we know the size
		File[] files = new File(Z.storiesPath).listFiles();
		for (File file : files) {
		    if(file.isFile()) {
	    		String name = file.getName();
	    		if (name.endsWith(".txt")) {
	    			results.add(name.substring(0, name.indexOf(".txt")));
	    		}
		    }
		}
		
		// remake menu files
		String original = storyNames[0];
		String[] newNames = new String[results.size()+1];
		newNames[0] = original;
		for (int i=1;i<=results.size();i++) {
			newNames[i] = results.get(i-1);
		}
		Arrays.sort(newNames);
		storyNames = newNames;
		
		// load menu files
		for(JComboBox box : combos){
			box.removeItem(box.getItemAt(0));
			for(String name: newNames) {
				box.addItem(name);
			}
		}
	}
	void updateInterface() {
		this.revalidate();
		this.repaint();
	}
	
	void resetButtons() {
		for(int i = 0; i< buttons.size();i++) {
			JButton button = buttons.get(i);
			buttons.get(i).setText(buttonNames[i]);
			if(haveReadStories.contains(button)) {
				buttons.get(i).setEnabled(false);
			} else {
				buttons.get(i).setEnabled(true);
				combos.get(i).setEnabled(true);
			}
		}
		if(haveReadStories.size()>=2) {
			getCommon.setEnabled(true);
		} else {
			getCommon.setEnabled(false);
		}
		sentenceCount = 0;
		sentences = new ArrayList<>();
	}
	
	void onlyButton(JButton button2) {
		for(int i=0;i<buttons.size();i++) {
			JButton button = buttons.get(i);
			if(button!=button2) {
				button.setEnabled(false);
				combos.get(i).setEnabled(false);
			} else {
				button.setEnabled(true);
			}
		}
	}

	void printSentence(String sentence) {
		ZPage.appendToPane(definition,  "\n  ------------------- "+(++sentenceCount)+" ------------------- \n  ");
		ZPage.appendToPane(definition,  "\n   English:\n\n       "+sentence);
		ZPage.appendToPane(definition,  "\n\n\n   Semantics:\n"+Z.getPrettyTree(sentence, 6));
		String rGoals = RGoal.Event2State2(sentence);
		ZPage.appendToPane(definition,  "\n\n   State Changes:\n\n       " + rGoals.replace("\n", "\n"+"       ")+"\n");
		ZPage.appendToPane(currentHistory, rGoals, ZPage.smallFontSize);
		word.setText("");
		RGoal.NofRGoalsReported = RGoal.NofRGoals;
		
		if (sentenceCount==sentences.size()) {
			ZPage.appendToPane(definition,  "\n  ------------------- the end ------------------- \n  ");
			
			rGoalLists.add(RGoal.rGoals);
			haveReadStories.add(readingMode);
			readingMode = null;
			
			resetButtons();
			RGoal.initialize();
		}
	}
	
	void readStory(String fileName) {
		definition.setText("");
		sentenceCount = 0;
		Map<String, List<String>> returns = Z.readStoryFromFile(fileName);
		sentences = returns.get(Z.SENTENCES);
		List<String> assignments = returns.get(Z.ASSIGNMENTS);
		printSentence(sentences.get(sentenceCount));
	}
	
	public static void main(String[] ignore) {
		PageStoryLearner page = new PageStoryLearner();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(page);
		frame.setBounds(0, 0, 1400, 1000);
		frame.setVisible(true);
	}
	
}
