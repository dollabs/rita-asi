/**
 * 
 */
package zhutianYang;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrame;
import start.Start;
import translator.Translator;
import utils.Mark;
import utils.Z;

/**
 * @author z
 *
 */
public class DirectPerceptionExpert {
	
	static String instructionsFolder = "/students/zhutianYang/instruction_data";
	static String localPath = "";
	
	public static List<String[]> selectTriples(String s) {
		StringBuffer input = new StringBuffer(s);
		StringBuffer result = new StringBuffer();
		List<String[]> triples = new ArrayList<>();
		int start, end;
		while ((start = input.indexOf("[")) >= 0) {
			end = input.indexOf("]", start);
			String triple = input.substring(start+1, end);
			List<String> exceptions = Arrays.asList(
					"has_det", "has_tense", "has_rel_clause", "has_rel_clause+2", "has_modifier", "has_intensifier", 
					"has_position", "has_number", "has_person", "has_purpose", "has_category", "has_clause_type",
					"is_question", "is_main", "is_proper", "is_perfective", "is_imperative", "is_progressive");
			if(!exceptions.contains(triple.split(" ")[1])) { 
				triples.add(triple.split(" "));
			}
			input.delete(0, end + 1);
		}
		printTriples(triples);
		return triples;
	}
	
	public static String printSelectedTriples(String s) {
		String printTriple = "";
		for(String[] triple: selectTriples(s)) {
			printTriple += ("["+triple[0]+" "+triple[1]+" "+triple[2]+"]\n");
		}
		return printTriple;
	}
	
	public static void printTriples(List<String[]> triples) {
		Mark.say("--------------------");
		for(String[] triple:triples) {
			Mark.say(triple);
		}
		Mark.say("--------------------");
	}
	
	public static List<String[]> triplesOriginal = new ArrayList<>();
	public static List<String[]> triplesFormatted = new ArrayList<>();
	public static String placeHolder = ":";
	public static int col = 0;
	public static int row = 0;

	public static List<String[]> getTrainingTriples(String sentence) {
		Sequence sequenceFromStart = Start.getStart().processForTestor(sentence);
		String rawText = Start.getStart().getProcessedSentence();
		List<String[]> triples = selectTriples(rawText);
		
		
		// for formatting triples into trees
		while(true) {
			int count = 0;
			triplesOriginal = new ArrayList<>(triples);
			triplesFormatted = new ArrayList<>();
			col = 0;
			row = 0;
			while(triplesOriginal.size()!=0) {
				// input the rest of the rows, modify triplesOriginal to triplesFormatted
				organizeTriples(triplesOriginal);
			}
			for(String[] triple: triplesFormatted) {
//				Mark.night(triple);
				if(triple[0].startsWith("00")) {
					count++;
				}
			}
			if(count==1) {
				break;
			} else {
				triples = shiftOne(triples);
			}
		}
		printFormattedTriples();
		Mark.mit(Z.getPrettyTree(sentence));;
		
		return triples;
	}

	public static void organizeTriples(List<String[]> triples){
		String[] triple = triplesOriginal.get(0);
		String[] newTripleFormatted = new String[3];
		triplesOriginal.remove(triple);
		
		for(int i=0;i<3;i++) {
			String element = triple[i];
			newTripleFormatted[i] = row + "" + col + placeHolder + element;
			
			String[] findTriple = findTriple(element);
			if(findTriple!=null) {
				row++;
				organizeTriples(triplesOriginal);
				row--;
			}
			col++;
		}
		triplesFormatted.add(newTripleFormatted);
		Mark.night(newTripleFormatted);
		row++;
	}
	
	public static String[] findTriple(String element) {
		for(String[] triple: triplesOriginal) {
			if(triple[0].equals(element)) {
//				printTriples(triplesOriginal);
				return triple;
			}
		}
		return null;
	}
	
	public static List<String[]> shiftOne(List<String[]> list){
		List<String[]> newList = new ArrayList<>();
		Mark.purple(list);
		for(int i=1;i<list.size();i++) {
			String[] item = list.get(i);
			newList.add(item);
		}
		Mark.yellow(newList);
		newList.add(list.get(0));
		return newList;
	}
	
