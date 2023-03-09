package memory.soms;
import frames.entities.Entity;
import junit.framework.TestCase;
import memory.soms.mergers.EntityMerger;
import memory.soms.metrics.EntityMetric;
/**
 * JUnit tests for the memory's self-organizing maps.
 * Tests functionality of NewSom.java
 * 
 * @author sglidden
 *
 */
public class NewSomTest extends TestCase {
	
	private Som<Entity> som;
	private Entity t1, t2, t3, t4, t5;
	public void setUp() {
		som = new NewSom<Entity>(new EntityMetric(), new EntityMerger(), .4);
		
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
	
	/**
	 * Tests constructors
	 */
	public void testSomConstructors() {
		Som<Entity> s = new NewSom<Entity>(new EntityMetric(), new EntityMerger(), .4);
		assertTrue(s.getMemory().isEmpty());
	}
	
	/**
	 * Test add() and quickAdd() functions
	 */
	public void testAdd() {
		som.add(t1);
		assertTrue(som.getMemory().contains(t1));
		som.add(t5);
		assertTrue(som.getMemory().contains(t5));
		assertFalse(som.neighbors(t1).contains(t5));
		assertFalse(som.neighbors(t5).contains(t1));
		som.add(t2);
		assertFalse(som.neighbors(t5).contains(t5));
		assertTrue(som.containsEquivalent(t2));
		assertFalse(som.getMemory().contains(t1));	// should be merged in oblivion
		assertEquals(3, som.getMemory().size());
		
		Entity t1b = new Entity();
		t1b.addTypes("thing", "living animal human programmer male");
		// new element should be equivalent to t1b
		assertTrue(som.containsEquivalent(t1b));
		
		som.add(t3);
		som.add(t4);
		
		assertTrue(som.getMemory().contains(t3));
		assertTrue(som.getMemory().contains(t4));
		assertFalse(som.getMemory().contains(t2));
		assertTrue(som.containsEquivalent(t1b));
	}
	
	public void testContainsEquivalent() {
		som.add(t1);
		assertTrue(som.containsEquivalent(t1));
		assertFalse(som.containsEquivalent(null));
		assertFalse(som.containsEquivalent(t2));
		
		som.add(t5);
		assertTrue(som.containsEquivalent(t1));
		assertFalse(som.containsEquivalent(null));
		assertFalse(som.containsEquivalent(t2));
		assertTrue(som.containsEquivalent(t5));
		
		Entity t1copy = new Entity();
		t1copy.addTypes("thing", "living animal human programmer male sam");
		
		Entity t5copy = new Entity();
		t5copy.addTypes("thing", "dead rotting corpse");
		
		assertTrue(som.containsEquivalent(t1copy));
		assertTrue(som.containsEquivalent(t5copy));
		
	}
	
	public void testNeighbors() {
		som.add(t1);
		som.add(t2);
		som.add(t3);
		som.add(t4);
		assertTrue(som.neighbors(t1).contains(t3));
		assertFalse(som.neighbors(t3).contains(t3));
		
		assertTrue(som.neighbors(t5).isEmpty());
		som.add(t1);
		som.add(t2);
		som.add(t3);
		som.add(t4);
//		System.out.println(som);
		assertTrue(som.neighbors(t1).contains(t3));
//		System.out.println("neighbors of lucy: "+som.neighbors(t3));
		assertFalse(som.neighbors(t3).contains(t3));
		
		assertTrue(som.neighbors(t5).isEmpty());
	}
	
	public void testNearest() {
		som.add(t1);
		som.add(t3);
		NewSom<Entity> ns = (NewSom<Entity>) som;
		System.out.println(ns);
		System.out.println(t2);
		System.out.println(ns.getNearest(t1, 1));
		assertEquals(1, ns.getNearest(t1, 1).size());
		som.add(t2);
		assertTrue(ns.getNearest(t1, 1).contains(t2));
	}
	
	public void testClone() {
		som.add(t1);
		som.add(t2);
		som.add(t3);
		som.add(t4);
		Som<Entity> som2 = som.clone();
		
		assertEquals(som.getMemory(), som2.getMemory());
		assertEquals(som.neighbors(t1), som2.neighbors(t1));
		assertEquals(som.neighbors(t4), som2.neighbors(t4));
		
	}
	
	public void testGetDistance() {
		som.add(t3);
		som.add(t4);
		som.add(t5);
		EntityMetric dm = new EntityMetric();
		assertEquals(dm.distance(t3, t4), som.getDistance(t3, t4));
		assertEquals(dm.distance(t5, t4), som.getDistance(t5, t4));
		assertEquals(1., som.getDistance(t3, null));
		assertEquals(dm.distance(t1, t4), som.getDistance(t1, t4));
	}
	
	
	public void testMultithreads() {
		for (int i=0; i<222; i++) {
			Thread thread = new Thread() { 
				public void run() { 
				som.add(t2);
				som.add(t3);
				som.getDistance(t2, t3);
				som.add(t4);
			}};
			thread.start();
		}
	}
}
