package bryanWilliams.Coherence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import frames.entities.Entity;

public class CauseNode {
    
    private Set<CauseNode> children;
    private Set<CauseNode> parents;
    private final Map<CauseNode, String> childToEdgeLabel;
    private final Entity event;
    private int height;
    private boolean inCyclicalGraph;
    
    public CauseNode(Entity event) {
        this.event = event;
        this.parents = new HashSet<CauseNode>();
        this.children = new HashSet<CauseNode>();
        this.childToEdgeLabel = new HashMap<>();
        this.height = 0;
        this.inCyclicalGraph = false;
    }

    // be careful - if using this constructor, inCyclicalGraph must be true if larger graph contains a cycle
    public CauseNode(Entity event, CauseNode parent, boolean inCyclicalGraph) {
        this.event = event;
        this.parents = new HashSet<CauseNode>();
        this.parents.add(parent);
        this.children = new HashSet<CauseNode>();
        this.childToEdgeLabel = new HashMap<>();
        this.height = 0;
        setInCyclicalGraph(inCyclicalGraph);
    }
    
    // be careful - if using this constructor, inCyclicalGraph must be true if larger graph contains a cycle
    public CauseNode(Entity event, CauseNode parent, List<CauseNode> children, boolean inCyclicalGraph) {
        this.event = event;
        this.parents = new HashSet<CauseNode>();
        this.parents.add(parent);
        this.children = new HashSet<CauseNode>(children);
        this.childToEdgeLabel = new HashMap<>();
        setInCyclicalGraph(inCyclicalGraph);
        this.recalculateHeight();
    }
    
    // be careful - if using this constructor, inCyclicalGraph must be true if larger graph contains a cycle
    public CauseNode(Entity event, List<CauseNode> parents, List<CauseNode> children, boolean inCyclicalGraph) {
        this.event = event;
        this.parents = new HashSet<CauseNode>(parents);
        this.children = new HashSet<CauseNode>(children);
        this.childToEdgeLabel = new HashMap<>();
        setInCyclicalGraph(inCyclicalGraph);
        this.recalculateHeight();
    }
    
    public Entity getEvent() {
        return event;
    }
    
    public List<CauseNode> getParents() {
        return new ArrayList<CauseNode>(parents);
    }
    
    public List<CauseNode> getChildren() {
        return new ArrayList<CauseNode>(children);
    }
    
    public boolean hasParent() {
        return parents.size() > 0;
    }
    
    public boolean hasChild() {
        return children.size() > 0;
    }
    
    public boolean hasLabelForChild(CauseNode child) {
        return childToEdgeLabel.containsKey(child);
    }
    
    /**
     * @return branching factor (number of children) of this node
     */
    public int getBranchingFactor () {
        return children.size();
    }
    
    public int getHeight() {
        return height;
    }
    
    // returns null if does not have child
    public String getLabelForChild(CauseNode child) {
        return childToEdgeLabel.get(child);
    }
    
    public void setParents(Collection<CauseNode> parents) {
        this.parents = new HashSet<CauseNode>(parents);
    }
    
    public void addParent(CauseNode parent) {
        this.parents.add(parent);
    }
    
    public void addChild(CauseNode child) {
        this.children.add(child);
        if (!inCyclicalGraph) {
            // Update height if necessary
            this.recalculateHeightsToRoot();
        }
    }
    
    public void addChild(CauseNode child, String label) {
        addChild(child);
        this.childToEdgeLabel.put(child, label);
    }
    
    public void setInCyclicalGraph(boolean inCyclicalGraph) {
        this.inCyclicalGraph = inCyclicalGraph;
        if (this.inCyclicalGraph) {
            this.height = -1;
        }
    }
    
    /**
     * @return maximum branching factor (number of children) found in entire node's subtree
     */
    public int getMaxBranchingFactor() {
        if (this.children.size() == 0) return 0;
        return Math.max(this.children.size(), 
                this.children.stream().map(n -> n.getBranchingFactor()).max(Integer::compare).get());
    }
    
    private void recalculateHeight() {
        if (this.getBranchingFactor() == 0) {
            this.height = 0;
        } else {
            // Height is one greater than the maximum height of all children
            this.height = 1 + this.children.stream().map(e -> e.getHeight()).max(Integer::compare).get();
        }
    }
    
    private void recalculateHeightsToRoot() {
        int oldHeight;
        Queue<CauseNode> q = new LinkedList<CauseNode>();
        q.add(this);
        while (!q.isEmpty()) {
            CauseNode curNode = q.remove();
            oldHeight = curNode.getHeight();
            curNode.recalculateHeight();
            // If height is unchanged, no more work to do
            if (curNode.getHeight() != oldHeight) {
                q.addAll(curNode.getParents());
            }   
        }
    }
    
    @Override
    public String toString() {
        return this.event.toEnglish();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((event == null) ? 0 : event.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CauseNode other = (CauseNode) obj;
        if (event == null) {
            if (other.event != null)
                return false;
        } else if (!event.equals(other.event))
            return false;
        return true;
    }
}
