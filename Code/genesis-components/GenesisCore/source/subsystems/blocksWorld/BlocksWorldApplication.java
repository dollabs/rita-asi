package subsystems.blocksWorld;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import subsystems.blocksWorld.models.*;
import subsystems.blocksWorld.views.*;
import utils.Mark;

/*
 * Created on Sep 10, 2005 @author Patrick
 */

public class BlocksWorldApplication {

	WorldModel model;

	JSplitPane splitPane;

	WorldView view;

	AndOrView andOrView;

	JTextField question;

	JTextPane explanationPane;

	JPanel controlBar;

	JPanel controls;

	public JTextField getQuestion() {
		if (question == null) {
			question = new JTextField(10);
			// question = new JTextField();
			question.setBorder(BorderFactory.createTitledBorder(null, "Question"));
			question.setFont(new Font("Swiss", Font.BOLD, 40));
			question.setEditable(true);
			question.setPreferredSize(new Dimension(1000, 50));
			question.addActionListener(new TextListener(question));
		}
		return question;
	}

	class TextListener implements ActionListener {
		JTextField field;

		public TextListener(JTextField field) {
			this.field = field;
		}

		public void actionPerformed(ActionEvent event) {
			String text = field.getText();
			String answer = model.answerQuestion(text);
			getExplanationPane().setText(answer);
			JOptionPane.showMessageDialog(getView(), getExplanationPane(), "Explanation", JOptionPane.PLAIN_MESSAGE);
			field.setText("");
		}
	}

	JPanel fromMenu;

	JPanel toMenu;

	JPanel initializeBar;

	Block mover;

	Brick target;

	JButton fromItem0 = new JButton("Table");

	JButton fromItem1 = new JButton("B1");

	JButton fromItem2 = new JButton("B2");

	JButton fromItem3 = new JButton("B3");

	JButton fromItem4 = new JButton("B4");

	JButton fromItem5 = new JButton("B5");

	JButton fromItem6 = new JButton("B6");

	JButton fromItem7 = new JButton("B7");

	JButton fromItem8 = new JButton("B8");

	JButton toItem0 = new JButton("Table");

	JButton toItem1 = new JButton("B1");

	JButton toItem2 = new JButton("B2");

	JButton toItem3 = new JButton("B3");

	JButton toItem4 = new JButton("B4");

	JButton toItem5 = new JButton("B5");

	JButton toItem6 = new JButton("B6");

	JButton toItem7 = new JButton("B7");

	JButton toItem8 = new JButton("B8");

	JButton pileItem = new JButton("Initialize in pile");

	JButton boardItem = new JButton("Initialize for simulation");

	JButton standardItem = new JButton("Initialize on table");

	JButton mixingItem = new JButton("Initialize for mixing");

	JButton cellItem = new JButton("Initialize as cell phone");

	JButton torchItem = new JButton("Initialize as torch");

	static boolean running = false;

	public BlocksWorldApplication() {

		getModel().addObserver(new WorldObserver());
		getModel().changed();
		getModel().initializeOnTable();
	}

	public void start() {
		getModel().addObserver(new WorldObserver());
		getModel().changed();
		getModel().initializeOnTable();
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean b) {
		running = b;
		if (b) {
			getWorld().getStopLight().setRed(true);
		}
		else {
			getWorld().getStopLight().setGreen(true);
		}
	}

