package zhutianYang;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import utils.Html;
import translator.Translator;
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

public class PageHowToLearner extends JPanel implements ActionListener {

	public static String pathHowToBooks = "corpora/zHowToLibrary/HowToBooks/";
	public static String pathWikiHowArticles = "corpora/zHowToLibrary/wikiHow/";
	public static List<String> paths = Arrays.asList(pathHowToBooks, pathWikiHowArticles);

	public static String goal = "";  // original = current + repair
	public static List<String> sentencesOriginal = new ArrayList<>();
	public static List<String> sentencesOriginalPrint = new ArrayList<>();
	public static List<String> sentencesCurrent = new ArrayList<>();
	public static List<String> sentencesCurrentPrint = new ArrayList<>();
	public static List<String> sentencesCurrentInnerese = new ArrayList<>();
	public static List<String> sentencesToRepair = new ArrayList<>();
	public static List<String> sentencesToRepairSuggestions = new ArrayList<>();
	public static List<List<String>> sentencesLists = Arrays.asList(sentencesOriginal, sentencesOriginalPrint,
			sentencesCurrent, sentencesCurrentPrint, sentencesCurrentInnerese, sentencesToRepair, sentencesToRepairSuggestions);
	public static int repairIndex = -1;
	public static String stepsFileName = "";
	public static String testFileName = "";

	int topHight = 182; //210;
	Translator t = Translator.getTranslator();
	Boolean initializedButton = false;
	String toRepair = "<font color=\"#bdc3c7\"><b>( /// )</b></font>";
	String z = "///";

	// header + library + playground
	JPanel left = new JPanel();
	JPanel top = new JPanel(); // = header + library
	JPanel header = new JPanel();
	JPanel onlineBox = new JPanel();
	JPanel libraryBox = new JPanel();
	JPanel library = new JPanel();
	JButton selectFiles = new JButton("Select file");
	Boolean haveInitialized = false;

	// books
	static String[] bookNames = {"Select books ..."};
	static JComboBox books = new JComboBox(bookNames);
	JButton booksButton = new JButton("Select books ...");
	JPanel booksBox = new JPanel();

	// wikiHows
	static String[] wikiHowNames = {"Select wikiHow articles ...",};
	static JComboBox wikiHows = new JComboBox(wikiHowNames);
	JButton wikiHowsButton = new JButton("Select wikiHow articles ...");
	JPanel wikiHowsBox = new JPanel();

	static List<String[]> names = Arrays.asList(bookNames, wikiHowNames);
	static List<JComboBox> combos = Arrays.asList(books, wikiHows);


//	"How to write a story - by Earnest Hemingway",
//	"More books ...",
//	"How to form a habit - by Charles Duhigg",
//	"How to achieve the most out of life - by Ray Dalio"

	// editors
	JPanel playground = new JPanel();
	JPanel playgroundWindows = new JPanel();

	JPanel original = new JPanel();
	JTextPane originalEditor = new JTextPane();
	String originalDefault = "Input your stories here ...";

	JPanel repaired = new JPanel();
	JButton buttonRepair = new JButton(new ImageIcon("students/zhutianYang/IconRightHand.png"));

	JTextPane repairedEditor = new JTextPane();
	JButton buttonGenerate = new JButton(new ImageIcon("students/zhutianYang/IconRightHand.png"));


	// modify individual sentences
	JPanel rewriteWindows = new JPanel();

	JPanel rewrite = new JPanel();
	JTextPane rewriteEditor = new JTextPane();
	JButton buttonTranslate = new JButton(new ImageIcon("students/zhutianYang/IconRightHand.png"));
	JButton rewriteRetry = new JButton(new ImageIcon("students/zhutianYang/IconCheck.png"));
	JButton rewriteDelete = new JButton(new ImageIcon("students/zhutianYang/IconCross.png"));
	JTextArea rewriteSuggestions = new JTextArea();

	String step1 = "Step 1: Select a book, or a wikiHow article, "
					+ "\n   or paste your paragraph here.";
	String step2 = "Step 2: Genesis will auto-repair the sentences, "
					+ "\n   leaving difficult ones to your revision.";
	String step3 = "Step 3: For each sentence, its Innerese will appear here."
					+ "\n   You can either accept its translation, "
					+ "\n   or modify it on the right panel,"
					+ "\n   or delete it from the article.";
	String step4 = "Step 4: Modify the sentences here. ";
	String step5 = "Step 5: After you have corrected all the sentences, "
			+ "\n   the generated How-To knowledge will appear here.";

