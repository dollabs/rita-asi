package jessicaNoss;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.* ;
import static org.junit.Assert.* ;

/*
 * Created on June 22, 2015
 * @author jmn
 */
public class JmnUtilsTests {

	@Test
	public void convertBooleansToStrings_listOfBooleansWithFunction_listOfYesNo() {
		List<Boolean> booleans = Arrays.asList(false, true, true, false, true);
		Function<Boolean, String> fn = (b -> b ? "Yes" : "No");
		List<String> expected = Arrays.asList("No", "Yes", "Yes", "No", "Yes");
		List<String> result = JmnUtils.convertBooleansToStrings(booleans, fn);
		assertTrue("got " + result.toString(), expected.equals(result));
	}

	@Test
	public void convertBooleansToStrings_listOfBooleans_listOfYesNo() {
		List<Boolean> booleans = Arrays.asList(false, true, true, false, true);
		List<String> expected = Arrays.asList("No", "Yes", "Yes", "No", "Yes");
		List<String> result = JmnUtils.convertBooleansToStrings(booleans, "Yes", "No");
		assertTrue("got " + result.toString(), expected.equals(result));
	}

	@Test
	public void convertPresenceIDsToBooleans_listOfInts_listOfBooleans() {
		List<Integer> presenceIDs = Arrays.asList(0,1,2,5,6,8);
		List<Boolean> expected = Arrays.asList(true, true, true, false, false, true, true, false, true, false);
		List<Boolean> result = JmnUtils.convertPresenceIDsToBooleans(presenceIDs, 10);
		assertTrue("got " + result.toString(), expected.equals(result));
	}
}
