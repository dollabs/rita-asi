package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	core.StoryTests.class,
	utils.PathFinderTests.class
	})

public class QuickTests {

}
