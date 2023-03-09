package memory.operators;
import frames.entities.Entity;
import frames.entities.Thread;
import junit.framework.TestCase;
public class OperatorsTest extends TestCase {
	public void setUp() {}
	
	public void testThreadCompare() {
		Thread a = new Thread();
		Thread b = new Thread();
		a.add("thing");
		b.add("thing");
		a.add("place");
		b.add("place");
		a.add("property");
		a.add("garage");
		b.add("property");
		b.add("home");
		b.add("house");
		assertEquals((double) 3/9, Operators.compare(a, b));
		
		Thread c = new Thread();
		c.add("thing");
		assertEquals((double) 3/5, Operators.compare(a, c));
		
		c.add("garage");
		assertEquals((double) 2/6, Operators.compare(a, c));
		
		c.add("door");
		assertEquals((double) 3/7, Operators.compare(a, c));
		
		c.add("property");
		assertEquals((double) 2/8, Operators.compare(a, c));
		
		// test null inputs
		assertEquals((double) 1, Operators.compare(a, null));
		assertEquals((double) 0, Operators.compare((Thread) null, null));
		
		// test symmetry
		assertEquals(Operators.compare(a, c), Operators.compare(c, a));
	}
	
	
	public void testThingCompare() {
		Entity t1 = new Entity();
		Entity t2 = new Entity();
		t1.addTypes("thing", "person male student sam");
		t2.addTypes("thing", "person male student adam");
		
		assertEquals((double) .2, Operators.compare(t1, t2));
		
		t1.addTypes("occupation", "researcher AI");
		assertEquals((double) .6, Operators.compare(t1, t2));
		
		// test null inputs
		assertEquals((double) 1, Operators.compare(t1, null));
		assertEquals((double) 0, Operators.compare((Entity) null, null));
		
		// test symmetry
		assertEquals(Operators.compare(t1, t2), Operators.compare(t2, t1));
	}
}
