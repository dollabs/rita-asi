/* Filename: Feature.java
 * Author: M. A. Finlayson
 * Format: Java 2 v.1.4.2_06
 * Date created: Jan 15, 2005
 */
package frames.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import utils.CollectionUtils;
import utils.StringUtils;
import utils.collections.ExclusiveCollection;
import utils.collections.ExclusiveSet;

/** Implements a feature in the Goldilock's principle sense.
 *
 * Members of a feature are objects that descend from the Thing class which are considered to be a part of the feature.
 * 
 * A feature may have a number of roots, which indicate top-level nodes in the graph structure of that feature.  Roots
 * are stored in the same array as elements in a Sequence, to facilitate graph walks using normal Thing-based code.
 * 
 * Features can be <i>complete</i>, that is, for every root of the feature, all the descendants
 * of that root are members of the feature.  This does not exclude the possibility that there are other 
 * members which are not reachable from one of the roots.
 * 
 * Features can be <i>closed</i>, that is, all the members of the feature are descendants of some root.  This does
 * not imply that all descendants of the roots are members.
 * 
 * Features that are both closed and complete are called <i>compact</i>
 * 
 * A root completion, root closure, or root compaction are making a set complete, closed, or compact keeping the set of roots for that
 * feature constant.
 * 
 * Similarly you can have a member closure which keeps the membership of the feature constant, but changes the root set.
 * 
 * A feature is called <i>consistent</i> if it does not contain roots which can be reached by graph walking down from other roots.
 *  
 * @author M.A. Finlayson
 * @since Jan 15, 2005; JDK 1.4.2_06
 */
public class Feature extends Sequence implements Set<Entity>{
    
    private double score = -1;
    
    private static final int FALSE = 0;
    private static final int TRUE = 1;
    private static final int UNKNOWN = 2;
    
    public static final String FEATURE_MARKER = "graphfeature";
    
    /** Cache for value of whether or not all the root descendants are members */
    private int complete = TRUE;
    
    /** Cache for value of whether or not all the members are root descendants */
    private int closed = TRUE;
    
    /** Cache for value of whether or not there are redundant roots*/
    private int consistent = TRUE;
    
    private ExclusiveSet<Entity> members = new ExclusiveSet<Entity>(new HashSet<Entity>(), Entity.class, ExclusiveCollection.FAMILY);
    
    public Feature(){
        super();
        addType(FEATURE_MARKER);
    }
    
    /** Makes a compact, consistent feature from the specified root. */
    public Feature(Entity root){
        this();
        addRoot(root);
        effectRootCompaction();
    }
    
    public Feature(Entity root, double score){
        this(root);
        setScore(score);
    }
    
    public boolean featureP(){
        return true;
    }
    
    public void setScore(double score){this.score = score;}
    public double getScore(){return score;}
    
    /** Makes a compact, consistent feature from the specified collection of roots */
    public Feature(Collection roots){
        this();
        for(Iterator i = roots.iterator(); i.hasNext(); ){
            addRoot((Entity)i.next());
        }
        effectRootCompaction();
    }
    
    /** Returns a copy of the members set */
    public Set<Entity> getMembers(){
        HashSet<Entity> result = new HashSet<Entity>();
        result.addAll(members);
        return result;
    }
    
