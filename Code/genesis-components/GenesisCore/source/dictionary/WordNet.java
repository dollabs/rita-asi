package dictionary;

import java.io.*;
import java.net.URL;
import java.util.*;

import memory.ThreadMemory;
import utils.Mark;

import constants.Markers;
import dictionary.dict.DictionaryAnchor;
import edu.mit.jwi.*;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.*;
import edu.mit.jwi.morph.*;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Thread;

/*
 * Adapted from Adam Kraft's SophisticatedWordnetGentleman class so as to work with Mark Finlayson's more sophisticated
 * PennTag class Created on Nov 21, 2007
 * @author phw
 */

// mtklein
// Updated Nov. 26 2007 to match ThreadMemory interface
public class WordNet implements ThreadMemory {

	// static final String wordNetPath = "externalTools/WordNet-3.0/dict";

	// static final String wordNetPath = "source/WordNet-3.0/dict";

	// static final String wordNetPath = "dict";

	static final String wordNetPath = "links/words/dict/";

	// static final String wordNetPath = "WordNet";

	private Bundle getBundleForWord(final String word, final String pos_string) {
		final Bundle bucket = new Bundle();

		final POS pos = PennTag.convert(pos_string);

		if (pos == null) {
			return bucket;
		}

		final IIndexWord baseIndex = getFirstIndexWord(word, pos);
		if (baseIndex == null) {
			// System.err.println("word "+word+" is not OK");
			return bucket;
		}
		// else System.out.println("word "+word+" is OK");
		final List<ISynset> synsets = getSynsetsForWord(word, pos);
		for (ISynset synset : synsets) { // still only knows about multiple
			// BASE SYNSETS. no "branching"
			// supported yet
			final Entity placeHolder = new Entity(word);
			placeHolder.removeType("thing");
			placeHolder.getPrimedThread().addTypeFront(baseIndex.getLemma());
			final frames.entities.Thread primedThread = placeHolder.getPrimedThread();
			final List<ISynset> visited = new ArrayList<ISynset>();
			while (synset != null) {
				IWord w;
				try {
					w = synset.getWords().get(0);
				}
				catch (final Exception e) {
					System.err.println("Warning: ");
					e.printStackTrace();
					return bucket;
				}
				primedThread.addTypeFront(w.getLemma().replaceAll("_", "-"));
				// primedThread.addTypeFront(w.getLemma());
				try {
					final List<ISynsetID> related = synset.getRelatedSynsets(Pointer.HYPERNYM);
					// System.out.println(related[0]);
					synset = related.isEmpty() ? null : getDict().getSynset(related.get(0));
					if (visited.contains(synset)) {
						synset = null;
					}
					else {
						visited.add(synset);
					}
				}
				catch (final NullPointerException e) {
					synset = null;
				}
				catch (final ArrayIndexOutOfBoundsException e) {
					synset = null;
				}

			}
			if (pos == POS.NOUN) {
				primedThread.addTypeFront("thing");
			}
			else if (pos == POS.VERB) {
				primedThread.addTypeFront("action");
			}
			else if (pos == POS.ADJECTIVE) {
				// primedThread.addTypeFront("feature");
				primedThread.addTypeFront(Markers.DESCRIPTOR);
			}
			else if (pos == POS.ADVERB) {
				primedThread.addTypeFront("feature");
			}
			else {
				Mark.err("Bugger all!");
			}
			bucket.add(primedThread);
			// bucket.prune(); //don't want to prune: simple threads will get
			// munged into more complicated ones, i.e. "change"
		}
		return bucket;
	}

	private IIndexWord getFirstIndexWord(final String wordStr, final POS pos) {
		List<String> allStems = getStemmer().findStems(wordStr, pos);
		if (allStems == null) {
			allStems = Collections.emptyList();
		}
		// System.out.println(allStems);
		for (final String candidate : allStems) {
			final IIndexWord idxWord = getDict().getIndexWord(candidate, pos);
			if (idxWord != null) {
				return idxWord;
			}
		}
		final IIndexWord idxWord = getDict().getIndexWord(wordStr, pos);
		return idxWord;
	}

	private SimpleStemmer sstem;

	protected SimpleStemmer getStemmer() {
		if (sstem == null) {
			sstem = new WordnetStemmer(getDict());
		}
		return sstem;
	}

