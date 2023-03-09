package gui;

/*
 * Created on May 11, 2010
 * @author phw
 */

public class StringUtilities {
	
	
	public static String prepareForDisplay(String s) {
		StringBuffer buffer = new StringBuffer(s);
		// Remove " and _
		while(true) {
			int index = buffer.indexOf("\"");
			if (index >=0) {
				buffer.deleteCharAt(index);
				continue;
			}
			index = buffer.indexOf("_");
			if (index >=0) {
				buffer.replace(index, index + 1, " ");
				continue;
			}
			break;
		}
		return buffer.toString();
	}

}
