package utils;

import java.util.Arrays;

/** Provides some random array utility functions.
 * @author M.A. Finlayson
 * @since Aug 19, 2004; JDK 1.4.2
 */
public class ArrayUtils {
	
	public static String toString(Object[] array){
		return "[" + StringUtils.join(Arrays.asList(array), ", ") + "]";
	}
	
	public static boolean contains(int[] array, int element){
		int test;
		for(int i = 0; i < array.length; i++){
			test = array[i];
			if(test == element){return true;}
		}
		return false;
	}
	
	public static boolean contains(double[] array, double element){
		double test;
		for(int i = 0; i < array.length; i++){
			test = array[i];
			if(test == element){return true;}
		}
		return false;
	}
	
	/** Returns true if the element is contained in the specified array.
	 * @author M.A. Finlayson
	 * @since Aug 19, 2004; JDK 1.4.2
	 */
	public static boolean contains(Object[] array, Object element){
		//System.out.println("Array length: " + array.length);
//		for(int i = 0; i < array.length; i++){
//			System.out.println(array[i].toString());
//		}
		//System.out.print("Checking containment of '" + element + "' in " + toString(array));
		Object test;
		for(int i = 0; i < array.length; i++){
			test = array[i];
			if(test == null){
				if(element == null){
					//System.out.println(" -- found!");
					return true;}
			} else {
				if(test.equals(element)){
					//System.out.println(" -- found!");
					return true;
				}
			}
		}
		//System.out.println(" -- not found.");
		return false;
	}
	
	/** Copies as much of the source array into the target array as possible.
	 * Throws a ClassCastException if the array types are not compatible.
	 * @author M.A. Finlayson
	 * @since Aug 19, 2004; JDK 1.4.2
	 */
	public static void copyInto(Object[] source, Object[] target){
		if(source == null){return;}
		for(int i = 0; i < source.length; i++){
			target[i] = source[i];
		}
	}

}
