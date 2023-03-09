/**
 * 
 */
package zhutianYang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import constants.Markers;
import constants.Radio;
import dictionary.WordNet;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import frames.entities.Entity.LabelValuePair;
import generator.Generator;
import matchers.StandardMatcher;
import storyProcessor.StoryProcessor;
import translator.Translator;
import utils.Mark;
import utils.PairOfEntities;
import utils.Z;
import utils.minilisp.LList;

/**
 * @author z
 *
 */
public class StoryAligner {
	
	public static Boolean hideStatisticsBar = false;
	public static Boolean storyNewLine = false;
	
	static Translator t = Translator.getTranslator();
	static Generator g = Generator.getGenerator();

	public static Boolean showOptionsInControls = true;
	static int count1 = 0;
	static int count2 = 0;
	static int countOfWant = 0;
	
	public static String learnFromStoryEnsembles(StoryProcessor left, StoryProcessor right) {
		
		Boolean debug = true;
		Mark.night("\n\n Story Aligner\n\n");
		variables = Arrays.asList("XX", "YY", "ZZ", "MM", "NN", "PP", "QQ", "KK", "AA", "BB", "CC");
		
		Boolean generateRecipe = Radio.learnProcedure.isSelected();
		Boolean learnDifference = Radio.learnDifference.isSelected();
		Boolean matchAllElements = true;//Radio.learnConcept.isSelected() || learnDifference;
		
		// markers to print on commentary
		String title = "";
		if(matchAllElements) title = "Pattern in ";
		else if(generateRecipe) title = "Specific recipe in ";
		else if(learnDifference) title = "Similar elements in ";
		
		String story1 = Z.string2Capitalized(left.getStory().getType().replace("_", " "));
		String story2 = Z.string2Capitalized(right.getStory().getType().replace("_", " "));
		Map<Integer, Entity> events1 = new HashMap<>();
		Map<Integer, Entity> events2 = new HashMap<>();
		Map<Integer, Entity> wants1 = new HashMap<>();
		Map<Integer, Entity> wants2 = new HashMap<>();
		List<String> elements1 = new ArrayList<>();
		List<String> elements2 = new ArrayList<>();
		
		// -------------------------------------------------------------
		// read first story
		// -------------------------------------------------------------
		Mark.say(debug, "\n\n\nFirst perspective");
		left.getStory().stream().forEachOrdered(e -> {
			Mark.green(debug, e);
			e = tidyAllEntitiesForMatching(e);
			if (e.getType().equals(Markers.ABDUCTION_RULE)) {
				
				count1++;
				Entity want = e.getSubject().getElement(0).getObject().getElement(0).getSubject();
				Entity event = e.getObject();
				Mark.say(debug, "Wanting element in first perspective:", want);
				elements1.add(e.getSubject().getElement(0).toString());
//				Mark.blue(debug, want);
				wants1.put(count1, want);
				events1.put(count1, event);
				
			} else if (matchAllElements && !elements1.contains(e.toString())
					&& !e.getType().equals(Markers.SCENE)) {
				
				if(e.getType().equals(Markers.MEANS)) {
					count1++;
					wants1.put(count1, e.getObject());
					events1.put(count1, e.getObject());
					count1++;
					wants1.put(count1, Z.means2Want(e));
					events1.put(count1, Z.means2Want(e));
				} else {
					count1++;
					wants1.put(count1, e);
					events1.put(count1, e);
				}
				
			} else {
				Mark.yellow(debug, e);
			}
			
		});
		
		// -------------------------------------------------------------
		// read second story
		// -------------------------------------------------------------
		Mark.say(debug, "\n\n\nSecond perspective");
		right.getStory().stream().forEachOrdered(e -> {
			Mark.green(debug, e);
			e = tidyAllEntitiesForMatching(e);
			if (e.getType().equals(Markers.ABDUCTION_RULE)) {
				
				count2++;
				Entity want = e.getSubject().getElement(0).getObject().getElement(0).getSubject();
				Entity event = e.getObject();
				Mark.say(debug, "Wanting element in second perspective:", want);
				elements2.add(e.getSubject().getElement(0).toString());
				Mark.blue(debug, want);
				wants2.put(count2, want);
				events2.put(count2, event);
				
			} else if (matchAllElements && !elements2.contains(e.toString())
					&& !e.getType().equals(Markers.SCENE)) {
				
				if(e.getType().equals(Markers.MEANS)) {
					count2++;
					wants2.put(count2, e.getObject());
					events2.put(count2, e.getObject());
					count2++;
					wants2.put(count2, Z.means2Want(e));
					events2.put(count2, Z.means2Want(e));
				} else {
					count2++;
					wants2.put(count2, e);
					events2.put(count2, e);
					if(learnDifference && 
							(e.getType().equals(Markers.WANT_MARKER) 
						  || e.getType().equals(Z.NEED))) {
						count2++;
						countOfWant = count2;
						wants2.put(count2, e.getObject().getElement(0).getSubject());
						events2.put(count2, e);
						Mark.mit(e.getObject().getElement(0).getSubject());
					} 
				}
				
			} else {
				Mark.yellow(debug, e);
			}
			
		});
		
		// At this point, call Z's code, either by direct call or by transmitting over a wire defined in
		// GenesisPlugBoardUpper (see Note to Z in that file).
		
		Map<Integer, String> common1 = new HashMap<>();
		Map<Integer, Entity> common1ent = new HashMap<>();
		List<Entity> common = new ArrayList<>();
		List<Entity> recipe1 = new ArrayList<>();
		List<Entity> recipe2 = new ArrayList<>();
		
		Mark.say(debug, "\n\n\n Relevant Events in first perspective");
		for(int key:wants1.keySet()) {
			Entity want = wants1.get(key);
			common1ent.put(key, want);
//			want.setSubject(new Entity("i"));
			
			common1.put(key, want.toString().replaceAll("[0-9]",""));
		}
		
		Mark.say(debug, "\n\n\n Relevant Events in second perspective");
		t.internalize("I am a person");
		int startCommon = -1;
		int startDiff = 0;
		int endDiff = 0;

		for(int key1:wants1.keySet()) {
			Mark.mit(wants1.get(key1));
		}
		for(int key:wants2.keySet()) {
			Entity want = wants2.get(key);
//			want.setSubject(new Entity("i"));
			for(int key1:wants1.keySet()) {
				LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(common1ent.get(key1), want);
				if(compareInner(want, common1ent.get(key1))) {
					Mark.say("\n\n");
					Mark.blue(want);
					Z.understand(want);
					
					Mark.green(common1ent.get(key1));
					Z.understand(common1ent.get(key1));
					
					Mark.red(bindings);
				}
				if(bindings!=null 
						&& !events1.get(key1).getName().contains(Markers.APPEAR_MARKER)) {
//				if(common1ent.get(key1).isDeepEqual(want)) {
//				if(common1.get(key1).equals(want.toString().replaceAll("[0-9]",""))) {
					
					if(allSimilarNounsInBindings(bindings)
							&& !recipe1.contains(events1.get(key1))
							&& !recipe2.contains(events2.get(key))) {
						common.add(wants1.get(key1));
						recipe1.add(events1.get(key1));
						recipe2.add(events2.get(key));
						
						if(startCommon==-1) startCommon = key1;
						if(key == countOfWant) {
							endDiff = key1; 
							Mark.mit(events1.get(key1));
						} else if(key1 > startDiff) startDiff = key1;
					}
				}
				
			}
		}
		
		Generator g = Generator.getGenerator();
		List<String> steps1 = new ArrayList<>();
		List<String> steps2 = new ArrayList<>();
		List<String> stepsGeneral = new ArrayList<>();
		List<String> stepsDifference = new ArrayList<>();
		String toPrint = "";
		

		// -------------------------------------------------------------
		// No 1 --- output general 
		// -------------------------------------------------------------
		
		Mark.say(debug, "\n\n\n Common Elements in two stories");
		toPrint += "\nCommon Elements in two stories:";
		common = variablizeAll(common);
		for(Entity one: common) {
			String step = g.generate(one);
			if(generateRecipe) {
				Z.understand(one);
				step = Z.event2SimpleAction(g.generate(one), one);
			} 
			Mark.night(step);
//			Z.understand(step);
			stepsGeneral.add(step);
			toPrint += "\n Step: " + Z.string2Capitalized(step);
		}
		toPrint += "\nThe end.";
		if(debug) Z.printList(stepsGeneral);
		
		
		// -------------------------------------------------------------
		// No 2,3 --- output specifics 
		// -------------------------------------------------------------
		
		Mark.say(debug, "\n\n\n Surface in first perspective");
		toPrint += "\n\nOriginal elements in " + story1 +":";
		for(Entity one: recipe1) {
//			Mark.show(g.generate(one));
//			Mark.show(one);
//			Z.understand(one);
//			Z.understand(one.getSubject());
//			Z.understand(one.getObject());
//			Mark.say("\n");
			String step = g.generate(one).replace(" A ", " a ");
			if(generateRecipe) {
				step = Z.event2SimpleAction(g.generate(one), one);
			} 
			steps1.add(step);
			toPrint += "\n Step: " + Z.string2Capitalized(step);
		}
		toPrint += "\nThe end.";
		if(debug) Z.printList(steps1);
		
		Mark.say(debug, "\n\n\n Surface in second perspective");
		toPrint += "\n\nOriginal elements in " + story2 +":";
		for(Entity one: recipe2) {
//			Mark.show(one);
//			Z.understand(one);
//			Z.understand(one.getSubject());
//			Z.understand(one.getObject());
//			Mark.say("\n");
			String step = g.generate(one).replace(" A ", " a ");
			if(generateRecipe) {
				step = Z.event2SimpleAction(g.generate(one), one);
			} 
			steps2.add(step);
			toPrint += "\n Step: " + Z.string2Capitalized(step);
		}
		toPrint += "\nThe end.";
		if(debug) Z.printList(steps2);

		
		// -------------------------------------------------------------
		// No 4 --- output different event after the last common event
		// -------------------------------------------------------------
		
		toPrint += "\n\nElements in "+story1+" but not in "+story2+":";
		if(startDiff < endDiff && startDiff > 0) {
			Mark.say(debug, "\n\n\n Difference from the first perspective");
			Mark.say(debug, "\n\n");
			Mark.night(debug, "Start at "+startDiff +": "+wants1.get(startDiff));
			Mark.night(debug, "End at "+endDiff+": "+wants1.get(endDiff));
			Mark.say(debug, "\n\n");
			
			for(int key:wants1.keySet()) {
				if(key>startDiff && key<=endDiff 
						&& !wants1.get(key).getType().equals(Markers.PREDICTION_RULE)){
					String step = g.generate(wants1.get(key));
					if (!Z.string2Capitalized(step).contains("Story "))
						toPrint += "\n Step: " + Z.string2Capitalized(step);
						stepsGeneral.add(step);
				}
			}
		} else {
			Mark.say(debug, "\n\n\n Difference from the first perspective");
			Mark.say(debug, "\n\n");
			Mark.night(debug, "Start until "+startCommon +": "+wants1.get(startCommon));
			Mark.night(debug, "End from "+startDiff+": "+wants1.get(startDiff));
			Mark.say(debug, "\n\n");
			
			
			for(int key:wants1.keySet()) {
				Mark.mit(wants1.get(key));
				if((key<startCommon || key>startDiff)
						&& !wants1.get(key).getType().equals(Markers.PREDICTION_RULE)){
					String step = g.generate(wants1.get(key));
					if (!Z.string2Capitalized(step).contains("Story "))
						toPrint += "\n Step: " + Z.string2Capitalized(step);
						stepsGeneral.add(step);
				}
			}
		}
		
		toPrint += "\nThe end.";
		if(debug) Z.printList(stepsGeneral);
		
//		RecipeLearner.writeKnowledgeFile("corpora/Ensembles/FirstPerspective.txt", "repair a phone", "", steps)
		
//		Mark.mit(toPrint);
		toPrint = toPrint.replace("Step: ", "&nbsp;- ").replace("\nThe end.", "");
		return toPrint;
	}

