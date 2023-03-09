package zhutianYang.AcquireRecipes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import frames.entities.Entity;
import generator.Generator;
import translator.Translator;
import utils.Mark;
import utils.Z;
import zhutianYang.RecipeLearner;

public class RecipeGenerator {
	
	public static final Boolean VERBOSE = false;

	public static final String folder_wiki = "students/zhutianYang/AcquireRecipes/WikiHow_input/";
	public static final String folder_beautified = "students/zhutianYang/AcquireRecipes/Beautified_output/";
	public static final String folder_translated = "students/zhutianYang/AcquireRecipes/Translated_output/";
	public static final String folder_generated = "students/zhutianYang/AcquireRecipes/Generated_output/";
	
	public static final int MIN_LINE_LENGTH = 10;
	public static final Boolean NOT_OVERWRITE = false;

	public static Map<String, Double> performance = new HashMap<>();
	public static Translator t = Translator.getTranslator();
	public static Generator g = Generator.getGenerator();

	public static void main(String[] args) {
//		String file = "Choose-Bluetooth-Headphones.txt";
//		generateRecipes(file);
		
		List<String> files = listFilesForFolder(new File(folder_wiki));
		
		for(String file:files) {
			if(file.endsWith(".txt")) {
				Mark.say("Generating for... " + file);
				generateRecipes(file);
//				beautifyFile(file);
//				translateFile(file);
			}
		}
		Mark.say("----------------------------------");
		Mark.say(performance);
	}
	
