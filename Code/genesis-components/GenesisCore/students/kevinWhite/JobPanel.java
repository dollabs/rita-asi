package kevinWhite;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

import connections.Connections;
import connections.WiredViewer;
import frames.entities.Entity;

@SuppressWarnings("serial")
public class JobPanel extends WiredViewer {

	private GroupLayout goalLayout;

	private SequentialGroup goalHoriz;

	private ParallelGroup goalVert;

	private GenesisPanel subjectPanel;

	private GenesisPanel partPanel;

	private GenesisPanel linkPanel;

	public JobPanel() {
		// set border and background
		this.setBackground(Color.WHITE);
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));

		// initialize Panels
		subjectPanel = new GenesisPanel("");
		linkPanel = new GenesisPanel("job", false);
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
//			Mark.say("The whole is", t.getObject().getType());
			if (t.getFeatures() != null) {
				linkPanel.setDesirability(false);
			}
			else {
				linkPanel.setDesirability(true);
			}
			partPanel.displayText(t.getObject().getType());
			subjectPanel.displayText(t.getSubject().getType());
			linkPanel.setVisible(true);
//			Mark.say("The part is", t.getSubject().getType());
		}
	}

}
