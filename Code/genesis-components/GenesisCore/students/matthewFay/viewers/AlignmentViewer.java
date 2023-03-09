package matthewFay.viewers;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableColumnModel;

import matthewFay.StoryAlignment.AlignmentProcessor;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.TextAreaRenderer;
import matthewFay.Utilities.EntityHelper.MatchNode;
import utils.PairOfEntities;
import connections.*;
import connections.signals.BetterSignal;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import generator.Generator;
import gui.panels.ParallelJPanel;

@SuppressWarnings("serial")
public class AlignmentViewer extends JPanel implements WiredBox, ActionListener {
	public static final String CONCEPT_ALIGNMENT_PORT = "REFLECTION ALIGNMENT PORT";

	public static final String GRAPH_PORT = "Graph Port";

	private JTabbedPane tabbedPane;

	public static JPanel alignmentPanel;

	private JPanel conceptPanel;

	private JPanel bindingsPanel;

	private JPanel treePanel;

	private ParallelJPanel controlPanel;

	public static JCheckBox useConcepts = new JCheckBox("Use Reflective Knowledge", true);

	public static JCheckBox debugTreeGeneration = new JCheckBox("Debug Tree Generation", false);

	public static JCheckBox generateNiceOutput = new JCheckBox("Generate Nice English Output", true);

	public static JCheckBox gapFilling = new JCheckBox("Collaborative Gap Filling", false);

	public static JCheckBox simultaneousMatchingAndAlignment = new JCheckBox("Simultaneous Matching+Alignment", true);

	public static JCheckBox simpleScorer = new JCheckBox("Simple Scorer", true);

	public static JButton rerunLastAlignmentButton = new JButton("Rerun Alignment!");

	public String getName() {
		return "Alignment Viewer";
	}

	public AlignmentViewer() {
		super(new BorderLayout());
		tabbedPane = new JTabbedPane();
		alignmentPanel = new JPanel(new BorderLayout());
		conceptPanel = new JPanel(new BorderLayout());
		bindingsPanel = new JPanel(new BorderLayout());
		treePanel = new JPanel(new BorderLayout());
		controlPanel = new ParallelJPanel();

		controlPanel.addLeft(generateNiceOutput);
		controlPanel.addLeft(gapFilling);

		controlPanel.addCenter(useConcepts);
		controlPanel.addCenter(simpleScorer);

		controlPanel.addRight(debugTreeGeneration);
		controlPanel.addRight(simultaneousMatchingAndAlignment);
		controlPanel.addRight(rerunLastAlignmentButton);

		simultaneousMatchingAndAlignment.setEnabled(false);

		rerunLastAlignmentButton.setActionCommand("rerun");
		rerunLastAlignmentButton.addActionListener(this);

		tabbedPane.add("Story Alignment", alignmentPanel);
		tabbedPane.add("Concept Alignment", conceptPanel);
		tabbedPane.add("Bindings", bindingsPanel);
		tabbedPane.add("Match Tree", treePanel);
		tabbedPane.add("Control Panel", controlPanel);
		this.add(tabbedPane);

		Connections.getPorts(this).addSignalProcessor("renderAlignment");
		Connections.getPorts(this).addSignalProcessor(CONCEPT_ALIGNMENT_PORT, "renderConceptAlignment");
		Connections.getPorts(this).addSignalProcessor(GRAPH_PORT, "renderGraph");
	}