	String innereseDefault = "START cannot translate this sentence, "
			+ "\n   please help rewrite it on the right panel."
			+ "\n\nClick on \"check\" to try translation again."
			+ "\nClick on \"cross\" to delete this sentence.";

	String suggestions = "Genesis will provide suggestions here to help your revision";


	JPanel preview = new JPanel();
	JPanel previewEditorBox = new JPanel();
	JPanel previewEditorButtons = new JPanel();
	JTextPane previewEditor = new JTextPane();
	List<JButton> buttons = new ArrayList<>();
	JButton buttonLeft = new JButton(new ImageIcon("students/zhutianYang/IconLeftArrow.png"));
	JButton buttonRight = new JButton(new ImageIcon("students/zhutianYang/IconRightArrow.png"));

	// long term + short term memories
	JPanel right = new JPanel();
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

	public void initiateBooks() {

		if(!haveInitialized) {
			// for both how to book and wikihow menus
			for(int j=0; j<=1; j++) {

				// read files in folder so we know the size
				List<String> results = new ArrayList<String>();
				File[] files = new File(paths.get(j)).listFiles();
//				Mark.say(paths.get(j));
				for (File file : files) {
				    if(file.isFile()) {
			    		String name = file.getName();
			    		if (name.endsWith(".txt")) {
			    			results.add(name.substring(0, name.indexOf(".txt")));
			    		}
				    }
				}

				// remake menu files
				String original = names.get(j)[0];
				String[] newNames = new String[results.size()+1];
				newNames[0] = original;
				for (int i=1;i<=results.size();i++) {
					newNames[i] = results.get(i-1);
				}

				JComboBox box = combos.get(j);
				box.removeItem(box.getItemAt(0));
				for(String name: newNames) {
					box.addItem(name);
				}
			}
			refresh();
			haveInitialized = true;
		}

	}

	public PageHowToLearner() {
		super();
//		initiateBooks();
		this.setLayout(new BorderLayout());

		// ================================================
		// the long and short term memories on the right
		// ================================================
		this.add(left);
		left.setLayout(new BorderLayout());

		// -------------------------------// -------------------------------


		// -------------------------------// -------------------------------
		// editor panels
		left.add(playground);
		playground.setPreferredSize(new Dimension(1000,1000));
		playground.setLayout(new BorderLayout());
		playground.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		ZPage.addLargeLabel(playground, "Playground     ");

		playground.add(playgroundWindows, BorderLayout.CENTER);
		playgroundWindows.setBackground(Color.RED);
		playgroundWindows.setPreferredSize(new Dimension(1000,1000));
		playgroundWindows.setLayout(new GridLayout(1,2));


		// ================================================
		// Step 1: display the original articles
		// ================================================
		playgroundWindows.add(original);
		original.setLayout(new BorderLayout());

		JPanel originalEditorBox = new JPanel();
		original.add(originalEditorBox);
		originalEditorBox.setLayout(new BorderLayout());
		originalEditorBox.setBorder(ZPage.defaultBorder);
		ZPage.addSmallLabel(originalEditorBox, "Step 1: Original Text", ZPage.CENTER);
		originalEditor = ZPage.addTextPane(originalEditorBox, ZPage.FONTMEDIAN);
		originalEditor.setContentType("text/html");
		originalEditor.setText(ZPage.makeComment(step1));
//		originalEditor.addMouseListener(new MouseAdapter() {
//			public void mouseClicked(MouseEvent e) {
//				initiateBooks();
//			}
//		});

//		original.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 5));

		JPanel buttonOriginalBox = new JPanel();
		buttonOriginalBox.setPreferredSize(new Dimension(40,40));
		buttonOriginalBox.setLayout(new GridLayout(9,1));
		for(int i=0; i<4; i++) buttonOriginalBox.add(new JPanel());
		buttonOriginalBox.add(buttonRepair);
		original.add(buttonOriginalBox, BorderLayout.EAST);
		buttonRepair.setBorder(BorderFactory.createBevelBorder(0));
		buttonRepair.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				updateRewritingComponents();
			}
		});


		// ================================================
		// step 2: Genesis-repaired article
		// ================================================
		playgroundWindows.add(repaired);
		repaired.setLayout(new BorderLayout());

		JPanel repairedEditorBox = new JPanel();
		repaired.add(repairedEditorBox);
		repairedEditorBox.setLayout(new BorderLayout());
		repairedEditorBox.setBorder(ZPage.defaultBorder);
		ZPage.addSmallLabel(repairedEditorBox, "Step 2: Genesis Repaired Text", ZPage.CENTER);
		repairedEditor = ZPage.addTextPane(repairedEditorBox, ZPage.FONTMEDIAN);
		repairedEditor.setContentType("text/html");
		repairedEditor.setText(ZPage.makeComment(step2));

		JPanel buttonSimplifyBox = new JPanel();
		buttonSimplifyBox.setPreferredSize(new Dimension(40,40));
		buttonSimplifyBox.setLayout(new GridLayout(9,1));
		for(int i=0; i<4; i++) buttonSimplifyBox.add(new JPanel());
		buttonSimplifyBox.add(buttonGenerate);
		repaired.add(buttonSimplifyBox, BorderLayout.EAST);
		buttonGenerate.setEnabled(false);
		buttonGenerate.setBorder(BorderFactory.createBevelBorder(0));
		buttonGenerate.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
				List<String> sentencesCurrentPrintNew = new ArrayList<>();
				for(String sentence: sentencesCurrentPrint) {
					sentencesCurrentPrintNew.add(sentence.replace("<font color=\"#2ecc71\"><b>", "").replace("</b></font>", ""));
				}
				RecipeLearner.instructionsToMicroStories(sentencesCurrentPrintNew, date, "").get(0);
