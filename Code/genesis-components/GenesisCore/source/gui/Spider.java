package gui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class Spider extends JPanel {
 double[] data = {};
 String[] labels = {};
  
 int fillPercentage = 90;
 
 int ballPercentage = 5;
 Color ballColor = Color.BLACK;
 Color areaColor = Color.YELLOW;

 boolean connectDots = false;
 boolean fillArea = false;

 public Spider () {
  setBackground(Color.WHITE);
 }

 public void setConnectDots(boolean b) {connectDots = b; repaint();}

 public void setFillArea(boolean b) {fillArea = b; repaint();}

 public void setData(double [] points) {
  if (data.length != 0 && data.length != points.length) {
   System.err.println("setData in Spider got wrong number of data points");
  }
  data = points;
  repaint();
 }
 
 public void setAxisLabels(String[] titles) {
     if (labels.length != 0 && labels.length != titles.length) {
         System.err.println("setData in Spider got wrong number of axis labels");
     }
     labels = titles;
     repaint();
 }

 public void setDataColor(Color c) {ballColor = c; repaint();}
 public void setAreaColor(Color c) {areaColor = c; repaint();}

 public void paint (Graphics g) {
  super.paint(g);
  if (data.length == 0) {return;}
  int totalWidth = getWidth();
  int totalHeight = getHeight();
  if (totalWidth == 0 || totalHeight == 0) {return;}
  int width = fillPercentage * totalWidth / 100;
  int height = fillPercentage * totalHeight / 100;
  int radius = Math.min(width, height) / 2;
  if (labels.length > 0) {
      radius -= 20;
  }
  int xCenter = totalWidth / 2;
  int yCenter = totalHeight / 2;
  drawData(g, radius, xCenter, yCenter);  
 }

 private void drawData(Graphics g, int radius, int xCenter, int yCenter) {
  double angle = 2 * Math.PI / data.length;
  int [] x = new int[data.length];
  int [] y = new int[data.length];
  int [] dataX = new int[data.length];
  int [] dataY = new int[data.length];
  int [] contourX = new int[data.length];
  int [] contourY = new int[data.length];
  int[] labelX = new int[labels.length];
  int[] labelY = new int[labels.length];
  int ballRadius = radius * ballPercentage / 100;
  for (int i = 0; i < data.length; ++i) {
   double theta = i * angle;
   x[i] = (int)(radius * Math.cos(theta));
   y[i] = (int)(radius * Math.sin(theta));
   dataX[i] = (int)(x[i] * data[i]);
   dataY[i] = (int)(y[i] * data[i]);
   contourX[i] = xCenter + dataX[i];
   contourY[i] = yCenter + dataY[i];
   if (i < labels.length) {
       labelX[i] = (int) (xCenter + (radius+20)*Math.cos(theta));
       labelY[i] = (int) (yCenter + (radius+20)*Math.sin(theta));
       // avoid placing caption directly on axis
       if (Math.abs(Math.cos(theta)) == 1) {
           labelY[i] += 20;
       }
   }
  }
  // Fill the area
  if (fillArea) {
   Color handle = g.getColor();
   g.setColor(areaColor);
   g.fillPolygon(contourX, contourY, data.length);
   g.setColor(handle);
  }
  // Draw axes
  for (int i = 0; i < data.length; ++i) {
   g.drawLine (xCenter, yCenter, xCenter + x[i], yCenter + y[i]);
   if (i < labels.length) {
       g.drawString(labels[i], labelX[i], labelY[i]);
   }
  }
  // Draw connecting line
  if (connectDots || fillArea) {
   for (int i = 0; i < data.length; ++i) {
    int nextI = (i+1) % data.length;
    g.drawLine(contourX[i], contourY[i], contourX[nextI], contourY[nextI]);
   }
  }
  // Draw data balls
  for (int i = 0; i < data.length; ++i) {
   Color handle = g.getColor();
   g.setColor(ballColor);
   drawDataPoint(g, ballRadius, contourX[i], contourY[i]);
   g.setColor(handle);
  }
 }

 private void drawDataPoint (Graphics g, int radius, int x, int y) {  
  g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
 }

 public static void main (String [] ignore) {
  Spider spider = new Spider();
  double [] constant1 = {1.0, 0.1, 0.3, 0.5, 0.7, 0.9};
  double [] constant2 = {1.0, 0.2, 0.4, 0.6, 0.8, 1.0};
  spider.setData(constant1);
  JFrame frame = new JFrame();
  frame.getContentPane().add(spider);
  frame.setBounds(100, 100, 500, 700);
  frame.addWindowListener(new WindowAdapter () { 
                     public void windowClosing(WindowEvent e) { 
                     System.exit(0); 
                    }} 
                   ); 
  spider.setData(constant2);
  spider.setDataColor(Color.RED);
  spider.setAreaColor(Color.LIGHT_GRAY);
  frame.show();
  spider.setConnectDots(true);
  spider.setFillArea(true);

 }

} 