	static Map<String, Entity> variabledNames = new HashMap<>();
	static List<String> variables = Arrays.asList("XX", "YY", "ZZ", 
			"MM", "NN", "PP", "QQ", "KK", "AA", "BB", "CC", "DD", "EE", "FF", "GG",
			"HH", "JJ", "LL", "OO", "RR", "SS", "TT", "UU", "VV", "WW");
	static int variablesIndex = 0;
	
	public static List<Entity> variablizeAll(List<Entity> common) {
		List<Entity> newCommon = new ArrayList<>();
		for(Entity one: common) {
			Entity oneNew = variablize(one);
			Mark.say(one);
			Mark.show(oneNew);
			newCommon.add(oneNew);
		}
		return newCommon;
	}
	
	public static Entity variablize(Entity ent) {
		Vector<LabelValuePair> properties = ent.getPropertyList();
		List<Object> features = ent.getFeatures();
		if(Z.isEnt(ent)) {
			if(Z.getWordnetAllThreadsString(ent).contains(Z.PERSON)) {
//				Mark.mit(ent, ent.getType());
//				Mark.show(variabledNames);
				String name = ent.getType();
				if(variabledNames.containsKey(name)) {
					ent = variabledNames.get(name);
				} else {
					String variable = variables.get(variablesIndex);
					variablesIndex ++;
					Entity newEntity = new Entity(variable);
					variabledNames.put(ent.getType(), newEntity);
					ent = newEntity;
				}
				
			}
		} else {
			if(Z.isRel(ent)) {
				ent = new Relation(ent.getType(), variablize(ent.getSubject()), variablize(ent.getObject()));
			} else if (Z.isSeq(ent)) {
				Sequence seq = new Sequence(ent.getType());
				for(Entity seq1: ent.getElements()) {
					seq.addElement(variablize(seq1));
				}
				ent = seq;
			} else if (Z.isFun(ent)) {
				ent = new Function(ent.getType(), variablize(ent.getSubject()));
			}
			// add the properties and features back on
			for(LabelValuePair property: properties) {
				ent.addProperty(property.getLabel(), property.getValue());
			}
			for(Object feature: features) {
				ent.addFeature(feature);
			}
		}
		return ent;
	}
	