	public static void printFormattedTriples(){
		String toPrint = "\n";
		int row = 0;
		Boolean trya = true;
		while(trya) {
			for(String[] triple: triplesFormatted) {
				if(triple[0].startsWith(row+"")) {
					int indentLast = 0;
					for(String element: triple) {
						int indent = Integer.parseInt(element.split(placeHolder)[0].substring(1));
						element = element.split(placeHolder)[1];
						toPrint += printTimes("\t", (indent-indentLast)) + element + " ";
						indentLast = indent;
					}
					row++;
					toPrint+="\n";
				}
			}
			if(row==triplesFormatted.size()) {
				toPrint+="\n";
				trya = false;
			}
		}
		Mark.yellow(toPrint);
	}
	
	public static String printTimes(String pattern, int times) {
		String result = "";
		for(int i=0;i<times;i++) {
			result+=pattern;
		}
		return result;
	}

	static float length_all = 0;
	static float count_all = 0;
	static float count_can = 0;
	static int count_file = 0;
	static float length_all_infile = 0;
	static float count_all_infile = 0;
	static float count_can_infile = 0;
	static String good_instructions = instructionsFolder + "/instructions_good.txt";
	static String bad_instructions = instructionsFolder + "/instructions_bad.txt";
	static List<Integer> exceptions = new ArrayList<>();
	static FileWriter HTMLwriter;
	
	public static void testGetTrainingTriplesFromJSonFile(String fileName) {
		
		length_all_infile = 0;
		count_all_infile = 0;
		count_can_infile = 0;
		count_file ++;
		//JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try (FileReader reader = new FileReader(fileName)) {
            //Read JSON file
            JSONObject obj = (JSONObject) jsonParser.parse(reader);
            JSONArray arr = (JSONArray) obj.get("instructions");
            
            String videoName = (String) obj.get("video_name");
            videoName = videoName.replace("table", "");
            
            FileWriter good_writer = new FileWriter(good_instructions, true);
            FileWriter bad_writer = new FileWriter(bad_instructions, true);

            HTMLwriter.write("<h1>Index: "+(int)count_file+"&nbsp;&nbsp;&nbsp; File: " + fileName.replace("/Users/z/git/genesis/students/zhutianYang/instruction_data/", "") + "&nbsp;&nbsp;&nbsp; Video: "+videoName+"</h1>");
            for (int i = 0; i < arr.size(); i++) {
            	String instruction = (String) arr.get(i);
            	int length = instruction.length() - instruction.replace(" ", "").length() + 1;
            	length_all += length;
            	count_all ++;
            	length_all_infile += length;
            	count_all_infile ++;
            	
            	String toPrint = fileName + "\n";
            	
            	instruction = instruction.replace("centere", "");
            	String original = instruction;
            	instruction = instruction.replace(", its", ", with its").replace("table cloth", "tablecloth");
            	
            	if(Z.isTranslatable(instruction)) {
            		Mark.night(instruction);
            		count_can += 1;
            		count_can_infile += 1;
            		String tree = Z.getPrettyTree(instruction);
            		
            		toPrint += (int)count_can + "   " + instruction + "\n";
            		toPrint +=  tree + "\n\n\n";
            		good_writer.write(toPrint);
            		
            		Sequence sequenceFromStart = Start.getStart().processForTestor(instruction);
            		String rawText = Start.getStart().getProcessedSentence();
            		List<String[]> triples = selectTriples(rawText);

            		String printTriple = "";
            		for(String[] triple: triples) {
            			printTriple += (" ["+triple[0]+" "+triple[1]+" "+triple[2]+"]");
            		}
            		
            		HTMLwriter.write("<font color='green'><h3> # " + count_all_infile + "&nbsp;&nbsp;&nbsp; "+original+"</h3></font>\n");
            		HTMLwriter.write("<p>"+printTriple+"</p>\n");
            		HTMLwriter.write("<pre>"+tree+"</pre>\n");
            	} else {
            		Mark.yellow(instruction);
            		HTMLwriter.write("<font color='red'><h3> # " + count_all_infile + "&nbsp;&nbsp;&nbsp; "+original+"</h3></font>\n");
            		toPrint += (int)(count_all - count_can) + "   " + instruction + "\n\n";
            		bad_writer.write(toPrint);
            	}
            }
            good_writer.close();
            bad_writer.close();
            HTMLwriter.write("<br><br>");
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
	}
	
