package lexicons;

import java.io.*;
import java.net.*;
import java.util.HashSet;

/*
 * To see if a word, such as "hawk," has be used, enter WorkingVocabulary.getWorkingVocabulary().contains("hawk"));
 * Words are inserted via PictureFinder code. Note that only words in Thing instances are recorded, not derivatives or
 * relations, so there will be no verbs, path prepositions, and so on, which means no generalizing on those elements,
 * which, for now, I think is ok or even desirable. Created on Nov 13, 2007
 * @author phw
 */

public class WorkingVocabulary extends HashSet<Object> {
	/**
	 * Serialization ID, should be updated on class changes
	 */
	private static final long serialVersionUID = 1L;

	private static WorkingVocabulary singleton;

	public static WorkingVocabulary getWorkingVocabulary() {
		if (singleton == null) {
			singleton = new WorkingVocabulary();
			getWorkingVocabulary().readBasicEnglish();
		}
		return singleton;
	}

	private void readBasicEnglish() {
		URL url = WorkingVocabulary.class.getResource("BasicEnglish.txt");
		try {

			InputStream is = new BufferedInputStream(url.openStream());
			// System.out.println("InputStream = " + is);
			InputStreamReader reader = new InputStreamReader(is);

			BufferedReader br = new BufferedReader(reader);
			String line;
			while ((line = br.readLine()) != null) {
				readTheLine(line);
			}
		}
		catch (Exception e) {
			System.err.println("Unable to find Basic English file");
			e.printStackTrace();
		}
	}

	private void readTheLine(String line) {
		if (line.length() == 0) {
			return;
		}
		else if (line.startsWith("%")) {
			return;
		}
		else {
			splitLine(line.trim());
		}
	}

	private void splitLine(String line) {
		int index = 0;
		while ((index = line.indexOf(' ')) > 0) {
			String word = stripPunctuation(line.substring(0, index));
			line = stripPunctuation(line.substring(index + 1).trim());
			getWorkingVocabulary().add(word);
			// System.out.println("Word: " + word);
		}
		getWorkingVocabulary().add(line);
		// System.out.println("Word: " + line);
	}

	private String stripPunctuation(String string) {
		int index = string.length() - 1;
		if (index >= 0 && ",.;?!".indexOf(string.charAt(index)) >= 0) {
			return stripPunctuation(string.substring(0, index));
		}
		return string;
	}

}
