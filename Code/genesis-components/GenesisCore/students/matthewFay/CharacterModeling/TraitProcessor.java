package matthewFay.CharacterModeling;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Multimap;

import translator.BasicTranslator;
import utils.Mark;
import matthewFay.CharacterModeling.representations.Trait;
import matthewFay.representations.BasicCharacterModel;
import matthewFay.viewers.CharacterViewer;
import connections.AbstractWiredBox;
import connections.Connections;
import connections.signals.BetterSignal;
import frames.entities.Entity;

public class TraitProcessor extends AbstractWiredBox {
	public static Entity isTraitQuestion(Entity element) {

		return null;
	}

	private static TraitProcessor trait_processor = null;

	public static TraitProcessor getTraitProcessor() {
		if (trait_processor == null) trait_processor = new TraitProcessor();
		return trait_processor;
	}

	private Map<String, Trait> traits_by_name = null;

	public Trait getTrait(String trait_name) {
		return traits_by_name.get(trait_name);
	}

	public TraitProcessor() {
		super("Trait processor");

		Connections.getPorts(this).addSignalProcessor("process");

		traits_by_name = new HashMap<>();
	}

	public void process(Object o) {
		if (CharacterViewer.disableCharacterProcessor.isSelected()) {
			return;
		}

		// Verify it's a plot element
		if (!(o instanceof Entity)) return;
		Entity element = (Entity) o;

		if (Trait.isTraitAssignment(element)) {
			doTraitAssignment(element);
		}

	}

	private void doTraitAssignment(Entity element) {
		String trait_name = element.getObject().getType();
		Entity character_entity = element.getSubject();
		BasicCharacterModel character = CharacterProcessor.findBestCharacterModel(character_entity);
		if (character != null) {
			if (!traits_by_name.containsKey(trait_name)) traits_by_name.put(trait_name, new Trait(trait_name));

			boolean positive_example = !element.hasFeature("not");

			// Mark.say("Adding "+character_entity+" as "+positive_example+" example for trait, "+trait_name);
			if (positive_example) {
				traits_by_name.get(trait_name).addPositiveExample(character);
				character.addTrait(traits_by_name.get(trait_name), true);

			}
			else {
				traits_by_name.get(trait_name).addNegativeExample(character);
				character.addTrait(traits_by_name.get(trait_name), false);
			}

			Connections.getPorts(this).transmit(traits_by_name.get(trait_name));
		}
	}
}
