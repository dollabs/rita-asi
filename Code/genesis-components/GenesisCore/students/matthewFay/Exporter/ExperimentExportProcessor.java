package matthewFay.Exporter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.CharacterModeling.representations.Trait;
import matthewFay.Utilities.Generalizer;
import matthewFay.representations.BasicCharacterModel;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import storyProcessor.StoryProcessor;
import utils.Mark;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.WiredBox;
import connections.signals.BetterSignal;
import frames.entities.Entity;
import frames.entities.Sequence;

public class ExperimentExportProcessor extends JPanel implements WiredBox, ActionListener {
	private static ExperimentExportProcessor eep = null;

	public static ExperimentExportProcessor getExperimentExportProcessor() {
		if (eep == null) eep = new ExperimentExportProcessor();
		return eep;
	}

	private JButton exportStoriesButton = new JButton("Export Stories with Cast and Traits (JSON)");
	private JButton exportCharactersButton = new JButton("Export Genericized Character Stoires (JSON)");

	public String getName() {
		return "Export Processor";
	}

	public ExperimentExportProcessor() {
		super(new BorderLayout());
		// GUI Stuff
		exportStoriesButton.addActionListener(this);
		exportStoriesButton.setActionCommand("stories");
		this.add(exportStoriesButton, BorderLayout.PAGE_START);
		
		exportCharactersButton.addActionListener(this);
		exportCharactersButton.setActionCommand("characters");
		this.add(exportCharactersButton, BorderLayout.CENTER);

		// Wire stuff
		setName("Export Processor");
		Connections.getPorts(this).addSignalProcessor(StoryProcessor.COMPLETE_STORY_ANALYSIS_PORT, "processStory");

		signals = new ArrayList<>();
		character_map = HashMultimap.create();
		story_map = new LinkedHashMap<>();
	}

	List<BetterSignal> signals;

	Multimap<String, BasicCharacterModel> character_map;

	Map<String, BetterSignal> story_map;

	public void processStory(Object o) {
		BetterSignal signal = BetterSignal.isSignal(o);
		if (signal == null) return;

		Sequence story = signal.get(0, Sequence.class);
		Sequence explicitElements = signal.get(1, Sequence.class);
		Sequence inferences = signal.get(2, Sequence.class);
		Sequence concepts = signal.get(3, Sequence.class);

		String title = story.getType();

		signals.add(signal);
		List<BasicCharacterModel> characters = CharacterProcessor.getActiveCharacters();
		character_map.putAll(title, characters);
		story_map.put(title, signal);
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		if(actionEvent.getActionCommand().equals("stories")) {
			exportStories();
		}
		if(actionEvent.getActionCommand().equals("characters")) {
			exportCharacters();
		}
	}
	
	public void exportStories() {
		Mark.say("Exporting...");
		// parent component of the dialog

		JFrame parentFrame = new JFrame();

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify a file to save");
		fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Data", "json"));

		int userSelection = fileChooser.showSaveDialog(parentFrame);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			Mark.say("Save as file: " + fileToSave.getAbsolutePath());

			try {
				BufferedWriter out = null;
				FileWriter fstream = new FileWriter(fileToSave);
				out = new BufferedWriter(fstream);

				out.write("{\n");

				out.write("\t\"stories\": [\n");

				boolean firstStory = true;
				for (String title : story_map.keySet()) {
					BetterSignal signal = story_map.get(title);
					// Manage commas between stories
					if (!firstStory) out.write(",\n");

					// Open story dict
					out.write("\t\t{\n");

					Sequence story = signal.get(0, Sequence.class);

					// Write Story Title
					out.write("\t\t\t\"title\":\"" + title + "\",\n");

					// Open Characters Array
					out.write("\t\t\t\"characters\": [\n");
					Collection<BasicCharacterModel> characters = character_map.get(title);

					boolean firstCharacter = true;
					for (BasicCharacterModel character : characters) {
						if (!firstCharacter) out.write(",\n");
						// Open character dict
						out.write("\t\t\t\t{ ");
						out.write("\"name\":\"" + character.getSimpleName() + "\", ");

						// Open Traits Array
						out.write("\"traits\": [");

						boolean firstTrait = true;
						for (Trait trait : character.getTraits(true)) {
							if (!firstTrait) out.write(", ");

							out.write("\"" + trait.getName() + "\"");
							firstTrait = false;
						}

						// Close Traits Array
						out.write("]");

						// Close character dict
						out.write("}");
						firstCharacter = false;
					}
					// Close characters array
					out.write("\n\t\t\t],\n");

					// Open Story Text array
					out.write("\t\t\t\"text\": [\n");

					Sequence explicitElements = signal.get(1, Sequence.class);
					boolean firstElt = true;
					for (Entity e : explicitElements.getAllComponents()) {
						// Manage commas between elts
						if (!firstElt) out.write(",\n");

						String english = e.toEnglish();

						// Write story line
						out.write("\t\t\t\t\"" + english + "\"");

						firstElt = false;
					}
					// Close Story Text Array
					out.write("\n\t\t\t]\n");

					// Close Story dict
					out.write("\t\t}");
					firstStory = false;
				}

				out.write("\n\t]\n");

				out.write("}");

				if (out != null) {
					out.close();
				}
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void exportCharacters() {
		Mark.say("Exporting...");
		// parent component of the dialog

		JFrame parentFrame = new JFrame();

		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Specify a file to save");
		fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Data", "json"));

		int userSelection = fileChooser.showSaveDialog(parentFrame);

		if (userSelection == JFileChooser.APPROVE_OPTION) {
			File fileToSave = fileChooser.getSelectedFile();
			Mark.say("Save as file: " + fileToSave.getAbsolutePath());

			try {
				BufferedWriter out = null;
				FileWriter fstream = new FileWriter(fileToSave);
				out = new BufferedWriter(fstream);

				out.write("{\n");

				out.write("\t\"characters\": [\n");

				boolean firstCharacter = true;
				for(BasicCharacterModel character : character_map.values()) {
					//Manage commas between characters
					if(!firstCharacter) out.write(",\n");
					
					//Open character dict
					out.write("\t\t{\n");
					
					//Write Character Name
					out.write("\t\t\t\"name\":\""+character.getSimpleName()+"\",\n");
					
					//Open plot array
					out.write("\t\t\t\"plot\": [\n");
					
					boolean firstPlotElt = true;
					for(Entity plotElt : character.getParticipantEvents()) {
						if (!firstPlotElt) out.write(",\n");
						String semi_generalized_event = Generalizer.generalize(plotElt, character.getEntity(), CharacterProcessor.getCharacterLibrary().keySet()).toString();
						out.write("\t\t\t\t\""+semi_generalized_event+"\"");
						firstPlotElt = false;
					}
					
					//Close plot array
					out.write("\n\t\t\t]");
					
					//Close character dict
					out.write("\n\t\t}");
					
					firstCharacter = false;
				}
				
				//Close json
				out.write("\n\t]\n}");

				if (out != null) {
					out.close();
				}
			}
			catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
