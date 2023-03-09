package hibaAwad;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import utils.Html;
import utils.Punctuator;
import utils.Mark;

import connections.Connections;
import connections.WiredBox;

@SuppressWarnings("serial")
public class CoherenceViewer extends JPanel implements WiredBox {


	public static final String CLEAR = "clear";
	public static final String textPort = "My Text Port";
	public static final String dataPort = "My Data Port";
	public static final String labelPort = "My Label Port";
	public static final String axisLabelPort = "Axis labels";
	
	private String header = "<html><body style=" + Html.normal + ">";
	private String trailer = "</body></html>";
	
	private JTextPane label = new JTextPane();
	private Spider spider = new Spider();
	
	private String previousText = "";
	private String text = "";
	private String[] axisLabels = {"Number of chains", "Length of longest chain", "Number of caused events"};
	public CoherenceViewer() {
		this.setLayout(new BorderLayout());
		label.setContentType("text/html");
	
		//label.setLocation(0, 100);
		this.add(spider, BorderLayout.CENTER);
		this.add(label, BorderLayout.LINE_START);

		Connections.getPorts(this).addSignalProcessor(textPort, "processText");
		Connections.getPorts(this).addSignalProcessor(dataPort, "processData");
		Connections.getPorts(this).addSignalProcessor(labelPort, "processStoryLabels");
		Connections.getPorts(this).addSignalProcessor(axisLabelPort, "processAxisLabels");
		spider.setAxislabels(axisLabels);
	}


	public void processData(Object o) {
		Mark.say("Processing", o,
				"in Viewer viewer via call through direct wire", o.getClass());
		processViaDirectCall(o);
	}

	public void processViaDirectCall(Object o) {
		double[] datapoints = (double[]) o;
		spider.setData(datapoints);

	}
	
	public void processStoryLabels(Object o) {
		Mark.say("Processing", o,
				"in Coherence viewer via call through direct wire", o.getClass());
		String label = (String) o;
		spider.addStoryLabel(label);
	}
	public void processAxisLabels(Object o){
		Mark.say("here");
		String[] labels = (String[]) o;
		spider.setAxislabels(labels);
		
	}

	public void processText(Object o) {
		if (o == CLEAR) {
			clear();
			return;
		}
		// Mark.say("Adding text", o);
		addText((String) o);
	}

	public void addText(Object o) {
		label.setBackground(Color.WHITE);
		String s = o.toString();
		// Drip pan
		if (s.equals(previousText)) {
			// return;
		}
		
		if (previousText.equals("")){
			s = Html.h1(s);
		}
		previousText = s.toString();
		text += Punctuator.addPeriod(s) + "\n";
		// text += Punctuator.addPeriod(s);
		// text += Punctuator.addSpace(s);
		// Mark.say("Setting text", text);
		setText(text);
		scrollToEnd();
	}

	private void scrollToEnd() {
		label.selectAll();
		int x = label.getSelectionEnd();
		label.select(x, x);
	}

	private void setText(String s) {
		String contents = header + Html.normal(s) + trailer;
		String stuff = Html.convertLf(contents);
		// Mark.say("Html text is", stuff);
		label.setText(stuff);
	}

	public void clear() {
		// Mark.say("Clearing");
		text = "";
		setText(text);
		previousText = "";
		spider.clearData();
		
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] ignore) {
		CoherenceViewer viewer = new CoherenceViewer();
		double[] constant1 = { 1.0, 0.1, 0.3, 0.5, 0.7, 0.9 };
		String[] labels = {"label1", "label2", "label3", "label4", "label5", "label6"};
		// spider.setData(constant1);
		JFrame frame = new JFrame();
		frame.getContentPane().add(viewer);
		frame.setBounds(100, 100, 500, 700);
		viewer.processText(Html.h1("h1"));
		viewer.processText("text");
		viewer.processData(constant1);
		viewer.processAxisLabels(labels);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		frame.show();
	}

}