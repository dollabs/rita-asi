/* Filename: EntityPair.java
 * Author: M. A. Finlayson
 * Format: Java 2 v.1.4.2
 * Date created: Jan 16, 2004
 */

package frames.entities;

import java.util.Vector;

/** Allows a string to hold a pair of classes.
 * @author M.A. Finlayson 
 * @since Jan 16, 2004; JDK 1.4.2
 */

public class ClassPair {
	
	public static String makeClassPair(String upper, String lower){
		return "{" + upper + ", " + lower + "}";
	}
	
	public static String getUpper(String cp){
		return cp.substring(1, cp.indexOf(','));
	}
	
	public static String getLower(String cp){
		return cp.substring(cp.indexOf(','), cp.length());
	}
	
	public static Vector<String> getClassesFromPair(String cp){
		Vector<String> result = new Vector<String>();
		result.add(getUpper(cp));
		result.add(getLower(cp));
		return result;
	}
}
