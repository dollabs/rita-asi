package memory.time;
import java.util.Date;

import frames.Frame;
import frames.entities.Entity;
/**
 * A timestamp.
 * 
 * @author Sam Glidden
 *
 */
@Deprecated
public class TimeInstant extends Frame {
	private Date time;
	public TimeInstant(Date time) {
		this.time = time;
	}
	public Date getDate() {
		return (Date) time.clone();
	}
	public Entity getThing() {
		return null;
	}
}
