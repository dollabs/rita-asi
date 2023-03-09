package gui.panels;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.BevelBorder;

/*
 * @copyright Ascent Technology, Inc, 2005
 */
public class TitledJPanelBorder extends BevelBorder {

 private static final long serialVersionUID = 1L;

 public TitledJPanelBorder() {
  super(BevelBorder.RAISED);
 }

 protected void paintRaisedBevel(Component c, Graphics g, int x, int y, int width, int height) {
  Color oldColor = g.getColor();
  int h = height;
  int w = width;

  g.translate(x, y);

  g.setColor(getHighlightOuterColor(c));
  // g.setColor(Color.RED);
  // Left
  if (l != 0) {g.drawLine(0, 0, 0, h - 2);}
  // Top
  if (t != 0) {g.drawLine(1, 0, w - 2, 0);}

  g.setColor(getHighlightInnerColor(c));
  // g.setColor(Color.YELLOW);
  // Left
  if (l != 0) {g.drawLine(1, 1, 1, h - 3);}
  // Top
  if (t != 0) {g.drawLine(2, 1, w - 3, 1);}

  g.setColor(getShadowOuterColor(c));
  // g.setColor(Color.BLUE);
  // Bottom
  if (b != 0) {g.drawLine(0, h - 1, w - 1, h - 1);}
  // Right
  if (r != 0) {g.drawLine(w - 1, 0, w - 1, h - 2);}

  g.setColor(getShadowInnerColor(c));
  // g.setColor(Color.GREEN);
  // Bottom
  if (b != 0) {g.drawLine(1, h - 2, w - 2, h - 2);}
  // Right
  if (r != 0) {g.drawLine(w - 2, 1, w - 2, h - 3);}

  g.translate(-x, -y);
  g.setColor(oldColor);

 }
 
 int l = 2, r = 2, t = 2, b = 2;
  
 public Insets getBorderInsets(Component c) {return new Insets(t, l, b, r);}
 
 /** 
  * Reinitialize the insets parameter with this Border's current Insets. 
  * @param c the component for which this border insets value applies
  * @param insets the object to be reinitialized
  */
 public Insets getBorderInsets(Component c, Insets insets) {
   insets.left = l; insets.top = t; insets.right = r; insets.bottom = b;
   return insets;
 }
 
 public void includeBorders (String border) {
  l = r = t = b = 0;
  if (border.indexOf('l') >= 0 || border.indexOf('L') >= 0) {l = 2;}
  if (border.indexOf('r') >= 0 || border.indexOf('R') >= 0) {r = 2;}
  if (border.indexOf('t') >= 0 || border.indexOf('T') >= 0) {t = 2;}
  if (border.indexOf('b') >= 0 || border.indexOf('B') >= 0) {b = 2;}
 }
 

 public void setBorders(String border) {
  if (border.indexOf('l') >= 0 || border.indexOf('L') >= 0) {l = 0;}
  if (border.indexOf('r') >= 0 || border.indexOf('R') >= 0) {r = 0;}
  if (border.indexOf('t') >= 0 || border.indexOf('T') >= 0) {t = 0;}
  if (border.indexOf('b') >= 0 || border.indexOf('B') >= 0) {b = 0;}
 }


}
