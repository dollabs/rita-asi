package gui.panels;

import java.awt.Dimension;

import javax.swing.JLabel;



/*
 * @copyright Ascent Technology, Inc, 2005
 */
public class TitledJPanelAdvice extends JLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1972370876623480936L;

	public TitledJPanelAdvice() {
		super();
		// setContentType("text/html");
		// setEditable(false);
		setOpaque(false);
		setBorder(null);
	}

	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		return new Dimension(d.width, d.height + 5);
	}
	
	
	public final static String adviceHeader = "<html><body><span style=\"color: #2B9F79; font-family:Arial, Verdana, Helvetica, sans-serif; font-size: 14pt; font-weight: bold\">";

	public final static String adviceTrailer = "</span></body></html>";

	public void setText(String advice) {
		String text = adviceHeader;
		text += advice;
		text += adviceTrailer;
		super.setText(text);
	}

}
