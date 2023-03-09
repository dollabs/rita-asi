package memory.distancemetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import frames.entities.Bundle;
import frames.entities.Entity;
import frames.entities.Thread;
import utils.specialMath.Tableau;
import utils.specialMath.TransportationProblem;
public class Operations {
	public static final double EPSILON = 0.000000001;
	public static final double MAX_DIST = 1.0;
	public static double distance(Point a, Point b){
		return a.getDistanceTo(b);
	}
	public static double distance(Thread a, Thread b){
		return distance(new ThreadWithSimilarityDistance(a),new ThreadWithSimilarityDistance(b));
	}
	public static double distance(Bundle a, Bundle b){
		return distance(new HungarianBundle(a),new HungarianBundle(b));
	}
	public static double distance(Entity a, Entity b){
		return distance(new EntityPointWithHeuristicDistance(a), new EntityPointWithHeuristicDistance(b));
	}
	
	public static boolean bestMappingHasExactMatch(Bundle a, Bundle b){
		List al = new ArrayList();
		List bl = new ArrayList();
		for(Thread t: a){
			al.add(new ThreadWithSimilarityDistance(t));
		}
		for(Thread t: b){
			bl.add(new ThreadWithSimilarityDistance(t));
		}
		HashMap bestMapping = hungarian(al,bl);
		assert(bestMapping.keySet().size() == Math.min(a.size(), b.size()));
		for(Object key:bestMapping.keySet()){
			if(distance((Point)key, (Point)(bestMapping.get(key)))<=EPSILON){
				return true;
			}
		}
		return false;
	}
	
	public static HashMap<Thread,Thread> getBestMapping(Bundle a, Bundle b){
		List al = new ArrayList();
		List bl = new ArrayList();
		for(Thread t: a){
			al.add(new ThreadWithSimilarityDistance(t));
		}
		for(Thread t: b){
			bl.add(new ThreadWithSimilarityDistance(t));
		}
		HashMap bestMapping = hungarian(al,bl);
		HashMap<Thread,Thread> result = new HashMap<Thread,Thread>();
		for(Object key:bestMapping.keySet()){
			Thread t1 = (Thread)((Point)key).getWrapped();
			Thread t2 = (Thread)((Point)bestMapping.get(key)).getWrapped();
			result.put(t1, t2);
		}
		return result;
	}
	
	// the shorter list becomes the keys in the HashMap;
	// the longer list becomes the values.
	// NOTE: some longer-list items will not have matches!
	public static HashMap hungarian(List a, List b){
		List longList = (a.size()>b.size())?a:b;
		List shortList =(longList==a)?b:a;
		int maxLen = longList.size();
		int minLen = shortList.size();
		Tableau t = new Tableau(maxLen,maxLen);
		//fill the tableau from the lists, reserving "dummy tasks" which all agents perfom equally well
		//(to use the agent-task metaphor)
		for(int i=0;i<minLen;i++){
			for(int j=0;j<maxLen;j++){
				t.set(i, j, distance((Point)(shortList.get(i)), (Point)(longList.get(j))));
			}
		}
		for(int i=minLen;i<maxLen;i++){
			for(int j=0;j<maxLen;j++){
				t.set(i,j,0.0);
			}
		}
		t = TransportationProblem.doHungarian(t);
		///build a hashmap based on the result
		HashMap result = new HashMap();
		for(int i=0;i<minLen;i++){
			for(int j=0;j<maxLen;j++){
				if(t.isStarred(i, j)){
					result.put(shortList.get(i), longList.get(j));
				}
			}
		}
		return result;
	}
	
	
//	tests
	private static class MutInt{
		public int foo;
		public int hashCode(){
			return foo;
		}
	}
	private static class Test extends Point<MutInt>{
		protected double getDistance(MutInt a, MutInt b) {
			return Math.abs(a.foo - b.foo);
		}
		private MutInt f;
		public MutInt getWrapped() {
			if(f==null)f = new MutInt();
			return f;
		}
		
	}
	public static void main(String args[]){
			
		Point<MutInt> bla = new Test();
		bla.getWrapped().foo = 5;
		Test bar = new Test();
		bar.getWrapped().foo = 6;
		System.out.println(Operations.distance(bla, bar));
		frames.entities.Thread t = new frames.entities.Thread("woot");
		Point<frames.entities.Thread> pt = new ThreadWithSimilarityDistance(t);
		System.out.println("this is an error:");
		System.out.flush();
		System.err.flush();
		System.out.println(Operations.distance(bla, pt));
	}
}
