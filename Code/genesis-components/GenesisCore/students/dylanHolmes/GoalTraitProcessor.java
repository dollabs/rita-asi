package dylanHolmes;

import gui.TabbedTextViewer;

import java.util.ArrayList;

import matchers.StandardMatcher;
import storyProcessor.ConceptTranslator;
import storyProcessor.StoryProcessor;
import utils.Html;
import utils.Mark;
import connections.*;
import connections.signals.BetterSignal;
import constants.Markers;

/**
 * A local processor class that just receives a complete story description, takes apart the object to fetch various
 * parts of the complete story description, and prints them so I can see what is in there.
 */
public class GoalTraitProcessor extends AbstractWiredBox {

	public static final String INPUT_GOALS = "my input port";

	public static final String OUTPUT_DISCOVERED_TRAITS = "my output port";

	public static final String HTML = "HTML port"; // send output to the Results panel
	
	
	public static final String GOALS_TAB = "Goal-directed behavior";
	
	/**
	 * The constructor places the processSignal signal processor on two input ports for illustration. Only the
	 * StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT is wired to in LocalGenesis.
	 */
	public GoalTraitProcessor() {
		this.setName("Local story processor");
		// Example of default port connection
		Connections.getPorts(this).addSignalProcessor("processSignal");
		// Example of named port connection
		Connections.getPorts(this).addSignalProcessor(INPUT_GOALS, "processSignal");
	}

	
	public void sendHTML(String s) {
		BetterSignal message = new BetterSignal(GOALS_TAB, s);
		Connections.getPorts(this).transmit(HTML, message);
	}
	/**
	 * I have to make all signal processors void methods of one argument, which must be of the Object class, so there
	 * will be a bit of casting.
	 */
	public void processSignal(Object signal) {
		// Should always check to be sure my input is in the expected form and ignore it if not. A BetterSignal is just
		// a convenient container for multiple objects that allows easy extraction of objects without further casting.
		if (signal instanceof BetterSignal) {
			Mark.say("Goal trait processor loud and clear");
			
			
			//BetterSignal ss = new BetterSignal(Markers.ANSWERS_TAB, TabbedTextViewer.CLEAR);
			//Connections.getPorts(this).transmit(HTML, ss);

			//this.sendHTML("<h1>This is another test</h1>");
			
			//Connections.getPorts(this).transmit(HTML, message);
			
			
			//Connections.getPorts(this).transmit(HTML, message);
			
			BetterSignal s = (BetterSignal) signal;
			ArrayList<Goal> goalCatalog = s.get(0, ArrayList.class);
			ArrayList<Goal> matchedGoals = s.get(1, ArrayList.class);
			
			for(Goal g : matchedGoals){
				//Mark.say("match: "+g.name);
			}
			
			// ways of achieving the same goal, aka coterminals
			ArrayList<ArrayList<Goal>> alternatives = new ArrayList<ArrayList<Goal>>(); 
			
			for(Goal g : matchedGoals) {
				ArrayList<Goal> c = new ArrayList<Goal>();
				c.add(g);
				for(Goal h : goalCatalog) {
					// TODO : select only for goals with satisfied prerequisites
					// after all, goals with unsatisfied prereqs are less plausible
					//Mark.say(g.end.toString());
					//Mark.say(h.end.toString());
					//Mark.say(StandardMatcher.getBasicMatcher().match(g.end.getSubject(), h.end));
					if(g.name != h.name && StandardMatcher.getBasicMatcher().match(g.end.getSubject(), h.end) != null) {
						c.add(h);
					}
				}
				if(c.size() > 1) {alternatives.add(c);}
			}
			
			//Mark.say(alternatives.size());
			for(ArrayList<Goal> alts : alternatives) {
				//Mark.say(alts.get(0).name);
				if(alts.get(0).name == "theft" && Goal.containsNamedGoal(alts, "solicit")) {
					sendHTML("Patrick is inconsiderate because he harmed Boris by stealing when he could have accomplished the same goal by asking.");
					return;
				}
				if(alts.get(0).name == "solicit" && Goal.containsNamedGoal(alts, "theft")) {
					sendHTML("Patrick is civil because he avoided harming Boris; he achieved his goal by asking instead of stealing.");
					return;
				}
			}
			if(Goal.containsNamedGoal(matchedGoals, "theft")) {
					sendHTML("Patrick harmed Boris by stealing, but I don't know how else he might have accomplished his goal.");
			}
			
			
		}
	}
}