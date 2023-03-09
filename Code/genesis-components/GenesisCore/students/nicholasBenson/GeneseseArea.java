package nicholasBenson;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextArea;


/** An extended TextArea that includes a custom Skin and line number
 * tracking. */
public class GeneseseArea extends TextArea {

	private GeneseseAreaSkin skin = null;
	public GeneseseArea() {
		super();
		skin = new GeneseseAreaSkin(this);
		this.setSkin(skin);
	}
	
	// Hacky: Need the skin object to get do useful visual things.
	public GeneseseAreaSkin getGASkin() {
		return skin;
	}
	
	
	// Line Number tracking. Slightly hack-y: see replaceText. //
	private IntegerProperty lineCount = new SimpleIntegerProperty(1);
	public IntegerProperty lineCountProperty() { return lineCount; }
	
	@Override
	/** 
	 * A hack to efficiently track the number of lines in the current
	 * story without having to re-invent the TextArea.
	 * 
	 * The original replaceText in TextArea is called to handle any
	 * changes in the textual content of the TextArea and does important
	 * things that we don't want to touch.
	 * 
	 * This override simply calculates the change in the number of lines
	 * of the TextArea, modifies this class's lineCountProperty, then
	 * calls super().
	 * 
	 * Note: Because of this hack, calling TextArea.setText() will break
	 * line-number tracking because setText does not call replaceText.
	 * Use this method, or replaceAllText instead.
	 */
	public void replaceText(int start, int end, String text) {
		
		int newlinesInArgument = 0;
		for (Character c : text.toCharArray()) {
			if (c == '\n') {
				newlinesInArgument++;
			}
		}
		
		int newlinesInIndices = 0;
		char[] chars = new char[end-start];
		getText().getChars(start, end, chars, 0);
		for (Character c : chars) {
			if (c == '\n') {
				newlinesInIndices++;
			}
		}
		
		lineCount.setValue(lineCount.getValue() + newlinesInArgument - newlinesInIndices);
		
		super.replaceText(start, end, text);
    }
	/**
	 * Due to the replaceText hack, this method provides a slightly
	 * faster way of replacing all of the text in the TextArea
	 * when we're switching to an entirely new story by not
	 * counting the number of lines in the new text.
	 * It also serves as a safe alternative to setText() that
	 * preserves the correctness of the lineNumber count.
	 */
	public void replaceAllText(String text) {

		int newlinesInArgument = 0;
		for (Character c : text.toCharArray()) {
			if (c == '\n') {
				newlinesInArgument++;
			}
		}
		
		lineCount.setValue(1 + newlinesInArgument);
		
		super.setText(text);
	}

}
