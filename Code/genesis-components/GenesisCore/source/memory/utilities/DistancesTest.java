package memory.utilities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Function;
import frames.entities.Sequence;
import frames.entities.Thread;
import junit.framework.TestCase;
import utils.EntityUtils;
public class DistancesTest extends TestCase {
	
	Thread t1 = new Thread(), t2 = new Thread();
	Bundle b1 = new Bundle(), b2 = new Bundle();
	Entity thing1 = new Entity(); 
	Entity thing2 = new Entity();
	Entity th1, th2, th3, th4, th5;
	
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
		
		th1 = new Entity();
		th1.addTypes("thing", "living animal human programmer male sam");
		th2 = new Entity();
		th2.addTypes("thing", "living animal human programmer male adam");
		th3 = new Entity();
		th3.addTypes("thing", "living animal human programmer female lucy");
		th4 = new Entity();
		th4.addTypes("thing", "living animal dog fido");
		th5 = new Entity();
		th5.addTypes("thing", "dead rotting corpse");
	}
	
	public void testDistanceThreads() {
		assertEquals(1/3., Distances.distance(t1, t2));
		assertEquals(Distances.distance(t2, t1), Distances.distance(t1, t2));
		t1.add("yellow");
		t1.add("hot");
		t1.add("natural");
		t2.add("water");
		t2.add("blue");
		t2.add("cold");
		t2.add("natural");
		assertEquals(3/6., Distances.distance(t1, t2));
		t2.add("hot");
		assertEquals(3/7., Distances.distance(t1, t2));
		assertEquals(0., Distances.distance(t1, t1));
		Thread t3 = new Thread();
		assertEquals(1., Distances.distance(t1, t3));
		assertEquals(0., Distances.distance(t3, t3));
	}
	
	public void testDistanceBundles() {
		assertEquals(1/2., Distances.distance(b1, b2));
		assertEquals(Distances.distance(b2, b1), Distances.distance(b1, b2));
		assertEquals(0., Distances.distance(b2, b2));
	}
	
	public void testDistanceThings() {
		// test flat things
		assertEquals( Distances.distance(b1, b2), Distances.distance(thing1, thing2));
		assertEquals(1/2., Distances.distance(thing1, thing2));
		
		// test NeedlemanWuncsh
		List<Entity> l1 = new ArrayList<Entity>();
		l1.add(th1);
		l1.add(th2);
		l1.add(th3);
		
		List<Entity> l2 = new ArrayList<Entity>();
		l2.add(th2);
		l2.add(th3);
		l2.add(th1);
		Map<Entity, Entity> pairing = NeedlemanWunsch.pair(l1, l2);
		assertTrue(pairing.get(th3).equals(th3));
		assertTrue(pairing.get(th2).equals(th2));
		assertFalse(pairing.containsKey(th1));
		
		// test complex multi-level things
		Sequence s1 = new Sequence();
		Function d1 = new Function(th1);
		Thread thread = new Thread();
		thread.add("thing");
		thread.add("worker");
		thread.add("lab");
		thread.add("csail");
		d1.addThread(new Thread(thread));
		
		s1.addElement(th1);
		s1.addElement(th2);
		s1.addElement(th3);
		
		assertEquals(.5, Distances.distance(s1.getBundle(), d1.getBundle()));
		
//		System.out.println("Sequence: "+s1);
//		System.out.println("Sequence children: "+EntityUtils.getOrderedChildren(s1));
//		System.out.println("Deriv: "+d1);
//		System.out.println("Deriv children: "+EntityUtils.getOrderedChildren(d1));
		assertEquals(5/9., Distances.distance(s1, d1));
//		System.out.println("th1: "+th1);
		assertEquals((6/14. + 1/2.) * 2/3. + 1/3., Distances.distance(th1, d1));
		
		s1.addElement(d1);
		
		Sequence s2 = new Sequence();
		s2.addElement(th2);
		s2.addElement(th3);
		s2.addElement(th1);
//		System.out.println("Sequence1 children: "+EntityUtils.getOrderedChildren(s1));
//		System.out.println("Sequence2 children: "+EntityUtils.getOrderedChildren(s2));
		assertTrue(Math.abs(((1 + 0 + 0 + Distances.distance(th1, d1))/4.)/3. - Distances.distance(s1, s2)) < .001);
	} 
	
	public void testDistances2() {
		// this test check to make sure a Thing is not zero distance from a perfect subset of itself
		assertEquals(0., Distances.distance(th1, th1));
		Function d1 = new Function(th1);
//		System.out.println(d1);
//		System.out.println(th1);
		assertEquals((2/3.)*(6/7.)+(1/3.), Distances.distance(d1, th1));
		
		Function d2 = new Function(new Entity());
		d2.setBundle((Bundle) th1.getBundle().clone());
		System.out.println(th1);
		System.out.println(d2);
		assertEquals(1/3., Distances.distance(d2, th1));
		assertEquals(1/3., Distances.distance(th1, d2));
		
		Function d3 = new Function(d2);
		Function d4 = new Function(th1);
		assertEquals(1/3.*1/3., Distances.distance(d3, d4));
		
	}
	
	
	
}
