package frames;
import java.util.*;


import connections.WiredViewer;
import frames.entities.Entity;
import frames.entities.Sequence;
import gui.EventViewer;
public class EventFrame extends Frame {
	public static String	FRAMETYPE	= "event";
	private List<Entity>		things		= new ArrayList<Entity>();
	public EventFrame() {
	}
	public EventFrame(Entity thing) {
		assert thing.isA(EventFrame.FRAMETYPE);
		this.things = thing.getElements();
	}
	public EventFrame(List<Entity> things) {
		this.things.addAll(things);
	}
	public void add(Entity thing) {
		this.things.add(thing);
	}
	@Override
	public Entity getThing() {
		Sequence s = new Sequence(EventFrame.FRAMETYPE);
		for (Entity t : this.things) {
			s.addElement(t);
		}
		return s;
	}
	@Override
	public WiredViewer getThingViewer() {
		return new EventViewer();
	}
}
