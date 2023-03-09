package gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import connections.signals.BetterSignal;
import genesis.FileSourceReader;
import utils.Html;
import utils.*;
import wrapper.TheGenesisSystem;

/*
 * Created on Dec 31, 2014
 * @author phw
 */

public class PopupBox extends JDialog {

	private JDialog dialog;

	private JEditorPane htmlPane;

	private JButton okButton;

	private JButton xxButton;

	private String forString = "for";

	private String okString = "with ok";

	private FileSourceReader fileSourceReader;

	private boolean result = false;

	public PopupBox(FileSourceReader r) {
		fileSourceReader = r;
		// Mark.say("Constructing popup box");
	}

	public int processPopup(Object input) {
		if (input instanceof BetterSignal) {
			BetterSignal signal = (BetterSignal) input;
			String file = signal.get(0, String.class);
			int seconds = 1;
			if (signal.size() > 1) {
				seconds = signal.get(1, Integer.class);
			}
			if (file.startsWith(":")) {
				file = file.substring(1).trim();
			}
			int withIndex = file.indexOf(okString);
			int forIndex = file.indexOf(forString);
			if (withIndex > 0) {
				file = file.substring(0, withIndex).trim();
				seconds = -1;
			}
			else if (forIndex > 0) {
				String timeString = file.substring(forIndex + forString.length()).trim();
				try {
					file = file.substring(0, forIndex).trim();
					forIndex = timeString.indexOf("sec");
					timeString = timeString.substring(0, forIndex).trim();
					seconds = Integer.parseInt(timeString);
				}
				catch (NumberFormatException e) {
					Mark.err("Could not get seconds out of", timeString);
				}
			}
			else {
				seconds = -1;
			}

			if (file.endsWith(".")) {
				file = file.substring(0, file.length() - 1);
			}

			if (!file.endsWith(".txt")) {
				file += ".txt";
			}

			// Mark.say("Hello!!!!!!!!!!!!!!!", file);

			String message = Webstart.readTextFile(file);

			// String message = fileSourceReader.readFileText(file);
			
			message = message.trim();

			// Strangely, following line did not work
			// if (!(message.startsWith("<html>"))) {

			// This one works
			if (!(message.substring(0, 6).equals("<head>"))) {
				message = Html.html(Html.large(message));
				// message = "<html>" + message + "</html>";
			}
			else {
				// Mark.say("Message!!!\n", message);
			}

			showDialog(message, seconds);

		}
		return 0;
	}

	public void showDialog(String message, int seconds) {
		Frame frame = TheGenesisSystem.getPopupAnchor();
		if (dialog == null) {

			dialog = new JDialog(frame, true);
			
			dialog.setLayout(new BorderLayout());

			htmlPane = new JEditorPane();
			htmlPane.setContentType("text/html");
			okButton = new JButton("Continue");
			xxButton = new JButton("Cancel");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
					setResult(true);
				}
			});
			xxButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(false);
					setResult(false);
				}
			});
			JScrollPane scroller = new JScrollPane(htmlPane);
			scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
			dialog.add(scroller, BorderLayout.CENTER);

			JPanel panel = new JPanel();
			panel.setLayout(new GridLayout(1, 0));
			panel.add(okButton);
			panel.add(xxButton);

			dialog.add(panel, BorderLayout.SOUTH);

			// Incredible, needed to give ok button focus first time around
			dialog.setModal(false);
			dialog.setVisible(true);
			okButton.requestFocus();
			xxButton.doClick();
			dialog.setModal(true);
		}

		int width = 800;
		int height = 700;
		int x = 0;
		int y = 0;

		if (frame != null) {
			width = frame.getSize().width / 2;
			height = frame.getSize().height / 2;
			x = frame.getX() + width / 2;
			y = frame.getY() + height / 2;
		}

		dialog.setBounds(x, y, width, height);

		htmlPane.setText(message);

		Mark.say(false, "Message\n", message);

		if (seconds > 0) {
			new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(seconds * 1000);
						dialog.setVisible(false);
					}
					catch (Throwable th) {

					}
				}
			}).start();
		}
		okButton.requestFocus();
		dialog.setVisible(true);

	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

}
