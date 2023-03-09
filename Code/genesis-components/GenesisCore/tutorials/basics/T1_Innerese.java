package basics;

import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Relation;
import frames.entities.Sequence;
import generator.Generator;
import generator.RoleFrames;
import utils.Mark;
import utils.tools.Constructors;

public class T1_Innerese {
	
	/*
	
	Innerese is the semantic structure used by Genesis System. 
	It is based on what linguists call "thematic role frames," or role frames for short.
	
	There are four classes of Innerese frames:
	
	1. Entity: A entity is usually a physical object. So called so as not to conflict with Java’s Object class.
			e.g., (ent ball)
	
	2. Function: A function suggests the thematic role served by certain frame. 
			e.g., (fun on (ent table)), (fun object (ent apple))
	
	3. Relation: A relation indicates how one frame is connected to another frame.
			e.g., (rel sleep (ent john)), (cause (rel sleep (ent john)) (rel smile (ent john)) )
	
	4. Sequence: A sequence contains any number of frames.
			e.g., (seq conjuction (rel sleep (ent john)) (rel snore (ent john)))
			e.g., (rel sleep (ent john) (seq roles (fun at (ent bed)) (fun during (ent break))))
	
	The class heritage is:
	
	       Entity
	       __|__
	      /     \
	 function  sequence
	     |
	 relation
	
	*/

	public static void main(String[] args) {
		
		// comment and uncomment the following lines to go through this tutorial
		demoEntity();
//		demoFunction();
//		demoRelation();
//		demoSequence();
//		demoConstruction();
		
	}
	
	public static void demoEntity() {
		
		// The most fundamental class is the Entity class. 
		// Create a new entity and inspect the result via print statements:
		Entity x = new Entity ("ball");
		Mark.say(x);
		
		// Every instance of Entity has a unique name and bundle of threads. 
		Mark.say(x.toXML());;
		
		// In the thread bundle, the first thread is considered the primed thread.
		// Because entities have threads, so do functions, relations, and sequences. 
		Mark.say(x.getPrimedThread());
		
		// Whenever you add a type to a entity, that new type goes to the end of the primed thread
		x.addType("baseball");
		Mark.say(x.getThread("thing"));
		
		// Test for class membership by checking all the threads
		Mark.say(true, x.isA("ball"));
		Mark.say(true, x.isA("person"));
		Mark.say("===========================================================\n\n");
	}
	
	public static void demoFunction() {
		
		// The Function class represents Jackendoff’s places and path elements. 
		// To construct a function that represents the top of a table:
		Entity e = new Entity("table");
		Function f = new Function("top", e);
		Mark.say(f);
		
		// The Function class provides getSubject and setSubject methods. 
		Mark.say(f.getSubject());
		f.setSubject(new Entity ("ball"));
		Mark.say(f);
		Mark.say("===========================================================\n\n");
		
	}
	
	public static void demoRelation() {
		
		// The Relation class simply adds an object slot to the Function class, 
		// It provides getObject and setObject methods.
		Entity d = new Entity("door");
		Entity w = new Entity("window");
		Relation r = new Relation("between", d, w);
		Mark.say(r);
		Mark.say(r.getSubject());
		Mark.say(r.getObject());
		Mark.say("===========================================================\n\n");
		
	}
	
	public static void demoSequence() {
		
		// An Innerese representation of a sentence may consists of a sequence of relations. 
		// A relation may exists between an entity and a sequence of functions.
		
		// For example, Given John killed Peter with a knife, Genesis produces a sequence 
		// containing two roles, one for the object, Peter, and one for the instrument, the knife.
		Sequence roles = new Sequence("roles");
		roles.addElement(new Function("object", new Entity("Peter")));
		roles.addElement(new Function("with", new Entity("knife")));
		Mark.say(roles);
		
		Relation k = new Relation("kill", new Entity("John"), roles);
		Mark.say(k);
		
	}
	
	public static void demoConstruction() {
		
		// To construct an Innerese frame yourself based on the above definitions 
		// can be tedious and error prone. Fortunately there are convenience constructors 

		// Method uses word net threads for Peter and Paul.
		Entity e = RoleFrames.makeRoleFrame("Peter", "slept");

		Mark.say("Innerese:", e);
		Mark.say("Translation:", Generator.getGenerator().generate(e));

		// With object
		e = RoleFrames.makeRoleFrame("Peter", "stabbed", "Paul");

		Mark.say("Innerese:", e);
		Mark.say("Translation:", Generator.getGenerator().generate(e));

		// Alternatively, entities are ok instead of strings

		Entity x = RoleFrames.makeEntity("Mary");

		e = RoleFrames.makeRoleFrame("Peter", "stabbed", x);

		Mark.say("Innerese:", e);
		Mark.say("Translation:", Generator.getGenerator().generate(e));

		// Ok to add other roles

		RoleFrames.addRole(e, "with", "knife");

		Mark.say("Innerese:", e);
		Mark.say("Translation:", Generator.getGenerator().generate(e));
		
	}


}
