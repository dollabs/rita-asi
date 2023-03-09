/* *******
 * role frames: an actor along with an act or property 
 *     optionally various entities that fill various roles in the act.
 */
// Updated 4 Jan 2018

package zhutianYang.School;

import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import utils.Mark;
import utils.tools.JFactory;
import translator.Translator;
import utils.Mark;

/**
 * This is a practice of how the representational substrate works, 
 *  1. how to understand the inner language: the four java classes 
 *  2. the Translator and the Generator
 *  3. how to write more conveniently
 * of how to go from English to Genesis's inner language and back.
 * 
 * @author phw
 */

public class TestSubstrates {
	
	public static final boolean IS_TO_XML = false;

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// 1 Four Classes in Role Frame
		
		   // 1-1 Gray - Every instance has a unique name and bundle of threads.
			Entity x = new Entity ("ball");
			Mark.say("Entity:", x);
			Mark.say("Type:", x.getType());
			Mark.say("Verbose:", x.toXML());
			
			Entity y = new Entity ("ball");
			y.addType("baseball");
			y.addType("riceball");
			y.addType("goodevening");
			Mark.say("Entity:", y);
			Mark.say("Type:", y.getType());
			Mark.say("Verbose:", y.toXML());
			Mark.say(y.isA("ball"),"It is a ball");
			   
		   Entity x2 = new Entity ();
		   Mark.say(x2.toXML()); // default is "thing"
		   
		   
		   // 1-2 Blue - Functions represent Jackendoff’s places and path elements
		   Function f = new Function("in", x);
		   Mark.say("Function:", f);
		   Mark.say("Type:", f.getType());
		   Mark.say("Verbose:", f.toXML());
		   
		   // 1-3 Red - Relations adds an object slot to the Function class
		   Entity h = new Entity("heart");
		   Entity s = new Entity("skin");
		   Relation r = new Relation("between",h,s);
		   Mark.say("Relation:", r);
		   Mark.say("Type:", r.getType());
		   Mark.say("Verbose", r.toXML());
		   
		   // 1-4 Black - Sequences appear when Genesis produces role frames from English
		   // eg. John killed Peter with a knife,
		   //     Genesis produces a sequence containing two roles, 
		   //     one for the object, Peter, and one for the instrument, the knife.
		   Sequence roles = new Sequence("roles");
		   Entity Peter = new Entity("Peter");
		   Entity knife = new Entity("knife");
		   roles.addElement(Peter);
		   roles.addElement(knife);
		   Mark.say("Role:", roles);
		   Mark.say("Type:", roles.getType());
		   Mark.say("Verbose:", roles.toXML());
	
		   //         - Relations from Sequence
		   Relation k = new Relation("kill", new Entity("John"), roles);
		   Mark.say("Relation:", k);
		   
		   // first define a way of killing people
		   Sequence roles2 = new Sequence("roles");
		   roles2.addElement(new Function("object", new Entity("Peter")));
		   roles2.addElement(new Function("with", new Entity("knife")));
		   // then, let John kill it
		   Relation k2 = new Relation("kill", new Entity("John"), roles2);
		   Mark.say("Relation:", k2); 
		   
		   
		 
		// 3 Convenience constructors build role frames
		   
		// 3-1 RoleFrames .makeRoleFrame/ .addRole
		   
		   Entity John = new Entity("John");
		   Entity rf = RoleFrames.makeRoleFrame(John, "kill", Peter);
		   Mark.say(rf);
		   
		   //     updating
		   rf = RoleFrames.addRole(rf, "with", knife);
		   Mark.say(rf);
		   
		   
		// 3-2 JFactory .createPlace/ Path/ Trajectory
		   
		   // Create an entity
		   Entity tree = new Entity("tree");
		   // Create a place relative to entity
		   Function place = JFactory.createPlace("top", tree);
		   // Create an path element using place
		   Function origin = JFactory.createPathElement("from", place);
		   
		   // Create a path
		   Sequence path = JFactory.createPath();
		   // Add the origin (path element)
		   path.addElement(origin);
		   
		   // Create a trajectory using path and other entities
		   Entity trajectory = JFactory.createTrajectory(Peter, "fly", path);
		   
		   // Create another path element
		   Function destination =
		    JFactory.createPathElement("to", JFactory.createPlace("at", new Entity("rock")));
		   // Add it to the path
		   JFactory.addPathElement(path, destination);
		   
		   // Have a look
		   Mark.say("Amended trajectory role frame: " + trajectory);
	}

}
