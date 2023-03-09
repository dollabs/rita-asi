package viz.videos;

import genesis.GenesisGetters;
import gui.TabbedTextViewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

import utils.Html;
import utils.Punctuator;
import utils.Mark;
import utils.PathFinder;
import connections.AbstractWiredBox;
import connections.Connections;
import constants.Markers;
import frames.entities.Entity;
import frames.entities.Sequence;

/*
 * Created on Oct 4, 2008
 * @author phw
 */

public class MovieManager extends AbstractWiredBox {

	public static String CLEAR = "clear";

	class GetTheMovies extends java.lang.Thread {
		@Override
		public void run() {
			try {
				Connections.getPorts(MovieManager.this).transmit(TabbedTextViewer.TAB, "Video annotation");
				movieDescriptions = new ArrayList<MovieDescription>();

				List<URL> fileNames = PathFinder.listFiles("visualmemory/annotations", ".txt");
				List<URL> movieURLs = PathFinder.listFiles("visualmemory/videos", "mov");
				movieURLs.addAll(PathFinder.listFiles("visualmemory/videos", "mpg"));

				for (URL annotationURL : fileNames) {
					String txtFileName = FilenameUtils.getBaseName(URLDecoder.decode(annotationURL.toString(), "utf-8"));
					URL movieURL = PathFinder.lookupURL("visualmemory/videos/" + txtFileName + ".mov");
					if (movieURL == null) {
						movieURL = PathFinder.lookupURL("visualmemory/videos/" + txtFileName + ".mpg");
					}
					if (movieURL == null) {
						Mark.say("No movie named ", txtFileName);
					}
					Mark.say("MovieURL is " + movieURL);
					Connections.getPorts(MovieManager.this).transmit(Html.h1("Annotations of " + txtFileName + ":"));

					MovieDescription movieDescription = new MovieDescription(movieURL);
					movieDescriptions.add(movieDescription);
					Mark.say("Now movie count is", movieDescriptions.size(), annotationURL, "-->", movieURL);
					BufferedReader reader = new BufferedReader(new InputStreamReader(annotationURL.openStream()));
					String line;
					while ((line = reader.readLine()) != null) {
						String prefix = "summary:";
						boolean test = line.toLowerCase().startsWith(prefix);
						MovieLink movieLink = null;
						if (test) {
							line = line.substring(prefix.length()).trim();
							// System.out.println("Summarizing " + txtFileName +
							// ": " + line);
							movieLink = new MovieLink(line, movieDescription);
							movieDescription.addSummary(movieLink);
						}
						else if (line.indexOf(":") > 0) {
							int index = line.indexOf(":");
							String frames = line.substring(0, index);
							line = line.substring(index + 1).trim();
							// System.out.println("Indexing " + txtFileName +
							// ": " + line);
							movieLink = new MovieLink(line, movieDescription, frames);
							movieDescription.addEvent(movieLink);
						}
						if (movieLink != null) {
							String phrase = movieLink.getPhrase();
							Sequence sequence = null;
							Entity instantiation = null;

							sequence = gauntlet.getStartParser().parse(phrase);
							if (sequence != null) {
								instantiation = gauntlet.getNewSemanticTranslator().interpret(sequence);
							}
							if (instantiation.isA(Markers.SEMANTIC_INTERPRETATION)) {
								if (instantiation.sequenceP()) {
									Sequence s = (Sequence) instantiation;
									if (!s.getElements().isEmpty()) {
										instantiation = s.getElement(0);
									}
									else {
										continue;
									}
								}
								else {
									continue;
								}
							}
							if (instantiation != null) {
								movieLink.setRepresentation(instantiation);
								Connections.getPorts(MovieManager.this).transmit(Html.normal(Punctuator.addPeriod(phrase)));
							}
							else {
								System.out.println("No parse for " + phrase + "!!!");
							}
						}
					}
				}

			}

			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (gauntlet != null) {
					gauntlet.openInterface();
				}
			}
			Connections.getPorts(MovieManager.this).transmit(TabbedTextViewer.TAB, TabbedTextViewer.SILENCE);

			System.out.println("Loaded: " + movieDescriptions.size());
			// for (MovieDescription d : movieDescriptions) {
			// System.out.println("Description: " + d);
			// }

		}
	}

	public static void main(String[] ignore) throws URISyntaxException, IOException {
		new MovieManager().loadMovieDescriptions();
	}

	private GenesisGetters gauntlet;

	ArrayList<MovieDescription> movieDescriptions;

	private MovieManager() {
		super("Video manager");
	}

	public MovieManager(GenesisGetters genesisGetters) {
		super("Video manager");
		gauntlet = genesisGetters;
	}

	public ArrayList<MovieDescription> getMovieDescriptions() {
		return movieDescriptions;
	}

	public void loadMovieDescriptions() {
		try {
			if (gauntlet != null) {
				gauntlet.closeInterface();
			}
			java.lang.Thread thread = new GetTheMovies();
			thread.start();
		}
		catch (Exception e) {
		}
	}
}
