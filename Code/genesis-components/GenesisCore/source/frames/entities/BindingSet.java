package frames.entities;

import java.util.*;

public class BindingSet extends Vector<Binding> {
	int sameTypeCount = 0;

	int matchedVariableCount = 0;

	int forcedTypeCount = 0;

	public boolean add(Binding b) {
		boolean result = super.add(b);
		incrementMatchedVariableCount();
		Vector<String> variableVector = ((Entity) (b.getVariable())).getTypes();
		Vector<String> valueVector = ((Entity) (b.getValue())).getTypes();
		sameTypeCount += vectorIntersection(variableVector, valueVector).size();
		return result;
	}

	public Object getValue(Object variable) {
		for (int i = 0; i < this.size(); ++i) {
			Binding binding = (Binding) (this.elementAt(i));
			if (variable.equals(binding.getVariable())) {
				return binding.getValue();
			}
		}
		return null;
	}

	// passthrough constructors
	public BindingSet() {
		super();
	}

	public BindingSet(Collection<Binding> c) {
		super(c);
	}

	public BindingSet(int ic) {
		super(ic);
	}

	public BindingSet(int ic, int ci) {
		super(ic, ci);
	}

	public Object clone() {
		BindingSet bs = new BindingSet(this);
		bs.sameTypeCount = sameTypeCount;
		bs.matchedVariableCount = matchedVariableCount;
		bs.forcedTypeCount = forcedTypeCount;
		return bs;
	}

	public int getScore() {
		return sameTypeCount;
	}

	public String toString() {
		String result = "\nMatch succeeded\n";
		if (forcedTypeCount > 0) {
			result += "Forced types: " + forcedTypeCount + "\n";
		}
		if (matchedVariableCount > 0) {
			result += "Matched variables: " + matchedVariableCount + "\n";
		}
		if (sameTypeCount > 0) {
			result += "Matched types in structures: " + sameTypeCount + "\n";
		}
		// result += super.toString();
		for (int i = 0; i < size(); ++i) {
			Binding binding = (Binding) (elementAt(i));
			result += binding.toString() + '\n';
		}
		return result;
	}

	public void incrementForcedTypeCount(int i) {
		forcedTypeCount += i;
	}

	public void incrementMatchedVariableCount() {
		++matchedVariableCount;
	}

	private static <T> Vector<T> vectorIntersection(Vector<T> v1, Vector<T> v2) {
		Vector<T> result = new Vector<T>();
		for (int i = 0; i < v1.size(); ++i) {
			T object = v1.elementAt(i);
			if (v2.contains(object)) {
				result.add(object);
			}
		}
		return result;
	}
}
