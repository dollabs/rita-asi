package frames.entities;

/**
 * This class simplifies the task of implementing the EntityFactory interface.
 * In most cases, you can implement a custom EntityFactory simply by extending
 * this class and overriding the configure(Thing) method.
 * 
 * This class handles all calls to the EntityFactory methods by first delegating
 * Thing creation to the delegate Factory, then calling configure() on the
 * returned Thing.  The default behavior of the class is for 
 * configure(Derivative/Relation/Sequence) to simply call configure(Thing),
 * thereby allowing classes to implement new behaviour by extending configure(Thing).
 * However, further control can be exercised by overriding each configure() method
 * individually.
 * 
 * @author Keith
 */
abstract public class EntityFactoryAdapter implements EntityFactory {
    protected EntityFactory delegate;
    
    /**
     * Creates a EntityFactoryAdapter which calls configure() on each constructed Thing.
     */
    public EntityFactoryAdapter() {
        this(EntityFactoryDefault.getInstance());
    }
    
    /**
     * Chainable version of the EntityFactoryAdapter.
    * 
    * Creates a EntityFactoryAdapter which:
    * <ol>
    *   <li> asks the delegate EntityFactory to create the requested thing
    *   <li> calls configure() on the returned Thing
    * </ol>
    * 
    * @param delegate The chained factory
     */
    public EntityFactoryAdapter(EntityFactory delegate) {
        this.delegate = delegate;
    }
    
    abstract protected Entity configure(Entity t);
    
    protected Function configure(Function d) {
        return (Function) configure((Entity) d);
    }

    protected Relation configure(Relation d) {
        return (Relation) configure((Function) d);
    }

    protected Sequence configure(Sequence d) {
        return (Sequence) configure((Entity) d);
    }
    
    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newThing()
     */
    public Entity newThing() {
        return configure(delegate.newThing());
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newThing(java.lang.String)
     */
    public Entity newThing(String type) {
       return configure(delegate.newThing(type));
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newThing(boolean, java.lang.String)
     */
    public Entity newThing(boolean readOnly, String suffix) {
       return configure(delegate.newThing(readOnly, suffix));
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newDerivative(bridge.reps.entities.Thing)
     */
    public Function newDerivative(Entity t) {
        return configure(delegate.newDerivative(t));
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newDerivative(java.lang.String, bridge.reps.entities.Thing)
     */
    public Function newDerivative(String string, Entity t) {
        return configure(delegate.newDerivative(string, t));
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newDerivative(boolean, java.lang.String, bridge.reps.entities.Thing)
     */
    public Function newDerivative(boolean readOnly, String suffix,
            Entity subject) {
        return configure(delegate.newDerivative(readOnly, suffix, subject));
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newRelation(bridge.reps.entities.Thing, bridge.reps.entities.Thing)
     */
    public Relation newRelation(Entity subject, Entity object) {
        return configure(delegate.newRelation(subject, object));
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newRelation(java.lang.String, bridge.reps.entities.Thing, bridge.reps.entities.Thing)
     */
    public Relation newRelation(String type, Entity subject, Entity object) {
        return configure(delegate.newRelation(type, subject, object));
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newRelation(boolean, java.lang.String, bridge.reps.entities.Thing, bridge.reps.entities.Thing)
     */
    public Relation newRelation(boolean readOnly, String suffix, Entity subject,
            Entity object) {
        return configure(delegate.newRelation(readOnly, suffix, subject, object));
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newSequence()
     */
    public Sequence newSequence() {
        return configure(delegate.newSequence());
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newSequence(java.lang.String)
     */
    public Sequence newSequence(String type) {
        return configure(delegate.newSequence(type));
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newSequence(boolean, java.lang.String)
     */
    public Sequence newSequence(boolean readOnly, String suffix) {
        return configure(delegate.newSequence(readOnly, suffix));
    }

}
