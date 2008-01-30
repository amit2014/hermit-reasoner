package org.semanticweb.HermiT.tableau;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.io.Serializable;

import org.semanticweb.HermiT.model.*;

public final class Node implements Serializable {
    private static final long serialVersionUID=-2549229429321484690L;
    protected static List<ExistentialConcept> NO_EXISTENTIALS=Collections.emptyList();
    public static final Node CACHE_BLOCKER=new Node(null);

    public static enum NodeState { ACTIVE,MERGED,PRUNED }
    
    protected final Tableau m_tableau;
    protected int m_nodeID;
    protected NodeState m_nodeState;
    protected Node m_parent;
    protected NodeType m_nodeType;
    protected int m_treeDepth;
    protected Set<Concept> m_positiveLabel;
    protected int m_positiveLabelSize;
    protected int m_positiveLabelHashCode;
    protected int m_negativeLabelSize;
    protected Set<AtomicAbstractRole> m_fromParentLabel;
    protected int m_fromParentLabelHashCode;
    protected Set<AtomicAbstractRole> m_toParentLabel;
    protected int m_toParentLabelHashCode;
    protected List<ExistentialConcept> m_unprocessedExistentials;
    protected Node m_previousTableauNode;
    protected Node m_nextTableauNode;
    protected Node m_previousMergedOrPrunedNode;
    protected Node m_mergedInto;
    protected PermanentDependencySet m_mergedIntoDependencySet;
    protected Node m_blocker;
    protected boolean m_directlyBlocked;
    protected Object m_blockingObject;
    protected boolean m_blockingSignatureChanged;
    protected Map<DescriptionGraph,Occurrence> m_occursInDescriptionGraphs;
    protected boolean m_occursInDescriptionGraphsDirty;
    protected int m_numberOfNIAssertionsFromNode;
    protected int m_numberOfNIAssertionsToNode;
    
