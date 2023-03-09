package zhutianYang;

import java.awt.*;
import java.awt.event.*;
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
import connections.AbstractWiredBox;
import connections.Connections;
import connections.TextEntryBox;
import connections.WiredBox;
import connections.signals.BetterSignal;
import constants.Markers;
import dictionary.WordNet;
import frames.entities.Bundle;

/*
 * Created on Oct 6, 2018 Zhutian Yang
 */

public class PageNoviceLearner extends JPanel implements WiredBox{
	
	JPanel headerPane = new JPanel();
	JPanel conversationPane = new JPanel();
	JTextPane conversation = new JTextPane();
	JTextArea input = new JTextArea();
	
	JPanel sidebar = new JPanel();
	
	// short term memories
	JPanel shortTermMemoryPane = new JPanel();
	JPanel shortTermMemoryContent = new JPanel();
	JLabel shortTermMemoryLabel = new JLabel(ZPage.shortTermMemory);
	
	JPanel classificationsPane = new JPanel();
	JTextPane classifications = new JTextPane();
	JLabel classificationsLabel = new JLabel(ZPage.InformationExtracted);
	
	JPanel questionsPane = new JPanel();
	JTextPane questions = new JTextPane();
	JLabel questionsLabel = new JLabel(ZPage.skillsUnknown);
	
	// long term memories
	JPanel longTermMemoryPane = new JPanel();
	JPanel longTermMemoryContent = new JPanel();
	JLabel longTermMemoryLabel = new JLabel(ZPage.longTermMempry);
	
	JPanel microstoriesPane = new JPanel();
	JTextPane microstories = new JTextPane();
	JLabel microstoriesLabel = new JLabel(ZPage.knowledgeLearned);
	JButton microstoriesDelete = new JButton(ZPage.deleteMemory);
	
	JPanel memoryPane = new JPanel();
	JTextPane memory = new JTextPane();
	JLabel memoryLabel = new JLabel(ZPage.allKnowledgeRemembered);
	JButton memoryDelete = new JButton(ZPage.forgetAllSkills);
	
	Boolean first = true;
	String text = "";
	Boolean printNext = true;
	
	
	public static final String TO_RECIPE_EXPERT = "from recipe expert";
	public static final String FROM_RECIPE_EXPERT = "from recipe expert";
	public static final String FROM_FILE_SOURCE = "from file source";
	
