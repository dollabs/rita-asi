package viz.videos;

import java.io.*;
import java.net.URL;

import frames.entities.Entity;

/*
 * Created on Oct 29, 2007 @author phw
 */

public class MovieLink {
	private String phrase = null;

	private Entity representation = null;

	private String videoFrames;

	private MovieDescription movieDescription;

	public MovieLink(String phrase, MovieDescription movieDescription) {
		this.phrase = phrase;
		this.movieDescription = movieDescription;
	}

	// public MovieLink(String phrase, Thing representation, MovieDescription
	// movieDescription) {
	// this(phrase, movieDescription);
	// this.representation = representation;
	// }

	public MovieLink(String phrase, MovieDescription movieDescription, String frames) {
		this(phrase, movieDescription);
		this.videoFrames = frames;
	}

	public MovieDescription getMovieDescription() {
		return movieDescription;
	}

	public URL getUrl() {
		return getMovieDescription().getUrl();
	}

	public String getPhrase() {
		return phrase;
	}

	public void setPhrase(String phrase) {
		this.phrase = phrase;
	}

	public Entity getRepresentation() {
		return representation;
	}

	public void setRepresentation(Entity representation) {
		this.representation = representation;
	}

	public String getVideoFrames() {
		return videoFrames;
	}

	public String toString() {
		return "[" + phrase + " --> " + getUrl() + "]";
	}

}
