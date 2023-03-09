package memory.time;
import java.util.Comparator;

import frames.entities.Entity;
/**
 * Compares two Things based on their timestamp in the memory's timeline.
 * 
 * @author sglidden
 *
 */
@Deprecated
public class TimeComparator implements Comparator<Entity> {
	public int compare(Entity o1, Entity o2) {
		TimeLine line = TimeLine.getTimeLine();
		if (line.getTimestamp(o1)==0 || line.getTimestamp(o2)==0) {
			return 0;
		}
		if (line.getTimestamp(o1) < line.getTimestamp(o2)) {
			return -1;
		}
		else if (line.getTimestamp(o1) > line.getTimestamp(o2)) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
