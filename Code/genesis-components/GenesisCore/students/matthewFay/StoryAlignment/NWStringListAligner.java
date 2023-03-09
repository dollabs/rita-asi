package matthewFay.StoryAlignment;

import java.util.ArrayList;
import utils.Mark;

public class NWStringListAligner extends NWAligner<String, String> {

	public NWStringListAligner() {
		setGapPenalty(0);
	}
	
	@Override
	public float sim(String a, String b) {
		if(a.equals(b))
			return 1;
		return -1;
	}
	
	public static void main(String args[]) {
		ArrayList<String> list1 = new ArrayList<String>();
		ArrayList<String> list2 = new ArrayList<String>();
		
		list1.add("Hello");
		list1.add("World");
		list1.add("Foo");
//		list1.add("Bar");
		list2.add("Hello");
//		list2.add("World");
		list2.add("Foo");
		list2.add("Bar");
		
		NWStringListAligner aligner = new NWStringListAligner();
		Mark.say(aligner.align(list1, list2));
		
	}

}