//				stepsFileName = RecipeLearner.stepsFileName;
//				testFileName = RecipeLearner.testFileName;
				microstories.setText(Z.printTXTFile(RecipeLearner.stepsFileName));
				memory.setText(Z.printList(ZPage.beautifyMemory(Z.getStoryText(RecipeExpert.knowledgeMapFile))));
			}
		});

		// ================================================
		// Step 3: Check Innerese
		// ================================================
		// south pane for rewriting
		//    rewriteWindows = preview + rewrite
		playground.add(rewriteWindows, BorderLayout.SOUTH);
		rewriteWindows.setPreferredSize(new Dimension(1000,200));
		rewriteWindows.setLayout(new GridLayout(1,2));

		// --- rewriteWindows = previewEditorBox + preview
		// preview = previewEditorBox (+ buttons)
		rewriteWindows.add(preview);
		preview.setLayout(new BorderLayout());
		preview.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 10));
		ZPage.addSmallLabel(preview, "Step 3: Check Innerese", ZPage.CENTER);

		preview.add(previewEditorBox);
		previewEditorBox.setLayout(new BorderLayout());
		previewEditorBox.add(previewEditorButtons, BorderLayout.NORTH);
		previewEditor = ZPage.addTextPane(previewEditorBox, ZPage.FONTMEDIAN);
		previewEditor.setContentType("text/html");
		previewEditor.setText(ZPage.makeComment(step3));

		// rewriteBox = rewrite + rewriteSuggestion
		///             rewrite = rewriteEditorBox + buttonRewriteBox
		JPanel rewriteBox = new JPanel();
		rewriteWindows.add(rewriteBox);
		rewriteBox.setLayout(new BorderLayout());
		rewriteBox.add(rewrite);
		rewrite.setLayout(new BorderLayout());
		

		JPanel rewriteEditorBox = new JPanel();
		rewrite.add(rewriteEditorBox);
		rewriteEditorBox.setLayout(new BorderLayout());
		rewriteEditorBox.setBorder(ZPage.defaultBorder);
		ZPage.addSmallLabel(rewriteEditorBox, "Step 4: Sentence Revision", ZPage.CENTER);
		rewriteEditor = ZPage.addTextPane(rewriteEditorBox, ZPage.FONTMEDIAN);
//		rewriteEditor.setContentType("text/html");
		rewriteEditor.setText(step4);

		JPanel buttonRewriteBox = new JPanel();
		buttonRewriteBox.setPreferredSize(new Dimension(40,40));
		buttonRewriteBox.setLayout(new GridLayout(5,1));
		buttonRewriteBox.add(new JPanel());
		rewrite.add(buttonRewriteBox, BorderLayout.EAST);

