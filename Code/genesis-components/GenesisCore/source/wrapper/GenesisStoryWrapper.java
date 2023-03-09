package wrapper;

import javax.swing.*;

import com.ascent.gui.frame.WFrameApplication;
import com.ascent.gui.frame.auxiliaries.WFrameApplicationStartException;
import com.ascent.gui.swing.WConstants;

import conceptNet.conceptNetNetwork.ConceptNetClient;
import constants.Switch;
import dictionary.BundleGenerator;
import genesis.Genesis;
import start.Start;
import utils.Mark;

/*
 * Created on Nov 1, 2007 @author phw
 */

public class GenesisStoryWrapper extends WFrameApplication {

	Genesis genesis;

	public String getNavigationBarItem() {
		return "Genesis";
	}

	public String getNavigationBarItemHelp() {
		return "View the Genesis story understanding system";
	}

	public Genesis getGenesis() {
		if (genesis == null) {
			genesis = Genesis.getGenesis();
		}
		return genesis;
	}

	@Override
	public JComponent getView() {
		return getGenesis();
	}

	public JComponent getControlBar() {
		return getMenuBar();
	}

	@Override
	public void start() {
		// super.start();
		Mark.say("Start story system");
		getGenesis().start();
	}

	public boolean isStarted() {
		WConstants.setBannerGif(WConstants.getImage(TheGenesisSystem.class, "story.gif"), WConstants
		        .getImage(TheGenesisSystem.class, "genesis-gray.gif"));
		return super.isStarted();
	}

	public void stop() {
		super.stop();
		Mark.say("Stop story system");
		BundleGenerator.writeWordnetCache();
		Start.writeStartCaches();
		if (Switch.useConceptNetCache.isSelected()) {
		    ConceptNetClient.writeCache();
		}
	}

	@Override
	public JMenuBar getMenuBar() {
		// Mark.say("Getting menu bar");
		return getGenesis().getMenuBar();
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

}
