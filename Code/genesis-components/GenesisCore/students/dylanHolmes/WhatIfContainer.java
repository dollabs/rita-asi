package dylanHolmes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.Utilities.EntityHelper.MatchNode;
import mentalModels.MentalModel;

import com.google.common.collect.Table;

import storyProcessor.ConceptAnalysis;
import utils.Mark;
import utils.PairOfEntities;
import connections.Connections;
import connections.WiredBox;
import connections.signals.BetterSignal;
import constants.Markers;
import edu.uci.ics.jung.graph.Forest;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import gui.panels.ParallelJPanel;


public class WhatIfContainer extends JPanel implements WiredBox {
	
	public JTable table;
	public DefaultTableModel model;
	private JComboBox includes;
	private JScrollPane scrollPane;
	
	public static final String[] modes = {"In both versions", "In first version only", "In second version only", "Delete"};
	public static final String INPUT = "my input port";
	public static final String OUT_STORY_FIRST_VERSION = "output first version of events";
	public static final String OUT_STORY_SECOND_VERSION = "output second version of events";
	private boolean ready = true; 
	
	
	public WhatIfContainer() {
		super(new BorderLayout());
		

		this.setName("What if?");
		Mark.say("WhatIfContainer local constructor");

		// ---------- ESTABLISH INCOMING WIRED BOX CONNECTIONS
		// (outgoing connections established in genesisgetters)
		Connections.getPorts(this).addSignalProcessor("processSignal");
		Connections.getPorts(this).addSignalProcessor(INPUT, "processSignal");
		
		
		// ---------- SET UP THE GUI ELEMENTS
		String[] columnNames = { "Include?", "Statement" };
		
		includes = new JComboBox();
		for(String m : modes) {
			includes.addItem(m);
		}

		

		Object[][] data = {};
		model = new DefaultTableModel(data, columnNames);
		// model = new EntityListTableModel();
		// model.setData(columnNames, data);

		table = new JTable(model);
		table.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(includes));
		table.getColumnModel().getColumn(1).setPreferredWidth(20);

		table.setPreferredScrollableViewportSize(new Dimension(500, 70));

		scrollPane = new JScrollPane(table);
		JButton btn_generate = new JButton("Generate What-if");
		btn_generate.addActionListener(new ActionListener() {
			  public void actionPerformed(ActionEvent evt) {
				  transmitStoryVersion(2);
			  }
		});
		
		this.add(scrollPane, BorderLayout.CENTER);
		this.add(btn_generate, BorderLayout.SOUTH);
	}

	void transmitStoryVersion(int version) {
		// Version can be 1 or 2
		if(version != 1 && version != 2){Mark.say("WhatIf: version must be 1 or 2");return;}
		
		Sequence s = new Sequence(Markers.STORY_MARKER);
		
//		Relation start = new Relation("start", new Entity("you"), new Entity(Markers.STORY_MARKER));
//		s.addElement(start);
		
		Vector rows = model.getDataVector();
		for(int i=0; i<rows.size(); i++) {
			Vector row = (Vector)rows.elementAt(i);
			
			// mode is 0: both, 1: first only, 2: second only
			int mode = Arrays.asList(modes).indexOf(row.elementAt(0));
			if(mode == 0 || mode == version) {
				//Entity e = Entity.reader((String)row.elementAt(1));
				//Mark.say(e);
				//Entity e = (Entity)row.elementAt(1);
				s.addElement((Entity)row.elementAt(1));
				s.getElement(s.getNumberOfChildren()-1).removeProperty(Markers.MARKUP);
			}
				
		}
		Mark.say("transmitting story version", version, s);
		//BetterSignal message = new BetterSignal(s);
		
		Connections.getPorts(this).transmit(OUT_STORY_SECOND_VERSION, s);
	}

	private void addDebugRow() {
		((DefaultTableModel) model).addRow(new Object[] { includes.getItemAt(0), "Hamlet isn't a thane." });
	}

	public void processSignal(Object signal) {
		Mark.say("SIGNAL RECEIVED FOR WHATIF CONTAINER");

		if (signal instanceof BetterSignal) {
			ready = false;
			while(model.getRowCount() > 0) {
				model.removeRow(0);
			}
			
			// Shows how to take BetterSignal instance apart, the one coming in on COMPLETE_STORY_ANALYSIS_PORT port.
			BetterSignal s = (BetterSignal) signal;
			Sequence story = s.get(0, Sequence.class);
			Sequence explicitElements = s.get(1, Sequence.class);
			Sequence inferences = s.get(2, Sequence.class);
			Sequence concepts = s.get(3, Sequence.class);
			// Now proceed to print what has come into my box.
			// Mark.say("\n\n\nStory elements");
			// for (Entity e : story.getElements()) {
			// Mark.say(e.asString());
			// }

			
			 Mark.say("\n\n\nExplicit story elements");
			 for (Entity e : explicitElements.getElements()) {
				 ((DefaultTableModel) model).addRow(new Object[]{includes.getItemAt(0), e});
			 }
			// this.addDebugRow();
			//
			// this.revalidate();
			// this.repaint();
			//
			// //((DefaultTableModel) model).addRow(new Object[]{includes.getItemAt(0), e.asString()});
			// Mark.say(e.asString());
			// }
			// //this.table.repaint();
			// Mark.say(table.getModel().getRowCount());
			//
			//

			// Mark.say("\n\n\nInstantiated commonsense rules");
			// for (Entity e : inferences.getElements()) {
			// Mark.say(e.asString());
			// }
			// Mark.say("\n\n\nInstantiated concept patterns");
			// for (Entity e : concepts.getElements()) {
			// Mark.say(e.asString());
			// }

		}
		table.revalidate();	
	}
}

// }




@Deprecated
class EntityListTableModel extends AbstractTableModel {
	private String[] columnNames = {};

	private Object[][] data = {};

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return data.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}

	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	public EntityListTableModel setData(String[] cols, Object[][] data) {
		this.columnNames = cols;
		this.data = data;
		this.fireTableDataChanged();
		return this;
	}

}