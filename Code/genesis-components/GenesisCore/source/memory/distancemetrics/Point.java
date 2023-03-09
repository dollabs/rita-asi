package memory.distancemetrics;
import java.util.HashMap;
/**
 * Provides a cached distance metric between objects of type Type.
 * Type can be mutable, we are smart enough to invalidate the cache
 * when it get stale.
 * 
 * 
 * @author adk
 *
 * @param <Type>
 */
public abstract class Point<Type> {
	public static boolean cacheEnabled = true;
	/**
	 * must override this to provide a way to get at the wrapped Type object
	 * @return
	 */
	public abstract Type getWrapped();
	/**
	 * must override to provide the distance between a and b
	 * @param a
	 * @param b
	 * @return
	 */
	protected abstract double getDistance(Type a, Type b);
	
	public double getDistanceTo(Point<Type> other){
		if(cacheEnabled){
			if(getDistCache().containsKey(other.getWrapped())){
				//DebugConsole.info("using cached");
				return ((Double)(getDistCache().get(other.getWrapped()))).doubleValue();
			} else {
				//DebugConsole.info("adding to cache");
				Double d = getDistance(getWrapped(),other.getWrapped());
				getDistCache().put(other.getWrapped(), d);
				return d.doubleValue();
			}
		}
		else return getDistance(getWrapped(),other.getWrapped());
	}
	private HashMap<Type,Double> distCache=null;
	protected HashMap<Type,Double> getDistCache(){
		//Type might be mutable so be sure to invalidate the cache if I have changed
		if(distCache==null || !isValid()){
			distCache = new HashMap<Type,Double>();
			validate();
		}
		return distCache;
	}
	
	private Integer lastHash = null;
	protected boolean isValid(){
		//has my hash changed?  if so none of my cached distances will be valid
		if(lastHash ==null)return false;
		else if(getHashOfWrapped()!=lastHash.intValue()) return false;
		else return true;
	}
	private int getHashOfWrapped(){
		return getWrapped().hashCode();
	}
	public int hashCode(){ //this may seem like weird behavior but it's not; the only mutable info in Point
		                   // is the cache, which should be invisible from the outside world, and the wrapped obj
		return getHashOfWrapped();
	}
	private void validate(){
		lastHash = new Integer(getHashOfWrapped());
	}
	public String toString(){
		return "<Point>"+getWrapped().toString()+"</Point>";
	}
	
	
	
	
	//tests
	private static class MutInt{
		public int foo;
		public int hashCode(){
			return foo;
		}
	}
	private static class Test extends Point<MutInt>{
		protected double getDistance(memory.distancemetrics.Point.MutInt a, memory.distancemetrics.Point.MutInt b) {
			return Math.abs(a.foo - b.foo);
		}
		private MutInt f;
		public memory.distancemetrics.Point.MutInt getWrapped() {
			if(f==null)f = new MutInt();
			return f;
		}
		
	}
	public static void main(String args[]){
		Test a = new Test();
		Test b = new Test();
		a.getWrapped().foo = 5; b.getWrapped().foo = 4;
		System.out.println(a.getDistanceTo(b));
		System.out.println(a.getDistanceTo(b));
		a.getWrapped().foo = 6;
		System.out.println(a.getDistanceTo(b));
		System.out.println(a.getDistanceTo(b));
		b.getWrapped().foo = 3;
		System.out.println(a.getDistanceTo(b));
		System.out.println(a.getDistanceTo(b));
		
		
	}
}
