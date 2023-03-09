/**
 * 
 */
package zhutianYang;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import utils.Html;
import utils.Z;


/**
 * @author rls
 *
 */
public class ZPage {
	
	public static Boolean TAKE_SCREENSHOT = true;
	public static String CENTER = "center";
	public static String BOTTOM = "bottom";
	public static String FONTLARGE = "large";
	public static String FONTMEDIAN = "median";
	public static String FONTSMALL = "small";

	// images
	public static JLabel headerHowToLearner = new JLabel(new ImageIcon("students/zhutianYang/Header How-to book learner_small.png"));
	public static JLabel headerNoviceLearner = new JLabel(new ImageIcon("students/zhutianYang/Header Novice learner.png"));
	public static JLabel headerStoryLearner = new JLabel(new ImageIcon("students/zhutianYang/Header Story learner.png"));
	
	// layout
	public static int singleSidebarPaneWidth = 350;
	public static int doubleSidebarPaneWidth = 600;
	public static int smallPaneHeight = 150;
	public static Rectangle windowSize = new Rectangle(0, 0, 1400, 1000);
	public static Dimension headerSize = new Dimension(10, 110);
	public static Dimension headerSizeSmall = new Dimension(10, 90);
	public static Border defaultBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
	
	// text
	public static String defaultFontName = "Palatino";
	public static int superFontSize = 30;
	public static Font superFont = new Font(defaultFontName, Font.PLAIN, superFontSize);
	public static int largeFontSize = 20;
	public static Font largeFont = new Font(defaultFontName, Font.PLAIN, largeFontSize);
	public static int medianFontSize = 16;
	public static Font medianFont = new Font(defaultFontName, Font.PLAIN, medianFontSize);
	public static int smallFontSize = 12;
	public static Font smallFont = new Font(defaultFontName, Font.PLAIN, smallFontSize);
	

	
	public static String headerGrey = "<font color=\"#"+Z.COLOR_GREY+"\">";
	public static String headerBlue = "<font color=\"#"+Z.COLOR_BLUE+"\">";
	public static String trailerComment = "</font>";
	public static String header = "<html><body>";
	public static String trailer = "</body></html>";
	public static String terminators = "?.!\n";
	

	// labels
	public static int labelHeight = 30;	
	public static int labelFontSize = 20;
	public static int smallLabelFontSize = 15;
	public static Font labelFont = new Font(defaultFontName, Font.BOLD, labelFontSize);
	public static Border bottomBorder = BorderFactory.createEmptyBorder(0, 0, 5, 0);
	
	// buttons
	public static int buttonHeight = 25;
	
	// inputs
	public static int largeInputFontSize = 30;
	public static Font largeInput = new Font(defaultFontName, Font.BOLD, largeInputFontSize);
	public static String defaultInputText = "Starts the conversation here";
	
	// memory labels + buttons
	public static String shortTermMemory = "Short Term Memory";
	public static String InformationExtracted = "Information Extracted";
	public static String skillsUnknown = "Skills Unknonwn";
	public static String deleteMemory ="Delete from Memory";
	
	public static String longTermMempry = "Long Term Memory";
	public static String knowledgeLearned = "Knowlegde Learned";
	public static String allKnowledgeRemembered = "All Knowledge Remembered";
	public static String forgetAllSkills = "Forget all Skills";
	
	public static JTextPane addTextPane(JPanel panel) {
		return addTextPane(panel, FONTLARGE);
	}
	
	public static JTextPane addTextPane(JPanel panel, String fontSetting) {
		JTextPane editor = new JTextPane();
		panel.add(editor);
		panel.add(new JScrollPane(editor));
		if(fontSetting.equals(FONTSMALL)) {
			editor.setFont(smallFont);
		} else if(fontSetting.equals(FONTMEDIAN)) {
			editor.setFont(medianFont);
		} else {
			editor.setFont(largeFont);
		}
		return editor;
	}
	
	public static void addSmallLabel(JPanel panel, String string, String alignment) {
		JLabel label = new JLabel(string);
		panel.add(label, BorderLayout.NORTH);
		makeSmallLabel(label, alignment);
	}
	
	public static void addSmallLabel(JPanel panel, String string) {
		JLabel label = new JLabel(string);
		panel.add(label, BorderLayout.NORTH);
		makeSmallLabel(label);
	}
	
	public static void addLargeLabel(JPanel panel, String string) {
		JLabel label = new JLabel(string);
		panel.add(label, BorderLayout.NORTH);
		makeLargeLabel(label);
	}
	
	public static void makeSmallLabel(JLabel label) {
		makeSmallLabel(label,ZPage.BOTTOM);
	}
	
