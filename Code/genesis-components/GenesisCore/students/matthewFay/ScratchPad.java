package matthewFay;

import generator.Generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import connections.Connections;
import connections.WiredBox;
import frames.entities.Entity;
import translator.BasicTranslator;
import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import matchers.*;
import matchers.original.BasicMatcherOriginal;
import matchers.representations.EntityMatchResult;
import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.representations.BasicCharacterModel;

public class ScratchPad {
	public static void main(String[] args) throws Exception {
//		GenesisTests();
		Java8Tests();
		ArrayList<Object> os = new ArrayList<>();
	}
	
	public static void GenesisTests() throws Exception {
		LList<String> strings1 = new LList<String>("Hello");
		LList<String> strings2 = strings1.cons("World");
		LList<String> strings3 = strings1.append(strings2);
		
		Mark.say(strings1);
		Mark.say(strings2);
		Mark.say(strings3);
		
		String people = "Grandma is a person.";
		String people2 = "Sally is a person.";
		String happy = "Grandma becomes happy.";
		String unhappy = "Sally becomes unhappy.";
		
		Generator gens = Generator.getGenerator();
		gens.setStoryMode();
		BasicTranslator trans = BasicTranslator.getTranslator();
		
		trans.translate(people);
		trans.translate(people2);
		Entity happy_entity = trans.translate(happy).getElement(0);
		Entity unhappy_entity = trans.translate(unhappy).getElement(0);
		Mark.say(happy_entity);
		Mark.say(unhappy_entity);
		
		Entity grandma = happy_entity.getSubject().getSubject();
		Mark.say(CharacterProcessor.getCharacterModel(grandma, true));
		Mark.say("Contains Grandma? "+CharacterProcessor.getCharacterLibrary().keySet().contains(grandma));
		
//		EntityMatcher em = new EntityMatcher();
//		EntityMatchResult emr = em.match(happy_entity, unhappy_entity);
//		Mark.say("Result:"+emr);
//		Mark.say("Result (original):"+BasicMatcherOriginal.getBasicMatcher().match(happy_entity, unhappy_entity));
//		ScoreMatcher sm = new ScoreMatcher();
//		Mark.say("ScoreMatch:"+sm.scoreMatch(happy_entity, unhappy_entity, new LList<PairOfEntities>()));
		
		
	}
	
	public static class Incrementer implements WiredBox {

		private String name = "";
		@Override
		public String getName() {
			return name;
		}
		
		private int value = 0;
		
		public Incrementer(String name, int init) {
			this.name = name;
			value = init;
			
			Connections.getPorts(this).addSignalProcessor(this::increment);
		}
		
		public void increment(Object object) {
			if (object instanceof Integer) {
				value += (Integer)object;
			} else {
				value++;
			}
			
			Mark.say(getName()+" has "+value);
		}
	}
	
	public static class Sender implements WiredBox {

		@Override
		public String getName() {
			return "sender";
		}
		
		public void send(int i) {
			Connections.getPorts(this).transmit(new Integer(i));
		}
	}
	
	public static void Java8Tests() {
//		List<String> names = Arrays.asList("microsoft","apple","linux","oracle");
//		Collections.sort(names, ScratchPad::compareByLength);
//		names.forEach((name)->Mark.say(name));
		
		Incrementer i1 = new Incrementer("First: ",1);
		Incrementer i2 = new Incrementer("Second: ",20);
		
		Sender s = new Sender();
		
		Connections.wire(s, i1);
		Connections.wire(s, i2);
		
		s.send(1);
		s.send(5);
	}
	
	public static int compareByLength(String in, String out) {
		return in.length() - out.length();
	}
}
