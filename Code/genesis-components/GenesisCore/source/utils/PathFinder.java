package utils;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import stories.StoryAnchor;

/**
 * Retrieve a resource from a file, from a path relative to the classpath, or from a story title. You can also use this
 * class to list all the files in a directory. Works in webstart, jar, and eclipse.
 * 
 * @author rlm
 */
public class PathFinder {

	public final static String STORY_ROOT = "story-root";

	/**
	 * This is so that we can still find files when in WebStart or in a Jar.
	 */
	public static ClassLoader myClassLoader = StoryAnchor.class.getClassLoader();

	/**
	 * Lookup the file or directory using a string path relative to the classpath. Works in eclipse, jar, and webstart.
	 * <p>
	 * Do not include a leading slash.
	 * </p>
	 * Examples:
	 * <ol>
	 * <li> <code> lookupURL("images") </code> -- gets the URL for the folder "images" on the classpath.</li>
	 * <li> <code> lookupURL("stories/Start experiment.txt")</code> -- gets the URL for the text file
	 * "Start experiment.txt" on the classpath.</li>
	 * </ol>
	 * 
	 * @param resource
	 *            a string reference to the path of a file or directory on the classpath.
	 */
	public static URL lookupURL(String resource) {
		return myClassLoader.getResource(resource);
	}

	private static HashMap<String, URL> storyCache = new HashMap<String, URL>();

	/**
	 * Find a Story file.
	 * <p>
	 * Stories are found using two methods:
	 * </p>
	 * <ol>
	 * <li>
	 * <p>
	 * Path Interpretation (case-sensitive):
	 * </p>
	 * <p>
	 * You can search for stories by using a classpath-relative path to the story file, such as:
	 * <code>"stories/Shakespeare/Macbeth1.txt"</code>. Path interpretation is case sensitive.
	 * </p>
	 * </li>
	 * <li>
	 * <p>
	 * Title Interpretation (case <em>in</em>sensitive):
	 * </p>
	 * <p>
	 * You may also search for a story by its title, such as <code> "macbeth1" </code> (<code> "macbeth1.txt"</code>
	 * will also work).
	 * </p>
	 * <p>
	 * Because title interpretation is case insensitive, it is an error to have multiple stories with the same name
	 * accessible via the classpath.
	 * </p>
	 * For example, on your classpath, you can only have ONE of:
	 * <ul>
	 * <li>macbeth1.txt</li>
	 * <li>Macbeth1</li>
	 * <li>Macbeth1.txt</li>
	 * <li>MACBETH1.TXT</li>
	 * </ul>
	 * </li>
	 * </ol>
	 * <p>
	 * The path interpretation is tried first, then the title interpretation.
	 * </p>
	 * 
	 * @param storyReference
	 *            either the title of the story, or a path to the story, as described above.
	 * @return URL of the story.
	 * @throws IOException
	 *             unless exactly one story matches.
	 */
	public static URL storyURL(String storyReference) throws IOException {
		// mpfay 8/14/13 - check cache
		if (storyCache.containsKey(storyReference)) return storyCache.get(storyReference);

		// first, try a path search using the class loader
		URL reference = lookupURL(storyReference);
		if (reference != null) {
			storyCache.put(storyReference, reference);
			return reference;
		}

		// If that failed, then we are dealing with a title search

		Mark.say("***** storyReference", storyReference);

		ArrayList<URL> initialResults = listStoryMatches(storyReference);
		ArrayList<URL> results = initialResults;

		if (results.isEmpty()) {
			Mark.err("Story " + storyReference + " not Found!");
			return null;
		}
		URL storyURL = results.get(0);
		if (results.size() > 1) {
			Mark.err("Multiple Stories Found for " + storyReference);
			for (URL u : results) {
				Mark.err("\t" + u);
			}
			// Mark.err("Multiple Stories Found for " + storyReference + "!!!");
			Mark.err("Using", storyURL);
		}
		storyCache.put(storyReference, storyURL);
		return storyURL;
	}

	/**
	 * Gets the Genesis story root.
	 * 
	 * @return URL pointing to the base directory containing all stories.
	 */
	public static URL storyRootURL() throws IOException {
		URL storyAnchor = myClassLoader.getResource(STORY_ROOT);
		URL result = new URL(storyAnchor.toString().replaceFirst(STORY_ROOT + "$", ""));
		Mark.say("The story root is", result);
		return result;
	}

	/**
	 * Find all stories that have the given file title, as described in {@link #storyURL}.
	 * <p>
	 * By convention, there should be at most one story with a given title.
	 * </p>
	 * 
	 * @param storyTitle
	 */
	public static ArrayList<URL> listStoryMatches(String storyTitle) throws IOException {
		// Mark.say("***** storyTitle", storyTitle);
		ArrayList<URL> results = new ArrayList<URL>();
		results.addAll(listFiles(storyRootURL(), storyTitle + ".txt"));
		results.addAll(listFiles(storyRootURL(), storyTitle));

		// Mark.say("***** results", results);

		// this part is to remove any entries whose names simply end with
		// storyTitle
		ArrayList<URL> matches = new ArrayList<URL>();
		for (URL u : results) {
			if (FilenameUtils.getName(URLDecoder.decode(u.toString(), "utf-8")).toLowerCase().startsWith(storyTitle.toLowerCase())) {
				matches.add(u);
			}
		}
		return matches;
	}