	public static void testVariablizeAll() {
		List<Entity> entities = new ArrayList<>();
		t.internalize("James is a person.");
		t.internalize("Anselmo is a person.");
		t.internalize("Jayme is a person.");
		entities.add(t.translate("Anselmo was heard").getElement(0));
		entities.add(t.translate("Jayme pulled water from James").getElement(0));
		entities.add(t.translate("Anselmo ingested air into James").getElement(0));
		entities.add(t.translate("James was sent to hospital").getElement(0));
		variablizeAll(entities);
	}
	
	
	
	
	
	
	

	
	public static Boolean compareInner(Entity ent1, Entity ent2) {
		String str1 = ent1.toString();
		String str2 = ent2.toString();
		if(!str1.startsWith("(rel ") || !str2.startsWith("(rel ")) {
			return false;
		}
		str1 = str1.substring(str1.indexOf("(rel "), str1.indexOf(" ("));
		str2 = str2.substring(str2.indexOf("(rel "), str2.indexOf(" ("));
		
		if(str1.contains("explanation")) {
			return false;
		}
		return str1.equals(str2);
	}
	
	
	
	
	
	
	
	
	

	
	public static void main(String[] args) {
//		testRemoveAllOwners();
//		testSimilarNouns();
		testMatcher2();
//		testVariablizeAll();
	}
	
	
	
