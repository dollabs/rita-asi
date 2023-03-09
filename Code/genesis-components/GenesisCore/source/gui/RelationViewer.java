package gui;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;


import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;
import generator.RoleFrames;

/*
 * Created on May 23, 2009
 * @author phw
 */

public class RelationViewer extends NegatableJPanel {

	String part = "";

	String type = "";

	JLabel innerPanel = new JLabel("");

	TitledBorder border = BorderFactory.createTitledBorder("");

	public RelationViewer() {
		setOpaque(false);
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		innerPanel.setBorder(border);
		this.add(innerPanel);
		innerPanel.setHorizontalAlignment(SwingConstants.CENTER);
	}

	public void view(Object signal) {
		if (signal instanceof Entity) {
			Entity input = (Entity) signal;
			if (input.relationP()) {
				type = ((Relation) input).getSubject().getType();
				if (RoleFrames.isRoleFrame(input)) {
					part = RoleFrames.getObject(input).getType();
				}
				else {
					part = ((Relation) input).getObject().getType();
				}
				innerPanel.setText(part);
				border.setTitle(type);
			}
		}
		setTruthValue(signal);
	}

	public static void main(String[] args) {
		RelationViewer reader = new RelationViewer();
		JFrame frame = new JFrame("Testing");
		frame.getContentPane().add(reader, BorderLayout.CENTER);
		frame.setBounds(100, 100, 200, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		Relation thing = new Relation(Markers.BODY_PART_MARKER, new Entity("bird"), new Entity("wings"));
		System.out.println(thing);
		reader.view(thing);
	}

}
