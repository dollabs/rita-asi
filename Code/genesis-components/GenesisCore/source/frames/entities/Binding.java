package frames.entities;

public class Binding {
	Object variable;

	Object value;

	public Binding(Object var, Object val) {
		variable = var;
		value = val;
	}

	public Object getVariable() {
		return variable;
	}

	public Object getValue() {
		return value;
	}

	public String toString() {
		return "[" + ((Entity) variable).getTypes() + " --- " + ((Entity) value).getTypes() + "]";
	}
}