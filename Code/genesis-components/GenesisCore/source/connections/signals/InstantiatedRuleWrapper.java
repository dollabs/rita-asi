package connections.signals;

import frames.entities.Relation;

/*
 * Created on Jun 9, 2015
 * @author phw
 */

public class InstantiatedRuleWrapper extends BetterSignal {

	public InstantiatedRuleWrapper() {
	}

	public InstantiatedRuleWrapper(Object... args) {
		super(args);
	}

	public Relation getRule() {
		return get(0, Relation.class);
	}

	public Relation getInstantiatedRule() {
		return get(1, Relation.class);
	}

	public static InstantiatedRuleWrapper isInstantiatedRuleWrapper(Object object) {
		if (object instanceof InstantiatedRuleWrapper) {
			return (InstantiatedRuleWrapper) object;
		}
		return null;
	}

}
