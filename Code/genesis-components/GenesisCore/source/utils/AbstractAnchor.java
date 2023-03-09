package utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * I realized that anchors produce paths relative to the bin (output) directory, the evil
 * twin of the source directory.  I want my program to be able to edit resources in the 
 * source directory.  sure, it's a bad idea in general, but it's really OK. trust me.
 * @author adk
 *
 */
public abstract class AbstractAnchor {

	public static URL binToSource(URL pathURL){
		String path = binToSource(pathURL.toString());
		try {
			return new URL(path);
		} catch( MalformedURLException e){
			throw new RuntimeException(e);
		}
		
	}
	
	public static String binToSource(String path){
		if(path.contains("/bin/")){
			path = path.replaceFirst("/bin/", "/source/");
		}else if(path.contains("\\bin\\")){
			path = path.replaceFirst("\\\\bin\\\\", "\\source\\");
		}
		return path;
	}
}
