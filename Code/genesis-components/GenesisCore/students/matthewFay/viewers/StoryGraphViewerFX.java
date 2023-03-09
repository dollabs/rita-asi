package matthewFay.viewers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;
import translator.BasicTranslator;
import utils.Mark;

import matthewFay.StoryGeneration.RuleGraphProcessor;
import matthewFay.representations.StoryGraph;
import matthewFay.representations.StoryGraphEdge;
import matthewFay.representations.StoryGraphNode;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

public class StoryGraphViewerFX extends Application implements Observer {
	public static double offsetY = 10.0;
	public static double offsetX = 10.0;
	public static double multiplier = 1.0;
	
	private Group graphGroup;
	
	private Map<Integer, Integer> columnFills = new HashMap<Integer, Integer>();
	private Map<StoryGraphNode, StoryGraphNodeViewerFX> boxes = new HashMap<StoryGraphNode, StoryGraphNodeViewerFX>();
	private Map<StoryGraphEdge, StoryGraphEdgeViewerFX> edges = new HashMap<StoryGraphEdge, StoryGraphEdgeViewerFX>();
	private StoryGraphPlotUnitControlsFX plotUnitControls = null;
	
	private StoryGraph graph = null;
	
	private Scene scene;
	
	public void showStoryGraph(StoryGraph graph) {
		showStoryGraph(graph,null);
	}
	
	public void showStoryGraph(StoryGraph graph, Sequence plotUnits) {
		reset();
		if(graph==null)
			return;
		this.graph = graph;
		graph.updateDephts();
		
		if(plotUnitControls != null)
			plotUnitControls.kill();
		if(plotUnits != null)
			plotUnitControls = new StoryGraphPlotUnitControlsFX(plotUnits, graphGroup, this);
		
		repaint();
	}
	
	public void highlightPlotUnit(List<Entity> events) {
		for(StoryGraphNode node : this.graph.getAllNodes()) {
			node.setActive(false);
		}
		for(Entity event : events) {
			StoryGraphNode node;
			if((node = graph.getNode(event)) != null) {
				node.setActive(true);
			}
		}
		repaint();
	}
	
