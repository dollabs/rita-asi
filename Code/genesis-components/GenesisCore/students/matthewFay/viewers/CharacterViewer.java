package matthewFay.viewers;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import utils.Mark;
import utils.PairOfEntities;
import utils.minilisp.LList;
import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.CharacterModeling.representations.Trait;
import matthewFay.StoryAlignment.Aligner;
import matthewFay.StoryAlignment.SequenceAlignment;
import matthewFay.StoryAlignment.SortableAlignmentList;
import matthewFay.StoryGeneration.PlotWeaver;
import matthewFay.Utilities.DefaultHashMap;
import matthewFay.Utilities.Generalizer;
import matthewFay.Utilities.HashMatrix;
import matthewFay.Utilities.Pair;
import matthewFay.representations.BasicCharacterModel;
import matthewFay.viewers.MatrixGridViewer.MatrixClickEvent;
import connections.Connections;
import connections.WiredBox;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Sequence;
import gui.panels.ParallelJPanel;

public class CharacterViewer extends JPanel implements WiredBox, ActionListener, MatrixGridViewer.MatrixClickListener {

	public static String TRAIT = "trait port";
	
	private JTabbedPane tabbedPane;
	
	private JPanel characterAlignmentComparePanel;
	private JPanel characterVectorComparePanel;
	
	private JPanel characterAlignmentDetailsPanel;
	
	private ParallelJPanel controlPanel;
	private JButton compareCharactersAlignmentButton = new JButton("Compare Characters via Alignment");
	private JButton compareCharactersVectorButton = new JButton("Compare Characters via Event Vectors");
	private JButton saveCharacterDocument = new JButton("Save Character stories");
	private JButton clearCharactersButton = new JButton("Clear Character Library");
	private JButton weaveCharactersButton = new JButton("Weave all Characters");
	
	private JButton expLearnTraitButton = new JButton("Learn vicious trait");
	
	public static JCheckBox disableCharacterProcessor = new JCheckBox("Disable Character Processor", false);
	private JCheckBox constrainVectorGeneralization = new JCheckBox("Constrain Event Generalization", false);
	public static JCheckBox trackOnlyExplicitEvents = new JCheckBox("Track only explicit events",true);
	
	private JLabel characterCountLabel = new JLabel("0 Characters in Library");
	
	public String getName() {
		return "Character Viewer";
	}
	
	@Override
	public void paint(Graphics g) {
		characterCountLabel.setText(CharacterProcessor.getCharacterLibrary().size()+" Characters in Library");
		super.paint(g);
	}
	
	public CharacterViewer() {
		super(new BorderLayout());
		
		tabbedPane = new JTabbedPane();
		
		controlPanel = new ParallelJPanel();
		controlPanel.addLeft(compareCharactersAlignmentButton);
		controlPanel.addLeft(compareCharactersVectorButton);
		controlPanel.addLeft(expLearnTraitButton);
		
		controlPanel.addCenter(saveCharacterDocument);
		controlPanel.addCenter(constrainVectorGeneralization);
		controlPanel.addCenter(disableCharacterProcessor);
		controlPanel.addCenter(trackOnlyExplicitEvents);
		controlPanel.addRight(clearCharactersButton);
		controlPanel.addRight(characterCountLabel);
		
		tabbedPane.add("Controls",controlPanel);
		
		controlPanel.addLeft(weaveCharactersButton);
		
		characterAlignmentComparePanel = new JPanel();
		tabbedPane.add("Alignment Comparison",characterAlignmentComparePanel);
		characterVectorComparePanel = new JPanel();
		tabbedPane.add("Vector Comparison",characterVectorComparePanel);
		characterAlignmentDetailsPanel = new JPanel(new BorderLayout());
		tabbedPane.add("Alignment Details", characterAlignmentDetailsPanel);
		
		compareCharactersAlignmentButton.setActionCommand("compare_align");
		compareCharactersAlignmentButton.addActionListener(this);
		compareCharactersVectorButton.setActionCommand("compare_vector");
		compareCharactersVectorButton.addActionListener(this);
		
		expLearnTraitButton.setActionCommand("exp_learn_trait");
		expLearnTraitButton.addActionListener(this);
		
		saveCharacterDocument.setActionCommand("save_characters");
		saveCharacterDocument.addActionListener(this);
		clearCharactersButton.setActionCommand("clear_library");
		clearCharactersButton.addActionListener(this);
		
		weaveCharactersButton.setActionCommand("weave_characters");
		weaveCharactersButton.addActionListener(this);
		
		this.add(tabbedPane);
	}