    public Node(Tableau tableau) {
        m_tableau=tableau;
        m_nodeID=-1;
    }
    protected void initialize(int nodeID,Node parent,NodeType nodeType,int treeDepth) {
        assert m_nodeID==-1;
        assert m_positiveLabel==null;
        assert m_fromParentLabel==null;
        assert m_toParentLabel==null;
        assert m_unprocessedExistentials==null;
        m_nodeID=nodeID;
        m_nodeState=NodeState.ACTIVE;
        m_parent=parent;
        m_nodeType=nodeType;
        m_treeDepth=treeDepth;
        m_positiveLabel=null;
        m_positiveLabelSize=0;
        m_positiveLabelHashCode=0;
        m_negativeLabelSize=0;
        m_fromParentLabel=null;
        m_fromParentLabelHashCode=0;
        m_toParentLabel=null;
        m_toParentLabelHashCode=0;
        m_unprocessedExistentials=NO_EXISTENTIALS;
        m_previousTableauNode=null;
        m_nextTableauNode=null;
        m_previousMergedOrPrunedNode=null;
        m_mergedInto=null;
        m_mergedIntoDependencySet=null;
        m_blocker=null;
        m_directlyBlocked=false;
        m_blockingObject=null;
        m_blockingSignatureChanged=false;
        m_occursInDescriptionGraphs=null;
        m_occursInDescriptionGraphsDirty=false;
        m_numberOfNIAssertionsFromNode=0;
        m_numberOfNIAssertionsToNode=0;
    }
    protected void destroy() {
        m_nodeID=-1;
        m_nodeState=null;
        m_parent=null;
        m_nodeType=null;
        if (m_positiveLabel!=null) {
            m_tableau.m_labelManager.removeConceptSetReference(m_positiveLabel);
            m_positiveLabel=null;
        }
        if (m_fromParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicAbstractRoleSetReference(m_fromParentLabel);
            m_fromParentLabel=null;
        }
        if (m_toParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicAbstractRoleSetReference(m_toParentLabel);
            m_toParentLabel=null;
        }
        if (m_unprocessedExistentials!=NO_EXISTENTIALS) {
            m_unprocessedExistentials.clear();
            m_tableau.putExistentialConceptsBuffer(m_unprocessedExistentials);
        }
        m_unprocessedExistentials=null;
        m_previousTableauNode=null;
        m_nextTableauNode=null;
        m_previousMergedOrPrunedNode=null;
        m_mergedInto=null;
        if (m_mergedIntoDependencySet!=null) {
            m_tableau.m_dependencySetFactory.removeUsage(m_mergedIntoDependencySet);
            m_mergedIntoDependencySet=null;
        }
        m_blocker=null;
        m_blockingObject=null;
        m_occursInDescriptionGraphs=null;
    }
    protected void finalize() {
        if (m_positiveLabel!=null)
            m_tableau.m_labelManager.removeConceptSetReference(m_positiveLabel);
        if (m_fromParentLabel!=null)
            m_tableau.m_labelManager.removeAtomicAbstractRoleSetReference(m_fromParentLabel);
        if (m_toParentLabel!=null)
            m_tableau.m_labelManager.removeAtomicAbstractRoleSetReference(m_toParentLabel);
        if (m_unprocessedExistentials!=NO_EXISTENTIALS && m_unprocessedExistentials!=null) {
            m_unprocessedExistentials.clear();
            m_tableau.putExistentialConceptsBuffer(m_unprocessedExistentials);
        }
        if (m_mergedIntoDependencySet!=null)
            m_tableau.m_dependencySetFactory.removeUsage(m_mergedIntoDependencySet);
    }
    public int getNodeID() {
        return m_nodeID;
    }
    public Node getParent() {
        return m_parent;
    }
    public boolean isParentOf(Node potentialChild) {
        return potentialChild.m_parent==this;
    }
    public boolean isAncestorOf(Node potendialDescendant) {
        while (potendialDescendant!=null) {
            if (potendialDescendant==this)
                return true;
            potendialDescendant=potendialDescendant.m_parent;
        }
        return false;
    }
    public NodeType getNodeType() {
        return m_nodeType;
    }
    public int getTreeDepth() {
        return m_treeDepth;
    }
    public boolean isBlocked() {
        return m_blocker!=null;
    }
    public Node getBlocker() {
        return m_blocker;
    }
    public boolean isDirectlyBlocked() {
        return m_directlyBlocked;
    }
    public boolean isIndirectlyBlocked() {
        return m_blocker!=null && !m_directlyBlocked;
    }
    public void setBlocked(Node blocker,boolean directlyBlocked) {
        m_blocker=blocker;
        m_directlyBlocked=directlyBlocked;
    }
    public Object getBlockingObject() {
        return m_blockingObject;
    }
    public void setBlockingObject(Object blockingObject) {
        m_blockingObject=blockingObject;
    }
    public boolean getBlockingSignatureChanged() {
        return m_blockingSignatureChanged;
    }
    public void setBlockingSignatureChanged(boolean blockingSignatureChanged) {
        m_blockingSignatureChanged=blockingSignatureChanged;
    }
    public boolean isActive() {
        return m_nodeState==NodeState.ACTIVE;
    }
    public boolean isMerged() {
        return m_nodeState==NodeState.MERGED;
    }
    public Node getMergedInto() {
        return m_mergedInto;
    }
    public boolean isPruned() {
        return m_nodeState==NodeState.PRUNED;
    }
    public Node getPreviousTableauNode() {
        return m_previousTableauNode;
    }
    public Node getNextTableauNode() {
        return m_nextTableauNode;
    }
    public Node getCanonicalNode() {
        Node result=this;
        while (result.m_mergedInto!=null)
            result=result.m_mergedInto;
        return result;
    }
    public PermanentDependencySet addCacnonicalNodeDependencySet(DependencySet dependencySet) {
        PermanentDependencySet result=m_tableau.m_dependencySetFactory.getPermanent(dependencySet);
        Node node=this;
        while (node.m_mergedInto!=null) {
            result=m_tableau.m_dependencySetFactory.unionWith(result,node.m_mergedIntoDependencySet);
            node=node.m_mergedInto;
        }
        return result;
    }
    public Set<Concept> getPositiveLabel() {
        if (m_positiveLabel==null) {
            m_positiveLabel=m_tableau.m_labelManager.getPositiveLabel(this);
            m_tableau.m_labelManager.addConceptSetReference(m_positiveLabel);
        }
        return m_positiveLabel;
    }
    public int getPositiveLabelSize() {
        return m_positiveLabelSize;
    }
    public int getPositiveLabelHashCode() {
        return m_positiveLabelHashCode;
    }
    protected void addToPositiveLabel(Concept concept) {
        if (m_positiveLabel!=null) {
            m_tableau.m_labelManager.removeConceptSetReference(m_positiveLabel);
            m_positiveLabel=null;
        }
        m_positiveLabelHashCode+=concept.hashCode();
        m_positiveLabelSize++;
    }
    protected void removeFromPositiveLabel(Concept concept) {
        if (m_positiveLabel!=null) {
            m_tableau.m_labelManager.removeConceptSetReference(m_positiveLabel);
            m_positiveLabel=null;
        }
        m_positiveLabelHashCode-=concept.hashCode();
        m_positiveLabelSize--;
    }
    public int getNegativeLabelSize() {
        return m_negativeLabelSize;
    }
    protected void addToNegativeLabel() {
        m_negativeLabelSize++;
    }
    protected void removeFromNegativeLabel() {
        m_negativeLabelSize--;
    }
    public Set<AtomicAbstractRole> getFromParentLabel() {
        if (m_fromParentLabel==null) {
            m_fromParentLabel=m_tableau.m_labelManager.getEdgeLabel(m_parent,this);
            m_tableau.m_labelManager.addAtomicAbstractRoleSetReference(m_fromParentLabel);
        }
        return m_fromParentLabel;
    }
    public int getFromParentLabelHashCode() {
        return m_fromParentLabelHashCode;
    }
    protected void addToFromParentLabel(AtomicAbstractRole atomicAbstractRole) {
        if (m_fromParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicAbstractRoleSetReference(m_fromParentLabel);
            m_fromParentLabel=null;
        }
        m_fromParentLabelHashCode+=atomicAbstractRole.hashCode();
    }
    protected void removeFromFromParentLabel(AtomicAbstractRole atomicAbstractRole) {
        if (m_fromParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicAbstractRoleSetReference(m_fromParentLabel);
            m_fromParentLabel=null;
        }
        m_fromParentLabelHashCode-=atomicAbstractRole.hashCode();
    }
    public Set<AtomicAbstractRole> getToParentLabel() {
        if (m_toParentLabel==null) {
            m_toParentLabel=m_tableau.m_labelManager.getEdgeLabel(this,m_parent);
            m_tableau.m_labelManager.addAtomicAbstractRoleSetReference(m_toParentLabel);
        }
        return m_toParentLabel;
    }
    public int getToParentLabelHashCode() {
        return m_toParentLabelHashCode;
    }
    protected void addToToParentLabel(AtomicAbstractRole atomicAbstractRole) {
        if (m_toParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicAbstractRoleSetReference(m_toParentLabel);
            m_toParentLabel=null;
        }
        m_toParentLabelHashCode+=atomicAbstractRole.hashCode();
    }
    protected void removeFromToParentLabel(AtomicAbstractRole atomicAbstractRole) {
        if (m_toParentLabel!=null) {
            m_tableau.m_labelManager.removeAtomicAbstractRoleSetReference(m_toParentLabel);
            m_toParentLabel=null;
        }
        m_toParentLabelHashCode-=atomicAbstractRole.hashCode();
    }
    protected void addToUnprocessedExistentials(ExistentialConcept existentialConcept) {
        if (m_unprocessedExistentials==NO_EXISTENTIALS) {
            m_unprocessedExistentials=m_tableau.getExistentialConceptsBuffer();
            assert m_unprocessedExistentials.isEmpty();
        }
        m_unprocessedExistentials.add(existentialConcept);
    }
    protected void removeFromUnprocessedExistentials(ExistentialConcept existentialConcept) {
        assert !m_unprocessedExistentials.isEmpty();
        if (existentialConcept==m_unprocessedExistentials.get(m_unprocessedExistentials.size()-1))
            m_unprocessedExistentials.remove(m_unprocessedExistentials.size()-1);
        else {
            boolean result=m_unprocessedExistentials.remove(existentialConcept);
            assert result;
        }
        if (m_unprocessedExistentials.isEmpty()) {
            m_tableau.putExistentialConceptsBuffer(m_unprocessedExistentials);
            m_unprocessedExistentials=NO_EXISTENTIALS;
        }
    }
    public boolean hasUnprocessedExistentials() {
        return !m_unprocessedExistentials.isEmpty();
    }
    public ExistentialConcept getSomeUnprocessedExistential() {
        return m_unprocessedExistentials.get(m_unprocessedExistentials.size()-1);
    }
    public Collection<ExistentialConcept> getUnprocessedExistentials() {
        return m_unprocessedExistentials;
    }
    public Occurrence addOccurenceInGraph(DescriptionGraph descriptionGraph,int position,int tupleIndex) {
        if (m_occursInDescriptionGraphs==null)
            m_occursInDescriptionGraphs=new HashMap<DescriptionGraph,Occurrence>();
        Occurrence lastOccurrence=m_occursInDescriptionGraphs.get(descriptionGraph);
        Occurrence newOccurrence=new Occurrence(position,tupleIndex,lastOccurrence);
        m_occursInDescriptionGraphs.put(descriptionGraph,newOccurrence);
        m_occursInDescriptionGraphsDirty=true;
        return newOccurrence;
    }
    public void removeOccurrenceInTuple(DescriptionGraph descriptionGraph,int tupleIndex,int position) {
        if (m_occursInDescriptionGraphs!=null) {
            Occurrence lastOccurrence=null;
            Occurrence occurrence=m_occursInDescriptionGraphs.get(descriptionGraph);
            while (occurrence!=null) {
                if (occurrence.m_tupleIndex==tupleIndex && occurrence.m_position==position) {
                    if (lastOccurrence==null)
                        m_occursInDescriptionGraphs.put(descriptionGraph,occurrence.m_next);
                    else
                        lastOccurrence.m_next=occurrence.m_next;
                    return;
                }
                occurrence=occurrence.m_next;
            }
        }
    }
    public String toString() {
        return String.valueOf(m_nodeID);
    }
    
    public static class Occurrence implements Serializable {
        private static final long serialVersionUID=-3146602839694560335L;

        public final int m_position;
        public final int m_tupleIndex;
        public Occurrence m_next;
        
        public Occurrence(int position,int tupleIndex,Occurrence next) {
            m_position=position;
            m_tupleIndex=tupleIndex;
            m_next=next;
        }
    }
}
