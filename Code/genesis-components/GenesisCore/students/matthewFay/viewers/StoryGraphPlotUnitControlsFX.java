package matthewFay.viewers;

import java.util.ArrayList;
import java.util.List;

import frames.entities.Entity;
import frames.entities.Sequence;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.control.Button;

public class StoryGraphPlotUnitControlsFX {
	private final Sequence plotUnits;
	private final Group group;
	private final StoryGraphViewerFX parent;
	
	private final List<Button> buttons;
	
	public StoryGraphPlotUnitControlsFX(Sequence plotUnits, Group group, StoryGraphViewerFX viewer) {
		this.plotUnits = plotUnits;
		this.group = group;
		this.buttons = new ArrayList<>();
		this.parent = viewer;
		int y = 0;
		int x = 0;
		if(this.buttons.isEmpty()) {
			for(Entity elt : this.plotUnits.getElements()) {
				if(elt.sequenceP()) {
					Sequence plotUnit = (Sequence)elt;
					Button b = new Button(plotUnit.getName());
					final List<Entity> events = new ArrayList<Entity>();
					for(Entity event : plotUnit.getElement(0).getAllComponents()) {
						events.add(event);
					}
					
					b.setOnAction(new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							parent.highlightPlotUnit(events);
						}
					});
					
					y+=100;
					b.setLayoutY(y);
					b.setLayoutX(x);
					group.getChildren().add(b);
				}
			}
		}
	}

	public void kill() {
		// TODO Auto-generated method stub
		for(Button b : buttons) {
			group.getChildren().remove(b);
		}
	}
}
