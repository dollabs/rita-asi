package dylanHolmes;

import java.util.ArrayList;

import frames.entities.Entity;
import frames.entities.Sequence;
import utils.Mark;
import matchers.StandardMatcher;


public class Goal {

	public String name;
	public Boolean preventative = false;
	public Entity end;
	public Entity prereqs;
	public Entity means;
	public utils.minilisp.LList<utils.PairOfEntities> bindings;
	public String successful; // e.g. explicitly confirmed, probable, etc.
	
	public String toString() {
		return "Goal@"+this.name+"{\n\t"+this.bindings+",\n\t"+this.end.asString()+",\n\t"+this.means.asString()+",\n\t"+this.prereqs.asString()+"}\n";
	}
	
	public static Entity pairWithNull(Entity e) {
		return new frames.entities.Function("unmatched",e);
	}
	
	public static Entity pairWithMatched(Entity e) {
		return new frames.entities.Function("matched",e.getSubject());
	}
		
	
	public static Entity reTag(String s, Entity e) {
		e.addType(s);
		return e;
		// immutable version:
		// return new bridge.reps.entities.Function(s,e.getSubject());
	}
	
	public Goal emptyMatches() {
		// return a copy of this goal in which each entity is marked as unmatched
		Entity tmp_end = Goal.pairWithNull(this.end);
		Entity tmp_prereqs = new Sequence();
		for(Entity e : this.prereqs.getElements()) {
			tmp_prereqs.addElement(Goal.pairWithNull(e));
		}
		Entity tmp_means = Goal.pairWithNull(this.means);
		
		return new Goal(this.name, tmp_end, tmp_prereqs, tmp_means);
	}
	
	
	public Entity match(Entity e) {
		ArrayList<Entity> candidates = new ArrayList<Entity>();
		candidates.add(this.means);
		candidates.addAll(this.prereqs.getElements());

		for(Entity c : candidates) {
			// assume all entities are tagged with matched/unmatched
			if(c.getType() == "unmatched") {
				//Mark.say(e.toXML());
				//Mark.say(c.getSubject().toXML());
				//Mark.say(e.toString());
				//Mark.say(c.getSubject().toString());
			
				// TODO: make the match based on the previous bindings (this.bindings)
				//Mark.say(this.bindings);
				utils.minilisp.LList<utils.PairOfEntities> bindings = StandardMatcher.getBasicMatcher().match(e, c.getSubject());
				if(bindings != null) {
					this.bindings = bindings;
					c = Goal.reTag("matched", c);
					return c;
				}
				
				//Mark.say(bindings);
				//Mark.say(0);
			}
			
		}
		return null;
	}
	
	
	public Goal(Entity goal, Entity prereqs, Entity means) {
		this.end = goal;
		this.prereqs = prereqs;
		this.means = means;
	}
	public Goal(String name, Entity goal, Entity prereqs, Entity means) {
		this.name = name;
		this.end = goal;
		this.prereqs = prereqs;
		this.means = means;
	}

	public static boolean containsNamedGoal(ArrayList<Goal> alts, String s) {
		for(Goal g : alts) {
			if(g.name == s){return true;}
		}
		return false;
	}
	
	
	public Entity asEntity() {
		// This will eventually return a Goal, represented in a standard Entity form.
		return null;
	}

}