	// mpfay 8/14/2013 - Added some caching to the file listing for a bit of a speedup
	private static HashMap<String, HashMap<String, ArrayList<URL>>> fileSearchCache = new HashMap<String, HashMap<String, ArrayList<URL>>>();

	/**
	 * Recursively list all files inside a given directory whose names end with endFilter.
	 * 
	 * @param root
	 *            String path to a directory containing resources. Use "/" to separate directories, regardless of
	 *            operating system.
	 * @param endFilter
	 *            only return matches that end with this string. Use it to filter on file extension or to match a single
	 *            file. <code>endFilter</code> is case-<em>in</em>sensitive.
	 * @return a list of URLs that point to the matching resources. Directories themselves are never matched.
	 */
	public static ArrayList<URL> listFiles(String root, String endFilter) throws IOException {
		URL rootURL = lookupURL(root);
		if (fileSearchCache.containsKey(rootURL.getPath())) {
			if (fileSearchCache.get(rootURL.getPath()).containsKey(endFilter)) return fileSearchCache.get(rootURL.getPath()).get(endFilter);
		}
		ArrayList<URL> files = listFiles(lookupURL(root), endFilter);
		if (!fileSearchCache.containsKey(rootURL.getPath())) fileSearchCache.put(rootURL.getPath(), new HashMap<String, ArrayList<URL>>());
		fileSearchCache.get(rootURL.getPath()).put(endFilter, files);
		return files;
	}

	/**
	 * Recursively list all files inside a given directory whose names end with endFilter. See
	 * {@link #listFiles(String, String)}.
	 * 
	 * @param root
	 *            URL path to a directory containing resources.
	 * @param endFilter
	 */
	public static ArrayList<URL> listFiles(URL root, String endFilter) throws IOException {
		// Mark.say("***** root.getProtocol()", root, root.getProtocol());
		if (root.getProtocol().equals("file")) {
			// Mark.say("***** xxxxx");
			return listFiles(new File(root.getPath()), endFilter);
		}
		else if (root.getProtocol().equals("jar")) {

			JarURLConnection connection = (JarURLConnection) root.openConnection();

			JarFile jarFile = connection.getJarFile();

			URL jarFileURL = connection.getJarFileURL();
			return listFiles(root, endFilter, jarFile, jarFileURL);
		}
		else {
			Mark.err("Genesis appears to not be in either" + " the filesystem or a jar.");
			return null;
		}
	}

	/**
	 * Recursively list all files inside a given directory whose names end with endFilter. See
	 * {@link #listFiles(String, String)}.
	 * 
	 * @param root
	 *            directory in which to search.
	 * @param endFilter
	 * @return
	 */
	public static ArrayList<URL> listFiles(File root, String endFilter) {
		ArrayList<URL> results = new ArrayList<URL>();
		Mark.say(false, "root.isFile()", root.isFile(), root);
		Mark.say(false, "root.isDirectory()", root.isDirectory(), root);
		if (root.isFile()) {
			try {
				results.add(root.toURI().toURL());
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return results;
		}
		else if (root.isDirectory()) {
			// Mark.say("***** yyyyy", root.isDirectory(), root);
			Collection<File> matches = FileUtils
			        .listFiles(root, FileFilterUtils.suffixFileFilter(endFilter, IOCase.INSENSITIVE), TrueFileFilter.INSTANCE);
			Mark.say("Size", matches.size());
			for (File f : matches) {
				try {
					// Mark.say("***** zzzzz");
					results.add(f.toURI().toURL());
				}
				catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
			return results;
		}
		return results;
	}

	/**
	 * Recursively list all files inside a given directory whose names end with endFilter. See
	 * {@link #listFiles(String, String)}.
	 * 
	 * @param root
	 *            URL to a directory in a JarFile to use as the root.
	 * @param endFilter
	 * @param jarFile
	 * @param jarFileURL
	 * @return
	 */
	public static ArrayList<URL> listFiles(URL root, String endFilter, JarFile jarFile, URL jarFileURL) {
		ArrayList<URL> matches = new ArrayList<URL>();
		Enumeration<JarEntry> entries = jarFile.entries();
		String jarRoot = "jar:" + jarFileURL.toString() + "!/";
		while (entries.hasMoreElements()) {
			JarEntry e = entries.nextElement();
			String entryName = e.getName();
			if ((jarRoot + entryName).startsWith(root.toString()) && entryName.toLowerCase().endsWith(endFilter.toLowerCase())) {
				URL match;
				try {
					match = new URL(jarRoot + entryName);
					matches.add(match);
				}
				catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
		}
		return matches;
	}

	public static void main(String[] args) throws IOException {
		System.out.println(PathFinder.storyRootURL());
		System.out.println(PathFinder.storyURL("Start experiment"));
		System.out.println(IOUtils.toString(PathFinder.storyURL("Start experiment").openStream()));
		System.out.println(PathFinder.storyURL("Macbeth plot"));
	}
}