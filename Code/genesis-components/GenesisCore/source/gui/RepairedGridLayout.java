package gui;

/* 
 * Copyright 1999 Ascent Technology, Inc. All rights reserved.
 * Written by PHW; stamped 5 May 2000 
 */

/**
 * Fixes bug in Sun implementation that screws up spacing when there are a
 * lot of components
 */

import java.awt.*;

public class RepairedGridLayout extends GridLayout {

 int total = 0;

 public RepairedGridLayout (int r, int c) {super(r, c); total = r * c;}

    public void layoutContainer(Container parent) {
      synchronized (parent.getTreeLock()) {
        Insets insets = parent.getInsets();
	int ncomponents = parent.getComponentCount();
        // System.out.println("Grid laying out components (" + ncomponents + ")");
/*
        if (ncomponents < total) {
         System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Grid NOT laying out");	
         return;
        }
        else {
         System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Grid laying out");	
        }
*/
	int nrows = getRows();
	int ncols = getColumns();
	if (ncomponents == 0) {
	    return;
	}
	if (nrows > 0) {
	    ncols = (ncomponents + nrows - 1) / nrows;
	} else {
	    nrows = (ncomponents + ncols - 1) / ncols;
	}
	int width = parent.getWidth() - (insets.left + insets.right);
	int height = parent.getHeight() - (insets.top + insets.bottom);
	int w = (width - (ncols - 1) * getHgap()) / ncols;
	int h = (height - (nrows - 1) * getVgap()) / nrows;

	for (int c = 0 ; c < ncols ; c++) {
            int xx = insets.left + (c * width / ncols);
            int ww = insets.left + ((c + 1) * width / ncols) - xx - getHgap();
	    for (int r = 0 ; r < nrows ; r++) {
                int yy = insets.top + (r * height / nrows);
                int hh = insets.top + ((r + 1) * height / nrows) - yy - getVgap();
		int i = r * ncols + c;
		if (i < ncomponents) {
                    // System.out.println("Repaired grid layout setting bounds");
		    parent.getComponent(i).setBounds(xx, yy, ww, hh);
		}
	    }
	}
      }
    }
    
}


/* 
 * Copyright 1999 Ascent Technology, Inc. All rights reserved.
 */



