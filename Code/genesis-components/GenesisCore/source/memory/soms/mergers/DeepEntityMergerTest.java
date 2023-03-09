package memory.soms.mergers;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import frames.entities.Thread;
import junit.framework.TestCase;
import memory.soms.metrics.EntityMetric;
/**
 * Tests DeepEntityMerger
 * 
 * @author sglidden
 *
 */
public class DeepEntityMergerTest extends TestCase {
	
	private Entity t1, t2, t3, t4, t5;
	EntityMetric tm = new EntityMetric();
	public void setUp() {
		t1 = new Entity();
		t1.addTypes("thing", "living animal human programmer male sam");
		t2 = new Entity();
		t2.addTypes("thing", "living animal human programmer male adam");
		t3 = new Entity();
		t3.addTypes("thing", "living animal human programmer female lucy");
		t4 = new Entity();
		t4.addTypes("thing", "living animal dog fido");
		t5 = new Entity();
		t5.addTypes("thing", "dead rotting corpse");
	}
	public void testThingMerge() {
		// some quick test of EntityMerger, because DeepEntityMerger
		// is very much dependent on it.
		Entity t12 = EntityMerger.merge(t1, t2);
		Entity desired = new Entity();
		desired.addTypes("thing", "living animal human programmer male");
		assertEquals(0., tm.distance(t12, desired));
		
		// make sure original things are untouched
		Entity t1a = new Entity();
		t1a.addTypes("thing", "living animal human programmer male sam");
		Entity t2a = new Entity();
		t2a.addTypes("thing", "living animal human programmer male adam");
		assertEquals(0., tm.distance(t1, t1a));
		assertEquals(0., tm.distance(t2, t2a));
	}
	
	public void testDeepThingMerger() {
		Function d1 = new Function(t1);
		Thread thread = new Thread();
		thread.add("thing");
		thread.add("worker");
		thread.add("lab");
		thread.add("csail");
		d1.addThread(new Thread(thread));
		Function d1a = (Function) d1.deepClone();
		Function d2 = new Function(t2);
		thread.add("winston");
		d2.addThread(new Thread(thread));
		Function d2a = (Function) d2.deepClone();
		
		Function d12 = (Function) DeepEntityMerger.merge((Entity)d1, (Entity)d2);
		
		Entity desiredThing = new Entity();
		desiredThing.addTypes("thing", "living animal human programmer male");
		Function desired = new Function(desiredThing);
		thread.remove("winston");
		desired.addThread(thread);
		
		assertEquals(0., tm.distance(desired, d12));
		assertEquals(0., tm.distance(d1, d1a));
		assertEquals(0., tm.distance(d2, d2a));
		
		// now test something a bit more complicated
		Sequence s1 = new Sequence();
		s1.addElement(d1);
		Bundle b = new Bundle();
		Thread thread2 = new Thread();
		thread2.add("thing");
		thread2.add("gamer");
		thread2.add("computer");
		thread2.add("starcraft");
		b.add(thread2);
		d1a.setBundle(b);
		s1.addElement(d1a);
		
		Sequence s2 = new Sequence();
		s2.addElement(d2);
		Sequence s12 = (Sequence) DeepEntityMerger.merge((Entity)s1, (Entity)s2);
		Sequence s12desired = new Sequence();
		s12desired.addElement(desired);
		
		assertEquals(0., tm.distance(s12, s12desired));
		Sequence s21 = (Sequence) DeepEntityMerger.merge((Entity)s2, (Entity)s1);
		Sequence s21desired = (Sequence) s1.deepClone();
		for (Entity tempD : s21desired.getAllComponents()) {
			if (tempD.isA("worker")) {
				((Function) tempD).setSubject(desiredThing);
			}
		}
		assertEquals(0., tm.distance(s21, s21desired));
	}
}
