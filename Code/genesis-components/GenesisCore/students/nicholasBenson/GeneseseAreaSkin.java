package nicholasBenson;

import java.util.function.Predicate;

import utils.Mark;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

import com.sun.javafx.scene.control.skin.TextAreaSkin;

public class GeneseseAreaSkin extends TextAreaSkin {
	
	// TODO: This class will be used to modify the visuals of the GeneseseArea.

	public GeneseseAreaSkin(TextArea textArea) {
		super(textArea);
		
		for (Node n : getScrollPane().lookupAll(".scroll-bar")) {
			Mark.say("FOUND SCROLLBAR: " + n);
		}
	}
	
	private ScrollPane scrollPane = null;
	/** For whatever reason the ScrollPane element of a TextArea is
	 * private. This definitely-not-awful solution gets around that
	 * pesky "private"-ness by reflecting on the children of the skin
	 * and returning the first ScrollPane it finds. The result is
	 * only ever determined once, the first time the function is called.
	 */
	public ScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = ((ScrollPane)(getChildren().filtered(new Predicate<Node>() {
				@Override public boolean test(Node n) {
					return n instanceof ScrollPane;
				}
			}).get(0)));
		}
		return scrollPane;
	}

	public DoubleProperty getScrollPaneVValueProperty() {
		return getScrollPane().vvalueProperty();
	}

}