	public void reset() {
		columnFills.clear();
		boxes.clear();
		edges.clear();
		graphGroup.getChildren().clear();
		
		// Hack for quick story (re)generation
		Button b = new Button("Generate");
		b.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				Sequence story = RuleGraphProcessor.getRuleGraphProcessor().generateRandomConnectedStory(graph);
				
				Mark.say("New Story:");
				for(Entity e : story.getElements()) {
					Mark.say(e.toEnglish());
				}
				Mark.say("Score: "+RuleGraphProcessor.getRuleGraphProcessor().scoreStory(graph, story));
			}
			
		});
		graphGroup.getChildren().add(b);
		// Hack2 for quick story (re)generation
		Button b2 = new Button("Generate Random");
		b2.setLayoutY(30);
		b2.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				Sequence story = RuleGraphProcessor.getRuleGraphProcessor().generateRandomStory(graph,3);
				
				Mark.say("New Story:");
				for(Entity e : story.getElements()) {
					Mark.say(e.toEnglish());
				}
				Mark.say("Score: "+RuleGraphProcessor.getRuleGraphProcessor().scoreStory(graph, story));
			}
			
		});
		graphGroup.getChildren().add(b2);
		
		Button b3 = new Button("Generate 30 Random Stories");
		b3.setLayoutY(30);
		b3.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				List<Sequence> stories = new ArrayList<Sequence>();
				for(int i=0;i<30;i++) {
					Sequence story = RuleGraphProcessor.getRuleGraphProcessor().generateRandomStory(graph,3);
					stories.add(story);
				}
				RuleGraphProcessor.getRuleGraphProcessor().saveStories(graph, stories);
			}
			
		});
		graphGroup.getChildren().add(b3);
	}
	
	public void repaint() {
		if(graph==null) {
			reset();
			return;
		}
		List<StoryGraphNode> allNodes = graph.getAllNodes();
		Collections.sort(allNodes);
		
		for(StoryGraphNode node : allNodes) {
			StoryGraphNodeViewerFX box;
			if(boxes.containsKey(node)) {
				box = boxes.get(node);
			} else {
				int column = node.depth;
				int row;
				if(columnFills.containsKey(column)) {
					row = columnFills.get(column);
				} else {
					row = 0;
				}
				columnFills.put(column, row+1);
				box = new StoryGraphNodeViewerFX(node, graphGroup, row, column);
				box.addObserver(this);
				boxes.put(node, box);
			}
		}
		
		for(StoryGraphEdge edge : graph.getAllEdges()) {
			StoryGraphEdgeViewerFX line;
			if(edges.containsKey(edge)) {
				line = edges.get(edge);
			} else {
				line = new StoryGraphEdgeViewerFX(
						edge,
						graphGroup,
						boxes.get(edge.getAntecedent()),
						boxes.get(edge.getConsequent()),
						edge.getEdgeType());
				edges.put(edge, line);
			}
		}
		
		repaintGraph();
	}
	
	public void repaintGraph() {
		for(StoryGraphNodeViewerFX nodeViewer : boxes.values()) {
			nodeViewer.drawBox();
		}
		for(StoryGraphEdgeViewerFX edgeViewer : edges.values()) {
			edgeViewer.drawWire();
		}
	}
	
	public void init_from_swing(final StoryGraph graph, final Sequence plotUnits, String title) {
		JFrame frame = new JFrame(title);
		final JFXPanel fxPanel = new JFXPanel();
		frame.add(fxPanel);
		frame.setSize(500, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				initFX(fxPanel);
				showStoryGraph(graph, plotUnits);
			}
		});
	}
	
	public StoryGraphViewerFX() {
		super();
		graphGroup = new Group();
	}
	
	private void initFX(JFXPanel fxPanel) {
		full_init();
		fxPanel.setScene(scene);
	}
	
	@Override
	public void start(Stage app_stage) throws Exception {
		full_init();
		
		sequenceDemo();
		
		app_stage.setTitle("Story Graph Viewer FX");
		app_stage.setScene(scene);
		app_stage.show();
	}
	
	public void full_init() {
		scene = new Scene(graphGroup, 500, 500, Color.WHEAT);
		
		final MouseData mouseData = new MouseData();
		
		scene.setOnMousePressed(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {
				mouseData.x = mouseEvent.getSceneX();
				mouseData.y = mouseEvent.getSceneY();
				mouseData.moved = false;
			}
			
		});
		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {			
			@Override
			public void handle(MouseEvent mouseEvent) {
				StoryGraphViewerFX.offsetX += (mouseEvent.getSceneX()-mouseData.x);
				StoryGraphViewerFX.offsetY += (mouseEvent.getSceneY()-mouseData.y);
				mouseData.x = mouseEvent.getSceneX();
				mouseData.y = mouseEvent.getSceneY();
				mouseData.moved = true;
				repaint();
			}
			
		});
		
		scene.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent mouseEvent) {
				if(mouseData.moved == false) {
					double deltaMultiplier = 1.0;
					if(mouseEvent.getButton() == MouseButton.PRIMARY) {
						deltaMultiplier = 1.1;
					}
					if(mouseEvent.getButton() == MouseButton.SECONDARY) {
						deltaMultiplier = 0.9;
					}
					StoryGraphViewerFX.multiplier *= deltaMultiplier;
					repaint();
				}
			}
			
		}); 
		
		scene.setOnScroll(new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent scrollEvent) {
				double deltaMultiplier = 1.0;
				if(scrollEvent.getDeltaY() > 0) {
					deltaMultiplier = 1.1;
				} else if(scrollEvent.getDeltaY() < 0) {
					deltaMultiplier = 0.9;
				}
				StoryGraphViewerFX.multiplier *= deltaMultiplier;
				repaint();
			}
			
		});
	}
	
	class MouseData { double x, y; boolean moved; };
	
	public static void main(String[] args) {
		launch(args);
	}
	
	public void basicDemo() {
		StoryGraph graph = new StoryGraph();
		Entity like1;
		Entity like2;
		Entity kiss;
		try {
			like1 = BasicTranslator.getTranslator().translate("Mark likes Mary.").getElement(0);
			like2 = BasicTranslator.getTranslator().translate("Mary likes Mark.").getElement(0);
			kiss = BasicTranslator.getTranslator().translate("Mark kisses Mary.").getElement(0);
			graph.addEdge(like1, kiss, "causal");
			graph.addEdge(like2, kiss, "causal");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Mark.say("Showing graph...");
		this.showStoryGraph(graph);
	}
	
	
	@SuppressWarnings("unused")
	public void sequenceDemo() {
		StoryGraph graph = new StoryGraph();
		Entity a,b,c,d,d2,e,f,g,a2,b2,a3;
		try {
			a = BasicTranslator.getTranslator().translate("Alpha was here").getElement(0);
			a2 = BasicTranslator.getTranslator().translate("Alpha2 was here").getElement(0);
			a3 = BasicTranslator.getTranslator().translate("Alpha3 was not here").getElement(0);
			b = BasicTranslator.getTranslator().translate("Bravo was here").getElement(0);
			b2 = BasicTranslator.getTranslator().translate("Bravo2 was here").getElement(0);
			c = BasicTranslator.getTranslator().translate("Charlie was here").getElement(0);
			d = BasicTranslator.getTranslator().translate("Delta was here").getElement(0);
			d2 = BasicTranslator.getTranslator().translate("Delta2 was here").getElement(0);
			e = BasicTranslator.getTranslator().translate("Echo was here").getElement(0);
			
			graph.addEdge(a, b, "causal");
			graph.addEdge(a2, b2, "causal");
			graph.addEdge(a3, b2, "causal");
			graph.addEdge(a3, c, "causal");
			graph.addEdge(b, c, "causal");
			graph.addEdge(b2, c, "causal");
			graph.addEdge(c, b, "causal");
			graph.addEdge(c, d, "causal");
			graph.addEdge(c, d2, "prediction");
			graph.addEdge(d, e, "causal");
			graph.addEdge(d2, e, "causal");
			graph.addEdge(e, d, "causal");
			
			graph.addEdge(a, e, "explanation");
			graph.getNode(b2).setPrediction(true);
			c.addFeature(Markers.ASSUMED);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		Mark.say("Showing graph...");
		this.showStoryGraph(graph);
	}

	@Override
	public void update(Observable o, Object arg) {
		this.repaintGraph();
	}

}
