package gui;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.print.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;

import connections.*;
import connections.signals.BetterSignal;
import consciousness.ProblemSolver;
import constants.Markers;
import constants.Switch;
import frames.entities.Entity;
import frames.entities.Sequence;
import generator.Generator;
import storyProcessor.ConceptDescription;
import translator.Translator;
import utils.*;
import utils.tools.Predicates;

/*
 * Created on Dec 14, 2013
 * @author phw
 */

public class ElaborationView extends JPanel implements Printable, WiredBox, MouseListener, MouseMotionListener {

	public static boolean debug = false;
	
	// When true, exposes a problem that needs to be fixed some day
	boolean todo = false;

	private int boxWidth = 110;

	private int boxHeight = 80;

	private int boxHPadding = 20;

	private int boxVPadding = 40;

	private int leftAndRightOffset = 20;

	private int topAndBottomOffset = 50;

	private int leftShift = 0;

	private int upShift = 0;

	private double magnification = 1;

	private ArrayList<Box> connectedBoxList;

	private ArrayList<Box> unconnectedBoxList;

	private ArrayList<Connection> connectionList;

	private HashMap<String, Box> boxSet = new HashMap<String, Box>();

	private HashMap<String, Connection> connectionSet = new HashMap<String, Connection>();

	private HashMap<String, String> sentenceCache = new HashMap<>();

	private final Stroke basicStroke = new BasicStroke(2.0f);

	private final Stroke boldStroke = new BasicStroke(3.0f);

