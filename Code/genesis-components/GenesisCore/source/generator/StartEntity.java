package generator;

import generator.*;

import java.util.ArrayList;

import utils.Mark;

/*
 * Syntactic sugar. Really a role frame. Created on Oct 11, 2011
 * @author PHW
 */

public class StartEntity extends RoleFrame {

	private ArrayList<String> features = new ArrayList<String>();

	private ArrayList<Restriction> restrictions = new ArrayList<Restriction>();

	private ArrayList<String> relations = new ArrayList<String>();

	private String determiner = "definite";

	private String number = null;

	private Object possessor = null;

	private ArrayList<RoleFrame> thats = new ArrayList<RoleFrame>();

	private ArrayList<RoleFrame> whiches = new ArrayList<RoleFrame>();

	private ArrayList<RoleFrame> whos = new ArrayList<RoleFrame>();

	private ArrayList<RoleFrame> whoms = new ArrayList<RoleFrame>();

	public static StartEntity makeStartEntity(Object object) {
		return new StartEntity(object);
	}

	public StartEntity(Object object) {
		super(object);
	}

	public StartEntity restrict(String preposition, StartEntity startEntity) {
		restrictions.add(new Restriction(preposition, startEntity));
		return this;
	}

	public StartEntity that(RoleFrame that) {
		thats.add(that);
		return this;
	}

	public StartEntity which(RoleFrame which) {
		whiches.add(which);
		return this;
	}

	public StartEntity who(RoleFrame who) {
		whos.add(who);
		return this;
	}

	public StartEntity whom(RoleFrame whom) {
		whoms.add(whom);
		return this;
	}

	public StartEntity feature(String feature) {
		return addFeature(feature);
	}

	public StartEntity addFeature(String feature) {
		// rendering += makeProperty(head, extractHead("has_property"),
		// feature);
		features.add(feature);
		return this;
	}

	public StartEntity indefinite() {
		return makeIndefinite();
	}

	public StartEntity makeIndefinite() {
		// carefullyAddDeterminer(makeProperty(head, "has_det", "indefinite"));
		determiner = "indefinite";
		return this;
	}

	public StartEntity definite() {
		return makeDefinite();
	}

	public StartEntity makeDefinite() {
		// carefullyAddDeterminer(makeProperty(head, "has_det", "definite"));
		determiner = "definite";
		return this;
	}

	public StartEntity noDeterminer() {
		return makeNoDeterminer();
	}

	public StartEntity makeNoDeterminer() {
		// carefullyAddDeterminer(makeProperty(head, "has_det", "null"));
		determiner = "null";
		return this;
	}

	public StartEntity another() {
		return makeAnother();
	}

	public StartEntity makeAnother() {
		makeNoDeterminer();
		addFeature("another");
		return this;
	}

	public StartEntity plural() {
		return makePlural();
	}

	public StartEntity makePlural() {
		// rendering += makeProperty(head, "has_number", "plural");
		number = "plural";
		return this;
	}

	public StartEntity singular() {
		return makeSingular();
	}

	public StartEntity makeSingular() {
		// rendering += makeProperty(head, "has_number", "singular");
		number = "singular";
		return this;
	}

	public StartEntity possessor(Object possessor) {
		// rendering += makeProperty(head, extractHead("related-to"),
		// possessor);
		this.possessor = possessor;
		return this;
	}

	public StartEntity makePossessor(Object possessor) {
		return possessor(possessor);
	}

	public StartEntity addPossessor(Object possessor) {
		return possessor(extractHead(possessor));
	}

	public String getRendering() {
		String rendering = this.rendering;
		rendering += makeProperty(head, "has_det", determiner);
		for (String feature : features) {
			rendering += makeProperty(head, extractHead("has_property"), feature);
		}
		if (number != null) {
			rendering += makeProperty(head, "has_number", number);
		}
		if (possessor != null) {
			rendering += makeProperty(head, "related-to", extractHead(possessor));
		}
		for (RoleFrame r : thats) {
			rendering += makeProperty(head, extractHead("has_rel_clause"), r.getHead());
			rendering += makeProperty(r.getHead(), "has_clause_type", "that");
			rendering += r.getRendering();
		}
		for (RoleFrame r : whiches) {
			rendering += makeProperty(head, extractHead("has_rel_clause"), r.getHead());
			rendering += makeProperty(r.getHead(), "has_clause_type", "which");
			rendering += r.getRendering();
		}

		for (RoleFrame r : whos) {
			rendering += makeProperty(head, extractHead("has_rel_clause"), r.getHead());
			rendering += makeProperty(r.getHead(), "has_clause_type", "who");
			rendering += r.getRendering();
		}

		for (RoleFrame r : whoms) {
			rendering += makeProperty(head, extractHead("has_rel_clause"), r.getHead());
			rendering += makeProperty(r.getHead(), "has_clause_type", "whom");
			rendering += r.getRendering();
		}
		for (Restriction r : restrictions) {
			rendering += makeProperty(head, r.getConnection(), r.getEntity().getHead());
			rendering += r.getEntity().getRendering();
		}
		return rendering;
	}

	private class Restriction {
		private String connection;

		public String getConnection() {
			return connection;
		}

		public StartEntity getEntity() {
			return startEntity;
		}

		private StartEntity startEntity;

		public Restriction(String connection, StartEntity startEntity) {
			this.connection = connection;
			this.startEntity = startEntity;
		}

		public String toString() {
			return "[" + connection + " " + startEntity.getRendering() + "]";
		}
	}

	public static void main(String[] ignore) throws Exception {
		// StartServerBox.getStartServerBox();
		Generator generator = Generator.getGenerator();

		StartEntity x = new StartEntity("man-1").addFeature("tall");
		StartEntity y = new StartEntity("man-2").addFeature("short");
		StartEntity c = new StartEntity("woman");
		StartEntity p1 = new StartEntity("package");
		StartEntity p2 = new StartEntity("package");
		StartEntity p3 = new StartEntity("package").restrict("with", new StartEntity("ribbon").indefinite());
		StartEntity d = new StartEntity("child");

		p1.that(new RoleFrame(c, "bring", p1).past());

		p2.which(new RoleFrame(c, "bring", p2).past());

		d.whom(new RoleFrame(c, "bring", d).past());

		// x.definite().addFeature("big").addFeature("yellow");

		// x.definite().addFeature("big");

		RoleFrame g1That = new RoleFrame(x, "give", p1).past();

		g1That.addRole("to", y);

		g1That.addModifier("now");

		RoleFrame g2Which = new RoleFrame(x, "give", p2).past();

		g2Which.addRole("to", y);

		g2Which.addModifier("now");

		RoleFrame g3 = new RoleFrame(y, "kiss", d).past();

		for (int i = 0; i < 10; ++i) {
			// generator.test(g1That,
			// "The tall man gave the package that the woman brought to the short man now.",
			// true);

			// generator.test(g2Which,
			// "The tall man gave the package which the woman brought to the short man now.",
			// true);

			// generator.test(g2,
			// "The short man gave the package with a ribbon to the tall man.",
			// true);

			generator.test(g3, "The short man kissed the child whom the woman brought.", true);
		}

	}

	public void reset() {
		thats.clear();
		whiches.clear();
		whos.clear();
		whoms.clear();
	}

}
