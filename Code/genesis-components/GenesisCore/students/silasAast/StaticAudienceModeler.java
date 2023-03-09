package silasAast;

import java.io.File;

import genesis.FileSourceReader;
import mentalModels.MentalModel;
import connections.AbstractWiredBox;
import connections.Connections;
import frames.entities.Sequence;

public class StaticAudienceModeler extends AbstractWiredBox {

	//INPUT PORTS
	public static String DECLARED_AUDIENCE = "declared audience"; //from StoryEnvironment
	public static String DECLARED_COMMONSENSE = "commonsense rules for declared audience"; //from StoryProcessor via MentalModel
	public static String DECLARED_CONCEPTS = "reflective rules for declared audience"; //from StoryProcessor via MentalModel
	
	
	//OUTPUT PORTS 
	public static String AUDIENCE_COMMONSENSE_OUT; //to StoryPreSimulator
	public static String AUDIENCE_REFLECTIVE_OUT; //to Story PreSimulator
	
	//FIELDS
	public Sequence commonsenseRules = new Sequence();
	public Sequence conceptRules = new Sequence();
	public String declaredAudience;
	
	public StaticAudienceModeler(){
		Connections.getPorts(this).addSignalProcessor(DECLARED_AUDIENCE, "modelAudience");
		
	}
	
	public void modelAudience(Object audience){
		if (audience instanceof String){
			declaredAudience = (String) audience;
		}
		
		String commonsenseFileName = declaredAudience+"Commonsense";
		String conceptsFileName = declaredAudience+"Reflective";
		
		MentalModel commonsenseModel = new MentalModel("NarratorModel",commonsenseFileName);
		MentalModel conceptModel = new MentalModel("NarratorModel", conceptsFileName);
		
		commonsenseRules = commonsenseModel.getCommonsenseRules();
		conceptRules = conceptModel.getConceptPatterns();

		Connections.getPorts(this).transmit(AUDIENCE_COMMONSENSE_OUT, commonsenseRules);
		Connections.getPorts(this).transmit(AUDIENCE_REFLECTIVE_OUT, conceptRules);
		
	}
}
