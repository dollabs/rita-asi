package tomLarson;

import java.awt.Color;
import java.awt.GridLayout;

import connections.Connections;
import connections.Ports;
import gui.WiredPanel;

@SuppressWarnings("rawtypes")
public class BundleViewer extends WiredPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7213110525847447483L;
	Ports		ports;
	BundleViewerHelper	bvh	= new BundleViewerHelper();
	ThreadTree input = null;
	public BundleViewer() {
		Connections.getPorts(this).addSignalProcessor("process");
		this.setLayout(new GridLayout(1, 1));
		this.add(this.bvh);
		this.setBackground(Color.WHITE);
	}
	public void process(Object signal) {
		if (signal instanceof ThreadTree || signal == null) {
			input = (ThreadTree) signal;
			//System.out.println(input);
			//System.out.println("Everything viewer input: \n" + input);
			this.bvh.setInput(input);
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
