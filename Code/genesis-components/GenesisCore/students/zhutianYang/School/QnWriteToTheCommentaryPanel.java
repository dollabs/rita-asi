package zhutianYang.School;

import connections.Connections;
import connections.signals.BetterSignal;
import constants.Markers;
import genesis.Genesis;
import gui.TextViewer;
import utils.Html;
import utils.Mark;

/*
 * Created on May 1, 2016
 * @author phw
 */

public class QnWriteToTheCommentaryPanel extends Genesis {

	public QnWriteToTheCommentaryPanel() {
		super();
		// Establish connection to commentary container from whatever box wants to have text seen; in this case, it is
		// the Genesis box itself. Note that thre is no named port on the commentary container.
		Connections.wire(this, getCommentaryContainer());
		demonstrateWrite();
	}

	private void demonstrateWrite() {
		String tabname = "Demonstration tab";
		String message = "Hello";
		// Note that Html class has many utility methods for composing a message. This one renders in italic.
		message += " " + Html.ital("world") + ".";
		// Signal is a BetterSignal instance with tab name as first argument, text to be written as second.
		BetterSignal bs = new BetterSignal(tabname, message);
		Connections.getPorts(this).transmit(bs);
		// When you want to clear the text on a tab, do this
		bs = new BetterSignal(tabname, TextViewer.CLEAR);
		// Comment following line to see Hello world. Goodbye.
		// Connections.getPorts(this).transmit(bs);
		Connections.getPorts(this).transmit(new BetterSignal(tabname, "Goodbye."));
	}


	public static void main(String[] args) {
		System.out.println(System.getProperty("user.dir"));
		QnWriteToTheCommentaryPanel myGenesis = new QnWriteToTheCommentaryPanel();
		myGenesis.startInFrame();

	}
}