	// find all steps after "Part #"
	public static void generateRecipes(String fileName) {
		
		File f = new File(folder_wiki + fileName);
		
		String goal = fileName.replace("-"," ").replace(".txt","");
		goal = Z.sentence2Action(Z.string2Capitalized(goal));
		List<String> steps1 = new ArrayList<>(); // steps signified by "Part #"
		List<Integer> indecies1 = new ArrayList<>(); // index of new "Part #"
		
		String date = new SimpleDateFormat("MMdd_HHmmss").format(Calendar.getInstance().getTime());
		String stepsFileNameString = "Steps_"+ goal +"_" + date;
		String stepsFileName = RecipeLearner.filePath + stepsFileNameString + ".txt";
		
		List<String> lines = new ArrayList<>();
		if(f.exists() && !f.isDirectory()) { 
			try {
				List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(folder_wiki + fileName), StandardCharsets.UTF_8));

				boolean hasLevel2 = false;
				// beautify first with numbers remains
				for(String line: fileContent) {
					if(line.contains("Part ")) hasLevel2 = true;
					if(line.contains(". ")){
						String[] sentences = line.split("\\. ");
						for(String sentence: sentences) {
							if(sentence.endsWith("]")) sentence = sentence.substring(0, sentence.indexOf("["));
							if(!sentence.endsWith(".")) sentence = sentence + ".";
							lines.add(sentence);
						}
					} else {
						if(line.endsWith("]")) line = line.substring(0, line.indexOf("["));
						lines.add(line);
					}
				}
				lines.remove("Steps");
				
				// get highest level steps
				if(hasLevel2) {
					for(int i = 0; i < lines.size(); i++) {
						String line = lines.get(i);
						if(line.startsWith("Part ")) {
							String number = line.substring(5,line.length());
							if(isNumeric(number)) {
								steps1.add(Z.sentence2Action(lines.get(i+1)));
								indecies1.add(i+1);
							}
						}
					}
					Mark.say("-----------------------");
					Mark.say("---- Level 1 steps: Problem = "+goal);
					Z.printList(steps1);
					generateRecipes(stepsFileName, goal, steps1);
					Mark.say("-----------------------");
					
					List<List<String>> allSubSteps = new ArrayList<>();
					for(int i = 0; i < indecies1.size(); i++) {
						int index = indecies1.get(i);
						int indexNext;
						if(i == indecies1.size()-1) {
							indexNext = lines.size()+1;
						} else {
							indexNext = indecies1.get(i+1);
						}
						List<String> subLines = lines.subList(index, indexNext-1);
						List<String> subSteps = new ArrayList<>();
						for(int j = 0; j < subLines.size(); j++) {
							String line = subLines.get(j);
							if(line.length()<3) {
								if(isNumeric(line)) {
									Mark.say(subLines.get(j+1));
									subSteps.add(Z.sentence2Action(subLines.get(j+1)));
								}
							}
						}
						Mark.say("-----------------------");
						Mark.say("---- Part "+ (i+1) + ": Problem = "+steps1.get(i));
						Z.printList(subSteps);
						generateRecipes(stepsFileName, steps1.get(i), subSteps);
						Mark.say("-----------------------");
						allSubSteps.add(subSteps);
					}
				} else {
					for(int j = 0; j < lines.size(); j++) {
						String line = lines.get(j);
						if(line.length()<3) {
							if(isNumeric(line)) {
								Mark.say(lines.get(j+1));
								steps1.add(Z.sentence2Action(lines.get(j+1)));
							}
						}
					}
					Mark.say("-----------------------");
					Mark.say("---- Problem = "+goal);
					Z.printList(steps1);
					generateRecipes(stepsFileName, goal, steps1);
					Mark.say("-----------------------");
				}
				
				try {
					FileWriter writer = new FileWriter(folder_generated + fileName, NOT_OVERWRITE);
					for (String line: lines) {
						writer.write(line+"\n");
					}
					writer.close();
//					Z.printTXTFile(folder_generated + fileName);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void generateRecipes(String fileName, String goal, List<String> steps) {
		RecipeLearner.writeKnowledgeFile(fileName, goal, "", steps);
	}
	
	public static void beautifyFile(String fileName) {
		File f = new File(folder_wiki + fileName);
		
		List<String> lines = new ArrayList<>();
		if(f.exists() && !f.isDirectory()) { 
			try {
				List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(folder_wiki + fileName), StandardCharsets.UTF_8));

				for(String line: fileContent) {
					if(line.contains(". ")){
						String[] sentences = line.split("\\. ");
						for(String sentence: sentences) {
							if(sentence.endsWith("]")) {
								sentence = sentence.substring(0, sentence.indexOf("["));
							}
							if(!sentence.endsWith(".")) {
								sentence = sentence + ".";
							}
							lines.add(sentence + "\n");
						}
					} else {
						if(line.endsWith("]")) {
							line = line.substring(0, line.indexOf("["));
						}
						lines.add(line);
					}
					
//					if(line.length() > MIN_LINE_LENGTH) {
//						
//					} else {
//						lines.add("\n\n");
//					}
				}
				
				try {
					FileWriter writer = new FileWriter(folder_beautified + fileName, NOT_OVERWRITE);
					for (String line: lines) {
						writer.write(line);
					}
					writer.close();
					Z.printTXTFile(folder_beautified + fileName);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static void translateFile(String fileName) {
		File f = new File(folder_beautified + fileName);
		List<String> lines = new ArrayList<>();
		int total = 0;
		int translated = 0;
		if(f.exists() && !f.isDirectory()) { 
			try {
				List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(folder_beautified + fileName), StandardCharsets.UTF_8));

				for(String line: fileContent) {
					if(line.length() > MIN_LINE_LENGTH) {
						total++;
						lines.add(line);
						Mark.say("\n"+line);
						Entity entity = t.translate(line);
						String innerese = entity.toEnglish();
						if(innerese.startsWith("(") && !innerese.startsWith("(seq thing)")) {
							translated++;
							String generated = "";
							generated = g.generate(entity);
							if(!generated.equals("")) {
								lines.add(generated);
								Mark.say(generated);
							} else {
								lines.add("--------------");
								Mark.say("---------------generator cannot");
							}
							
						} else {
							lines.add("--------------");
							Mark.say("---------------translater cannot");
						}
					} 
					lines.add("\n");
				}
				performance.put(fileName, (double) total/translated );
				

				try {
					FileWriter writer = new FileWriter(folder_translated + fileName, NOT_OVERWRITE);
					for (String line: lines) {
						writer.write(line);
					}
					writer.close();
					Z.printTXTFile(folder_translated + fileName);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static boolean isNumeric(String str)  {  
		try{  
			double d = Double.parseDouble(str);  
		} catch (NumberFormatException nfe) {  
		    return false;  
		}  
		return true;  
	}

	public static List<String> listFilesForFolder(File folder) {
		List<String> files = new ArrayList<>();
	    for (final File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	        		files.add(fileEntry.getName());
//	            System.out.println(fileEntry.getName());
	        }
	    }
	    Mark.say(files);
	    return files;
	}
	
}
