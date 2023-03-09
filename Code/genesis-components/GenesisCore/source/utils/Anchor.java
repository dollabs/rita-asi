package utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

// @author mtklein
// Extend this to make getting resources dead-easy
// Supports odd paths with spaces, etc.

// Ex:  public FooAnchor extends Anchor { }
// then somewhere else,
// new FooAnchor().get("bar.txt")
// gives back the full path of bar.txt in the same directory as FooAnchor

public class Anchor {
	
	public String source(){
		return source("");
	}
	
	public String source(final String resource){
		return get(resource).replace("binary", "source");
	}
	
	public String get() {
		return get("");
	}
	public String get(final String resource) {
		final URL url = this.getClass().getResource(resource);
		try {
			if (url == null) {
				return "";
			}
			final String decoded = URLDecoder.decode(url.getPath(), "UTF-8");
			return decoded;
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
}
