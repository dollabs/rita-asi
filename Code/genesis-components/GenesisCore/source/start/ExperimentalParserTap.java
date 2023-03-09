package start;

import connections.*;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.ISpeak;
import translator.Translator;
import utils.Mark;

/*
 * Created on Mar 15, 2015
 * @author phw
 */

public class ExperimentalParserTap extends AbstractWiredBox {

	public static final String DATA = "Data in";

	public ExperimentalParserTap() {
		super("Experimental parser tap");
		Connections.getPorts(this).addSignalProcessor(DATA, this::process);
	}

	public void process(Object o) {
		Mark.say("Hello world");
		boolean debug = true;
		try {
			// Mark.say(o.getClass().getName());
			if (o instanceof String) {

				String sentence = (String) o;
				// Sequence parse = bs.get(1, Sequence.class);

				Entity innerese = Translator.getTranslator().translateToEntity(sentence);
				// Mark.say(debug, "Sentence:", sentence);
				// Mark.say(debug, "Parse:   ", parse);
				// Mark.say(debug, "Innerese:", innerese);
				// Mark.say(debug, "Tokenized input", tokenize(sentence));


				BetterSignal data = new BetterSignal();
				data.add(sentence);
				data.add(innerese);

				Connections.getPorts(this).transmit(DATA, data);
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Sequence tokenize(String sentence) {
		String[] words = sentence.split(" ");
		Mark.say("Word count", words.length);
		Sequence result = new Sequence();
		for (String word : words) {
			result.addElement(ISpeak.makeEntity(word));
		}
		return result;
	}

}
