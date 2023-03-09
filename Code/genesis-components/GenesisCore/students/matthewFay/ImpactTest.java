package matthewFay;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import utils.Mark;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.RPCBox;
import connections.Connections.NetWireException;
import connections.WiredBox;

public class ImpactTest extends AbstractWiredBox {
	public ImpactTest() throws NetWireException {
		super("ImpactCommunicator");
		try {
			WiredBox impact = Connections.subscribe("IMPACT");
			rpcImpact = (RPCBox) impact;
		}
		catch (Exception e) {
			rpcImpact = null;
		}
	}

	public RPCBox rpcImpact;

	Date currentTime = new Date();

	DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	DateFormat df2 = new SimpleDateFormat("HH:mm");

	public void addTime(int offset) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentTime);
		cal.add(Calendar.MINUTE, offset);
		currentTime = cal.getTime();
	}

	public String getTime(int offset) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentTime);
		cal.add(Calendar.MINUTE, offset);
		return df.format(cal.getTime()) + "T" + df2.format(cal.getTime());
	}

	public static void main(String[] args) throws NetWireException, InterruptedException {
		ImpactTest impact = new ImpactTest();

		/**
		 * Emulating Story: Start Story titled "IED 2". Agent-1 meets Agent-2 at Location-1-1. Agent-1 gives IED-1 to
		 * Agent-2. Agent-2 travels to Location-9-1. Agent-2 meets Agent-3 at Location-9-1. Agent-2 gives IED-1 to
		 * Agent-3. Agent-3 travels to Location-5-5. Agent-3 places IED-1 at Location-5-5. Agent-3 detonates IED-1.
		 * Agent-3 makes a phone call. Remember the story. The end.
		 */

		// impact.reset();
		// impact.meet("Agent-1", "Agent-2", "Location-1-1");
		// impact.addTime(10);
		// impact.give("Agent-1", "IED-1", "Agent-2");
		// impact.addTime(10);
		// impact.travel("Agent-2", "Location-1-1", "Location-9-1");
		// impact.addTime(10);
		// impact.meet("Agent-2", "Agent-3", "Location-9-1");
		// impact.addTime(10);
		// impact.give("Agent-2", "IED-1", "Agent-3");
		// impact.addTime(10);
		// impact.travel("Agent-3", "Location-9-1", "Location-5-5");
		// impact.addTime(10);
		// impact.place("Agent-3", "IED-1", "Location-5-5");
		// impact.addTime(10);
		// impact.detonate("Agent-3", "IED-1");
		// impact.addTime(10);
		// impact.makePhoneCall("Agent-3");
		// impact.not_be();

		/**
		 * Emulating Story: Start Story titled "Kidnapping 15". Agent-1 retrieves Weapon-1 from Location-5-1. Agent-1
		 * meets Agent-2 at Location-9-1. Agent-2 gives Ammunition-1 to Agent-1. Agent-1 makes a phone call. Agent-1
		 * travels to Location-5-5. Agent-1 meets Agent-3 at Location-5-5. Agent-1 threatens Agent-3. Agent-3 travels to
		 * Location-9-5. Agent-1 travels to Location-9-5. Remember the story. The end.
		 */

		// impact.reset();
		// impact.retrieve("Agent-1", "Weapon-1", "Location-5-1");
		// impact.addTime(10);
		// impact.meet("Agent-1", "Agent-2", "Location-9-1");
		// impact.addTime(10);
		// impact.give("Agent-2", "Ammunition-1", "Agent-1");
		// impact.addTime(10);
		// impact.makePhoneCall("Agent-1");
		// impact.addTime(10);
		// impact.travel("Agent-1", "Location-9-1", "Location-5-5");
		// impact.addTime(10);
		// impact.meet("Agent-1", "Agent-3", "Location-5-5");
		// impact.addTime(10);
		// impact.threaten("Agent-1", "Agent-3");
		// impact.addTime(10);
		// impact.travel("Agent-3", "Location-5-5", "Location-9-5");
		// impact.addTime(10);
		// impact.travel("Agent-1", "Location-5-5", "Location-9-5");
		// impact.not_be();

		/**
		 * Silly Prompt: Agent-X meets Agent-Y at Location-7-1. Agent-X gives IED-7 to Agent-Y. Agent-Y travels to
		 * Location-7-2. Agent-Y meets Agent-Z at Location-7-2. Agent-Y gives IED-7 to Agent-Z. Agent-Z retrieves
		 * Weapon-6 from Location-7-2. /--Genesis Prediction: Agent-Z travels to Location-7-3. Agent-Z places IED-7 at
		 * Location-7-3. Agent-Z detonates IED-7. Agent-Z makes a phone call.
		 */
		impact.reset();
		impact.meet("Agent-X", "Agent-Y", "Location-7-1");
		impact.addTime(10);
		impact.give("Agent-X", "IED-7", "Agent-Y");
		impact.addTime(10);
		impact.travel("Agent-X", "Location-7-1", "Location-7-2");
		impact.addTime(10);
		impact.meet("Agent-Y", "Agent-Z", "Location-7-2");
		impact.addTime(10);
		impact.give("Agent-Y", "IED-7", "Agent-Z");
		impact.addTime(10);
		impact.retrieve("Agent-Z", "Weapon-6", "Location-7-2");
		impact.travel("Agent-Z", "Loction-7-2", "Location-7-3");
		impact.not_be();

		// impact.give("James", "bomb", "Jesse");
		// impact.addTime(10);
		// impact.place("Jesse", "bomb", "Area-51-2");
		// impact.addTime(10);
		// impact.travel("Jesse", "Area-51-2", "Area-52-2");
		// impact.addTime(10);
		// impact.place("Jesse", "bomb", "Area-52-2");
		// impact.not_be();

		Thread.sleep(10000);

		// impact.reset();
		// impact.travel("John", "Area-50-1", "Area-51-2");
		// impact.addTime(10);
		// impact.place("John", "IED-1", "Area-51-2");
		// impact.addTime(10);
		// impact.travel("John", "Area-50-1", "Area-52-2");
		// impact.addTime(10);
		// impact.place("John", "IED-1", "Area-52-2");
		// impact.not_be();
	}

	private Object sendMessage(String message) {
		String[] message_obj = new String[2];
		message_obj[0] = message;
		message_obj[1] = null;

		Mark.say("Sending Command:");
		Mark.say(message);

		Object o = null;
		if (rpcImpact != null) {
			o = rpcImpact.rpc("rpcMethod", message_obj);
		}
		else {
			Mark.say("Message not sent, not connected...");
		}

		Mark.say(o);

		return o;
	}

	public void reset() {
		String message = "reset5(for: envisioning)!";
		sendMessage(message);
		currentTime = new Date();
	}

	public void build(String subject, String object, String using) {
		String mob = "build(subject:human \"" + subject + "\", object:IED \"" + object + "\",using:explosive_material \"" + using
		        + "\", from:time \"" + getTime(0) + "\", to:time \"" + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void detonate(String subject, String object) {
		String mob = "detonate(subject:human \"" + subject + "\", object:IED \"" + object + "\", from:time \"" + getTime(0) + "\", to:time \""
		        + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void give(String subject, String object, String to) {
		String mob = "give(subject:human \"" + subject + "\", object:tangible_object \"" + object + "\",to:human \"" + to + "\", from:time \""
		        + getTime(0) + "\", to:time \"" + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void makePhoneCall(String subject) {
		String mob = "make(subject:human \"" + subject + "\", object: phone_call(article: a),from:time \"" + getTime(0) + "\", to:time \""
		        + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void meet(String subject, String object, String location) {
		String mob = "meet(subject:human \"" + subject + "\", object:human \"" + object + "\", at:location \"" + location + "\", from:time \""
		        + getTime(0) + "\", to:time \"" + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void place(String subject, String object, String location) {
		String mob = "place(subject:human \"" + subject + "\", object:tangible_object \"" + object + "\",at:location \"" + location
		        + "\", from:time \"" + getTime(0) + "\", to:time \"" + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void raiseAlert(String subject) {
		String mob = "raise(subject:human \"Agent-15\", object: alert(article: a), from:time \"" + getTime(0) + "\", to:time \"" + getTime(10)
		        + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void retrieve(String subject, String object, String location) {
		String mob = "retrieve(subject:human \"" + subject + "\", object:tangible_object \"" + object + "\",from:location \"" + location
		        + "\", from:time \"" + getTime(0) + "\", to:time \"" + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void selfDetonate(String object) {
		String mob = "self-detonate(subject:IED \"" + object + "\", from:time \"" + getTime(0) + "\", to:time \"" + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void threaten(String subject, String object) {
		String mob = "threaten(subject:human \"" + subject + "\", object:human \"" + object + "\", from:time \"" + getTime(0) + "\", to:time \""
		        + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void travel(String subject, String origin, String location) {
		String mob = "travel(subject:human \"" + subject + "\", from:location \"" + origin + "\", to:location \"" + location + "\", from:time \""
		        + getTime(0) + "\", to:time \"" + getTime(10) + "\").";
		mob = "envision5(object:expression [=(" + mob + ")])!";
		sendMessage(mob);
	}

	public void not_be() {
		String mob = "not_be(subject:expression [what], adjective: permitted)?";
		sendMessage(mob);
	}
	/*
	 * //Background: Start Story titled "Bomb Placement". John controls the bomb. John travels to Area-51-2. John places
	 * the bomb at Area-51-2. The end. //Prompt: Start Story titled "Physical Constraint Story". Nick controls the bomb.
	 * Nick travels to Area-51-2. Nick travels to Area-52-2. The end. build(subject:human \"Agent-15\", object:IED
	 * \"IED-11\",using:explosive_material \"Explosive-Material-11\", from:time \"2013-04-01T11:10\", to:time
	 * \"2013-04-01T11:20\"). detonate(subject:human \"Agent-15\", object:IED \"IED-11\", from:time
	 * \"2013-04-01T11:10\", to:time \"2013-04-01T11:20\"). give(subject:human \"Agent-15\", object:tangible_object
	 * \"IED-11\",to:human \"Agent-16\", from:time \"2013-04-01T11:10\", to:time \"2013-04-01T11:20\").
	 * make(subject:human \"Agent-15\", object: phone_call(article: a),from:time \"2013-04-01T11:10\", to:time
	 * \"2013-04-01T11:20\"). meet(subject:human \"Agent-15\", object:human \"Agent-16\", at:location
	 * \"Location-11-15\", from:time \"2013-04-01T11:10\", to:time \"2013-04-01T11:20\"). place(subject:human
	 * \"Agent-15\", object:tangible_object \"IED-11\",at:location \"Location-11-15\", from:time
	 * \"2013-04-01T11:10\",to:time \"2013-04-01T11:20\"). raise(subject:human \"Agent-15\", object: alert(article: a),
	 * from:time \"2013-04-01T11:10\", to:time \"2013-04-01T11:20\"). retrieve(subject:human \"Agent-15\",
	 * object:tangible_object \"IED-11\",from:location \"Location-11-15\", from:time \"2013-04-01T11:10\", to:time
	 * \"2013-04-01T11:20\"). self-detonate(subject:IED \"IED-11\", from:time \"2013-04-01T11:10\", to:time
	 * \"2013-04-01T11:20\"). threaten(subject:human \"Agent-15\", object:human \"Agent-16\", from:time
	 * \"2013-04-01T11:10\", to:time \"2013-04-01T11:20\"). travel(subject:human \"Agent-15\", from:location
	 * \"Location-11-15\", to:location \"Location-11-19\", from:time \"2013-04-01T11:10\", to:time
	 * \"2013-04-01T11:20\").
	 */
}