	private void doCompareCharactersAlignment() {
		HashMatrix<String, String, Float> comparison_scores = new HashMatrix<>();
		Aligner aligner = new Aligner();
		for(BasicCharacterModel c1 : CharacterProcessor.getCharacterLibrary().values()) {
			for(BasicCharacterModel c2 : CharacterProcessor.getCharacterLibrary().values()) {
				if(!comparison_scores.contains(c1.getSimpleName(),c2.getSimpleName()) ) {
					LList<PairOfEntities> bindings = new LList<>();
					bindings = bindings.cons(new PairOfEntities(c1.getEntity(),c2.getEntity()));
					SortableAlignmentList alignments = aligner.align(c1.getParticipantEvents(), c2.getParticipantEvents(),bindings);
					if(!alignments.isEmpty()) {
						float adjusted_score = alignments.get(0).getMatchCount();
						comparison_scores.put(c1.getSimpleName(), c2.getSimpleName(), adjusted_score);
						comparison_scores.put(c2.getSimpleName(), c1.getSimpleName(), adjusted_score);
					}
				}
			}
		}
		tabbedPane.remove(characterAlignmentComparePanel);
		characterAlignmentComparePanel = new MatrixGridViewer(comparison_scores);
		((MatrixGridViewer)characterAlignmentComparePanel).addMatrixClickListener(this);
		tabbedPane.add("Alignment Comparison", characterAlignmentComparePanel);
		tabbedPane.setSelectedComponent(characterAlignmentComparePanel);
	}
	
	private void doCompareCharactersVector() {
		HashMatrix<String, String, Float> comparison_scores = new HashMatrix<>();
		for(Entity e1 : CharacterProcessor.getCharacterLibrary().keySet()) {
			BasicCharacterModel c1 = CharacterProcessor.getCharacterLibrary().get(e1);
			for(Entity e2 : CharacterProcessor.getCharacterLibrary().keySet()) {
				BasicCharacterModel c2 = CharacterProcessor.getCharacterLibrary().get(e2);
				if(!comparison_scores.contains(c1.getSimpleName(),c2.getSimpleName()) ) {
					float adjusted_score = 0;
					//Do event vector comparisons
					if(constrainVectorGeneralization.isSelected()) {
						adjusted_score = cosine(c1.getSemiGeneralizedEventCounts(),c2.getSemiGeneralizedEventCounts());
					} else {
						adjusted_score = cosine(c1.getGeneralizedEventCounts(),c2.getGeneralizedEventCounts());
					}
					comparison_scores.put(c1.getSimpleName(), c2.getSimpleName(), adjusted_score);
					comparison_scores.put(c2.getSimpleName(), c1.getSimpleName(), adjusted_score);
				}
			}
		}
		tabbedPane.remove(characterVectorComparePanel);
		characterVectorComparePanel = new MatrixGridViewer(comparison_scores,true);
		tabbedPane.add("Vector Comparison", characterVectorComparePanel);
		tabbedPane.setSelectedComponent(characterVectorComparePanel);
	}
	
	private float cosine(DefaultHashMap<String, Integer> a, DefaultHashMap<String, Integer> b) {
		Set<String> dimensions = new HashSet<String>();
		for(String key : a.keySet())
			dimensions.add(key);
		for(String key : b.keySet())
			dimensions.add(key);
		float cosine = 0.0f;
		float absum = 0.0f;
		float aasum = 0.0f;
		float bbsum = 0.0f;
		for(String dimension : dimensions) {
			absum += a.get(dimension)*b.get(dimension);
			aasum += a.get(dimension)*a.get(dimension);
			bbsum += b.get(dimension)*b.get(dimension);
		}
		cosine = (absum*absum) / (aasum*bbsum);
		return cosine;
	}
	
	private void saveCharacterStories() {
		String character_list = "characters = [";
		String action_sets = "action_sets = [";
		ArrayList<BasicCharacterModel> characters = new ArrayList<>(CharacterProcessor.getCharacterLibrary().values());
		for(BasicCharacterModel character : characters) {
			character_list += "\""+character.getEntity()+"\"";
			action_sets += "\"";
			for(Entity event : character.getParticipantEvents()) {
				String event_string = Generalizer.generalize(event, character.getEntity()).asString().replace(" ","_");
				if(constrainVectorGeneralization.isSelected()) {
					event_string = Generalizer.generalize(event, character.getEntity(), CharacterProcessor.getCharacterLibrary().keySet()).asString().replace(" ","_");
				}
				action_sets += event_string+" ";
			}
			action_sets += "\"";
			if(characters.indexOf(character) != characters.size()-1) {
				character_list += ",\n\t";
				action_sets += ",\n\t";
			}
		}
		character_list += "]\n";
		action_sets += "]\n";
		Mark.say(character_list);
		Mark.say(action_sets);
	}
	
