package jakeBarnwell.concept;

import utils.Mark;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class ProfessorTests {
	
	private static Professor prof;
	
	private static void reset() {
		prof.fire();
		prof.newConcept("tests");
	}
	
	private void learnYes(String s) {
		prof.learn(s, Charge.POSITIVE);
	}
	
	private void learnNo(String s) {
		prof.learn(s, Charge.NEGATIVE);
	}
	
	private void checkYes(String s) {
		assertTrue("\"" + s + "\" should be an example of the concept.", prof.ask(s));
	}
	
	private void checkNo(String s) {
		assertFalse("\"" + s + "\" should NOT be an example of the concept.", prof.ask(s));
	}
	
	@Rule
	public TestWatcher watcher = new TestWatcher() {
	   protected void starting(Description description) {
	      Mark.par("Starting test: " + description.getMethodName());
	   }
	};
	
	@BeforeClass
	public static void setup() {
		prof = Professor.hire();
		prof.isPerson("John", "Mary", "Bob", "Sue", "Quinn", "Xavier");
	}
	
	@Before
	public void before() {
		prof.newConcept("tests");
	}
	
	@After
	public void after() {
		prof.fire();
	}
	
	@Test
	public void identity() {
		learnYes("Mary is harmed.");
		
		checkYes("Mary is harmed.");
	}
	
	@Test
	public void someoneIsHarmed() {
		learnYes("Mary is harmed.");
		learnYes("Bob is harmed.");
		
		checkYes("Sue is harmed.");
		checkNo("The dog is harmed.");
	}
	
	@Test
	public void pHarmsPgeneralizeObj() {
		learnYes("Mary harms Sue.");
		learnYes("Mary harms Bob.");
		
		checkYes("Mary harms John.");
		checkNo("Mary harms Foobar.");
		checkNo("Mary harms Paul.");
	}
	
	@Test
	public void pHarmsPgeneralizeSubj() {
		learnYes("Mary harms Sue.");
		learnYes("John harms Sue.");
		
		checkYes("Mary harms Sue.");
		checkYes("Bob harms Sue.");
		checkNo("Foobar harms Sue.");
		checkNo("The dog harms Sue.");
	}
	
	@Test
	public void pHarmsPgeneralizeSubjObj() {
		learnYes("Mary harms Sue.");
		learnYes("John harms Sue.");
		learnYes("John harms Bob.");
		
		checkYes("Sue harms Mary.");
		checkNo("Sue harms Paul.");
		checkNo("Foobar harms Sue.");
	}
	
	@Test
	public void pHarmsPbutCantHarmSelf() {
		learnYes("Mary harms Sue.");
		learnYes("Mary harms John.");
		learnYes("Sue harms John.");
		learnNo("Mary harms Mary.");
		
		checkYes("Bob harms Mary.");
		checkNo("Bob harms Bob.");
	}
	
	@Test
	public void pNotHarmsP() {
		learnYes("Mary does not harm Sue.");
		learnYes("Bob does not harm Sue.");
		
		checkYes("John does not harm Sue.");
		checkNo("Foobar does not harm Sue.");
	}
	
	@Test
	public void notIdentity() {
		learnYes("Mary does not harm Sue.");
		checkYes("Mary does not harm Sue.");
	}
	
	@Test
	public void notProperlyFails() {
		learnYes("Mary harms Sue.");
		
		checkNo("Mary does not harm Sue.");
	}
	
	@Test
	public void noNotProperlyFails() {
		learnYes("Mary does not harm Sue.");
		
		checkNo("Mary harms Sue.");
	}
	
	// TODO START parser issue. Once fixed, enable this test.
	@Ignore
	public void implicitSomebodyPassesExplicit() {
		learnYes("Mary is harmed.");
		
		checkYes("Somebody harms Mary.");
	}
	
	// TODO START parser issue. Once fixed, enable this test.
	@Ignore
	public void explicitSomebodyPassesImplicit() {
		learnYes("Somebody harms Mary.");
		
		checkYes("Mary is harmed.");
	}
	
	@Test
	public void explicitSomebodyPassive() {
		learnYes("Mary is harmed by somebody.");
		
		checkYes("Somebody harms Mary.");
	}
	
	@Test
	public void explicitSomebodyActive() {
		learnYes("Somebody harms Mary.");
		
		checkYes("Mary is harmed by somebody.");
	}

	@Test
	public void recognizesSelf() {
		learnYes("Mary harms Mary.");
		checkYes("Mary harms herself.");
		reset();
		learnYes("Mary harms herself.");
		checkYes("Mary harms Mary.");
		reset();
		learnYes("Bob harms himself.");
		checkYes("Bob harms Bob.");
		reset();
		learnYes("Bob harms Bob.");
		checkYes("Bob harms himself.");
		reset();
		learnYes("The frog harms itself.");
		checkYes("The frog harms the frog.");
		reset();
		learnYes("The frog harms the frog.");
		checkYes("The frog harms itself.");
	}
	
	@Test
	public void generalizeSubjWithPreposPhrase() {
		learnYes("Mary stabs Bob with a knife.");
		learnYes("Mary stabs John with a knife.");
		
		checkYes("Mary stabs Sue with a knife.");
		checkYes("Mary stabs Bob with a knife.");
		checkNo("Sue stabs Bob with a knife.");
	}
	
	@Test
	public void preposPhraseUnnecessary() {
		learnYes("Mary stabs John with a knife.");
		learnYes("Mary stabs Bob with a knife.");
		learnYes("Mary stabs John.");
		
		checkYes("Mary stabs John with a knife.");
		checkYes("Mary stabs John.");
		checkYes("Mary stabs Bob.");
		checkYes("Mary stabs Sue.");
		checkYes("Mary stabs Sue with five spoons.");
		checkYes("Mary stabs Sue behind the door.");
		
	}
		
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(Professor.class);
	    for(Failure failure : result.getFailures()) {
	      Mark.say(failure.toString());
	    }
	}

}
