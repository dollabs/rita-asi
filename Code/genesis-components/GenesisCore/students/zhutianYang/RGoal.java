/**
 *  my neat representation of relations: "phone + battery"
 */
package zhutianYang;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import constants.Markers;
import dictionary.WordNet;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import generator.Generator;
import matchers.Substitutor;
import translator.Translator;
import utils.Mark;
import utils.Z;

/**
 * @author oxen
 *
 */
public class RGoal {

	static Boolean DEBUG = true;
	static Boolean TRANSLATED = false;

	static List<Integer[]> permutations;
	static Translator t = Translator.getTranslator();
	static Generator g = Generator.getGenerator();

	static List<Entity> trees = new ArrayList<Entity>();
	public static List<RGoal> rGoals = new ArrayList<RGoal>();
	static int NofRGoals = 0;
	static int NofRGoalsReported = 0;

	// indicate the base object we are operating, e.g., boal, glass
	static final List<String> rPlusPlaces = Arrays.asList(Z.INTO, Markers.IN, Z.UNDER);
	
	static final List<String> rPlus = Arrays.asList(Markers.TO, Z.INTO, Z.ONTO, Z.OVER, 
						Markers.AT, Markers.IN, Markers.ON, Markers.WITH_MARKER, Z.UNDER, Z.AROUND,
						Z.TOWARD);
	
	static final List<String> rManner = Arrays.asList(Z.OUTSIDE);
	
	static final List<String> rMinus = Arrays.asList(Markers.FROM, Z.WITHOUT, Z.UP);
	
	public static final List<String> onPlaces = Arrays.asList("table", "food");
	public static final List<String> inPlaces = Arrays.asList("building", "bowl");
	
	static final List<String> vOneArg = Arrays.asList("breathe","cough", "eat", "focus", 
			"run", "rotate", "scream", "sleep", "smile", "swim", "sink", "think", "walk");
	
	static final List<String> vTwoArgs = Arrays.asList("give", "offer", "pour", "place", 
			"send", "tell");

	static final Map<String, List<String[]>> vIncorporation = new HashMap<String,List<String[]>>();

	// for replacing "she" and "he" with the person
	static final List<String> pronouns = Arrays.asList(Z.SHE, Z.HE, Z.IT, Z.THAT);
	static List<Entity> persons = new ArrayList<>();
	static Entity currentPerson;
	static Boolean SUBSTITUTE_PRONOUN = true;

	// for replacing "she" and "he" with the person
	static List<String> things = new ArrayList<>();
	static Entity currentThing;
	static Boolean SUBSTITUTE_IT = false;
	static List<String> owners = Arrays.asList("his", "her", "its", "their", "our", "my");
	static Map<String, Entity> ownership = new HashMap<>();

	// ---------------------- for RGoal class
	private int index = -1;
	private Entity figure;
	private Entity ground;
	private String relation = Z.CONTACT;
	private Boolean state;
	private String action = "";

	// for Event2State model2: using preposition heuristic
	public RGoal(Entity AAA, Entity BBB, String relation, Boolean state, String action) {
		
		if(ownership.containsKey(AAA.getType()) && AAA.hasProperty(Markers.OWNER_MARKER)) {
			AAA.removeProperty(Markers.OWNER_MARKER);
			AAA.addProperty(Markers.OWNER_MARKER,ownership.get(AAA.getType()));
		}
		if(ownership.containsKey(BBB.getType()) && BBB.hasProperty(Markers.OWNER_MARKER)) {
			BBB.removeProperty(Markers.OWNER_MARKER);
			BBB.addProperty(Markers.OWNER_MARKER,ownership.get(BBB.getType()));
		}
		
//		if(!AAA.getType().equals(Markers.CONJUNCTION) && !BBB.getType().equals(Markers.CONJUNCTION)) {
			NofRGoals++;
			
			Mark.mit(AAA);
			Z.understand(AAA);
			Mark.mit(BBB);
			Z.understand(BBB);
			
			if(relation.startsWith(" ")) relation = relation.substring(1);
			if (relation.equals(Z.INTO))
				relation = Markers.IN;
			this.ground = Z.repairAdWord(BBB);
			this.figure = Z.repairAdWord(AAA);
			this.relation = relation;
			this.state = state; // + or -
			this.action = action.replace(".", "");
			
			Mark.show(AAA);
			Z.understand(figure);
			Mark.show(BBB);
			Z.understand(ground);
			Mark.night(this.toString());
//		}
		
	}

	public RGoal(int index, Entity AAA, Boolean state, Entity BBB) {
		this.index = index;
		this.figure = AAA;
		this.state = state; // + or -
		this.ground = BBB;
	}

