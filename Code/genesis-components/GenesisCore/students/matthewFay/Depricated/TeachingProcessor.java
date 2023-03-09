package matthewFay.Depricated;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import genesis.GenesisControls;
import gui.panels.ParallelJPanel;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import utils.Mark;

import connections.*;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Relation;
import frames.entities.Sequence;

@Deprecated
@SuppressWarnings("unused")
public class TeachingProcessor extends AbstractWiredBox implements ActionListener {
	public static final String CONCEPT_PORT = "reflection port";

	public static final String STORY_PORT = "story port";

	public static final String RULE_PORT = "rule port";

	public static final String RULE1 = "rule1";

	public static final String USED_RULES1 = "used rules1";

	public static final String CONCEPT_PORT2 = "reflection port2";

	public static final String STORY_PORT2 = "story port2";

	public static final String RULE_PORT2 = "rule port2";

	public static final String RULE2 = "rule2";

	public static final String USED_RULES2 = "used rules2";

	public static final String STAGE_DIRECTION = "stage direction";

	public static final String STAGE_DIRECTION2 = "stage direction 2";

	// Teacher is reader slot 2
	private Sequence teacherRules = new Sequence();

	private Sequence teacherReflectiveKnowledge = new Sequence();

	private Sequence teacherStory = new Sequence();

	private Sequence teacherUsedRules = new Sequence();

	// Student is reader slot 1
	private Sequence studentRules = new Sequence();

	private Sequence studentReflectiveKnowledge = new Sequence();

	private Sequence studentStory = new Sequence();

	private Sequence studentUsedRules = new Sequence();

	private boolean preserveKnowledge = true;

	private boolean taughtCommonSenseKnowledge = false;

	private boolean taughtReflexiveKnowledge = false;

	private boolean taughtCommonSenseKnowledge2 = false;

	private boolean taughtReflexiveKnowledge2 = false;

	// Teaching Level is the amount of constraints placed on
	// the amount of knowledge a teacher can teach the student
	// Level 0 - All teacher's commonsense knowledge is taught (does not require knowing student knowledge)
	// Level 1 - All unknown commonsense knowledge is taught
	// Level 2 - All knowledge relavent to story is taught (does not require knowing student knowledge)
	// Level 3 - All unknown commonsense knowledge relevant to story/reflective knowledge is taught
	// private int teachingLevel = 2;
	// 0 - Agent 1 teaches Agent 2
	// 1 - Collaboration, both teach each other.
	// private boolean collaboration = false;

	public TeachingProcessor() {
		super("Teaching processor");
		// Student Stuff
		Connections.getPorts(this).addSignalProcessor(STORY_PORT, "processStory");
		Connections.getPorts(this).addSignalProcessor(CONCEPT_PORT, "processConcepts");
		Connections.getPorts(this).addSignalProcessor(RULE_PORT, "processRules");
		Connections.getPorts(this).addSignalProcessor(USED_RULES1, "processUsedRules");

		// Teacher Stuff
		Connections.getPorts(this).addSignalProcessor(STORY_PORT2, "processStory2");
		Connections.getPorts(this).addSignalProcessor(CONCEPT_PORT2, "processConcepts2");
		Connections.getPorts(this).addSignalProcessor(RULE_PORT2, "processRules2");
		Connections.getPorts(this).addSignalProcessor(USED_RULES2, "processUsedRules2");

		Connections.getPorts(this).addSignalProcessor(STAGE_DIRECTION, "processDirection");
		Connections.getPorts(this).addSignalProcessor(STAGE_DIRECTION2, "processDirection2");

		LocalGenesis.localGenesis().getControls().addTab("Teaching", getTeachingControls());
	}

