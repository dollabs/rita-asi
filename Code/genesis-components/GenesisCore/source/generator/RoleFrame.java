package generator;

import utils.Mark;

/*
 * Created on Feb 15, 2011
 * @author phw
 */

public class RoleFrame extends RoleFrameParent {

	protected String rendering = "";

	/*
	 * Blank
	 */
	public RoleFrame() {
	}

	/*
	 * This one is for objects
	 */
	public RoleFrame(Object object) {
		String s = (String) object;
		if (s.indexOf('+') < 0 && s.indexOf('-') < 0) {
			++index;
			s += "+" + index;
		}
		String o = extractHead(s);
		head = o;
	}

	/*
	 * This one is for events
	 */
	public RoleFrame(Object subject, Object action) {
		this(subject, action, null);
	}

	/*
	 * This one is for events
	 */
	public RoleFrame(Object subject, Object action, Object object) {
		String s = extractHead(subject);
		String a = extractNewHead(action);
		String o = extractHead(object);
		head = a;
		rendering += makeTriple(s, a, o);
		// Mark.say("New head is", head);
		if (object != null && o == null) {
			Mark.say("No head for", ((RoleFrame) object).rendering);
		}
	}

	public String head(Object o) {
		return head(o);
	}

	public String extractHead(Object o) {
		return extractHead(o, true);
	}

	public String extractNewHead(Object o) {
		return extractHead(o, false);
	}

	/*
	 * Extracts and adds rendering of argument to rendering of this role frame as well as returning head.
	 */
	public String extractHead(Object o, boolean newIndex) {
		if (o == null || "null".equals(o.toString())) {
			return null;
		}
		// Argument is another role frame
		else if (o instanceof RoleFrame) {
			String newStuff = ((RoleFrame) o).getRendering();
			if (rendering.indexOf(newStuff) < 0) {
				rendering += newStuff;
			}
			return ((RoleFrame) o).getHead();
		}
		// Argument is a string, such as "ball"
		else if (o instanceof String) {
			if (newIndex) {
				return getIndexedWord((String) o, false);
			}
			else {
				return getIndexedWord((String) o);
			}
		}
		Mark.err("Unexpected type in RoleFrame.extractHead");
		return null;
	}

	public void setRendering(String rendering) {
		this.rendering = rendering;
	}

	public String getRendering() {
		return rendering;
	}

	public void addTriple(String triple) {
		rendering += triple;
	}

	public RoleFrame modifier(String feature) {
		return addModifier(feature);
	}

	public RoleFrame addAttitude(String feature) {
		rendering += makeProperty(head, "has_attitude", feature);
		return this;
	}

	public RoleFrame attitude(String feature) {
		return addAttitude(feature);
	}

	/*
	 * At one point there were only RoleFrame instances. Seems more natural to have Entity as well. These methods are
	 * retained so that legacy code will work for a while
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame feature(String feature) {
		return addFeature(feature);
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame addFeature(String feature) {
		rendering += makeProperty(head, extractHead("has_property"), feature);
		return this;
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame indefinite() {
		return makeIndefinite();
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame makeIndefinite() {
		carefullyAddDeterminer(makeProperty(head, "has_det", "indefinite"));
		return this;
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame definite() {
		return makeDefinite();
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame makeDefinite() {
		carefullyAddDeterminer(makeProperty(head, "has_det", "definite"));
		return this;
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame noDeterminer() {
		return makeNoDeterminer();
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame makeNoDeterminer() {
		carefullyAddDeterminer(makeProperty(head, "has_det", "null"));
		return this;
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame another() {
		return makeAnother();
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame makeAnother() {
		makeNoDeterminer();
		addFeature("another");
		return this;
	}

	protected RoleFrame carefullyAddDeterminer(String triple) {
		// Mark.say("Adding", triple, "to", rendering);
		if (rendering.contains(triple)) {
		}
		else if (rendering.contains("has_det")) {
			StringBuffer buffer = new StringBuffer(rendering);
			int index1 = buffer.indexOf("has_det");
			int index2 = buffer.indexOf("]", index1);
			int index3 = triple.indexOf("has_det");
			int index4 = triple.indexOf("]", index3);
			buffer.replace(index1, index2, triple.substring(index3, index4));
			rendering = buffer.toString();
		}
		else {
			rendering += triple;
		}
		return this;
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame plural() {
		return makePlural();
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame makePlural() {
		rendering += makeProperty(head, "has_number", "plural");
		return this;
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame singular() {
		return makeSingular();
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame makeSingular() {
		rendering += makeProperty(head, "has_number", "singular");
		return this;
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame possessor(Object possessor) {
		rendering += makeProperty(head, extractHead("related-to"), possessor);
		return this;
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame makePossessor(Object possessor) {
		return possessor(possessor);
	}

	/*
	 * @deprecated Should be applied to Entity instances only in future.
	 */
	@Deprecated
	public RoleFrame addPossessor(Object possessor) {
		return possessor(extractHead(possessor));
	}

