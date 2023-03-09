package gui.images;

import utils.Anchor;

/**
 * Created on January 9 ,2008
 * @author Raymond Cheng
 * 
 * Merely a wrapper for Anchor.java
 * get(String filename) returns the full path to that file
 *  
 * Ex:  public FooAnchor extends Anchor { }
 * then somewhere else,
 * new FooAnchor().get("bar.txt")
 * gives back the full path of bar.txt in the same directory as FooAnchor
 */

public class GuiImagesAnchor extends Anchor {

}
