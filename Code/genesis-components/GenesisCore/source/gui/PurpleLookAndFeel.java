package gui;

import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.basic.BasicLookAndFeel;

public class PurpleLookAndFeel extends BasicLookAndFeel {
	public PurpleLookAndFeel() {
		super();

	}
	
	public String getName() {return "the purple test";}

	@Override
	protected void initSystemColorDefaults(UIDefaults table) {
		String[] defaultSystemColors = { "desktop", "#000000", /*
																 * Color of the
																 * desktop
																 * background
																 */
		"activeCaption", "#00FF00", /*
									 * Color for captions (title bars) when they
									 * are active.
									 */
		"activeCaptionText", "#000000", /*
										 * Text color for text in captions
										 * (title bars).
										 */
		"activeCaptionBorder", "#000000", /*
											 * Border color for caption (title
											 * bar) window borders.
											 */
		"inactiveCaption", "#808080", /*
										 * Color for captions (title bars) when
										 * not active.
										 */
		"inactiveCaptionText", "#C0C0C0", /*
											 * Text color for text in inactive
											 * captions (title bars).
											 */
		"inactiveCaptionBorder", "#C0C0C0", /*
											 * Border color for inactive caption
											 * (title bar) window borders.
											 */
		"window", "#FF0000", /* Default color for the interior of windows */
		"windowBorder", "#0000FF", /* ??? */
		"windowText", "#0000FF", /* ??? */
		"menu", "#C0C0C0", /* Background color for menus */
		"menuPressedItemB", "#000080", /* LightShadow of menubutton highlight */
		"menuPressedItemF", "#FFFFFF", /*
										 * Default color for foreground "text"
										 * in menu item
										 */
		"menuText", "#000000", /* Text color for menus */
		"textText", "#FF0000", /* Text foreground color */
		"textHighlight", "#000080", /* Text background color when selected */
		"textHighlightText", "#FFFFFF", /* Text color when selected */
		"textInactiveText", "#808080", /* Text color when disabled */
		"control", "#0000FF", /*
								 * Default color for controls (buttons, sliders,
								 * etc)
								 */
		"controlText", "#00FF00", /* Default color for text in controls */
		"controlHighlight", "#C0C0C0",

		/* "controlHighlight", "#E0E0E0", *//*
											 * Specular highlight (opposite of
											 * the shadow)
											 */
		"controlLtHighlight", "#000000", /* Highlight color for controls */
		"controlShadow", "#AA0000", /* Shadow color for controls */
		"controlDkShadow", "#000000", /* Dark shadow color for controls */
		"scrollbar", "#00FF00", /* Scrollbar background (usually the "track") */
		"info", "#FFFFE1", /* ??? */
		"infoText", "#000000" /* ??? */
		};

		loadSystemColors(table, defaultSystemColors, isNativeLookAndFeel());
	}

	@Override
	public boolean isSupportedLookAndFeel() {
		return true;
	}

	public static void main(String[] args) {
		try {
			
			PurpleLookAndFeel laf = new PurpleLookAndFeel();
			// laf.initSystemColorDefaults(laf.getDefaults());
			// UIManager.setLookAndFeel(laf);
			LookAndFeelInfo[] info = UIManager.getInstalledLookAndFeels();
			for (int i = 0; i < info.length; ++i) {
				System.out.println("Feel: " + info[i]);
			}
			
			UIManager.setLookAndFeel(laf);
			
//			UIManager.setLookAndFeel(new MetalLookAndFeel());
			
//     		UIManager.setLookAndFeel(new WindowsClassicLookAndFeel());
			
//			UIManager.setLookAndFeel(new WindowsLookAndFeel());
			
//			UIManager.setLookAndFeel(new MotifLookAndFeel());
			
//			UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

//			UIManager.put("activeCaption", Color.YELLOW);
			
//			UIManager.put("inactiveCaption", Color.RED);
			
			System.out.println("The look and feel, system class name: " + UIManager.getSystemLookAndFeelClassName());
			
			System.out.println("The look and feel, look and feel class name: " + UIManager.getLookAndFeel());
			
			System.out.println("The look and feel, name: " + UIManager.getLookAndFeel().getName());

		} catch (Exception x) {
			for (StackTraceElement e : x.getStackTrace()) {
				System.out.println(e);
			}
		}

		JFrame.setDefaultLookAndFeelDecorated(true); 
		JDialog.setDefaultLookAndFeelDecorated(true);
		System.setProperty("sun.awt.noerasebackground", "true");
		
		JFrame jframe = new JFrame("Hello world");
		
		// JFrame.setDefaultLookAndFeelDecorated(false);
		
		JFrame jframe2 = new JFrame("Goodby world");
		
		jframe.getToolkit().beep();
		
		// SwingUtilities.updateComponentTreeUI(jframe);
		
		
		jframe.setLayout(new BorderLayout());

		jframe.getContentPane().add(new JButton("Hello."), BorderLayout.NORTH);
		jframe.getContentPane().add(new JSlider(19, 42), BorderLayout.SOUTH);
		jframe.getContentPane().add(new JTextArea("Foo\nBar"), BorderLayout.CENTER);

		jframe.setSize(640, 480);
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.setVisible(true);

	
		jframe2.setBounds(100, 100, 640, 480);
		jframe2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe2.setVisible(true);
}

	@Override
    public String getDescription() {
	    return null;
    }

	@Override
    public String getID() {
	    return null;
    }

	@Override
    public boolean isNativeLookAndFeel() {
	    return false;
    }
}
