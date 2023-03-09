package matthewFay.Depricated;

import genesis.GenesisControls;
import gui.panels.ParallelJPanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import utils.Mark;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.Connections.NetWireException;
import connections.signals.BetterSignal;
import connections.WiredBox;
import constants.Markers;
import frames.entities.Relation;
import frames.entities.Sequence;

@Deprecated
public class PersonaProcessor extends AbstractWiredBox implements ActionListener {

	public final static String RULE_PORT = "rule port 1";

	public final static String CONCEPT_PORT = "concept port 1";

	public final static String RULE_PORT2 = "rule port 2";

	public final static String CONCEPT_PORT2 = "concept port 2";

	public final static String IDIOM = "start parser";

	public static final String STAGE_DIRECTION = "stage direction 1";

	private HashMap<String, Persona> personas;

	private Persona currentPersona;

	private enum State {
		building, loading, idle, versioning
	};

	private State state = State.idle;

	private String personaToLoad = "";

	public JButton listPersonasButton;

	public JComboBox<String> personasList;

	public JTextField personaNameField;

	public JButton addPersonaButton;

	public JButton deletePersonaButton;

	public JButton loadPersonaButton1;

	public JButton loadPersonaButton2;

	public JButton readMacbethButton;

	public JButton readHamletButton;

	public JButton readEstoniaButton;

	public JButton connectToPersonaButton;

	public JButton savePersonasButton;

	private static ParallelJPanel personaControls = null;

	public JPanel getPersonaControls() {
		if (personaControls == null) {
			personaControls = new ParallelJPanel();

			connectToPersonaButton = new JButton("Connect To Persona Server");
			savePersonasButton = new JButton("Save all Personas");

			listPersonasButton = new JButton("Get Personas List");
			personasList = new JComboBox<String>();
			personasList.setPrototypeDisplayValue("This is a long persona name");

			loadPersonaButton1 = new JButton("Load Persona");

			deletePersonaButton = new JButton("Delete Persona");

			personaControls.addLeft(connectToPersonaButton);
			personaControls.addLeft(savePersonasButton);
			personaControls.addLeft(personasList);
			personaControls.addLeft(listPersonasButton);
			personaControls.addLeft(loadPersonaButton1);
			personaControls.addLeft(deletePersonaButton);

			personaNameField = new JTextField("persona name", 20);
			addPersonaButton = new JButton("Add Persona");

			personaControls.addCenter(personaNameField);
			personaControls.addCenter(addPersonaButton);

			readMacbethButton = new JButton("Read Macbeth");
			readHamletButton = new JButton("Read Hamlet");
			readEstoniaButton = new JButton("Read Estonia");

			personaControls.addRight(readMacbethButton);
		}
		return personaControls;
	}

	public PersonaProcessor() {
		super("Persona Processor");
		Connections.getPorts(this).addSignalProcessor(CONCEPT_PORT, "processConcepts");
		Connections.getPorts(this).addSignalProcessor(RULE_PORT, "processRules");
		Connections.getPorts(this).addSignalProcessor(IDIOM, "processIdiom");
		Connections.getPorts(this).addSignalProcessor(STAGE_DIRECTION, "processStageDirection");

		personas = new HashMap<String, Persona>();
		currentPersona = new Persona();

		state = State.building;

		LocalGenesis.localGenesis().getControls().addTab("Persona", getPersonaControls());

		connectToPersonaButton.setActionCommand("connect to persona");
		connectToPersonaButton.addActionListener(this);

		savePersonasButton.setActionCommand("save all personas");
		savePersonasButton.addActionListener(this);

		addPersonaButton.setActionCommand("store persona");
		addPersonaButton.addActionListener(this);

		listPersonasButton.setActionCommand("get persona list");
		listPersonasButton.addActionListener(this);

		loadPersonaButton1.setActionCommand("load persona");
		loadPersonaButton1.addActionListener(this);

		deletePersonaButton.setActionCommand("delete persona");
		deletePersonaButton.addActionListener(this);

		readMacbethButton.setActionCommand("read macbeth");
		readMacbethButton.addActionListener(this);

	}

	public void addPersona(String name, Persona persona) {
		personas.put(name, persona);
	}

	public void processStageDirection(Object signal) {
		if (signal == Markers.RESET) {
			currentPersona = new Persona();
			state = State.building;
		}
	}

	public void processIdiom(Object signal) {
		// Gets Persona as "Location\Name"
		// Location can be server(implemented) or local(unimplemented)
		String personaLongName = null;
		if (signal instanceof String) {
			personaLongName = (String) signal;
			personaLongName = personaLongName.substring(1, personaLongName.length() - 1);
		}
		if (personaLongName != null) {
			String[] splitName = personaLongName.split("\\\\");
			if (splitName.length == 2) {
				String personaLocation = splitName[0];
				String personaName = splitName[1];
				Mark.say("Location: " + personaLocation);
				Mark.say("Name: " + personaName);
				if (personaLocation.equals("Server")) {
					Mark.say("Sending Request...");
					personaToLoad = personaName;
					state = State.loading;
					Connections.getPorts(this).transmit(new BetterSignal("get", personaToLoad));
				}
				else if (personaLocation.equals("Local")) {
					if (personas.containsKey(personaName)) {
						loadPersona(personas.get(personaName));
					}
					else {
						Mark.say("Persona not found in local pool, falling back to server");
					}
				}
			}
			else {
				// Just persona name, compare server and local versions, get latest//
				personaToLoad = personaLongName;
				if (!personas.containsKey(personaLongName)) {
					state = State.loading;
					Connections.getPorts(this).transmit(new BetterSignal("get", personaToLoad));
				}
				else {
					state = State.versioning;
					Connections.getPorts(this).transmit(new BetterSignal("version", personaLongName));
				}
			}
		}
	}

