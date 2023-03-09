/* Filename: CorrespondenceComparator.java
 * Author: M. A. Finlayson
 * Format: Java 2 v.1.4.2
 * Date created: Aug 18, 2004
 */
package frames.entities;

import java.util.Comparator;

/** One line description.
 * More detailed description...
 * 
 * @author M.A. Finlayson
 * @since Aug 18, 2004; JDK 1.4.2
 */
public class CorrespondenceComparator implements Comparator {
	
	public final static int ASCENDING = 0;
	public final static int DESCENDING = 1;
	
	public final int comparison;
	
	public CorrespondenceComparator(int i){
		if(i != ASCENDING &
		   i != DESCENDING){
		   	throw new IllegalArgumentException("comparison value " + i + " not recognized.");
		   }
		comparison = i;
	}

	/**
	 * @author M.A. Finlayson
	 * @since Aug 18, 2004; JDK 1.4.2
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object) compare
	 */
	public int compare(Object o1, Object o2) {
		Correspondence c1 = (Correspondence)o1;
		Correspondence c2 = (Correspondence)o2;
		
		double d1 = c1.getScore();
		double d2 = c2.getScore();
		
		if(comparison == ASCENDING){
			if(d1 > d2){
				return 1;
			} else if (d1 == d2){
				return Double.compare(c1.hashCode(), c2.hashCode());
			} else {
				return -1;
			}
		} else if (comparison == DESCENDING){
			if(d1 > d2){
				return -1;
			} else if (d1 == d2){
				return Double.compare(c1.hashCode(), c2.hashCode());
			} else {
				return 1;
			}
		}
		return 0;
	}
	
	

}
