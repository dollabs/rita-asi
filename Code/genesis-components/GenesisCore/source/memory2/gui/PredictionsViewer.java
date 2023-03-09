package memory2.gui;

import java.awt.Dimension;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import memory2.models.World;
import connections.Connections;
import connections.WiredBox;
import frames.entities.Entity;

public class PredictionsViewer extends JPanel implements WiredBox{
	private static final long serialVersionUID = 7584315067624862864L;
	private ThingList l1list = new ThingList();
	private ThingList l2list = new ThingList();
	private ThingList l3list = new ThingList();
	private ThingList l4list = new ThingList();
//	private ThingList l5list = new ThingList();

	public PredictionsViewer() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
	
		JLabel l1label = new JLabel(World.EXPLICIT);
		l1list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		l1list.setVisibleRowCount(-1);
		l1list.addListSelectionListener(l1list);
		JScrollPane l1Scroller = new JScrollPane(l1list);
		l1Scroller.setPreferredSize(new Dimension(250, 80));
		this.add(l1label);
		this.add(l1Scroller);
		
		JLabel l2label = new JLabel(World.CIRCUMSTANTIAL);
		l2list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		l2list.setVisibleRowCount(-1);
		l2list.addListSelectionListener(l2list);
		JScrollPane l2Scroller = new JScrollPane(l2list);
		l2Scroller.setPreferredSize(new Dimension(250, 80));
		this.add(l2label);
		this.add(l2Scroller);
		
		JLabel l3label = new JLabel(World.HISTORICAL);
		l3list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		l3list.setVisibleRowCount(-1);
		l3list.addListSelectionListener(l3list);
		JScrollPane l3Scroller = new JScrollPane(l3list);
		l3Scroller.setPreferredSize(new Dimension(250, 80));
		this.add(l3label);
		this.add(l3Scroller);
		
		JLabel l4label = new JLabel(World.ANALOGICAL);
		l4list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		l4list.setVisibleRowCount(-1);
		l4list.addListSelectionListener(l4list);
		JScrollPane l4Scroller = new JScrollPane(l4list);
		l4Scroller.setPreferredSize(new Dimension(250, 80));
		this.add(l4label);
		this.add(l4Scroller);
		
//		JLabel l5label = new JLabel("Level 5:");
//		l5list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//		l5list.setVisibleRowCount(-1);
//		l5list.addListSelectionListener(l5list);
//		JScrollPane l5Scroller = new JScrollPane(l5list);
//		l5Scroller.setPreferredSize(new Dimension(250, 80));
//		this.add(l5label);
//		this.add(l5Scroller);
		
		Connections.getPorts(this).addSignalProcessor("input");
	}
	
	public void display(final Map<String, List<Entity>> preds) {
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				clearAllLists();
				if (preds != null) {
					if (preds.containsKey(World.EXPLICIT)) l1list.setListData(preds.get(World.EXPLICIT).toArray());
					if (preds.containsKey(World.CIRCUMSTANTIAL)) l2list.setListData(preds.get(World.CIRCUMSTANTIAL).toArray());
					if (preds.containsKey(World.HISTORICAL)) l3list.setListData(preds.get(World.HISTORICAL).toArray());
					if (preds.containsKey(World.ANALOGICAL)) l4list.setListData(preds.get(World.ANALOGICAL).toArray());
//					if (preds.containsKey("l5")) l5list.setListData(preds.get("l5").toArray());
				}
//			}
//		});
	}
	
	// receives SOMs to display
	@SuppressWarnings("unchecked")
	public void input(Object input) {
		if (input instanceof Map) {
			this.display((Map<String, List<Entity>>)input);
			return;
		}
		System.err.println("Bad input to PredictionsViewer");
	}
	
	private void showSelectedThing(Entity t) {
		Connections.getPorts(this).transmit(t);
	}

	private void clearOtherLists(ThingList lst) {
		if (l1list!=lst) l1list.clearSelection();
		if (l2list!=lst) l2list.clearSelection();
		if (l3list!=lst) l3list.clearSelection();
		if (l4list!=lst) l4list.clearSelection();
//		if (l5list!=lst) l5list.clearSelection();
	}
	
	private void clearAllLists() {
		l1list.setListData(new Vector<String>());
		l2list.setListData(new Vector<String>());
		l3list.setListData(new Vector<String>());
		l4list.setListData(new Vector<String>());
//		l5list.setListData(new Vector<String>());
	}
	
	private class ThingList extends JList implements ListSelectionListener {
		private static final long serialVersionUID = -903641950356317204L;
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				if (this.getSelectedIndex() == -1) {
					//No selection
				} else {
					//Selection
					Entity selected = (Entity) this.getSelectedValue();
					clearOtherLists(this);
					showSelectedThing(selected);
				}
		    }
		}
	}
}