	public void processConcepts(Object signal) {
		if (signal instanceof Sequence) {
			Sequence concepts = (Sequence) signal;
			if (state == State.building) {
				currentPersona.addConcept((Sequence) concepts.getElement(concepts.getNumberOfChildren() - 1));
			}
		}
	}

	public void processRules(Object signal) {
		if (signal instanceof Sequence) {
			Sequence rules = (Sequence) signal;
			if (state == State.building) {
				currentPersona.addRule((Relation) rules.getElement(rules.getNumberOfChildren() - 1));
			}
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("connect to persona")) {
			try {
				System.out.println("Connecting to Persona");

				WiredBox pub = Connections.subscribe("persona", 3);

				Connections.getPorts(this).addSignalProcessor("processPersona");

				Connections.wire(pub, this);
				Connections.wire(this, pub);
				System.out.println("Connected to Persona");
			}
			catch (NetWireException e1) {
				e1.printStackTrace();
			}
		}
		else if (e.getActionCommand().equals("save all personas")) {
			Connections.getPorts(this).transmit(new BetterSignal("save"));
		}
		else if (e.getActionCommand().equals("store persona")) {
			Mark.say("Sending Persona!");
			currentPersona.setName(personaNameField.getText());
			Connections.getPorts(this).transmit(new BetterSignal("add", currentPersona));
		}
		else if (e.getActionCommand().equals("get persona list")) {
			// Send "list" command to personaPublisher
			personasList.removeAllItems();
			Connections.getPorts(this).transmit(new BetterSignal("list"));
		}
		else if (e.getActionCommand().equals("delete persona")) {
			// Send "delete" command to personaPublisher
			Connections.getPorts(this).transmit(new BetterSignal("delete", personasList.getSelectedItem().toString()));
			personasList.removeAllItems();
			Connections.getPorts(this).transmit(new BetterSignal("list"));
		}
		else if (e.getActionCommand().equals("load persona")) {
			if (personasList.getItemCount() > 0) {
				personaToLoad = personasList.getSelectedItem().toString();
				state = State.loading;
				Connections.getPorts(this).transmit(new BetterSignal("get", personaToLoad));
			}
		}
		else if (e.getActionCommand().equals("read macbeth")) {
			LocalGenesis.localGenesis().getFileSourceReader().readTheWholeStoryWithThread("personaReadMacbeth.txt");
		}
		else if (e.getActionCommand().equals("read estonia")) {
			LocalGenesis.localGenesis().getFileSourceReader().readTheWholeStoryWithThread("personaReadEstonia.txt");
		}
	}

	public void processPersonaSignal(Object sig) {
		BetterSignal signal = BetterSignal.isSignal(sig);
		if (signal == null) return;
		if (signal.get(0, String.class).equals("list")) {
			personasList.addItem(signal.get(1, String.class));
		}
		if (signal.get(0, String.class).equals("version")) {
			String name = signal.get(1, String.class);
			if (name.equals(personaToLoad) && state == State.versioning) {
				int vserver = signal.get(2, Integer.class);
				int vlocal = personas.get(personaToLoad).getVersion();
				if (vlocal >= vserver)
					loadPersona(personas.get(personaToLoad));
				else {
					state = State.loading;
					Connections.getPorts(this).transmit(new BetterSignal("get", personaToLoad));
				}
			}
		}
		if (signal.get(0, String.class).equals("persona")) {
			Persona recievedPersona = signal.get(1, Persona.class);
			if (!personas.containsKey(recievedPersona.getName())) {
				personas.put(recievedPersona.getName(), recievedPersona);
			}
			else {
				Persona myPersona = personas.get(recievedPersona.getName());
				if (myPersona.getVersion() < recievedPersona.getVersion()) {
					personas.put(recievedPersona.getName(), recievedPersona);
				}
			}
			if (recievedPersona.getName().equals(personaToLoad) && state == State.loading) {
				loadPersona(recievedPersona);
			}
		}
	}

	public void loadPersona(Persona persona) {
		currentPersona = persona;
		// currentPersona = new Persona(persona.getRules(),persona.getConcepts());

		Sequence rulesToLoad = currentPersona.getRules();
		Sequence conceptsToLoad = currentPersona.getConcepts();

		System.out.println("Sending Rules: " + rulesToLoad.asString());

		// Still necessary after new functions? Looking like yes//

		LocalGenesis.localGenesis().getFileSourceReader().readTheWholeStoryWithThread("personaLoadCommonSense.txt");

		LocalGenesis.localGenesis();
		LocalGenesis.localGenesis();
		if (GenesisControls.leftButton.isSelected() || GenesisControls.bothButton.isSelected()) {
			Connections.getPorts(this).transmit(PersonaProcessor.RULE_PORT, rulesToLoad);
			Connections.getPorts(this).transmit(PersonaProcessor.CONCEPT_PORT, conceptsToLoad);
		}
		LocalGenesis.localGenesis();
		LocalGenesis.localGenesis();
		if (GenesisControls.rightButton.isSelected() || GenesisControls.bothButton.isSelected()) {
			Connections.getPorts(this).transmit(PersonaProcessor.RULE_PORT2, rulesToLoad);
			Connections.getPorts(this).transmit(PersonaProcessor.CONCEPT_PORT2, conceptsToLoad);
		}

		personaToLoad = "";
		state = State.idle;
	}

}
