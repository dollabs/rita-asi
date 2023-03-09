package wrapper;

import genesis.*;
import gui.ElaborationView;
import storyProcessor.StoryProcessor;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.*;
import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

import javax.swing.*;

import utils.*;

import com.ascent.gui.frame.ABasicFrame;
import com.ascent.gui.swing.WConstants;

/*
 * Created on Nov 1, 2007 @author phw
 */
public class TheGenesisSystem extends ABasicFrame {

	public static void main(String[] args) {

//		Mark.say(System.getProperty("os.name"));
//		Mark.say(System.getProperty("os.arch"));
		new TheGenesisSystem(args).start();

		// to show you the trace of the story processing:
//		FileSourceReader.debug = true;
//		StoryProcessor.debug = true;
//		ElaborationView.debug = true;
//		FileSourceReader.getFileSourceReader().readTheWholeStoryWithThread("Align Annotate.txt");
	}

	public TheGenesisSystem(String[] args) {

		super(args, TheGenesisSystem.class.getResource("genesis.xml"));
		if (args.length != 0) {
			Webstart.setWebStart(true);
		}
		else {
			Webstart.setWebStart(false);
			setDocumentationBase("file://c:/phw/javagit/genesis/webstart");
		}

		setTitle("Genesis");
		WConstants.setRequiresLogin(false);
		WConstants.setBannerGif(WConstants.getImage(TheGenesisSystem.class, "genesis.gif"), WConstants
		        .getImage(TheGenesisSystem.class, "genesis-gray.gif"));
		ActionListener l = new MyActionListener();
		GenesisControls.makeSmallVideoRecordingButton.addActionListener(l);
		GenesisControls.makeMediumVideoRecordingButton.addActionListener(l);
		GenesisControls.makeLargeVideoRecordingButton.addActionListener(l);
		GenesisControls.makeCoUButton.addActionListener(l);

		if (Webstart.isWebStart()) {
			try {
				// Must display JEditorPane to get URL read
				System.out.println("Showing top and getting counter");
				getNavigationBar().add(getCounter());

			}
			catch (Exception e) {
				Mark.say("Protection against strange errors");
			}
		}

	}

	/*
	 * Covers up web page, but displays it, so counter incremented.
	 */
	private class MyJEditorPane extends JEditorPane {
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.setColor(WConstants.navigationBarColor);
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
	}

	/*
	 * Fetches counter page
	 */
	private JComponent getCounter() {
		try {
			MyJEditorPane counter = new MyJEditorPane();
			counter.setContentType("text/html");
			counter.setEditable(false);
			URL url = new URL("http://people.csail.mit.edu/phw/genesis-runs.html");
			counter.setPage(url);
			return counter;
		}
		catch (Exception e) {
			Mark.say("Unable to count");
		}
		return null;

	}

	private void setToVideoRecordingDimensions(Rectangle r) {
		Mark.say("Adjusting size to", r);
		ABasicFrame.getFrame().setBounds(r);
		ABasicFrame.getFrame().validate();
	}

	private class MyActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == Genesis.makeSmallVideoRecordingButton) {
				setToVideoRecordingDimensions(new Rectangle(0, 0, 1024, 768));
			}
			else if (e.getSource() == Genesis.makeMediumVideoRecordingButton) {
				setToVideoRecordingDimensions(new Rectangle(0, 0, 1280, 1024));
			}
			else if (e.getSource() == Genesis.makeLargeVideoRecordingButton) {
				setToVideoRecordingDimensions(new Rectangle(0, 0, 1600, 1200));
			}
			else if (e.getSource() == Genesis.makeCoUButton) {
				setToVideoRecordingDimensions(new Rectangle(0, 0, 1920, 1080));
			}
		}
	}

}