	// End of legacy methods

	public RoleFrame addInternalModifier(String feature) {
		rendering += makeProperty(head, extractHead("has_modifier"), feature);
		// rendering += makeProperty("has_modifier", "has_position", "trailing");
		rendering += makeProperty("has_modifier", "has_position", "mid_verbal");
		return this;
	}

	public RoleFrame addModifier(String feature) {
		rendering += makeProperty(head, extractHead("has_modifier"), feature);
		// rendering += makeProperty("has_modifier", "has_position", "trailing");
		// Caused adverb to appear both leading and trailing 18 April 2017
		// rendering += makeProperty("has_modifier", "has_position", "leading");
		return this;
	}

	public RoleFrame modify(String feature) {
		addModifier(feature);
		return this;
	}

	public RoleFrame addTrailingModifier(String feature) {
		rendering += makeProperty(head, extractHead("has_modifier"), feature);
		rendering += makeProperty("has_modifier", "has_position", "trailing");
		return this;
	}

	public RoleFrame addParticle(String particle) {
		String x = (String) head;
		int index = x.lastIndexOf('+');
		head = x.subSequence(0, index) + "_" + particle + x.substring(index);
		StringBuffer b = new StringBuffer(rendering);
		index = -1;
		while ((index = b.indexOf(x)) >= 0) {
			b.replace(index, index + x.length(), (String) head);
		}
		rendering = b.toString();
		return this;
	}

	public RoleFrame particle(String particle) {
		addParticle(particle);
		return this;
	}

	public RoleFrame role(String role, Object entity) {
		return addRole(role, entity);
	}

	public RoleFrame addRole(String role, Object entity) {
		String e = extractHead(entity);
		rendering += makeTriple(head, role, e);
		return this;
	}

	// public RoleFrame connect(String role, RoleFrame roleFrame) {
	// RoleFrame result = new RoleFrame();
	// String newRendering = rendering;
	// newRendering += roleFrame.getRendering();
	// newRendering += makeTriple(head, role, roleFrame.getHead());
	// newRendering += makeProperty(role, "is_clausal", "Yes");
	// result.setRendering(newRendering);
	// return result;
	// }

	public RoleFrame connect(String role, RoleFrame roleFrame) {
		RoleFrame result = new RoleFrame(this, role, roleFrame);
		return result;
	}

	public RoleFrame makeAfter(RoleFrame roleFrame) {
		return connect("after", roleFrame);
	}

	public RoleFrame after(RoleFrame roleFrame) {
		return connect("after", roleFrame);
	}

	public RoleFrame makeBefore(RoleFrame roleFrame) {
		return connect("before", roleFrame);
	}

	public RoleFrame before(RoleFrame roleFrame) {
		return connect("before", roleFrame);
	}

	public RoleFrame makeWhile(RoleFrame roleFrame) {
		return connect("while", roleFrame);
	}

	public RoleFrame makeBecause(RoleFrame roleFrame) {
		return connect("because", roleFrame);
	}

	public RoleFrame because(RoleFrame roleFrame) {
		return connect("because", roleFrame);
	}

	// public RoleFrame makeIf(RoleFrame f) {
	// return connect("if", this);
	// }
	//
	// public RoleFrame makeThen(RoleFrame f) {
	// return f.connect("then", this);
	// }

	public RoleFrame then(RoleFrame f) {
		return f.connect("then", this);
	}

	// Embedding

	public RoleFrame partOf(RoleFrame whole) {
		RoleFrameGrandParent part = new RoleFrameGrandParent("part+1");
		RoleFrame roleFrame = new RoleFrame(this.getHead(), "is-a", part.getHead());
		roleFrame.rendering += makeTriple(part.getHead(), "of+1", whole.getHead());
		roleFrame.rendering += makeProperty(part.getHead(), "has_det", "null");
		return roleFrame;
	}

	// Embedding

	public RoleFrame embed(String embedding, RoleFrame roleFrame) {
		return embed(this, embedding, roleFrame);
	}

	private RoleFrame embed(RoleFrame subject, String embedding, RoleFrame roleFrame) {
		RoleFrame result = new RoleFrame(subject.getHead(), embedding, roleFrame.getHead());
		result.rendering += subject.getRendering();
		result.rendering += roleFrame.getRendering();
		return result;
	}

	public RoleFrame believe(RoleFrame roleFrame) {
		return embed(this, "believe", roleFrame);
	}

