package gui;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.net.URL;

import javax.swing.*;

import connections.*;
import viz.images.ImageAnchor;

/*
 * Created on Jun 5, 2007 Extended on 29 Sep 2007 by phw to handle movies as well as images @author phw
 */

public class BasicPicturePanel extends JPanel {

	private BufferedImage image = null;

	private int frameRate = 1000 / 30;

	private File source;

	int xOffset = 0, yOffset = 0;

	double scale = 1.0;

	int circleX, circleY, circleW;

	boolean drawCircle = false;

	public BasicPicturePanel() {
		setBackground(Color.WHITE);
		setOpaque(true);
		// setBorder(BorderFactory.createTitledBorder("Image"));
		setBorder(BorderFactory.createLineBorder(Color.BLUE));
		// Connections.getPorts(this).addSignalProcessor("getInput");
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	public void getInput(Object o) {
		if (o instanceof String) {
			setImage((String) o);
		}
		else if (o instanceof File) {
			File file = (File) o;
			source = file;
			setImage(file.getName());

		}
		else if (o instanceof BufferedImage) {
			setImage((BufferedImage) o);
		}
		else if (o == null) {
		}
		else {
			System.err.println("ImagePanel.setImage got a " + o.getClass());
		}
	}

	public void setImage(String name) {
		setImage(this.getBufferedImageFromIconFileName(name));
	}

	/*
	 * Seems to work in both eclipse and jnlpa, god knows why, code from internet
	 */
	public static BufferedImage getBufferedImageFromIconFileName(String name) {
		JPanel p = new JPanel();
		URL url = ImageAnchor.class.getResource(name);
		ImageIcon icon = new ImageIcon(url);
		BufferedImage b = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
		icon.paintIcon(p, b.createGraphics(), 0, 0);
		return b;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
		repaint();
	}

	public void clear() {
		image = null;
		repaint();
	}

	public void paint(Graphics graphics) {
		super.paint(graphics);
		if (image == null) {
			return;
		}
		// System.err.println("Painting");
		paintImage(graphics, image);
	}

	private void paintImage(Graphics g, RenderedImage anImage) {
		// System.err.println("Painting image");
		int paneWidth = getWidth();
		int paneHeight = getHeight();
		int imageWidth = anImage.getWidth();
		int imageHeight = anImage.getHeight();
		int inset = 15;
		int effectiveWidth = paneWidth - 2 * inset;
		int effectiveHeight = paneHeight - 2 * inset;
		// Determine governing dimension
		if ((double) effectiveHeight / effectiveWidth < (double) imageHeight / imageWidth) {
			// Image is too tall
			scale = ((double) effectiveHeight) / imageHeight;
		}
		else {
			// Image is to wide
			scale = ((double) effectiveWidth) / imageWidth;
		}
		xOffset = (int) ((paneWidth - scale * imageWidth) / 2);
		yOffset = (int) ((paneHeight - scale * imageHeight) / 2);

		// String comment = "Offsets are " + xOffset + "/" + yOffset;
		int width = (int) (scale * imageWidth);
		int height = (int) (scale * imageHeight);
		// comment += " and pane dimensions are " + paneWidth + "/" + paneHeight
		// + " with image dimensions " + width + "/" + height;
		// System.out.println(comment);
		g.drawImage(image, xOffset, yOffset, width, height, this);
		drawTheCircle(g);
	}

	// public static void main(String[] args) {
	// ImagePanel view = new ImagePanel();
	// //
	// // URL movieUrl = ClipsBridge.class.getResource("ClipsAnchor.txt");
	// // File file = new File(movieUrl.getFile());
	// // file = file.getParentFile().getParentFile().getParentFile();
	// //
	// // String sep = System.getProperty("file.separator");
	// //
	// // File input = new File(file + sep + "between_0_0");
	// // System.err.println("File: " + file);
	// // // view.setMovie(input);
	// // input = new File(file + sep + "between_1_0");
	// // // view.setMovie(input);
	// // input = new File(file + sep + "between_0_1");
	// // // view.setMovie(input);
	// // input = new File(file + sep + "kiss_0_0");
	// // // view.setMovie(input);
	// // input = new File(file + sep + "falls_0_0");
	// // // view.setMovie(input);
	// // input = new File(file + sep + "over_0_0");
	// // view.setMovie(input);
	//
	// // view.setBackground(Color.YELLOW);
	//
	// // view.setImage("animated-test.gif");
	// JFrame frame = new JFrame();
	// frame.getContentPane().setLayout(new BorderLayout());
	// frame.getContentPane().add(view, BorderLayout.CENTER);
	// frame.setBounds(0, 0, 400, 400);
	// frame.setVisible(true);
	// QueuingWiredBox box = new QueuingWiredBox();
	// Connections.wire(box, view);
	// box.process("dog.jpg");
	// box.process("tree.jpg");
	// }

	public double getScale() {
		return scale;
	}

	public int getXOffset() {
		return xOffset;
	}

	public int getYOffset() {
		return yOffset;
	}

	public BufferedImage getImage() {
		return image;
	}

	public void drawACircle(int x, int y, int w) {
		w *= scale / 2;
		circleX = x - w;
		circleY = y - w;
		circleW = 2 * w;
		drawCircle = true;
		repaint();
	}

	private void drawTheCircle(Graphics g) {
		if (drawCircle) {
			g.drawOval(circleX, circleY, circleW, circleW);
		}
	}
}