	private IDictionary idict;

	protected IDictionary getDict() {
		if (idict == null) {
			try {
				idict = new Dictionary(fileDict(wordNetPath));
				idict.open();
			}
			catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return idict;
	}

	// if necessary, copies wordnet from the jar to the
	// local filesystem, so we can use JWI:
	public final static String TMP_PATH = "GenesisCore/";

	/**
	 * This had to be redone, painfully, because retrieve directory from jar file and iterate over it using webstart,
	 * which is the only reason to get the files out of the jar file in the first place. So, wiring in all the file
	 * names.
	 */

	private static URL fileDict(String dictPath) throws IOException {

		boolean debug = false;
		URL jarDict = WordNet.class.getClassLoader().getResource(dictPath);

		// URL testDict = DictionaryAnchor.class.getResource("DictionaryAnchor.class");

		URL testDict = WordNet.class.getResource("dict/");

		// if (jarDict.getProtocol().equals("bundleresource")){
		// jarDict = ((BundleURLConnection)
		// jarDict.openConnection()).getLocalURL();
		// }

		// testDict = new URL(testDict.getPath());

		// Mark.say("WordNet paths, wordNetPath/dictPath/jarDict/testDict:", wordNetPath, dictPath, jarDict);

		Mark.say(debug, "wordNetPath", wordNetPath);
		Mark.say(debug, "dictPath", dictPath);
		Mark.say(debug, "testDict", testDict);
		Mark.say(debug, "jarDict", jarDict);

		if (jarDict != null && jarDict.getProtocol().equals("file")) {// we're not even in a JAR
			Mark.say(debug, "Returning", jarDict);
			return jarDict;
		}

		File fileDir = new File(System.getProperty("java.io.tmpdir"), TMP_PATH);
		File fileDict = new File(fileDir, dictPath);

		Mark.say(debug, "fileDir", fileDir);
		Mark.say(debug, "fileDict", fileDict);

		Mark.say(debug, "Using local temp directory for WordNet: " + fileDict);

		// if directory already exists, we'll try to just use it
		if (fileDict.exists()) {
			return fileDict.toURI().toURL();
		}

		// See doc above

		// // otherwise we copy everything in our jar's DICT_PATH into tmpDict:
		// JarFile jar = ((JarURLConnection) jarDict.openConnection()).getJarFile();
		//
		// byte[] buf = new byte[1024];
		// Enumeration<JarEntry> entries = jar.entries();
		//
		// Mark.say("C");
		//
		// while (entries.hasMoreElements()) {
		// JarEntry entry = entries.nextElement();
		// String name = entry.getName();
		//
		// if (name.startsWith(dictPath) && !name.endsWith("/")) {
		// System.err.println("extracting: " + name);
		//
		// OutputStream fileOut = new FileOutputStream(new File(fileDir, name));
		// InputStream jarIn = jar.getInputStream(entry);
		//
		// int read = 0;
		// while (read >= 0) {
		// fileOut.write(buf, 0, read);
		// read = jarIn.read(buf);
		// }
		// fileOut.close();
		// jarIn.close();
		// }
		// }

		// See doc above

		copyFromJarToTemp("adj.exc", fileDict);
		copyFromJarToTemp("adv.exc", fileDict);
		copyFromJarToTemp("cntlist.rev", fileDict);
		copyFromJarToTemp("data.adj", fileDict);
		copyFromJarToTemp("data.adv", fileDict);
		copyFromJarToTemp("data.noun", fileDict);
		copyFromJarToTemp("data.verb", fileDict);
		copyFromJarToTemp("frames.vrb", fileDict);
		copyFromJarToTemp("index.adj", fileDict);
		copyFromJarToTemp("index.adv", fileDict);
		copyFromJarToTemp("index.noun", fileDict);
		copyFromJarToTemp("index.sense", fileDict);
		copyFromJarToTemp("index.verb", fileDict);
		copyFromJarToTemp("lexnames", fileDict);
		copyFromJarToTemp("log.grind.3.0", fileDict);
		copyFromJarToTemp("noun.exc", fileDict);
		copyFromJarToTemp("sentidx.vrb", fileDict);
		copyFromJarToTemp("sents.vrb", fileDict);
		copyFromJarToTemp("verb.exc", fileDict);

		Mark.say("Returning", fileDict.toURI().toURL());

		return fileDict.toURI().toURL();
	}

	private static void copyFromJarToTemp(String name, File fileDir) throws IOException {
//		Mark.say("Attempting to copy to", fileDir);
		byte[] buf = new byte[1024];
		URL source = DictionaryAnchor.class.getResource(name);
		InputStream sourceStream = source.openStream();
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		OutputStream destinationStream = new FileOutputStream(new File(fileDir, name));
		int read = 0;
		while (read >= 0) {
			destinationStream.write(buf, 0, read);
			read = sourceStream.read(buf);
		}
		destinationStream.close();
		sourceStream.close();
//		Mark.say("Copied", name, "to", fileDir);
	}

	private List<ISynset> getSynsetsForWord(final String word, final POS pos) {
		final IIndexWord idxWord = getFirstIndexWord(word, pos);
		final List<IWordID> wArr = idxWord.getWordIDs();
		final List<ISynset> synsets = new ArrayList<ISynset>();
		if (wArr == null) {
			return new ArrayList<ISynset>();
		}
		for (final IWordID wordID : wArr) {
			final IWord iword = getDict().getWord(wordID);
			final ISynsetID synsetID = iword.getSynset().getID();
			final ISynset synset = getDict().getSynset(synsetID);
			synsets.add(synset);
		}
		return synsets;
	}

	public void add(final String word, final Thread thread) {
		// Wordnet is read-only, so this is a no-op
	}

	/*
	 * Finds all threads of all types (noun, verb, etc.) and returns them as a Bundle object.
	 */
	public Bundle lookup(final String word) {
		Bundle result = new Bundle();
		for (final String tag : Arrays.asList("NN", "VB", "JJ", "RB")) {
			Bundle thisBundle = lookup(word, tag);
			if (thisBundle != null) {
				result.addAll(thisBundle);
				continue;
			}
		}
		return pruneBundle(result);
	}

	private Bundle pruneBundle(Bundle bundle) {
		Bundle result = new Bundle();
		for (Thread t : bundle) {
			if (!result.contains(t)) {
				result.addElement(t);
			}
		}
		return result;
	}

	/*
	 * Finds first thread of all types (noun, verb, etc.) and returns them as a Bundle object.
	 */
	public Bundle lookupFirstChoices(final String word) {
		Bundle result = new Bundle();
		for (final String tag : Arrays.asList("NN", "VB", "JJ", "RB")) {
			Bundle thisBundle = lookup(word, tag);
			if (thisBundle != null) {
				result.add(thisBundle.getPrimedThread());
				continue;
			}
		}
		return result;
	}

	/*
	 * Finds thread of the type specified (noun, verb, etc.)
	 */
	public Bundle lookup(final String word, final String partOfSpeech) {
		final Bundle result = getBundleForWord(word, partOfSpeech);
		if (result.isEmpty()) {
			return new Bundle();
		}
		return result;
	}

	/**
	 * Dwayne Reeves
	 */
	/*
	 * public Set<Thing> getHyponym(final String word, final POS pos) {
	 * Set<Thing> children = new HashSet<Thing>();
	 * IDictionary dict = getDict(); // get the synset
	 * IIndexWord idxWord = dict.getIndexWord(word, pos);
	 * IWordID wordID = idxWord.getWordIDs().get(0); // 1st meaning IWord iword = dict.getWord(wordID); ISynset synset =
	 * iword.getSynset(); // get the hyponyms
	 * List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPONYM);
	 * //print out each hypernym's id and synonyms
	 * List<IWord> words;
	 * for(ISynsetID sid : hypernyms){
	 * words = dict.getSynset(sid).getWords();
	 * iword = words.iterator().next();
	 * Bundle b = lookup(iword.getLemma(), "NN");
	 * children.add(new Thing(b));
	 * System.out.print(sid + " {");
	 * for(Iterator<IWord> i = words.iterator();
	 * i.hasNext();){
	 * System.out.print(i.next().getLemma());
	 * if(i.hasNext())
	 * System.out.print(", ");
	 * }
	 * System.out.println("}");
	 * }
	 * return children;
	 * }
	 */

}
