package gui;

import viz.gifs.PictureAnchor;

import java.awt.Color;

import javax.swing.*;


import connections.Ports;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;
import generator.RoleFrames;

/*
 * Created on Jul 14, 2007 @author phw
 */
public class MoodViewer extends PictureViewer {
	Ports ports;

	public MoodViewer() {
		super();
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	public void setState(String state) {
		if (state != null && state.equals(Markers.POSITIVE)) {
			setImage(new ImageIcon(PictureAnchor.class.getResource("mood/happy.png")));
		}
		else if (state != null && state.equals(Markers.NEGATIVE)) {
			setImage(new ImageIcon(PictureAnchor.class.getResource("mood/sad.png")));
		}
		else if (state != null && state.equals(Markers.ANGRY)) {
			setImage(new ImageIcon(PictureAnchor.class.getResource("mood/angry.png")));
		}
	}

	public void view(Object input) {
		if (!(input instanceof Entity)) {
			return;
		}
		Entity t = (Entity) input;
		if (t.isAPrimed(Markers.MENTAL_STATE_MARKER) && t.relationP()) {

			Relation r = (Relation) t;

			Entity e = RoleFrames.getObject(r);

			if (e.isAPrimed(Markers.MENTAL_STATE)) {
				setState(e.getType());
			}
		}
		setTruthValue(input);
	}

	public static void main(String[] args) {
		MoodViewer viewer = new MoodViewer();
		JFrame frame = new JFrame("Testing");
		frame.getContentPane().add(viewer);
		frame.setBounds(100, 100, 400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		Entity t = new Entity(Markers.MENTAL_STATE);
		t.addType("negative");
		Relation r = new Relation(Markers.MENTAL_STATE_MARKER, new Entity(), t);
		viewer.view(r);
	}

}

/**
 * Amusement<br>
 * Anger<br>
 * Contempt<br>
 * Contentment<br>
 * Disgust<br>
 * Embarrassment<br>
 * Excitement<br>
 * Fear<br>
 * Guilt<br>
 * Happiness<br>
 * Pleasure<br>
 * Pride<br>
 * Relief<br>
 * Sadness<br>
 * Satisfaction<br>
 * Shame<br>
 * Surprise
 */
