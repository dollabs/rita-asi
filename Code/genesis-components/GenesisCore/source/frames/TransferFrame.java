package frames;

import constants.RecognizedRepresentations;
import frames.entities.Entity;

/*
 * Implemented to parallel Ben's frames, but minimalist.
 * Created on Nov 10, 2008
 * @author phw
 */

public class TransferFrame extends Frame{
	public static final String FRAMETYPE = (String) RecognizedRepresentations.TRANSFER_THING;
    public Entity getThing() {
	    return null;
    }

}
