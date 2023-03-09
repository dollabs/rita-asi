package memory.utilities;
import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Thread;
import junit.framework.TestCase;
public class SubSetUtilsTest extends TestCase {
	
	Thread t1 = new Thread(), t2 = new Thread();
	Bundle b1 = new Bundle(), b2 = new Bundle();
	Entity thing1 = new Entity(); 
	Entity thing2 = new Entity();
	
	public void setUp() {
		t1.add("thing");
		t1.add("element");
		t1.add("fire");
		t2.add("thing");
		t2.add("element");
		b1.add(t1);
		b1.add(t2);
		b2.add(t2);
		thing1.setBundle(b1);
		thing2.setBundle(b2);
	}
	
	public void testIsSubSetThread() {
		assertTrue(SubSetUtils.isSubSet(t2, t1));
		assertFalse(SubSetUtils.isSubSet(t1, t2));
		t2.add("water");
		assertFalse(SubSetUtils.isSubSet(t2, t1));
		t1.add("water");
		assertTrue(SubSetUtils.isSubSet(t2, t1));
		t2.add("fire");
		assertFalse(SubSetUtils.isSubSet(t2, t1));
		t1 = null;
		assertFalse(SubSetUtils.isSubSet(t2, t1));
		t2 = null;
		assertTrue(SubSetUtils.isSubSet(t2, t1));
	}
	
	public void testIsSubSetBundle() {
		assertTrue(SubSetUtils.isSubSet(b2, b1));
		assertFalse(SubSetUtils.isSubSet(b1, b2));
		b2.add(t1);
		assertTrue(SubSetUtils.isSubSet(b1, b2));
		t2.add("water");
		b2.remove(t2);
		assertTrue(SubSetUtils.isSubSet(b2, b1));
		assertFalse(SubSetUtils.isSubSet(b1, b2));
	}
	
	public void testIsSubSetThing() {
		assertTrue(SubSetUtils.isSubSet(thing2, thing1));
		assertFalse(SubSetUtils.isSubSet(thing1, thing2));
		
		Entity thing3 = new Entity();
		Thread t3 = new Thread();
		t3.add("thing");
		t3.add("element");
		t3.add("water");
		Bundle b3 = new Bundle();
		b3.add(t3);
		thing3.setBundle(b3);
		Function d1 = new Function(thing3);
		d1.setBundle((Bundle) b1.clone());
		
		assertTrue(SubSetUtils.isSubSet(thing2, (Entity) d1));
		assertFalse(SubSetUtils.isSubSet((Entity) d1, thing2));
		
		Function d2 = new Function(thing2);
		d2.setBundle((Bundle) b1.clone());
		System.out.println(d1);
		System.out.println(d2);
		
		assertTrue(SubSetUtils.isSubSet((Entity) d2, (Entity) d1));
		assertFalse(SubSetUtils.isSubSet((Entity) d1, (Entity) d2));
	}
}
