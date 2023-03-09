package gui;

import java.awt.Color;
import java.util.Vector;

import javax.swing.*;

import utils.Mark;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Aug 14, 2008
 * @author phw
 */

public class RoleViewer extends NegatableJPanel {

	JLabel guts;

	public RoleViewer() {
		guts = new JLabel();
		this.add(guts);
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		this.setBackground(Color.WHITE);
	}

	public void view(Object o) {
		if (o instanceof Relation) {
			Relation t = (Relation) o;
			if (t.relationP() && t.getObject().sequenceP(Markers.ROLE_MARKER )) {
				Vector<Entity> roles = t.getObject().getElements();
				Entity object = theObject(roles);
				roles = withoutObject(roles);
				// Thing o2 = t.getObject();
				// if (o2.sequenceP()) {
				// Sequence s = (Sequence)o2;
				String table = "";
				table += "<html>";
				table += "<table>";
				table += "<tr><td>actor:</td><td>" + t.getSubject().getType() + "</td></tr>";
				table += "<tr><td>action:</td><td>" + t.getType() + "</td></tr>";
				if (object != null) {
					table += "<tr><td>object:</td><td>" + object.getType() + "</td></tr>";
				}
				for (Entity element : roles) {
					table += "<tr><td>" + element.getType() + ":</td><td>" + element.getSubject().getType() + "</td></tr>";
				}
				// for (Iterator i = s.getElements().iterator(); i.hasNext();) {
				// Object e = i.next();
				// if (((Thing)e).functionP()) {
				// Derivative d = (Derivative)e;
				// String label = d.getType();
				// String value = d.getSubject().getType();
				// if (!"null".equals(value)) {
				// table += "<tr><td>" + label + ":</td><td>" + value +
				// "</td></tr>";
				// }
				// }
				// }
				table += "</table>";
				table += "</html>";
				guts.setText(table);
				// }
			}
			else {
				Mark.err("Role viewer got frame which is not a role frame", t.asString());
			}
		}
	}

	private Vector<Entity> withoutObject(Vector<Entity> elements) {
		Vector<Entity> result = new Vector<Entity>();
		if (elements != null) {
			for (Entity t : elements) {
				if (!t.functionP(Markers.OBJECT_MARKER)) {
					result.add(t);
				}
			}
		}
		return result;
	}

	private Entity theObject(Vector<Entity> elements) {
		if (elements != null) {
			for (Entity t : elements) {
				if (t.functionP(Markers.OBJECT_MARKER)) {
					return t.getSubject();
				}
			}
		}
		return null;
	}

	public static void main(String[] ignore) {
		JFrame f = new JFrame();
		RoleViewer v = new RoleViewer();
		v.view(new Object());
		f.getContentPane().add(v);
		f.setSize(300, 200);
		f.setVisible(true);
	}
}