	private static ParallelJPanel teachingControls = null;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JPanel getTeachingControls() {
		if (teachingControls == null) {
			teachingControls = new ParallelJPanel();

			String[] options = { "Teach Everything", "Teach All Unknown Knowledge*", "Teach all relevant knowledge",
			        "Teach only new relevant knowledge*" };
			LocalGenesis.teachingLevel = new JComboBox(options);
			teachingControls.addLeft(LocalGenesis.teachingLevel);
			teachingControls.addLeft(new JLabel("* Teacher requires information about student knowledge."));
			LocalGenesis.collaboration = new JCheckBoxMenuItem("Enable Collaboration");
			teachingControls.addLeft(LocalGenesis.collaboration);
			JButton startTeaching = new JButton("Begin Teaching");
			startTeaching.setActionCommand("teaching mode");
			startTeaching.addActionListener(this);
			teachingControls.addCenter(startTeaching);
			JButton stopTeaching = new JButton("Stop Teaching");
			stopTeaching.setActionCommand("preparation mode");
			stopTeaching.addActionListener(this);
			teachingControls.addCenter(stopTeaching);
		}
		return teachingControls;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("teaching mode")) {
			// Student Learning Period
			// First need to make sure ready to accept rules
			Mark.say("Beginning New Teaching Pass");
			if (LocalGenesis.teachingLevel.getSelectedIndex() == 0) {
				Relation ruleToSend;
				for (int i = 0; i < teacherRules.getNumberOfChildren(); i++) {
					ruleToSend = (Relation) teacherRules.getElement(i);
					Mark.say("Teaching student: ", ruleToSend.asString());
					// Connections.getPorts(this).transmit(RULE1, ruleToSend);
				}
			}
			if (LocalGenesis.teachingLevel.getSelectedIndex() == 1) {
				boolean newKnowledge = true;
				Relation ruleToSend;
				for (int i = 0; i < teacherRules.getNumberOfChildren(); i++) {
					newKnowledge = true;
					ruleToSend = (Relation) teacherRules.getElement(i);
					for (int j = 0; j < studentRules.getNumberOfChildren(); j++) {
						if (ruleToSend.isEqual(studentRules.getElement(j))) {
							newKnowledge = false;
							break;
						}
					}
					if (newKnowledge) {
						Mark.say("Teaching student: ", ruleToSend.asString());
						// Connections.getPorts(this).transmit(RULE1, ruleToSend);
					}
				}
			}
			if (LocalGenesis.teachingLevel.getSelectedIndex() == 2) {
				Relation ruleToSend;
				for (int i = 0; i < teacherUsedRules.getNumberOfChildren(); i++) {
					ruleToSend = (Relation) teacherUsedRules.getElement(i);
					Mark.say("Teaching student: ", ruleToSend.asString());
					// Connections.getPorts(this).transmit(RULE1, ruleToSend);
				}
			}
			if (LocalGenesis.teachingLevel.getSelectedIndex() == 3) {
				boolean newKnowledge = true;
				Relation ruleToSend;
				for (int i = 0; i < teacherUsedRules.getNumberOfChildren(); i++) {
					newKnowledge = true;
					ruleToSend = (Relation) teacherUsedRules.getElement(i);
					for (int j = 0; j < studentUsedRules.getNumberOfChildren(); j++) {
						if (ruleToSend.isEqual(studentUsedRules.getElement(j))) {
							newKnowledge = false;
							break;
						}
					}
					if (newKnowledge) {
						Mark.say("Teaching student: ", ruleToSend.asString());
						// Connections.getPorts(this).transmit(RULE1, ruleToSend);
					}
				}
			}
			// Disabled Collaboration until new system is working well
			/*
			 * if (readThrough == 2 && LocalGenesis.collaboration.getState()) {
			 * if(LocalGenesis.teachingLevel.getSelectedIndex()==0) { Relation ruleToSend; for (int i=0;
			 * i<studentRules.getNumberOfChildren(); i++) { ruleToSend = (Relation) studentRules.getElement(i); //
			 * Mark.say("Teaching 'teacher': ", ruleToSend.asString()); Connections.getPorts(this).transmit(RULE2,
			 * ruleToSend); } } if(LocalGenesis.teachingLevel.getSelectedIndex()==1) { boolean newKnowledge = true;
			 * Relation ruleToSend; for (int i=0; i<studentRules.getNumberOfChildren(); i++) { newKnowledge = true;
			 * ruleToSend = (Relation) studentRules.getElement(i); for (int j=0; j<teacherRules.getNumberOfChildren();
			 * j++) { if(ruleToSend.isEqual(teacherRules.getElement(j))) { newKnowledge = false; break; } }
			 * if(newKnowledge) { // Mark.say("Teaching 'teacher': ", ruleToSend.asString());
			 * Connections.getPorts(this).transmit(RULE2, ruleToSend); } } }
			 * if(LocalGenesis.teachingLevel.getSelectedIndex()==2) { Relation ruleToSend; for (int i=0;
			 * i<studentUsedRules.getNumberOfChildren(); i++) { ruleToSend = (Relation) studentUsedRules.getElement(i);
			 * // Mark.say("Teaching 'teacher': ", ruleToSend.asString()); Connections.getPorts(this).transmit(RULE2,
			 * ruleToSend); } } if(LocalGenesis.teachingLevel.getSelectedIndex()==3) { boolean newKnowledge = true;
			 * Relation ruleToSend; for (int i=0; i<studentUsedRules.getNumberOfChildren(); i++) { newKnowledge = true;
			 * ruleToSend = (Relation) studentUsedRules.getElement(i); for (int j=0;
			 * j<teacherUsedRules.getNumberOfChildren(); j++) { if(ruleToSend.isEqual(teacherUsedRules.getElement(j))) {
			 * newKnowledge = false; break; } } if(newKnowledge) { // Mark.say("Teaching 'teacher': ",
			 * ruleToSend.asString()); Connections.getPorts(this).transmit(RULE2, ruleToSend); } } } }
			 */
		}
		if (e.getActionCommand().equals("preparation mode")) {
			// readThrough = 1;
		}
	}

	public void processUsedRules(Object signal) {
		if (signal instanceof Entity) {
			Entity rule = (Entity) signal;
			boolean newRule = true;
			for (int j = 0; j < studentUsedRules.getNumberOfChildren(); j++) {
				if (studentUsedRules.getElement(j).isEqual(rule)) newRule = false;
			}
			if (newRule) {
				studentUsedRules.addElement(rule);
				// Mark.say("New Student Rule", rule.asString());
			}
		}
	}

	public void processUsedRules2(Object signal) {
		if (signal instanceof Entity) {
			Entity rule = (Entity) signal;
			boolean newRule = true;
			for (int j = 0; j < teacherUsedRules.getNumberOfChildren(); j++) {
				if (teacherUsedRules.getElement(j).isEqual(rule)) newRule = false;
			}
			if (newRule) {
				teacherUsedRules.addElement(rule);
				// Mark.say("New Teacher Rule: ", rule.asString());
			}
		}
	}

	public void processDirection(Object o) {
		if (o == Markers.RESET) {
			Mark.say("processDirection received direction", o);
			Mark.say("reset");
			// teachingLevel = GenesisControls.teachingLevel.getSelectedIndex();
			// collaboration = GenesisControls.collaboration.getState();
			if (!preserveKnowledge) {

				teacherRules = new Sequence();
				studentRules = new Sequence();

				teacherReflectiveKnowledge = new Sequence();
				studentReflectiveKnowledge = new Sequence();

				teacherStory = new Sequence();
				studentStory = new Sequence();

				teacherUsedRules = new Sequence();
				studentUsedRules = new Sequence();

				taughtCommonSenseKnowledge = false;
				taughtReflexiveKnowledge = false;

				taughtCommonSenseKnowledge2 = false;
				taughtReflexiveKnowledge2 = false;
			}
		}
	}

	public void processDirection2(Object o) {
		if (o == Markers.RESET) {
			Mark.say("processDirection2 received direction", o);
		}
	}

	public void processStory(Object signal) {
		if (signal instanceof Entity) {
			Entity t = (Entity) signal;
			// Mark.say("Process story received from Student", t.asStringWithNames());
			if (signal instanceof Sequence) {
				Sequence seq = (Sequence) signal;
				studentStory.addAll(seq);
			}
		}
	}

	public void processConcepts(Object signal) {
		if (signal instanceof Entity) {
			Entity t = (Entity) signal;
			// Mark.say("Process concepts received from Student", t.asString());
			if (signal instanceof Sequence) {
				Sequence seq = (Sequence) signal;
				studentReflectiveKnowledge.addElement(seq.getElement(seq.getNumberOfChildren() - 1));

			}
		}
	}

	public void processStory2(Object signal) {
		if (signal instanceof Entity) {
			Entity t = (Entity) signal;
			// Mark.say("Process story received from Teacher", t.asStringWithNames());
			if (signal instanceof Sequence) {
				Sequence seq = (Sequence) signal;
				teacherStory.addAll(seq);
			}
		}
	}

	public void processConcepts2(Object signal) {
		if (signal instanceof Entity) {
			Entity t = (Entity) signal;
			// Mark.say("Process concepts received from Teacher", t.asString());
			if (signal instanceof Sequence) {
				Sequence seq = (Sequence) signal;
				teacherReflectiveKnowledge.addElement(seq.getElement(seq.getNumberOfChildren() - 1));
			}
		}
	}

	public void processRules(Object signal) {
		if (signal instanceof Entity) {
			Entity t = (Entity) signal;
			// Mark.say("Process rules received from Student", t.asString());
			if (signal instanceof Sequence) {
				Sequence rules = (Sequence) signal;
				Relation newRule = (Relation) rules.getElement(rules.getNumberOfChildren() - 1);
				if (!studentRules.containsDeprecated(newRule)) studentRules.addElement(rules.getElement(rules.getNumberOfChildren() - 1));
			}
			if (!taughtCommonSenseKnowledge) {
				taughtCommonSenseKnowledge = true;
				if (LocalGenesis.teachingLevel.getSelectedIndex() == 0) {
					Relation ruleToSend;
					for (int i = 0; i < teacherRules.getNumberOfChildren(); i++) {
						ruleToSend = (Relation) teacherRules.getElement(i);
						Mark.say("Teaching student: ", ruleToSend.asString());
						Connections.getPorts(this).transmit(RULE1, ruleToSend);
					}
				}
				if (LocalGenesis.teachingLevel.getSelectedIndex() == 1) {
					boolean newKnowledge = true;
					Relation ruleToSend;
					for (int i = 0; i < teacherRules.getNumberOfChildren(); i++) {
						newKnowledge = true;
						ruleToSend = (Relation) teacherRules.getElement(i);
						for (int j = 0; j < studentRules.getNumberOfChildren(); j++) {
							if (ruleToSend.isEqual(studentRules.getElement(j))) {
								newKnowledge = false;
								break;
							}
						}
						if (newKnowledge) {
							Mark.say("Teaching student: ", ruleToSend.asString());
							Connections.getPorts(this).transmit(RULE1, ruleToSend);
						}
					}
				}
				if (LocalGenesis.teachingLevel.getSelectedIndex() == 2) {
					Relation ruleToSend;
					for (int i = 0; i < teacherUsedRules.getNumberOfChildren(); i++) {
						ruleToSend = (Relation) teacherUsedRules.getElement(i);
						Mark.say("Teaching student: ", ruleToSend.asString());
						Connections.getPorts(this).transmit(RULE1, ruleToSend);
					}
				}
				if (LocalGenesis.teachingLevel.getSelectedIndex() == 3) {
					boolean newKnowledge = true;
					Relation ruleToSend;
					for (int i = 0; i < teacherUsedRules.getNumberOfChildren(); i++) {
						newKnowledge = true;
						ruleToSend = (Relation) teacherUsedRules.getElement(i);
						for (int j = 0; j < studentUsedRules.getNumberOfChildren(); j++) {
							if (ruleToSend.isEqual(studentUsedRules.getElement(j))) {
								newKnowledge = false;
								break;
							}
						}
						if (newKnowledge) {
							Mark.say("Teaching student: ", ruleToSend.asString());
							Connections.getPorts(this).transmit(RULE1, ruleToSend);
						}
					}
				}
			}
		}
	}

	public void processRules2(Object signal) {
		if (signal instanceof Entity) {
			Entity t = (Entity) signal;
			// Mark.say("Process rules received from Teacher", t.asString());
			if (signal instanceof Sequence) {
				Sequence rules = (Sequence) signal;
				teacherRules.addElement(rules.getElement(rules.getNumberOfChildren() - 1));
			}
			if (GenesisControls.collaboration.getState()) {
				if (!taughtCommonSenseKnowledge2) {
					taughtCommonSenseKnowledge2 = true;
					if (LocalGenesis.teachingLevel.getSelectedIndex() == 0) {
						Relation ruleToSend;
						for (int i = 0; i < studentRules.getNumberOfChildren(); i++) {
							ruleToSend = (Relation) studentRules.getElement(i);
							// Mark.say("Teaching 'teacher': ", ruleToSend.asString());
							Connections.getPorts(this).transmit(RULE2, ruleToSend);
						}
					}
					if (LocalGenesis.teachingLevel.getSelectedIndex() == 1) {
						boolean newKnowledge = true;
						Relation ruleToSend;
						for (int i = 0; i < studentRules.getNumberOfChildren(); i++) {
							newKnowledge = true;
							ruleToSend = (Relation) studentRules.getElement(i);
							for (int j = 0; j < teacherRules.getNumberOfChildren(); j++) {
								if (ruleToSend.isEqual(teacherRules.getElement(j))) {
									newKnowledge = false;
									break;
								}
							}
							if (newKnowledge) {
								// Mark.say("Teaching 'teacher': ", ruleToSend.asString());
								Connections.getPorts(this).transmit(RULE2, ruleToSend);
							}
						}
					}
					if (LocalGenesis.teachingLevel.getSelectedIndex() == 2) {
						Relation ruleToSend;
						for (int i = 0; i < studentUsedRules.getNumberOfChildren(); i++) {
							ruleToSend = (Relation) studentUsedRules.getElement(i);
							// Mark.say("Teaching 'teacher': ", ruleToSend.asString());
							Connections.getPorts(this).transmit(RULE2, ruleToSend);
						}
					}
					if (LocalGenesis.teachingLevel.getSelectedIndex() == 3) {
						boolean newKnowledge = true;
						Relation ruleToSend;
						for (int i = 0; i < studentUsedRules.getNumberOfChildren(); i++) {
							newKnowledge = true;
							ruleToSend = (Relation) studentUsedRules.getElement(i);
							for (int j = 0; j < teacherUsedRules.getNumberOfChildren(); j++) {
								if (ruleToSend.isEqual(teacherUsedRules.getElement(j))) {
									newKnowledge = false;
									break;
								}
							}
							if (newKnowledge) {
								// Mark.say("Teaching 'teacher': ", ruleToSend.asString());
								Connections.getPorts(this).transmit(RULE2, ruleToSend);
							}
						}
					}
				}
			}
		}
	}

}
