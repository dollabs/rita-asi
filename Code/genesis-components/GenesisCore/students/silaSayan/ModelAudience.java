package silaSayan;

import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;

public class ModelAudience extends AbstractWiredBox {
	
	private String declaredAudience;

	//PORTS
	
	private static String DECLARED_AUDIENCE = "audience declared by user";
	
	public ModelAudience(){
		this.setName("Audience Modeller");
		Connections.getPorts(this).addSignalProcessor(DECLARED_AUDIENCE, "establishAudience");
		
	}
	
	//@TODO: transmit on specific port
	public void establishAudience(Object o){
		if (o instanceof String){
			declaredAudience = (String) o;
		}
		BetterSignal signal = new BetterSignal("Declared Audience", declaredAudience);
		Connections.getPorts(this).transmit(signal);
	}
	
	
	public static void loadAudienceModel(int audienceType){
		return;
	}
}
