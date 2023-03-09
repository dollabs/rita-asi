package matthewFay.viewers;

import utils.Mark;
import matthewFay.representations.StoryGraphEdge;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class StoryGraphEdgeViewerFX {
	private static final Color defaultColor = Color.BLACK;
	private static final Color explanationColor = Color.ORANGE;
	private static final Color predictionColor = Color.DARKSLATEGRAY;
	private static final Color causeColor = Color.BLACK;
	private static final Color selectedBackChainingColor = Color.MAGENTA;
	private static final Color selectedForwardChainingColor = Color.BLUE;
	private static final Color selectedColor = Color.DARKVIOLET;
	
	private Color currentColor = defaultColor;
	
	private StoryGraphNodeViewerFX originBox;
	private StoryGraphNodeViewerFX targetBox;
	private String type;
	
	private final Line line;
	
	private final Line triLine1;
	private final Line triLine2;
	private final Line triLine3;
	
	private Rectangle inDot = null;
	private Rectangle outDot = null;
	
	private final StoryGraphEdge edge;
	private final Group group;
	
	public StoryGraphEdgeViewerFX(StoryGraphEdge edge, Group group, StoryGraphNodeViewerFX originBox, StoryGraphNodeViewerFX targetBox, String type) {
		this.edge = edge;
		this.group = group;
		
		this.originBox = originBox;
		this.targetBox = targetBox;
		this.type = type;
		
		line = new Line();
		triLine1 = new Line();
		triLine2 = new Line();
		triLine3 = new Line();
	}
	
	public void drawWire() {		
		currentColor = defaultColor;
		if(type == "prediction")
			currentColor = predictionColor;
		if(type == "explanation")
			currentColor = explanationColor;
		if(type == "cause")
			currentColor = causeColor;
		if(targetBox.getSelected())
			currentColor = selectedBackChainingColor;
		if(originBox.getSelected())
			currentColor = selectedForwardChainingColor;
		if(originBox.getSelected() && targetBox.getSelected())
			currentColor = selectedColor;
		if(originBox.getColumn() >= targetBox.getColumn())
			currentColor = Color.RED;
//			Mark.err("Bad Box Layout!");
		
		if(originBox.getColumn()+1 >= targetBox.getColumn()) {
			if(group.getChildren().contains(triLine1)) {
				group.getChildren().remove(triLine1);
				group.getChildren().remove(triLine2);
				group.getChildren().remove(triLine3);
			}
			if(!group.getChildren().contains(line)) {
				group.getChildren().add(line);
			}
			line.setStartX(originBox.getOutX());
			line.setStartY(originBox.getOutY());
			line.setEndX(targetBox.getInX());
			line.setEndY(targetBox.getInY());
			
			line.setStrokeWidth(2d*StoryGraphViewerFX.multiplier);
			
			line.setStroke(currentColor);
		} else {
			if(!group.getChildren().contains(triLine1)) {
				group.getChildren().add(triLine1);
				group.getChildren().add(triLine2);
				group.getChildren().add(triLine3);
			}
			if(group.getChildren().contains(line)) {
				group.getChildren().remove(line);
			}
			triLine1.setStartX(originBox.getX()+(originBox.getOutX()-originBox.getInX())*.8);
			triLine1.setStartY(originBox.getY());
			
			triLine1.setEndX(  originBox.getX()+(originBox.getOutX()-originBox.getInX())*.8);
			triLine2.setStartX(originBox.getX()+(originBox.getOutX()-originBox.getInX())*.8);
			triLine1.setEndY(  originBox.getY()-StoryGraphNodeViewerFX.deltaY*StoryGraphViewerFX.multiplier*.25);
			triLine2.setStartY(originBox.getY()-StoryGraphNodeViewerFX.deltaY*StoryGraphViewerFX.multiplier*.25);
			
			triLine2.setEndX(  targetBox.getInX()-StoryGraphNodeViewerFX.deltaX*StoryGraphViewerFX.multiplier*.75);
			triLine3.setStartX(targetBox.getInX()-StoryGraphNodeViewerFX.deltaX*StoryGraphViewerFX.multiplier*.75);
			triLine2.setEndY(  originBox.getY()-StoryGraphNodeViewerFX.deltaY*StoryGraphViewerFX.multiplier*.5);
			triLine3.setStartY(originBox.getY()-StoryGraphNodeViewerFX.deltaY*StoryGraphViewerFX.multiplier*.5);
			
			triLine3.setEndX(targetBox.getInX());
			triLine3.setEndY(targetBox.getInY());
			
			triLine1.setStrokeWidth(2d*StoryGraphViewerFX.multiplier);
			triLine2.setStrokeWidth(2d*StoryGraphViewerFX.multiplier);
			triLine3.setStrokeWidth(2d*StoryGraphViewerFX.multiplier);
			
			triLine1.setStroke(currentColor);
			triLine2.setStroke(currentColor);
			triLine3.setStroke(currentColor);
		}
	}
}
