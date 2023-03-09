package start;

import frames.entities.Entity;
import generator.*;
import translator.BasicTranslator;
import utils.Mark;

public class GeneratorTest {

	public static void main(String[] ignore) throws Exception {
		// StartServerBox.getStartServerBox();

		Generator generator = Generator.getGenerator();
		
		/*

		RoleFrame rf = new RoleFrame("boy", "kick", "ball");
		generator.test(rf, "The boy kicks the ball.");

		generator
		        .test(new RoleFrame("boy", "kick", "ball").addRole("with", "bat").addRole("toward", "fence"), "The boy kicks the ball with the bat toward the fence.");

		generator
		        .test(new RoleFrame("boy", "kick", "ball").addRole("with", "knife").makeNegative().makePast().makePassive(), "The ball wasn't kicked by the boy with the knife.");

		RoleFrame person1 = new RoleFrame("man").addFeature("large").makeIndefinite();
		RoleFrame person2 = new RoleFrame("person").addFeature("second");
		RoleFrame object1 = new RoleFrame("phone").addFeature("heavy").addFeature("black").addFeature("cell").makeIndefinite();

		// Test
		// Generator.getGenerator().test("a large man gave a large black cell phone to a second person");

		generator
		        .test(new RoleFrame(person1, "give", object1).addRole("to", person2).makePast(), "A large man gave a heavy black cell phone to the second person.");

		RoleFrame f1 = new RoleFrame(person1, "hide").makeFuture();
		RoleFrame f2 = new RoleFrame("soldier", "stop");
		f2.makePresent();
		generator.test(f1, "A large man will hide.");
		generator.test(f2, "The soldier stops.");

		generator.test(f1.connect("before", f2), "A large man will hide before the soldier stops.", true);

		Entity dog = new Entity("dog").possessor("boy").makeDefinite();

		RoleFrame run = new RoleFrame(dog, "ran_away").makePast();

		generator.test(run, "The boy's dog ran away.");

		run = new RoleFrame(dog, "run").addParticle("away").makePast().addModifier("quickly");

		generator.test(run, "The boy's dog ran away quickly.", true);

		generator.test(new RoleFrame("Estonia", "is", new Entity("friend").possessor("I")), "Estonia is my friend.");

		generator.test(new RoleFrame(new Entity("man"), "run"), "The man runs.");
		generator.test(new RoleFrame(new Entity("man").feature("large"), "run"), "The large man runs.");
		generator.test(new RoleFrame(new Entity("man").definite(), "run"), "The man runs.");
		generator.test(new RoleFrame(new Entity("man").indefinite(), "run"), "A man runs.");
		generator.test(new RoleFrame(new Entity("men").noDeterminer(), "run"), "Men run.");
		generator.test(new RoleFrame(new Entity("dog").possessor("man"), "run"), "The man's dog runs.");
		
		
		generator.test(new RoleFrame("man", "dig", "hole"), "The man digs the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").present(), "The man digs the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").past(), "The man dug the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").future(), "The man will dig the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").passive(), "The hole is dug by the man.");
		generator.test(new RoleFrame("man", "dig", "hole").may(), "The man may dig the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").negative(), "The man doesn't dig the hole.");
		generator.test(new RoleFrame("man", "dig", "hole").progressive(), "The man is digging the hole.");
		
		generator.test(new RoleFrame("man", "dig", "hole").modify("quickly"), "The man digs the hole quickly.");
		generator.test(new RoleFrame("man", "dig", "box").particle("up"), "The man digs up the box.");

		RoleFrame c1 = new RoleFrame("man", "ran");
		RoleFrame c2 = new RoleFrame("soldier", "appear");
		
		generator.test(c1.after(c2), "The man runs after the soldier appears.");
		
		generator.test(c1.before(c2), "The man runs before the soldier appears.");
		
		generator.test(c1.makeWhile(c2), "The man runs while the soldier appears.");
		
		generator.test(c1.because(c2), "The man runs because the soldier appears.");

		
		
		// Entity man = new Entity("man").indefinite().addFeature("large");
		
		Entity man = new Entity("man").indefinite().definite().another();

		generator.test(man, "another man", true);
		
		*/
		
		StartEntity x = new StartEntity("man");
		
		generator.test(x.addFeature("big"), "the big man", true);
		

		
		// Thread.sleep(10000);
		
		
	}
}