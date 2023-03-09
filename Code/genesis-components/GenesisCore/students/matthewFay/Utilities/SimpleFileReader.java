package matthewFay.Utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.Mark;

public class SimpleFileReader {
	private String fileName;
	
	private List<String> lines;
	
	public SimpleFileReader(String fileName) {
		this.fileName = fileName;
	}
	
	public boolean hasLine() {
		if(lines == null) {
			lines = new ArrayList<String>();
			try {
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String line;
				while((line = br.readLine()) != null) {
					if(line.length() > 0 && !line.startsWith("//")) {
						lines.add(line);
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Mark.say(e);
				return false;
			}
		}
		if(lines.size() > 0)
			return true;
		return false;
	}
	
	public String nextLine() {
		if(!hasLine())
			return null;
		String line = lines.get(0);
		lines.remove(0);
		return line;
	}
}
