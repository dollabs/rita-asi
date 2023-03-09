package gui;

import java.awt.*;
import java.awt.print.*;

/**
 * A utility class for easing the printing of screens. To use, arrange for gui to import javal.awt.* and
 * java.awt.print.* and have the gui implement Printable. Then, define the methods commented out below.
 * 
 * @author Patrick Winston
 */
// public int print (Graphics graphics, PageFormat format, int pageIndex) {return EasyPrint.easyPrint(this, graphics,
// format, pageIndex);}
// public void printMe () {EasyPrint.easyPrint(this);}
public class EasyPrint {
	/**
	 * Prints.
	 */
	public static int easyPrint(Component screen, Graphics graphics, PageFormat format, int pageIndex) {
		double offsetX = 0, offsetY = 0;
		Graphics2D g = (Graphics2D) graphics;
		Dimension d = screen.getSize();
		double scaleW = format.getImageableWidth() / d.width;
		double scaleH = format.getImageableHeight() / d.height;
		double scale = 1.0;
		if (scaleW > scaleH) {
			scale = scaleH;
		}
		else {
			scale = scaleW;
		}
		offsetX = format.getImageableX();
		offsetY = format.getImageableY();
		g.translate(offsetX, offsetY);
		g.scale(scale, scale);
		screen.print(g);
		System.out.println("Hello world, I'm runing easy print with four arguments!");
		return Printable.PAGE_EXISTS;
	}

	/**
	 * Arranges to print.
	 */
	public static void easyPrint(Printable screen) {
		System.out.println("Printing...");
		PrinterJob printerJob = PrinterJob.getPrinterJob();
		boolean doPrint = printerJob.printDialog();
		if (!doPrint) {
			System.out.println("Printing cancelled");
			return;
		}
		Book book = new Book();
		PageFormat oldFormat = new PageFormat();
		PageFormat newFormat = printerJob.pageDialog(oldFormat);
		if (oldFormat == newFormat) {
			System.out.println("Printing cancelled");
			return;
		}
		book.append(screen, newFormat);
		printerJob.setPageable(book);
		System.out.println("Hello world, I'm runing easy print with one argument!");
		try {
			printerJob.print();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Printing failed");
		}
	}
}
