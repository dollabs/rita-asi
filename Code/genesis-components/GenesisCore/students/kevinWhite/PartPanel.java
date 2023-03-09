package kevinWhite;

import java.awt.Color;

import javax.swing.*;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import generator.RoleFrames;

@SuppressWarnings("serial")
public class PartPanel extends WiredViewer {

	private GroupLayout goalLayout;

	private SequentialGroup goalHoriz;

	private ParallelGroup goalVert;

	private GenesisPanel subjectPanel;

	private GenesisPanel partPanel;

	private GenesisPanel linkPanel;

	public PartPanel() {
		// set border and background
		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		// initialize Panels
		subjectPanel = new GenesisPanel("");
		linkPanel = new GenesisPanel("part", false);
		partPanel = new GenesisPanel("");

		// subjectPanel.setBackground(Color.RED);
		// linkPanel.setBackground(Color.YELLOW);
		// partPanel.setBackground(Color.CYAN);

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

		goalHoriz.addComponent(partPanel);
		goalVert.addComponent(partPanel);

		goalLayout.setHorizontalGroup(goalHoriz);
		goalLayout.setVerticalGroup(goalVert);
		Connections.getPorts(this).addSignalProcessor("process");
	}

	@Override
	public void view(Object object) {

		if (object instanceof Entity) {
			Entity t = (Entity) object;
			// Mark.say("The whole is", t.getObject().getType()); //TODO Ask
			// Professor Winston about why "wing" is the subject
			if (t.getFeatures().contains(Markers.NOT)) {
				linkPanel.setDesirability(false);
			}
			else {
				linkPanel.setDesirability(true);
			}
			partPanel.displayText(t.getSubject().getType());
			subjectPanel.displayText(RoleFrames.getObject(t).getType());
			linkPanel.setVisible(true);
			partPanel.displayText(t.getSubject().getType());
			// Mark.say("The part is", t.getSubject().getType());
		}
	}

}
