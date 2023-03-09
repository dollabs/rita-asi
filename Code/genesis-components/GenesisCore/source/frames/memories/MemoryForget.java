/* Filename: MemoryForget.java
 * Author: M. A. Finlayson
 * Format: Java 2 v.1.4.2_06
 * Date created: Jan 27, 2005
 */
package frames.memories;

import frames.entities.Entity;

/** Indicates that the module implements a forget facility.
 *
 * @author M.A. Finlayson
 * @since Jan 27, 2005; JDK 1.4.2_06
 */
public interface MemoryForget {
    
    
    /** Removes strong references to the named Thing.
     * @author M.A. Finlayson
     * @since Jan 27, 2005; JDK 1.4.2
     */
    public boolean forget(Entity t);
    
    /** Indicates if the Thing can be forgotten.
     * @author M.A. Finlayson
     * @since Jan 27, 2005; JDK 1.4.2
     */
    public boolean isForgettable(Entity t);
}