	public void assembleControlBars() {
		if (fromMenu != null) {
			return;
		}
		MenuListener listener = new MenuListener();
		fromMenu = new JPanel();
		toMenu = new JPanel();
		initializeBar = new JPanel();
		fromMenu.setLayout(new GridLayout(1, 0));
		toMenu.setLayout(new GridLayout(1, 0));
		initializeBar.setLayout(new GridLayout(1, 0));

		Font bigFont = new Font("Swiss", Font.BOLD, 20);

		fromItem0.setFont(bigFont);
		fromItem1.setFont(bigFont);
		fromItem2.setFont(bigFont);
		fromItem3.setFont(bigFont);
		fromItem4.setFont(bigFont);
		fromItem5.setFont(bigFont);
		fromItem6.setFont(bigFont);
		fromItem7.setFont(bigFont);
		fromItem8.setFont(bigFont);

		toItem0.setFont(bigFont);
		toItem1.setFont(bigFont);
		toItem2.setFont(bigFont);
		toItem3.setFont(bigFont);
		toItem4.setFont(bigFont);
		toItem5.setFont(bigFont);
		toItem6.setFont(bigFont);
		toItem7.setFont(bigFont);
		toItem8.setFont(bigFont);

		fromMenu.add(fromItem0);
		fromItem0.addMouseListener(listener);
		fromMenu.add(fromItem1);
		fromItem1.addMouseListener(listener);
		fromMenu.add(fromItem2);
		fromItem2.addMouseListener(listener);
		fromMenu.add(fromItem3);
		fromItem3.addMouseListener(listener);
		fromMenu.add(fromItem4);
		fromItem4.addMouseListener(listener);
		fromMenu.add(fromItem5);
		fromItem5.addMouseListener(listener);
		fromMenu.add(fromItem6);
		fromItem6.addMouseListener(listener);
		fromMenu.add(fromItem7);
		fromItem7.addMouseListener(listener);
		fromMenu.add(fromItem8);
		fromItem8.addMouseListener(listener);
		toMenu.add(toItem0);
		toItem0.addMouseListener(listener);
		toMenu.add(toItem1);
		toItem1.addMouseListener(listener);
		toMenu.add(toItem2);
		toItem2.addMouseListener(listener);
		toMenu.add(toItem3);
		toItem3.addMouseListener(listener);
		toMenu.add(toItem4);
		toItem4.addMouseListener(listener);
		toMenu.add(toItem5);
		toItem5.addMouseListener(listener);
		toMenu.add(toItem6);
		toItem6.addMouseListener(listener);
		toMenu.add(toItem7);
		toItem7.addMouseListener(listener);
		toMenu.add(toItem8);
		toItem8.addMouseListener(listener);

		standardItem.setFont(bigFont);
		mixingItem.setFont(bigFont);
		pileItem.setFont(bigFont);
		boardItem.setFont(bigFont);
		cellItem.setFont(bigFont);
		torchItem.setFont(bigFont);

		initializeBar.add(cellItem);
		initializeBar.add(pileItem);
		initializeBar.add(standardItem);
		initializeBar.add(mixingItem);
		initializeBar.add(torchItem);
		initializeBar.add(boardItem);
		cellItem.addMouseListener(listener);
		torchItem.addMouseListener(listener);
		standardItem.addMouseListener(listener);
		mixingItem.addMouseListener(listener);
		pileItem.addMouseListener(listener);
		boardItem.addMouseListener(listener);

	}

	public JPanel getFromControlBar() {
		if (fromMenu == null) {
			assembleControlBars();
		}
		return fromMenu;
	}

	public JPanel getToControlBar() {
		if (toMenu == null) {
			assembleControlBars();
		}
		return toMenu;
	}

	public WorldModel getModel() {
		if (model == null) {
			model = new WorldModel();
		}
		return model;
	}

	JPanel theView;

	public JComponent getView() {
		if (theView == null) {
			// splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
			splitPane.setDividerLocation(0.0);
			splitPane.setDividerSize(12);
			splitPane.setOneTouchExpandable(true);
			splitPane.setLeftComponent(getTree());
			splitPane.setRightComponent(getWorld());
			splitPane.resetToPreferredSizes();

			theView = new JPanel();
			theView.setLayout(new BorderLayout());
			// theView.add(getControls(), BorderLayout.NORTH);
			theView.add(splitPane, BorderLayout.CENTER);
			splitPane.setPreferredSize(new Dimension(1000, 500));
			theView.invalidate();
		}
		return theView;
	}

	public WorldView getWorld() {
		if (view == null) {
			view = new WorldView();
		}
		return view;
	}

	public AndOrView getTree() {
		if (andOrView == null) {
			andOrView = new AndOrView();
		}
		return andOrView;
	}

	class WorldObserver implements Observer {

		public void update(Observable observable, Object object) {
			Vector blocks = getModel().getBlocks();
			Vector squares = new Vector();
			for (int i = 0; i < blocks.size(); ++i) {
				Block block = (Block) (blocks.get(i));
				Square square = new Square();
				if (block instanceof Hand) {
					square = new T();
				}
				square.setColor(block.getColor());
				square.setSize(block.getSize());
				square.setName(block.getName());
				Location l = new Location(block.getLocation().getX(), block.getLocation().getY(), block.getLocation().getFill());
				square.setLocation(l);
				squares.add(square);
			}
			getWorld().setSquares(squares);
			getTree().setRoot(mapGoals(getModel().getRoot()));
			ChangeHandler handler = new ChangeHandler();
			SwingUtilities.invokeLater(handler);

		}

		public Node mapGoals(Goal goal) {
			if (goal == null) {
				return null;
			}
			Node node = new Node(goal.getDescription());
			Vector v = goal.getSubgoals();
			for (int i = 0; i < v.size(); ++i) {
				Goal subgoal = (Goal) (v.get(i));
				node.addChild(mapGoals(subgoal));
			}
			return node;
		}



	}

	class ChangeHandler implements Runnable {
		public void run() {
			getView().repaint();
			getTree().revalidate();
			getTree().repaint();
		}
	}

