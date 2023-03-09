package memory.distancemetrics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import frames.entities.Entity;
import utils.EntityUtils;
public class EntityPointWithHeuristicDistance extends Point<Entity>{
	public static final double EPSILON = 0.000000001;
	public static final double MAX_DIST = 1.0;
	private Entity thinger;
	public EntityPointWithHeuristicDistance(Entity bleat){
		thinger = bleat;
	}
	
	protected double getDistance(Entity a, Entity b) {
		if(!EntityUtils.getRepType(a).equals(EntityUtils.getRepType(b))){
			return MAX_DIST;
		}
		double bDist = Operations.distance(a.getBundle(), b.getBundle());
		if(bDist>EPSILON){
//			if(EntityUtils.hasComponents(a)||EntityUtils.hasComponents(b)){
//				return MAX_DIST;
//			}else return bDist;
			return bDist;
		}
		else{
			if(!EntityUtils.hasComponents(a) && !EntityUtils.hasComponents(b)){
				return 0.0;
			}
			else if(EntityUtils.hasComponents(a) != EntityUtils.hasComponents(b)){
				return MAX_DIST;
			}
			else{
				List<Point<Entity>> aa = new ArrayList<Point<Entity>>(a.getDescendants().size()); //TODO this can be more eficient
				List<Point<Entity>> bb = new ArrayList<Point<Entity>>(b.getDescendants().size());
				for(Entity t:a.getDescendants()){
					aa.add(new EntityPointWithHeuristicDistance(t));
				}
				for(Entity t:b.getDescendants()){
					bb.add(new EntityPointWithHeuristicDistance(t));
				}
		
				HashMap optimalPairing = Operations.hungarian(aa,bb);
				//System.out.println("optimal pairing:");
				//System.out.println(optimalPairing);
				double accum = 0.0;
				for(Object k : optimalPairing.keySet()){
					accum += Operations.distance((Point<Entity>)k, (Point<Entity>)(optimalPairing.get(k)));
				}
				//here's where to cache the optimal pairing to avoid computing that hungarian algorithm again
				//unnecesarily
				return accum/Math.min(aa.size(), bb.size());
			}
		}
	}
	
	public Entity getWrapped() {
		return thinger;
	}
	
}
