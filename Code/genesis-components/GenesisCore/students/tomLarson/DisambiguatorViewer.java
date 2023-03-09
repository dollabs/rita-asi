package tomLarson;

import java.awt.Color;
import java.awt.GridLayout;

import connections.Connections;
import connections.Ports;
import gui.WiredPanel;

@SuppressWarnings("rawtypes")
public class DisambiguatorViewer extends WiredPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7213110525847447483L;
	Ports		ports;
	DisambiguatorViewerHelper	dvh	= new DisambiguatorViewerHelper();
	ThreadTree input = null;
	public DisambiguatorViewer() {
		Connections.getPorts(this).addSignalProcessor("process");
		this.setLayout(new GridLayout(1, 1));
		this.add(this.dvh);
		this.setBackground(Color.WHITE);
	}
	public void process(Object signal) {
		if (signal instanceof ThreadTree || signal == null) {
			input = (ThreadTree) signal;
			//System.out.println("Everything viewer input: \n" + input);
			this.dvh.setInput(input);
		} else {
			//System.err.println(this.getClass().getName() + ": Didn't know what to do with input of type "
			//		+ signal.getClass().toString() + ": " + signal + " in NewFrameViewer");
		}
	}
	
	public void clear() {
	//	this.fv.clearData();
	}
	public Ports getPorts() {
		if (this.ports == null) {
			this.ports = new Ports();
		}
		return this.ports;
	}
	public ThreadTree getInput() {return input;}
}
