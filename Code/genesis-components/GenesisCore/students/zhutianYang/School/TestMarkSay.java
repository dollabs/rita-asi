/**
 * 
 */
package zhutianYang.School;

import utils.Mark;

public class TestMarkSay {
	
	public static void main(String[] args) {
		
		// if the first argument in the Mark.say is boolean, print the elements after it only if it is "true"
		boolean yes = true;
		
	    Mark.say(yes); // ">>> "
	    System.out.println(yes); // "true"
	    
	    Mark.say(yes,yes); // ">>> true"
	}
	
	

}
