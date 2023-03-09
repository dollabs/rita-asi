package memory.distancemetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import frames.entities.Bundle;
import frames.entities.Thread;
public class HungarianBundle extends Point<Bundle> {
	public static final double MAX_DIST = 1.0;
	protected double getDistance(Bundle a, Bundle b) {
		if(a.size()==0 || b.size()==0)return MAX_DIST;
		//build arrays of Point<thread>s and compare their distance.  with caching enabled, this will
		//only happen once for each new bundle
		List<Point<Thread>> aa = new ArrayList<Point<Thread>>(a.size()); //TODO this can be more eficient
		List<Point<Thread>> bb = new ArrayList<Point<Thread>>(b.size());
		for(Thread t:a){
			aa.add(new ThreadWithSimilarityDistance(t));
		}
		for(Thread t:b){
			bb.add(new ThreadWithSimilarityDistance(t));
		}
		return getDistance(aa,bb);
	}
	private double getDistance(List<Point<Thread>> a,List<Point<Thread>> b){
		HashMap optimalPairing = Operations.hungarian(a,b);
		//System.out.println("optimal pairing:");
		//System.out.println(optimalPairing);
		double accum = 0.0;
		for(Object k : optimalPairing.keySet()){
			accum += Operations.distance((Point<Thread>)k, (Point<Thread>)(optimalPairing.get(k)));
		}
		//here's where to cache the optimal pairing to avoid computing that hungarian algorithm again
		//unnecesarily
		return accum/Math.min(a.size(), b.size());
	}
	private Bundle myBundle;
	public Bundle getWrapped() {
		return myBundle;
	}
	public HungarianBundle(Bundle b){
		myBundle = b;
	}
	
	//tests
	public static void main(String args[]){
		Bundle a = new Bundle();
		Thread at = new Thread();
		at.add("a");
		at.add("b");
		at.add("c");
		at.add("d");
		at.add("e");
		a.add(at);
		at = new Thread();
		at.add("a");
		at.add("b");
		at.add("c");
		at.add("h");
		at.add("q");
		a.add(at);
		at = new Thread();
		at.add("a");
		at.add("b");
		at.add("r");
		at.add("w");
		at.add("g");
		a.add(at);
		at = new Thread();
		at.add("h");
		at.add("q");
		at.add("x");
		a.add(at);
		at = new Thread();
		at.add("h");
		at.add("l");
		at.add("i");
		a.add(at);
		at = new Thread();
		at.add("h");
		at.add("y");
		at.add("i");
		a.add(at);
		at = new Thread();
		at.add("t");
		at.add("r");
		at.add("t");
		a.add(at);
		
		at = new Thread();
		at.add("e");
		at.add("r");
		at.add("h");
		a.add(at);
		at = new Thread();
		at.add("h");
		at.add("q");
		at.add("t");
		at.add("o");
		a.add(at);
		
		
		HungarianBundle b1 = new HungarianBundle(a);
		
		a = new Bundle();
		at = new Thread();
		at.add("a");
		at.add("b");
		at.add("c");
		at.add("d");
		at.add("e");
		a.add(at);
		at = new Thread();
		at.add("a");
		at.add("b");
		at.add("c");
		at.add("o");
		at.add("p");
		a.add(at);
		at = new Thread();
		at.add("a");
		at.add("b");
		at.add("y");
		at.add("u");
		at.add("r");
		a.add(at);
		at = new Thread();
		at.add("h");
		at.add("q");
		at.add("t");
		a.add(at);
		at = new Thread();
		at.add("h");
		at.add("l");
		at.add("p");
		a.add(at);
		at = new Thread();
		at.add("h");
		at.add("y");
		at.add("e");
		a.add(at);
		at = new Thread();
		at.add("t");
		at.add("y");
		at.add("t");
		a.add(at);
		at = new Thread();
		at.add("e");
		at.add("r");
		at.add("t");
		a.add(at);
		at = new Thread();
		at.add("h");
		at.add("q");
		at.add("t");
		at.add("m");
		a.add(at);
		HungarianBundle b2 = new HungarianBundle(a);
		
		System.out.println(b1);
		System.out.println(b2);
		
		System.out.println("hungarian distance: ");
		long time = System.currentTimeMillis();
		System.out.println(Operations.distance(b1, b2));
		time = System.currentTimeMillis() - time;
		System.out.println("time for this computation in milliseconds: "+time);
		
	}
}
