package subsystems.blocksWorld.views;

import java.awt.*;
import java.util.Vector;

import javax.swing.*;

import utils.Mark;

/*
 * Created on Sep 12, 2005 @author Patrick
 */

public class AndOrView extends JPanel {
  
  private int interRowSpace = 90;
  
  private int nodeHeight = 20;
  
  private int nodeWidth = 100;
  
  private int nodeSeparation = 10;
  
  public AndOrView() {
    setBackground(Color.WHITE);
    setMinimumSize(new Dimension(0, 0));
    setPreferredSize(new Dimension(400, 400));
   
    setLayout(new MyLayoutManager());
  }
  
   
  public int maxDepth() {
    return maxDepth(root, 0);
  }
  
  public int maxDepth(Node n, int level) {
    Vector children = n.getChildren();
    int result = level + 1;
    if (children.size() == 0) {
      return result;
    }
    for (int i = 1; i < children.size(); ++i) {
      Node child = (Node) (children.get(i));
      result = Math.max(result, maxDepth(child, level + 1));
    }
    return result;
  }
  
  public int maxWidth() {
    return Math.max(1, maxWidth(root.getChildren(), 1));
  }
  
  public int maxWidth(Vector nodes, int result) {
    result = Math.max(nodes.size(), result);
    Vector nextLevelNodes = new Vector();
    for (int i = 0; i < nodes.size(); ++i) {
      Node node = (Node) (nodes.get(i));
      Vector children = node.getChildren();
      nextLevelNodes.addAll(children);
    }
    if (nextLevelNodes.size() > 0) {
      return maxWidth(nextLevelNodes, result);
    }
    return result;
    
  }
  
  public Vector getOffsetVector() {
    return offsetVector;
  }
  
  public void setOffsetVector(Vector offsetVector) {
    this.offsetVector = offsetVector;
  }
  
  public Node getRoot() {
    return root;
  }
  
  public void setRoot(Node root) {
    if (root == null) {return;}
    this.root = root;
    removeAll();
    addNode(root);
  }
  
  private void addNode(Node node) {
		// Mark.say("Adding", node);
    add(node);
    Vector children = node.getChildren();
    for (int i = 0; i < children.size(); ++i) {
      Node n = (Node) (children.get(i));
      addNode(n);
    }
  }
  
  private Node root;
  
  private Vector offsetVector = new Vector();
  
  public void paintComponent(Graphics g) {
    if (root == null) {return;}
    super.paintComponent(g);
    int level = 0;
    Vector nodes = new Vector();
    nodes.add(root);
    int yOffset = 0;
    int treeWidth = Math.max(5, maxWidth());
    int treeHeight = Math.max(5, maxDepth());
    double xScale = (double) getWidth() / (treeWidth * nodeWidth + (treeWidth - 1) * nodeSeparation);
    double yScale = (double) getHeight() / (treeHeight * interRowSpace + nodeHeight);
    while (nodes.size() > 0) {
      Vector next = new Vector();
      for (int i = 0; i < nodes.size(); ++i) {
        Node node = (Node) (nodes.get(i));
        Vector v = node.getChildren();
        int locationOfFirstChild = next.size();
        
        int x1 = i * (nodeWidth + nodeSeparation) + nodeWidth / 2;
        int y1 = yOffset + nodeHeight / 2;
        int y2 = y1 + interRowSpace;
        
        x1 = (int)(xScale * x1);
        y1 = (int)(yScale * y1);
        y2 = (int)(yScale * y2);
        
        for (int j = 0; j < v.size(); ++j) {
          
          int x2 = (j + locationOfFirstChild) * (nodeWidth + nodeSeparation) + nodeWidth / 2;
          x2 = (int)(xScale * x2);
          g.drawLine(x1, y1, x2, y2);
        }
        next.addAll(node.getChildren());
      }
      nodes = next;
      yOffset += interRowSpace;
    }
  }
  
  class MyLayoutManager implements LayoutManager {
    public void removeLayoutComponent(Component component) {
    }
    
    public void addLayoutComponent(String string, Component arg1) {
    }
    
    public Dimension minimumLayoutSize(Container container) {
      return null;
    }
    
    public Dimension preferredLayoutSize(Container container) {
      return null;
    }
    
    public void layoutContainer(Container container) {
      if (root == null) {return;}
      int level = 0;
      Vector nodes = new Vector();
      nodes.add(root);
      int yOffset = 0;
      while (nodes.size() > 0) {
        int treeWidth = Math.max(5, maxWidth());
        int treeHeight = Math.max(5, maxDepth());
        
        double xScale = (double) getWidth() / (treeWidth * nodeWidth + (treeWidth - 1) * nodeSeparation);
        double yScale = (double) getHeight() / (treeHeight * interRowSpace + nodeHeight);
        
				// Mark.say("Scales " + xScale + "/" + yScale);
        
        int deltaX = (int) (xScale * (nodeWidth + nodeSeparation));
        int y = (int) (yScale * yOffset);
        int panelWidth = (int)(xScale * nodeWidth);
        int panelHeight = (int)(yScale * nodeHeight);
        
				// Mark.say("Panel " + panelWidth + "/" + panelHeight);
        
        
        Vector next = new Vector();
        for (int i = 0; i < nodes.size(); ++i) {
          Node node = (Node)(nodes.get(i));
					// Mark.say("Setting bounds for", node, i + deltaX + ", " + y + ", " + panelWidth + ", " +
					// panelHeight);
          node.setBounds(i * deltaX, y, panelWidth, panelHeight);
          
          
          next.addAll(node.getChildren());
        }
        nodes = next;
        yOffset += interRowSpace;
      }
      
    }
    
  }
  
  public static void main(String[] args) {
    AndOrView view = new AndOrView();
    Node node1 = new Node();
    node1.setText("Root");
    Node node2 = new Node();
    node2.setText("Node2");
    Node node3 = new Node();
    node3.setText("Node3");
    Node node4 = new Node();
    node4.setText("Node4");
    node1.addChild(node2);
    node1.addChild(node3);
    node3.addChild(node4);
    view.setRoot(node1);
		Mark.say("Depth, width: " + view.maxDepth() + ", " + view.maxWidth());
    JFrame frame = new JFrame("Test");
    frame.getContentPane().add(view, BorderLayout.CENTER);
    // frame.pack();
    frame.setBounds(0, 0, 800, 600);
    frame.show();
    view.revalidate();
  }
}

