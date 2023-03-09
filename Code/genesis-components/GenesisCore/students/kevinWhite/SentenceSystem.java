package kevinWhite;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author kwhite17
 * @author minhtuev
 *
 * This class applies all the test data to the lattice and feeds them into the learners.
 * 
 */
public class SentenceSystem {

	ArrayList<String> sentences = new ArrayList<String>();
	ConceptManager conceptManager;

	public SentenceSystem() {
		this.conceptManager = new ConceptManager();
		getSwimmers();
		//getSpeakers();
		//getLookers();
		//getCrawlers();
		getFlyers();
		//getGrabbers();
		getWalkers();
		//getSwayers();
		getThrowers();
		//getSpinners();
	}

	public void getSwimmers() {
		sentences.add("Humans can swim.");
		sentences.add("Lions can swim.");
		sentences.add("Bats can't swim.");
		sentences.add("Ducks can swim.");
		sentences.add("Robots can't swim.");
		sentences.add("Boats can swim.");
		sentences.add("Helicopters can't swim.");
		sentences.add("Spaceships can swim.");
		sentences.add("Ants can't swim.");
		sentences.add("Plants can't swim.");
	}
	
	public void getSpeakers(){
		sentences.add("Humans can speak.");
		sentences.add("Insects can't speak.");
		sentences.add("Plants can't speak.");
		sentences.add("Whales can speak.");
		sentences.add("Lizards can speak.");
		sentences.add("Robots can speak.");
		sentences.add("Drones can't speak.");
		sentences.add("Airplanes can't speak.");
		sentences.add("Computers can speak.");
		sentences.add("Boats can't speak.");
	}
	
	public void getLookers(){
		sentences.add("Humans can look.");
		sentences.add("Birds can look.");
		sentences.add("Insects can look.");
		sentences.add("Reptiles can look.");
		sentences.add("Mammals can look.");
		sentences.add("Boats can't look.");
		sentences.add("Drones can look.");
		sentences.add("Airplanes can't look");
		sentences.add("Cars can't look.");
		sentences.add("Robots can look.");
	}
	
	public void getCrawlers(){
		sentences.add("Humans can crawl.");
		sentences.add("Elephants can't crawl.");
		sentences.add("Lions can crawl.");
		sentences.add("Ants can crawl.");
		sentences.add("Snakes can't crawl.");
		sentences.add("Tanks can crawl.");
		sentences.add("Boats can't crawl.");
		sentences.add("Cars can't crawl.");
		sentences.add("Airplanes can't crawl.");
		sentences.add("Robots can crawl.");
	}

	public void getFlyers() {
		sentences.add("Humans can't fly.");
		sentences.add("Centipedes can't fly.");
		sentences.add("Chickens can't fly.");
		sentences.add("Hawks can fly.");
		sentences.add("Butterflies can fly.");
		sentences.add("Boats can't fly.");
		sentences.add("Cars can't fly.");
		sentences.add("Airplanes can fly.");
		sentences.add("Drones can fly.");
		sentences.add("Phones can't fly.");
	}
	
	private void getGrabbers() {
		sentences.add("Humans can grab.");
		sentences.add("Cats can grab.");
		sentences.add("Dogs can grab.");
		sentences.add("Worms can't grab.");
		sentences.add("Fish can grab.");
		sentences.add("Cars can't grab.");
		sentences.add("Robots can grab.");
		sentences.add("Drones can grab.");
		sentences.add("Airplanes can't grab.");
		sentences.add("Boats can't grab.");
	}
	
	private void getWalkers() {
		sentences.add("Snakes can't walk.");
		sentences.add("Humans can walk.");
		sentences.add("Lions can walk.");
		sentences.add("Worms can't walk.");
		sentences.add("Insects can walk.");
		sentences.add("Robots can walk.");
		sentences.add("Boats can't walk.");
		sentences.add("Cars can't walk.");
		sentences.add("Airplanes can't walk.");
		sentences.add("Slugs can't walk.");
	}
	
	private void getSwayers() {
		sentences.add("Humans can sway.");
		sentences.add("Birds can sway.");
		sentences.add("Ants can't sway.");
		sentences.add("Butterflies can sway.");
		sentences.add("Scorpions can't sway.");
		sentences.add("Boats can sway.");
		sentences.add("Airplanes can sway.");
		sentences.add("Cars can sway.");
		sentences.add("Poles can sway.");
		sentences.add("Boxes can't sway.");
	}
	
	private void getThrowers() {
		sentences.add("Humans can throw a ball.");
		sentences.add("Insects can throw a ball."); 
		sentences.add("Fish can't throw a ball.");
		sentences.add("Dogs can throw a ball.");
		sentences.add("Snakes can't throw a ball.");
		sentences.add("Robots can throw a ball.");
		sentences.add("Airplanes can't throw a ball.");
		sentences.add("Boats can't throw a ball.");
		sentences.add("A bulldozer can throw a ball.");
		sentences.add("Catapults can throw a ball.");
	}
	
	private void getSpinners() {
		sentences.add("Humans can spin.");
		sentences.add("Snakes can't spin.");
		sentences.add("Dogs can spin.");
		sentences.add("Plants can't spin.");
		sentences.add("Butterflies can't spin.");
		sentences.add("Airplanes can spin.");
		sentences.add("Cars can spin.");
		sentences.add("Robots can spin.");
		sentences.add("Balls can spin.");
		sentences.add("Blocks can't spin.");
	}
	
	public ArrayList<String> getSentences() {
		return sentences;
	}

	public static void main(String[] args) {
		SentenceSystem sentenceSystem = new SentenceSystem();
		AutomatedLearner learner = new AutomatedLearner(sentenceSystem.getSentences(),false);
//		AutomatedLearner learner = new AutomatedLearner(sentenceSystem.getSentences(), true);
		
		for (FasterLLConcept flc: learner.getConceptList()){
			sentenceSystem.conceptManager.addConcept(flc);
		}
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		// Continuously input sentences from the command line and feed into the lattice.
		try {
			String line = "";
			while (!line.equals("exit"))
			{
				System.out.print("Enter a sentence or type exit to terminate:");
				line = bufferedReader.readLine();
				HashMap<String,Object> sentData = FasterLLConcept.parseSimpleSentence(line);
				FasterLLConcept verbConcept = sentenceSystem.conceptManager.getConcept((String) sentData.get("verb"));
				System.out.println(verbConcept.interpretSimpleSentence(sentData));
			}
		} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}