package nicholasBenson;

import javafx.embed.swing.JFXPanel;

import javax.swing.JComponent;
import utils.Mark;

import com.ascent.gui.frame.WFrameApplication;
import com.ascent.gui.frame.auxiliaries.WFrameApplicationStartException;

/** A wrapper class for launching GenAssist from within TheGenesisSystem. */
public class GenAssistWrapper extends WFrameApplication {
	
	private JFXPanel fxPanel;
	private GenAssist genAssist;
	
	/** Lazy-loading getter for a single GenAssist instance. */
	public GenAssist getGenAssist() {
		if (genAssist == null) {
			genAssist = new GenAssist();
		}
		return genAssist;
	}
	
	/** Lazy-loading getter for a JFXPanel containing GenAssist, suitable for 
	 * embedding GenAssist within a Swing application. */
	public JFXPanel getFXPanel() {
		if (fxPanel == null) {
			fxPanel = new JFXPanel();
			fxPanel.setScene(getGenAssist().getPrimaryScene());
		}
		return fxPanel;
	}
	

	// Methods for use by TheGenesisSystem, which expects a WFrameApplication.
	
	@Override
	public void start() throws WFrameApplicationStartException {
		super.start();
		Mark.say("Start GenAssist");
		
		getFXPanel();
	}
	
	@Override
	public String getAccessType() {
		return null;
	}
	@Override
	public void restoreTaskBarImage() {
		
	}
	@Override
	public void restoreTaskBarTitle() {
		
	}
	@Override
	public String getNavigationBarItem() {
		return "GenAssist";
	}
	@Override
	public String getNavigationBarItemHelp() {
		return "An word processing application for writing stories in Genesese.";
	}
	@Override
	public JComponent getView() {
		return getFXPanel();
	}

}
