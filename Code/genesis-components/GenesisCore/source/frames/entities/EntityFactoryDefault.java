package frames.entities;

/**
 * This implementation of EntityFactory simply calls the appropriate
 * Entity/Function/Relation/Sequence constructor, and returns the
 * resulting Entity.  No further configuration is performed.
 * 
 * This class implements the Singleton design pattern, because it is
 * immutable and nonconfigurable.  To get an instance of this class,
 * use EntityFactoryDefault.getInstance().
 * 
 * @see frames.entities.EntityFactory
 * 
 * @author Keith
 */
public class EntityFactoryDefault implements EntityFactory {
    private static EntityFactoryDefault _instance = null;
    
    /**
     * Default constructor.
     * 
     * Protected because of the Singleton design pattern.  Use
     * getInstance() instead.
     */
    protected EntityFactoryDefault() {};
    
    /**
     * Get the instance of EntityFactoryDefault.
     */
    public static EntityFactoryDefault getInstance() {
        if (_instance == null) {
            synchronized(EntityFactoryDefault.class) {
                if (_instance == null) _instance = new EntityFactoryDefault();
            }
        }
        return _instance;
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newThing()
     */
    public Entity newThing() {
        return new Entity();
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newThing(java.lang.String)
     */
    public Entity newThing(String type) {
        return new Entity(type);
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newThing(boolean, java.lang.String)
     */
    public Entity newThing(boolean readOnly, String suffix) {
        return new Entity(readOnly, suffix);
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newDerivative(bridge.reps.entities.Thing)
     */
    public Function newDerivative(Entity t) {
        return new Function(t);
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newDerivative(java.lang.String, bridge.reps.entities.Thing)
     */
    public Function newDerivative(String string, Entity t) {
        return new Function(string, t);
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newDerivative(boolean, java.lang.String, bridge.reps.entities.Thing)
     */
    public Function newDerivative(boolean readOnly, String suffix,
            Entity subject) {
        return new Function(readOnly, suffix, subject);
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newRelation(bridge.reps.entities.Thing, bridge.reps.entities.Thing)
     */
    public Relation newRelation(Entity subject, Entity object) {
        return new Relation(subject, object);
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newRelation(java.lang.String, bridge.reps.entities.Thing, bridge.reps.entities.Thing)
     */
    public Relation newRelation(String type, Entity subject, Entity object) {
        return new Relation(type, subject, object);
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newRelation(boolean, java.lang.String, bridge.reps.entities.Thing, bridge.reps.entities.Thing)
     */
    public Relation newRelation(boolean readOnly, String suffix, Entity subject,
            Entity object) {
        return new Relation(readOnly, suffix, subject, object);
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newSequence()
     */
    public Sequence newSequence() {
        return new Sequence();
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newSequence(java.lang.String)
     */
    public Sequence newSequence(String type) {
        return new Sequence(type);
    }

    /* (non-Javadoc)
     * @see bridge.reps.entities.EntityFactory#newSequence(boolean, java.lang.String)
     */
    public Sequence newSequence(boolean readOnly, String suffix) {
        return new Sequence(readOnly, suffix);
    }

}
