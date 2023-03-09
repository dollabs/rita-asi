package zhutianYang;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import constants.Markers;
import frames.entities.Entity;
import generator.Generator;
import start.Start;
import translator.Translator;
import utils.Mark;
import utils.Z;

public class StratagemLearner {
	
	public static Translator t = StratagemExpert.getTranslator();
	public static Generator g = Generator.getGenerator();
	// templates
	public static final String COMMENT = "// Generated on date by students/zhutianYang/StratagemLearner.java";
	public static final String WRITE_PROBLEM = "If the intention is \"";
	public static final String WRITE_CHECK = "Condition: Check \"";
	public static final String WRITE_HAPPEN = "Step: Happen \"";
	public static final String WRITE_CONDITION = "Condition: ";
	public static final String WRITE_STEP = "Step: ";
	public static final String WRITE_THE_END = "The end.";
	
	public static List<String> names = new ArrayList<>();
	public static List<String> holders = Arrays.asList("xxx","yyy","zzz", "mmm", "nnn", "ooo", "ppp");

	public static void main(String[] args) {
		String problem;
		List<String> checks = new ArrayList<>();
		List<String> steps = new ArrayList<>();
		List<String> happens = new ArrayList<>();
		
//		problem = "who may make Yuan become dead";
//		steps.add("A beautiful lady can kill Yuan");
//		writeMicroStory(problem, conditions, happens, steps);
		
		problem = "make king distrust yuan";
		happens.add("I spread rumor to make king believe that xxx has excessive power");
//		steps = new ArrayList<>();
//		happens.add("the lady can seduce Yuan and she can poison him.");
		writeMicroStory(problem, checks, happens, steps);
		
//		Mark.say(getSeparated(checks));
		
		//// 180710
//		List<String> strings = new ArrayList<>();
//		strings.add("Yuan is my friend and I will not let him down");
//		strings.add("Sometimes I don't see him");
//		strings.add("Sometimes Yuan don't see me");
//		Z.printList(makeAnonymous(strings));
	}
	
	public static List<String> writeMicroStory(String problem, List<String> checks, List<String> happens, List<String> steps) {
		return writeMicroStory(problem, checks, happens, steps, new ArrayList<String>());
	}
	
	public static List<String> writeMicroStory(String problem, List<String> checks, List<String> happens, List<String> steps, List<String> namesInStory) {

		if(steps.isEmpty()&&happens.isEmpty()) {
			return null;
		}
		for(String name: namesInStory) {
			name = name.substring(0,1).toUpperCase()+name.substring(1);
			if(!names.contains(name)) {
				Mark.say("add name!!! "+ name);
				names.add(name);
			}
		}
		
		String fileName = StratagemExpert.stratagemMicroStoriesFile;
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		try {
			FileWriter writer = new FileWriter(fileName, true);
			writer.write("\r\n\r\n"+COMMENT.replace("date", "0"+date)+"\r\n");
			List<String> toWrite = new ArrayList<>();
			
			// to write problem
			problem = Z.toPresentTense(problem).get(0);
			toWrite.add(WRITE_PROBLEM + problem.replace(".", "") + "\".");
			
			// to write checkable conditions
			checks = Z.getSeparated(checks);
			if(!checks.isEmpty()) {
				for(String condition:checks) {
					if(!Z.contains(steps,condition)) {
						toWrite.add(WRITE_CHECK + condition + "\".");
					}
				}
			}
			
			// to write normal steps
			steps = Z.getSeparated(steps);
			if(!steps.isEmpty()) {
				for(String step:steps) {
					toWrite.add(WRITE_STEP + step + ".");
				}
			}
			
			// to write just-do-it steps
			happens = Z.getSeparated(happens);
			if(!happens.isEmpty()) {
				for(String happen:happens) {
					toWrite.add(WRITE_HAPPEN + happen + "\".");
				}
			}
			
			toWrite = makeAnonymous(toWrite);
			for(String ha:toWrite) {
				ha = StratagemExpert.capitalizeNames(ha,names);
				writer.write(ha+"\r\n");
			}
			
			writer.write(WRITE_THE_END);
			writer.write("\r\n");
			writer.close();
			Z.printTXTFile(fileName);
			
			return toWrite;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	public static List<String> makeAnonymous(List<String> toWrites){
		List<String> newToWrites = new ArrayList<>();
		List<String> words = Arrays.asList(Z.splitAll(toWrites.toString()));
		int count = 0;
		Map <String, String> bindings =  new HashMap<String, String>();
		for(String name: names) {
			if(words.contains(name) || words.contains(name.toLowerCase())) {
				bindings.put(name, holders.get(count++));
			} 
		}
		for(String toWrite:toWrites) {
			String toAdd = toWrite;
			for(String key: bindings.keySet()) {
				
				toAdd = Z.replaceString(toAdd, key, bindings.get(key));
				toAdd = Z.replaceString(toAdd, key.toLowerCase(), bindings.get(key));
				toAdd = toAdd.substring(0,1).toUpperCase()+toAdd.substring(1);
				if(toAdd.toLowerCase().startsWith(bindings.get(key))) {
					toAdd = toAdd.substring(0,1).toLowerCase() + toAdd.substring(1);
				}
				toAdd = toAdd.replace(bindings.get(key).toUpperCase(), bindings.get(key));
				
			}
			newToWrites.add(toAdd);
			
		}
		return newToWrites;
	}

}
