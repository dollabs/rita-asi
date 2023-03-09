package subsystems.blocksWorld.models;

import java.awt.*;
import java.util.*;

import javax.swing.JOptionPane;

import com.ascent.gui.frame.ABasicFrame;

import utils.Mark;



/*
 * Created on Sep 10, 2005
 * @author Patrick
 */

public class WorldModel extends Observable {

	private Vector<Block> blocks = new Vector();

	public Hand fastHand = new Hand("Fast hand", Color.RED, new Location(-10, 100));

	public Hand strongHand = new Hand("Strong hand", Color.BLUE, new Location(100, 100));

	public Table table = new Table();

	public Brick b1 = new Brick("B1", Color.BLUE, 10, 10, 0, 10);

	public Brick b2 = new Brick("B2", Color.GREEN, 10, 10, 10, 10);

	double tolerance = 0.1;

	private Goal root;

	ArrayList<Goal> roots;

	public ArrayList<Goal> getRoots() {
		if (roots == null) {
			roots = new ArrayList<>();
		}
		return roots;
	}

	public Goal getRoot() {
		return root;
	}

	public void setRoot(Goal root) {
		if (root != null) {
			this.root = root;
			getRoots().add(0, this.root);
		}
	}

	private static Color gray = new Color(125, 125, 125);

	public Brick b3 = new Brick("B3", Color.RED, 20, 20, 20, 10);

	public Brick b4 = new Brick("B4", gray, 10, 10, 40, 10);

	public Brick b5 = new Brick("B5", Color.BLUE, 10, 20, 50, 10);

	public Brick b6 = new Brick("B6", Color.GREEN, 20, 10, 60, 10);

	public Brick b7 = new Brick("B7", Color.YELLOW, 10, 10, 80, 10);

	public Brick b8 = new Brick("B8", gray, 10, 20, 90, 10);

	private int maximumArmAltitude = 80;

	private int framesPerMove = 10;

	private int fastStepDelay = 0;

	private int strongStepDelay = 0;

	private int goalDelay = 0;

	public WorldModel() {
		initializeBlocks();
	}

	public void resetDelays() {
		zeroDelays();
		// Wait a little to let existing threads settle
		try {
			Thread.sleep(500);
		}
		catch (Exception e) {
			Mark.say("Sleep threw exception in make new goal");
			e.printStackTrace();
		}

		fastStepDelay = 40;
		strongStepDelay = 120;
		goalDelay = 50;
	}

	public void zeroDelays() {
		fastStepDelay = 0;
		strongStepDelay = 0;
		goalDelay = 0;
	}

	private void addBlock(Block block) {
		blocks.add(block);
		// changed();
	}

	public void initializeBlocks() {
		addBlock(fastHand);
		addBlock(strongHand);
		addBlock(table);
		addBlock(b1);
		addBlock(b2);
		addBlock(b3);
		addBlock(b4);
		addBlock(b5);
		addBlock(b6);
		addBlock(b7);
		addBlock(b8);
		initializeOnTable();
	}
	
	public void initializeMartini() {
		blocks.clear();
		addBlock(fastHand);
		addBlock(strongHand);
		addBlock(table);
		addBlock(b1);
		addBlock(b2);
		// addBlock(b3);
		// addBlock(b4);
		addBlock(b5);
		// addBlock(b6);
		addBlock(b7);
		// addBlock(b8);
		initializeOnTable();
	}

	public void initializeOnTable() {
		zeroDelays();
		b1.setLocation(new Location(0, 10));
		b1.setSupport(table);
		b1.getSupported().clear();
		b2.setLocation(new Location(10, 10));
		b2.setSupport(table);
		b2.getSupported().clear();
		b3.setLocation(new Location(20, 10));
		b3.setSupport(table);
		b3.getSupported().clear();
		b4.setLocation(new Location(40, 10));
		b4.setSupport(table);
		b4.getSupported().clear();
		b5.setLocation(new Location(50, 10));
		b5.setSupport(table);
		b5.getSupported().clear();
		b6.setLocation(new Location(60, 10));
		b6.setSupport(table);
		b6.getSupported().clear();
		b7.setLocation(new Location(80, 10));
		b7.setSupport(table);
		b7.getSupported().clear();
		b8.setLocation(new Location(90, 10));
		b8.setSupport(table);
		b8.getSupported().clear();
		table.getSupported().clear();
		table.addSupported(b1);
		table.addSupported(b2);
		table.addSupported(b3);
		table.addSupported(b4);
		table.addSupported(b5);
		table.addSupported(b6);
		table.addSupported(b7);
		table.addSupported(b8);
		parkFastHand();
		parkStrongHand();
		setRoot(null);
		resetDelays();
		changed();
	}

