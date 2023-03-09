package bryanWilliams.goalAnalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import conceptNet.conceptNetModel.ConceptNetJustification;
import constants.Markers;
import frames.entities.Entity;

/**
 * This class is used to represent a candidate character goal. It's mutable
 * through the setConfirmed() method.
 * 
 * @author bryanwilliams
 */
public class CharacterGoal {
    // e.g. Matt wants to harm YY
    private final Entity want;
    // e.g. Matt is a bully
    private final Entity cause;
    // the justification for connecting the cause of the goal to this goal
    private final List<ConceptNetJustification> causationJustification;
    // whether or not we've received evidence that this candidate character goal is an actual character goal
    private boolean confirmed;
    
    // Unpacks want until finds the contained relation. Warning - will not work on sequences (i.e. a character
    // wanting multiple things)
    public CharacterGoal(Entity cause, Entity want, List<ConceptNetJustification> justification) {
        this.want = want;
        this.cause = cause;
        this.causationJustification = new ArrayList<>(justification);
        if (!isAcceptableWant(want)) {
            throw new IllegalArgumentException(
                    "Error - format of the goal does not match (rel want (ent character) (seq roles (fun object (wanted relation)))): "
                    +want);
        }
    }
        
    public CharacterGoal(Entity cause, Entity want) {
        this(cause, want, Collections.emptyList());
    }
    
    /**
     * Checks if an entity is in the standardized form I require "wants" (goals) to be in - 
     * the entity should be a relation of the want type, should have one element in its object sequence,
     * that element should be a function, and the subject of that function should be a relation.
     * 
     * e.g. "The United States wants to gain land"
     * (rel want (ent united_states-5563) (seq roles (fun object (rel gain (ent united_states-5563) (seq roles (fun object (ent land-5815)))))))
     */
    public static boolean isAcceptableWant(Entity e) {
        return e.relationP() &&
               e.getType().equals(Markers.WANT_MARKER) &&
               e.getObject().sequenceP() && 
               e.getObject().getElements().size() == 1 &&
               e.getObject().getElement(0).functionP() &&
               e.getObject().getElement(0).getSubject().relationP();
    }
    
    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }
        
    public Entity getWant() {
        return want;
    }

    // goals explicitly stated in the story will not have a cause
    public boolean hasCause() {
        return cause != null;
    }
    
    public Entity getCause() {
        return cause;
    }

    // If goal's want is e.g. XX wants to harm YY, wantedAction will be XX harms YY
    public Entity getWantedAction() {
        return want.getObject().getElement(0).getSubject();
    }
    
    public boolean causationUsedConceptNet() {
        return causationJustification.size() > 0;
    }
    
    public List<ConceptNetJustification> getCausationJustification() {
        return causationJustification;
    }
    
    @Override
    public String toString() {
        return "CharacterGoal [want=" + want + ", cause=" + cause + ", confirmed="
                + confirmed + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((want == null) ? 0 : want.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CharacterGoal other = (CharacterGoal) obj;
        if (want == null) {
            if (other.want != null)
                return false;
        } else if (!want.equals(other.want))
            return false;
        return true;
    }
}
