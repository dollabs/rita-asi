package frames.entities;

/*
 * Created on Jan 13, 2008
 * @author phw
 */

public class EFactory {

	public static Sequence createEventLadder () {
		  Sequence result = new Sequence();
		  result.addType("eventLadder");
		  return result;
		 }
	
	public static Sequence extendEventLadder(Sequence eventLadder, Function event) {
		  if (!eventLadder.isA("eventLadder")) {System.err.println("Sorry " + eventLadder + " is not a trajectoryLadder."); return null;}
		  eventLadder.addElement(event);
		  return eventLadder;
	}
	
	public static Sequence createEventSpace () {
		  Sequence result = new Sequence();
		  result.addType("eventSpace");
		  return result;
		 }
	
	public static Sequence extendEventSpace(Sequence eventSpace, Function eventLadder) {
		  if (!eventSpace.isA("eventSpace")) {System.err.println("Sorry " + eventSpace + " is not a trajectoryLadder."); return null;}
		  eventSpace.addElement(eventLadder);
		  return eventSpace;
	}
}
