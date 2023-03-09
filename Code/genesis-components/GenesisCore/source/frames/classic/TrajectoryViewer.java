/*
 * Created on Jan 31, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package frames.classic;

import java.awt.Color;
import java.util.Vector;

import connections.Ported;
import frames.entities.Entity;
import frames.entities.EntityToViewerTranslator;
import frames.entities.Sequence;

/**
 * @author Keith
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TrajectoryViewer extends FrameViewer {

    /**
     * 
     */
    public TrajectoryViewer() {
        super();
    }

    /**
     * @param t
     */
    public TrajectoryViewer(String t) {
        super(t);
    }
    
    public TrajectoryViewer(String t, int scrollable){
    	super(t, scrollable);
    }

    /**
     * @param t
     * @param color
     */
    public TrajectoryViewer(String t, Color color) {
        super(t, color);
    }
    
    /* (non-Javadoc)
     * @see bridge.views.frameviews.classic.FrameViewer#setInput(bridge.reps.entities.Thing)
     */
    public void setInput(Entity thing) {
        if (thing.sequenceP() && thing.isA("eventSpace")) {
            Vector ladders = ((Sequence)thing).getElements();
            Vector translations = new Vector();
            for (int i = 0; i < ladders.size(); ++i) {
                Entity element = (Entity)(ladders.elementAt(i));
                translations.add(EntityToViewerTranslator.translate(element)); 
                // Send to display
                super.setInputVector(translations);
            }
            if (translations.isEmpty()) {
                super.setInputVector(translations);
            }
        }
    }

// public void setInput(Object input, Object port) {
//  System.err.println("Input to TrajectoryViewer is " + input);
//  if (input instanceof Thing) {
//   setInput((Thing) input);
//  }
// }
}
