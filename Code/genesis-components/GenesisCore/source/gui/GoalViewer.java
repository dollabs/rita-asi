package gui;

import viz.gifs.PictureAnchor;

import java.awt.*;

import javax.swing.*;




import connections.*;
import connections.Ports;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;

/*
 * Created on Jul 14, 2007 @author phw
 */
public class GoalViewer extends PictureViewer {
	Ports ports;

	private String action = "";

	private String actor = "";

	public GoalViewer() {
		super();
		setBorder(BorderFactory.createLineBorder(Color.BLACK));
	}

	public void setState(String state, String actor, String action) {
		setActor(actor);
		setAction(action);
		if (state != null && state.equals("want")) {
			setImage(new ImageIcon(PictureAnchor.class.getResource("thumbUp.jpg")));
		}
		else if (state != null && state.equals("notWant")) {
			setImage(new ImageIcon(PictureAnchor.class.getResource("thumbDown.jpg")));
		}

	}

	private void setActor(String actor) {
		this.actor = actor;
	}

	private void setAction(String action) {
		this.action = action;
	}

	public void view(Object input) {
		if (!(input instanceof Entity)) {
			return;
		}
		Entity t = (Entity) input;
		if (t.isAPrimed(Markers.GOAL_MARKER) && t.relationP()) {
			Relation r = (Relation) t;
			String action = "?";
			if (r.getObject().isAPrimed(Markers.ROLE_MARKER)) {
				Sequence s = (Sequence) (r.getObject());
				for (Entity x : s.getElements()) {
					if (x.functionP() && x.isAPrimed(Markers.OBJECT_MARKER)) {
						action = x.getSubject().getType();
					}
				}
			}
			if (r.isA("not")) {
				setState("notWant", r.getSubject().getType(), action);
			}
			else {
				setState("want", r.getSubject().getType(), r.getObject().getType());
			}
		}
		setTruthValue(input);
	}

	public void paint(Graphics g) {
		super.paint(g);
		int w = getWidth();
		int h = getHeight();
		Font f = g.getFont();
		int fontSize = w / 10;
		g.setFont(new Font(f.getName(), f.getStyle(), fontSize));
		FontMetrics fontMetrics = g.getFontMetrics();
		int fOffset = fontMetrics.getDescent();
		int wActor = fontMetrics.stringWidth(actor);
		int wAction = fontMetrics.stringWidth(action);
		g.drawString(actor, 5, h - fOffset);
		g.drawString(action, w - wAction - 5, h - fOffset);
	}

	public static void main(String[] args) {
		GoalViewer viewer = new GoalViewer();
		JFrame frame = new JFrame("Testing");
		frame.getContentPane().add(viewer);
		frame.setBounds(100, 100, 400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		Relation t = new Relation("goal", new Entity("may"), new Entity("rug"));
		t.addType("want");
		t.addType("not", "feature");
		viewer.view(t);
	}

}