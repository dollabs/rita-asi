package bryanWilliams.goalAnalysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import frames.entities.Entity;

/**
 * This class is used to represent goals mentioned in Genesis rules.
 * Mutable through the addCause and clearCauses methods.
 *
 * @author bryanwilliams
 *
 */
public class Goal {
    // the actual goal
    private final Entity want;
    private final Set<Entity> causes;
    
    public Goal(Entity want, Set<Entity> causes) {
        this.want = want;
        this.causes = new HashSet<>(causes);
    }
    
    public Goal(Entity want, Entity... causes) {
        this(want, new HashSet<>(Arrays.asList(causes)));
    }
    
    public Goal(Entity want) {
        this(want, new HashSet<>());
    }

    public Entity getWant() {
        return want;
    }

    public Set<Entity> getCauses() {
        return new HashSet<>(causes);
    }
    
    public void addCause(Entity cause) {
        causes.add(cause);
    }
    
    public void clearCauses() {
        causes.clear();
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
        Goal other = (Goal) obj;
        if (want == null) {
            if (other.want != null)
                return false;
        } else if (!want.equals(other.want))
            return false;
        return true;
    }
    
    @Override
    public String toString() {
        return "Goal [want=" + want + ", causes=" + causes + "]";
    }
}
