package hibaAwad;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import utils.Mark;

import connections.WiredBox;

@SuppressWarnings("serial")
public class Spider extends JPanel implements WiredBox {
	ArrayList<double[]> dataset = new ArrayList<double[]>();
	double[] max = {};
	ArrayList<String> storyLabels = new ArrayList<String>();
	String[] axislabels = {}; 
	int fillPercentage = 90;

	int ballPercentage = 2;
	Color ballColor = Color.BLACK;
	Color areaColor = Color.YELLOW;

	boolean connectDots = false;
	boolean fillArea = false;
	boolean drawAxis = false;
	public static final String textPort = "My Text Port";
	public Spider() {
		setBackground(Color.WHITE);
		setDataColor(Color.RED);
		setAreaColor(Color.LIGHT_GRAY);
		// this.setPreferredSize(new Dimension(500, 700));
		//this.add(new JLabel("hello"));
	//	setConnectDots(true);
	//	setFillArea(true);
	}

	public void processData(Object o) {
		Mark.say("Processing", o,
				"in Viewer viewer via call through direct wire", o.getClass());
		processViaDirectCall(o);
	}

	public void setConnectDots(boolean b) {
		connectDots = b;
		repaint();
	}

	public void setFillArea(boolean b) {
		fillArea = b;
		repaint();
	}

	public void processViaDirectCall(Object o) {
		double[] datapoints = (double[]) o;
		setData(datapoints);

	}
	
	public void setData(double[] points) {
		if (dataset.size() != 0 && dataset.get(0).length != points.length) {
			System.err
					.println("setData in Spider got wrong number of data points");
		}

		if (dataset.size() == 0) {
			max = points.clone();
		} else {
			for (int i = 0; i < points.length; i++) {
				if (points[i] > max[i]) {
					max[i] = points[i];
				}
			}
			
		}
		dataset.add(points.clone());

		repaint();
	}
	
	public void clearData(){
		dataset = new ArrayList<double[]>();
		storyLabels = new ArrayList<String>();
		
	}

	public void addStoryLabel(String label){
		storyLabels.add(label);
		repaint();
	}
	
	public void setDataColor(Color c) {
		ballColor = c;
		repaint();
	}

	public void setAreaColor(Color c) {
		areaColor = c;
		repaint();
	}
	public String[] getAxislabels() {
		return axislabels;
	}

	public void setAxislabels(String[] axislabels) {
		this.axislabels = axislabels;
		repaint();
		
	}


	public void paint(Graphics g) {
		super.paint(g);
		if (dataset.size() == 0) {
			return;
		}
		int totalWidth = getWidth();
		int totalHeight = getHeight();
		if (totalWidth == 0 || totalHeight == 0) {
			return;
		}
		int width = fillPercentage * totalWidth / 100;
		int height = fillPercentage * totalHeight / 100;
		int radius = Math.min(width, height) / 2;
		int xCenter = totalWidth / 2;
		int yCenter = totalHeight / 2;
		drawData(g, radius, xCenter, yCenter);
	}

	private void drawData(Graphics g, int radius, int xCenter, int yCenter) {
		//when you draw rescale by maximum. 
		ArrayList<double[]> scaledDataset = new ArrayList<double[]>();
		for (int j = 0; j < dataset.size(); j++) {
			double[] scaledData = new double[dataset.get(j).length];
			for (int i = 0; i < dataset.get(j).length; i++) {
				scaledData[i] = dataset.get(j)[i] / max[i];
			}
			scaledDataset.add(scaledData);
		}
		for (int j=0;j<scaledDataset.size(); ++j){
		double[] data = scaledDataset.get(j);
		double angle = 2 * Math.PI / data.length;
		int[] x = new int[data.length];
		int[] y = new int[data.length];
		int[] dataX = new int[data.length];
		int[] dataY = new int[data.length];
		int[] contourX = new int[data.length];
		int[] contourY = new int[data.length];
		int ballRadius = radius * ballPercentage / 100;
		for (int i = 0; i < data.length; ++i) {
			double theta = i * angle;
			x[i] = (int) (radius * Math.cos(theta));
			y[i] = (int) (radius * Math.sin(theta));
			dataX[i] = (int) (x[i] * data[i]);
			dataY[i] = (int) (y[i] * data[i]);
			contourX[i] = xCenter + dataX[i];
			contourY[i] = yCenter + dataY[i];
		}
		// Fill the area
		if (j==0){
		if (fillArea) {
			Color handle = g.getColor();
			g.setColor(areaColor);
			g.fillPolygon(contourX, contourY, data.length);
			g.setColor(handle);
		}
		}
		
		// Draw axes
		if (j==0){
		for (int i = 0; i < data.length; ++i) {
			g.drawLine(xCenter, yCenter, xCenter + x[i], yCenter + y[i]);
		}
		}
		
		//Draw axes lables
		if (j==0){
			for (int i = 0; i < data.length; ++i) {
				g.drawString(axislabels[i], xCenter + x[i] +10, yCenter + y[i]);
			}
		}
		// Draw connecting line
		if (j==0){
		if (connectDots || fillArea) {
			for (int i = 0; i < data.length; ++i) {
				int nextI = (i + 1) % data.length;
				g.drawLine(contourX[i], contourY[i], contourX[nextI],
						contourY[nextI]);
			}
		}
		}
		
		// Draw data balls
		for (int i = 0; i < data.length; ++i) {
			Color handle = g.getColor();
			g.setColor(ballColor);
			drawDataPoint(g, ballRadius, contourX[i], contourY[i]);
			g.setColor(handle);
		}
		
		//Draw label for each ball
		if (j< storyLabels.size()){
			
		for (int i = 0; i<data.length; ++i){
			g.drawString(storyLabels.get(j),contourX[i]-20*(i+1) , contourY[i]+20);
			}
			}
		
		}

	}

	private void drawDataPoint(Graphics g, int radius, int x, int y) {
		g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
	}





	@SuppressWarnings({ "deprecation", "unused" })
	public static void main(String[] ignore) {
		Spider spider = new Spider();
		double[] constant1 = { 1.0, 0.1, 0.3, 0.5, 0.7, 0.9 };
		double[] a = { 5, 10, 3, 4};
		double[] b = {10, 20, 1, 2};
		String[] labels = {"hello", "yes", "no", "ouf"};
		// spider.setData(constant1);
		JFrame frame = new JFrame();
		frame.getContentPane().add(spider);
		frame.setBounds(100, 100, 500, 700);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		spider.setData(a);
		spider.setData(b);
		spider.setDataColor(Color.RED);
		spider.setAreaColor(Color.LIGHT_GRAY);
		spider.setAxislabels(labels);
		spider.addStoryLabel("a");
		spider.addStoryLabel("b");
		frame.show();
		spider.setConnectDots(true);
		spider.setFillArea(true);

	}

}