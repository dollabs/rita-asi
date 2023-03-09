package connections;

/**
 * @author adk
 * a WiredBox used to test and demonstrate cross-language functionality
 *
 */
public class SimpleTestBox implements WiredBox{

	@Override
	public String getName() {
		return "Simple Test Box";
	}
	
	public void print(Object in){
		System.out.println("Java Simple test box got: "+in);
	}
	
	public SimpleTestBox(){
		Connections.getPorts(this).addSignalProcessor("print");
	}
	
	public void transmit(){
		Connections.getPorts(this).transmit("sent from Java");
	}

}