//		JPanel rewriteButtons = new JPanel();
//		rewriteButtons.setLayout(new GridLayout(1,3));
//		rewriteButtons.add(rewriteNext);
//		rewriteButtons.add(rewriteAccept);
//		rewriteButtons.add(rewriteDelete);
//		rewriteEditorBox.add(rewriteButtons, BorderLayout.SOUTH);

		rewriteBox.add(rewriteSuggestions, BorderLayout.SOUTH);
		rewriteSuggestions.setPreferredSize(new Dimension(1000,200));
		rewriteSuggestions.setText(suggestions);
		rewriteSuggestions.setFont(ZPage.medianFont);
//		rewriteSuggestions.setVerticalAlignment(JLabel.TOP);
		rewriteSuggestions.setBorder(BorderFactory.createEmptyBorder(10, 5, 0, 0));
		rewriteSuggestions.setBackground(Z.UI_GREY);

		// header + library
		left.add(top,BorderLayout.NORTH);

		top.setLayout(new BorderLayout());
		
		
//		top.add(header, BorderLayout.NORTH);
//		top.setPreferredSize(new Dimension(1000, topHight));
//		header.setPreferredSize(ZPage.headerSizeSmall);
//		header.add(ZPage.headerHowToLearner);
		
		
		top.add(onlineBox, BorderLayout.SOUTH);
		onlineBox.setLayout(new BorderLayout());
		ZPage.addLargeLabel(onlineBox, "Online     ");
		JPanel searchBox = new JPanel();
		searchBox.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
		
		JTextPane searchEditor = new JTextPane();
		searchEditor = ZPage.addTextPane(searchBox, ZPage.FONTMEDIAN);
		searchEditor.setText("Input keyword");
		searchEditor.setPreferredSize(new Dimension(400, 25));
		onlineBox.add(searchBox, BorderLayout.WEST);
		
		
		top.add(libraryBox, BorderLayout.NORTH);
		libraryBox.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));
		libraryBox.setLayout(new BorderLayout());
		ZPage.addLargeLabel(libraryBox, "Library     ");

		libraryBox.add(library);
		library.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 40));
		library.setLayout(new BorderLayout(1,3));
		library.setPreferredSize(new Dimension(100,95));

		JPanel box1 = new JPanel();
		selectFiles.addActionListener(this);
		selectFiles.setPreferredSize(new Dimension(110, 25));
		box1.add(selectFiles);
		library.add(box1, BorderLayout.WEST);

		library.add(booksBox, BorderLayout.CENTER);
		booksBox.add(booksButton);
		booksButton.setPreferredSize(new Dimension(400, 25));
		booksButton.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				initiateBooks();
				booksBox.remove(booksButton);
				booksBox.add(books);
				books.showPopup();

				wikiHowsBox.remove(wikiHowsButton);
				wikiHowsBox.add(wikiHows);
			}
		});
		books.addActionListener(this);

		library.add(wikiHowsBox, BorderLayout.EAST);
		wikiHowsBox.add(wikiHowsButton);
		wikiHowsButton.setPreferredSize(new Dimension(500, 25));
		wikiHowsButton.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				initiateBooks();
				booksBox.remove(booksButton);
				booksBox.add(books);

				wikiHowsBox.remove(wikiHowsButton);
				wikiHowsBox.add(wikiHows);
				wikiHows.showPopup();
			}
		});
		wikiHows.addActionListener(this);


//		for(JComboBox box: combos) {
//			if(box==books) {
//				library.add(box, BorderLayout.CENTER);
//			} else {
//				library.add(box, BorderLayout.EAST);
//				box.setPreferredSize(new Dimension(500, 30));
//			}
//			box.addActionListener(this);
////			box.addMouseListener(new MouseAdapter() {
////				public void mouseClicked(MouseEvent e) {
////					initiateBooks();
////				}
////			});
//			box.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
//		}

		// -------------------------------
//				library.add(libraryBooks);
//				libraryBooks.setBorder(BorderFactory.createEmptyBorder(0, 30, 30, 10));
//				libraryBooks.setLayout(new BorderLayout());
//				ZPage.addSmallLabel(libraryBooks, "Books", ZPage.CENTER);