	public PageNoviceLearner() {
		super();
		this.setLayout(new BorderLayout());
		
		// the conversation and input on the left
		this.add(conversationPane);
		conversationPane.setLayout(new BorderLayout());
		
		conversationPane.add(headerPane, BorderLayout.NORTH);
		headerPane.add(ZPage.headerNoviceLearner);
		headerPane.setPreferredSize(ZPage.headerSize);
		headerPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		
		conversationPane.add(conversation);
		conversationPane.add(new JScrollPane(conversation), BorderLayout.CENTER);
		conversation.setContentType("text/html");
		conversation.setPreferredSize(new Dimension(0, 600));
		conversation.setFont(new Font(ZPage.defaultFontName, Font.PLAIN, ZPage.largeFontSize));
		conversation.setBorder(ZPage.defaultBorder);
		
		conversationPane.add(input, BorderLayout.SOUTH);
		input.addKeyListener(new MyInputListener());
		input.setBorder(ZPage.defaultBorder);
		input.setLineWrap(true);
		input.setPreferredSize(new Dimension(600, 100));
		input.setFont(new Font(ZPage.defaultFontName, Font.PLAIN, ZPage.largeFontSize));
		input.setText(ZPage.defaultInputText);
		input.addMouseListener(new MouseListener());
		
		
		// the long and short term memories on the right
		this.add(sidebar, BorderLayout.EAST);
		sidebar.setLayout(new BorderLayout());
		sidebar.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, 0));
		sidebar.add(shortTermMemoryPane, BorderLayout.NORTH);
		shortTermMemoryPane.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, 2*ZPage.smallPaneHeight));
		sidebar.add(longTermMemoryPane);
		
		// ---------------------------------------------------------
		shortTermMemoryPane.setLayout(new BorderLayout());
		shortTermMemoryPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		shortTermMemoryPane.add(shortTermMemoryLabel, BorderLayout.NORTH);
		ZPage.makeLargeLabel(shortTermMemoryLabel);
		
		shortTermMemoryPane.add(shortTermMemoryContent);
		shortTermMemoryContent.setLayout(new GridLayout(2,1));
		shortTermMemoryContent.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
		
		// for extracting information from human English
		shortTermMemoryContent.add(classificationsPane);
		classificationsPane.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		classificationsPane.setLayout(new BorderLayout());
		
		classificationsPane.add(classificationsLabel, BorderLayout.NORTH);
		ZPage.makeSmallLabel(classificationsLabel);
		
		classificationsPane.add(classifications);
		classificationsPane.add(new JScrollPane(classifications));
		classifications.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		
		// for listing out questions to ask
		shortTermMemoryContent.add(questionsPane);
		questionsPane.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		questionsPane.setLayout(new BorderLayout());
		
		questionsPane.add(questionsLabel, BorderLayout.NORTH);
		ZPage.makeSmallLabel(questionsLabel);
		
		questionsPane.add(questions);
		questionsPane.add(new JScrollPane(questions));
		questions.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		
		
		// ---------------------------------------------------------
		longTermMemoryPane.setLayout(new BorderLayout());
		longTermMemoryPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		longTermMemoryPane.add(longTermMemoryLabel, BorderLayout.NORTH);
		ZPage.makeLargeLabel(longTermMemoryLabel);
		
		longTermMemoryPane.add(longTermMemoryContent);
		longTermMemoryContent.setLayout(new BorderLayout());
		longTermMemoryContent.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
		
		// for listing out all microstories learned in this session
		longTermMemoryContent.add(microstoriesPane);
		microstoriesPane.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		microstoriesPane.setLayout(new BorderLayout());
		
		microstoriesPane.add(microstoriesLabel, BorderLayout.NORTH);
		ZPage.makeSmallLabel(microstoriesLabel);
		
		microstoriesPane.add(microstories);
		microstoriesPane.add(new JScrollPane(microstories));
		microstories.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		
		microstoriesPane.add(microstoriesDelete, BorderLayout.SOUTH);
		microstoriesDelete.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				Z.deleteFile(RecipeExpert.stepsFileName);
				Z.deleteFile(RecipeExpert.testFileName);
				microstories.setText("");
			}
		});
		
		// for listing out all past knowledge registered
		longTermMemoryContent.add(memoryPane, BorderLayout.SOUTH);
		memoryPane.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		memoryPane.setLayout(new BorderLayout());
		
		memoryPane.add(memoryLabel, BorderLayout.NORTH);
		ZPage.makeSmallLabel(memoryLabel);
		
		memoryPane.add(memory);
		memoryPane.add(new JScrollPane(memory));
		memory.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		memory.setFont(new Font(ZPage.defaultFontName, Font.PLAIN, ZPage.smallFontSize));
		
		memoryPane.add(memoryDelete, BorderLayout.SOUTH);
		memoryDelete.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				try {
					FileWriter writer = new FileWriter(RecipeExpert.knowledgeMapFile,false);
					writer.close();
					memory.setText("");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		
		Connections.getPorts(this).addSignalProcessor(FROM_RECIPE_EXPERT, this::printResponse);
		Connections.getPorts(this).addSignalProcessor(FROM_FILE_SOURCE, this::followScript);

	}
	
	private void scrollToEnd() {
		conversation.selectAll();
		int x = conversation.getSelectionEnd();
		conversation.select(x, x);
		
		questions.selectAll();
		x = questions.getSelectionEnd();
		questions.select(x, x);
		
		microstories.selectAll();
		x = microstories.getSelectionEnd();
		microstories.select(x, x);
		
		memory.selectAll();
		x = memory.getSelectionEnd();
		memory.select(x, x);
	}
	
	class MouseListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			Mark.say(input.getText().trim());
			if (input.getText().trim().equals(ZPage.defaultInputText)) {
				input.setText("");
			}
		}
	}
	
	public void followScript(Object o) {
		if (o instanceof String) {
			String string = (String) o;
			if(string.startsWith(Z.INSERT_CONVERSATION)) {
				string = string.substring(string.indexOf(":")+2);
				input.setText(string);
				getResponse(string);
			}
		}
	}
	
	public void printResponse(Object o) {
		if (o instanceof BetterSignal) {
			
			// print genesis responses in conversation panel
			BetterSignal input = (BetterSignal) o;
			String contents = (String) input.get(1, String.class);
			if(contents.length()>5) {
				text += contents;
				contents = ZPage.header + Html.normal(text) + ZPage.trailer;
				String stuff = Html.convertLf(contents);
				stuff = stuff.replace("font-size: 25.0px", "font-size: "+ZPage.largeFontSize+"px");
				
				conversation.setText(stuff);
				questions.setText(Z.printList(RecipeExpert.questions));
				microstories.setText(Z.printTXTFile(RecipeExpert.stepsFileName));
				memory.setText(Z.printTXTFile(RecipeExpert.knowledgeMapFile));
				scrollToEnd();
			}
			
			// print human intention in classifications panel
			Map<String, Object> updates = (HashMap<String, Object>) input.get(2, HashMap.class);
			String toPrint = "";
			for (String content: updates.keySet()) {
				toPrint += "   [ "+content+" ]\n";
				toPrint += updates.get(content)+" \n";
			}
			classifications.setText(toPrint);
			
			return;
		}
	}
	
	public void getResponse(String string) {
		LocalGenesis.setRadioButtonsUsingIdiom("Set expert teaches novice button to true");
		Connections.getPorts(this).transmit(TO_RECIPE_EXPERT, new BetterSignal(string));
		input.setText("");
		printNext = false;
	}
	
	class MyInputListener extends KeyAdapter {
		String terminators = "\n";
		
		public void keyTyped(KeyEvent event) {
			
			char key = event.getKeyChar();
			if (terminators.indexOf(key) >= 0) {
				
				// take in input and clear text entry field
				String sentence = input.getText().trim();
				getResponse(sentence);
			}
		}
	}
	
	public static void main(String[] ignore) {
		PageNoviceLearner page = new PageNoviceLearner();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(page);
		frame.setBounds(ZPage.windowSize);
		frame.setVisible(true);
	}

}
