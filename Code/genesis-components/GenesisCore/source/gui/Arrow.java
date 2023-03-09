package gui;
import java.awt.*;
import javax.swing.*;
import connections.*;
/*
 * Created on Oct 9, 2006
 * @author phw
 */
public class Arrow extends JComponent implements WiredBox {
 
 int taps = 0;
 public Arrow() {
  super();
  Connections.getPorts(this).addSignalProcessor("setInput");
 }
 private String text;
 
 public void paintComponent(Graphics g) {
  super.paintComponent(g);
  int w = getWidth();
  int h = getHeight();
  if (w == 0 || h == 0) {
   return;
  }
  int[] x = new int[7];
  int[] y = new int[7];
  x[0] = 0;
  y[0] = h * 3 / 12;
  x[1] = 19 * w / 20;
  y[1] = y[0];
  x[2] = x[1];
  y[2] = 5;
  x[3] = w;
  y[3] = h / 2;
  x[4] = x[1];
  y[4] = h - 5;
  x[5] = x[1];
  y[5] = 9 * h / 12;
  x[6] = x[0];
  y[6] = y[5];
  g.setColor(Color.RED);
  g.fillPolygon(x, y, x.length);
  int tapWidth = y[5] - y[0];
  int tapHalfWidth = tapWidth / 2;
  if (taps != 0) {
  int sectionHalfWidth = w / taps / 2;
  for (int i = 0; i < taps; ++i) {
   int tapOrigin = i * w / taps;
   int tapOffset = tapOrigin - tapHalfWidth + sectionHalfWidth;
   g.fillRect(tapOffset, 0, tapWidth, y[0]);
  }
  }
  if (text != null) {
   g.setFont(new Font("Georgia", Font.BOLD, Math.max(14, h/5)));
   g.setColor(Color.WHITE);
  FontMetrics metrics = g.getFontMetrics();
  int stringWidth = metrics.stringWidth(text);
  int stringHeight = metrics.getHeight();
  
  g.drawString(text, 10, (h + stringHeight) / 2);
  
  }
  
//  Font [] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//  
//  for (int i = 0; i < fonts.length; ++i) {
//   System.out.println(fonts[i].getFamily());
//  }
  
 }
 /**
  * @param args
  */
 
  public static void main(String[] args) {
   Arrow arrow = new Arrow();
  
   JFrame frame = new JFrame("Testing");
   frame.getContentPane().add(arrow, BorderLayout.CENTER);
   frame.setBounds(100, 100, 800, 200);
   frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   arrow.setText("Hello world");
   frame.setVisible(true);
 }
 private int getTaps() {
  return taps;
 }
 public void setTaps(int taps) {
  this.taps = taps;
 }
 public String getText() {return text;}
 
 public void setText(String text) {
  this.text = text;
  repaint();
 }
 public void setInput(Object input) {
  if (input instanceof String) {
   setText((String)input);
  }
 }
}
