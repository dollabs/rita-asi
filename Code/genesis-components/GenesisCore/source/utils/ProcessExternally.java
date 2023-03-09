package utils;

import java.io.*;

/*
 * Created on Dec 29, 2009
 * @author phw
 */

public class ProcessExternally {

	public static void processFileExternally(String path) {

		String exec = "cmd.exe /C \"" + path + "\"";

		Mark.say("Process externally", exec);
		try {
			Runtime.getRuntime().exec(exec);
		}
		catch (IOException e) {
			System.err.println("Unable to process file externally: " + path);
			e.printStackTrace();
		}
	}
}
