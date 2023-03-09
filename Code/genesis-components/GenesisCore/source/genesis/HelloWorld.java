package genesis;

import javax.swing.*;

/*
 * Created on Oct 28, 2008
 * @author phw
 */

public class HelloWorld extends JFrame {

	public static void main(String[] args) {
		HelloWorld hw = new HelloWorld();
		hw.getContentPane().add(new JLabel("Hello World"));
		hw.setBounds(0, 0, 500, 500);
		hw.setVisible(true);
		System.gc();
		System.out.println("Memory A1: " + Runtime.getRuntime().freeMemory());
		System.out.println("Memory A2: " + Runtime.getRuntime().totalMemory());
		Genesis genesis = Genesis.getGenesis();
		System.out.println("Memory B1: " + Runtime.getRuntime().freeMemory());
		System.out.println("Memory B2: " + Runtime.getRuntime().totalMemory());
		genesis.startInFrame();
		System.gc();
		System.out.println("Memory C1: " + Runtime.getRuntime().freeMemory());
		System.out.println("Memory C2: " + Runtime.getRuntime().totalMemory());

	}

}
