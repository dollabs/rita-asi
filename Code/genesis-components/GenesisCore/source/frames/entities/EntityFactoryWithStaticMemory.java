package frames.entities;

import frames.memories.BasicMemory;

/**
 * This implementation of EntityFactory stores every Thing that is created into
 * the System-wide static memory located at BasicMemory.staticMemory.
 *  
 * The non-chained version of this class implements the Singleton design pattern, because 
 * it is immutable and nonconfigurable.  To get an instance of this class, use 
 * EntityFactoryDefault.getInstance().
 *
 * @author Keith
 */
public class EntityFactoryWithStaticMemory extends EntityFactoryWithMemory {
    private static EntityFactoryWithStaticMemory _instance = null;
    
    /**
     * Creates a EntityFactory which stores every constructed things in the 
     * System-wide static memory located at BasicMemory.staticMemory.
     * 
     * Protected because of the Singleton design pattern.  Use getInstance() instead.
     */
    protected EntityFactoryWithStaticMemory() {
        super(BasicMemory.getStaticMemory());
    }
    
    /**
     * Get a non-chained instance of EntityFactoryWithStaticMemory (Singleton design pattern)
     */
    public static EntityFactoryWithStaticMemory getInstance() {
        if (_instance == null) _instance = new EntityFactoryWithStaticMemory();
        return _instance;
    }    

   /**
    * Chainable version of EntityFactoryWithStaticMemory
    * 
    * Creates a EntityFactory which:
    * <ol>
    *   <li> asks the delegate EntityFactory to create the requested thing
    *   <li> stores the Thing returned from the delegate in BasicMemory.staticMemory
    * </ol>
    * 
    * @param delegate The chained factory
    */
    public EntityFactoryWithStaticMemory(EntityFactory delegate) {
        super(BasicMemory.getStaticMemory(), delegate);
    }

}
