package matthewFay.Depricated;

import generator.Generator;

import java.awt.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableColumnModel;

import matthewFay.Demo;
import matthewFay.StoryAlignment.Alignment;
import matthewFay.StoryAlignment.RankedSequenceAlignmentSet;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.TextAreaRenderer;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Sequence;

@SuppressWarnings("serial")
@Deprecated
public class GapViewer extends JPanel implements WiredBox {

	public static boolean generateNiceOutput = true;
	
	public static final String ADDGAP_PORT = "addGap";
	public static final String CLEARVIEW_PORT = "clearview";
	
	private JTabbedPane tabbedPane; 
	private JPanel filledGapPanel;
	private List<JPanel> gapDetailPanels;
	
	private boolean finishedGapFilling = false;
	
	public String getName() {
		return "Gap Viewer";
	}
	
	public GapViewer() {
		super(new BorderLayout());
		tabbedPane = new JTabbedPane();
		gapDetailPanels = new ArrayList<JPanel>();
		filledGapPanel = new JPanel(new BorderLayout());
		
		tabbedPane.add("Finished Sequence", filledGapPanel);
		this.add(tabbedPane);
		
		Connections.getPorts(this).addSignalProcessor("renderSequence");
		Connections.getPorts(this).addSignalProcessor(CLEARVIEW_PORT, "clearView");
		Connections.getPorts(this).addSignalProcessor(ADDGAP_PORT, "renderGap");
	}

	@SuppressWarnings("unchecked")
	public void renderSequence(Object o) {
		Sequence s = null;
		ArrayList<Integer> importantPoints = null;
		if(o instanceof Sequence) {
			s = (Sequence)o;
		}
		BetterSignal signal = BetterSignal.isSignal(o);
		if(signal != null) {
			try {
				s = signal.get(0, Sequence.class);
			} catch (Exception e) {
				return;
			}
			
			try {
				importantPoints = signal.get(1, ArrayList.class);
			} catch (Exception e) {
				importantPoints = null;
			}
		}
		if(s == null)
			return;
		filledGapPanel.removeAll();
		JScrollPane table = generateTableFromSequence(s, "Gaps Filled");
		if(importantPoints != null) {
			for(Integer i : importantPoints) {
				setCellColor(table, 0, i+1, new Color(128,192,128));
			}
		}
		filledGapPanel.add(table, BorderLayout.CENTER);
		filledGapPanel.validate();
		filledGapPanel.repaint();
	}

	@SuppressWarnings("unchecked")
	public void renderGap(Object o) {
		if(finishedGapFilling) {
			finishedGapFilling = false;
			clearView();
		}
		RankedSequenceAlignmentSet<Entity, Entity> alignments = null;
		if(o instanceof RankedSequenceAlignmentSet) {
			alignments = (RankedSequenceAlignmentSet<Entity, Entity>)o;
		}
		BetterSignal signal = BetterSignal.isSignal(o);
		if(signal != null) {
			try {
				alignments = signal.get(0, RankedSequenceAlignmentSet.class);
			} catch(Exception e) {
				return;
			}
		}
		if(alignments == null)
			return;
		JPanel alignmentPanel = new JPanel(new BorderLayout());
		alignmentPanel.add(generateFullTable(alignments), BorderLayout.CENTER);
		tabbedPane.add("Gap "+tabbedPane.getTabCount(),alignmentPanel);
		tabbedPane.validate();
		tabbedPane.repaint();
	}
	
	public void clearView() {
		filledGapPanel.removeAll();
		filledGapPanel.validate();
		filledGapPanel.repaint();
		while(tabbedPane.getTabCount() > 1)
			tabbedPane.remove(1);
		tabbedPane.validate();
		tabbedPane.repaint();
		gapDetailPanels.clear();
	}
	