	public static Boolean allSimilarNounsInBindings(LList<PairOfEntities> bindings) {
//		for(List<Entity> one: Z.getBinds(bindings)) {
//			if(!similarNouns(one.get(0), one.get(1))) {
//				Mark.mit("not similar: ", one.get(0), one.get(1));
//				return false;
//			};
//		}
		return true;
	}
	
	public static WordNet wordNet = new WordNet();
	public static Boolean similarNouns(String obj1, String obj2) {
		return similarNouns(t.translateToEntity(obj1), t.translateToEntity(obj2));
	}
	public static Boolean similarNouns(Entity obj1, Entity obj2) {
		Boolean debug = false;
		int threshold = 5;
		
		List<List<String>> oneses = Z.getWordnetAllThreads(obj1);
		List<List<String>> twoses = Z.getWordnetAllThreads(obj2);
		String nearestObj = Markers.THING_WORD;
		int distance = 100;
		for(List<String> ones: oneses) {
			if(ones.get(0).equals(Markers.THING_WORD)) {
				for(List<String> twos: twoses) {
					if(twos.get(0).equals(Markers.THING_WORD)) {
						Mark.show(debug, ones);
						Mark.show(debug, twos);
						for(int i=0; i<ones.size(); i++) {
							for(int j=0; j<twos.size(); j++) {
								int length = ones.size() + twos.size();
								String one = ones.get(i);
								String two = twos.get(j);
								Mark.say(debug, one, two);
								if(one.equals(two)) {
									if(length - (i+j) <distance) {
										distance = length - (i+j);
										nearestObj = ones.get(i);
										Mark.say(debug, distance);
										Mark.say(debug, nearestObj);
									}
								} 
							}
						}
						Mark.night(debug, distance);
						Mark.night(debug, nearestObj);
					}
				}
				Mark.show(debug, "\n\n");
			}
		}
		Mark.night(distance);
		Mark.night(nearestObj);
		return distance<threshold;
	}
	
