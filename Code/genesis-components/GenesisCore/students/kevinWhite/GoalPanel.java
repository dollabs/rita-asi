package kevinWhite;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import connections.Connections;
import connections.WiredViewer;
import constants.Markers;
import frames.entities.Entity;

@SuppressWarnings("serial")
public class GoalPanel extends WiredViewer {

	private GroupLayout goalLayout;

	private SequentialGroup goalHoriz;

	private ParallelGroup goalVert;

	private GenesisPanel subjectPanel;

	private GenesisPanel objectivePanel;

	private GenesisPanel linkPanel;

	/**
	 * The GoalPanel consists of 3 panels of equal width and height that display images and text. The text and image it
	 * displays is dependent on the input given to the Genesis system.
	 */
	public GoalPanel() {
		// set border and background
		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		// initialize Panels
		subjectPanel = new GenesisPanel("");
		linkPanel = new GenesisPanel("goal", false);
		objectivePanel = new GenesisPanel("");

		linkPanel.setVisible(false);

		// create GroupLayout
		goalLayout = new GroupLayout(this);
		this.setLayout(goalLayout);

		// horizontal group
		goalHoriz = goalLayout.createSequentialGroup();
		// vertical group
		goalVert = goalLayout.createParallelGroup();

		// add panels
		goalHoriz.addComponent(subjectPanel);
		goalVert.addComponent(subjectPanel);

		goalHoriz.addComponent(linkPanel);
		goalVert.addComponent(linkPanel);

		goalHoriz.addComponent(objectivePanel);
		goalVert.addComponent(objectivePanel);

		goalLayout.setHorizontalGroup(goalHoriz);
		goalLayout.setVerticalGroup(goalVert);
		Connections.getPorts(this).addSignalProcessor("process");
	}

	private Entity getObjectRole(Entity roles) {
		for (Entity t : roles.getElements()) {
			if (t.isA(Markers.OBJECT_MARKER)) {
				return t;
			}
		}
		return null;
	}

	@Override
	public void view(Object object) {

		if (object instanceof Entity) {
			Entity t = (Entity) object;
			linkPanel.setDesirability(!t.getFeatures().contains("not"));
			subjectPanel.displayText(t.getSubject().getType());
			Entity roles = t.getObject();
			Entity objectRole = getObjectRole(roles);
			// Drip pan; don't know what it does, don't know why it could be null
			if (objectRole != null) {
				Entity action = objectRole.getSubject();
				String name = action.getType();
				objectivePanel.displayText(name);
				linkPanel.setVisible(true);
			}
		}
	}
}