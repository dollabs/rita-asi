package utils;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class PathFinderTests {

	@Test
	public void testStoryRoot() throws IOException{
		System.out.println(PathFinder.storyRootURL());
	}
	
	@Test
	public void testAbsoluteStorySearch() throws IOException{
		System.out.println(PathFinder.storyURL("stories/Start experiment.txt"));
	}
	
	@Test
	public void testRelativeStorySearch() throws IOException{
		System.out.println(PathFinder.storyURL("Start expEriment"));
	}
	
	@Test
	public void testRelativeStorySearch2() throws IOException {
		System.out.println(PathFinder.storyURL("Start experiment.TXT"));
	}

	@Test
	public void testTrickyStorySearch() throws IOException {
		System.out.println(PathFinder.storyURL("macbeth plot"));
	}
	
	@Test
	public void testStoryAccess() throws IOException{
		System.out.println(IOUtils.toString(PathFinder.storyURL("Start experiment").openStream()));
	}
	
	@Test
	public void testSearchFailure(){
		try{
			PathFinder.storyURL("Not a valid Storyalsidj");
			fail("Exception not Thrown for non-existant story");
		}
		catch (Exception e){
			// success 
			System.out.println("did not find non-existant story");
		}
	}
	
	@Test
	public void listResourceTest() throws IOException{
		ArrayList<URL> matches;
		matches = PathFinder.listFiles(PathFinder.lookupURL("images"), ".jpg");
		System.out.println(matches.size() + " matches found.");
			
		if (matches.isEmpty()){ 
				fail();
		}
	}
}
