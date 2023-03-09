package gui.panels;

import java.awt.*;

import javax.swing.*;



/*
 * Created on Jan 10, 2006
 * @author Patrick
 */

public class SquaringPanel extends BorderingPanel {
 
 protected LocalLayout layout = new LocalLayout(10);

 public SquaringPanel() {
  super();
  setLayout(layout);
 }

 public SquaringPanel(Component center) {
  this();
  add(center);
 }

 public void setBorderPercent(int i) {layout.setBorderPercent(i);} 
 
 class LocalLayout extends BorderedFrameLayout {
   
   public LocalLayout(int i) {super(i);}
    
    public void layoutContainer(Container parent) {
      synchronized (parent.getTreeLock()) {
       if (parent.getComponents().length == 0) {return;}
        int w = parent.getWidth();
        int h = parent.getHeight();
        int size = (100 - borderPercent) * Math.min(w, h) / 100;
        // Log.debug("Squaring panel data: " + borderPercent + ", " + size + ", " + Math.min(w, h));
        parent.getComponent(0).setBounds((w - size) / 2, (h - size) / 2, size, size);
      }
    }
  }
  
  public static void main(String [] ignore) {
    JFrame frame = new JFrame();
    SquaringPanel bf = new SquaringPanel();
    bf.setBackground(Color.WHITE);
    bf.setBorderPercent(20);
    JPanel panel = new JPanel();
    panel.setBackground(Color.RED);
    bf.add(panel);
    // frame.getContentPane().setLayout(new GridLayout(1, 1));
    frame.getContentPane().add(bf);
    frame.setBounds(0, 0, 800, 500);
    frame.show();
  }
  



}
