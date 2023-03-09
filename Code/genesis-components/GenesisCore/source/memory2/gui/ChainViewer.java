package memory2.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import memory2.datatypes.Chain;
import memory2.datatypes.DoubleBundle;
import connections.Connections;
import connections.WiredBox;
import frames.entities.Thread;


public class ChainViewer extends JPanel implements WiredBox {
	private static final long serialVersionUID = 6564876398596734669L;

	private Chain chain;
	JPanel chainpanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
	JTextArea bundlebox = new JTextArea(5, 50);

	public ChainViewer() {
		super();
		this.setLayout(new BorderLayout());
		chainpanel.setPreferredSize(new Dimension(40, 40));
		this.add(chainpanel, BorderLayout.NORTH);
		bundlebox.setEditable(false);
		JScrollPane areaScrollPane = new JScrollPane(bundlebox);
		areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.add(areaScrollPane, BorderLayout.CENTER);
		this.setBackground(Color.WHITE);

		Connections.getPorts(this).addSignalProcessor("input");
	}

	// receives Chain to display
	public void input(Object input) {
//		System.err.println("ChainViewer received input: "+input);
		if (input instanceof Chain) {
			Chain clean = (Chain) input;
			viewChain(clean);
			return;
		}
		System.err.println("Bad input to ChainViewer: "+input);
	}

	public void viewChain(final Chain c) {
//		SwingUtilities.invokeLater(new Runnable() {
//			public void run() {
				chain = c; 
				chainpanel.removeAll();
				bundlebox.setText("mouse over above items");
				bundlebox.setFont(new Font("Monospaced", Font.ITALIC, 12));
				for(DoubleBundle db : chain) {
					DBLabel label = new DBLabel(db);
					label.setFont(new Font("Sans-serif", Font.PLAIN, 12));
					label.setBorder(BorderFactory.createLineBorder(Color.blue));
					chainpanel.add(label);
					label.addMouseListener(label);
				}
				updateUI();
//			}
//		});
	}

	public void clear() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chain = null;
				chainpanel.removeAll();
				bundlebox.setText("");
			}
		});
	}
	
	private String shortThread(Thread t) {
		if (t.size() > 5) {
			return t.get(0).toString()+" "+t.get(1).toString()+" ... "+t.get(t.size()-3).toString()+" "+t.get(t.size()-2).toString()+" "+t.get(t.size()-1).toString();
		}
		return t.toString(true);
	}

	private class DBLabel extends JLabel implements MouseListener {

		private static final long serialVersionUID = 4272170115844899274L;
		private DoubleBundle db;
		public DBLabel(DoubleBundle d) {			
			super(" "+d.toString()+" ");
			this.db = d;
		}
		public void mouseClicked(MouseEvent e) {
		}
		public void mouseEntered(MouseEvent e) {
//			System.out.println("MOUSED OVER: " + db.toString());
			bundlebox.setText("positive examples:\n");
			for (Thread t : db.getPosSet()) {
				bundlebox.append("   "+shortThread(t)+"\n");
			}
			bundlebox.append("negative examples:\n");
			for (Thread t : db.getNegSet()) {
				bundlebox.append("   "+shortThread(t)+"\n");
			}
			bundlebox.setFont(new Font("Monospaced", Font.PLAIN, 12));
		}
		public void mouseExited(MouseEvent e) {
		}
		public void mousePressed(MouseEvent e) {
		}
		public void mouseReleased(MouseEvent e) {
		}
	}
}
