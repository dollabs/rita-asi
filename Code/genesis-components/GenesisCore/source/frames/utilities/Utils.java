package frames.utilities;
/**
 * Class of useful functions.
 * 
 * @author blamothe
 */
public class Utils {
	/**
	 * Concatenates two arrays
	 */
	public static String[] arraycat(String[] a, String[] b) {
		   String [] result = new String [a.length+b.length];
		   System.arraycopy(a, 0, result, 0, a.length);
		   System.arraycopy(b, 0, result, a.length, b.length);
		   return result;
	}
}
