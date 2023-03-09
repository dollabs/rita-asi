package subsystems.rashi;

import frames.entities.Entity;

public interface IntentionsProcessor {
	
	public boolean isConceptType(Entity concept);
	
	public void processConcept(Entity concept);
	
	public void getConclusions();
	
	

}