	public static void makeSmallLabel(JLabel label, String alignment) {
		label.setPreferredSize(new Dimension(100, labelHeight));
		label.setFont(new Font(defaultFontName, Font.BOLD, smallLabelFontSize));
		if(alignment.equals(ZPage.CENTER)) {
			label.setHorizontalAlignment(JLabel.CENTER);
		} else {
			label.setVerticalAlignment(JLabel.BOTTOM);
		}
	}
	
	public static void makeLargeLabel(JLabel label) {
		label.setPreferredSize(new Dimension(100, ZPage.labelHeight));
		label.setFont(new Font(defaultFontName, Font.BOLD, labelFontSize));
		label.setHorizontalAlignment(JLabel.CENTER);
	}
	
	public static void appendToPane(JTextPane tp, String msg){
		appendToPane(tp, msg, Color.BLACK, ZPage.largeFontSize);
	}
	
	public static void appendToPane(JTextPane tp, String msg, Color c){
		appendToPane(tp, msg, c, ZPage.largeFontSize);
	}
	
	public static void appendToPane(JTextPane tp, String msg, int fontSize){
		appendToPane(tp, msg, Color.BLACK, fontSize);
	}
	
	public static void appendToPane(JTextPane tp, String msg, Color c, int fontSize){
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, ZPage.defaultFontName);
        aset = sc.addAttribute(aset, StyleConstants.FontSize, fontSize);
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
        
        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);
        tp.replaceSelection(msg);
    }

	public static String makeHTML(String content) {
		String stuff = Html.convertLf(header + Html.normal(content) + trailer);
		stuff = stuff.replace("font-size: 25.0px", "font-size: "+ZPage.smallFontSize+"px");
		return stuff;
	}
	
	public static List<String> beautifyMemory(List<String> lines) {
		List<String> newLines = new ArrayList<>();
		for(String line: lines) {
			line = line.substring(0, line.indexOf("@")) + headerGrey
					+ line.substring(line.indexOf("@"),line.length()) + trailer;
			newLines.add(line);
		}
		return newLines;
	}
	
	public static String makeComment(String content) {
		return makeHTML(headerGrey+content+trailerComment);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public static String ASCIIArt_big = "\n" + 
			"  _   _            _            _                                    \n" + 
			" | \\ | | _____   _(_) ___ ___  | |    ___  __ _ _ __ _ __   ___ _ __ \n" + 
			" |  \\| |/ _ \\ \\ / / |/ __/ _ \\ | |   / _ \\/ _` | '__| '_ \\ / _ \\ '__|\n" + 
			" | |\\  | (_) \\ V /| | (_|  __/ | |__|  __/ (_| | |  | | | |  __/ |   \n" + 
			" |_| \\_|\\___/ \\_/ |_|\\___\\___| |_____\\___|\\__,_|_|  |_| |_|\\___|_|   \n" + 
			"                                                                     \n" + 
			"";
	public static String ASCIIArt_thin = "\n" + 
			"      __          __   ___          ___       __        ___  __  \n" + 
			"|\\ | /  \\ \\  / | /  ` |__     |    |__   /\\  |__) |\\ | |__  |__) \n" + 
			"| \\| \\__/  \\/  | \\__, |___    |___ |___ /~~\\ |  \\ | \\| |___ |  \\ \n" + 
			"                                                                 \n" + 
			"";
	public static String ASCIIArt_shadow = "\n\n" + 
	"███╗   ██╗ ██████╗ ██╗   ██╗██╗ ██████╗███████╗    ██╗     ███████╗ █████╗ ██████╗ ███╗   ██╗███████╗██████╗ \n" + 
	"████╗  ██║██╔═══██╗██║   ██║██║██╔════╝██╔════╝    ██║     ██╔════╝██╔══██╗██╔══██╗████╗  ██║██╔════╝██╔══██╗\n" + 
	"██╔██╗ ██║██║   ██║██║   ██║██║██║     █████╗      ██║     █████╗  ███████║██████╔╝██╔██╗ ██║█████╗  ██████╔╝\n" + 
	"██║╚██╗██║██║   ██║╚██╗ ██╔╝██║██║     ██╔══╝      ██║     ██╔══╝  ██╔══██║██╔══██╗██║╚██╗██║██╔══╝  ██╔══██╗\n" + 
	"██║ ╚████║╚██████╔╝ ╚████╔╝ ██║╚██████╗███████╗    ███████╗███████╗██║  ██║██║  ██║██║ ╚████║███████╗██║  ██║\n" + 
	"╚═╝  ╚═══╝ ╚═════╝   ╚═══╝  ╚═╝ ╚═════╝╚══════╝    ╚══════╝╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═══╝╚══════╝╚═╝  ╚═╝\n" + 
	"                                                                                                             \n" + 
	"";
	public static String ASCIIArt = ASCIIArt_shadow;
}