	ZRelation toRelation() {
		Entity AAA = this.figure;
		Entity BBB = this.ground;
		Boolean state = this.state;
		String type = Z.CONTACT;
//		Mark.say(AAA, BBB, state);
		ZRelation zRelation = new ZRelation(AAA, BBB, state, type);
		return zRelation;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Entity getFigure() {
		return figure;
	}

	public void setFigure(Entity figure) {
		this.figure = figure;
	}

	public Entity getGround() {
		return ground;
	}

	public void setGround(Entity ground) {
		this.ground = ground;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String acttion) {
		this.action = action;
	}

	@Override
	public String toString() {
		if (index == -1) {
			return ("  " + (state ? "+ " : "- ") + relation.toUpperCase() + " ( " + Z.entity2String(figure) + ", "
					+ Z.entity2String(ground) + " )").replace("_", " ");
		} else {
			return (index + "  " + (state ? "+ " : "- ") + relation.toUpperCase() + " ( " + Z.entity2String(figure) + ", "
					+ Z.entity2String(ground) + " )").replace("_", " ");
		}
	}

	public static void listPrint(List<RGoal> equations) {
		for (RGoal equation : equations) {
			Mark.say(equation.toString());
			return;
		}
		Mark.night("no RGoals found!!");
	}

	public static Boolean equal(RGoal one, RGoal two, int[] attributes) {

		Boolean answer = true;
		for (int attribute : attributes) {
			answer = answer && equals(one, two, attribute);
		}
		return answer;
	}

	public static Boolean equals(RGoal one, RGoal two, int attribute) {

		switch (attribute) {
		case 1:
			if (Z.getName(one.getFigure()).equalsIgnoreCase(Z.getName(two.getFigure())))
				return true;
			break;
		case 2:
			if (one.getState() == two.getState())
				return true;
			break;
		case 3:
			if (Z.getName(one.getGround()).equalsIgnoreCase(Z.getName(two.getGround())))
				return true;
			break;
		default:
			break;
		}
		return false;
	}

	public static Boolean equalsBBB(RGoal one, RGoal two) {

		if (Z.getName(one.getGround()).equalsIgnoreCase(Z.getName(two.getGround()))) {
			return true;
		} else {
			return false;
		}

	}

	public static List<Integer> similarity(RGoal one, RGoal two) {

		List<Integer> similarities = new ArrayList<>();
		if (equals(one, two, 1))
			similarities.add(1);
		if (equals(one, two, 2))
			similarities.add(2);
		if (equals(one, two, 3))
			similarities.add(3);
		return similarities;

	}

	public static List<Integer> difference(RGoal one, RGoal two) {

		List<Integer> differences = new ArrayList<>();
		if (!equals(one, two, 1))
			differences.add(1);
		if (!equals(one, two, 2))
			differences.add(2);
		if (!equals(one, two, 3))
			differences.add(3);
		return differences;

	}

	public static List<Entity> toSwap(RGoal one, RGoal two, int difference) {
		List<Entity> toSwap = new ArrayList<>();

		switch (difference) {
		case 1:
			toSwap.add(one.getFigure());
			toSwap.add(two.getFigure());
			break;
		case 2:
			toSwap = null;
			break;
		case 3:
			toSwap.add(one.getGround());
			toSwap.add(two.getGround());
			break;
		default:
			toSwap = null;
		}

		return toSwap;
	}

	public static RGoal swap(RGoal one, List<Entity> toSwap) {
		if (toSwap != null) {
			if (Z.getName(one.getFigure()).equalsIgnoreCase(Z.getName(toSwap.get(0)))) {
				one.setFigure(toSwap.get(1));
			} else if (Z.getName(one.getFigure()).equalsIgnoreCase(Z.getName(toSwap.get(1)))) {
				one.setFigure(toSwap.get(0));
			}

			if (Z.getName(one.getGround()).equalsIgnoreCase(Z.getName(toSwap.get(0)))) {
				one.setGround(toSwap.get(1));
			} else if (Z.getName(one.getGround()).equalsIgnoreCase(Z.getName(toSwap.get(1)))) {
				one.setGround(toSwap.get(0));
			}
		}
		return one;
	}

	public static void swapAll(List<RGoal> ones, List<Entity> toSwap) {

		if (toSwap != null) {
			for (RGoal one : ones) {
//				Mark.say(toSwap.toString());
				one = swap(one, toSwap);
			}
		}
	}

	public static List<Entity> swapOneByTwo(RGoal one, RGoal two) {

		Mark.say("one before: ", one);
		Mark.say("two before: ", two);

		List<Integer> differences = difference(one, two);
		if (differences.size() == 1) {
			int difference = differences.get(0);
			List<Entity> toSwap = toSwap(one, two, difference);
			Mark.say(toSwap == null ? "null" : toSwap);
//			one = swap(one,toSwap);
			Mark.say("one after: ", one);
			Mark.say("two after: ", two);
			return toSwap;
		} else {
			Mark.say("Failed swap, differences: ", differences.toString());
			return null;
		}

	}

	public static Boolean matchSpecialHolder(RGoal one, RGoal two) {

		if (!ZRelation.isHolder(Z.getName(one.getFigure()))
				&& Z.getName(one.getFigure()).equalsIgnoreCase(Z.getName(two.getFigure()))) {
			return true;
		}

		if (!ZRelation.isHolder(Z.getName(one.getGround()))
				&& Z.getName(one.getGround()).equalsIgnoreCase(Z.getName(two.getGround()))) {
			return true;
		}

		return false;

	}

//	
//	public static void testMatchingRGoals() {
//		permutations = new ArrayList<>();
//
//		List<RGoal> ones = new ArrayList<>();
//		ones.add(new RGoal(1, "bb", true, "cc"));
//		ones.add(new RGoal(2, "oo", true, "air"));
//		ones.add(new RGoal(3, "oo", false, "kk"));
//		ones.add(new RGoal(4, "aa", true, "gg"));
//		ones.add(new RGoal(5, "aa", true, "hh"));
//		ones.add(new RGoal(6, "oo", true, "hh"));
//		ones.add(new RGoal(7, "oo", false, "air"));
//		ones.add(new RGoal(8, "oo", true, "power"));
//		
//		List<RGoal> twos = new ArrayList<>();
//		twos.add(new RGoal(1, "aa", true, "bb"));
//		twos.add(new RGoal(2, "aa", true, "cc"));
//		twos.add(new RGoal(3, "dd", true, "air"));
//		twos.add(new RGoal(4, "dd", false, "ff"));
//		twos.add(new RGoal(5, "dd", true, "bb"));
//		twos.add(new RGoal(5, "dd", false, "air"));
//		
//		listPrint(ones);
//		listPrint(twos);
//		
//		List<RGoal> onesChosen = new ArrayList<>();
//		List<RGoal> twosChosen = new ArrayList<>();
//		List<RGoal> onesNew = new ArrayList<>(ones);
//		List<RGoal> twosNew = new ArrayList<>(twos);
//		
//		List<String> toSwap = new ArrayList<>();
//		for(RGoal two:twos) {
//			for(RGoal one:ones) {
//				if(matchSpecialHolder(one,two)) {
//					List<Integer> differences = difference(one, two);
//					if(differences.size()==1) {
//						if(differences.get(0)!=2) {
//							toSwap = toSwap(one,two,differences.get(0));
//							swapAll(ones,toSwap);
//						}
//					}
//					if(difference(one, two).isEmpty()) {
//						onesChosen.add(one);
//						twosChosen.add(two);
//						onesNew.remove(one);
//						twosNew.remove(two);
//						break;
//					}
//				}
//			}
//		}
//		onesNew = clearSpecialHolders(onesNew);
//		twosNew = clearSpecialHolders(twosNew);
//		Mark.say("ones chosen: ");
//		listPrint(onesChosen);
//		Mark.say("twos chosen: ");
//		listPrint(twosChosen);
//		
//		Mark.say("ones: ");
//		listPrint(onesNew);
//		Mark.say("twos: ");
//		listPrint(twosNew);
//		
//		List<RGoal> oneStage = traceOf(toSwap.get(1),onesNew);
//		List<RGoal> twoStage = traceOf(toSwap.get(1),twosNew);
//		
//		List<String> oneHolders = allHolders(onesNew);
//		List<String> twoHolders = allHolders(twosNew);
//		
//		Mark.say(oneHolders.toString());
//		Mark.say(twoHolders.toString());
//		
////		oneHolders.remove(toSwap.get(1));
////		twoHolders.remove(toSwap.get(1));
//		
//		Set<Integer> s = new HashSet<Integer>();
//		for(int i=0;i<oneHolders.size()-1;i++) {
//			s.add(i);
//		}
//
//		permutations(s, new Stack<Integer>(), s.size());
//		int[] countChains = new int[permutations.size()];
//		int count = 0;
//		for(Integer[] permutation:permutations) {
//			Mark.say(Arrays.toString(permutation));
//			int index1 = 0;
//			for(int index2:permutation) {
//				if(twoHolders.get(index2)!=oneHolders.get(index1)) {
//					toSwap = new ArrayList<>();
//					toSwap.add(twoHolders.get(index2));
//					toSwap.add(oneHolders.get(index1));
//					swapAll(onesNew,toSwap);
//				}
//				index1++;
//			}
//			Mark.say(count+" ones: ");
//			listPrint(onesNew);
//			Mark.say(count+" twos: ");
//			listPrint(twosNew);
//			Mark.say("    Count: "+countSimilarities(onesNew,twosNew));
//			countChains[count] = countSimilarities(onesNew,twosNew);
//			count++;
//		}
//		Mark.say(Arrays.toString(countChains));
//		int max = 0;
//		int maxCounter = 0;
//		for (int counter = 1; counter < countChains.length; counter++){
//		     if (countChains[counter] > max){
//		    	 	  maxCounter = counter;
//		    	 	  max = countChains[counter];
//		     }
//		}
//		Integer[] permutation = permutations.get(maxCounter);
//		Mark.say(Arrays.toString(permutation));
//		int index1 = 0;
//		for(int index2:permutation) {
//			toSwap = new ArrayList<>();
//			toSwap.add(twoHolders.get(index2));
//			toSwap.add(oneHolders.get(index1));
//			swapAll(onesNew,toSwap);
//			index1++;
//		}
//		Mark.say("Final ones: ");
//		listPrint(onesNew);
//		Mark.say("Final twos: ");
//		listPrint(twosNew);
//		
////		allTraces(onesNew);
////		allTraces(twosNew);
//		
//		
////		List<Integer> differences = difference(ones.get(3),twos.get(0));
////		RGoal one = ones.get(3);
////		RGoal two = twos.get(0);
//		
//	}
//	
	public static int countSimilarities(List<RGoal> ones, List<RGoal> twos) {
		int count = 0;
		for (RGoal one : ones) {
			for (RGoal two : twos) {
				if (similarity(one, two).size() == 3)
					count++;
			}
		}
		return count;
	}

	public static void permutations(Set<Integer> items, Stack<Integer> permutation, int size) {

		/* permutation stack has become equal to size that we require */
		if (permutation.size() == size) {
			/* print the permutation */
			Integer[] temp = permutation.toArray(new Integer[0]);
//	        System.out.println(Arrays.toString(temp));
			permutations.add(temp);
		}

		/* items available for permutation */
		Integer[] availableItems = items.toArray(new Integer[0]);
		for (Integer i : availableItems) {
			/* add current item */
			permutation.push(i);

			/* remove item from available item set */
			items.remove(i);

			/* pass it on for next permutation */
			permutations(items, permutation, size);

			/* pop and put the removed item back */
			items.add(permutation.pop());
		}
	}

	public static List<RGoal> clearSpecialHolders(List<RGoal> ones) {

		List<RGoal> onesNew = new ArrayList<>(ones);

		for (RGoal one : ones) {
			if (!ZRelation.isHolder(Z.getName(one.getFigure())) || !ZRelation.isHolder(Z.getName(one.getGround()))) {
				onesNew.remove(one);
			}
		}
		return onesNew;
	}

	public static Map<String, List<RGoal>> allTracesOf(List<RGoal> ones) {
		Mark.say("---------- all traces ------");
		Map<String, List<RGoal>> allTraces = new HashMap<String, List<RGoal>>();
		List<String> allHolders = allHolders(ones);
		Mark.say("all holders: ", allHolders.toString());
		for (String holder : allHolders) {
			allTraces.put(holder, traceOf(holder, ones));
		}
		Mark.say("-----------------------------");
		return allTraces;
	}

	public static List<RGoal> traceOf(String toFind, List<RGoal> ones) {
		Mark.say("Trace of", toFind);
		List<RGoal> found = new ArrayList<>();
		for (RGoal one : ones) {
			if (Z.getName(one.getFigure()) == toFind || Z.getName(one.getGround()) == toFind) {
				found.add(one);
				Mark.say("   " + one.toString());
			}
		}
		return found;
	}

	public static List<String> allHolders(List<RGoal> ones) {
		List<String> found = new ArrayList<>();
		for (RGoal one : ones) {
			if (!found.contains(one.getFigure())) {
				found.add(Z.getName(one.getFigure()));
			}
			if (!found.contains(one.getGround())) {
				found.add(Z.getName(one.getGround()));
			}
		}
		java.util.Collections.sort(found);
		return found;
	}
	
	public static String printRGoalsFull() {
		return printRGoalsFull(NofRGoalsReported);
	}

	public static String printRGoalsFull(int NofRGoalsReported) {
		String result = "";
		for (int i = NofRGoalsReported; i < NofRGoals; i++) {
			result += rGoals.get(i).toString() + "\n";
			Mark.show(rGoals.get(i).toString() + " "+rGoals.get(i).getAction());
		}
		return result;
	}

	public static String printRGoalsFull(List<RGoal> rGoals) {
		String result = "";
		for (int i = 0; i < rGoals.size(); i++) {
			result += rGoals.get(i).toString() + " "+rGoals.get(i).getAction()+ "\n";
			Mark.show(rGoals.get(i).toString() + " "+rGoals.get(i).getAction());
		}
		return result;
	}

	public static String printRGoals() {
		return printRGoals(NofRGoalsReported);
	}

	public static String printRGoals(int NofRGoalsReported) {
		String result = "";
		for (int i = NofRGoalsReported; i < NofRGoals; i++) {
			result += rGoals.get(i).toString() + "\n";
			Mark.show(rGoals.get(i).toString() + " "+rGoals.get(i).getAction());
		}
		return result;
	}

	public static String printRGoals(List<RGoal> rGoals) {
		String result = "";
		for (int i = 0; i < rGoals.size(); i++) {
			result += rGoals.get(i).toString() + "\n";
			Mark.show(rGoals.get(i).toString());
		}
		return result;
	}

	public static void printTrees() {
		for (Entity entity : trees) {
			Z.printInnereseTree(entity);
		}
	}

	public static void testGetDepth() {
		List<String> strings = new ArrayList<>();
		strings.add("I love you");
		strings.add("I give you a cup");
		strings.add("I give you a cup of coffee");
		strings.add("Can I give you a cup of coffee?");
		for (String string : strings) {
			Entity entity = t.translate(string);
			Mark.say("\n\n", string);
			Z.printInnereseTree(entity);
			Mark.say(getDepth(entity));
		}
	}

	public static int getDepth(Entity entity) {
		if (Z.isEnt(entity)) {
			return 1;
		} else {
			int max = 0;
			for (Entity entity1 : entity.getChildren()) {
				int temp = getDepth(entity1);
				if (temp > max) {
					max = temp;
				}
			}
			if (Z.entity2Name(entity).equals(Markers.SEMANTIC_INTERPRETATION)) {
				return max;
			} else {
				return max + 1;
			}

		}
	}

	public static void getTrees(Entity entity) {
		Mark.say(DEBUG, "   \n\n", entity);
//		int count = 0;
//		for (Entity entity1: entity.getChildren()) {
//			Mark.say(DEBUG, "          "+(++count) +"   ",entity1);
//		}

		for (Entity entity1 : entity.getChildren()) {
			Mark.say(DEBUG, "      ", entity1);

			if (entity1.getChildren().size() > 0) {
				Mark.say(DEBUG, "          ", Z.entity2Name(entity1));
				Mark.say(DEBUG, "          ", getDepth(entity1));

				// in case the sentence is negative
				Boolean reverse = Z.isNegative(entity1);

				// =====================================
				//
				// deal with "she", "he", "it"
				//
				// =====================================
//				t.translate("Anselmo is a person");
//				t.translate("James is a person");
//				t.translate("Correia is a person");
				if (SUBSTITUTE_PRONOUN) {
					// replace "she", "he", and "it" with the character's name
					List<Entity> nounEntities = Z.getNounEntities(entity1);
					List<String> nounNames = Z.getNamesFromEntities(nounEntities);

					for (String pronoun : pronouns) {
						if (nounNames.contains(pronoun)) {
							if (currentPerson != null) {
								t.substitute(currentPerson, pronoun, entity1);
								Mark.say("    subtituting " + pronoun + " for..." + currentPerson);
								Mark.say("\n\n              ", entity1, "\n\n");
							} else {
								t.substitute(currentThing, pronoun, entity1);
								Mark.say("    subtituting " + pronoun + " for..." + currentThing);
								Mark.say("\n\n              ", entity1, "\n\n");
							}
						}
					}
					// update character's name that the "she" and "he" are referring to
					for (Entity noun : nounEntities) {
						for (Entity person : persons) {
							Mark.mit(noun, person);
							if (Z.getName(noun).equalsIgnoreCase(Z.entity2Name(person))) {
								currentPerson = person;
								Mark.say("    current person..." + currentPerson);
							}
						}
					}
				}

				// =====================================
				//
				// for each type structure, generate RGoal
				//
				// =====================================

				String name = Z.entity2Name(entity1);
				String action = g.generate(entity1);
				// "a cup of tea" --> +(tea,cup)
				if (name.equals(Z.OF)) {

					trees.add(entity1);

					List<Entity> names = Z.getNounEntities(entity1);
					rGoals.add(new RGoal(names.get(0), names.get(1), Z.OF, true, action));

					// "Add whiskey on top of the muddled sugar"
				} else if (name.equals(Z.OUTOF)) {

					trees.add(entity1);

					Entity subject = entity1.getSubject();
					Entity object = entity1.getObject();
					Z.printInnereseTree(entity1); // TODO
					Z.understand(entity1.getSubject());
					rGoals.add(new RGoal(subject, object, Z.OUT, (reverse ? true : false), action));
					
					// "Add whiskey on top of the muddled sugar"
				} else if (name.equals(Z.ONTOPOF)) {

					trees.add(entity1);

					Entity subject = entity1.getObject();
					Map<String, Entity> roles = Z.getRoleEntities(entity1.getSubject());
					rGoals.add(new RGoal(subject, roles.get(Markers.OBJECT_MARKER), 
							Z.TOP, (reverse ? false : true), action));

				// "pour the milk from the cup into the bowl" --> -(milk,cup), +(milk, bowl)
				} else if (Z.isRel(entity1) && (getDepth(entity1) == 4 || getDepth(entity1) == 5)) {

					trees.add(entity1);
					
					rel2RGoal(entity1, reverse);
				
				// "place the bitters in a glass"
				} else if (Z.isRel(entity1) && getDepth(entity1) == 2) {

					trees.add(entity1);
					
					List<Entity> names = Z.getNounEntities(entity1);
					String rel = Z.getName(entity1);
					if (rPlus.contains(rel)) {
						
						// "serve with a rod"
						if(rel.equals(Markers.WITH_MARKER)) {
							
							Entity figure = entity1.getObject();
							List<String> hasGenerated = new ArrayList<>();
							if (vIncorporation.containsKey(entity1.getSubject().getType())) {
								String relation = "";
								List<String[]> triples = vIncorporation.get(entity1.getSubject().getType());
								for(String[] triple: triples) {
									Mark.mit(triple);
									String figureType = triple[0];
									String groundType = triple[1];
									String position = triple[2];
									String motion = triple[3];
									
									//------ position incorporation, e.g., "the spoon is in the glass"
									if(position.length()>0 && Z.isInstance(figure, groundType) && Z.isInstance(currentThing, figureType)) {
										relation = position;
									}
									
									// ------ motion incorporation, e.g., "the spoon is in circular motion #1"
									if(motion.length()>0 && Z.isInstance(currentThing, figureType)) {
										if(!hasGenerated.contains(motion)) {
											rGoals.add(new RGoal(figure, currentThing, motion, (reverse ? false : true), action));
											hasGenerated.add(motion);
										} 
									}
								}
								if(relation.length()>0) {
									rGoals.add(new RGoal(figure, currentThing, relation, (reverse ? false : true), action));
								}
							}
						} else {
							rGoals.add(new RGoal(names.get(0), names.get(1), Z.getWholeName(entity1),
									(reverse ? false : true), action));
						}
						
					} else if (rMinus.contains(rel)) {
						rGoals.add(new RGoal(names.get(0), names.get(1), Z.getWholeName(entity1),
								(reverse ? true : false), action));
					} else {
						Mark.show("!!! what is this relation: " + rel);
					}
		
					
//				// "add the salad fork outside of the dinner fork."   where "outside" is a manner
//				} else if (Z.isSeq(entity1) && getDepth(entity1) == 3) {
//
//					trees.add(entity1);
//					
//					Map<String, Entity> names = Z.getRoleEntities(entity1);
//					if(names.keySet().contains(Markers.MANNER_MARKER)) {
//						Entity manner = names.get(Markers.MANNER_MARKER);
//						Entity ground = new Entity();
//						String relation = manner.getType();
//						Z.understand(manner);
//						if(manner.hasProperty(Markers.OWNER_MARKER)) {
//							ground = (Entity) manner.getProperty(Markers.OWNER_MARKER);
//							Mark.show(ground);
//						}
//						entity1.addElement(new Function(Z.OUTSIDE,entity));
//					}
//					Relation rel = new Relation(Markers.MAKE, new Entity("somebody"), entity1);
//					rel2RGoal(rel, reverse);
					
					
				// "the knife blade is turned toward the center."
				} else if (!name.equals(Markers.ROLE_MARKER)) {

					trees.add(entity1);

					Relation rel = new Relation(Markers.MAKE, new Entity("somebody"), entity1);
					rel2RGoal(rel, reverse);
					
				} else {
					Mark.say("      ",entity1);
					Mark.show("!!!!!!!!!!!!!! why am I here??");
				}

				getTrees(entity1);

				
			// "to James, who jumped into the water" --> +(james, water)
			} else if (Z.isEnt(entity1)) {

				if (entity1.hasProperty(Markers.CLAUSES)) {
					List<Entity> clauses = (List<Entity>) entity1.getProperty(Markers.CLAUSES);
					for (Entity entity2 : clauses) {

						trees.add(entity2);

						rel2RGoal(entity2, Z.isNegative(entity2));
					}
					Mark.show(clauses.get(0));
					Z.printInnereseTree(clauses.get(0));
				}

			}
		}
	}
	
	// initialize the 
	public static Map<String, List<String[]>> initiateVerbIncorporation() {
		
		List<String[]> triples = new ArrayList<>();
		
		triples.add(new String[]{"substance","container","in", ""});
		vIncorporation.put("add", triples);
		triples = new ArrayList<>();
		
		triples.add(new String[]{"physical-entity","physical-entity","on", ""});
		vIncorporation.put("crush", triples);
		triples = new ArrayList<>();
		
		triples.add(new String[]{"substance","container","in", ""});
		vIncorporation.put("douse", triples);
		triples = new ArrayList<>();

		triples.add(new String[]{"physical-entity","physical-entity","on", ""});
		vIncorporation.put("garnish", triples);
		triples = new ArrayList<>();

		triples.add(new String[]{"physical-entity","physical-entity","around", ""});
		vIncorporation.put("serve", triples);
		triples = new ArrayList<>();

		triples.add(new String[]{"cutlery","container","in", "circ1"});
		triples.add(new String[]{"cutlery","foodstuff","in", "circ1"});
		vIncorporation.put("stir", triples);
		triples = new ArrayList<>();
		
		triples.add(new String[]{"physical-entity","physical-entity","on", ""});
		triples.add(new String[]{"physical-entity","container","in", ""});
		vIncorporation.put("wet", triples);
		triples = new ArrayList<>();
		
		return vIncorporation;
		
	}

	public static Boolean rel2RGoal(Entity entity1, Boolean reverse) {

		initiateVerbIncorporation();
		Boolean madeGoal = false;
		List<String> hasGenerated = new ArrayList<>();

		Entity subject = entity1.getSubject();
		Map<String, Entity> roles = Z.getRoleEntities(entity1);
		
		
		// ---
//		if(roles.keySet().contains(Markers.MANNER_MARKER)) {
//			
//			Entity mannerEntity = roles.get(Markers.MANNER_MARKER);
//			Entity ground = new Entity();
//			String relation = mannerEntity.getType();
//			for(String manner : rManner) {
//				if(relation.equals(manner)) {
//					Z.understand(mannerEntity);
//					if(mannerEntity.hasProperty(Markers.OWNER_MARKER)) {
//						ground = (Entity) mannerEntity.getProperty(Markers.OWNER_MARKER);
//						Mark.show(ground);
//					}
//				}
//			}
//		}

		// +++
		for (String plus : rPlus) {
			
			if (roles.containsKey(plus)) {
				
				Entity ground = roles.get(plus);
				String action = g.generate(entity1);
				
				if (roles.containsKey(Markers.OBJECT_MARKER)) {
					
					Entity figure = roles.get(Markers.OBJECT_MARKER);
					
					if (SUBSTITUTE_IT) {
						Mark.say(figure);
						Mark.say(ground);
						if (ground.equals(figure)) {
							entity1 = Substitutor.substitute(currentThing, figure, entity1);
							String newAction = g.generate(entity1);  //TODO
							Map<String, String> map = Z.getDifferences(action, newAction);
							for(String before: map.keySet()) {
								newAction = newAction.replace(Z.ITSELF, before);
							}
							action = newAction;
							figure = currentThing;
							roles.put(Markers.OBJECT_MARKER, currentThing);
						}
					}

					// "garnish the glass with a slice of orange"
					if (plus.equalsIgnoreCase(Markers.WITH_MARKER)) {
						
						if (vIncorporation.containsKey(entity1.getType())) {
							String rel = "";
							List<String[]> triples = vIncorporation.get(entity1.getType());
							for(String[] triple: triples) {
								String figureType = triple[0];
								String groundType = triple[1];
								String position = triple[2];
								String motion = triple[3];
								
								//------ position incorporation, e.g., "the spoon is in the glass"
								if(position.length()>0 && Z.isInstance(figure, groundType) && Z.isInstance(ground, figureType)) {
									rel = position;
								}
								
								// ------ motion incorporation, e.g., "the spoon is in circular motion #1"
								if(motion.length()>0 && Z.isInstance(ground, figureType)) {
									if(!hasGenerated.contains(motion)) {
										rGoals.add(new RGoal(ground, figure, motion, (reverse ? false : true), action));
										hasGenerated.add(motion);
										madeGoal = true;
									} 
								}
							}
							if(rel.length()>0) {
								rGoals.add(new RGoal(ground, figure, rel, (reverse ? false : true), action));
								madeGoal = true;
							}
						}
//						rGoals.add(new RGoal(roles.get(Markers.OBJECT_MARKER),
//								new Entity(Z.action2PastState(Z.getName(roles.get(Z.ACTION)))), Z.STATE,
//								(reverse ? false : true), g.generate(entity1)));
//						Mark.mit("!!!!!!!! Investigate this: ", rGoals.get(rGoals.size()-1));
						
						
					} else if (plus.equalsIgnoreCase(Markers.TO)) {
						
						for(String inPlace: inPlaces) {
							if(Z.isInstance(figure, inPlace)) {
								madeGoal = true;
								rGoals.add(new RGoal(figure, ground, Markers.IN, (reverse ? false : true), action));
							}
						}
						
						if(!madeGoal) {
							for(String onPlace: onPlaces) {
								if(Z.isInstance(figure, onPlace)) {
									madeGoal = true;
									rGoals.add(new RGoal(figure, ground, Markers.ON, (reverse ? false : true), action));
								}
							}
						}
						
						if(!madeGoal) {
							rGoals.add(new RGoal(figure, ground, Markers.ON, (reverse ? false : true), action));
						}
					
					} else if (plus.equalsIgnoreCase(Z.INTO)) {
						madeGoal = true;
						rGoals.add(new RGoal(figure, ground, Markers.IN, (reverse ? false : true), action));
						
					} else if (plus.equalsIgnoreCase(Z.ONTO)) {
						madeGoal = true;
						rGoals.add(new RGoal(figure, ground, Markers.ON, (reverse ? false : true), action));
					
					} else if (plus.equalsIgnoreCase(Z.OVER)) {
						madeGoal = true;
						rGoals.add(new RGoal(figure, ground, Markers.ON, (reverse ? false : true), action));
						
					} else if (plus.equalsIgnoreCase(Markers.ON)) {
						
						Z.understand(ground);
						if(ground.hasProperty(Markers.OWNER_MARKER)) {
							madeGoal = true;
							String relation = ground.getType();
							ground = (Entity) ground.getProperty(Markers.OWNER_MARKER);
							rGoals.add(new RGoal(figure, ground, relation, (reverse ? false : true), action));
						} else {
							Mark.mit("!!!! investigate");
						}
						
						
					} else {
						
						madeGoal = true;
						// "the bread plate goes up in the left corner."
						if(figure.getType().equals(Z.UP)) {
							rGoals.add(new RGoal(subject, ground, plus, (reverse ? false : true), action));
						} else {
							rGoals.add(new RGoal(figure, ground, plus, (reverse ? false : true), action));
						}
						
						
					}

				} else {
					// "stir the glass with a spoon"
					if (plus.equals(Markers.WITH_MARKER) && currentThing != null) {
						
						if (vIncorporation.containsKey(entity1.getType())) {
							List<String[]> triples = vIncorporation.get(entity1.getType());
							for(String[] triple: triples) {
								String figureType = triple[0];
								String groundType = triple[1];
								String position = triple[2];
								String motion = triple[3];
								
								//------ position incorporation, e.g., "the spoon is in the glass"
								if(position.length()>0 && Z.isInstance(currentThing, groundType) && 
										Z.isInstance(roles.get(plus), figureType)) {
									rGoals.add(new RGoal(roles.get(plus), currentThing, position,
											(reverse ? false : true), g.generate(entity1)));
									madeGoal = true;
								}
								
								// ------ motion incorporation, e.g., "the spoon is in circular motion #1"
								if(motion.length()>0 && Z.isInstance(roles.get(plus), figureType)) {
									if(!hasGenerated.contains(motion)) {
										rGoals.add(new RGoal(roles.get(plus), currentThing, motion,
												(reverse ? false : true), g.generate(entity1)));
										hasGenerated.add(motion);
										madeGoal = true;
									} 
								}
							}
						}
						
						// generate "stirred" and "crunched" as answers
//						rGoals.add(new RGoal(currentThing, roles.get(plus), plus, (reverse ? false : true), g.generate(entity1)));
//						rGoals.add(new RGoal(currentThing, new Entity(Z.action2PastState(Z.getName(roles.get(Z.ACTION)))),
//										Z.STATE, (reverse ? false : true), g.generate(entity1)));
						
					} else {
						
						// "pour in the rye"
						if(vTwoArgs.contains(roles.get(Z.ACTION).getType())) {
							rGoals.add(new RGoal(ground, currentThing, plus, (reverse ? false : true), g.generate(entity1)));
						
						// "jump in the river"
						} else {
							rGoals.add(new RGoal(subject, ground, plus, (reverse ? false : true), g.generate(entity1)));
						}
						
						Mark.mit("!!!!!!!! Investigate this: ", rGoals.get(rGoals.size()-1));
						madeGoal = true;
					}
				}
				if (rPlusPlaces.contains(plus)) {
					currentThing = roles.get(plus);
					for (Entity entity : Z.getNounEntities(entity1)) {
						String longStr = Z.getName(roles.get(plus));
						if (longStr.contains(" ")) {
							longStr = longStr.substring(longStr.lastIndexOf(" ") + 1);
						}
						if (longStr.equals(Z.entity2Name(entity))) {
							currentThing = entity;
						}
					}
				}
			}
		}
		
		// ---
		for (String minus : rMinus) {
			
			Entity figure = roles.get(Markers.OBJECT_MARKER);
			Entity ground = roles.get(minus);
			String action = g.generate(entity1);
			
			if (roles.containsKey(minus)) {
				madeGoal = true;
				if (roles.containsKey(Markers.OBJECT_MARKER)) {
					
					if (SUBSTITUTE_IT) {
						if (ground.equals(figure)) {
							figure = currentThing;
							roles.put(Markers.OBJECT_MARKER, currentThing);
						}
					}
					
					rGoals.add(new RGoal(figure, ground, minus, (reverse ? true : false), action));
				} else {
					rGoals.add(new RGoal(subject, ground, minus, (reverse ? true : false), action));
				}
			}
		}

		// for verbs that don't have prepositions
		if (!madeGoal) {
			if (currentThing != null) {
				if (vIncorporation.containsKey(entity1.getType())) {
					String relation = "";
					List<String[]> triples = vIncorporation.get(entity1.getType());
					for(String[] triple: triples) {
						String figureType = triple[0];
						String groundType = triple[1];
						String position = triple[2];
						String motion = triple[3];
						
						//------ position incorporation, e.g., "the spoon is in the glass"
						if(position.length()>0 && Z.isInstance(currentThing, groundType) && 
								Z.isInstance(roles.get(Markers.OBJECT_MARKER), figureType)) {
							relation = position;
						}
						
						// ------ motion incorporation, e.g., "the spoon is in circular motion #1"
						if(motion.length()>0 && Z.isInstance(roles.get(Markers.OBJECT_MARKER), figureType)) {
							rGoals.add(new RGoal(roles.get(Markers.OBJECT_MARKER), currentThing, motion,
									(reverse ? false : true), g.generate(entity1)));
							madeGoal = true;
						}
					}
					if(relation.length()>0) {
						rGoals.add(new RGoal(roles.get(Markers.OBJECT_MARKER), currentThing, relation,
								(reverse ? false : true), g.generate(entity1)));
						madeGoal = true;
					}
				}
			} else {
				Mark.say("!!!!!!!!!!!!!! no rGoal from", entity1);
			}
		}

		return madeGoal;
	}

	public static String Event2State2(String string) {
		
		Mark.say("\n\n==========================================\n\n");
		Mark.say("\n\n" + string + "\n\n");

		Entity entity = t.translate(string);
		
		// substitute "it" because Translator makes it into "itself"
		SUBSTITUTE_IT = false;
		if (string.contains(" it ")) {
			if (!Z.getNounEntities(entity).contains(Z.IT)) {
				SUBSTITUTE_IT = true;
			}
		}
		
		// take note of his and her, because Translator makes mistakes in ownership given multiple sentences
		for(String owner: owners) {
			string = string.toLowerCase();
			if (Z.contains(string, owner)&&currentPerson!=null) {
				String object = string.substring(string.indexOf(owner)+owner.length()+1, string.length());
				object = object.substring(0,object.indexOf(" "));
				ownership.put(object, currentPerson);
				Mark.night(ownership);
			}
		}

		Z.printInnereseTree(entity, true);
		Mark.say("--------------------");
		getTrees(entity);
		return printRGoals();
		
	}

	public static void testModel2(List<String> strings) {

		// read each sentence
		for (String string : strings) {
			Event2State2(string);
			printRGoals(NofRGoalsReported);
			NofRGoalsReported = NofRGoals;
		}

	}

	public static List<String> getStory(String storyName) {
		// read stories
		Map<String, List<String>> returns = Z.readStoryFromFile(storyName);

		// add characters
		List<String> strings = returns.get(Z.SENTENCES);
		for (String sentence : returns.get(Z.ASSIGNMENTS)) {
			Entity assignment = t.translate(sentence);
			Mark.say("    internalizing...", sentence);
			List<String> nounEntities = Z.getNounNames(assignment);
			if (nounEntities.contains(Z.PERSON) || nounEntities.contains(Z.THING)) { // TODO
				persons.add(assignment.getElement(0).getObject());
			}
		}
		return strings;
	}

	public static List<String> getExamples() {
		List<String> strings = new ArrayList<>();
		persons.add(new Entity("James"));
		persons.add(new Entity("Correia"));
		persons.add(new Entity("Anselmo"));
		currentPerson = persons.get(1);
//		strings.add("but he then went under the water and he didn't come up.");
		strings.add("Anselmo yelled to James, who jumped into the pool.");
//		strings.add("He jumped over the fence and into the water.");
//		strings.add("Together they pulled up the boy, handing him to an uncle.");
//		strings.add("The gunk and water poured out of his mouth and nose.");
//		strings.add("Tiverton rescue personnel arrived as the boy was coming back to life.");
		return strings;
	}

	public static List<ZRelation> rGoals2ZRelations(List<RGoal> rGoals) {
		List<ZRelation> zRelations = new ArrayList<>();
		for (RGoal rGoal : rGoals) {
			zRelations.add(rGoal.toRelation());
		}
		return zRelations;
	}

	public static void initialize() {

		trees = new ArrayList<Entity>();
		rGoals = new ArrayList<>();
		NofRGoals = 0;
		NofRGoalsReported = 0;

		persons = new ArrayList<>();
		currentPerson = null;
		SUBSTITUTE_PRONOUN = true;

		things = new ArrayList<>();
		currentThing = null;
		SUBSTITUTE_IT = false;

		Z.NofObj = 0;
		ZRelation.reset();

	}

	public static Boolean matchRGoal(RGoal one, RGoal two) {

		if (one.getState() == two.getState() && one.getRelation() == two.getRelation()) {

			String OneAAA = Z.getName(one.getFigure()).replace("_", " ");
			if (OneAAA.contains(" "))
				OneAAA = OneAAA.substring(OneAAA.lastIndexOf(" ")).replace(" ", "");
			String OneBBB = Z.getName(one.getGround()).replace("_", " ");
			if (OneBBB.contains(" "))
				OneBBB = OneBBB.substring(OneBBB.lastIndexOf(" ")).replace(" ", "");

			String twoAAA = Z.getName(two.getFigure()).replace("_", " ");
			if (twoAAA.contains(" "))
				twoAAA = twoAAA.substring(twoAAA.lastIndexOf(" ")).replace(" ", "");
			String twoBBB = Z.getName(two.getGround()).replace("_", " ");
			if (twoBBB.contains(" "))
				twoBBB = twoBBB.substring(twoBBB.lastIndexOf(" ")).replace(" ", "");

//			Z.check(OneAAA.equals(twoAAA));
//			Z.check(OneBBB.equals(twoBBB));
//			Z.check(OneAAA.equals(twoBBB));
//			Z.check(OneBBB.equals(twoAAA));

			if ((OneAAA.equals(twoAAA) && OneBBB.equals(twoBBB)) || (OneAAA.equals(twoBBB) && OneBBB.equals(twoAAA))) {
				Mark.show(OneAAA, OneBBB, twoAAA, twoBBB);
				return true;
			}
			Mark.say(OneAAA, OneBBB, twoAAA, twoBBB);
		}

		return false;
	}

	public static List<RGoal> getCommonRGoals(List<RGoal> ones, List<RGoal> twos) {
		List<RGoal> common = new ArrayList<>();
		for (RGoal one : ones) {
			for (RGoal two : twos) {
				if (matchRGoal(one, two)) {
					Mark.say("     matched......", one, two);
					common.add(one);
					break;
				}
			}
		}
		return common;
	}

	public static List<RGoal> getCommonRGoals(List<List<RGoal>> stories) {
		List<RGoal> commons = stories.get(0);
		int size = stories.size();
		if (size >= 2) {
			for (int i = 1; i < size; i++) {
				commons = getCommonRGoals(commons, stories.get(i));
			}
		}
		return commons;
	}

	public static void main(String[] args) {

//		testGetDepth();

//		Mark.mit("Relevant relations in Martini3");
//		RGoal.listPrint(getCommonRGoals(ones, rGoals));
//		Mark.mit("Relevant relations in Martini4");
//		RGoal.listPrint(getCommonRGoals(rGoals, ones)); 

//		testMatchingRGoals();
		testRGoalFromString();
//		testRGoalFromStory();
	}
	
	public static void testRGoalFromString() {
		List<String> strings = new ArrayList<>();
//		strings.add("the bread plate goes up in the left corner.");
		
		persons.add(new Entity("James"));
		persons.add(new Entity("Correia"));
		persons.add(new Entity("Anselmo"));
		strings.add("Anselmo walked from a swing set to the fence.");
		strings.add("His attention was drawn to James.");
		
		// failure mode
//		strings.add("add the salad fork outside of the dinner fork.");
		
		for(String string: strings) {
			Event2State2(string);
		}
//		RecipeLearner.writeKnowledgeFile("Make an old fashioned",rGoals);
//		Z.rGoals2States(rGoals);
	}
	
	public static void testCurrentPerson() {

//		t.translate("Anselmo is a person");
//		t.translate("James is a person");
//		t.translate("Correia is a person");
//		testModel2(getExamples());
//		Mark.say(currentPerson);

	}
	
	public static void testRGoalFromStory() {

		String goal = "Save Drowning People";
//		testModel2(getStory("Martini1"));
//		List<RGoal> ones = rGoals;
//		String rGoals1 = printRGoalsFull(0);
//		Z.rGoals2States(rGoals);
//		
		initialize();
		testModel2(getStory("Save Drowning People 1"));
		List<RGoal> twos = rGoals;
		String rGoals2 = printRGoals(0);
		
//		initialize();
//		testModel2(getStory("Martini2"));
//		List<RGoal> threes = rGoals;
//		String rGoals3 = printRGoals(0);
//		
//		RecipeLearner.writeKnowledgeFile(goal,getCommonRGoals(twos, rGoals));
//		
//		initialize();
//		testModel2(getStory("News2"));
//		String rGoals4 = printRGoals(0);
//		
//		Mark.mit("All relations in Martini1");
//		Mark.say(rGoals1);
//		
//		Mark.mit("All relations in Martini2");
//		Mark.say(rGoals2);
//		
//		Mark.mit("All relations in News1");
//		Mark.say(rGoals3);
//		
//		Mark.mit("All relations in News2");
//		Mark.say(rGoals4);
	}

	public static void testMatchingRGoals() {
		String[] strings = new String[20];
		strings[0] = "In an Old-Fashioned glass, muddle the sugar and bitters.";
		strings[1] = "Put the bourbon and Angostura bitters in a mixing glass.";

		strings[2] = "Douse it with bitters and add a few drops of water.";
		strings[3] = "Place sugar in an Old Fashioned glass.";

		Event2State2(strings[0]);
		List<RGoal> ones = rGoals;
		String string1 = printRGoals(0);

		Event2State2(strings[1]);

		Mark.say(string1);
		printRGoals(0);
		RGoal.listPrint(getCommonRGoals(ones, rGoals));
	}

}