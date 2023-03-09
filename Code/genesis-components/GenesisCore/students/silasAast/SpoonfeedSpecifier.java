package silasAast;

import connections.AbstractWiredBox;
import connections.Connections;
import frames.entities.Sequence;

public class SpoonfeedSpecifier extends AbstractWiredBox {
	
	// FIELDS
	private static Sequence desiredReaction = new Sequence(); 
	
	public SpoonfeedSpecifier(){

	}

	public static Sequence setGoal(Sequence narratorView){
		if (narratorView instanceof Sequence && narratorView!=null){
			desiredReaction = (Sequence) narratorView;
		}
		return desiredReaction;
	}
	
}
