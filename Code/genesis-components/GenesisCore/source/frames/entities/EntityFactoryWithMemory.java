package frames.entities;

import frames.memories.BasicMemory;

/**
 * This implementation of EntityFactory stores every Thing that is created into
 * a bridge.modules.memory.BasicMemory object that is specified by the user when
 * the factory is constructed.
 * 
 * @author Keith
 */
public class EntityFactoryWithMemory extends EntityFactoryAdapter {
    protected BasicMemory memory; 
    
    /**
     * Creates a EntityFactory which stores every constructed things in a user-specified BasicMemory
     * 
     * @param memory The memory into which every constructed thing is stored
     */
    public EntityFactoryWithMemory(BasicMemory memory) {
        this(memory, EntityFactoryDefault.getInstance());
    }
    
    /**
     * Chainable version of EntityFactoryWithMemory
     * 
     * Creates a EntityFactory which:
     * <ol>
     *   <li> asks the delegate EntityFactory to create the requested thing
     *   <li> stores the Thing returned from the delegate in the user-specified memory.
     * </ol>
     * 
     * @param memory The memory into which every constructed thing is stored
     * @param delegate The chained factory
     */
    public EntityFactoryWithMemory(BasicMemory memory, EntityFactory delegate) {
        super(delegate);
        this.memory = memory;
    }
    
    /** Returns the memory which backs this factory.
     * @author M.A. Finlayson
     * @since Jan 21, 2005; JDK 1.4.2
     */
    public BasicMemory getMemory(){
        return memory;
    }
    
    /**
     * Override EntityFactoryAdapter.configure to implement the desired behavior.
     * EntityFactoryAdapter calls this function on every created Thing.
     */
    protected Entity configure(Entity t) {
        memory.store(t);
        return t;
    }
}
