package memory.icmc;
import java.util.HashSet;
import java.util.Set;
public class Space {
	
	private Set<Slice> slices = new HashSet<Slice>();
	
	
	
	
	
	
	
	
	
	
	
//	
//	Map<KnowledgeRep, Map<RepComponent, Slice>> reps = new HashMap<KnowledgeRep, Map<RepComponent, Slice>>();
//	
//	
//	
//	private void add(Thing thing) {
//		Map<RepComponent, Slice> slices = reps.get(FrameFactory.getRep(thing));
//		
//		for (RepComponent r : getRepComponents(thing)) {
//			slices.get(r.getType()).add(r.getThings());
//		}
//	}
//	
//	
//	private Set<RepComponent> getRepComponents(Thing thing) {
//		
//		return new HashSet<RepComponent>();
//	}
//	
//	private class RepComponent {
//		private Set<Thing> comps;
//		private String type;
//		private Set<Thing> getThings() {
//			return comps;
//		}
//		
//		private String getType() {
//			return type;
//		}
//	}
}
