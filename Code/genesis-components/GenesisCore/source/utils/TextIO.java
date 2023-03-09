package utils;

import java.io.*;
import java.net.*;
import java.util.regex.Pattern;

/**
 * Class offering static methods that reading and writing strings from and to
 * files and urls. Also contains deletion methods. Copyright 1999 Ascent
 * Technology, Inc. All rights reserved. Used here by permission.
 * 
 * @author Patrick Winston
 */
public class TextIO {
	public static boolean debug = false;

	/**
	 * Writes string to file. Returns true of string is written.
	 */
	public static boolean writeStringToFile(String s, File outputFile) {
		boolean b = false;
		try {
			FileOutputStream fileStream = new FileOutputStream(outputFile);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileStream);
			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
			writeStringToStream(s, bufferedWriter);
			bufferedWriter.close();
			outputStreamWriter.close();
			fileStream.close();
			b = true;
		}
		catch (Exception e) {
		}
		return b;

	}

	/**
	 * Writes string to url. Returns true if string is written.
	 */
	public static boolean writeStringToURL(String s, URL url) throws Exception {
		boolean b = false;
		try {
			HttpURLConnection urlConnection = (HttpURLConnection) (url.openConnection());
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("PUT");

			PrintWriter printWriter = new PrintWriter(urlConnection.getOutputStream());
			printWriter.print(s);
			printWriter.flush();
			printWriter.close();

			// Following MUST be after write!
			String response = urlConnection.getResponseMessage();
			if (response.equals("OK")) {
				b = true;
			}
			urlConnection.disconnect();
		}
		catch (IOException e) {
		}
		return b;
	}

	private static void writeStringToStream(String s, BufferedWriter bufferedWriter) throws Exception {
		if (s != null) {
			bufferedWriter.write(s);
		}
	}

	/**
	 * Reads string from file.
	 */
	public static String readStringFromFile(File file) throws Exception {
		String s = "";
		if (debug) System.out.println("TextIO: Reading string from " + file);
		if (debug) if (!file.exists()) System.out.println(file + " does not exist in " + file.getAbsolutePath());
		if (file.exists()) {
			FileInputStream fileStream = new FileInputStream(file);
			InputStreamReader inputStreamReader = new InputStreamReader(fileStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			if (debug) System.out.println("TextIO: Begin stream reading... ");
			s = readStringFromStream(bufferedReader);
			if (debug) System.out.println("TextIO: End stream reading: " + s);
			bufferedReader.close();
			inputStreamReader.close();
			fileStream.close();
		}
		return s;
	}

	/**
	 * Reads string from url.
	 */
	public static String readStringFromURL(URL url) throws Exception {

		// Amazing from here...
		
		 URLConnection con = url.openConnection();
		 Pattern p = Pattern.compile("text/html;\\s+charset=([^\\s]+)\\s*");
		 java.util.regex.Matcher m = p.matcher(con.getContentType());
		 /*
		 * If Content-Type doesn't match this pre-conception, choose default
		 and
		 * hope for the best.
		 */
		 String charset = m.matches() ? m.group(1) : "ISO-8859-1";
		 Reader r = new InputStreamReader(con.getInputStream(), charset);
		 StringBuilder buf = new StringBuilder();
		 while (true) {
		 int ch = r.read();
		 if (ch < 0) break;
		 buf.append((char) ch);
		 }
		// Mark.say("Returning xxx", buf.toString());
		 return buf.toString();

		// ... to here

//		String s = "";
//		try {
//			InputStream stream = (InputStream) (url.getContent());
//			InputStreamReader inputStreamReader = new InputStreamReader(stream);
//			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//			s = readStringFromStream(bufferedReader);
//			bufferedReader.close();
//			inputStreamReader.close();
//		}
//		catch (IOException e) {
//			System.out.println("Failed to read from " + url);
//		}
//		return s;
	}

	/*
	 * Reads string from file. Ignores Java style comments, including nested *
	 * type comments
	 */
	private static String readStringFromStream(BufferedReader bufferedReader) {
		StringBuffer everything = new StringBuffer("");
		String nextLine;
		try {
			while ((nextLine = bufferedReader.readLine()) != null) {
				// Deal with // style comments
				int termination = nextLine.indexOf("//");
				if (termination >= 0) {
					nextLine = nextLine.substring(0, termination);
				}
				everything.append(nextLine);
				everything.append('\n');
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Problem trying to read a string from stream");
			System.out.println(e);
		}
		int index;
		while ((index = everything.indexOf("/*")) >= 0) {
			int otherIndex = index + 1;
			int limit = everything.length();
			int depth = 1;
			while (limit >= otherIndex + 2) {
				if (everything.charAt(otherIndex) == '/' && everything.charAt(otherIndex + 1) == '*') {
					++depth;
				}
				else if (everything.charAt(otherIndex) == '*' && everything.charAt(otherIndex + 1) == '/') {
					--depth;
				}
				if (depth == 0) {
					everything.delete(index, otherIndex + 2);
					break;
				}
				++otherIndex;
			}
			if (depth != 0) {
				System.err.println("Unbalanced comment starting at " + everything.substring(index, index + 50) + "...");
			}
		}
		String result = new String(everything);
		return result;
	}

	/**
	 * Deletes file if it exists.
	 */
	public static boolean deleteFile(File file) {
		boolean result = true;
		if (file.exists()) {
			result = file.delete();
			if (file.exists()) {
				result = false;
			}
			else {
			}
		}
		return result;
	}

	/**
	 * Deletes url if it exists.
	 */
	public static boolean deleteURL(URL url) {
		boolean result = false;
		try {
			URLConnection urlConnection = url.openConnection();
			urlConnection.setDoOutput(true);
			urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			if (urlConnection instanceof HttpURLConnection) {
				HttpURLConnection conn = (HttpURLConnection) urlConnection;
				conn.setRequestMethod("DELETE");
				result = true;
			}
		}
		catch (FileNotFoundException e) {
		}
		catch (IOException e) {
		}
		catch (NullPointerException e) {
		}
		catch (Exception e) {
		}
		return result;
	}

	// Listing

	/**
	 * Lists all files in directory satisfying filter
	 */
	public static File[] list(File directory, FilenameFilter filter) {
		File[] array;
		array = directory.listFiles(filter);
		return array;
	}

	/**
	 * Lists all files in directory
	 */
	public static File[] list(File directory) {
		File[] array;
		array = directory.listFiles();
		return array;
	}

	/**
	 * Tests behavior.
	 */
	public static void main(String argv[]) {
		try {
			URL url1 = new URL("http://ewall.mit.edu/phw/share/uploads/hello.html");
			URL url2 = new URL("http://ewall.mit.edu/phw/share/uploads/hello.html");

			// url1 = new
			// URL("http://www.ai.mit.edu/people/phw/Server/test@ascent.com.options");
			// url2 = new
			// URL("http://www.ai.mit.edu/people/phw/Server/test@ascent.com.options");
			// url2 = new
			// URL("http://www.ai.mit.edu/people/phw/Server/phw@ascent.com.options");

			url1 = new URL("http://www.ai.mit.edu/people/phw/Server/test.data");
			url2 = new URL("http://www.ai.mit.edu/people/phw/Server/test.data");

			TextIO.writeStringToURL("Hello World X?", url1);

			System.out.println("----------");

			System.out.println("Read " + TextIO.readStringFromURL(url1));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
