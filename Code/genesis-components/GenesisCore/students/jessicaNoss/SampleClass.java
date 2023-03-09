package jessicaNoss;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/*
 * This sample class can be used to display data in a table.  Most of the naming conventions (such as "foo")
 * are arbitrary, but some matter, as noted below.
 * 
 * Based on the example from http://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/TableView.html
 * 
 * Created on Jan 16, 2015
 * @author jmn
 */

public class SampleClass {
	
	public SampleClass(String name) {
		this.setFoo(name);
	}

	//The StringProperty attribute can be named anything, such as firstNameProperty for a person
    private StringProperty foo;
    
    //The setter can be named anything; it doesn't need to have foo in its name.
    public void setFoo(String value) { fooMethod().set(value); }
    
    //The getter MUST be in the form "get" + [property name from SimpleStringProperty],
    //with the first letter of the property name in uppercase.
    public String getPropertyName1() { return fooMethod().get(); }
    
    //The StringProperty method can be named anything and does not need to contain "foo"
    public StringProperty fooMethod() {
    	//The SimpleStringProperty is the name that matters because it is used for lookup.
    	//It can be any string, but must be consistent with the getter.
    	//The case of the first letter in the string does not matter here (propertyName1 vs PropertyName1),
    	//but it must be uppercase in the getter.
        if (foo == null) foo = new SimpleStringProperty(this, "propertyName1");
        return foo;
    }
}
