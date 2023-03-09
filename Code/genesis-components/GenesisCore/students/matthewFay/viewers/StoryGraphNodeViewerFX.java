package matthewFay.viewers;

import java.util.Observable;
import matthewFay.representations.StoryGraphNode;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class StoryGraphNodeViewerFX extends Observable {
	private static final Color defaultColor = Color.WHITE;
	private static final Color predictedColor = Color.YELLOW;
	private static final Color activeColor = Color.GREEN;
	private static final Color selectedColor = Color.PINK;
	private static final Color negativeColor = Color.RED;
	private static final Color edgeColor = Color.BLACK;
	
	public static final double deltaX = 50, deltaY = 50;
	public static final double width = 110, height = 80;
	
	private static final double fontSpacing = 3;
	private static final int maxFontSize = 50;
	
	
	private boolean selected = false;
	public boolean getSelected() {
		return selected;
	}

	private int row, column;
	public int getRow() {
		return row;
	}
	public int getColumn() {
		return column;
	}
	private double x, y;
	private Color fillColor = defaultColor;	
	private String label;
	
	private final StoryGraphNode node;
	public StoryGraphNode getNode() {
		return node;
	}
	
	public double getOutX() {
		return x + width*StoryGraphViewerFX.multiplier;
	}
	
	public double getOutY() {
		return y + height/2d*StoryGraphViewerFX.multiplier;
	}
	
	public double getInX() {
		return x;
	}
	
	public double getInY() {
		return y+height/2d*StoryGraphViewerFX.multiplier;
	}
	
	public double getX() {
		this.x = (StoryGraphViewerFX.offsetX+column*(width+deltaX))*StoryGraphViewerFX.multiplier;
		return x;
	}
	
	public double getY() {
		this.y = (StoryGraphViewerFX.offsetY+row*(height+deltaY))*StoryGraphViewerFX.multiplier;
		return y;
	}
	
	private final StoryGraphNodeViewerFX self;
	private final Group group;
	private final Rectangle r;
	private final Text text;
	public StoryGraphNodeViewerFX(StoryGraphNode node, Group group, int row, int column) {
		self = this;
		this.group = group;
		
		this.node = node;
		this.row = row;
		this.column = column;
		this.label = node.getEntity().toEnglish()+":"+node.depth;
		
		r = new Rectangle(x,y,width*StoryGraphViewerFX.multiplier,height*StoryGraphViewerFX.multiplier);
		text = new Text(x+fontSpacing*StoryGraphViewerFX.multiplier,y+fontSpacing*StoryGraphViewerFX.multiplier+maxFontSize,label);
		
		r.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				// TODO Auto-generated method stub
				self.selected = !self.selected;
				self.setChanged();
				self.notifyObservers();
				
				arg0.consume();
			}
			
		});
		text.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				// TODO Auto-generated method stub
				self.selected = !self.selected;
				self.setChanged();
				self.notifyObservers();
				
				arg0.consume();
			}
			
		});
	}
	
	
	public void drawBox() {
		this.x = (StoryGraphViewerFX.offsetX+column*(width+deltaX))*StoryGraphViewerFX.multiplier;
		this.y = (StoryGraphViewerFX.offsetY+row*(height+deltaY))*StoryGraphViewerFX.multiplier;
		
		if(!group.getChildren().contains(r))
			group.getChildren().add(r);
		
		r.setX(x);
		r.setY(y);
		r.setWidth(width*StoryGraphViewerFX.multiplier);
		r.setHeight(height*StoryGraphViewerFX.multiplier);

		r.setFill(fillColor);		
		r.setStrokeType(StrokeType.INSIDE);
		r.setStrokeWidth(3d*StoryGraphViewerFX.multiplier);
		r.setStroke(edgeColor);
		
		//Colors from properties of Node/Story Elt
		if(node.getPrediction()) {
			r.setFill(predictedColor);
		}
		if(node.getNegated()) {
			r.setStroke(negativeColor);
		}
		if(node.getActive()) {
			r.setFill(activeColor);
		}
		//Colors from GUI
		if(selected) {
			r.setFill(selectedColor);
		}
		
		if(node.getAssumed()) {
			r.getStrokeDashArray().addAll(10d,10d);
		}
		
		int fontSize = maxFontSize;
		if(!group.getChildren().contains(text))
			group.getChildren().add(text);
		text.setX(x+fontSpacing*StoryGraphViewerFX.multiplier);
		text.setY(y+fontSpacing*StoryGraphViewerFX.multiplier);
		text.setText(label);
		
		text.setFont(new Font(fontSize));
		text.setFill(Color.BLACK);
		text.setWrappingWidth((width-fontSpacing*2d)*StoryGraphViewerFX.multiplier);
		text.setTextAlignment(TextAlignment.CENTER);
		Bounds rb = r.getBoundsInLocal();
		Bounds tb = text.getBoundsInLocal();
		while(rb.getHeight() < tb.getHeight()+fontSize) {
			fontSize--;
			text.setY(y+fontSpacing*StoryGraphViewerFX.multiplier+fontSize);
			text.setFont(new Font(fontSize));
			tb = text.getBoundsInLocal();
		}
	}
	
}