package matthewFay.representations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import frames.entities.Entity;
import matthewFay.CharacterModeling.CharacterProcessor;
import matthewFay.Utilities.EntityHelper;
import matthewFay.Utilities.HashMatrix;

/**
 * The RelationGraph class is used to represent all the
 * relationships between characters over the course of
 * a story.  Currently relationship is used broadly and includes
 * things like "Macbeth killed Duncan."
 * 
 * Todo: Implementation, Visualization, proper handling of state effects
 * 
 * @author Matthew
 *
 */
public class RelationGraph {
	private HashMatrix<BasicCharacterModel, BasicCharacterModel, List<Entity>> adjacency_matrix;
	
	public RelationGraph() {
		adjacency_matrix = new HashMatrix<>();
	}
	
	public void addEvent(Entity event) {
		Set<BasicCharacterModel> character_set = new HashSet<>();
		List<Entity> entities = EntityHelper.getAllEntities(event);
		for(Entity entity : entities) {
			if(CharacterProcessor.isCharacter(entity)) {
				character_set.add(CharacterProcessor.getCharacterModel(entity, true));
			}
		}
		if(character_set.size() == 2) {
			List<BasicCharacterModel> character_list = new ArrayList<>(character_set);
			BasicCharacterModel c1 = character_list.get(0);
			BasicCharacterModel c2 = character_list.get(1);
			List<Entity> elts;
			if(!adjacency_matrix.contains(c1, c2)) {
				elts = new ArrayList<Entity>();
				adjacency_matrix.put(c1, c2, elts);
				adjacency_matrix.put(c2, c1, elts);
			} else {
				elts = adjacency_matrix.get(c1, c2);
			}
			elts.add(event);
		}
		//Not a binary relation, unhandled...//
		return;
	}
}