	public static void testGetTrainingTriplesFromJSonFileFolder(String folderName, String statsName) {
		
		// find local folder path
		Path currentRelativePath = Paths.get("");
		localPath = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + localPath);
		folderName = localPath + folderName;
		System.out.println("Current relative path is: " + folderName);
		
		// initiate recorder file
		good_instructions = localPath + good_instructions.replace(".txt", "_"+Z.getDate()+".txt");
		bad_instructions = localPath + bad_instructions.replace(".txt", "_"+Z.getDate()+".txt");
		File file = new File(good_instructions);
		file = new File(bad_instructions);
		Boolean writeStats = false;
		
		
		// write an HTML file
		try {
			HTMLwriter = new FileWriter(folderName + "/triplesNTrees.html", false);
			HTMLwriter.write("<!DOCTYPE html>\n<html>\n<body>");
			
			
			File folder = new File(folderName);
			for (final File fileEntry : folder.listFiles()) {
		    	String name = fileEntry.getName();
		    	if(name.contains(statsName)) {
		    		writeStats = true;
		    	}
		    	if(name.endsWith(".json")) {
		    		if(!exceptions.contains(Integer.parseInt(name.substring(5, name.indexOf(".json"))))) {
			    		Mark.mit(name);
			    		testGetTrainingTriplesFromJSonFile(folderName + "/" + name);
		    		}
		    	}
		    }
			
			HTMLwriter.write("\n</body>\n</html>");
			HTMLwriter.close();
		} catch (IOException e3) {
			e3.printStackTrace();
		}
		
		
		// write statistics for each file
		if(writeStats) {
			BufferedReader csvReader;
			try {
				FileWriter csvWriter = new FileWriter(statsName.replace(".csv", "_parsed.csv"));
				try {
					csvReader = new BufferedReader(new FileReader(folderName + "/" + statsName));
					String line = "";
					try {
						while ((line = csvReader.readLine()) != null) {  
						    List<String> data = new ArrayList<String>(Arrays.asList(line.split(",")));
						    Mark.mit(data.get(0));
						    testGetTrainingTriplesFromJSonFile(folderName + "/" + data.get(0));
						    data.add("" +count_can_infile);
						    data.add("" +count_can_infile/count_all_infile);
						    csvWriter.append(String.join(",", data));
						    csvWriter.append("\n");
						}
						csvReader.close();  
					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}
				csvWriter.flush();  
				csvWriter.close(); 
			} catch (IOException e1) {
				e1.printStackTrace();
			}  
		}
		
		String toPrint = "\nNumber of sentences: " + count_all +
				"\nNumber of sentences parsed: " + count_can +
				"\nAverage length of sentence: " + length_all/count_all +
				"\nPortion of translatable sentence: " + count_can/count_all;
		
		Mark.say(toPrint);
		FileWriter bad_writer;
		try {
			bad_writer = new FileWriter(bad_instructions, true);
			bad_writer.write(toPrint);
			bad_writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
    }
	
	static <T> T[] append(T[] arr, T element) {
	    final int N = arr.length;
	    arr = Arrays.copyOf(arr, N + 1);
	    arr[N] = element;
	    return arr;
	}
	
	public static void getType(Object obj) {
		Mark.say(obj.getClass().getSimpleName());
	}
	
	public static void main(String[] args) {
//		testGetTrainingTriples();
		
//		exceptions = Arrays.asList(19);
		testGetTrainingTriplesFromJSonFileFolder(instructionsFolder, "astats_200.csv");
	}
	
	public static void testGetTrainingTriples() {
		List<String> strings = new ArrayList<>();
//		strings.add("Put fork on the plate's right, with its blade pointing towards the plate.");
//		strings.add("put the folk slightly above the plate");
//		strings.add("I love to play with John");
//		strings.add("I ate and I prayed");
//		strings.add("What are the people in the background doing?");
//		strings.add("What website copyrighted the picture?");
		strings.add("place the soup spoon to the right of the knife");

		for(String string: strings) {
			Mark.say(string);
			getTrainingTriples(string);
//			Mark.say(Z.getPrettyTree(string));
		}
	}

}
