package frames.entities;

/**
 * EntityFactory Interface
 * 
 * EntityFactory provides a module programmer with a great deal of
 * control over how Things, Derivatives, Relations, and Sequences get
 * created during the execution of an algorithm.
 * 
 * For example, the Thing.deepCopy method creates many new Things as
 * part of its operation; by providing deepCopy with a EntityFactory,
 * you can control how the new created Things get instantiated and
 * configured.
 * 
 * Why would you want to use a EntityFactory?  One common use in
 * Bridge is to control how Things get stored in Memory objects.  The
 * EntityFactoryWithMemory, for example, will make sure that every
 * Thing that is created also gets stored in a particular memory so it
 * can be recalled later.
 * 
 * This interface has one method corresponding to each constructor in
 * Thing/Derivative/Relation/Sequence.  To use this interface, simply
 * replace Thing constructor calls with the appropriate EntityFactory
 * function call.  For example, the following function:
 * 
 * <pre>
 *   public Thing makeATable() {
 *     return new Thing("table");
 *   }
 * </pre>
 * 
 * would become
 * 
 * <pre>
 *   public Thing makeATable(EntityFactory factory) {
 *     return factory.newThing("table");
 *   }
 * </pre>
 * 
 * Finally, note that many of the current implementations of EntityFactory allow you to chain them together.
 * For example, if you wanted to make sure that every Thing created is stored in both a module-specific memory
 * and in the global default memory, you could use the following code:
 * 
 * <pre>
 *   BasicMemory moduleSpecificMemory = new BasicMemory();
 *   EntityFactory storeInModuleFactory = new EntityFactoryWithMemory(moduleSpecificMemory);
 *   EntityFactory storeInBothFactory = new EntityFactoryWithStaticMemory(storeInModuleFactory);
 *   
 *   Thing myTable = makeATable(storeInBothFactory);
 * </pre>
 * 
 * If you use chained factories, remember that each Factory in the chain acts as a filter.  This means the 
 * innermost factory in the chain gets the Thing exactly as its constructor creates it, optionally modifies 
 * it a bit, and passes it to the next outer factory.  Thus if you want to store only fully configure Things
 * in memory, the Memory stages of the Factory Chain should be the outermost stages.
 * 
 * @see EntityFactoryDefault, EntityFactoryWithMemory, and EntityFactoryWithStaticMemory for useful implementations of the EntityFactory interface.
 * 
 * @see EntityFactoryAdapter for a useful starting point if you want to create your own ThingFactories.
 *  
 * @author Keith Bonawitz
 */
public interface EntityFactory {
    // =======================================================
    // ===================== THINGS ==========================
    // =======================================================
    
    /**
     * Constructs object with a unique name.  Adds new object instance to an
     * instance list for later retrieval.  Creates thread bundle.
     */
    public Entity newThing();
    
    /**
     * Creates a thing, with a type added to its primed thread.
     */
    public Entity newThing(String type);
    
    /**
     * Constructs object with a name determined by suffix string provided; used
     * only in reading.  Adds new object instance to an instance list for later
     * retrieval.  Creates thread bundle.
     */
    public Entity newThing(boolean readOnly, String suffix);
    
    // =======================================================
    // ===================== DERIVATIVES =====================
    // =======================================================
    public Function newDerivative(Entity t);
    
    public Function newDerivative(String string, Entity t);
    
    /**
     * Constructs object with a name determined by suffix string provided; used
     * only in reading.  Adds new object instance to an instance list for later
     * retrieval.  Creates thread bundle.
     */
    public Function newDerivative(boolean readOnly, String suffix,
            Entity subject);
    
    // =======================================================
    // =====================  RELATIONS  =====================
    // =======================================================
    /**
     * Constructs a new relation, given a subject and an object.
     */
    public Relation newRelation(Entity subject, Entity object);
    
    /**
     * Constructs a new relation, given a type, a subject and an object.
     */
    public Relation newRelation(String type, Entity subject, Entity object);
    
    /**
     * Constructs object with a name determined by suffix string provided; used
     * only in reading.  Adds new object instance to an instance list for later
     * retrieval.  Creates thread bundle.
     */
    public Relation newRelation(boolean readOnly, String suffix, Entity subject,
            Entity object);
    
    // =======================================================
    // =====================  SEQUENCES  =====================
    // =======================================================
    /**
     * Constructs element-free sequence.
     */
    public Sequence newSequence();
    
    /**
     * Constructs element-free sequence.
     */
    public Sequence newSequence(String type);
    
    /**
     * Constructs object with a name determined by suffix string provided; used
     * only in reading.  Adds new object instance to an instance list for later
     * retrieval.  Creates thread bundle.
     */
    public Sequence newSequence(boolean readOnly, String suffix);
    
}
