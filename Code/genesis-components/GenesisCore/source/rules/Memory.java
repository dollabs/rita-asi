package rules;

import java.util.Comparator;

import constants.Markers;
import frames.entities.Relation;
import mentalModels.MentalModel;
import utils.Mark;

public class Memory implements Comparable<Memory> {

	int position;

	String type;

	String name;

	Relation rule;
	
	/** Introduced by PHW to fix code rot
	 */
	public void setHost(MentalModel host) {
		// Mark.say("Attaching", host, "to", rule);
		rule.addProperty(Markers.MENTAL_MODEL_HOST, host);
	}

	public Memory(int position, String type, String name, Relation rule) {
		this(position, type, rule);
		this.name = name;
	}

	public Memory(int position, String type, Relation rule) {
		super();
		// Mark.say("Adding", type, "at", position, rule);
		this.position = position;
		this.type = type;
		this.rule = rule;
	}

	public int getPosition() {
		return position;
	}

	public String getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Relation getRule() {
		return rule;
	}

	@Override
	public int compareTo(Memory that) {
		if (this.getPosition() > that.getPosition()) {
			return 1;
		}
		else {
			return -1;
		}
	}

	public String toString() {
		if (getName() != null) {
			return position + ", " + type + ", " + name + ", " + rule;
		}
		return position + ", " + type + ", " + rule;
	}
	
	
	/***
	 * In cases where rule order is being changed
	 */
	public static Comparator<Memory> NonSchizophrenicComparator 
	= new Comparator<Memory>() {
		/**
		 * Always places explanation rules first.
		 */
		public int compare(Memory m1, Memory m2) {
			if (m1.type.equals(Markers.EXPLANATION_RULE)) {
				return -1;
			} else {
				return 1;
			}
		}

	};
	
	public static Comparator<Memory> HyperpresumptionComparator 
	= new Comparator<Memory>() {
		/**
		 * Always places presumption rules first
		 * @param m1
		 * @param m2
		 * @return
		 */
		public int compare(Memory m1, Memory m2) {
			if (m1.type.equals(Markers.PRESUMPTION_RULE)) {
				return -1;
			} else {
				return 1;
			}
		}

	};
	
}