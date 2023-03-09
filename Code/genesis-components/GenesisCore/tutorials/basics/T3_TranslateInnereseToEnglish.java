package basics;

import frames.entities.Entity;
import generator.Generator;
import generator.RoleFrame;
import generator.RoleFrames;
import utils.Mark;

public class T3_TranslateInnereseToEnglish {

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		
		Generator g = Generator.getGenerator();
		
		// Make a role frame for demonstration
		Entity entity = RoleFrames.makeRoleFrame("John", "love", "Mary");
		Mark.say(entity);
		
		// Now, generate into English
		String english = g.generate(entity);
		Mark.say(english);
		
		// More complicated
		Entity entity2 = RoleFrames.makeRoleFrame("John", "want", entity);
		english = g.generate(entity2);
		Mark.say(english);
		
		
		/*** Construct frames using RoleFrame Class ***/

		// ---------- Example 1
		RoleFrame person1 = new RoleFrame("man").addFeature("thin").makeIndefinite();
		RoleFrame f1 = new RoleFrame(person1, "hide").makeFuture();
		RoleFrame f2 = new RoleFrame("truck", "appear");
		RoleFrame f3 = f1.connect("before", f2);
		Mark.say(g.generate(f3));

		// ---------- Example 2
		RoleFrame dog = new RoleFrame("dog").addPossessor("boy");
		RoleFrame run = new RoleFrame(dog, "run").makePast();
		english = g.generate(run);
		Mark.say(english);

		run.addParticle("away");
		english = g.generate(run);
		Mark.say(english);

		run.addModifier("quickly");
		english = g.generate(run);
		Mark.say(english);

		// put them all together
		dog = new RoleFrame("dog").addPossessor("boy");
		run = new RoleFrame(dog, "run").addParticle("away").addModifier("quickly").makePast();
		english = g.generate(run);
		Mark.say(english);

		// ---------- Example 3
		Mark.say(g.generate(new RoleFrame("boy", "hit", "ball").makeNegative().makePast().makePassive()));

		// ---------- Example 4
		person1 = new RoleFrame("man").addFeature("wealthy").makeIndefinite();
		RoleFrame person2 = new RoleFrame("person").addFeature("second");
		RoleFrame object1 = new RoleFrame("phone").addFeature("black").addFeature("cell").makeIndefinite();
		Mark.say(g.generate(new RoleFrame(person1, "give", object1).addRole("to", person2).makePast()));

	}

}