	public static void testSimilarNouns() {
		List<String[]> strings = new ArrayList<>();
		strings.add(new String[]{"apple", "banana"});
		strings.add(new String[]{"water", "shore"});
		t.internalize("Alice is a person");
		t.internalize("Bob is a person");
		strings.add(new String[]{"alice", "bob"});
		strings.add(new String[]{"sister", "aunt"});
		for(String[] string: strings) {
			Mark.say(true, similarNouns(string[0], string[1]));
		}
		
	}
	
	public static Entity tidyEntityForMathing(Entity ent) {
		return removeGroup(removeOwner(ent));
	}
	
	public static Entity removeGroup(Entity ent) {
		if(ent.hasProperty(Markers.GROUP)) {
			ent.removeProperty(Markers.GROUP);
			Mark.show("removed group! " + ent.getPropertyList());
		}
		return ent;
	}
	
	public static Entity removeClauses(Entity ent) {
		if(ent.hasProperty(Markers.CLAUSES)) {
			ent.removeProperty(Markers.CLAUSES);
			Mark.show("removed clauses! " + ent.getPropertyList());
		}
		return ent;
	}
	
	public static Entity removeOwner(Entity ent) {
		if(ent.hasProperty(Markers.OWNER_MARKER)) {
			ent.removeProperty(Markers.OWNER_MARKER);
			ent.addProperty(Markers.DETERMINER, Markers.DEFINITE);
			Mark.show("removed owner! " + ent.getPropertyList());
		}
		return ent;
	}
	
	public static Entity tidyAllEntitiesForMatching(String string) {
		return tidyAllEntitiesForMatching(t.translate(string).getElement(0));
	}

	static int countt = 0;
	public static Entity tidyAllEntitiesForMatching(Entity ent) {
		
		Boolean debug = false;
		Mark.night(debug, "Count: " + (++countt));
		if(debug) Z.understand(ent);
		Vector<LabelValuePair> properties = ent.getPropertyList();
		List<Object> features = ent.getFeatures();
		if(Z.isEnt(ent)) {
			ent = tidyEntityForMathing(ent);
		} else {
			if(Z.isRel(ent)) {
				ent = new Relation(ent.getType(), tidyAllEntitiesForMatching(ent.getSubject()), tidyAllEntitiesForMatching(ent.getObject()));
			} else if (Z.isSeq(ent)) {
				Sequence seq = new Sequence(ent.getType());
				for(Entity seq1: ent.getElements()) {
					seq.addElement(tidyAllEntitiesForMatching(seq1));
				}
				ent = seq;
			} else if (Z.isFun(ent)) {
				if(debug) Z.understand(ent);
				ent = new Function(ent.getType(), tidyAllEntitiesForMatching(ent.getSubject()));
			}
			// add the properties and features back on
			for(LabelValuePair property: properties) {
				ent.addProperty(property.getLabel(), property.getValue());
			}
			for(Object feature: features) {
				ent.addFeature(feature);
			}
		}
		return ent;
	}
	