    /** Returns a copy of the list of roots.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public List<Entity> getFeatureRoots(){
        ArrayList<Entity> result = new ArrayList<Entity>();
        result.addAll(getElements());
        return result;
    }
    
    /** Adds the specified thing to the root list.
     * Returns <code>true</code> only if the root was not originally a part of the list.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public boolean addRoot(Entity root){
        if(getElements().contains(root)){
            return false;
        } else {
            super.addElement(root);
            add(root);
            complete = UNKNOWN;
            closed = UNKNOWN;
            consistent = UNKNOWN;
            return true;
        }
    }
    
    public boolean addAllRoots(Collection roots){
        boolean result = false;
        for(Iterator i = roots.iterator(); i.hasNext();){
            result = result | addRoot((Entity)i.next());
        }
        return result;
    }
    
    public void addElement(Entity element){
        addRoot(element);
    }
    
    
    
    /** Removes a root from the root list.
     * Returns <code>true</code> if the feature contained the root.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public boolean removeRoot(Entity root){
        if(getElements().contains(root)){
            complete = UNKNOWN;
            closed = UNKNOWN;
            consistent = UNKNOWN;
            super.removeElement(root);
            return members.remove(root);
        } else {
            return false;
        }
    }
    
    /** Returns <code>true</code> if the root set changed as a result of the call.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public boolean removeAllRoots(Collection<Entity> r){
        boolean result = false;
        for(Iterator<Entity> i = r.iterator(); i.hasNext(); ){
            result = result | removeRoot(i.next());
        }
        return result;
    }
    
    public boolean retainAllRoots(Collection<Entity> r){
        Collection<Entity> reject = new ArrayList<Entity>();
        Entity next;
        for(Iterator<Entity> i = getElements().iterator(); i.hasNext(); ){
            next = i.next();
            if(!r.contains(next)){reject.add(next);}
        }
        return removeAllRoots(reject);
    }
    
    public boolean removeElement(Entity element){
        return removeRoot(element);
    }
    
    
    /** Adds all descendants of all the roots to the feature leaving set of roots constant.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public void effectRootCompletion(){
        // get copy of root list
        List roots = getFeatureRoots();
        // add to members list all descendants of each root
        for(int i = 0; i < roots.size(); i++){members.addAll(((Entity)roots.get(i)).getDescendants());}
        // now we are sure the feature is complete
        complete = TRUE;
    }

    /** Removes all non-roots and non-descendants of roots from the feature leaving set of roots constant.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public void effectRootClosure(){
        // get copy of root list
        List<Entity> roots = getFeatureRoots();
        // get copy of members list
        Set<Entity> mems = getMembers();
        // remove from the members list all who are descendants of roots
        for(int i = 0; i < roots.size(); i++){
        	mems.removeAll(roots.get(i).getDescendants());
        }
        // also remove from that list all roots
        mems.removeAll(roots);
        // whatever is left is neither a root nor a descendant of a root, so we can remove it.
        members.removeAll(mems);
        // now we are sure the feature is closed
        closed = TRUE;
    }
    
 
    /** Makes the feature both complete and closed, and then trims unncessary roots.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public void effectRootCompaction(){
        effectRootCompletion();
        effectRootClosure();
        trimUnnecessaryRoots();
    }
    
    /** Removes all roots that can be reached by walked down from other roots, respecting membership of the feature.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public void trimUnnecessaryRoots(){
        List<Entity> queue = new ArrayList<Entity>();
        Set<Entity> visited = new HashSet<Entity>();
        
        queue.addAll(getElements());
        
        Entity root;
        Set<Entity> children;
        while(!queue.isEmpty()){
            root = queue.remove(0);
            if(!visited.add(root)){continue;}
            children = root.getChildren();
            children.retainAll(members);
            queue.addAll(children);
            removeAllRoots(children);
        }
        
        consistent = TRUE;
    }
    
    /** Returns <code>true</code> if and only if all descendants of all the roots are members of the feature.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public boolean isComplete(){
        if(complete == TRUE){return true;} 
        else if (complete == FALSE){return false;}
        
        Collection<Entity> mems = getMembers();
        for(int i = 0; i < getElements().size(); i++){
            if(!mems.containsAll(getElements().get(i).getDescendants())){
                complete = FALSE;
                return false;
            }
        }
        complete = TRUE;
        return true;
    }
    
    /** Returns <code>true</code> if and only if all members are descendants of roots.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public boolean isClosed(){
        if(closed == TRUE){return true;} 
        else if (closed == FALSE){return false;}
        
        List<Entity> list = getFeatureRoots();
        Set<Entity> mems = getMembers();
        for(int i = 0; i < list.size(); i++){
            mems.removeAll(list.get(i).getDescendants());
        }
        if(mems.isEmpty()){
            closed = TRUE;
            return true;
        } else {
            closed = FALSE;
            return false;
        }
    }
    
    public boolean isCompact(){
        return isComplete() & isClosed();
    }
    
    public boolean isConsistent(){
        if(consistent == TRUE){return true;} 
        else if (consistent == FALSE){return false;}
        
        List<Entity> queue = new ArrayList<Entity>();
        Set<Entity> visited = new HashSet<Entity>();
        Set<Entity> roots = new HashSet<Entity>();
        roots.addAll(getElements());
        
        queue.addAll(getElements());
        
        Entity root;
        Set<Entity> descendants;
        while(!queue.isEmpty()){
            root = queue.remove(0);
            if(!visited.add(root)){continue;}
            descendants = root.getDescendants();
            visited.addAll(descendants);
            queue.removeAll(descendants);
            if(roots.removeAll(descendants)){return false;};
        }
        return true;
        
    }
    
    /** Returns true if the feature overlaps in membership with the given feature.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public boolean isConnectedTo(Feature f){
        return CollectionUtils.isIntersectionNonempty(this, f);
    }
    
    /** Returns <code>true</code> if the feature contains nodes of the indicated order or above
     * that are contained in the given feature.
     * @author M.A. Finlayson
     * @since Jan 15, 2005; JDK 1.4.2
     */
    public boolean isConnectedTo(int order, Feature f){
        if(order == 0){
            return isConnectedTo(f);
        } else {
            Set<Entity> A = getMembers();
            Set B = f.getMembers();
            Entity next;
            for(Iterator<Entity> i = A.iterator(); i.hasNext(); ){
                next = i.next();
                if(next.order() < order){i.remove();}
            }
            for(Iterator i = B.iterator(); i.hasNext(); ){
                next = (Entity)i.next();
                if(next.order() < order){i.remove();}
            }
            return CollectionUtils.isIntersectionNonempty(A, B);
        }
    }
    
