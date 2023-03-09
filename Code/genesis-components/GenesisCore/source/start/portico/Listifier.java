package start.portico;

import java.util.*;

/*
 * Created on Feb 24, 2008 @author phw
 */

public class Listifier {
	public static List<String> listify(String s) {
		ArrayList<String> result = new ArrayList<String>();
		s = s.trim().toLowerCase();
		if (s.length() > 0) {
			while ((s.charAt(s.length() - 1) == '?') || (s.charAt(s.length() - 1) == '.') || (s.charAt(s.length() - 1) == '!')) {
				s = s.substring(0, s.length() - 1);
				if (s.length() == 0) {
					break;
				}
			}
		}
		StringTokenizer tokenizer = new StringTokenizer(s);
		while (tokenizer.hasMoreTokens()) {
			result.add(tokenizer.nextToken());
		}
		return result;
	}

	public static void main(String[] ignore) {
		System.out.println(Listifier.listify("This is a test...."));
	}
}
