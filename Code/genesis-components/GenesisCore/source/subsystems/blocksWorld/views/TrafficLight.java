package subsystems.blocksWorld.views;


import java.awt.*;

import javax.swing.*;



/*
 * Created on Sep 15, 2005
 * @author Patrick
 */

public class TrafficLight extends JPanel {
 
 private Light redLight = new Light(Color.RED), orangeLight = new Light(Color.ORANGE), greenLight = new Light(Color.GREEN);
 private boolean red, orange, green;
 
 public TrafficLight () {
		setLayout(new GridLayout(0, 1));
  add(redLight);
  add(orangeLight);
  add(greenLight);
  setMinimumSize(new Dimension(0, 0));
  setPreferredSize(new Dimension(100, 200));
 }

 public boolean isGreen() {
  return green;
 }
 public void setGreen(boolean green) {
  this.green = green;
  greenLight.setOn(green);
  if (green) {
   setOrange(false);
   setRed(false);
  }
 }
 public boolean isOrange() {
  return orange;
 }
 public void setOrange(boolean orange) {
  this.orange = orange;
  orangeLight.setOn(orange);
  if (orange) {
   setGreen(false);
   setRed(false);
  }
 }
 public boolean isRed() {
  return red;
 }
 public void setRed(boolean red) {
  this.red = red;
  redLight.setOn(red);
  if (red) {
   setGreen(false);
   setOrange(false);
  }

 }
 class Light extends JComponent {
  

  boolean on = false;
  
  public boolean isOn() {
   return on;
  }
  public void setOn(boolean on) {
   this.on = on;
  }
  Color color;
  public Light (Color color) {
   this.color = color;
   
   }
  
  
 
 public void paint (Graphics g) {
   int width = getWidth();
   int height = getHeight();
   int diameter = Math.min(width, height) * 11 / 16;
   int xOffset = (width - diameter) / 2;
   int yOffset = (height - diameter) / 2;
   
   int [] xArray = new int[4];
   int [] yArray = new int[4];
   
   
   
   g.setColor(Color.WHITE);
   g.fillRect(0, 0, width - 1, height -1);
   g.setColor(Color.BLACK);
   g.fillRect(xOffset, 0, diameter - 1, height -1);
   g.setColor(Color.WHITE);
   g.drawRect(xOffset, 0, diameter - 1, height -1);
   
   g.setColor(Color.BLACK);
   xArray[0] = 0;
   yArray[0] = yOffset;
   xArray[1] = xOffset / 2;
   yArray[1] = yOffset + diameter;
   xArray[2] = xOffset;
   yArray[2] = yArray[1];
   xArray[3] = xOffset;
   yArray[3] = yArray[0];
   g.fillPolygon(xArray, yArray, 4);
   int offset = 2 * xOffset + diameter; 
   xArray[0] = offset;
   xArray[1] = offset - xOffset / 2;
   xArray[2] = offset - xOffset;
   xArray[3] = xArray[2];
   g.fillPolygon(xArray, yArray, 4);
   
   
   if (on) {
     g.setColor(color);
   }
   else {
    g.setColor(Color.LIGHT_GRAY);
   }
   g.fillOval(xOffset, yOffset, diameter - 1, diameter -1);
   
   g.setColor(Color.BLACK);
   g.drawOval(xOffset, yOffset, diameter - 1, diameter -1);
   //g.drawRect(0, 0, width - 1, height -1);
 }
 }
 
 public static void main (String [] ignore) {
  TrafficLight stopLight = new TrafficLight();
  stopLight.setGreen(true);
  stopLight.setOrange(true);
  JFrame frame = new JFrame();
  frame.getContentPane().add(stopLight);
  frame.pack();
  frame.show();
 }
}
