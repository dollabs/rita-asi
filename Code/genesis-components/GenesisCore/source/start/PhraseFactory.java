package start;

import generator.*;

import java.util.*;

import utils.Mark;
import connections.*;
import connections.Connections.NetWireException;
import constants.GenesisConstants;

/*
 * Created on Feb 9, 2011
 * @author phw
 */

public class PhraseFactory extends RoleFrameParent {

	private static PhraseFactory phraseFactory;

	RPCBox clientBox;

	WiredBox clientProxy;

	public static String wireServer = DefaultSettings.WIRE_SERVER;

	UUID uuid = UUID.randomUUID();

	public static PhraseFactory getPhraseFactory() {
		if (phraseFactory == null) {
			phraseFactory = new PhraseFactory();
		}
		return phraseFactory;
	}

	private PhraseFactory() {
		createClient();
	}

	private boolean createClient() {
		try {
			clientProxy = Connections.subscribe(GenesisConstants.server, 5);
			clientBox = (RPCBox) clientProxy;
			return true;
		}
		catch (NetWireException e) {
			// Mark.err("Failed to create Start client");
			return false;
		}
	}

	public String before(RoleFrameParent one, RoleFrameParent two) {
		Mark.say("One's relation", one.getHead());
		Mark.say("Two's relation", two.getHead());
		return makeTriple(one.getHead(), "before", two.getHead()) + one + two;
	}

	public String generate(RoleFrame r) {
		return translate(r.getRendering());
	}

	public String generate(RoleFrameParent p) {
		return generate(p.toString());
	}

	public String generate(String request, RoleFrameGrandParent... entities) {
		String instructions = request.trim();
		if (instructions.startsWith("[")) {
			return translate(instructions);
		}
		instructions = new RoleFrameParent(request, entities).toString();

		return translate(instructions);
	}

	public String translate(String instructions) {
		String request = remap(instructions);
		String result = null;
		try {
			Object[] arguments = { request, uuid.toString() };
			Object value = clientBox.rpc("remoteGenerate", arguments);
			if (value != null) {
				result = ((String) value).trim();
			}
		}
		catch (Exception e) {
			try {
				Mark.say("Bug in effort to process sentence remotely!");
				result = (String) (StartServerBox.getStartServerBox().remoteGenerate(request, uuid.toString()));
				Mark.say("Succeeded locally:", result);
			}
			catch (Exception e1) {
				Mark.say("Bug in effort to process sentence locally too!  Give up.");
				e1.printStackTrace();
			}
		}
		return result;
	}

	/*
	 * Convert indexes to a canonical form, so as to benefit from cache
	 */
	private String remap(String triples) {
		HashMap<String, String> substitutions = new HashMap<String, String>();
		int index = 0;
		StringBuffer buffer = new StringBuffer(triples);
		// Look for +, see if in cache, if so, substitute, if not put in cache
		// and substitute
		int from = 0;
		while (true) {
			int nextPlus = buffer.indexOf("+", from);
			if (nextPlus < 0) {
				break;
			}
			// There is one, so find where number ends
			int nextSpace = buffer.indexOf(" ", nextPlus);
			int nextBracket = buffer.indexOf("]", nextPlus);
			int winner = 0;
			if (nextSpace >= 0 && nextBracket >= 0) {
				winner = Math.min(nextSpace, nextBracket);
			}
			else if (nextSpace >= 0) {
				winner = nextSpace;
			}
			else if (nextBracket >= 0) {
				winner = nextBracket;
			}
			else {
				Mark.err("Ooops, bug in Start.remap");
			}
			String key = buffer.substring(nextPlus, winner);
			String substitution = substitutions.get(key);
			if (substitution == null) {
				substitution = Integer.toString(index++);
				substitutions.put(key, substitution);
			}
			buffer.replace(nextPlus + 1, winner, substitution);
			from = nextPlus + 1;
		}
		return buffer.toString();
	}

	public RPCBox getClientBox() {
		return clientBox;
	}

	public static void main(String[] x) {

		Generator generator = Generator.getGenerator();

		RoleFrame p1 = new RoleFrame("man").addFeature("large");

		RoleFrame p2 = new RoleFrame("woman").addFeature("small").makeIndefinite();

		RoleFrame p3 = new RoleFrame("person").addFeature("second").makeIndefinite();

		RoleFrame t1 = new RoleFrame("truck").makeIndefinite();

		RoleFrame o1 = new RoleFrame("phone").addFeature("cell").addFeature("black").makeIndefinite();

		RoleFrame f1 = new RoleFrame(p2, "hide").makeFuture();

		RoleFrame f2 = new RoleFrame(t1, "appear").makePresent();

		System.out.println(generator.generate(f2));

		System.out.println(generator.generate(new RoleFrame("truck", "appear")));

		System.out.println(generator.generate(new RoleFrame("man", "give", o1).addRole("to", "woman").makePast()));

		System.out.println(generator.generate(new RoleFrame("man", "give", o1).addRole("to", "woman").makePresent()));

		System.out.println(generator.generate(new RoleFrame("man", "give", o1).addRole("to", "woman").makeFuture()));

		System.out.println(generator.generate(new RoleFrame(p1, "give", o1).addRole("to", p2).makePresent()));

		System.out.println(generator.generate(f1.connect("before", f2.makePast())));

		System.out.println(generator.generate(p1.embed("know", f2)));

		System.out.println(generator.generate(p2.embed("think", f2.negative()).negative().makePast()));

		System.out.println(generator.generate(new RoleFrame(p2, "left", p1).makeFuture()
		        .connect("after", new RoleFrame(p2, "bury", o1).makePresent())));

		// System.out.println(p1.believe(f2).negative().getRendering());

		// System.out.println(Start.getStart().processSentence("John kissed Mary"));

		// System.out.println(">>>>>>>>>>>>>>>");

	}

}
