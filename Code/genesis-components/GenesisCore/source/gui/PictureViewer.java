package gui;

import gui.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.net.*;

import javax.swing.*;

import utils.Mark;
import viz.images.ImageAnchor;
import connections.Connections;

/*
 * Copy of class in PHW's utilities
 */

public class PictureViewer extends NegatableJPanel {

	private Image image = null;

	private int frameRate = 1000 / 30;

	private File source;

	// public PictureViewer(String name) {
	// this();
	// setImage(name);
	// }
	//
	// public PictureViewer(File file) {
	// this();
	// setImage(file.getPath());
	// setPreferredSize(new Dimension(150, 150));
	// source = file;
	//
	// }
	//
	// public PictureViewer() {
	// setBackground(Color.WHITE);
	// setOpaque(true);
	// // setBorder(BorderFactory.createTitledBorder("Image"));
	// // setBorder(BorderFactory.createLineBorder(Color.BLUE));
	// Connections.getPorts(this).addSignalProcessor("view");
	// }

	public void view(Object o) {
		if (o instanceof String) {
			setImage((String) o);
		}
	}

	public void setImage(String name) {
		URL url;
		try {
			url = ImageAnchor.class.getResource(name);
			// Mark.say("URL", url);
			SwingUtilities.invokeLater(new Display(url));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	class Display implements Runnable {
		URL name;

		public Display(URL name) {
			this.name = name;
		}

		public void run() {
			ImageIcon icon = new ImageIcon(name);
			setImage(icon.getImage());
		}
	}

	public void setImage(Image image) {
		this.image = image;
		repaint();
	}

	public void setImage(ImageIcon image) {
		this.image = image.getImage();
		repaint();
	}

	public void clear() {
		image = null;
		repaint();
	}

	int yOffset = 0;

	public int getYOffset() {
		return yOffset;
	}

	public void setYOffset(int offset) {
		yOffset = offset;
	}

	public File getSource() {
		return source;
	}

	public void paint(Graphics graphics) {
		super.paint(graphics);
		if (image == null) {
			return;
		}

		Graphics2D g = (Graphics2D) graphics;
		int pWidth = getWidth();
		int pHeight = getHeight() - yOffset;
		int iWidth = image.getWidth(this);
		int iHeight = image.getHeight(this);
		int inset = Math.min(pWidth, pHeight) / 20;
		pWidth -= 2 * inset;
		pHeight -= 2 * inset + 10;
		// Determine governing dimension
		int xBorder = 0, yBorder = 0;
		double scale = 1.0;
		if ((float) pHeight / pWidth < (float) iHeight / iWidth) {
			// Image is too tall
			scale = (double) pHeight / iHeight;
			xBorder = (int) (pWidth - scale * iWidth) / 2;
		}
		else {
			// Image is to wide
			scale = (double) pWidth / iWidth;
			yBorder = (int) (pHeight - scale * iHeight) / 2;
		}
		AffineTransform t = AffineTransform.getTranslateInstance(xBorder + inset, yBorder + inset + yOffset);
		t.scale(scale, scale);
		g.drawImage(image, t, this);
		g.drawRect(2, 2, getWidth() - 4, getHeight() - 4);
		if (source != null) {
			FontMetrics fm = g.getFontMetrics();
			int width = fm.stringWidth(source.getName());
			g.drawString(source.getName(), (pWidth - width) / 2, getHeight() - 5);
		}
	}

	// public static void main(String[] args) {
	// ImagePanel view = new ImagePanel();
	// JFrame frame = new JFrame();
	// frame.getContentPane().setLayout(new BorderLayout());
	// frame.getContentPane().add(view, BorderLayout.CENTER);
	// frame.setBounds(0, 0, 400, 400);
	// frame.setVisible(true);
	// view.processInput("bellbell.jpg");
	//
	// }

}
