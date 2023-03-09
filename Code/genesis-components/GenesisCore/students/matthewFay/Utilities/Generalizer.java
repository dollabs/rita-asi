package matthewFay.Utilities;

import generator.Generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import frames.entities.Entity;
import translator.BasicTranslator;
import utils.Mark;
import utils.PairOfEntities;

public class Generalizer {
	private final static ArrayList<Entity> transforms = new ArrayList<Entity>() {
		{
			add(new Entity("AAA"));
			add(new Entity("BBB"));
			add(new Entity("CCC"));
			add(new Entity("DDD"));
			add(new Entity("EEE"));
			add(new Entity("FFF"));
			add(new Entity("GGG"));
			add(new Entity("HHH"));
			add(new Entity("III"));
			add(new Entity("JJJ"));
			add(new Entity("KKK"));
			add(new Entity("LLL"));
			add(new Entity("MMM"));
			add(new Entity("NNN"));
			add(new Entity("OOO"));
			add(new Entity("PPP"));
			add(new Entity("QQQ"));
			add(new Entity("RRR"));
			add(new Entity("SSS"));
			add(new Entity("TTT"));
			add(new Entity("UUU"));
			add(new Entity("VVV"));
			add(new Entity("WWW"));
			add(new Entity("XXX"));
			add(new Entity("YYY"));
			add(new Entity("ZZZ"));
		}
	};
	
	private final static char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();
	private Entity getTransform(int i) {
		int mod = i%26;
		int multi = i/26+1;
		
		char c = alphabet[mod];
		String s = "";
		for(int count=0;count<multi;count++) {
			s+=c;
		}
		return new Entity(s);
	}
	
	private static Entity focus = new Entity("FOCUS");
	public static Entity getFocus() {
		return focus;
	}
	
	public static Entity generalize(Entity element) {
		return generalize(element, null);
	}
	
	public static Entity generalize(Entity element, Entity focus) {
		return generalize(element, focus, EntityHelper.getAllEntities(element));
	}
	
	public static Entity generalize(Entity element, Entity focus, Collection<Entity> targets) {
		return generalize(element, focus, targets, true);
	}
	
	public static Entity generalize(Entity element, Entity focus, Collection<Entity> targets, boolean copy) {
		if(copy) {
			return generalize(element.deepClone(false), focus, targets, false);
		}
		List<Entity> entities = EntityHelper.getAllEntities(element);
		if(entities.size() >= transforms.size()) {
			Mark.err("Too many entities for Generalization!");
			Mark.err("from: "+element);
			Mark.err("entities: "+entities);
		} else {
			List<PairOfEntities> bindings = new ArrayList<>();
			int j = 0;
			for(int i=0;i<entities.size();i++) {
				Entity entity = entities.get(i);
				if(entity.isEqual(focus)) {
					bindings.add(new PairOfEntities(focus, Generalizer.focus));
				} else {
					for(Entity e : targets) {
						if(e.isEqual(entity)) {
							bindings.add(new PairOfEntities(entities.get(i),transforms.get(j)));
							j++;
							break;
						}
					}
				}
			}
			element = EntityHelper.findAndReplace(element, bindings, true);
		}
		return element;
	}
	
	public static void main(String[] args) throws Exception {
		BasicTranslator basicTranslator = BasicTranslator.getTranslator();
		Entity sentence = basicTranslator.translate("Macbeth kills Duncan with a knife.").getElement(0);
		
		Mark.say(sentence);
		
		Entity duncan = sentence.getObject().getElement(0).getSubject();
		Mark.say(duncan);
		
		Entity gen = generalize(sentence, duncan); 
		Mark.say(gen);
		
		Entity sentence2 = basicTranslator.translate("Claudius kills King Hamlet with a poison.").getElement(0);
		Mark.say(sentence2);
		Entity king_hamlet = sentence2.getObject().getElement(0).getSubject();
		
		Entity gen2 = generalize(sentence2,king_hamlet);
		Mark.say(gen2);
		
		Mark.say("Equal?: "+gen.asString().equals(gen2.asString()));
	}
}