	private final Stroke dottedStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 3 }, 0.0f);

	private final Stroke boldDottedStroke = new BasicStroke(3.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] { 6 }, 0.0f);

	private float idealRatio = 2f;

	private int rowCount;

	private int rowWidth;

	private int rowHeight;

	public static final String STORY = "story port";

	public static final String CONCEPT = "concept port";

	public static final String INSPECTOR = "inspection port";

	public static final String SUMMARY = "summary";

	private boolean alwaysShowAllElements = false;

	public static int PLAIN = 0, FANCY = 1;

	private int mode = FANCY;
	
	private Boolean showConnectionTypeInElaborationGraph = false;
	
	
	public ElaborationView() {
		addMouseListener(this);
		addMouseMotionListener(this);

		initializeColors(Switch.useColorInElaborationGraph.isSelected());
		showConnectionTypeInElaborationGraph = Switch.showConnectionTypeInElaborationGraph.isSelected();

		Connections.getPorts(this).addSignalProcessor(STORY, this::processStory);

		Connections.getPorts(this).addSignalProcessor(CONCEPT, this::processConcept);

		Connections.getPorts(this).addSignalProcessor(INSPECTOR, this::processFragment);

		Connections.getPorts(this).addSignalProcessor(SUMMARY, this::processSummary);

		Connections.getPorts(this).addSignalProcessor(Port.RESET, this::clear);

		Switch.useColorInElaborationGraph.addItemListener(new ColorMonitor());
		Switch.showConnectionTypeInElaborationGraph.addItemListener(new ShowConnectionTypeMonitor());

		setFocusable(true);

		addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				requestFocusInWindow();
			}
		});

		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.isControlDown() && e.getKeyCode() == 80) {
					printMe();
				}
			}
		});
	}

	private class ColorMonitor implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getSource() == Switch.useColorInElaborationGraph) {
				initializeColors(Switch.useColorInElaborationGraph.isSelected());
			}
		}
	}
	

	private class ShowConnectionTypeMonitor implements ItemListener {
		@Override
		public void itemStateChanged(ItemEvent e) {
			if (e.getSource() == Switch.showConnectionTypeInElaborationGraph) {
				initializeColors(Switch.showConnectionTypeInElaborationGraph.isSelected());
			}
		}
	}

	private void addUnconnectedBoxes() {
		int maxWidth = Math.max(5, maxX());
		int maxHeight = maxY();
		int row = maxHeight + 2;
		int column = 0;
		for (Box box : getUnconnectedBoxList()) {
			if (column > maxWidth) {
				column = 0;
				++row;
			}
			box.setY(row);
			box.setX(column++);
			addBox(box);
		}
	}

	public void processFragment(Object o) {
		if (o instanceof ConceptDescription) {
			ConceptDescription rd = (ConceptDescription) o;
			List<Entity> fragment = rd.getStoryElementsInvolved().getElements();
			// In this case, there is no story because, evidently, a pushed button has be unpushed, that is no concept
			// is to be displayed
			if (rd.getStory() == null) {
				return;
			}
			List<Entity> story = rd.getStory().getElements();
			List<Entity> causes = new ArrayList<>();
			Sequence result = new Sequence();
			for (Entity e : story) {
				if (!Predicates.isCause(e)) {
					// if (Predicates.contained(e, fragment)) {
					if (fragment.contains(e)) {
						result.addElement(e);
					}
				}
				else {
					// if (Predicates.contained(e.getObject(), fragment)) {
					if (fragment.contains(e.getObject())) {
						for (Entity antecedent : e.getSubject().getElements()) {
							// if (Predicates.contained(antecedent, fragment)) {
							if (fragment.contains(antecedent)) {
								result.addElement(e);
								break;
							}
						}
					}
				}
			}
			processStory(result);
		}
		else if (o instanceof BetterSignal) {
			BetterSignal bs = (BetterSignal) o;
			if (bs.size() == 2) {
				Set keepers = bs.get(0, Set.class);
				Sequence story = bs.get(1, Sequence.class);
				Sequence result = new Sequence();
				for (Entity e : story.getElements()) {
					// if (Predicates.contained(e, keepers)) {
					if (keepers.contains(e)) {
						result.addElement((Entity) e);
					}
				}
				processStory(result);
			}
			if (bs.size() == 1) {
				Sequence story = bs.get(0, Sequence.class);
				Sequence result = new Sequence();
				for (Entity e : story.getElements()) {
					result.addElement(e);
				}
				processStory(result);
			}
		}
	}

	// }

	public void clear(Object o) {
		if (o == Markers.RESET) {
			// Mark.say("Resetting");
			sentenceCache.clear();
			processStory(o);
		}
	}

	public void processStory(Object o) {
		// Start over
		getConnectedBoxList().clear();
		getUnconnectedBoxList().clear();
		getConnectionList().clear();
		boxSet.clear();
		connectionSet.clear();
		offsetX = 0;
		offsetY = 0;
		magnification = 1;
		try {
			if (o instanceof Sequence) {
				Sequence sequence = (Sequence) o;
				Mark.say(debug, "Received story with", sequence.getElements().size());
				HashSet<Entity> connectedEntities = new HashSet<Entity>();
				if (mode == FANCY) {
					for (Entity element : sequence.getElements()) {
						processEntity(element, connectedEntities);
					}
					for (Entity element : sequence.getElements()) {
						if (!isCausalConnection(element) && !connectedEntities.contains(element)) {
							getUnconnectedBoxList().add(getBox(element));
						}
					}
				}
				else if (mode == PLAIN) {
					for (Entity element : sequence.getElements()) {
						getUnconnectedBoxList().add(getBox(element));
					}
				}
			}
			if (mode == FANCY) {
				compress();
			}
			else if (mode == PLAIN) {
				realign();
			}
			if (Switch.showDisconnectedSwitch.isSelected()) {
				addUnconnectedBoxes();
			}
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		repaint();
	}

	public void processConcept(Object object) {
		if ((object instanceof ConceptDescription)) {
			ConceptDescription conceptDescription = (ConceptDescription) object;
			// First clear
			ArrayList<Box> boxes = getConnectedBoxList();
			for (Box b : boxes) {
				b.setTemporaryColor(null);
			}
			// Now, color
			boxes = new ArrayList<Box>();
			List<Entity> elements = conceptDescription.getStoryElementsInvolved().getElements();
			elements = expandCauses(elements);
			// Mark.say("Element count", elements.size());
			for (Entity t : elements) {
				Box box = getBox(t);
				if (box != null) {
					boxes.add(box);
					box.setTemporaryColor(conceptBox);
				}
				else {
					Mark.say("Found NO box for", t.asStringWithIndexes());
				}
			}
		}
		repaint();
	}

	public void processSummary(Object object) {
		if (object instanceof BetterSignal) {
			BetterSignal bs = (BetterSignal) object;
			Set<Entity> entitySet = bs.get(0, Set.class);
			// First clear
			ArrayList<Box> boxes = getConnectedBoxList();
			for (Box b : boxes) {
				b.setTemporaryColor(null);
			}
			// Now, color
			boxes = new ArrayList<Box>();
			List<Entity> elements = new ArrayList<Entity>();
			elements.addAll(entitySet);

			elements = expandCauses(elements);
			for (Entity t : elements) {
				Box box = getBox(t);
				if (box != null) {
					boxes.add(box);
					box.setTemporaryColor(conceptBox);
				}
				else {
					Mark.say("Found NO box for", t.asStringWithIndexes());
				}
			}
		}
		else {
			Mark.say("Ready to work on processSummary");
		}
		repaint();
	}

	private List<Entity> expandCauses(List<Entity> elements) {
		ArrayList<Entity> result = new ArrayList<Entity>();
		for (Entity e : elements) {
			// Special hack for causes
			// Had to introduce conjunction part because "help" isa "cause".
			if (e.isA(Markers.CAUSE_MARKER) && e.getSubject().isA(Markers.CONJUNCTION)) {
				Vector<Entity> antecedents = e.getSubject().getElements();
				Entity consequent = e.getObject();
				for (Entity a : antecedents) {
					result.add(a);
				}
				result.add(consequent);
			}
			else {
				result.add(e);
			}
		}
		return result;
	}

	private Entity translate(String string) throws Exception {
		Entity s = Translator.getTranslator().translate(string);
		if (s.sequenceP() && s.getElements().size() == 1) {
			return s.getElement(0);
		}
		Mark.err("Translation produces > 1 elements from", string);
		return null;
	}

	String hash(Entity e) {
		// Mark.say("Hash:", e.asStringWithIndexes());
		return e.asStringWithIndexes();
	}

	private void processEntity(Entity entity, HashSet<Entity> connectedEntities) {
		try {
			if (isCausalConnection(entity)) {
				int order = -1;
				Connection connection = connectionSet.get(hash(entity));
				Box consequentBox = null;
				if (connection == null) {
					Entity consequent = entity.getObject();
					TreeSet<Box> antecedentBoxes = new TreeSet<Box>();

					// Mark.say("\n>>> Initial value", order);

					for (Entity antecedent : entity.getSubject().getElements()) {
						Box antecedentBox = getBox(antecedent);
						antecedentBoxes.add(addBox(antecedentBox));
						connectedEntities.add(antecedent);

						// Revised 5 Feb 2016 to do nice coloring of problem solving steps
						// Needed because some antecedents will not show up as consequents
						if (entity.isA(Markers.MEANS)) {
							antecedentBox.setOrder(++order);
							antecedentBox.setColor(meansBox);
							colorProblemSoverBox(antecedent, antecedentBox);
						}

					}
					consequentBox = addBox(getBox(consequent));
					
					// Mark.say("\n>>> Consequent", consequentBox.text);
					// for (Box a : antecedentBoxes) {
					// Mark.say("Antecedent", a.getOrder(), a.text);
					// }

					connectedEntities.add(consequent);
					for (Box antecedentBox : antecedentBoxes) {
						connection = connect(antecedentBox, consequentBox);
						if (entity.isA(Markers.PREDICTION_RULE)) {
							Mark.say(debug, "Deduction", entity);
							consequentBox.setColor(deductionBox);
							connection.setColor(blackBox);
							connection.setConnectionType(Markers.PREDICTION_RULE);
						}
						else if (entity.isA(Markers.PRESUMPTION_RULE)) {
							Mark.say(debug, "Presumption", entity);
							antecedentBox.setColor(presumptionBox);
							connection.setColor(presumptionBoxWire);
							connection.setConnectionType(Markers.PRESUMPTION_RULE);
							// connection.setDotted(true);
						}
						else if (entity.isA(Markers.ABDUCTION_RULE)) {
							Mark.say(debug, "Abduction");
							antecedentBox.setColor(abductionBox);
							connection.setColor(abductionBoxWire);
							connection.setConnectionType(Markers.ABDUCTION_RULE);
							// connection.setDotted(true);
						}
						else if (entity.isA(Markers.PROXIMITY_RULE)) {
							Mark.say(debug, "Proximity");
							consequentBox.setColor(proximityBox);
							connection.setColor(proximityBoxWire);
							connection.setConnectionType(Markers.PROXIMITY_RULE);
							// connection.setDotted(true);
						}
						else if (entity.hasProperty(Markers.GOAL_ANALYSIS)) {
							Mark.say(debug, "Goal analysis");
						    connection.setColor(goalWire);
							connection.setConnectionType(Markers.GOAL_ANALYSIS);
						}
						// Must come after subtypes
						else if (entity.isA(Markers.EXPLANATION_RULE)) {
							Mark.say(debug, "Explanation");
						    consequentBox.setColor(explanationBox);
						    connection.setColor(explanationBoxWire);
							connection.setConnectionType(Markers.EXPLANATION_RULE);
						    // connection.setDotted(true);
						}
						else if (entity.isA(Markers.ENABLER_RULE)) {
							Mark.say(debug, "Enabler");
							antecedentBox.setColor(enablerBox);
							connection.setColor(enablerBoxWire);
							connection.setConnectionType(Markers.ENABLER_RULE);
							// connection.setDotted(true);
						}
						else if (entity.isA(Markers.UNKNOWABLE_ENTAIL_RULE)) {
							Mark.say(debug, "Unknowable leads to");
							consequentBox.setColor(unknowableBox);
							connection.setColor(unknowableBoxWire);
							connection.setConnectionType(Markers.UNKNOWABLE_ENTAIL_RULE);
							// connection.setDotted(true);
						}
						else if (entity.isA(Markers.ENTAIL_RULE)) {
							Mark.say(debug, "Leads to");
							consequentBox.setColor(leadsToBox);
							connection.setColor(leadsToBoxWire);
							connection.setConnectionType(Markers.ENTAIL_RULE);
							// connection.setDotted(true);
						}
						// Revised 5 Feb 2016 to do nice coloring of problem solving steps
						else if (entity.isA(Markers.MEANS)) {
							Mark.say(debug, "Means");
							connection.setColor(meansBoxWire);
							connection.setConnectionType(Markers.MEANS);
							colorProblemSoverBox(consequent, consequentBox);
						}
						if (entity.hasProperty(Markers.CONCEPTNET_JUSTIFICATION)) {
							connection.setConnectionType(Markers.CONCEPTNET_JUSTIFICATION);
						    connection.setDotted(true);
						}
					}
				}
				
			}
		}
		catch (Exception e) {
			Mark.err("Blew out in ElaborationView.processEntity");
			e.printStackTrace();
		}
	}

	private void colorProblemSoverBox(Entity entity, Box box) {
		// Defensive programming
		if (entity == null || box == null) {
			Mark.say("Entity/box\n>>>  ", entity, "\n>>>  ", box);
			return;
		}

		if (entity.hasFeature(Markers.NOT)) {
			box.setSlash(true);
		}

		// Get the problem solving node type, if any
		int type = (int) (entity.getIntegerProperty(ProblemSolver.NODE_TYPE));

		// Mark.say("Type", type);

		if (type < 0) {
			// No problem solving type
			// box.setColor(meansBox);
		}
		else if (type == ProblemSolver.PROBLEM) {
			box.setColor(Colors.PROBLEM);
		}
		else if (type == ProblemSolver.INTENTION) {
			box.setColor(Colors.INTENSION);
		}
		else if (type == ProblemSolver.CONDITION) {
			box.setColor(Colors.CONDITION);
		}
		else if (type == ProblemSolver.METHOD) {
			box.setColor(Colors.METHOD);
		}
		else if (type == ProblemSolver.RESULT) {
			box.setColor(Colors.RESULT);
		}
		else if (type == ProblemSolver.COMMENT) {
			box.setColor(Colors.COMMENT);
		}

	}

	public boolean isCausalConnection(Entity entity) {
		return entity.isA(Markers.CAUSE_MARKER) && entity.getSubject().sequenceP(Markers.CONJUNCTION);
	}

	private String getSentence(String hash) {
		return sentenceCache.get(hash);
	}

	public Box getBox(Entity entity) {
		String hash = hash(entity);
		Box box = boxSet.get(hash);
		if (box == null) {
			String sentence;
			try {
				sentence = getSentence(hash);
				if (sentence == null) {
					// Generate via cache does not work for unknown reason
					sentence = Generator.getGenerator().generate(entity);
					// Mark.say("Generator in elaboration view", entity, sentence);
					// Mark.say("Generated", sentence);
					sentenceCache.put(hash, sentence);
				}
			}
			catch (Exception e) {
				Mark.err("Could not generate from", entity);
				sentence = entity.toString();
			}
			int x = maxX() + 1;
			// Special case
			if (boxSet.size() == 0) {
				x = 0;
			}
			box = new Box(sentence, x, 0);
			if (entity.isA(Markers.ACTION_MARKER)) {
				box.setEvent(true);
			}
			if (entity.hasProperty(Markers.GOAL_ANALYSIS)) {
			    // use temporary color since temp color given precedence over normal
			    // color at draw time
			    box.setTemporaryColor(goalBox);
			}
			boxSet.put(hash, box);
		}
		return box;
	}

	private int maxX() {
		int result = 0;
		for (Box box : getConnectedBoxList()) {
			result = Math.max(result, box.x);
		}
		return result;
	}

	private int maxY() {
		int result = 0;
		for (Box box : getConnectedBoxList()) {
			result = Math.max(result, box.y);
		}
		return result;
	}

	private void compress() {
		// Pull non-events to the left column
		for (int i = 1; i < getConnectedBoxList().size(); ++i) {
			Box box = getConnectedBoxList().get(i);
			if (Switch.pullNonEventsToLeft.isSelected() && !box.isEvent()) {
				// Mark.say("Not event, dropping", box);
				// Mark.say("i/size", i, getBoxList().size());
				int initialX = box.getX();
				int newX = 0;
				// If inputs, move to right one past rightmost input
				if (box.getInputs().size() > 0) {
					for (Box input : box.getInputs()) {
						// Mark.say("input loc", input.getX(), input);
						newX = Math.max(newX, input.getX());
					}
					++newX;
				}
				dropToColumn(newX, box);
				pullToLeft(initialX + 1);
			}
		}
		// Drop antecedents
		for (Box box : getConnectedBoxList()) {
			ArrayList<Box> inputs = box.getInputs();
			// Arrange antecedents
			if (inputs.size() > 1) {
				for (int i = 1; i < inputs.size(); ++i) {
					int initialX = inputs.get(i).getX();
					// Mark.say("Dropping", inputs.get(i));
					drop(inputs.get(i));
					pullToLeft(initialX + 1);
				}
			}
		}
		// Lower dead ends
		for (int c = 0; c < maxX(); ++c) {
			dropDeadEndsToBottom(c);
		}

		// Push non events to the bottom of each column

		Map<Integer, List<Box>> columnMap = new HashMap<>();

		// Sort the boxes into columns
		for (Box b : getConnectedBoxList()) {
			List<Box> column = columnMap.get(b.getX());
			if (column == null) {
				column = new ArrayList<>();
				columnMap.put(b.getX(), column);
			}
			column.add(b);
		}

		// Now work on each column
		for (List<Box> column : columnMap.values()) {
			dropNonEvents(column);
		}

		// This doesn't work, not needed either
		// Pull consequents to column next to rightmost antecedent
		// for (Box box : getConnectedBoxList()) {
		// ArrayList<Box> outputs = box.getOutputs();
		// // Find rightmost column
		// int rightMost = 0;
		// for (int i = 1; i < outputs.size(); ++i) {
		// rightMost = Math.max(rightMost, outputs.get(i).getX());
		// }
		// for (int i = 1; i < outputs.size(); ++i) {
		// int initialX = outputs.get(i).getX();
		// // Do nothing if already there
		// if (initialX != rightMost) {
		// dropToColumn(rightMost + 1, outputs.get(i));
		// pullToLeft(initialX + 1);
		// }
		// }
		// }
		// Break into lines to get good width/height ratio
		realign();

		// Postprocessing for means; probably causes trouble
		for (Box box : getConnectedBoxList()) {
			// Mark.say("\n>>> Top", box.text);
			List<Integer> positions = new ArrayList<>();

			int max = -1;

			for (Box input : box.getInputs()) {
				int order = input.getOrder();
				// Mark.say(input.getY(), order, input.text);
				if (order >= 0) {
					positions.add(input.getY());
					max = Math.max(max, order);
				}
			}
			if (max > positions.size() - 1) {
				Mark.err(todo, "Screw up in Elaboration viewer---no reordering\n>>> Consequent", box.text);
				box.getInputs().stream().forEachOrdered(b -> {
					Mark.err(todo, b.text);
				});
				continue;
			}
			// Mark.say("Positions", max, positions);
			for (Box input : box.getInputs()) {
				int order = input.getOrder();
				try {
					if (order >= 0 && input.getY() != positions.get(order)) {
						// Mark.say("Ordering", input.getY(), "-->", positions.get(order), input.text);
						input.setY(positions.get(order));
					}
				}
				catch (Exception e) {
					Mark.err("Weirdness", order, positions);
				}
			}

		}
	}

	private void dropNonEvents(List<Box> column) {
		int row = 0;
		for (Box b : column) {
			if (!b.getInputConnections().isEmpty()) {
				b.setY(row++);
			}
		}
		for (Box b : column) {
			if (b.getInputConnections().isEmpty()) {
				b.setY(row++);
			}
		}

	}

	private void dropDeadEndsToBottom(int c) {
		// Gather elements in column
		ArrayList<Box> result = new ArrayList<Box>();
		ArrayList<Box> column = new ArrayList<Box>();
		for (Box b : getConnectedBoxList()) {
			if (b.getX() == c) {
				column.add(b);
			}
		}
		for (Box b : column) {
			if (b.getOutputs().size() > 0) {
				result.add(b);
			}
		}
		for (Box b : column) {
			if (b.getOutputs().size() == 0) {
				result.add(b);
			}
		}
		for (int i = 0; i < result.size(); ++i) {
			result.get(i).setY(i);
		}
	}

	private void realign() {
		Point lowerRight = getMaxPoint();
		int maxX = lowerRight.x;
		int maxY = lowerRight.y;

		rowCount = calculateRows(maxX, maxY, idealRatio);
		// Mark.say("Before data", maxX, maxY, rows);

		breakApart(rowCount);

	}

	private int calculateRows(int maxX, int maxY, float idealRatio) {
		int bestN = 0;
		double previousRatio = 0;
		for (int n = 1; true; ++n) {
			double ratio = (double) maxX / (n * n * maxY);
			if (n == 1 || Math.abs(ratio - idealRatio) < Math.abs(previousRatio - idealRatio)) {
				bestN = n;
				previousRatio = ratio;
			}
			else {
				return bestN;
			}
		}
	}

	private void breakApart(int rows) {
		int originalWidth = maxX() + 1;
		rowHeight = maxY() + 1;

		rowWidth = (int) Math.ceil((double) originalWidth / rows);

		// Mark.say("Row dimensions", rowWidth, rowHeight, rowCount);

		for (Box b : getConnectedBoxList()) {
			int boxX = b.getX();
			int boxY = b.getY();
			int newX = boxX % rowWidth;
			int newY = boxY + (boxX / rowWidth) * (rowHeight + 1);
			// if (newX != boxX && newY != boxY) {
			setBox(b, newX, newY, "breakApart");
			// }
		}
	}

	/*
	 * Pull to left all boxes with x >= i
	 */
	private void pullToLeft(int i) {
		if (i <= 0) {
			return;
		}
		// First, make sure column is empty
		for (Box box : getConnectedBoxList()) {
			if (box.getX() == i - 1) {
				// Not empty
				// Mark.say("Cannot pull to column", i, "because", box.getX(), box.getY(), "occupied by", box);
				return;
			}
		}
		for (Box box : getConnectedBoxList()) {
			if (box.getX() >= i) {
				setBox(box, box.getX() - 1, box.getY(), "pullToLeft");
			}
		}
	}

	/*
	 * Drop specified box to bottom of column on left
	 */
	private void drop(Box box) {
		int c = box.getX() - 1;
		dropToColumn(c, box);
	}

	/*
	 * Drop specified box to bottom of specified column.
	 */
	private void dropToColumn(int c, Box box) {
		// Do nothing if c negative
		if (c < 0) {
			return;
		}
		// Mark.say("Old x/y for", box, box.getX(), box.getY());
		int r = 0;
		for (Box x : getConnectedBoxList()) {
			if (x.getX() == c && x != box) {
				// if (c == 1) {
				// Mark.say("+++", x);
				// }
				++r;
			}
		}
		// Mark.say("New x/y for", box, c, r);
		setBox(box, c, r, "dropToColumn");

	}

	private void setBox(Box box, int c, int r, String s) {
		for (Box b : getConnectedBoxList()) {
			if (b == box) {
				continue;
			}
			else if (b.getX() == c && b.getY() == r) {
				// This can happen when two things cause an event. The second one may then point back in time causing
				// this configuration.
				//
				// Mark.say("Cannot move box", box, "at", box.getX(), box.getY(), "to", c, r, "in", s,
				// "because of a conflict with", b);

				// Try a level lower down until win
				setBox(box, c, r + 1, s);
				return;
			}
		}
		box.setX(c);
		box.setY(r);
	}

	private Connection connect(Box s, Box d) {
		Connection connection = new Connection(s, d);
		s.addOutput(d);
		d.addInput(s);
		// Mark.say("Connected", s, "to", d);
		getConnectionList().add(connection);
		s.addOutputConnection(connection);
		d.addInputConnection(connection);
		return connection;
	}

	private Box addBox(Box box) {
		if (!getConnectedBoxList().contains(box)) {
			getConnectedBoxList().add(box);
			validate();
		}
		return box;
	}

	private AffineTransform inverse;

	public void paint(Graphics g) {
		try {
			super.paint(g);

			Graphics2D graphics = (Graphics2D) g;

			// Prepare transform
			int width = getWidth();
			int height = getHeight();

			// int vPadding = 0;
			// int hPadding = 0;

			AffineTransform transform = new AffineTransform();
			Point lowerRight = getMaxPoint();
			// + boxWidth to allow for wires, a hack
			int maxWidth = lowerRight.x + boxWidth;
			int maxHeight = lowerRight.y;

			double scale = 1.0;

			int hTranslation = leftAndRightOffset;
			int vTranslation = topAndBottomOffset;

			if (width * maxHeight < height * maxWidth) {
				scale = (double) width / maxWidth;
				// hTranslation = (int) ((width - maxHeight * scale) / 2);
				vTranslation = (int) ((height - maxHeight * scale) / 2);
				hTranslation = 0;
			}
			else {
				scale = (double) height / maxHeight;
				hTranslation = (int) ((width - maxWidth * scale) / 2);
				// vTranslation = (int) ((width - maxWidth * scale) / 2);
				vTranslation = 0;
			}

			// Deal with offsets

			transform.translate(offsetX, offsetY);

			// Move center to origin, magnify, move back

			transform.translate(-width / 2, -height / 2);

			transform.scale(magnification, magnification);

			transform.translate(width / magnification / 2, height / magnification / 2);

			// Size diagram to window and establish offsets

			transform.translate(hTranslation, vTranslation);

			transform.scale(scale, scale);

			// g.setTransform(transform);

			// Used to figure out where mouse is clicked
			try {
				inverse = transform.createInverse();
			}
			catch (NoninvertibleTransformException e) {
				e.printStackTrace();
			}

			int wBox = (int) (transform.getScaleX() * boxWidth);
			int hBox = (int) (transform.getScaleY() * boxHeight);
			drawBoxes(graphics, transform, wBox, hBox);

			drawConnections(graphics, transform);

			// Throws concurrent mod exception for unknown reason
			// for (Connection connection : getConnectionList()) {
			// drawConnection(g, connection);
			// }
			if (mouseDown) {
				this.drawCross(graphics, width, height);
			}
			if (showType && showConnectionTypeInElaborationGraph) {
				this.drawConnectionType(graphics, width, height);
			}
		}
		catch (Exception e) {
			Mark.err("Harmless exception in display");
		}
	}

	public void drawConnections(Graphics2D graphics, AffineTransform transform) {
		// Work on this row by row
		try {
			for (int row = 0; row < rowCount * (rowHeight + 1); ++row) {
				ArrayList<Connection> columnSpanners = new ArrayList<Connection>();
				ArrayList<Connection> rowSpanners = new ArrayList<Connection>();
				ArrayList<Connection> adjacents = new ArrayList<Connection>();
				for (Connection connection : getConnectionList()) {
					Box source = connection.source;
					Box destination = connection.destination;
					if (source.getY() != row) {
						continue;
					}
					if (destination.getX() != source.getX() + 1) {
						if (source.getY() / rowHeight != destination.getY() / rowHeight) {
							rowSpanners.add(connection);
						}
						else {
							columnSpanners.add(connection);
						}
					}
					else {
						adjacents.add(connection);
					}
				}
				// Now draw adjacents for this row
				for (Connection connection : adjacents) {
					drawConnection(graphics, transform, connection);
				}
				// Now draw spanners for this row
				int columnSpannerCount = columnSpanners.size();
				int rowSpannerCount = rowSpanners.size();
				int endSpacing = boxVPadding / 5;
				int topOfBoxSpacing = boxVPadding / (rowSpannerCount + columnSpannerCount + 1);
				for (int i = 0; i < columnSpanners.size(); ++i) {
					int offset = topOfBoxSpacing + i * topOfBoxSpacing;
					drawColumnConnection(graphics, transform, columnSpanners.get(i), offset);
				}
				// Now do inter-row stuff, should make this go around the corner
				int spannersProcessed = 0;
				for (int i = 0; i < rowSpanners.size(); ++i) {
					int offset = topOfBoxSpacing + (i + columnSpannerCount) * topOfBoxSpacing;
					++spannersProcessed;
					drawRowConnection(graphics, transform, rowSpanners.get(i), offset, spannersProcessed * endSpacing);
				}

			}
		}
		catch (Exception e) {
			Mark.err("Harmless exception drawing conections");
		}
	}

	public void drawBoxes(Graphics2D graphics, AffineTransform transform, int wBox, int hBox) {
		for (int i = 0; i < getConnectedBoxList().size(); ++i) {
			drawBox(graphics, transform, wBox, hBox, getConnectedBoxList().get(i));
		}
	}

	private Point getMaxPoint() {
		int maxX = maxX();
		int maxY = maxY();
		int x = 2 * leftAndRightOffset + (maxX + 1) * boxWidth + maxX * boxHPadding;
		int y = 2 * topAndBottomOffset + (maxY + 1) * (boxHeight + boxVPadding);
		return new Point(x, y);
	}

	private Point getBoxOrigin(Box box) {
		int x = leftAndRightOffset + box.getX() * (boxWidth + boxHPadding);
		int y = topAndBottomOffset + box.getY() * (boxHeight + boxVPadding);
		return new Point(x, y);
	}

	/*
	 * Bug in pdf printer requires strange handling of affine transformation
	 */
	public void drawConnection(Graphics2D graphics, AffineTransform transform, Connection connection) {
		Box s = connection.source;
		Box d = connection.destination;
		if (connection.getTemporaryColor() != null) {
			graphics.setColor(connection.getTemporaryColor());
		}
		else {
			graphics.setColor(connection.getColor());
		}

		graphics.setStroke(connection.getStroke());

		// Mark.say("Drawing line", sX, sY, dX, dY);
		int dX = leftAndRightOffset + d.getX() * (boxWidth + boxHPadding);
		int dY = topAndBottomOffset + d.getY() * (boxHeight + boxVPadding) + boxHeight / 2;

		int sX = leftAndRightOffset + s.getX() * (boxWidth + boxHPadding) + boxWidth;
		int sY = topAndBottomOffset + s.getY() * (boxHeight + boxVPadding) + boxHeight / 2;
		drawLine(graphics, transform, sX, sY, dX, dY);

		graphics.setColor(blackBox);
	}

	public void drawColumnConnection(Graphics2D graphics, AffineTransform transform, Connection connection, int delta) {
		Box s = connection.source;
		Box d = connection.destination;
		if (connection.getTemporaryColor() != null) {
			graphics.setColor(connection.getTemporaryColor());
		}
		else {
			graphics.setColor(connection.getColor());
		}

		graphics.setStroke(connection.getStroke());

		int sX = leftAndRightOffset + s.getX() * (boxWidth + boxHPadding) + boxWidth - delta;
		int sY = topAndBottomOffset + s.getY() * (boxHeight + boxVPadding);

		int dX = leftAndRightOffset + d.getX() * (boxWidth + boxHPadding);
		int dY = topAndBottomOffset + d.getY() * (boxHeight + boxVPadding) + boxHeight / 2;

		int p1X = sX;
		int p1Y = topAndBottomOffset + s.getY() * (boxHeight + boxVPadding) - delta; // - boxVPadding / 2;
		drawLine(graphics, transform, sX, sY, p1X, p1Y);

		int p2Y = p1Y;
		int p2X = leftAndRightOffset + (d.getX() - 1) * (boxWidth + boxHPadding) + boxWidth + boxHPadding / 4;
		drawLine(graphics, transform, p1X, p1Y, p2X, p2Y);

		int p3X = p2X;
		int p3Y = p2Y;
		drawLine(graphics, transform, p3X, p3Y, dX, dY);

		graphics.setColor(blackBox);
	}

	public void drawRowConnection(Graphics2D graphics, AffineTransform transform, Connection connection, int delta, int deltaX) {
		Box s = connection.source;
		Box d = connection.destination;
		if (connection.getTemporaryColor() != null) {
			graphics.setColor(connection.getTemporaryColor());
		}
		else {
			graphics.setColor(connection.getColor());
		}

		graphics.setStroke(connection.getStroke());

		int sX = leftAndRightOffset + s.getX() * (boxWidth + boxHPadding) + boxWidth - delta;
		int sY = topAndBottomOffset + s.getY() * (boxHeight + boxVPadding);

		int dX = leftAndRightOffset + d.getX() * (boxWidth + boxHPadding);
		int dY = topAndBottomOffset + d.getY() * (boxHeight + boxVPadding) + boxHeight / 2;

		// Draw line up from box
		int p1X = sX;
		int p1Y = topAndBottomOffset + s.getY() * (boxHeight + boxVPadding) - delta; // - boxVPadding / 2;

		drawLine(graphics, transform, sX, sY, p1X, p1Y);

		// Draw line to just right of the box

		int p2X = leftAndRightOffset + s.getX() * (boxWidth + boxHPadding) + boxWidth - boxHPadding + delta;
		int p2Y = p1Y;

		// Draw line to just right of the rightmost box + deltaX

		p2X = leftAndRightOffset + rowWidth * (boxWidth + boxHPadding) + deltaX;
		p2Y = p1Y;

		drawLine(graphics, transform, p1X, p1Y, p2X, p2Y);

		// Draw line to row break of destination

		// p3Y complicated because have to find right row for lines

		int p3X = p2X;
		int p3Y = topAndBottomOffset + (d.y / (rowHeight + 1)) * (rowHeight + 1) * (boxHeight + boxVPadding) - boxHeight - boxVPadding / 2 + deltaX;

		drawLine(graphics, transform, p2X, p2Y, p3X, p3Y);

		// Draw line to just to left of destination

		int p4X = dX - boxHPadding;
		int p4Y = topAndBottomOffset + (d.y / rowHeight) * rowHeight * (boxHeight + boxVPadding) + boxHeight - boxVPadding / 2;

		drawLine(graphics, transform, p3X, p3Y, p4X, p4Y);

		// Draw line to destination

		drawLine(graphics, transform, p4X, p4Y, dX, dY);

		graphics.setColor(blackBox);
	}

	private void drawLine(Graphics2D graphics, AffineTransform transform, int sX, int sY, int dX, int dY) {
		Point s = new Point(sX, sY);
		Point d = new Point(dX, dY);
		transform.transform(s, s);
		transform.transform(d, d);
		graphics.drawLine(s.x, s.y, d.x, d.y);
	}

	/*
	 * Bug in pdf printer requires strange handling of affine transformation
	 */
	private void drawBox(Graphics graphics, AffineTransform transform, int width, int height, Box box) {
		int bX = box.getX();
		int bY = box.getY();
		// Mark.say("Drawing", box.text, bX, bY);
		int x = leftAndRightOffset + bX * (boxWidth + boxHPadding);
		int y = topAndBottomOffset + bY * (boxHeight + boxVPadding);

		// Now, transform upper left corner
		Point point = new Point(x, y);
		transform.transform(point, point);
		x = point.x;
		y = point.y;

		// Now get box width and height

		point = new Point(boxWidth, boxHeight);

		if (box.getTemporaryColor() != null) {
			graphics.setColor(box.getTemporaryColor());
		}
		else {
			graphics.setColor(box.getColor());
		}
		graphics.fillRect(x, y, width, height);
		graphics.setColor(box.getOutlineColor());

		graphics.drawRect(x, y, width, height);

		Rectangle rectangle = new Rectangle(x, y, width, height);
		Font font = graphics.getFont();
		graphics.setFont(new Font(font.getName(), Font.BOLD, font.getSize() + 50));
		drawLabel(graphics, box.text, rectangle);
		graphics.setFont(font);

		if (box.isSlash()) {
			// graphics.setColor(Color.RED);
			Stroke handle = ((Graphics2D)graphics).getStroke();
			((Graphics2D) graphics).setStroke(new BasicStroke(5));
			graphics.drawLine(x + width, y, x, y + height);
			((Graphics2D) graphics).setStroke(handle);
		}

		graphics.setColor(blackBox);
	}

	private class Connection {
		public Box source;

		public Box destination;
		
		private String connectionType = "causal";

		private Color color;

		private Color temporaryColor;

		private boolean bold = false;

		private boolean dotted = false;

		public Stroke getStroke() {
			if (bold) {
				if (dotted) {
					return boldDottedStroke;
				}
				else {
					return boldStroke;
				}
			}
			else {
				if (dotted) {
					return dottedStroke;
				}
				else {
					return basicStroke;
				}
			}
		}

		public boolean isBold() {
			return bold;
		}

		public void setBold(boolean bold) {
			this.bold = bold;
		}

		public boolean isDotted() {
			return dotted;
		}

		public void setDotted(boolean dotted) {
			this.dotted = dotted;
		}
		
		// added by Z on 10 Aug 2019 to indicate which kind of inference it is
		// when mouse over the box
		public void setConnectionType(String type) {
			this.connectionType = type;
		}
		public String getConnectionType() {
			return this.connectionType;
		}
		

		public Color getColor() {
			if (color == null) {
				color = blackBox;
			}
			return color;
		}

		public Color getTemporaryColor() {
			return temporaryColor;
		}

		public void setTemporaryColor(Color temporaryColor) {
			this.temporaryColor = temporaryColor;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public Connection(Box source, Box destination) {
			super();
			this.source = source;
			this.destination = destination;
		}
	}

	private class Box implements Comparable {
		private String text;

		private int x, y;

		private ArrayList<Box> inputs;

		private ArrayList<Box> outputs;

		private Color color;

		private Color outlineColor;

		private Color temporaryColor;

		private boolean event;

		private ArrayList<Connection> inputConnections = new ArrayList<Connection>();

		private ArrayList<Connection> outputConnections = new ArrayList<Connection>();

		// Order hack is for postprocessing to get antecedents in proper top to bottom order for means expressions

		private int order = -1;

		private boolean slash = false;

		public boolean isSlash() {
			return slash;
		}

		public void setSlash(boolean slash) {
			this.slash = slash;
		}

		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public Box(String text, int x, int y) {
			super();
			this.text = text;
			this.x = x;
			this.y = y;
		}

		public void addInputConnection(Connection connection) {
			inputConnections.add(connection);

		}

		public void addOutputConnection(Connection connection) {
			outputConnections.add(connection);
		}

		public ArrayList<Connection> getInputConnections() {
			return inputConnections;
		}

		public ArrayList<Connection> getOutputConnections() {
			return outputConnections;
		}

		public void setEvent(boolean b) {
			event = b;
		}

		public boolean isEvent() {
			return event;
		}

		// public boolean equals(Object o) {
		// if (o instanceof Box) {
		// return this.text.equals(((Box) o).text);
		// }
		// return false;
		// }

		public Color getColor() {
			if (color == null) {
				color = assertionBox;
			}
			return color;
		}

		public void setColor(Color color) {
			this.color = color;
		}

		public Color getOutlineColor() {
			if (outlineColor == null) {
				outlineColor = blackBox;
			}
			return outlineColor;
		}

		public void setOutlineColor(Color outlineColor) {
			this.outlineColor = outlineColor;
		}

		public Color getTemporaryColor() {
			return temporaryColor;
		}

		public void setTemporaryColor(Color temporaryColor) {
			this.temporaryColor = temporaryColor;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public void setX(int x) {
			this.x = x;
		}

		public void setY(int y) {
			this.y = y;
		}

		public ArrayList<Box> getInputs() {
			if (inputs == null) {
				inputs = new ArrayList<Box>();
			}
			return inputs;
		}

		public void addInput(Box box) {
			if (!getInputs().contains(box)) {
				getInputs().add(box);
			}
		}

		public ArrayList<Box> getOutputs() {
			if (outputs == null) {
				outputs = new ArrayList<Box>();
			}
			return outputs;
		}

		public void addOutput(Box box) {
			if (!getOutputs().contains(box)) {
				getOutputs().add(box);
			}
		}

		public String toString() {
			return "<" + text + ">";
		}

		@Override
		public int compareTo(Object o) {
			Box that = (Box) o;
			if (this.getX() < that.getX()) {
				return -1;
			}
			else if (this.getX() > that.getX()) {
				return 1;
			}
			else if (this.getY() < that.getY()) {
				return -1;
			}
			else if (this.getY() > that.getY()) {
				return 1;
			}
			return 0;
		}
	}

	private void highlight(Box box) {
		// Mark.say("Redoing", getConnectionList().size(), "connections");
		extinguish();
		for (Connection c : box.getInputConnections()) {
			c.setTemporaryColor(Color.red);
			c.setBold(true);
			showTypeString += "IN: " + c.getConnectionType() + ", ";
			showType = true;
		}
		for (Connection c : box.getOutputConnections()) {
			c.setTemporaryColor(Color.green);
			c.setBold(true);
			showTypeString += "OUT: " + c.getConnectionType() + ", ";
			showType = true;
		}
	}

	private void extinguish() {
		for (Connection c : getConnectionList()) {
			c.setBold(false);
			c.setTemporaryColor(null);
		}
	}

	public ArrayList<Box> getConnectedBoxList() {
		if (connectedBoxList == null) {
			connectedBoxList = new ArrayList<Box>();
		}
		return connectedBoxList;
	}

	public ArrayList<Box> getUnconnectedBoxList() {
		if (unconnectedBoxList == null) {
			unconnectedBoxList = new ArrayList<Box>();
		}
		return unconnectedBoxList;
	}

	public ArrayList<Connection> getConnectionList() {
		if (connectionList == null) {
			connectionList = new ArrayList<Connection>();
		}
		return connectionList;
	}

	/*
	 * Imported from legacy viewer
	 */
	private void drawLabel(Graphics g, String label, Rectangle rectangle) {
		String[] words = Pattern.compile(" ").split(label);
		FontMetrics fm = g.getFontMetrics();
		int width = rectangle.width;
		ArrayList<String> result = new ArrayList<String>();
		String row = "";
		int spaceWidth = fm.stringWidth(" ");
		int maxWidth = 0;
		for (String word : words) {
			int rowWidth = fm.stringWidth(row);
			int wordWidth = fm.stringWidth(word);
			if (rowWidth == 0) {
				row = word;
			}
			else if (rowWidth + spaceWidth + wordWidth < width) {
				row += " " + word;
			}
			else {
				result.add(row);
				int thisWidth = fm.stringWidth(row);
				if (thisWidth > maxWidth) {
					maxWidth = thisWidth;
				}
				row = word;
			}
		}
		if (!row.isEmpty()) {
			result.add(row);
		}
		int lineCount = result.size();
		int lineHeight = g.getFontMetrics().getHeight();
		int height = lineCount * lineHeight;

		if (maxWidth > rectangle.width - 4 || height > rectangle.height - 4) {
			Font font = g.getFont();
			if (font.getSize() - 1 >= 1) {
				g.setFont(new Font(font.getName(), Font.BOLD, (int) font.getSize() - 1));
				drawLabel(g, label, rectangle);
			}
		}
		else {
			lineHeight = g.getFontMetrics().getHeight();
			int heightOffset = ((lineCount - 1) * lineHeight) / 2;
			for (int i = 0; i < lineCount; ++i) {
				String line = result.get(i);
				int stringWidth = g.getFontMetrics().stringWidth(line);
				int x = rectangle.x + rectangle.width / 2 - stringWidth / 2;
				int y = rectangle.y + rectangle.height / 2;
				// put the text on the boxes
				g.drawString(line, x, y - heightOffset + i * lineHeight);
			}
		}
	}

	public Box getBox(MouseEvent e) {
		Point input = new Point(e.getX(), e.getY());
		Point2D output = inverse.transform(input, null);
		int x = (int) output.getX();
		int y = (int) output.getY();
		// Mark.say("Point", x, y);
		try {
			for (Box box : getConnectedBoxList()) {
				Point p = getBoxOrigin(box);
				if (x >= p.x && x <= p.x + boxWidth && y >= p.y && y <= p.y + boxHeight) {
					return box;
				}
			}
		}
		catch (Exception exception) {
			Mark.err("Harmless exception drawing boxes");
		}
		return null;
	}

	private void magnify(double m) {

		double previousMagnification = magnification;
		int halfWidth = getWidth() / 2;
		int halfHeight = getHeight() / 2;

		magnification = m * magnification;
		// Completely mysterious legacy code, but it works
		offsetX = (int) (magnification * (offsetX - halfWidth) / previousMagnification) + halfWidth;
		offsetY = (int) (magnification * (offsetY - halfHeight) / previousMagnification) + halfHeight;

		repaint();
	}

	private void drawCross(Graphics g, int width, int height) {
		int r = 5;
		int w = width / 2;
		int h = height / 2;
		g.setColor(crossColor);

		g.drawLine(w - r, h, w + r, h);
		g.drawLine(w, h - 4, w, h + r);
		g.setColor(blackBox);

	}
	
	// added by Zhutian on 10 August 2019 for 
	private void drawConnectionType(Graphics g, int width, int height) {
		int r = 5;
		int w = width / 2;
		int h = height / 2;
		g.setColor(crossColor);

		g.drawString(showTypeString, w, h);
//		g.drawString(w, h - 4, w, h + r);
		g.setColor(blackBox);

	}

	public void setAlwaysShowAllElements(boolean alwaysShowAllElements) {
		this.alwaysShowAllElements = alwaysShowAllElements;
	}

	// Mouse listener methods

	private int offsetX = 0;

	private int offsetY = 0;

	private int pressedAtX = -1;

	private int pressedAtY = -1;

	private int offsetXWhenPressed = -1;

	private int offsetYWhenPressed = -1;

	private boolean mouseDown;
	
	private boolean showType;
	private String showTypeString;

	@Override
	public void mousePressed(MouseEvent e) {
		pressedAtX = e.getX();
		pressedAtY = e.getY();
		offsetXWhenPressed = offsetX;
		offsetYWhenPressed = offsetY;
		mouseDown = true;
		repaint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressedAtX = -1;
		pressedAtY = -1;
		offsetXWhenPressed = -1;
		offsetYWhenPressed = -1;
		mouseDown = false;
		repaint();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (pressedAtX != -1 && pressedAtY != -1) {
			setOffsetX(e.getX() - pressedAtX + offsetXWhenPressed);
			setOffsetY(e.getY() - pressedAtY + offsetYWhenPressed);
		}
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
		repaint();
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
		repaint();
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			magnify(1.2);
		}
		else if (e.getButton() == MouseEvent.BUTTON3) {
			magnify(1 / 1.2);
		}

	}

	Box highlighted = null;

	@Override
	public void mouseMoved(MouseEvent e) {
		Box box = getBox(e);
		if (box != highlighted) {
			if (box != null) {
				// Mark.say("Highlight", box);
				highlight(box);
			}
			else {
				extinguish();
				
				// added by Zhutian on 10 Aug 2019
				showType = false;
				showTypeString = "";
			}
			highlighted = box;
			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * @param args
	 * @throws Exception
	 */
//	 public static void main(String[] args) throws Exception {
//	 JFrame frame = new JFrame();
//	 frame.setBounds(0, 0, 1000, 400);
//	 frame.setVisible(true);
//	 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	 ElaborationView ev = new ElaborationView();
//	 ev.test();
//	 frame.getContentPane().add(ev);
//	 frame.revalidate();
//	
//	 }

	private Color crossColor;

	private Color blackBox;

	private Color assertionBox;

	private Color assertionBoxWire;

	private Color deductionBox;

	private Color deductionBoxWire;

	private Color explanationBox;

	private Color explanationBoxWire;

	private Color abductionBox;

	private Color abductionBoxWire;

	private Color enablerBox;

	private Color enablerBoxWire;

	private Color proximityBox;

	private Color proximityBoxWire;

	private Color presumptionBox;

	private Color presumptionBoxWire;

	private Color leadsToBox;

	private Color leadsToBoxWire;

	private Color unknowableBox;

	private Color unknowableBoxWire;

	private Color meansBox;

	private Color meansBoxWire;

	private Color conceptBox;
	
	private Color goalBox;
	
	private Color goalWire;

	private void initializeColors(boolean useColor) {
		setBackground(Color.white);
		if (useColor) {
			crossColor = Color.red;
			blackBox = Color.black;

			assertionBox = Color.white;
			assertionBoxWire = Color.black;

			deductionBox = Color.yellow;
			deductionBoxWire = Color.black;

			explanationBox = Color.white;
			explanationBoxWire = Color.orange;

			presumptionBox = new Color(51, 204, 255);
			presumptionBoxWire = presumptionBox;

			// Violet
			abductionBox = new Color(153, 153, 255);
			abductionBoxWire = abductionBox;

			enablerBox = Color.pink;
			enablerBoxWire = Color.pink;

			proximityBox = Color.white;
			proximityBoxWire = Color.red;

			leadsToBox = Color.white;
			leadsToBoxWire = Color.cyan;

			unknowableBox = Color.white;
			unknowableBoxWire = Color.magenta;

			meansBox = Color.lightGray;
			meansBoxWire = Color.gray;

			conceptBox = Color.green;
			
			goalWire = Color.magenta;
			goalBox = Color.magenta;

			Colors.TEXT_COLOR = new Color(204, 255, 255);
			// WConstants.nameBarColor = Color.blue;
			// GenesisColors.menuBarColor = new JMenuBar().getBackground();
		}
		else {
			crossColor = Color.white;
			blackBox = Color.black;
			assertionBox = Color.white;
			deductionBox = Color.lightGray;
			explanationBox = Color.white;
			presumptionBox = Color.white;
			explanationBoxWire = Color.black;
			abductionBox = Color.lightGray;
			enablerBox = Color.lightGray;
			proximityBox = Color.white;
			leadsToBox = Color.white;
			leadsToBoxWire = Color.black;
			unknowableBox = Color.white;
			meansBox = Color.white;
			conceptBox = Color.gray;

			Colors.TEXT_COLOR = Color.white;
			// WConstants.nameBarColor = Color.lightGray;
			// GenesisColors.menuBarColor = Color.gray;
		}
	}

	public int print(Graphics graphics, PageFormat format, int pageIndex) {
		return EasyPrint.easyPrint(this, graphics, format, pageIndex);
	}

	public void printMe() {
		EasyPrint.easyPrint(this);
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

}