	public void initializeInPile() {
		initializeOnTable();
		zeroDelays();
		putOn(b1, b3, null);
		putOn(b4, b5, null);
		putOn(b2, b6, null);
		putOn(b7, b2, null);
		putOn(b8, b6, null);
		parkFastHand();
		parkStrongHand();
		setRoot(null);
		resetDelays();
		changed();

	}

	public void initializeCellPhone() {
		initializeOnTable();
		zeroDelays();
		putOn(b2, b1, null);
		putOn(b7, b2, null);
		parkFastHand();
		parkStrongHand();
		setRoot(null);
		resetDelays();
		changed();

	}

	public void initializeElectricTorch() {
		initializeOnTable();
		zeroDelays();
		putOn(b2, b8, null);
		putOn(b7, b2, null);
		parkFastHand();
		parkStrongHand();
		setRoot(null);
		resetDelays();
		changed();

	}

	public void initializeForBoard() {
		initializeOnTable();
		zeroDelays();
		putOn(b4, b1, null);
		putOn(b7, b2, null);
		parkFastHand();
		parkStrongHand();
		setRoot(null);
		resetDelays();
		changed();

	}

	public Vector<Block> getBlocks() {
		return blocks;
	}

	// Real guts of this below

	public Goal putOn(Block mover, Brick target, Goal supergoal) {
		return putOn(fastHand, mover, target, supergoal);
	}

	public Goal putOver(Block mover, Brick target, Goal supergoal) {
		return putOver(fastHand, mover, target, supergoal);
	}

	public Goal putOnWithSpeed(Block mover, Brick target, Goal supergoal) {
		return putOn(fastHand, mover, target, supergoal);
	}

	public Goal putOnWithStrength(Block mover, Brick target, Goal supergoal) {
		return putOn(strongHand, mover, target, supergoal);
	}

	public Goal putOn(Hand hand, Block mover, Brick target, Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "put " + mover + " on " + target);
		if (supergoal == null) {
			setRoot(goal);
		}
		if (mover.equals(target)) {
			return goal;
		}
		if (target.equals(mover.getSupport())) {
			return goal;
		}

		// Note! Cleartop must be before find space! Otherwise cleartop may put stuff in found space, so two things go
		// in same hole!

