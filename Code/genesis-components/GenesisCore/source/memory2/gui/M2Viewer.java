package memory2.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import memory2.datatypes.Chain;
import memory2.M2;
import memory2.Mem;
import connections.Connections;
import connections.WiredBox;

public class M2Viewer extends JPanel  implements WiredBox{
	private static final long serialVersionUID = 7584315067624862864L;
//	CirclePanel circle = new CirclePanel();
	M2List m2list = new M2List();
	ChainViewer chainviewer = new ChainViewer();
	JButton refreshButton = new JButton("Refresh");
	
//	private M2Viewer parent;
	
	public M2Viewer() {
		super();
		this.setLayout(new BorderLayout());
//		this.add(this.circle, BorderLayout.CENTER);
		m2list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		m2list.setVisibleRowCount(-1);
		m2list.addListSelectionListener(m2list);
		JScrollPane listScroller = new JScrollPane(m2list);
		listScroller.setPreferredSize(new Dimension(250, 80));
		
		refreshButton.addActionListener(new RefreshListener());
		
		this.add(refreshButton, BorderLayout.NORTH);
		this.add(listScroller, BorderLayout.CENTER);
		this.add(this.chainviewer, BorderLayout.SOUTH);
		Connections.getPorts(this).addSignalProcessor("input");
//		this.parent = this;
	}
	
	public void display(final Set<Chain> chains) {
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				if (chains != null) {
					m2list.setListData(chains.toArray());
				}
				chainviewer.clear();
//			}
//		});
	}
	// receives SOMs to display
	@SuppressWarnings("unchecked")
	public void input(Object input) {
		if (input instanceof Set) {
			Set<Chain> clean = (Set<Chain>) input;
			if (!clean.isEmpty() && clean.toArray()[0] instanceof Chain) {
				this.display(clean);
				return;
			}
		}
		System.err.println("Bad input to M2Viewer: "+input);
	}
	
	
	private void showSelectedChain() {
		if (m2list.getSelectedIndex() == -1) {
			//No selection
		} else {
			//Selection
			Chain selected = (Chain) m2list.getSelectedValue();
			chainviewer.viewChain(selected);
		}
	}
	
	private class M2List extends JList implements ListSelectionListener {
		private static final long serialVersionUID = -903641950356317204L;
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting() == false) {
				showSelectedChain();
		    }
		}
	}
	
	private class RefreshListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Mem mem = M2.getMem();
			mem.outputAll();
		}
		
	}
}