	public static JScrollPane generateTable(SequenceAlignment alignment) {
		Generator generator = Generator.getGenerator();
		
		int totalElements = alignment.size();
	
		Object[][] data = new Object[2][totalElements+1];
		String[] eltSlots = new String[totalElements+1];
		
		eltSlots[0] = "Story";
		data[0][0] = alignment.bName;
		data[1][0] = alignment.aName;
		
		for(Integer i=0; i<totalElements; i++) {
			eltSlots[i+1] = "Element "+i.toString();
			if(alignment.get(i).b != null)
				if(generateNiceOutput) {
					try {
					data[0][i+1] = generator.generate(alignment.get(i).b);
					} catch(Exception e) {
						data[0][i+1] = alignment.get(i).b.asString();
					}
				} else {
					data[0][i+1] = alignment.get(i).b.asString();
				}
			else
				data[0][i+1] = "---";
			if(alignment.get(i).a != null)
				if(generateNiceOutput) {
					try {
					data[1][i+1] = generator.generate(alignment.get(i).a);
					} catch(Exception e) {
						data[1][i+1] = alignment.get(i).a.asString();
					}
				} else {
					data[1][i+1] = alignment.get(i).a.asString();
				}
			else
				data[1][i+1] = "---";
		}
		
		return generateTable(data, eltSlots);
	}
	
	public static JScrollPane generateTable(RankedSequenceAlignmentSet<Entity, Entity> rankedAlignmentSet) {
		Generator generator = Generator.getGenerator();
		
		int totalAlignments = rankedAlignmentSet.size();
		int totalElements = 0;
		if(totalAlignments > 0) {
			totalElements = rankedAlignmentSet.getMaxLength();
		}
	
		Object[][] data = new Object[totalAlignments+1][totalElements+1];
		String[] eltSlots = new String[totalElements+1];
		
		eltSlots[0] = "Story";
		
		for(Integer eltIter=0; eltIter<totalElements; eltIter++) {
			eltSlots[eltIter+1] = "Element "+eltIter.toString();
			for(int alignmentIter = 0;alignmentIter<totalAlignments;alignmentIter++) {
				if(alignmentIter == 0) {
					data[0][0] = rankedAlignmentSet.get(alignmentIter).bName;
					data[1][0] = rankedAlignmentSet.get(alignmentIter).aName;
					if(rankedAlignmentSet.get(alignmentIter).get(eltIter).b != null)
						if(generateNiceOutput) {
							try {
							data[0][eltIter+1] = generator.generate(rankedAlignmentSet.get(alignmentIter).get(eltIter).b);
							} catch(Exception e) {
								data[0][eltIter+1] = rankedAlignmentSet.get(alignmentIter).get(eltIter).b.asString();
							}
						} else {
							data[0][eltIter+1] = rankedAlignmentSet.get(alignmentIter).get(eltIter).b.asString();
						}
					else
						data[0][eltIter+1] = "---";
					if(rankedAlignmentSet.get(alignmentIter).get(eltIter).a != null)
						if(generateNiceOutput) {
							try {
							data[1][eltIter+1] = generator.generate(rankedAlignmentSet.get(alignmentIter).get(eltIter).a);
							} catch(Exception e) {
								data[1][eltIter+1] = rankedAlignmentSet.get(alignmentIter).get(eltIter).a.asString();
							}
						} else {
							data[1][eltIter+1] = rankedAlignmentSet.get(alignmentIter).get(eltIter).a.asString();
						}
					else
						data[1][eltIter+1] = "---";
				} else {
					data[alignmentIter+1][0] = rankedAlignmentSet.get(alignmentIter).aName;
					if(rankedAlignmentSet.get(alignmentIter).get(eltIter).a != null)
						if(generateNiceOutput) {
							try {
							data[alignmentIter+1][eltIter+1] = generator.generate(rankedAlignmentSet.get(alignmentIter).get(eltIter).a);
							} catch(Exception e) {
								data[alignmentIter+1][eltIter+1] = rankedAlignmentSet.get(alignmentIter).get(eltIter).a.asString();
							}
						} else {
							data[alignmentIter+1][eltIter+1] = rankedAlignmentSet.get(alignmentIter).get(eltIter).a.asString();
						}
					else
						data[alignmentIter+1][eltIter+1] = "---";
				}
			}
		}
		
		return generateTable(data, eltSlots);
	}
	
