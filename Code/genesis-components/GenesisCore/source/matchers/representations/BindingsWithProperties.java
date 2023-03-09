package matchers.representations;

import java.util.*;
import java.util.stream.Collectors;

import frames.entities.Entity.LabelValuePair;
import utils.*;
import utils.minilisp.LList;

/**
 * This class is used to keep track of any properties that are relevant to action taken at the conclusion of this matching.
 * 
 * For instance, CSERM requires keeping track of the justifications relevant to the similarity match while the match is happening.
 * Properties are used to capture this information. See StandardMatcher.similarityMatch() and RuleEngine.instantiateCauseWithProperties()
 * for more information.
 * 
 * CAUTION: When properties are transferred between BindingsWithProperties instances, although the property list itself is not cloned,
 * the property values are not cloned since there's no easy way to do this.
 * 
 * @author bryanwilliams
 *
 */
public class BindingsWithProperties {
    private final List<LabelValuePair> properties;
    private final LList<PairOfEntities> bindings;
    
    public BindingsWithProperties(LList<PairOfEntities> bindings, List<LabelValuePair> properties) {
        this.bindings = bindings.copy();
        this.properties = clonePropertyList(properties);
		// Mark.say("Hello world\n", bindings, "\n", properties);
    }
    
    public BindingsWithProperties(LList<PairOfEntities> bindings) {
        this(bindings, Collections.emptyList());
    }
    
    public BindingsWithProperties() {
        this(new LList<>());
    }
    
    // caution - does not clone individual values (no easy way to do this)
    private static List<LabelValuePair> clonePropertyList(List<LabelValuePair> properties) {
        return properties.stream()
                .map(LabelValuePair::clone)
                .collect(Collectors.toList());
    }
    
    public List<LabelValuePair> getProperties() {
        return clonePropertyList(properties);
    }
    
    public Map<String, Object> getPropertyToValue() {
        return Collections.unmodifiableMap(properties.stream()
                .collect(Collectors.toMap(LabelValuePair::getLabel, LabelValuePair::getValue)));
    }
    
    public boolean hasProperty(String propertyLabel) {
        return properties.stream()
                .anyMatch(lvp -> lvp.getLabel().equals(propertyLabel));
    }
    
    /**
     * Returns the value of the first stored property with this label, or null if none exists.
     */
    public Object getValue(String propertyLabel) {
        return properties.stream()
                .filter(lvp -> lvp.getLabel().equals(propertyLabel))
                .map(LabelValuePair::getValue)
                .findFirst().orElse(null);
    }
    
    /**
     * Return false if property does not exist or value is identical to new one,
     * otherwise changes first stored value with this property to new value and returns true
     */
    public boolean setValue(String property, Object value) {
        if (!hasProperty(property)) {
            return false;
        }
        LabelValuePair pair = properties.stream()
                .filter(lvp -> lvp.getLabel().equals(property))
                .findFirst().orElseThrow(RuntimeException::new);
        Object oldValue = pair.getValue();
        if (value.equals(oldValue)) {
            return false;
        }
        pair.setValue(value);
        return true; 
    }    
    
    /**
     * Sets the properties of this instance to match the given properties.
     */
    public void setProperties(List<LabelValuePair> properties) {
        this.properties.clear();
        for (LabelValuePair p : properties) {
            this.properties.add(p.clone());
        }
    }
    
    public LList<PairOfEntities> getBindings() {
        return bindings.copy();
    }
    
    /**
     * Returns a new BindingsWithProperties instance with the new property added.
     *
     * Does NOT replace the value if property is already present - this is undesirable for things
     * like features, which are values that all have Markers.FEATURE as their property 
     * Instead, will add the property again with a different value
     */
    public BindingsWithProperties addProperty(String property, Object value) {
        return addProperty(property, value, false);
    }
    
    /**
     * Returns a new BindingsWithProperties instance which is the result of adding the new property.
     *
     * Does NOT replace the value if property is already present - this is undesirable for things
     * like features, which are values that all have Markers.FEATURE as their property 
     * Instead, will add the property again with a different value
     */
    public BindingsWithProperties addProperty(String property, Object value, boolean identifier) {
        List<LabelValuePair> properties = clonePropertyList(this.properties);
        properties.add(new LabelValuePair(property, value, identifier));
        return new BindingsWithProperties(bindings, properties);
    }
    
    /**
     * Returns a new BindingsWithProperties instance which is the result of adding the new bindings.
     *
     * Does not form any duplicate bindings - if an additional binding is already present, it will not be
     * re-added.
     */
    public BindingsWithProperties withAdditionalBindings(LList<PairOfEntities> bindingsToAdd) {
        LList<PairOfEntities> totalBindings = bindings.copy();
        List<PairOfEntities> newBindings = bindingsToAdd.toList();
        for (PairOfEntities binding : totalBindings) {
            if (newBindings.contains(binding)) {
                newBindings.remove(binding);
            }
        }
        for (PairOfEntities newBinding : newBindings) {
            totalBindings = totalBindings.cons(newBinding);
        }
        return new BindingsWithProperties(totalBindings, properties);
    }
    
    /**
     * Returns a new BindingsWithProperties instance which is the result of replacing the existing
     * bindings with the new ones.
     */
    public BindingsWithProperties withReplacedBindings(LList<PairOfEntities> newBindings) {
        return new BindingsWithProperties(newBindings, this.properties);
    }

    @Override
    public String toString() {
        return "BindingsWithProperties [properties=" + properties + ", bindings=" + bindings + "]";
    }
}
