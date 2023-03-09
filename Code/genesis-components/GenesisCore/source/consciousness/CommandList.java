/**
 * 
 */
package consciousness;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import connections.*;
import connections.signals.BetterSignal;
import utils.Mark;

/**
 * @author phw
 *
 */
public class CommandList extends AbstractWiredBox {

	public static final String ADD_REQUEST = "Add request";
	public static final String ADD_SUCCESS = "Add success";
	public static final String ADD_FAILURE = "Add failure";
	public static final String CLEAR = "Clear";
	public static final Object DONE = "Done";
	
	public static String CommandsFile = "students/zhutianYang/commands.txt";
	public static PrintWriter out;

	private List<String> commands;

	boolean initialized = false;
	public static final String FROM_PROBLEM_SOLVER = "get commands from problem solver";
	public static final String TO_RECIPE_EXPERT = "connect to Z's interface";

	public CommandList(String name) {
		// No initialization here.
		super(name);
		Connections.getPorts(this).addSignalProcessor(FROM_PROBLEM_SOLVER, this::process);
	}

	public void process(Object input) {
		// Initialize here.
		initialize();
		if (input instanceof BetterSignal) {
			BetterSignal bs = (BetterSignal) input;
			String instruction = bs.get(0, String.class);
			if (instruction.equals(CommandList.ADD_REQUEST)) {
//			        || instruction.equals(CommandList.ADD_SUCCESS)
//			        || instruction.equals(CommandList.ADD_FAILURE)) {
				String command = bs.get(1, String.class);
				Mark.say("Adding command", command);
				commands.add(command);
			}
			else if (instruction.equals(CommandList.CLEAR)) {
				Mark.say("Clearing commands");
				commands.clear();
			}
			else if (instruction.equals(CommandList.DONE)) {
				commands.stream().forEachOrdered(s -> {
					Mark.say("Element:", s);
				});
				out.print(commands);
				out.close();
				Connections.getPorts(this).transmit(TO_RECIPE_EXPERT, new BetterSignal(commands));
			}
		}
	}

	private void initialize() {
		// Initialize just one via initialized variable which is
		// set to true first time executed.
		if (initialized) {
//			Mark.say("Already initialized!");
		}
		else {
			initialized = true;
			commands = new ArrayList<>();
			try {
				out = new PrintWriter(CommandsFile);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			Mark.say("Initialize now!");
		}
	}

}