	public static JScrollPane generateFullTable(List<SequenceAlignment> alignmentSets) {
		Generator generator = Generator.getGenerator();
		
		int totalAlignments = alignmentSets.size();
		int totalElements = 0;
		for(Alignment<Entity, Entity> alignment : alignmentSets) {
			totalElements = alignment.size() > totalElements ? alignment.size() : totalElements;
		}
	
		Object[][] data = new Object[2*totalAlignments][totalElements+1];
		String[] eltSlots = new String[totalElements+1];
		
		eltSlots[0] = "Story";
		
		for(Integer eltIter=0; eltIter<totalElements; eltIter++) {
			eltSlots[eltIter+1] = "Element "+eltIter.toString();
			for(int alignmentIter = 0;alignmentIter<totalAlignments;alignmentIter++) {
				data[2*alignmentIter+0][0] = alignmentSets.get(alignmentIter).bName;
				data[2*alignmentIter+1][0] = alignmentSets.get(alignmentIter).aName;
//				if(alignmentSets.get(alignmentIter).get(eltIter).b != null)
				if(alignmentSets.get(alignmentIter).size() > eltIter && alignmentSets.get(alignmentIter).get(eltIter) != null && alignmentSets.get(alignmentIter).get(eltIter).b != null) 
					if(generateNiceOutput) {
						try {
						data[2*alignmentIter+0][eltIter+1] = generator.generate(alignmentSets.get(alignmentIter).get(eltIter).b);
						} catch(Exception e) {
							data[2*alignmentIter+0][eltIter+1] = alignmentSets.get(alignmentIter).get(eltIter).b.asString();
						}
					} else {
						data[2*alignmentIter+0][eltIter+1] = alignmentSets.get(alignmentIter).get(eltIter).b.asString();
					}
				else
					data[2*alignmentIter+0][eltIter+1] = "---";
				if(alignmentSets.get(alignmentIter).size() > eltIter && alignmentSets.get(alignmentIter).get(eltIter) != null && alignmentSets.get(alignmentIter).get(eltIter).a != null)
					if(generateNiceOutput) {
						try {
						data[2*alignmentIter+1][eltIter+1] = generator.generate(alignmentSets.get(alignmentIter).get(eltIter).a);
						} catch(Exception e) {
							data[2*alignmentIter+1][eltIter+1] = alignmentSets.get(alignmentIter).get(eltIter).a.asString();
						}
					} else {
						data[2*alignmentIter+1][eltIter+1] = alignmentSets.get(alignmentIter).get(eltIter).a.asString();
					}
				else
					data[2*alignmentIter+1][eltIter+1] = "---";
			}
		}
		
		return generateTable(data, eltSlots);
	}
	
	public static JScrollPane generateTableFromSequence(Sequence sequence, String title) {
		Generator generator = Generator.getGenerator();
		
		int totalAlignments = 1;
		int totalElements = sequence.getNumberOfChildren();
	
		Object[][] data = new Object[totalAlignments][totalElements+1];
		String[] eltSlots = new String[totalElements+1];
		
		eltSlots[0] = "Story";
		data[0][0] = title;
		
		for(Integer eltIter=0; eltIter<totalElements; eltIter++) {
			eltSlots[eltIter+1] = "Element "+eltIter.toString();
			//data[0][eltIter+1] = sequence.getElement(eltIter).asString();
			data[0][eltIter+1] = generator.generate(sequence.getElement(eltIter));
		}
		
		return generateTable(data, eltSlots);
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
	
	/*
	 * Only use with scrollPanes generated by GapViewer
	 */
	public static void setCellColor(JScrollPane scrollPane, int row, int column, Color color) {
		((TextAreaRenderer)((JTable)scrollPane.getViewport().getView()).getCellRenderer(row, column)).setColor(row, column, color);
	}
	
	public static void main(String args[]) {
		Sequence GapStory = Demo.ComplexGapStory();
		Sequence GiveStory = Demo.VerboseGive();
		Sequence ComplexTakeStory = Demo.ComplexTakeStory();
		
		GapFiller gf = new GapFiller();
		
		gf.addPattern(GiveStory);
		gf.addPattern(ComplexTakeStory);
		Mark.say(GapStory.asString());
		Sequence s = gf.fillGap(GapStory);
		Mark.say(s.asString());
		
		GapViewer viewer = new GapViewer();
		viewer.renderSequence(new BetterSignal(s, gf.gapsFilledAt));
		viewer.renderGap(gf.lastAlignments);
		
		JFrame frame = new JFrame("Simple Alignment Viewer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(viewer);
		frame.pack();
		frame.setVisible(true);
	}
}
