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
public class PossessionPanel extends WiredViewer {
	private GroupLayout goalLayout;

	private SequentialGroup goalHoriz;

	private ParallelGroup goalVert;

	private GenesisPanel subjectPanel;

	private GenesisPanel possessionPanel;

	private GenesisPanel linkPanel;

	public PossessionPanel() {
		// set border and background
		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		// initialize Panels
		subjectPanel = new GenesisPanel("");
		linkPanel = new GenesisPanel("possession", false);
		linkPanel.setBackground(Color.BLACK);
		possessionPanel = new GenesisPanel("");

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

		goalHoriz.addComponent(possessionPanel);
		goalVert.addComponent(possessionPanel);

		goalLayout.setHorizontalGroup(goalHoriz);
		goalLayout.setVerticalGroup(goalVert);
		Connections.getPorts(this).addSignalProcessor("process");
	}

	@Override
	public void view(Object object) {

		if (object instanceof Entity) {

			Entity t = (Entity) object;
			// Mark.say("amazing, here it is; object is", t.asString());
			// Mark.say("The owner is", t.getSubject().getType());
			subjectPanel.displayText(t.getSubject().getType());
			if (t.getFeatures().contains(Markers.NOT)) {
				linkPanel.setDesirability(false);
			}
			else {
				linkPanel.setDesirability(true);
			}
			linkPanel.setVisible(true);
			possessionPanel.displayText(RoleFrames.getObject(t).getType());
		}
	}

}