	public static void testRemoveAllOwners() {
		List<String> strings = new ArrayList<>();
//		strings.add("my mother gives Tom's gift to my father.");
		strings.add(" She recharged her phone for 4 hours.");
		for(String string: strings) {
			Mark.say(g.generate(tidyAllEntitiesForMatching(string)));
		}
	}

	
//	public static void helpTranslator() {
//		t = Translator.getTranslator();
//		
//		t.internalize("xx, yy, and zz are entities.");
//		t.internalize("vv is a variable.");
//		t.internalize("uu is a variable.");
//		t.internalize("aa is an action.");
//		
//		// Hamlet & Simba
//		t.internalize("Denmark is a country.");
//		t.internalize("Pride Land is a country.");
//		t.internalize("Hamlet is a person.");
//		t.internalize("Simba is a person.");
//		
//		// Replace battery
////		t.internalize("Alice is a person.");
////		t.internalize("Bob is a person.");
////		t.internalize("The battery, cover, and phone are entities.");
////		t.internalize("The replacement battery is an entity.");
//		
//		// Save drawning people 
////		t.internalize("The emergency responders are persons.");
////		t.internalize("Anselmo is a person.");
////		t.internalize("Correia is a person.");
////		t.internalize("Jayme is a person.");
////		t.internalize("Sarah is a person.");
////		t.internalize("Personnel are persons.");
////		
////		t.internalize("Hazel is a person.");
////		t.internalize("EW is a person.");
////		t.internalize("Sisk is a person.");
////		t.internalize("Lisa is a person.");
////		t.internalize("Jason is a person.");
////		t.internalize("Paramedics are persons.");
////		t.internalize("The bathrooms, and the docks are entities.");
//		
//	}
	

	
	public static void testMatcher2() {
		Translator t2 = Translator.getTranslator();
		Entity ent1 = new Entity();
		Entity ent2 = new Entity();
		
//		t.internalize("King Hamlet is a person.");
//		t.internalize("Claudius is a person.");
//		t.internalize("Scar is a person.");
//		t.internalize("Mufasa is a person.");
//		ent1 = t2.translate("King Hamlet was killed by Claudius.");
//		ent2 = t2.translate("Scar killed Mufasa");
		
//		t.internalize("Alice is a person.");
//		t.internalize("Bob is a person.");
//		t.internalize("The battery, cover, and phone are entities.");
//		t.internalize("The replacement battery is an entity.");
//		Entity ent1 = t2.translate("Alice exposed phone's part.");
//		Entity ent2 = t2.translate("Bob exposed phone's part.");
		
//		t.internalize("Sisk is a person.");
//		t.internalize("Hazel is a person.");
//		t.internalize("xx is a person.");
//		t.internalize("yy is a person.");
//		Entity ent1 = t2.translate("Sisk pressed Hazel's chest.");
//		Entity ent2 = t2.translate("xx pressed yy's chest.");
		
//		t.internalize("xx is Phillips.");
//		t.internalize("tt is a tool.");
//		t.internalize("kk is an entity.");
//		ent1 = t2.translate("Phillips wants to create innovative tools that increase programming productivity.").getElement(0);
//		ent2 = t2.translate("xx wants to create tt that increase kk.").getElement(0);//tt to increase kk.")
		
//		t.internalize("xx is a person.");
//		t.internalize("cyber security is a technology.");
//		t.internalize("ss is a technology.");
//		t.internalize("vv is a variable.");
//		t.internalize("software is a tool.");
//		Entity ent1 = t2.translate("Alex strives to improve cyber security").getElement(0);
//		Entity ent2 = t2.translate("xx strives to improve ss").getElement(0);
		
//		t.internalize("xx is a person.");
//		t.internalize("Phillips is a person.");
//		Entity ent1 = t2.translate("Phillips explored a lot of ideas").getElement(0);
//		Entity ent2 = t2.translate("xx explores ideas").getElement(0);
		
//		t.internalize("xx is a person.");
//		t.internalize("Alex is a person.");
//		t.internalize("kk is an entity.");
//		ent1 = t2.translate("Alex came up with many research ideas.").getElement(0);
//		ent2 = t2.translate("xx comes up with kk").getElement(0);
		
//		t.internalize("xx is a person.");
//		t.internalize("Phillips is a person.");
//		t.internalize("ww is an entity.");
//		Entity ent1 = t2.translate("Phillips does not publish ww").getElement(0);
//		Entity ent2 = t2.translate("Phillips did not publish any academic paper.").getElement(0);
		
//		t.internalize("xx is a person.");
//		t.internalize("yy is an entity.");
//		t.internalize("Phillips is a person.");
//		t.internalize("tt is an tool.");
//		Entity ent1 = t2.translate("Phillips developed a tool that helped programmers all over the world").getElement(0);
//		Entity ent2 = t2.translate("xx develops tt that helps yy all over the world").getElement(0);

//		t.internalize("xx is a person.");
//		t.internalize("Yang is a person.");
//		t.internalize("the Genesis Group is an entity.");
//		t.internalize("yy is an entity.");
//		Entity ent1 = t2.translate("Yang worked at the Genesis Group").getElement(0);
//		Entity ent2 = t2.translate("xx works at yy").getElement(0);
		
//		t.internalize("xx is a person.");
//		t.internalize("I am a person.");
//		t.internalize("uu is an university.");
//		t.internalize("Nanyang Technological University is an university");
//		t.internalize("programs are research works.");
//		t.internalize("kk is variable.");
//		t.internalize("vv is variable.");
//		t.internalize("tt is variable.");
//		ent1 = t2.translate("xx interns at kk in vv before tt").getElement(0);
//		ent2 = t2.translate("In the summer before his fourth year, he interned at Microsoft.").getElement(0);
		
//		t.internalize("xx is a person.");
//		t.internalize("I am a person.");
//		t.internalize("uu is an university.");
//		t.internalize("Nanyang Technological University is an university");
//		t.internalize("programs are research works.");
//		t.internalize("yy are research works.");
//		ent1 = t2.translate("I continued to refine the programs at Nanyang Technological University.").getElement(0);
//		ent2 = t2.translate("xx continues to refine yy at uu").getElement(0);
		
//		t.internalize("phillips is a person.");
//		t.internalize("Yang is a person.");
//		t.internalize("uu is an university.");
//		t.internalize("Nanyang Technological University is an university");
//		t.internalize("programs are research works.");
//		t.internalize("yy are research works.");
//		ent1 = t2.translate("Yang developed a program.").getElement(0);
//		ent2 = t2.translate("phillips developed a tool").getElement(0);
		
		
		// untested
//		t.internalize("xx is a person.");
//		t.internalize("a man is a person.");
//		ent1 = t2.translate("A man spent five minutes in front of the kiosk.").getElement(0);
//		ent2 = t2.translate("xx spends five minutes in front of the kiosk").getElement(0);
//		
//		t.internalize("xx is a person.");
//		t.internalize("a man is a person.");
//		t.internalize("subway is an entity.");
//		ent1 = t2.translate("a man bought the ticket finally and ran to the subway.").getElement(1);
//		ent2 = t2.translate("xx runs to the subway").getElement(0);
//		
//		t.internalize("xx is a person.");
//		t.internalize("a girl is a person.");
//		t.internalize("passenger is an entity");
//		ent1 = t2.translate("A girl talked to a nearby passenger.").getElement(0);
//		ent2 = t2.translate("xx talks to a passenger").getElement(0);
//		
//		t.internalize("xx is a person.");
//		t.internalize("a girl is a person.");
//		t.internalize("the ticket office is an entity");
//		ent1 = t2.translate("Another girl approached the ticket office.").getElement(0);
//		ent2 = t2.translate("xx approaches the ticket office").getElement(0);
		
//		t.internalize("appetites is an entity.");
//		t.internalize("productivity is an entity.");
//		t.internalize("pounds is a measure");
//		t.internalize("feet is a measure");
//		t.internalize("seventy-five is five");
//		ent1 = t2.translate("the appetites total seventy-five pounds.").getElement(0);
//		ent2 = t2.translate("the productivity totals five feet.").getElement(0);
//		
//		t.internalize("gas is an entity.");
//		t.internalize("body is an entity.");
//		ent1 = t2.translate("an ideal gas is seen as energy's system.").getElement(0);
//		ent2 = t2.translate("the black body is seen as energy's system.").getElement(0);
//		
		t.internalize("gas is an entity.");
		t.internalize("radiation is an entity.");
		t.internalize("boltzmann's-principle is an entity.");
		t.internalize("wien's law is an entity.");
		ent1 = t2.translate("we derive the entropy change of ideal gas with boltzmann's-principle").getElement(0);
		ent2 = t2.translate("we derive the entropy change of the radiation with wien's law.").getElement(0);
		
		
		Z.understand(ent1);
		Z.understand(ent2);
		
		Z.printAllNounEntities(ent1);
		Z.printAllNounEntities(ent2);
		
		LList<PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(ent1, ent2);
		Mark.show(bindings);
	}
}
