package jessicaNoss;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import frames.entities.Entity;
import frames.entities.Sequence;
//import parameters.Switch;
//import utils.Html;
import utils.Mark;
//import constants.Markers;
import generator.Generator;

public abstract class JmnUtils {
	public static String entityToText(Entity e) {
		return Generator.getGenerator().generate(e);
	}

	public static List<String> convertEntitiesToStrings(List<Entity> entities) {
		return entities.stream().map(e -> JmnUtils.entityToText(e)).collect(Collectors.toList());
	}

	public static List<String> convertBooleansToStrings(List<Boolean> booleans, Function<Boolean, String> fn) {
		return booleans.stream().map(b -> fn.apply(b)).collect(Collectors.toList());
	}

	public static List<String> convertBooleansToStrings(List<Boolean> booleans, String trueString, String falseString) {
		return booleans.stream().map(b -> b ? trueString : falseString).collect(Collectors.toList());
	}

	public static List<Boolean> convertPresenceIDsToBooleans(List<Integer> presenceIDs, Integer length) {
		List<Boolean> booleans = new ArrayList<Boolean>();
		for (int i=0; i<length; i++) {
			booleans.add(presenceIDs.contains(i));
		}
		return booleans;
	}

	public static List<List<String>> generateFakeData(String s) {
		List<String> row0 = Arrays.asList(s+"a", s+"b", s+"c");
		List<String> row1 = Arrays.asList(s+"e", s+"f", s+"g");
		List<String> row2 = Arrays.asList(s+"h", s+"i", s+"j");
		List<List<String>> data = new ArrayList<List<String>>();
		data.addAll(Arrays.asList(row0, row1, row2));
		return data;
	}
	
	public static String sequenceToString(Sequence story) {
//		Stream<Entity> stream = story.stream(); //not sure how to use streams
		String english = "";
		for (Entity entity : story.getElements()) {
			String sentence = Generator.getGenerator().generate(entity);
			if (sentence != null) {
				english += sentence.trim() + "  ";
			}
			else {
				Mark.err("Unexpected null sentence");
			}
		}
		return english;
	}
}