    public static String toDebugPrintStringFromCollection(Collection c){
        StringBuffer result = new StringBuffer();
        Feature feature;
        for(Iterator i = c.iterator(); i.hasNext(); ){
            feature = (Feature)i.next();
            
            result.append(feature.toStringDebug());
            result.append("\n\n");
        }
        return result.toString();
    }
    
  
    public String toStringDebug(){
        StringBuffer result = new StringBuffer();
        result.append(getName());
        result.append(": (");
        result.append("Complete? ");
        switch(complete){
	    	case FALSE: result.append("F; "); break;
	    	case TRUE: result.append("T; "); break;
	    	case UNKNOWN: result.append("U; "); break;
        }
        result.append("Closed? ");
        switch(closed){
	    	case FALSE: result.append("F; "); break;
	    	case TRUE: result.append("T; "); break;
	    	case UNKNOWN: result.append("U; "); break;
	    }
        result.append("Consistent? ");
        switch(consistent){
	    	case FALSE: result.append("F"); break;
	    	case TRUE: result.append("T"); break;
	    	case UNKNOWN: result.append("U"); break;
	    }
        result.append(")[");
        result.append(getScore());
        result.append("]\n");
        
        result.append("\tRoots: ");
        String[] names = new String[getElements().size()];
        for(int i = 0; i < getElements().size(); i++){
            names[i] = ((Entity)getElements().get(i)).getName();
        }
        result.append(StringUtils.join(names, ", "));
        
        result.append("\n\tMembers: ");
        names = new String[members.size()];
        int j = 0;
        for(Iterator<Entity> i = members.iterator(); i.hasNext(); ){
            names[j] = i.next().getName();
            j++;
        }
        result.append(StringUtils.join(names, ", "));
        
        return result.toString();
    }

// Need to wait to implement these
//    public boolean equals(Object o){
//        return false;
//    }
//    
//    public int hashCode(){
//        return -1;
//    }
    

    
    //***************************
    // Set methods 
    //***************************

    public boolean add(Entity m){
        boolean result = members.add(m);
        if(result){
            complete = UNKNOWN;
            closed = UNKNOWN;
            consistent = UNKNOWN;
            return true;
        } else {
            return false;
        }
   }
    
    public boolean addAll(Collection<? extends Entity> m){
        boolean result = members.addAll(m);
        if(result){
            complete = UNKNOWN;
            closed = UNKNOWN;
            consistent = UNKNOWN;
            return true;
        } else {
            return false;
        }
    }
    
    public void clear(){
        closed = TRUE;
        complete = TRUE;
        consistent = TRUE;
        for(int i = 0; i < getElements().size(); i++){
            removeElement((Entity)getElements().get(i));
        }
        members.clear();
    }
    
    public boolean contains(Object m){return members.contains(m);}
  
    public boolean containsAll(Collection m){return members.containsAll(m);}
    
    /** Returns true if this Feature has as a member at least one of the indicated
     * collection.
     * @author M.A. Finlayson
     * @since Mar 9, 2005; JDK 1.4.2
     */
    public boolean containsSome(Collection m){
        Object member;
        for(Iterator i = m.iterator(); i.hasNext(); ){
            member = i.next();
            if(contains(member)) return true;
        }
        return false;
    }
    