		if (mover instanceof Brick) {
			// In this version, must be handled by problem solver!
			// clearTop((Brick) mover, goal);
		}
		// In this version, must be handled by problem solver!
		Location location = findLocation(mover, target);
		if (location != null) {
			grasp(hand, mover, goal);
			moveHand(hand, location, goal);
			mover.setSupport(target);
			ungrasp(hand, goal);
		}
		else {
			Mark.say("Unable to get a location for " + mover + ", on " + target);
		}
		return goal;
	}

	public Goal putOver(Hand hand, Block mover, Brick target, Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "put " + mover + " over " + target);
		if (supergoal == null) {
			setRoot(goal);
		}
		if (mover.equals(target)) {
			return goal;
		}
		if (target.equals(mover.getSupport())) {
			return goal;
		}

		// Note! Cleartop must be before find space! Otherwise cleartop may put stuff in found space, so two things go
		// in same hole!

		if (mover instanceof Brick) {
			// In this version, must be handled by problem solver!
			// clearTop((Brick) mover, goal);
		}
		// In this version, must be handled by problem solver!
		// Location location = findLocation(mover, target);
		Location location = findLocationOver(mover, target);
		if (location != null) {
			grasp(hand, mover, goal);
			moveHand(hand, location, goal);
			// mover.setSupport(target);
			ungrasp(hand, goal);
		}
		else {
			Mark.say("Unable to get a location for " + mover + ", on " + target);
		}
		return goal;
	}

	public Goal tilt(Block mover, Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "tilt " + mover);
		if (supergoal == null) {
			setRoot(goal);
		}
		// Note! Cleartop must be before find space! Otherwise cleartop may put stuff in found space, so two things go
		// in same hole!

		// Mark.say("Tilting");
		// Mark.say("Mover", mover);
		mover.getLocation().fill(0.75);
		step();
		pause(1);
		mover.getLocation().fill(0.5);
		step();
		pause(1);
		mover.getLocation().fill(0.25);
		// stop();
		pause(1);
		mover.getLocation().fill(0.01);
		step();
		pause(1);
		mover.getLocation().fill(0.0);
		step();
		// mover.getLocation().unfill();
		return goal;
	}
	
	private void stop() {
	int response = JOptionPane.showOptionDialog(ABasicFrame.getTheFrame(),

		        "Stopped", "Pause",

		        JOptionPane.DEFAULT_OPTION,

		        JOptionPane.PLAIN_MESSAGE,

		        null, null, null);
	
	}

	public static void pause(double t) {
		try {
			Thread.sleep((int) (t * 1000));
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	};

	public Location makeLocation(Block x, Brick t, Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "find location for " + x + " on " + t);
		Vector obstacles = t.getSupported();
		while (true) {
			Location location = findLocation(x, t);
			if (location != null) {
				return location;
			}
			Vector v = t.getSupported();
			System.out.println("Supported by " + t + ": " + v);
			if (v.size() == 0) {
				break;
			}
			Block obstacle = (Block) (v.get(0));
			getRidOf(obstacle, goal);
		}
		return null;
	}

	public Location findLocation(Block x, Brick t) {
		boolean debug = false;
		Vector obstacles = t.getSupported();
		// The possible locations start at the top left
		double trialX = t.getLocation().getX();
		double trialY = t.getLocation().getY() + t.getSize().getHeight();
		// Try all possible locations, but Stop if x overhangs in given position
		for (int i = 0; i + x.getSize().getWidth() <= t.getSize().getWidth(); ++i) {
			Location testLocation = new Location(trialX + i, trialY);
			// Check against all obstacles
			if (okLocation(x, testLocation, obstacles)) {
				return new Location(trialX + i + (x.getSize().getWidth() / 2), trialY + x.getSize().getHeight());
			}
		}
		Mark.say(debug, "Could not find a location for", x, "on", t);
		return null;
	}

	public Location findLocationOver(Block x, Brick t) {
		boolean debug = false;
		Vector obstacles = t.getSupported();
		// The possible locations start at the top left
		double trialX = t.getLocation().getX();
		double trialY = t.getLocation().getY() + t.getSize().getHeight();
		// Try all possible locations, but Stop if x overhangs in given position
		for (int i = 0; i + x.getSize().getWidth() <= t.getSize().getWidth(); ++i) {
			Location testLocation = new Location(trialX + i, trialY);
			// Check against all obstacles
			if (okLocation(x, testLocation, obstacles)) {
				return new Location(trialX + i + (x.getSize().getWidth() / 2), trialY + 1.2 * x.getSize().getHeight());
			}
		}
		Mark.say(debug, "Could not find a location for", x, "on", t);
		return null;
	}

	public Goal grasp(Block b, Goal supergoal) {
		return grasp(fastHand, b, supergoal);
	}

	public Goal grasp(Hand hand, Block b, Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "grasp " + b);

		Block holding = hand.getBlock();
		if (holding != null) {
			if (holding.equals(b)) {
				return goal;
			}
			else if (holding.getSupport() == null) {
				getRidOf(holding, goal);
			}
			else {
				ungrasp(hand, goal);
			}
		}
		// Should be handled by problem solver
		// if (b instanceof Brick && ((Brick) b).getSupported().size() > 0) {
		// clearTop((Brick) b, goal);
		// }
		moveHand(hand, b.getTopCenter(), goal);
		hand.setBlock(b);
		b.removeSupport();
		return goal;
	}

	public Goal ungrasp(Hand hand, Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "ungrasp " + fastHand.getBlock());
		ungrasp(hand);
		return goal;
	}

	public Goal ungrasp(Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "ungrasp " + fastHand.getBlock());
		ungrasp(fastHand);
		return goal;
	}

	public void ungrasp(Hand hand) {
		hand.setBlock(null);
	}

	public void parkFastHand() {
		// Mark.say("Moving fasthand from", fastHand.getLocation(), " to ", fastHand.getParkingLocation());
		moveHand(fastHand, fastHand.getParkingLocation());
	}

	public void parkStrongHand() {
		// Mark.say("Moving strong hand from", strongHand.getLocation(), " to ", strongHand.getParkingLocation());
		moveHand(strongHand, strongHand.getParkingLocation());
	}

	public Goal moveHand(Hand hand, Location p, Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "move hand");
		moveHand(hand, p);
		return goal;
	}

	public Goal moveHand(Location p, Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "move hand");
		moveHand(fastHand, p);
		return goal;
	}

	public Goal clearTop(Brick b, Goal supergoal) {
		Goal goal = makeNewGoal(supergoal, "clear the top of " + b);
		Vector obstacles = b.getSupported();
		for (int i = 0; i < obstacles.size(); ++i) {
			Block obstacle = (Block) (obstacles.get(i));
			getRidOf(obstacle, goal);
		}
		return goal;
	}

	public boolean hasClearTop(Brick b, Goal supergoal) {
		Vector obstacles = b.getSupported();
		if (obstacles.isEmpty()) {
			return true;
		}
		return false;
	}

	public Goal getRidOf(Block x, Goal supergoal) {
		System.out.println("Getting rid of " + x);
		Goal goal = makeNewGoal(supergoal, "get rid of " + x);
		putOn(x, table, goal);
		return goal;
	}


	public void moveHand(Hand hand, Location p) {
		Location oldHandPlace = hand.getLocation();
		double deltaX = p.getX();
		deltaX -= hand.getSize().getWidth() / 2;
		deltaX -= oldHandPlace.getX();
		double deltaY = p.getY();
		deltaY -= oldHandPlace.getY();

		// Mark.say("Delta hand", deltaX, deltaY);

		// If need to swing arm, raise it first

		double step = (maximumArmAltitude - oldHandPlace.getY()) / framesPerMove;

		if (deltaX != 0) {
			// Mark.say("Raising", step);
			for (int i = 0; i < framesPerMove; ++i) {
				oldHandPlace.translate(0, step);
				Block block = hand.getBlock();
				if (block != null) {
					Location oldBlockPlace = block.getLocation();
					oldBlockPlace.translate(0, step);
				}
				step(hand);
			}
		}

		// Now perfrom horizontal motion

		step = deltaX / framesPerMove;
		// Mark.say("Moving", step);
		for (int i = 0; i < framesPerMove; ++i) {
			oldHandPlace.translate(step, 0);
			Block block = hand.getBlock();
			if (block != null) {
				Location oldBlockPlace = block.getLocation();
				oldBlockPlace.translate(step, 0);
			}
			step(hand);
		}

		// And finally, move vertically

		step = (p.getY() - oldHandPlace.getY()) / framesPerMove;
		// Mark.say("Lowering", step);
		for (int i = 0; i < framesPerMove; ++i) {
			oldHandPlace.translate(0, step);
			Block block = hand.getBlock();
			if (block != null) {
				Location oldBlockPlace = block.getLocation();
				oldBlockPlace.translate(0, step);
			}
			step(hand);
		}
	}

	private Goal makeNewGoal(Goal supergoal, String description) {
		Goal goal = new Goal(supergoal, description);
		if (goalDelay > 0) {
			try {
				Thread.sleep(goalDelay);
			}
			catch (Exception e) {
				Mark.say("Sleep threw exception in make new goal");
				e.printStackTrace();
			}
			changed();
		}
		return goal;
	}

	/*
	 * Moves the bottom center of the hand to a specified place.
	 */
	class MoveHandThread extends Thread {
		Location p;

		public MoveHandThread(Location p) {
			this.p = p;
		}

		public void run() {
			Location oldHandPlace = fastHand.getLocation();
			double deltaX = p.getX();
			deltaX -= fastHand.getSize().getWidth() / 2;
			deltaX -= oldHandPlace.getX();
			double deltaY = p.getY();
			deltaY -= oldHandPlace.getY();

			// If need to swing arm, raise it first

			double step = (maximumArmAltitude - oldHandPlace.getY()) / framesPerMove;
			if (deltaX != 0) {
				for (int i = 0; i < framesPerMove; ++i) {
					oldHandPlace.translate(0, step);
					Block block = fastHand.getBlock();
					if (block != null) {
						Location oldBlockPlace = block.getLocation();
						oldBlockPlace.translate(0, step);
					}
					step();
				}
			}

			// Now perfrom horizontal motion

			step = deltaX / framesPerMove;
			for (int i = 0; i < framesPerMove; ++i) {
				oldHandPlace.translate(step, 0);
				Block block = fastHand.getBlock();
				if (block != null) {
					Location oldBlockPlace = block.getLocation();
					oldBlockPlace.translate(step, 0);
				}
				step();
			}

			// And finally, move vertically

			step = (p.getY() - oldHandPlace.getY()) / framesPerMove;
			for (int i = 0; i < framesPerMove; ++i) {
				oldHandPlace.translate(0, step);
				Block block = fastHand.getBlock();
				if (block != null) {
					Location oldBlockPlace = block.getLocation();
					oldBlockPlace.translate(0, step);
				}
				step();
			}
		}
	}

	private boolean okLocation(Block x, Location testLocation, Vector obstacles) {
		for (int j = 0; j < obstacles.size(); ++j) {
			Block obstacle = (Block) (obstacles.get(j));
			if (intersects(testLocation, x.getSize(), obstacle.getLocation(), obstacle.getSize())) {
				return false;
			}
		}

		return true;
	}

	private boolean intersects(Location thisLocation, Dimension thisSize, Location thatLocation, Dimension thatSize) {
		if (thisLocation.getX() > thatLocation.getX() + thatSize.getWidth() - tolerance
		        || thisLocation.getX() + thisSize.getWidth() < thatLocation.getX() + tolerance
		        || thisLocation.getY() > thatLocation.getY() + thatSize.getHeight() - tolerance
		        || thisLocation.getY() + thisSize.getHeight() < thatLocation.getY() + tolerance) {
			return false;
		}
		return true;
	}

	public void step() {
		changed();
		try {
			Thread.sleep(fastStepDelay);
		}
		catch (Exception e) {
			Mark.say("Sleep threw exception in step");
			e.printStackTrace();
		}

	}

	public void step(Hand hand) {
		changed();
		try {
			if (hand == fastHand) {
				// Mark.say("Steping", hand, fastStepDelay);
				Thread.sleep(fastStepDelay);
			}
			else if (hand == strongHand) {
				// Mark.say("Steping", hand, strongStepDelay);
				Thread.sleep(strongStepDelay);
			}
		}
		catch (Exception e) {
			Mark.say("Sleep threw exception in step");
			e.printStackTrace();
		}

	}

	public void changed() {
		setChanged();
		notifyObservers();
	}

	public int getStepSize() {
		return fastStepDelay;
	}

	public void setStepSize(int stepSize) {
		this.fastStepDelay = stepSize;
	}

	public String answerQuestion(String question) {
		if (getRoots().isEmpty()) {
			return "?";
		}

		String result = "Did I do that?";

		for (Goal root : getRoots()) {

			Goal goal = root.find(question);

			if (goal == null) {
				continue;
			}

			Goal supergoal = goal.getSupergoal();
			Vector subgoals = goal.getSubgoals();
			if (question.toLowerCase().indexOf("why") >= 0) {
				if (supergoal == null) {
					result = "Because you told me to";
				}
				else {
					result = "Because I wanted to " + supergoal;
				}
			}
			else if (question.toLowerCase().indexOf("how") >= 0) {
				if (subgoals.size() == 0) {
					result = "I just did it";
				}
				else {
					result = "Well, I";
					for (int i = 0; i < subgoals.size(); ++i) {
						Goal subgoal = (Goal) (subgoals.get(i));
						result += "\n  " + subgoal.toString();
					}
				}
			}
			else if (question.toLowerCase().indexOf("when") >= 0) {
				result = "When I ";
				result += root.toString();
			}
			break;
		}
		return result;
	}

}
