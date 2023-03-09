package gui;

import genesis.GenesisGetters;

import java.awt.*;

import javax.swing.*;

import utils.Talker;
import connections.*;

/*
 * Created on Mar 5, 2009
 * @author phw
 */

public class TextBox extends JLabel implements WiredBox {

	GenesisGetters genesisGetters;

	public TextBox(GenesisGetters genesisGetters) {
		super("", JLabel.CENTER);
		this.genesisGetters = genesisGetters;
		Font font = this.getFont();
		this.setFont(new Font(font.getFamily(), Font.BOLD, 15));
		this.setBackground(Color.YELLOW);
		this.setOpaque(true);
		Connections.getPorts(this).addSignalProcessor("process");
	}

	public void process(Object o) {
		if (o instanceof String) {
			setText(o.toString());
			genesisGetters.getWindowGroupManager().setGuts(genesisGetters.getRightPanel(), this);
			// getters.getOutputTabbedPane().setSelectedComponent(this);
			Connections.getPorts(this).transmit(o.toString());
		}
	}

	public static void main(String[] args) {
		try {
			JFrame frame = new JFrame();
			frame.setBounds(200, 200, 400, 500);
			frame.setVisible(true);
			TextBox talker = new TextBox(new GenesisGetters());
			frame.getContentPane().add(talker);
			talker.process("I have learned that the man touched the woman because the man ran into the woman");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
