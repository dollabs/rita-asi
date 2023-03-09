package frames.classic;

import java.util.*;
import java.awt.*;

public class FrameBundle {
	private Color barColor = Color.gray;

	public void setBarColor(Color c) {
		barColor = c;
	}

	public Color getBarColor() {
		return barColor;
	}

	private String title = null;

	public void setTitle(String t) {
		title = t;
	}

	public String getTitle() {
		return title;
	}

	private String topText = "";

	private String bottomText = "";
	
	private String id = "";

	private Vector<FrameBundle> bundles = new Vector<FrameBundle>();

	public void setTop(String t) {
		topText = t;
	}

	public String getTop() {
		return topText;
	}

	public void setBottom(String t) {
		bottomText = t;
	}

	public void addFrameBundle(FrameBundle b) {
		bundles.add(b);
	}

	public Vector getFrameBundles() {
		return bundles;
	}

	private boolean showNoThreads = false;
	
	private boolean negated = false;

	/**
	 * Calculates depth of nesting.
	 */
	public int depth() {
		int result = 1;
		for (int i = 0; i < bundles.size(); ++i) {
			FrameBundle f = (FrameBundle) (bundles.elementAt(i));
			int x = f.depth();
			// result = Math.max(result, x + 1);
			result += x;
		}
		return result;
	}

//	public FrameBundle(String top, Vector bottom, Color c) {
//		this(top, bottom);
//		barColor = c;
//	}

	public FrameBundle(String top, Vector bottom, boolean negated) {
		topText = top;
		bottomText = "";
		bottom = sort(bottom);
		for (int i = 0; i < bottom.size() - 1; ++i) {
			bottomText += ((String) (bottom.elementAt(i))) + ((i == 0) ? ": " : ", ");
		}
		bottomText += ((String) (bottom.elementAt(bottom.size() - 1)));
		setNegated(negated);
	}

	private Vector sort(Vector<String> bottom) {
		Vector result = new Vector();
		result.add(bottom.firstElement());
		id = bottom.firstElement();
		bottom.remove(0);
		if (showNoThreads) {
			return result;
		}
		for (String s : bottom) {
			if (s.startsWith("feature")) {
				result.add(s);
			}
		}
		for (String s : bottom) {
			if (!s.startsWith("feature")) {
				result.add(s);
			}
		}
		// TODO Auto-generated method stub
		return result;
	}

	public String toString() {
		return toString(0);
	}

	private String toString(int depth) {
		String filler = "";
		for (int i = 0; i < depth; i++)
			filler += "  ";
		String s = filler + topText + "\n";
		for (int i = 0; i < bundles.size(); i++) {
			Object o = bundles.get(i);
			String substring;
			if (o instanceof FrameBundle) {
				substring = ((FrameBundle) o).toString(depth + 1);
				s += substring + "\n";
			}
			else {
				substring = o.toString();
				s += filler + substring + "\n";
			}
		}
		s += filler + bottomText;
		return s;
	}

	public void setShowNoThreads(boolean b) {
		showNoThreads = b;
	}
	
	public String getListenerBottom() {
		return bottomText;
	}

	public String getBottom() {
		if (showNoThreads) {
			return id;
		}
		return bottomText;
	}

	public boolean isNegated() {
	    return negated;
    }

	public void setNegated(boolean negated) {
	    this.negated = negated;
    }

}