//				libraryBooks.add(libraryBooksOptions);
//				libraryBooksOptions.setLayout(new GridLayout(4,1));
//				for(int i = 0; i<bookButtons.size();i++) {
//					bookButtons.get(i).setText(bookNames[i]);
//					libraryBooksOptions.add(bookButtons.get(i));
//					bookButtons.get(i).addMouseListener(new MouseAdapter() {
//						public void mouseReleased(MouseEvent e) {
//							Object src = e.getSource();
//							for(int i=0;i<bookButtons.size();i++) {
//								if (src == bookButtons.get(i)) {
//									originalEditor.setText(Z.getStoryText(libraryPath+bookNames[i]+".txt"));
//									if(bookNames[i].contains(" - ")) {
//										goal = bookNames[i].substring(0,bookNames[i].indexOf(" -"));
//									}
//							    }
//							}
//						}
//					});
//				}
//				libraryBooksOptions.add(books);

//				// -------------------------------
//				library.add(libraryWikiHows);

		// ================================================
		// the long and short term memories on the right
		// ================================================
		this.add(right, BorderLayout.EAST);
		right.setLayout(new BorderLayout());
		right.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, 0));
		right.add(longTermMemoryPane);

		// ---------------------------------------------------------
		longTermMemoryPane.setLayout(new BorderLayout());
		longTermMemoryPane.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
		longTermMemoryPane.add(longTermMemoryLabel, BorderLayout.NORTH);
		ZPage.makeLargeLabel(longTermMemoryLabel);

		longTermMemoryPane.add(longTermMemoryContent);
		longTermMemoryContent.setLayout(new BorderLayout());
		longTermMemoryContent.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

		// for listing out all past knowledge registered
		longTermMemoryContent.add(memoryPane, BorderLayout.NORTH);
		memoryPane.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, topHight));
		memoryPane.setLayout(new BorderLayout());
		memoryPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 20, 0));
		memoryPane.add(memoryLabel, BorderLayout.NORTH);
		ZPage.makeSmallLabel(memoryLabel, ZPage.CENTER);

		memoryPane.add(memory);
		memoryPane.add(new JScrollPane(memory));
		memory.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		memory.setFont(new Font(ZPage.defaultFontName, Font.PLAIN, ZPage.smallFontSize));
		memory.setContentType("text/html");

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

		// for listing out all microstories learned in this session
		longTermMemoryContent.add(microstoriesPane);
		microstoriesPane.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		microstoriesPane.setLayout(new BorderLayout());

		microstoriesPane.add(microstoriesLabel, BorderLayout.NORTH);
		ZPage.makeSmallLabel(microstoriesLabel, ZPage.CENTER);

		microstoriesPane.add(microstories);
		microstoriesPane.add(new JScrollPane(microstories));
		microstories.setPreferredSize(new Dimension(ZPage.singleSidebarPaneWidth, ZPage.smallPaneHeight));
		microstories.setText(step5);
		microstories.setFont(ZPage.smallFont);

		microstoriesPane.add(microstoriesDelete, BorderLayout.SOUTH);
		microstoriesDelete.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				Z.deleteFile(RecipeLearner.stepsFileName);
				Z.deleteFile(RecipeLearner.testFileName);
				microstories.setText("");
			}
		});

