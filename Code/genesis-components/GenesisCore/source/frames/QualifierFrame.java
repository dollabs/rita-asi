package frames;
import frames.entities.Entity;
/**
 * Meta-Frame to hangle negation and other qualifiers to 
 * regular frames.
 * 
 * @author sglidden
 *
 */
public class QualifierFrame extends Frame {
	
	Entity thing;
	@Override
	public Entity getThing() {
		return thing;
	}
	
	/**
	 * @return Thing subject of the qualification
	 */
	public Entity getSubjectThing() {
		return thing.getSubject();
	}
	
	public String getQualification() {
		return thing.getBundle().getThread("Qualification").get(1);
	}
}
