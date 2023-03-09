package connections;

/**
 * stub for bridging Java Connections code with wired box implementations in other languages. see LibUtil
 * @author adk
 *
 */
public class Receiver implements WiredBox{

	@Override
	public String getName() {
		return "Signal receiver stub for connecting a wired box that is implemented in a language other than Java";
	}
	protected Collector collector;//the Java stub of the external object containing extM
}