	class MenuListener extends MouseAdapter {
		public void mouseReleased(MouseEvent e) {
			if (isRunning()) {
				return;
			}
			Component component = e.getComponent();
			boolean move = false;
			if (fromItem0.equals(component)) {
				mover = getModel().table;
			}
			else if (fromItem1.equals(component)) {
				mover = getModel().b1;
			}
			else if (fromItem2.equals(component)) {
				mover = getModel().b2;
			}
			else if (fromItem3.equals(component)) {
				mover = getModel().b3;
			}
			else if (fromItem4.equals(component)) {
				mover = getModel().b4;
			}
			else if (fromItem5.equals(component)) {
				mover = getModel().b5;
			}
			else if (fromItem6.equals(component)) {
				mover = getModel().b6;
			}
			else if (fromItem7.equals(component)) {
				mover = getModel().b7;
			}
			else if (fromItem8.equals(component)) {
				mover = getModel().b8;
			}
			else if (toItem0.equals(component)) {
				target = getModel().table;
				move = true;
			}
			else if (toItem1.equals(component)) {
				target = getModel().b1;
				move = true;
			}
			else if (toItem2.equals(component)) {
				target = getModel().b2;
				move = true;
			}
			else if (toItem3.equals(component)) {
				target = getModel().b3;
				move = true;
			}
			else if (toItem4.equals(component)) {
				target = getModel().b4;
				move = true;
			}
			else if (toItem5.equals(component)) {
				target = getModel().b5;
				move = true;
			}
			else if (toItem6.equals(component)) {
				target = getModel().b6;
				move = true;
			}
			else if (toItem7.equals(component)) {
				target = getModel().b7;
				move = true;
			}
			else if (toItem8.equals(component)) {
				target = getModel().b8;
				move = true;
			}
			else if (cellItem.equals(component)) {
				getModel().initializeCellPhone();
			}
			else if (torchItem.equals(component)) {
				getModel().initializeElectricTorch();
			}
			else if (standardItem.equals(component)) {
				getModel().initializeBlocks();
				getModel().initializeOnTable();
			}
			else if (mixingItem.equals(component)) {
				getModel().initializeMartini();
				getModel().initializeOnTable();
			}
			else if (pileItem.equals(component)) {
				getModel().initializeInPile();
			}
			else if (boardItem.equals(component)) {
				getModel().initializeForBoard();
			}

			// Need to wrap in a thread to keep from hanging on mouse click

			if (move == true && mover != null && mover != getModel().table) {
				new PutOnThread(getModel(), mover, target).start();
			}
			else {
				target = null;
			}
		}
	}

	class PutOnThread extends Thread {
		Block from;

		Brick to;

		WorldModel model;

		public PutOnThread(WorldModel model, Block from, Brick to) {
			this.model = model;
			this.from = from;
			this.to = to;
		}

		public void run() {
			setRunning(true);
			model.putOn(from, to, null);
			setRunning(false);
		}

	}

	public static void main(String[] args) {
		BlocksWorldApplication world = new BlocksWorldApplication();
		JFrame frame = new JFrame();
		frame.getContentPane().add(world.getView());
		frame.pack();
		frame.show();
		WorldModel model = world.getModel();
		// world.getModel().moveHand(world.getModel().b1.getTopCenter());
		// world.getModel().hand.setBlock(world.getModel().b1);
		// world.getModel().moveHand(new Location(8, 6));
		// world.getModel().grasp(world.getModel().b1);
		// model.putOn(model.b1, model.b6);
		// model.putOn(model.b2, model.b4, null);
		// model.putOn(model.b4, model.table, null);
		// world.getModel().changed();



	}

	public JPanel getInitializeBar() {
		if (initializeBar == null) {
			assembleControlBars();
		}
		return initializeBar;
	}

	private JTextPane getExplanationPane() {
		if (explanationPane == null) {
			explanationPane = new JTextPane();
			Font font = explanationPane.getFont();
			explanationPane.setFont(new Font(font.getName(), Font.BOLD, 40));
		}
		return explanationPane;
	}

	public JPanel getControlBar() {
//		Mark.say("Getting control bar");
		if (controls == null) {
			controls = new JPanel();
			controls.add(getControlBars());
		}
		return controls;
	}

	public JPanel getChoiceColumn() {
		Mark.say("Calling getChoiceColumn");
		return null;
	}

	public JPanel getChoiceRow() {
		Mark.say("Calling getChoiceColumn");
		return null;
	}

	public JPanel getControlBars() {
		if (controlBar == null) {
			controlBar = new JPanel();
			controlBar.setLayout(new GridLayout(0, 1));
			controlBar.add(getInitializeBar());
			controlBar.add(getFromControlBar());
			controlBar.add(getToControlBar());
			controlBar.add(getQuestion());
		}

		return controlBar;
	}

	public String getAccessType() {
		// TODO Auto-generated method stub
		return null;
	}

	public void restoreTaskBarImage() {
		// TODO Auto-generated method stub
	}

	public void restoreTaskBarTitle() {
		// TODO Auto-generated method stub

	}

}
