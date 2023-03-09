package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.io.File;
import java.net.URL;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import samples.SamplesAnchor;

/*
 * Created on Aug 5, 2006 @author phw
 */
public class FileReaderPanel extends JPanel {
	TrafficLight trafficLight;

	JFileChooser fileChooser;

	JLabel directoryLabel = new JLabel("Directory");

	JLabel fileLabel = new JLabel("File");

	JTextField directoryField = new JTextField();

	JTextField fileField = new JTextField();

	JButton fileChooserButton;

	JPanel buttonPanel;

	JPanel radioPanel;

	JLabel selectedFileLabel;

	JPanel labelPanel;

	File inputFile;

	// States
	public static int noFile = 0, readyToRead = 1, running = 2, stopped = 3;

	private int state = noFile;

	public FileReaderPanel() {
		super();
		// setLayout(new MyLayoutManager());
		setBackground(Color.WHITE);
		// add("light", getTrafficLight());
		// add("labels", getLabelPanel());
		// add("buttons", getButtonPanel());
		// add(getStepButton(), BorderLayout.CENTER);
		add(getTrafficLight(), BorderLayout.EAST);
		getTrafficLight().setPreferredSize(new Dimension(50, 80));
		// this.setPreferredSize(new Dimension(600, 100));
	}

	public static void main(String[] args) {
		FileReaderPanel reader = new FileReaderPanel();
		JFrame frame = new JFrame("Testing");
		frame.getContentPane().add(reader, BorderLayout.CENTER);
		frame.setBounds(100, 100, 600, 200);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public TrafficLight getTrafficLight() {
		if (trafficLight == null) {
			trafficLight = new TrafficLight();
		}
		return trafficLight;
	}

	public JLabel getSelectedFileLabel() {
		if (selectedFileLabel == null) {
			selectedFileLabel = new JLabel("Hello World");
		}
		return selectedFileLabel;
	}

	public JFileChooser getFileChooser() {
		String file = new SamplesAnchor().get("sample.txt");
		return getFileChooser(file);
	}

	public JFileChooser getFileChooser(String file) {
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			ExampleFileFilter filter = new ExampleFileFilter();
			filter.addExtension("txt");
			filter.addExtension("text");
			filter.addExtension("story");
			filter.setDescription("Sentence-containing files");
			fileChooser.setFileFilter(filter);
		}
		fileChooser.setCurrentDirectory(new File(file));
		return fileChooser;
	}

	private JPanel getLabelPanel() {
		if (labelPanel == null) {
			labelPanel = new JPanel();
			// labelPanel.setLayout(new BorderLayout());
			Box.createGlue();
			JLabel fileLabel = new JLabel("File:");
			fileLabel.setBackground(Color.YELLOW);
			fileLabel.setOpaque(true);
			labelPanel.add(fileLabel);
			Box.createGlue();
			getSelectedFileLabel().setBackground(Color.YELLOW);
			getSelectedFileLabel().setOpaque(true);
			labelPanel.add(getSelectedFileLabel());
			Box.createGlue();
		}
		return labelPanel;
	}

	class MyLayoutManager implements LayoutManager {
		Component trafficLight;

		Component buttonPanel;

		Component labelPanel;

		public void addLayoutComponent(String identifier, Component component) {
			if ("light".equalsIgnoreCase(identifier)) {
				trafficLight = component;
				add(trafficLight);
			}
			if ("buttons".equalsIgnoreCase(identifier)) {
				buttonPanel = component;
				add(buttonPanel);
			}
			if ("labels".equalsIgnoreCase(identifier)) {
				labelPanel = component;
				add(labelPanel);
			}
		}

		public void removeLayoutComponent(Component component) {
		}

		public Dimension preferredLayoutSize(Container component) {
			return null;
		}

		public Dimension minimumLayoutSize(Container component) {
			return null;
		}

		public void layoutContainer(Container parent) {
			int width = parent.getWidth();
			int height = parent.getHeight();
			if (trafficLight != null) {
				// Right 1/8 of screen, with h/w ration of 4
				int lightAreaWidth = width / 8;
				int lightAreaHeight = height;
				int lightWidth = lightAreaWidth;
				int lightHeight = lightAreaHeight;
				int lightHToWRatio = 3;
				if (lightAreaHeight > lightHToWRatio * lightAreaWidth) {
					// Evidently too tall, control with width
					lightWidth = lightAreaWidth * 9 / 10;
					lightHeight = lightWidth * lightHToWRatio;
				}
				else {
					// Evidently too wide, control with height
					lightHeight = lightAreaHeight * 9 / 10;
					lightWidth = lightHeight / lightHToWRatio;
				}
				int xOffset = width - lightAreaWidth + (lightAreaWidth - lightWidth) / 2;
				int yOffset = (lightAreaHeight - lightHeight) / 2;
				trafficLight.setBounds(xOffset, yOffset, lightWidth, lightHeight);
			}
			if (labelPanel != null) {
				// Bottom half of left 7/8s
				int baseWidth = 7 * width / 8;
				int baseHeight = height / 2;
				labelPanel.setBackground(Color.CYAN);
				labelPanel.setBounds(baseWidth / 16, 17 * baseHeight / 16, 14 * baseWidth / 16, 14 * baseHeight / 16);
			}

			if (buttonPanel != null) {
				// Top half of left 7/8s
				int baseWidth = 7 * width / 8;
				int baseHeight = height / 2;
				buttonPanel.setBounds(baseWidth / 16, baseHeight / 16, 14 * baseWidth / 16, 14 * baseHeight / 16);
			}
		}
	}

	public JButton getFileChooserButton() {
		if (fileChooserButton == null) {
			fileChooserButton = new JButton("Choose file");
		}
		return fileChooserButton;
	}

	private int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
		if (state == readyToRead) {
			trafficLight.setGreen(true);
		}
		else if (state == stopped) {
			System.out.println("Setting light to red");
			trafficLight.setRed(true);
		}
	}

	public File getInputFile() {
		return inputFile;
	}

}
