package utils;

import java.io.*;

/*
 * From the Web artical by Michael C. Daconta, JavaWorld 12/29/00
 */
public class WindowsConnection {

	public static boolean run(String arg) {
		return WindowsConnection.run(arg, null, null);
	}

	public static boolean run(String arg, File dir) {
		return WindowsConnection.run(arg, null, dir);
	}

	public static boolean runWithoutWaiting(String arg) {
		return WindowsConnection.runWithoutWaiting(arg, null, null);
	}

	public static boolean runWithoutWaiting(String arg, File dir) {
		return WindowsConnection.runWithoutWaiting(arg, null, dir);
	}

	public static boolean runWithoutWaiting(String arg, String[] env, File dir) {
		boolean debug = false;
		try {
			String osName = System.getProperty("os.name");
			String cmd = null;
			if (osName.equals("Windows 7") || osName.equals("Windows XP") || osName.equals("Windows NT")) {
				cmd = "cmd.exe /C " + arg;
			}
			else if (osName.equals("Windows 95")) {
				cmd = "command.com /C " + arg;
			}
			Runtime rt = Runtime.getRuntime();
			Mark.say(debug, "Executing: " + arg + "\n in directory " + dir + "\n with env=" + env);
			Process proc = rt.exec(cmd, env, dir);

			return true;
		}
		catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
		// return true;

	}

	public static boolean run(String arg, String[] env, File dir) {
		boolean debug = false;
		try {
			String osName = System.getProperty("os.name");
			String cmd = null;
			if (osName.equals("Windows 7") || osName.equals("Windows XP") || osName.equals("Windows NT")) {
				cmd = "cmd.exe /C " + arg;
			}
			else if (osName.equals("Windows 95")) {
				cmd = "command.com /C " + arg;
			}
			Runtime rt = Runtime.getRuntime();
			Mark.say(debug, "Executing: " + arg + "\n in directory " + dir + "\n with env=" + env);
			Process proc = rt.exec(cmd, env, dir);
			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			int exitVal = proc.waitFor();
			if (exitVal == 0) {
				Mark.say("Successfully executed " + arg);
				return true;
			}
			else {
				Mark.err("Failed to execute " + arg);
				return false;
			}
		}
		catch (Throwable t) {
			t.printStackTrace();
			return false;
		}
		// return true;

	}
}

class StreamGobbler extends Thread {
	InputStream is;

	String type;

	StreamGobbler(InputStream is, String type) {
		this.is = is;
		this.type = type;
	}

	public void run() {
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				// System.out.println(type + ">" + line);
			}
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
