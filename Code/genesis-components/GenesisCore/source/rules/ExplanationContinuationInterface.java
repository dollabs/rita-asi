package rules;

import java.util.List;

import frames.entities.Entity;
import frames.entities.Relation;

/*
 * Created on Aug 13, 2015
 * @author phw
 */

public interface ExplanationContinuationInterface {

	public boolean test(Entity elementToBeExplained, Relation rule, List<Relation> previousExplanations);

}
