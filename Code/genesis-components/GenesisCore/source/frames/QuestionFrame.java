package frames;

import frames.entities.Entity;
import frames.entities.Function;

/**
 * Represents a question. Has a question word/type, and an internal frame.
 * 
 * @author sglidden
 */
public class QuestionFrame extends Frame {
	public static String	FRAMETYPE	= "question";

	// question keywords
	public enum Type {
		whereIs, whatIs, did, fromWhereDid, toWhereDid
	}

	private Type	type;
	private Entity	query;

	public QuestionFrame(Type t, Entity query) {
		type = t;
		this.query = query;
	}

	public QuestionFrame(Entity thing) {
		for (Type type : Type.values()) {
			if (thing.isA(type.toString())) {
				this.type = type;
			}
		}
		assert type != null;
		assert thing instanceof Function;
		query = thing.getSubject();
	}

	@Override
	public Entity getThing() {
		Function result = new Function(query);
		result.addTypes("thing", QuestionFrame.FRAMETYPE + " " + type.toString());
		return result;
	}

	public Type getType() {
		return type;
	}

	public Entity getQuery() {
		return query;
	}
}
