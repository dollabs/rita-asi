package connections;

/*
 * Created on Jan 26, 2013
 * @author phw
 */

class LinkedPortPair {

	String sourcePortName;

	WiredBox sourceBox;

	String destinationPortName;

	WiredBox destinationBox;

	public LinkedPortPair(String sourcePortName, WiredBox sourceBox, String destinationPortName, WiredBox destinationBox) {
		super();
		this.sourcePortName = sourcePortName;
		this.sourceBox = sourceBox;
		this.destinationPortName = destinationPortName;
		this.destinationBox = destinationBox;
	}

}
