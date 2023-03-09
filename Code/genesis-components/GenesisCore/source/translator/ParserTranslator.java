package translator;

import java.util.*;

import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;
import frames.entities.Entity;
import generator.RoleFrames;
import start.Start;
import utils.Mark;

/**
 * As of 12 Feb 2016, doesn't work. Not clear why; good subject for summer scrubbing perhaps. Probably the problem has
 * something to do with markers that should be coming in, but don't seem to be.
 * <p>
 * Created on Feb 12, 2016
 *
 * @author phw
 */

public class ParserTranslator extends AbstractWiredBox {

	public static final String MODE = "parsing mode";

	public static final String SENTENCE_IN = "parser/translator sentence input";

	public static final String ENTITY_OUT = "parser/translator entity output";

	private static ParserTranslator parserTranslator;

	public static ParserTranslator getParserTranslator(String name) {
		if (parserTranslator == null) {
			parserTranslator = new ParserTranslator(name);
		}
		return parserTranslator;
	}

	public ParserTranslator(String name) {
		super(name);
		// Mark.say("Constructing ParserTranslator");
		Connections.getPorts(this).addSignalProcessor(SENTENCE_IN, this::processSentence);
		Connections.getPorts(this).addSignalProcessor(MODE, this::processMode);
	}

	public void processMode(Object object) {
		Start.getStart().setMode(object);
	}

	public void processSentence(Object object) {
		boolean debug = false;
		if (object instanceof String) {
			String sentence = (String) object;
			Mark.say(debug, "Coming into ParserTranslator:", sentence);

			try {

				Entity e = Translator.getTranslator().translate(sentence);

				Mark.say(debug, "Transmitting from ParserTranslator", e);
				Connections.getPorts(this).transmit(ENTITY_OUT, e);

				// Connections.getPorts(this).transmit(e);

			}
			catch (Exception e) {
				Mark.err("Blew out translating", sentence);
				e.printStackTrace();
			}

		}

		else if (object instanceof BetterSignal) {

			if (debug) {
				Mark.say("ParserTranslator got a BetterSignal!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				return;
			}
		}

	}

}
