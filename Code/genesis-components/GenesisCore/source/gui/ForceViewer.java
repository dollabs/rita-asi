package gui;
import java.awt.*;
import java.awt.geom.AffineTransform;
import javax.swing.*;
import connections.Connections;
import connections.WiredBox;
import constants.Markers;
import frames.ForceFrame;
import frames.entities.Entity;
import frames.entities.Relation;
/*
 * Created on Jun 13, 2007
 * @author phw, ryscheng 
 */
public class ForceViewer extends NegatableJPanel {
	
	private String 	agonistName="";
	private String	antagonistName="";
	private boolean circleOnLeft = true;
	private boolean arrowheadInCircle = true;
	private boolean plusInRectangle = true;
	private boolean simple = true;
	private boolean antQuestion = false;
	private boolean agoQuestion = false;
	int DOT = 0, ARROW = 1, QUESTION = 2;
	int lineMode = DOT;
	private boolean viewable = false;
	
	private int diameter = 80;
 
	public void setAgonistName(String name){
		agonistName=name;
	}
	
	public void setAntagonistName(String name){
		antagonistName=name;
	}
	
	public String getAgonistName(){
		return agonistName;
	}
	
	public String getAntagonistName(){
		return antagonistName;
	}
	
	public void unSetAgoQuestion() {
		agoQuestion = false;
	}
	public void unSetAntQuestion() {
		antQuestion = false;
	}
	
	public void setAgoQuestion() {
		agoQuestion = true;
		circleOnLeft = true;
		setLineMode();
	}
	
	public void setAntQuestion() {
		antQuestion = true;
		circleOnLeft = true;
		setLineMode();
	}
	
	public void setArrow() {
		circleOnLeft = true;
		arrowheadInCircle = true;
		setLineMode();
	}
 
	public void setDot() {
		circleOnLeft = false;
		arrowheadInCircle = false;
		setLineMode();
	}
 
	public void setPlus() {
		plusInRectangle = true;
		//circleOnLeft = true;
		setLineMode();
	}
 
	public void setMinus() {
		plusInRectangle = false;
		//circleOnLeft = false;
		setLineMode();
	}
 
 	public void setSimple() {
 		simple = true;
 		setLineMode();
 	}
 	
 	public void setCompound() {
 		simple = false;
 		setLineMode();
 	}
 	
 	private void setLineMode () {
 		if (antQuestion || agoQuestion){
 			lineMode = QUESTION;
 		}
 		else if (plusInRectangle) {
 			if (simple) {
 				if (arrowheadInCircle) {
 					lineMode = DOT;
 				} 
 				else {
 					lineMode = ARROW;
 				}
 			} 
 			else {
 				if (arrowheadInCircle) {
 					lineMode = DOT;
 				} 
 				else {
 					lineMode = ARROW;
 				}
 			}
 		} 
 		else {
 			if (simple) {
 				if (arrowheadInCircle) {
 					lineMode = ARROW;
 				} 
 				else {
 					lineMode = DOT;
 				}
 			} 
 			else {
 				if (arrowheadInCircle) {
 					lineMode = ARROW;
 				} 
 				else {
 					lineMode = DOT;
 				}
 			}
 		}
 		//setViewable(true);
 		//repaint();
 	}
 
 	public ForceViewer () {
 		setOpaque(true);
 		setBackground(Color.WHITE);
 	}
 
 	//public void paintComponent (Graphics x) {
 	public void paintComponent (Graphics x) {
 		super.paintComponent(x);
 		

 		Graphics2D g = (Graphics2D)x;
 		int width = getWidth();
 		int height = getHeight();
 		if (width == 0 || height == 0) {return;}
 		
 		g.drawRect(0, 0, width-1, height-1);
 		if (!isViewable()) {return;}
 		int offsetX = 0;
 		int offsetY = 5;
 		int circleX = offsetX;
 		int separation = 10;
 		int rectX = circleX + diameter + separation;
 		
 		if (!circleOnLeft) {
 			rectX = offsetX;
 			circleX = rectX + separation + diameter / 3;
 		}
  
 		int designSize = 160;
 		double multiplier = 1.0;
 		double tX = 0.0;
 		double tY = 0.0;
 		if (height > width) {
 			multiplier = (double) width / designSize;
 		}
 		else {
 			multiplier = (double) height / designSize;
 		}
 		
 		
 		tY = height - multiplier * (diameter + 2 * separation);
 		tY /= 2.0;
 		
 		tX = width - multiplier * (diameter + separation + diameter / 3);
 		tX /= 2.0;
 		
 		/**
 		FontMetrics fm = g.getFontMetrics(); 
 		g.drawString(getAntagonistName(),2,fm.getAscent());
 		g.drawString(getAgonistName(),(width/2),fm.getAscent());
 		**/
 		
 		AffineTransform transform = g.getTransform();
 		transform.translate(tX, tY);
 		transform.scale(multiplier, multiplier);
 	
 		g.setTransform(transform);
 		//Label the Agonist and Antagonist
 		Font font = new Font(null,Font.PLAIN,25);
 		g.setFont(font);
 		FontMetrics fm = g.getFontMetrics(); 
 		g.drawString(getAntagonistName(),rectX+(diameter/3 - fm.stringWidth(getAntagonistName()))/2,0);
 		g.drawString(getAgonistName(),(circleX+(diameter-fm.stringWidth(getAgonistName()))/2),0);
 		
 		g.drawOval(circleX, offsetY, diameter, diameter);
 		g.drawRect(rectX, offsetY, diameter / 3, diameter);
 		int lineY = offsetY + diameter + 2 * separation;
 		int lineWidth = diameter + separation + diameter/3;
 		g.drawLine(offsetX, lineY, offsetX + lineWidth, lineY);
  
 		int circleCenterX = circleX + diameter / 2;
 		int circleCenterY = offsetY + diameter / 2;
 		
 		if (agoQuestion){
 			g.drawString("?",circleCenterX - (fm.stringWidth("?")/2),circleCenterY + (fm.getAscent()/2));
 		}
 		else if (arrowheadInCircle) {
 			this.drawArrowhead(g, circleCenterX, circleCenterY);
 		}	
 		else {
 			this.drawDot(g, circleCenterX, circleCenterY);
 		}
 		
 		int midLine = (offsetX + lineWidth) / 2;
  
 		if (lineMode == DOT) {
 			drawDot(g, midLine, lineY);
 		}
 		else if (lineMode == ARROW) {
 			drawArrowhead(g, midLine, lineY);
 		}
 		else if (lineMode == this.ARROW) {
 			drawArrowhead(g, midLine + 10, lineY);
 			drawDot(g, midLine - 10, lineY);
 			drawSlash(g, midLine, lineY);
 		}
 		else if (lineMode == this.DOT) {
 			drawDot(g, midLine + 10, lineY);
 			drawArrowhead(g, midLine - 10, lineY);
 			drawSlash(g, midLine, lineY);
 		}
 		else if (lineMode == QUESTION) {
 			g.drawString("?", midLine, lineY - 2);
 		}
 		
 		if (antQuestion){
 			g.drawString("?",rectX + diameter / 6, offsetY + 10 + fm.getAscent());
 		}
 		else if (plusInRectangle) {
 			drawPlusOrMinus(g, rectX + diameter / 6, offsetY + 10, true);
 		}
 		else {
 			drawPlusOrMinus(g, rectX + diameter / 6, offsetY + 10, false);
 		} 

 	}
 
