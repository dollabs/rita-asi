package viz.videos;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

/*
 * Created on Jun 25, 2008 @author phw
 */

public class MovieDescription {

	private ArrayList<MovieLink> summaries;

	private ArrayList<MovieLink> events;

	private URL url;

	private File file;

	public MovieDescription(URL url) {
		this.url = url;
	}

	public MovieDescription(File file) {
		this.file = file;
	}

	public ArrayList<MovieLink> getSummaries() {
		if (summaries == null) {
			summaries = new ArrayList<MovieLink>();
		}
		return summaries;
	}

	public void addSummary(MovieLink t) {
		getSummaries().add(t);
	}

	public ArrayList<MovieLink> getEvents() {
		if (events == null) {
			events = new ArrayList<MovieLink>();
		}
		return events;
	}

	public void addEvent(MovieLink t) {
		getEvents().add(t);
	}

	public String toString() {
		if (url == null) {
			return null;
		}
		int index = url.getPath().lastIndexOf('/');
		// return url.getPath().substring(index + 1);
		return url.getPath();
	}

	public URL getUrl() {
		return url;
	}

	public File getDirectory() {
		return file;
	}

}
