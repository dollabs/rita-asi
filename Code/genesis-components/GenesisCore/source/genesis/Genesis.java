package genesis;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.*;

import javax.swing.*;

import start.Start;
import utils.*;
import wrapper.TheGenesisSystem;
import dictionary.BundleGenerator;
import dictionary.FallbackBundleGenerator;
import conceptNet.conceptNetNetwork.ConceptNetClient;
import connections.*;
import connections.Connections.NetWireError;
import connections.Connections.NetWireException;
import constants.Switch;

/*
 * Created on Jul 10, 2006 @author phw
 */
public class Genesis extends GenesisPlugBoardLower implements WindowListener {


	public static JFrame GENESIS_FRAME;

	public static Genesis genesis;

	/**
	 * Major change 27 Apr 2017: provides handle for Genesis instance from anywhere in the system
	 * 
	 * @return
	 */
	public static Genesis getGenesis() {
		if (genesis == null) {
			genesis = new Genesis();
		}
		return genesis;
	}

	public static void main(String[] args) {
		if (args.length != 0) {
			Webstart.setWebStart(true);
		}
		else {
			Webstart.setWebStart(false);
		}
		// Webstart.getTextFiles();
		// Webstart.getTextFile("Macbeth1");

		Genesis genesis = getGenesis();
		genesis.startInFrame();
	}

	JPanel trafficLightPanel;

	protected Genesis() {
		BundleGenerator.setSingletonClass(FallbackBundleGenerator.class);// ADK
		                                                                 // 10/12/2010
		                                                                 // try
		                                                                 // the
		                                                                 // remote
		                                                                 // WordNet
		                                                                 // server,
		                                                                 // fall
		                                                                 // back
		                                                                 // on
		                                                                 // local.
		setLayout(new BorderLayout());
		try {
			Connections.useWireServer(DefaultSettings.WIRE_SERVER);
			Mark.say("You are connected to the wire server at " + DefaultSettings.WIRE_SERVER);
			Mark.say("You may browse to " + DefaultSettings.WIRE_SERVER + " to view a graph of distributed components.");
			// WiredBox service = new WiredBox() {
			// public String getName() {
			// return null;
			// }
			// };
		}
		catch (NetWireException e) {
			printNetworkError(e);
		}
		catch (NetWireError e) {
			printNetworkError(e);
		}
		Connections.setVerbose(false);

		GENESIS_FRAME = TheGenesisSystem.getPopupAnchor();

	}

	/**
	 * Could not get this to work because of focus issue.
	 */
	class MyKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent ke) {
			if (ke.isControlDown()) {
				if (ke.getKeyCode() == 'r') {
					ke.consume();
					Mark.say("Hello ctrl r!");
				}
			}

		}

		@Override
		public void keyPressed(KeyEvent e) {
			Mark.say("Hello ctrl r!");

		}

		@Override
		public void keyReleased(KeyEvent e) {
			Mark.say("Hello ctrl r!");

		}
	}

	private void printNetworkError(Throwable e) {
		e.printStackTrace();
		System.err.println("For the impatient:");
		System.err.println(e.toString());
		if (e.getCause() != null) {
			System.err.println("\tCause: " + e.getCause());
			if (
			// e.getCause() instanceof XmlRpcException
			true) {
				System.err.println("This likely means one of the following:\n" + "\t--> you need to update Propagators,\n"
				        + "\t--> you don't have a working Internet connection, or,\n"
				        + "\t--> the wire server is down for development or maintenance.");
			}
		}
	}

	public void start() {
		Mark.say("Memory Max = " + Runtime.getRuntime().maxMemory());
		if (Switch.useWordnetCache.isSelected()) {
			try {
				try {
					BundleGenerator.readWordnetCache();
				}
				catch (Exception e) {
					Mark.err("Strange blowout in Genesis.start at point 1");
					BundleGenerator.purgeWordnetCache();
					BundleGenerator.writeWordnetCache();
				}

			}
			catch (Exception e) {
				Mark.err("Strange blowout in Genesis.start at point 2");
			}
		}
		if (Switch.useStartCache.isSelected()) {
			Start.readStartCaches();
		}
		if (Switch.useConceptNetCache.isSelected()) {
		    ConceptNetClient.readCache();
		}
		initializeWiring();
		Mark.say("Wiring initialized");
		initializeListeners();
		Mark.say("Listeners initialized");
		initializeGraphics();
		Mark.say("Graphics initialized");

		// Mark.say("Popup anchor is", GENESIS_FRAME);

	}

	public void startInFrame() {
		start();
		JFrame frame = new JFrame();
		GENESIS_FRAME = frame;
		frame.setTitle("Genesis");
		frame.getContentPane().setBackground(Color.WHITE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(this);
		frame.setJMenuBar(getMenuBar());
		frame.setBounds(0, 0, 1024, 768);
		frame.addWindowListener(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// this.getTalker().setFrame(frame);
		frame.setVisible(true);
		Mark.say("Solo initialized");
		GenesisControls.makeSmallVideoRecordingButton.addActionListener(new MyActionListener(frame));
		GenesisControls.makeLargeVideoRecordingButton.addActionListener(new MyActionListener(frame));
		Mark.say("Now popup anchor is", GENESIS_FRAME);
	}

	public JInternalFrame getJInternalFrameVersion() {
		start();
		JInternalFrame frame = new JInternalFrame();
		// GENESIS_FRAME = frame;
		frame.setTitle("Genesis");
		frame.getContentPane().setBackground(Color.WHITE);
		frame.getContentPane().setLayout(new BorderLayout());
		frame.getContentPane().add(this);
		frame.setJMenuBar(getMenuBar());
		frame.setBounds(0, 0, 1024, 768);
		// frame.addWindowListener(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// this.getTalker().setFrame(frame);
		frame.setVisible(true);
		Mark.say("Solo initialized");
		// GenesisControls.makeSmallVideoRecordingButton.addActionListener(new MyActionListener(frame));
		// GenesisControls.makeLargeVideoRecordingButton.addActionListener(new MyActionListener(frame));
		Mark.say("Now popup anchor is", GENESIS_FRAME);
		return frame;
	}

	private void setToVideoRecordingDimensions(JFrame frame, Rectangle rectangle) {
		Mark.say("Adjusting size");
		frame.setBounds(rectangle);
		frame.invalidate();

	}

	public class MyActionListener implements ActionListener {
		JFrame frame;

		public MyActionListener(JFrame frame) {
			this.frame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {

			if (e.getSource() == Genesis.makeSmallVideoRecordingButton) {
				setToVideoRecordingDimensions(frame, new Rectangle(0, 0, 1024, 768));
			}
			else if (e.getSource() == Genesis.makeMediumVideoRecordingButton) {
				setToVideoRecordingDimensions(frame, new Rectangle(0, 0, 1280, 1024));
			}
			else if (e.getSource() == Genesis.makeLargeVideoRecordingButton) {
				setToVideoRecordingDimensions(frame, new Rectangle(0, 0, 1600, 1200));
			}
			else if (e.getSource() == Genesis.makeCoUButton) {
				setToVideoRecordingDimensions(frame, new Rectangle(0, 0, 1920, 1080));
			}
		}

	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5573053212665932691L;

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowClosing(WindowEvent e) {
		Mark.say("Stop called");
		if (Switch.useStartCache.isSelected()) {
			Start.writeStartCaches();
		}
		if (Switch.useWordnetCache.isSelected()) {
			BundleGenerator.writeWordnetCache();
		}
		if (Switch.useConceptNetCache.isSelected()) {
		    ConceptNetClient.writeCache();
		}

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowOpened(WindowEvent e) {

	}

}
