package matthewFay.StoryThreading;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableColumnModel;

import matthewFay.StoryAlignment.TextAreaRenderer;
import matthewFay.Utilities.HashMatrix;
import matthewFay.viewers.AlignmentViewer;
import connections.Connections;
import connections.WiredBox;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import gui.panels.ParallelJPanel;

@SuppressWarnings("serial")
public class StoryThreadingViewer extends JPanel implements WiredBox {
	public static final String COMPARISON_PORT = "COMPARISON PORT";
	
	private JTabbedPane tabbedPane;
	public ParallelJPanel controlPanel;
	
	public static JCheckBox doCompareAllEntities;
	public static JCheckBox doMinimumSpanningStory;
	
	private JPanel comparisonPanel;
	
	public String getName() {
		return "StoryThreading Viewer";
	}
	
	public StoryThreadingViewer() {
		super(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		controlPanel = new ParallelJPanel();
		comparisonPanel = new JPanel(new BorderLayout());
		
		doCompareAllEntities = new JCheckBox("Compare All Entities", false);
		controlPanel.addLeft(doCompareAllEntities);
		doMinimumSpanningStory = new JCheckBox("Find Minimum Spanning Story", true);
		controlPanel.addLeft(doMinimumSpanningStory);
		
		tabbedPane.add("Character Comparison", comparisonPanel);
		tabbedPane.add("Control Panel", controlPanel);
		this.add(tabbedPane);
		
		Connections.getPorts(this).addSignalProcessor(COMPARISON_PORT,"showComparison");
	}
	
	public void showComparison(Object o) {
		BetterSignal signal = BetterSignal.isSignal(o);
		if(signal == null)
			return;
		
		comparisonPanel.removeAll();
		JScrollPane table = generateComparisonTable(signal);
		
		comparisonPanel.add(table, BorderLayout.CENTER);
		comparisonPanel.validate();
		comparisonPanel.repaint();
	}

	private class ThingScore {
		public Entity thing;
		public float score;
		public ThingScore(Entity t, float s) {
			thing = t;
			score = s;
		}
	}
	
	@SuppressWarnings("unchecked")
	private JScrollPane generateComparisonTable(BetterSignal signal) {
		HashMatrix<Entity, Entity, Float> similarity = signal.getAll(HashMatrix.class).get(0);
		HashMap<Entity, List<ThingScore>> scores = new HashMap<Entity, List<ThingScore>>();
		
		int width = 0;
		int height = 0;
		
		for(Entity entity_a : similarity.keySetRows()) {
			List<ThingScore> thingScores = new ArrayList<ThingScore>();
			scores.put(entity_a, thingScores);
			for(Entity entity_b : similarity.keySetCols()) {
				float score = similarity.get(entity_a, entity_b);
				if(score > 0) {
					boolean added=false;
					for(int i=0;i<thingScores.size();i++) {
						if(score >= thingScores.get(i).score) {
							thingScores.add(i, new ThingScore(entity_b, score));
							added=true;
							break;
						}
					}
					if(!added)
						thingScores.add(new ThingScore(entity_b, score));
				}
			}
			if (thingScores.size() >= width)
				width = thingScores.size();
		}
		height = scores.keySet().size();
		
		Object[][] data = new Object[height][width];
		String[] cols = new String[width];
		
		cols[0] = "Rank:";
		for(int i=1;i<width;i++) {
			cols[i] = "#"+i;
		}
		
		int i=0;
		int j=0;
		for(Entity entity_a : scores.keySet()) {
			i=0;
			data[j][i] = entity_a.asString()+":";
			List<ThingScore> tss = scores.get(entity_a);
			i++;
			while(i<width) {
				if(i<tss.size()) {
					ThingScore ts = tss.get(i);
					data[j][i] = ts.thing.asString()+"="+ts.score;
				} else {
					data[j][i]="";
				}
				i++;
			}
			j++;
		}
		
		JScrollPane scrollPane = generateTable(data,cols);
		
		return scrollPane;
	}
	
	public static JScrollPane generateTable(Object[][] data, String[] cols) {
		JTable alignmentTable = new JTable(data, cols);
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		TableColumnModel cmodel = alignmentTable.getColumnModel();
		
		for(int i=0; i<cmodel.getColumnCount(); i++) {
			cmodel.getColumn(i).setPreferredWidth(175);
			cmodel.getColumn(i).setCellRenderer(textAreaRenderer);
		}
			
		
		alignmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		JScrollPane scrollPane = new JScrollPane(alignmentTable);

		return scrollPane;
	}
}