	public void doWeave() {
		Map<Entity, BasicCharacterModel> cms = CharacterProcessor.getCharacterLibrary();
		List<BasicCharacterModel> characters = new ArrayList<>(cms.values());
		
		PlotWeaver weaver = new PlotWeaver(characters);
		List<Entity> woven_plot = weaver.weavePlots();
		
		Mark.say("Woven plot:");
		for(Entity plot_elt : woven_plot) {
			Mark.say(plot_elt.toEnglish());
		}
	}
	
	public void expLearnTrait() {
		BasicCharacterModel macbeth = CharacterProcessor.findBestCharacterModel("macbeth");
		BasicCharacterModel claudius= CharacterProcessor.findBestCharacterModel("claudius");
		BasicCharacterModel duncan = CharacterProcessor.findBestCharacterModel("duncan");
		
		if(macbeth == null || claudius == null || duncan == null) {
			Mark.err("Cannot proceeed, macbeth, claudius, or duncan not found.");
			return;
		}
		
		//First macbeth and claudius are vicious, so find initial definition//
		Mark.say("Trait Learning Demo: Vicious");
		Mark.say("---------");
		Mark.say("Macbeth and Claudius are vicious");
		Trait vicious = new Trait("Vicious");
		vicious.addPositiveExample(macbeth);
		vicious.addPositiveExample(claudius);
		
		Mark.say("Current vicious trait:");
		for(Entity e : vicious.getElements()) {
			Mark.say(e);
		}
		
		Mark.say("Duncan is not vicious");
		vicious.addNegativeExample(duncan);
		
		Mark.say("Final vicious trait:");
		for(Entity e : vicious.getElements()) {
			Mark.say(e);
		}
		
		TraitViewer.getTraitViewer().addTrait(vicious);
		
//		Set<Entity> elts = new HashSet<>(vicious.getElements());
//		Sequence seq = new Sequence();
//		for(Entity e : vicious.getElements())
//			seq.addElement(e);
//		Connections.getPorts(this).transmit(TRAIT,new BetterSignal(elts, seq));
	}
	
	@Override
	public void actionPerformed(ActionEvent action) {
		// TODO Auto-generated method stub
		String command = action.getActionCommand();
		if(command.equals("compare_align")) {
			doCompareCharactersAlignment();
		} else if(command.equals("compare_vector")) {
			doCompareCharactersVector();
		} else if(command.equals("save_characters")) {
			saveCharacterStories();
		} else if(command.equals("clear_library")) {
			CharacterProcessor.getCharacterLibrary().clear();
			characterCountLabel.setText(CharacterProcessor.getCharacterLibrary().size()+" Characters in Library");
		} else if(command.equals("weave_characters")) {
			doWeave();
		} else if(command.equals("exp_learn_trait")) {
			expLearnTrait();
		}
	}

	
	private void doShowCharacterAlignmentDetails(String x_name, String y_name) {
		characterAlignmentDetailsPanel.removeAll();
		
		BasicCharacterModel character1 = null;
		BasicCharacterModel character2 = null;
		for(BasicCharacterModel c1 : CharacterProcessor.getCharacterLibrary().values()) {
			if(c1.getSimpleName().equals(x_name)) {
				character1 = c1;
				break;
			}
		}
		for(BasicCharacterModel c2 : CharacterProcessor.getCharacterLibrary().values()) {
			if(c2.getSimpleName().equals(y_name)) {
				character2 = c2;
				break;
			}
		}
		
		if(character1 != null && character2 != null) {
			Mark.say("Found: "+character1.getSimpleName()+" "+character2.getSimpleName());
			
			Aligner aligner = new Aligner();
			LList<PairOfEntities> bindings = new LList<>();
			bindings = bindings.cons(new PairOfEntities(character1.getEntity(),character2.getEntity()));
			SortableAlignmentList alignments = aligner.align(character1.getParticipantEvents(), character2.getParticipantEvents(),bindings);
			if(!alignments.isEmpty()) {
				SequenceAlignment sa = (SequenceAlignment)alignments.get(0);
				characterAlignmentDetailsPanel.add(AlignmentViewer.generateTable(sa,true), BorderLayout.CENTER);
			}
			tabbedPane.setSelectedComponent(characterAlignmentDetailsPanel);
		}
	}
	
	@Override
	public void handleMatrixClickEvent(MatrixClickEvent e) {
		// TODO Auto-generated method stub
		doShowCharacterAlignmentDetails(e.x_name, e.y_name);
	}
	
}
