package gui;

import gui.images.GuiImagesAnchor;

import java.awt.*;
import java.util.*;

import javax.swing.*;


import connections.*;
import connections.Ports;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;

/**
 * Created on January 8, 2008
 * @author Raymond Cheng
 * Adapted from TrajectoryViewer.java
 * 
 * Displays simple transition images if input port sees a recognizable transition
 */
public class TransitionViewer extends NegatableJPanel {

	/**
	 *  Extends WiredPanel: New method for wiring makes plugs obsolete
	 */
	private static final long serialVersionUID = 8425249526548473816L;
	/**
	 * @param role, name, ports, roleFileAssoc
	 */
	String role; 						//Must store a key value from roleFileAssoc
	String name;
	private Ports ports;
	private Hashtable roleFileAssoc;	//Associates a role with the image to display
	
	public TransitionViewer(){
		//Currently contains 5 static values as possible values of the role.
		roleFileAssoc = new Hashtable();
		roleFileAssoc.put("appear","appear.png");
		roleFileAssoc.put("disappear","disappear.png");
		roleFileAssoc.put("increase","increase.png");
		roleFileAssoc.put("decrease","decrease.png");
		roleFileAssoc.put("change","change.png");
		roleFileAssoc.put("notAppear","not_appear.png");
		roleFileAssoc.put("notDisappear","not_disappear.png");
		roleFileAssoc.put("notIncrease","not_increase.png");
		roleFileAssoc.put("notDecrease","not_decrease.png");
		roleFileAssoc.put("notChange","not_change.png");
		
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setOpaque(false);
	}
	
	private void setParameters(String role) {
		this.role = role;
		this.repaint();
	}

	private void clearData() {
		this.role = null;
		this.name = null;
	}
	
	public Ports getPorts() {
		if (this.ports == null) {
			this.ports = new Ports();
		}
		return this.ports;
	}

	public void view (Object signal) {
		//Assume hierarchy = transitionspace(sequence) - ladder(sequence) - transition(derivative)
		if (signal instanceof Sequence) {
			Sequence space = (Sequence) signal;
			if (space.isA("transitionSpace")) {
				Sequence ladder = (Sequence) space.getElement(0);
				Function transition = (Function) ladder.getElement(0);
				setParameters(matchTransitionRole(transition.getTypes()));
			}
		}
		else if (signal instanceof Function) {
			Function transition = (Function) signal;
			setParameters(matchTransitionRole(transition.getTypes()));
		}
		else {
			System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type " + signal.getClass().toString() + ": "
			        + signal + " in TransitionViewer");
		}
		setTruthValue(signal);
	}
	
	

	//Takes a vector of types from a thread, ordered most general first
	//Returns the most specific type in thread that is a key in roleFileAssoc.
	private String matchTransitionRole(Vector types){
		
		for (int i=(types.size()-1);i>=0;i--){
			if (roleFileAssoc.containsKey(types.get(i))){
				return (String)types.get(i);
			}
		}
		return null;
	}
	
	public void paint(Graphics graphics) {
		super.paint(graphics);
		Graphics2D g = (Graphics2D) graphics;
		int height = this.getHeight();
		int width = this.getWidth();
		FontMetrics fm = g.getFontMetrics(); 
		Image displayImage;
		Toolkit toolkit = Toolkit.getDefaultToolkit();		//Used to load images
		MediaTracker mediaTracker = new MediaTracker(this); //Used to track images
		String file=null;
		int imageWidth,imageHeight,x,y;
		double imageProportion;
		int imageTextSeparator = height - 5 - fm.getDescent();
		int imageSpace=20;									//Minimum space from image to border
		int textSpace=5;									//Space from left border for text
		//String fontName = Font.SANS_SERIF;
		String fontName = "Sans_Serif";
		int fontStyle = Font.PLAIN;
		int fontSize = 10;
		Font font = new Font(fontName,fontStyle,fontSize);
	
		if (width <= 0 || height <= 0) {
			return;
		}
		if (this.role == null) {
			return;
		}
		
		g.setFont(font);
		// g.setColor(bgColor);
//		g.fillRect(0, 0, width, height);	//Force background
//		g.drawRect(0, 0, width - 1, height - 1);
		
		//If the role is too large to display, shrink size until it fits
		while(fm.stringWidth(this.role) > (width-textSpace)){
			fontSize--;
			font = new Font(fontName,fontStyle,fontSize);
			g.setFont(font);
			fm = g.getFontMetrics();
		}
		g.drawString(this.role, textSpace, imageTextSeparator);
		
		//Grab the image file associated with the role and display
		file = (String)roleFileAssoc.get(this.role);
		file = new GuiImagesAnchor().get(file);
		if(file!=null){
			if (width > imageTextSeparator){
				imageSpace = imageTextSeparator / 5;
			}
			else {
				imageSpace = width / 5;
			}
			imageWidth = width - (2*imageSpace);
			imageHeight = imageTextSeparator - (2*imageSpace); 
			displayImage = toolkit.createImage(file);
			mediaTracker.addImage(displayImage, 0);
			try{
				mediaTracker.waitForID(0);
			}
			catch (Exception e){
				e.printStackTrace();
			}
			//Image is scaled to window and centered
			imageProportion = ((double) displayImage.getWidth(null))/((double) displayImage.getHeight(null));
			if (((double)imageHeight*imageProportion) > imageWidth){
				imageHeight = (int)((double)imageWidth/imageProportion);
				x = (width - imageWidth)/2;
				y = (imageTextSeparator - imageHeight)/2;
				g.drawImage(displayImage, x, y, imageWidth, imageHeight,  null);
			}
			else{
				imageWidth = (int)((double)imageHeight*imageProportion);
				x = (width - imageWidth)/2;
				y = (imageTextSeparator - imageHeight)/2;
				g.drawImage(displayImage, x, y, imageWidth, imageHeight,  null);
			}
		}
		
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame();
		TransitionViewer view = new TransitionViewer();
		Entity t = new Entity("Ray");
		Function d = new Function("appeared",t);
		
		frame.getContentPane().add(view);
		frame.setBounds(0, 0, 200, 200);
		frame.setVisible(true);

		//Edit this line to test specific roles
		d.addType("increase");
		
		view.view(d);
	}

}
