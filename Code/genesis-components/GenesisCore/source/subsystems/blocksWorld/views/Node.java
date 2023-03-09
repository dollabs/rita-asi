package subsystems.blocksWorld.views;

import java.awt.*;
import java.util.Vector;

import javax.swing.JButton;

/*
 * Created on Sep 12, 2005
 * @author Patrick
 */

public class Node extends JButton {
  private Vector children = new Vector();
  private String text;
  
  public Node (String string) {super(string);}
  
  public String getText() {
   return text;
  }
  public void setText(String text) {
   this.text = text;
  }
  public Node () {
   setBackground(Color.YELLOW);
   setOpaque(true);
  }
 
  public Vector getChildren() {
   return children;
  }
  public void setChildren(Vector children) {
   this.children = children;
  }
  
  public void addChild(Node node) {children.add(node);}
  
  public String toString() {return getText();}
  
  public void paint (Graphics g) {
   super.paint(g);
   // Log.warning("Painting button " + getWidth() + "/" + getHeight());
  }

  
}