 	private void drawPlusOrMinus (Graphics g, int x, int y, boolean plus) {
 		g.drawLine(x - 5, y, x + 5, y);
 		if (plus) {
 			g.drawLine(x, y - 5, x, y + 5);
 		}
 	}
 	
 	private void drawDot (Graphics g, int x, int y) {
 		int radius = 6;
 		g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
 	}
 
 	private void drawArrowhead (Graphics g, int x, int y) {
 		int arrowheadHeight = 5;
 		int arrowheadWidth = 8;
 		g.drawLine(x - arrowheadWidth / 2, y - arrowheadHeight, x + arrowheadWidth / 2, y);
 		g.drawLine(x - arrowheadWidth / 2, y + arrowheadHeight, x + arrowheadWidth / 2, y);
 	}
 
 	public void drawSlash (Graphics g, int x, int y) {
 		g.drawLine(x + 5, y - 8, x - 5, y + 8);
 	}
 	
 	public void clearData() {
 		setViewable(false);
 	}
 	
 	public void setParameters(ForceFrame frame) {
 		Relation thing = (Relation) frame.getThing();
 		setParameters(thing);
 	}
 	
 	public void setParameters(Relation thing) {
 		/**
 		System.err.println("Entering setParameters.");
 		System.err.println("Agonist Agent: " + ForceFrame.getAgonistName(thing));
 		System.err.println("Antagonist Agent: " + ForceFrame.getAntagonistName(thing));
 		System.err.println("Agonist Tendency: " + ForceFrame.getAgonistTendency(thing));
 		System.err.println("Antagonist Tendency: " + ForceFrame.getAntagonistTendency(thing));
 		System.err.println("Agonist Strength: " + ForceFrame.getAgonistStrength(thing));
 		System.err.println("Antagonist Strength: " + ForceFrame.getAntagonistStrength(thing));
 		**/
 		
 		setAgonistName(ForceFrame.getAgonistName(thing));
 		setAntagonistName(ForceFrame.getAntagonistName(thing));
 		unSetAgoQuestion();
 		unSetAntQuestion();
 		
 		if (ForceFrame.getAgonistTendency(thing).equals("active")) {
 			setArrow();
 		} else if(ForceFrame.getAgonistTendency(thing).equals("unknown")){
 			setAgoQuestion();
 		} else {
 			setDot();
 		}
 		
 		if (ForceFrame.getAntagonistStrength(thing).equals("strong")) {
 			setSimple();
 			setPlus();
 		} else if (ForceFrame.getAntagonistStrength(thing).equals("weak")) {
 			setSimple();
 			setMinus();
 		} else if (ForceFrame.getAntagonistStrength(thing).equals("grow")) {
 			setCompound();
 			setPlus();
 		} else if (ForceFrame.getAntagonistStrength(thing).equals("fade")) {
 			setCompound();
 			setMinus();
 		} else if (ForceFrame.getAntagonistStrength(thing).equals("unknown")) {
 			setAntQuestion();
 		}
 		
 		setViewable(true);
 		repaint();
 	}
 
 	public void setParameters(Object object) {
 		// tbd, and remove stubs below;
 		System.err.println("Entering setParameters without proper force frame");
 	}
 
 public void view(Object signal) {
	 if (signal instanceof Relation) {
		 Relation force = (Relation) signal;
		 if (force.isA(ForceFrame.FRAMETYPE)) {
			 ForceViewer.this.setParameters(force);
		 }
	 }
	 setTruthValue(signal);
 }
 
 public static void main(String[] args) {
  ForceViewer view = new ForceViewer();
  JFrame frame = new JFrame();
  frame.getContentPane().add(view);
  frame.setBounds(0, 0, 200, 200);
  frame.setVisible(true);
  Entity t = ForceFrame.getMap().get("The ball kept rolling because the wind blew on it.");
  t.addFeature(Markers.NOT);
  view.view(t);
  
 }
 public boolean isViewable() {return viewable;}
 
 public void setViewable(boolean viewable) {
  this.viewable = viewable;
 }
}