    public boolean isEmpty(){return members.isEmpty();}
    
    public Iterator<Entity> iterator(){return members.iterator();}
    
    public boolean remove(Object m){
        removeRoot((Entity)m);
        boolean result = members.remove(m);
        if(result){
            complete = UNKNOWN;
            closed = UNKNOWN;
            consistent = UNKNOWN;
            return true;
        } else {
            return false;
        }
    }
    
    // TODO: How do you fix this properly?
    @SuppressWarnings("unchecked")
	public boolean removeAll(Collection<?> m){    	
        removeAllRoots((Collection<Entity>) m);
        boolean result = members.removeAll(m);
        if(result){
            complete = UNKNOWN;
            closed = UNKNOWN;
            consistent = UNKNOWN;
            return true;
        } else {
            return false;
        }
    }
    
    // TODO: How do you fix this properly?
    @SuppressWarnings("unchecked")
    public boolean retainAll(Collection<?> m){
        retainAllRoots((Collection<Entity>) m);
        boolean result = members.retainAll(m);
        if(result){
            complete = UNKNOWN;
            closed = UNKNOWN;
            consistent = UNKNOWN;
            return true;
        } else {
            return false;
        }
    }
    
    public int size(){return members.size();}
    
    public Object[] toArray(){return members.toArray();}
    
    public <T> T[] toArray(T[] array){return members.toArray(array);}

    //***************************
    // Static methods 
    //***************************
    
    /** Merges the two given features, making a new feature
     * which contains the union of the members and roots of the two.
     * @author M.A. Finlayson
     * @since Jan 17, 2005; JDK 1.4.2
     */
    public static Feature merge(Feature A, Feature B){
        Feature result = new Feature();
        
        //System.out.println("Merging " + A.getName() + " and " + B.getName() + " to make " + result);
        
        result.addAll(A.members);
        result.addAll(B.members);
        
        result.addAllRoots(A.getElements());
        result.addAllRoots(B.getElements());
        
        if(A.closed == TRUE & B.closed == TRUE){
            result.closed = TRUE;
        } else {
            result.closed = UNKNOWN;
        }
        
        if(A.complete == TRUE & B.complete == TRUE){
            result.complete = TRUE;
        } else {
            result.complete = UNKNOWN;
        }
        
        result.consistent = UNKNOWN;
        
        return result;
    }
    
    /** Merges a collection of features and returns the result of the merge.
     * @author M.A. Finlayson
     * @since Jan 17, 2005; JDK 1.4.2
     */
    public static Feature merge(Collection<Feature> features){
        Feature result = new Feature();
        
        //********** Debugging
        //String[] names = new String[features.size()];
        //int j = 0;
        //for(Iterator i = features.iterator(); i.hasNext(); ){
        //    names[j] = ((Feature)i.next()).getName();
        //    j++;
        //}
        //System.out.println("Merging " + StringUtils.join(names, ", ") + " to make " + result.getName());
        //**********
        
        Feature f;
        for(Iterator<Feature> i = features.iterator(); i.hasNext(); ){
            f = i.next();
            result.addAll(f.members);
            result.addAllRoots(f.getElements());
            
            if(result.closed == TRUE & f.closed == TRUE){
                result.closed = TRUE;
            } else {
                result.closed = UNKNOWN;
            }
            
            if(result.complete == TRUE & f.complete == TRUE){
                result.complete = TRUE;
            } else {
                result.complete = UNKNOWN;
            }
            
        }
      
        result.consistent = UNKNOWN;
        
        return result;
    }
    
    public static Feature mergeConsistently(Feature A, Feature B){
        Collection<Feature> features = new HashSet<Feature>();
        features.add(A);
        features.add(B);
        return mergeConsistently(features);
    }

    public static Feature mergeConsistently(Collection<Feature> features){
        Feature result = merge(features);
        result.trimUnnecessaryRoots();
        return result;
    }