	/**
	 * Combination Fancied up by phw 9/5/2013 because Abdi's clause hack will otherwise put multiple triples into the
	 * role frame rendering.
	 * 
	 * @param roleFrame
	 * @return
	 */
	public RoleFrame combine(RoleFrame roleFrame) {
		String[] triples = roleFrame.rendering.split("]");
		for (String t : triples) {
			t += ']';
			if (!rendering.contains(t)) {
				rendering += t;
			}
			else {
				// Mark.say("Noting", rendering, "constraints", t);
			}
		}
		// rendering += roleFrame.rendering;
		return this;
	}

	public RoleFrame makeQuestion() {
		rendering += makeProperty(head, "is_question", "yes");
		return this;
	}

	public RoleFrame makeWhyQuestion() {
		rendering += makeProperty(head, "is_question", "yes");
		rendering += makeProperty(head, "has_purpose", "why");
		return this;
	}

	public RoleFrame makeWhatIfQuestion() {
		rendering += makeProperty(head, "is_question", "yes");
		rendering += makeProperty("happen+1", "if+1", head);
		rendering += makeProperty("what+1", "happen+1", null);
		return this;
	}

	public RoleFrame makeHowQuestion() {
		rendering += makeProperty(head, "is_question", "yes");
		rendering += makeProperty(head, "has_method", "how");
		return this;
	}

	// Arbitrary addition

	public RoleFrame addDecoration(String x, String y, String z) {
		rendering += makeProperty(x, y, z);
		return this;
	}

	// Decorations

	public RoleFrame progressive() {
		return makeProgressive();
	}

	public RoleFrame makeProgressive() {
		rendering += makeTriple(head, "is_progressive", "Yes");
		return this;
	}

	public RoleFrame present() {
		return makePresent();
	}

	public RoleFrame makePresent() {
		rendering += makeProperty(head, "has_tense", "present");
		return this;
	}

	public RoleFrame past() {
		return makePast();
	}

	public RoleFrame makePast() {
		rendering += makeProperty(head, "has_tense", "past");
		return this;
	}

	public RoleFrame future() {
		return makeFuture();
	}

	public RoleFrame makeFuture() {
		Mark.say("Calling future");
		// rendering += makeProperty(head, "has_tense", "future");
		rendering += makeProperty(head, "has_modal", "will");
		return this;
	}

	public RoleFrame perfective() {
		return makePerfective();
	}

	public RoleFrame makePerfective() {
		rendering += makeProperty(head, "is_perfective", "yes");
		return this;
	}

	public RoleFrame imperative() {
		return makeImperative();
	}

	public RoleFrame makeImperative() {
		rendering += makeProperty(head, "is_imperative", "yes");
		return this;
	}

	public RoleFrame negative() {
		return makeNegative();
	}

	public RoleFrame makeNegative() {
		rendering += makeProperty(head, "is_negative", "Yes");
		return this;
	}

	public RoleFrame passive() {
		return makePassive();
	}

	public RoleFrame makePassive() {
		rendering += makeProperty(head, "has_voice", "passive");
		return this;
	}

	public RoleFrame may() {
		return makeMay();
	}

	public RoleFrame makeMay() {
		rendering += makeProperty(head, "has_modal", "may");
		return this;
	}

	public RoleFrame must() {
		return makeMust();
	}

	public RoleFrame makeMust() {
		rendering += makeProperty(head, "has_modal", "must");
		return this;
	}

	public RoleFrame makeModal(String modal) {
		rendering += makeProperty(head, "has_modal", modal);
		return this;
	}

	public RoleFrame that() {
		return makeThat();
	}

	public RoleFrame makeThat() {
		rendering += makeProperty(head, "has_clause_type", "that");
		return this;
	}

	public RoleFrame to() {
		return makeTo();
	}

	public RoleFrame makeTo() {
		rendering += makeProperty(head, "has_tense", "to");
		return this;
	}

	// Mind's this eye
	public RoleFrame addAtTime(long time) {
		addRole("at", time);
		return this;
	}

	// For Mind's eye
	public RoleFrame addFromTime(long time) {
		addRole("from", time);
		return this;
	}

	// For Mind's eye
	public RoleFrame addToTime(long time) {
		addRole("to", time);
		return this;
	}

	public void addQuantity(int i) {
		// Mark.say("Adding quantity before", rendering);
		rendering += makeProperty(head, "has_quantity", Integer.toString(i));
		// Mark.say("Adding quantity after", rendering);
	}

	public void addQuantity(String i) {
		// Mark.say("Adding quantity before", rendering);
		rendering += makeProperty(head, "has_quantity", i);
		// Mark.say("Adding quantity after", rendering);
	}

	public Object clone() {
		RoleFrame result = new RoleFrame();
		result.setRendering(rendering);
		return result;
	}

	public static void main(String[] ignore) throws Exception {
		Mark.say("Hello world");
		Mark.say(new RoleFrame("John", "kiss", "Mary"));
	}

}
