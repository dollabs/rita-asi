package gui.panels;

import java.awt.*;



public class BorderedFrameLayout implements LayoutManager {
  
  protected int borderPercent = 10;
  
  public BorderedFrameLayout() {super();}
  
  public BorderedFrameLayout(int i) {this(); borderPercent = i;}

  public void setBorderPercent(int i) {borderPercent = i;

  }
  
  public void layoutContainer(Container parent) {
    synchronized (parent.getTreeLock()) {
      if (parent.getComponents().length == 0) {return;}
      int w = parent.getWidth();
      int h = parent.getHeight();
      int size = Math.min(w, h);
      int offset = size * borderPercent / 100;
      // Log.warning("Dimensions: " + parent.getWidth() + ", " + parent.getHeight());
      parent.getComponent(0).setBounds(offset, offset, w - 2 * offset, h - 2 * offset);
    }
  }
  
  public void removeLayoutComponent(Component component) {
    
  }
  
  public void addLayoutComponent(String arg0, Component component) {
    
  }
  
  public Dimension minimumLayoutSize(Container parent) {
    if (parent.getComponents().length == 0) {return null;}
    return parent.getComponent(0).getMinimumSize();
  }
  
  public Dimension preferredLayoutSize(Container parent) {
    if (parent.getComponents().length == 0) {return null;}
    return parent.getComponent(0).getPreferredSize();
  }
}
  