    public static Set<Feature> consolidateConnectedFeatures(Collection<Feature> c){

        ArrayList<Feature> features = new ArrayList<Feature>();
        features.addAll(c);
        
        Feature A, B;
        HashSet<Feature> connected = new HashSet<Feature>();
        int after = features.size();
        int before = after;
        int noluck = 0;
        
        while(before >= after & noluck < after & !features.isEmpty()){
            before = after;
            A = features.remove(0);
            connected.clear();
            for(int i = 0; i < features.size(); i++){
                B = features.get(i);
                System.out.println("Testing " + A.getName() + " against " + B.getName());
                if(A.isConnectedTo(B)){
                    System.out.println(A.getName() +" is connected to " + B.getName());
                    connected.add(B);
                }
            }
            
            if(connected.isEmpty()){
                noluck++;
                features.add(A);
            } else {
                noluck = 0;
                features.removeAll(connected);
                connected.add(A);
                features.add(mergeConsistently(connected));
            }
            System.out.println("# of Features: " + features.size() + ", noluck: " + noluck);
            after = features.size();
        }

        Set<Feature> result = new HashSet<Feature>();
        result.addAll(features);
        return result;
    }
    
    public static Set<Feature> consolidateConnectedFeatures(int order, Collection<Feature> c){
        if(order == 0){
            return consolidateConnectedFeatures(c);
        } 
        
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.addAll(c);
        
        Feature A, B;
        HashSet<Feature> connected = new HashSet<Feature>();
        int after = features.size();
        int before = after;
        int noluck = 0;
        
        while(before >= after & noluck < after & !features.isEmpty()){
            before = after;
            A = features.remove(0);
            connected.clear();
            for(int i = 0; i < features.size(); i++){
                B = features.get(i);
                System.out.println("Testing " + A.getName() + " against " + B.getName());
                if(A.isConnectedTo(order, B)){
                    System.out.println(A.getName() +" is connected to " + B.getName());
                    connected.add(B);
                }
            }
            
            if(connected.isEmpty()){
                noluck++;
                features.add(A);
            } else {
                noluck = 0;
                features.removeAll(connected);
                connected.add(A);
                features.add(merge(connected));
            }
            System.out.println("# of Features: " + features.size() + ", noluck: " + noluck);
            after = features.size();
        }

        Set<Feature> result = new HashSet<Feature>();
        result.addAll(features);
        return result;
    }
    
    public static Set<Feature> consolidateConnectedFeaturesConsistently(Collection<Feature> c){
       
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.addAll(c);
        
        Feature A, B;
        HashSet<Feature> connected = new HashSet<Feature>();
        int after = features.size();
        int before = after;
        int noluck = 0;
        
        while(before >= after & noluck < after & !features.isEmpty()){
            before = after;
            A = features.remove(0);
            connected.clear();
            for(int i = 0; i < features.size(); i++){
                B = features.get(i);
                System.out.println("Testing " + A.getName() + " against " + B.getName());
                if(A.isConnectedTo(B)){
                    System.out.println(A.getName() +" is connected to " + B.getName());
                    connected.add(B);
                }
            }
            
            if(connected.isEmpty()){
                noluck++;
                features.add(A);
            } else {
                noluck = 0;
                features.removeAll(connected);
                connected.add(A);
                features.add(mergeConsistently(connected));
            }
            System.out.println("# of Features: " + features.size() + ", noluck: " + noluck);
            after = features.size();
        }

        Set<Feature> result = new HashSet<Feature>();
        result.addAll(features);
        return result;
    }
  
    public static Set<Feature> consolidateConnectedFeaturesConsistently(int order, Collection<Feature> c){
        if(order == 0){
            return consolidateConnectedFeaturesConsistently(c);
        } 
        
        ArrayList<Feature> features = new ArrayList<Feature>();
        features.addAll(c);
        
        Feature A, B;
        HashSet<Feature> connected = new HashSet<Feature>();
        int after = features.size();
        int before = after;
        int noluck = 0;
        
        // loop over all features until no more features can be made
        while(before >= after & noluck < after & !features.isEmpty()){
            before = after;
            A = features.remove(0);
            connected.clear();
            for(int i = 0; i < features.size(); i++){
                B = features.get(i);
                //System.out.println("Testing " + A.getName() + " against " + B.getName());
                if(A.isConnectedTo(order, B)){
                    //System.out.println(A.getName() +" is connected to " + B.getName());
                    connected.add(B);
                }
            }
            
            if(connected.isEmpty()){
                noluck++;
                features.add(A);
            } else {
                noluck = 0;
                features.removeAll(connected);
                connected.add(A);
                features.add(mergeConsistently(connected));
            }
            //System.out.println("# of Features: " + features.size() + ", noluck: " + noluck);
            after = features.size();
        }

        Set<Feature> result = new HashSet<Feature>();
        result.addAll(features);
        return result;
    }

    
}