	@SuppressWarnings("unchecked")
	public void renderGraph(Object o) {
		Forest<MatchNode, Integer> graph = null;
		BetterSignal signal = BetterSignal.isSignal(o);
		if (signal == null) return;
		try {
			graph = signal.get(0, Forest.class);
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		treePanel.removeAll();

		treePanel.add(generateVisualTree(graph), BorderLayout.CENTER);
		treePanel.validate();
		treePanel.repaint();
	}

	public void renderAlignment(Object o) {
		BetterSignal signal = BetterSignal.isSignal(o);
		if (signal == null) return;
		alignmentPanel.removeAll();
		bindingsPanel.removeAll();
		JScrollPane table = generateTable(signal);
		JScrollPane bindingsTable = generateBindingsTable(signal);

		alignmentPanel.add(table, BorderLayout.CENTER);
		alignmentPanel.validate();
		alignmentPanel.repaint();
		bindingsPanel.add(bindingsTable, BorderLayout.CENTER);
		bindingsPanel.validate();
		bindingsPanel.repaint();
	}

	public void renderConceptAlignment(Object o) {
		BetterSignal signal = BetterSignal.isSignal(o);
		if (signal == null) return;
		conceptPanel.removeAll();
		JScrollPane table = generateTable(signal);

		conceptPanel.add(table, BorderLayout.CENTER);
		conceptPanel.validate();
		conceptPanel.repaint();
	}

	public static JScrollPane generateBindingsTable(BetterSignal alignmentSignal) {
		int totalElements = 0;
		List<SequenceAlignment> alignments = alignmentSignal.getAll(SequenceAlignment.class);
		for (SequenceAlignment alignment : alignments) {
			totalElements = Math.max(alignment.bindings.size(), totalElements);
		}

		Object[][] data = new Object[alignments.size() * 2][totalElements + 1];
		String[] eltSlots = new String[totalElements + 1];

		eltSlots[0] = "Story";
		for (Integer i = 0; i < totalElements; i++) {
			eltSlots[i + 1] = "Binding " + i.toString();
		}

		int currentAlignment = 0;
		for (SequenceAlignment alignment : alignments) {
			data[2 * currentAlignment][0] = alignment.bName + "Score: " + alignment.score;
			data[2 * currentAlignment + 1][0] = alignment.aName + "Score: " + alignment.score;

			int pairCount = 0;
			List<PairOfEntities> revList = new ArrayList<PairOfEntities>();
			for (PairOfEntities pair : alignment.bindings) {
				revList.add(pair);
			}
			for (int pair = revList.size() - 1; pair >= 0; pair--) {
				data[2 * currentAlignment][pairCount + 1] = revList.get(pair).getDatum().asString();
				data[2 * currentAlignment + 1][pairCount + 1] = revList.get(pair).getPattern().asString();
				pairCount++;
			}
			currentAlignment++;
		}

		return generateTable(data, eltSlots);
	}

	public static JScrollPane generateTable(SequenceAlignment sequence_alignment, boolean highlight_alignment) {
		JScrollPane table = generateTable(new BetterSignal(sequence_alignment));

		if (highlight_alignment) {
			for (int i = 0; i < sequence_alignment.size(); i++) {
				if (sequence_alignment.get(i).a != null && sequence_alignment.get(i).b != null) {
					setCellColor(table, 0, i + 1, new Color(128, 192, 128));
					setCellColor(table, 1, i + 1, new Color(128, 192, 128));
				}
			}
		}

		return table;

	}

	public static JScrollPane generateTable(BetterSignal alignmentSignal) {
		Generator generator = Generator.getGenerator();

		int totalElements = 0;
		List<SequenceAlignment> alignments = alignmentSignal.getAll(SequenceAlignment.class);
		for (SequenceAlignment alignment : alignments) {
			totalElements = Math.max(alignment.size(), totalElements);
		}

		Object[][] data = new Object[alignments.size() * 2][totalElements + 1];
		String[] eltSlots = new String[totalElements + 1];

		eltSlots[0] = "Story";
		for (Integer i = 0; i < totalElements; i++) {
			eltSlots[i + 1] = "Element " + i.toString();
		}

		int currentAlignment = 0;
		for (SequenceAlignment alignment : alignments) {

			data[currentAlignment * 2][0] = alignment.bName + " Score: " + alignment.score;
			// data[currentAlignment*2][0] = "Story B (Score: "+alignment.score+")";
			data[currentAlignment * 2 + 1][0] = alignment.aName + " Score: " + alignment.score;
			// data[currentAlignment*2+1][0] = "Story A (Score: "+alignment.score+")";
			for (Integer i = 0; i < alignment.size(); i++) {
				if (alignment.get(i).b != null) {
					if (generateNiceOutput.isSelected()) {
						try {
							String output = generator.generate(alignment.get(i).b);
							if (output != null)
								data[currentAlignment * 2][i + 1] = generator.generate(alignment.get(i).b);
							else
								data[currentAlignment * 2][i + 1] = alignment.get(i).b.asString();
						}
						catch (Exception e) {
							data[currentAlignment * 2][i + 1] = alignment.get(i).b.asString();
						}
					}
					else {
						data[currentAlignment * 2][i + 1] = alignment.get(i).b.asString();
					}

				}
				else {
					data[currentAlignment * 2][i + 1] = "---";
				}
				if (alignment.get(i).a != null) {
					if (generateNiceOutput.isSelected()) {
						try {
							String output = generator.generate(alignment.get(i).a);
							if (output != null)
								data[currentAlignment * 2 + 1][i + 1] = output;
							else
								data[currentAlignment * 2 + 1][i + 1] = alignment.get(i).a.asString();
						}
						catch (Exception e) {
							data[currentAlignment * 2 + 1][i + 1] = alignment.get(i).a.asString();
						}
					}
					else {
						data[currentAlignment * 2 + 1][i + 1] = alignment.get(i).a.asString();
					}
				}
				else {
					data[currentAlignment * 2 + 1][i + 1] = "---";
				}
			}

			currentAlignment++;
		}

		JScrollPane scrollPane = generateTable(data, eltSlots);
		for (SequenceAlignment alignment : alignments) {
			for (Integer i = 0; i < alignment.size(); i++) {
				if (alignment.get(i).b != null) {
					if (alignment.get(i).b.hasFeature("GapFilled")) {
						setCellColor(scrollPane, (currentAlignment - 1) * 2, i + 1, new Color(128, 192, 128));
					}
				}
				if (alignment.get(i).a != null) {
					if (alignment.get(i).a.hasFeature("GapFilled")) {
						setCellColor(scrollPane, (currentAlignment - 1) * 2 + 1, i + 1, new Color(128, 192, 128));
					}
				}
			}
		}

		return scrollPane;
	}

	/*
	 * Dirty Hack - Only use with scrollPanes generated by GapViewer/AlignmentViewer
	 */
	public static void setCellColor(JScrollPane scrollPane, int row, int column, Color color) {
		((TextAreaRenderer) ((JTable) scrollPane.getViewport().getView()).getCellRenderer(row, column)).setColor(row, column, color);
	}

	public static JScrollPane generateTable(Object[][] data, String[] cols) {
		JTable alignmentTable = new JTable(data, cols);
		TextAreaRenderer textAreaRenderer = new TextAreaRenderer();
		TableColumnModel cmodel = alignmentTable.getColumnModel();

		for (int i = 0; i < cmodel.getColumnCount(); i++) {
			cmodel.getColumn(i).setPreferredWidth(175);
			cmodel.getColumn(i).setCellRenderer(textAreaRenderer);
		}

		alignmentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JScrollPane scrollPane = new JScrollPane(alignmentTable);

		return scrollPane;
	}

	private static JFrame frame = null;

	public static boolean firstPopout = true;

	public static void popoutVisualTree(Forest<MatchNode, Integer> graph) {
		VisualizationViewer<MatchNode, Integer> vv = generateVisualTree(graph);
		if (frame == null) {
			frame = new JFrame("Matcher Graph View");
			// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.getContentPane().removeAll();
			frame.getContentPane().add(vv);

			frame.setPreferredSize(new Dimension(2000, 800));
			frame.pack();
			frame.setVisible(true);
		}
		else {
			frame.getContentPane().removeAll();
			frame.getContentPane().add(vv);
			// frame.setPreferredSize(new Dimension(2000,800));
			// frame.pack();
			frame.validate();
			frame.setVisible(true);
		}
		if (firstPopout) {
			try {
				Thread.sleep(2000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			firstPopout = false;
		}
	}

	@SuppressWarnings("rawtypes")
	private static VisualizationViewer<MatchNode, Integer> generateVisualTree(Forest<MatchNode, Integer> graph) {
		// Make Some Objects for Tree Layout
		TreeLayout<MatchNode, Integer> layout = new TreeLayout<MatchNode, Integer>(graph);
		VisualizationViewer<MatchNode, Integer> vv = new VisualizationViewer<MatchNode, Integer>(layout);
		vv.setPreferredSize(new Dimension(1600, 1000));

		// Draw each vertex
		ToStringLabeller<MatchNode> vertexPaint = new ToStringLabeller<MatchNode>() {
			public String transform(MatchNode t) {
				if (t.bindingSet != null && t.bindingSet.size() > 0) {
					if (t.story1_entities.size() < 1 && t.story2_entities.size() < 1) return Float.toString(t.score);
					// return "";
					return "Score: " + Float.toString(t.score) + "\n" + t.bindingSet.first().toString();
				}
				return "root";
			}
		};
		vv.getRenderContext().setVertexLabelTransformer(vertexPaint);

		DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
		gm.setMode(ModalGraphMouse.Mode.TRANSFORMING);
		vv.setGraphMouse(gm);

		return vv;

		/*
		 * boolean external = true; if(external ) { JFrame frame = new JFrame("Simple Graph View");
		 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); frame.getContentPane().add(vv); frame.pack();
		 * frame.setVisible(true); } else { // treePanel.removeAll(); // treePanel.add(vv); // tabbedPane.repaint(); }
		 */
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getActionCommand().endsWith("rerun")) {
			AlignmentProcessor.getAlignmentProcessor().rerunLastAlignment();
		}
	}
}
