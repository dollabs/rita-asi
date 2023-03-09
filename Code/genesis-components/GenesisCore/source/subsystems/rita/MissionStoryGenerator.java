package subsystems.rita;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import frames.entities.Entity;
import generator.Generator;
import translator.BasicTranslator;
import translator.Translator;
import utils.Mark;

public class MissionStoryGenerator {
	
	List<Map<String, Object>> allData = new ArrayList<Map<String, Object>>();
	List<String> entries = new ArrayList<>(Arrays.asList("yaw","motion_x","motion_z","motion_y","x","y","z","pitch","world_time"));
	
	public void addData(HashMap<String, Object> data) {
		Map<String, Double> newData = new HashMap<String, Double>();
		for (String key: data.keySet()) {
			if (entries.contains(key)) {
				System.out.println(key + ": " + data.get(key));
				newData.put(key, (Double) data.get(key));
			}
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		Translator t = Translator.getTranslator();
		Generator g = Generator.getGenerator();
		String sentence = "Bob got angry";
		Entity entity = t.translate(sentence);
		Mark.say(sentence);
		Mark.say(entity);
//		Mark.say(g.generate(entity));
	}

}