//		initiateBooks();
	}

	void updateRewritingComponents() {
		for(int i = 0; i<sentencesOriginal.size();i++) {
			String sentence = sentencesOriginal.get(i);
			if(Z.isTranslatable(sentence)) {
				sentencesCurrent.add(sentence);
				sentencesCurrentPrint.add(sentence);
				sentencesCurrentInnerese.add(Z.getInnerese(sentence));
				sentencesToRepairSuggestions.add("");
			} else {
				Boolean needsRepair = false;
				List<String> repaired = Z.repairSentence(sentence, i);
				if(repaired.size()==2) {
					if (Z.isTranslatable(repaired.get(0))){
						sentencesCurrent.add(repaired.get(0));
						sentencesCurrentPrint.add(repaired.get(1));
						sentencesCurrentInnerese.add(Z.getInnerese(repaired.get(0)));
						sentencesToRepairSuggestions.add("");
					} else {
						needsRepair = true;
					}
				} else {
					needsRepair = true;
				}
				if(needsRepair) {
					sentencesToRepair.add(sentence);
					sentencesToRepairSuggestions.add(Z.getRepairSuggestions(sentence));
					sentencesCurrent.add(sentence);
					sentencesCurrentPrint.add(toRepair.replace(z, sentence));
					sentencesCurrentInnerese.add(innereseDefault);
				}
			}
			repairedEditor.setText(ZPage.makeHTML(Z.printList(sentencesCurrentPrint,1)));
			originalEditor.setText(ZPage.makeHTML(Z.printList(sentencesOriginalPrint,1)));
		}
		if(sentencesToRepair.size()==0) buttonGenerate.setEnabled(true);
		for(int i=0;i<sentencesCurrent.size();i++) {
			repairIndex = 0;
			if(sentencesToRepair.contains(sentencesCurrent.get(i))) {
				repairIndex = i;
				break;
			}
		}
		updateRewriteEditors(repairIndex);
		updateButtons();
	}

	void updateButtons() {
		if (!initializedButton) {
			previewEditorBox.remove(previewEditorButtons);
			buttons = new ArrayList<>();
			previewEditorButtons = new JPanel();
			previewEditorBox.add(previewEditorButtons, BorderLayout.NORTH);
			previewEditorButtons.setPreferredSize(new Dimension(1000, 30));
			previewEditorButtons.setLayout(new GridLayout(1, sentencesCurrent.size()+2));
			for(int i=0;i<sentencesCurrent.size();i++) {
				JButton button = new JButton(i+1+"");
				buttons.add(button);
				previewEditorButtons.add(button);
				button.addActionListener(new MyActionListener());
			}

			buttonLeft = new JButton(new ImageIcon("students/zhutianYang/IconLeftArrow.png"));
			buttonRight = new JButton(new ImageIcon("students/zhutianYang/IconRightArrow.png"));
			rewriteRetry = new JButton(new ImageIcon("students/zhutianYang/IconCheck.png"));
			rewriteDelete = new JButton(new ImageIcon("students/zhutianYang/IconCross.png"));

			rewriteRetry.addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {

					// take in and update lists
					String newString = rewriteEditor.getText().replace("\n", "");
					List<String> shorterStrings = Z.story2Sentences(newString);
					Mark.say(shorterStrings);
					int size = shorterStrings.size();
					if(size>1) {
						for(int i=size-1; i>0; i--) sentencesOriginal.add("");
						for(int i=sentencesOriginal.size()-1; i>repairIndex; i--) {
							sentencesOriginal.set(i, sentencesOriginal.get(i-size+1));
						}
					}

					for(int i=0; i<size; i++) {
						sentencesOriginal.set(repairIndex+i, shorterStrings.get(i));
					}
					sentencesOriginalPrint = new ArrayList<>(sentencesOriginal);
					reset();
					updateRewritingComponents();
					updateButtons();
				}
			});

			rewriteDelete.addMouseListener(new MouseAdapter() {
				public void mouseReleased(MouseEvent e) {
					sentencesOriginal.remove(sentencesOriginal.get(repairIndex));
					sentencesOriginalPrint = new ArrayList<>(sentencesOriginal);
					reset();
					updateRewritingComponents();
					updateButtons();
				}
			});

			previewEditorButtons.add(buttonLeft);
			previewEditorButtons.add(buttonRight);
			previewEditorButtons.add(rewriteDelete);
			previewEditorButtons.add(rewriteRetry);

			buttonLeft.setBorder(BorderFactory.createBevelBorder(0));
			buttonRight.setBorder(BorderFactory.createBevelBorder(0));
			rewriteDelete.setBorder(BorderFactory.createBevelBorder(0));
			rewriteRetry.setBorder(BorderFactory.createBevelBorder(0));

			buttonLeft.addActionListener(new MyActionListener());
			buttonRight.addActionListener(new MyActionListener());
			rewriteRetry.addActionListener(new MyActionListener());
			rewriteDelete.addActionListener(new MyActionListener());
			initializedButton = true;
		}

		for(int j=0;j<buttons.size();j++) {
			if(sentencesToRepair.contains(sentencesCurrent.get(j))) {
				buttons.get(j).setForeground(Color.WHITE);
				buttons.get(j).setBackground(Z.RED);
			}
		}
		if(sentencesToRepair.size()==0) buttonGenerate.setEnabled(true);

		buttons.get(repairIndex).setBorder(BorderFactory.createBevelBorder(1));
		if(sentencesCurrent.size()==1) {
			buttonLeft.setEnabled(false);
			buttonRight.setEnabled(false);
			buttonGenerate.setEnabled(true);
		} else if(repairIndex<=0) {
			buttonLeft.setEnabled(false);
			buttonRight.setEnabled(true);
		} else if (repairIndex>=sentencesCurrent.size()-1){
			buttonLeft.setEnabled(true);
			buttonRight.setEnabled(false);
		} else {
			Mark.mit(sentencesCurrent);
			buttonLeft.setEnabled(true);
			buttonRight.setEnabled(true);
		}
		refresh();
	}

	void updateRewriteEditors(int index){
		previewEditor.setText(ZPage.makeHTML(sentencesCurrentInnerese.get(repairIndex)));
		rewriteEditor.setText(sentencesCurrent.get(repairIndex));
		rewriteSuggestions.setText(sentencesToRepairSuggestions.get(repairIndex));
	}

	// ------------------
	//
	//     listeners
	//
	// ------------------
	class MyActionListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			JButton button = (JButton) e.getSource();
			if(buttons.contains(button) || button==buttonLeft || button==buttonRight) {
				if (button==buttonLeft) {
					repairIndex--;
				} else if (button==buttonRight) {
					repairIndex++;
				} else {
					repairIndex = Integer.parseInt(button.getText())-1;
				}
				for(JButton button1:buttons) {
					button1.setEnabled(true);
					button1.setBorder(BorderFactory.createBevelBorder(0));
				}
				buttons.get(repairIndex).setEnabled(false);
				buttons.get(repairIndex).setBorder(BorderFactory.createBevelBorder(1));

				updateRewriteEditors(repairIndex);
				updateButtons();
			}
		}
	}


	public void actionPerformed(ActionEvent e) {

//		initiateBooks();
		
		Mark.say(e.getSource());

		for(int i=0;i<combos.size();i++) {

			JComboBox cb = combos.get(i);
			String libraryPath = (i==0)? pathHowToBooks : pathWikiHowArticles;
			String[] articleNames = (i==0)? bookNames : wikiHowNames;
			String selectedStory = libraryPath+(String)cb.getSelectedItem()+".txt";
	        // initiate buttons among the four
	        if(!selectedStory.contains(articleNames[0])) {
	        	printOriginalStory(selectedStory);
	        }
		}
		if(e.getSource().equals(selectFiles)) {
			JFileChooser chooser;
			String choosertitle = "Select a file";
			int result;

		    chooser = new JFileChooser();
		    chooser.setCurrentDirectory(new java.io.File("."));
		    chooser.setDialogTitle(choosertitle);
//		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    chooser.setAcceptAllFileFilterUsed(false);
		    //
		    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
//		      System.out.println("getCurrentDirectory(): " +  chooser.getCurrentDirectory());
//		      System.out.println("getSelectedFile() : " +  chooser.getSelectedFile());
		    	String selectedFolder = chooser.getCurrentDirectory().toString();
		    	String selectedStory = chooser.getSelectedFile().toString();
		    	printOriginalStory(selectedStory);
		    } else {
		      Mark.say("No Selection ");
		    }
		}
	}

	void printOriginalStory(String selectedStoryPath) {
		List<String> lines = Z.getStoryText(selectedStoryPath);
    	if(lines!=null) {
    		reset();
    		sentencesOriginal = new ArrayList<>(lines);
    		sentencesOriginalPrint = new ArrayList<>(lines);
    		originalEditor.setText(ZPage.makeHTML(Z.listToStory(sentencesOriginalPrint)));
        	goal = selectedStoryPath.substring(selectedStoryPath.lastIndexOf("/"), selectedStoryPath.length());
			if(goal.contains(" - ")) goal = goal.substring(0,goal.indexOf(" -"));
    	}
	}

	public void reset() {
//		sentencesOriginal = new ArrayList<>();
//		sentencesOriginalPrint = new ArrayList<>();
		sentencesCurrent = new ArrayList<>();
		sentencesCurrentPrint = new ArrayList<>();
		sentencesCurrentInnerese = new ArrayList<>();
		sentencesToRepair = new ArrayList<>();
		sentencesToRepairSuggestions = new ArrayList<>();
		repairIndex = -1;
		stepsFileName = "";
		testFileName = "";
		originalEditor.setText(ZPage.makeHTML(""));
		rewriteSuggestions.setText("");
		initializedButton = false;
	}

	public void refresh() {
		this.revalidate();
		this.repaint();
	}

	public static void main(String[] ignore) {
		PageHowToLearner page = new PageHowToLearner();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(page);
		frame.setBounds(ZPage.windowSize);
		frame.setVisible(true);
	}

}
