/* Filename: ExclusiveCollection.java
 * Author: M. A. Finlayson
 * Format: Java 2 v.1.4.2_06
 * Date created: Feb 2, 2005
 */
package utils.collections;

import java.util.Collection;

/** An interface for exclusive collections.
 * 
 * @author M.A. Finlayson
 * @since Feb 2, 2005; JDK 1.4.2_06
 */
public interface ExclusiveCollection<T> extends Collection<T> {
    
	/** Indicates that the ExclusiveCollection should allow only the specified class to be a member. */
	public static final int STRICT = 0;
	
	/** Indicates that the ExclusiveCollection should allow a class and all of its descendants to be members. */
	public static final int FAMILY = 1;	
    
    /** Returns <b>true</b> if this object will be accepted
     * by the collection.
     * @author M.A. Finlayson
     * @since Feb 2, 2005; JDK 1.4.2
     */
    public boolean testType(Object element);
    
    /** Returns a class object representing the class
     * which this collection will accept.
     * @author M.A. Finlayson
     * @since Feb 2, 2005; JDK 1.4.2
     */
    public Class getType();

    /** Returns either STRICT or FAMILY depending on whether
     * the collection will accept only instances of the type
     * for the collection, or if it will accept
     * descedants of that collection as well.
     * @author M.A. Finlayson
     * @since Feb 2, 2005; JDK 1.4.2
     */
    public int getRestriction();

}
