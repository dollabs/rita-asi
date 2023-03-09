package hibaAwad;

import utils.Mark;
import connections.*;
import frames.entities.Entity;
import mentalModels.MentalModel;

/*
 * Created on Apr 27, 2013
 * @author phw
 */

public class MagicBox extends AbstractWiredBox {

	MentalModel mentalModel;

	public MagicBox(MentalModel mm) {
		mentalModel = mm;
		Connections.getPorts(this).addSignalProcessor("process");
	}
	
	public void process(Object o) {
		if (o instanceof Entity) {
			Entity t = (Entity)o;
			Mark.say("MagicBox received", t.asString());
			
			String traitName = t.getObject().getType();
			
			Mark.say("Personality trait is", traitName);
			
			MentalModel traitModel = mentalModel.getLocalMentalModel(traitName);
			
			Mark.say("Someone is", traitName);
			
			if (traitModel != null) {
				Mark.say("Found mental model for", traitName);
			}
			else {
				Mark.say("Did not find mental model for", traitName);
			}
		}
	}

}
