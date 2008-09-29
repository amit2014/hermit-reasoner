package org.semanticweb.HermiT.tableau;

import org.semanticweb.HermiT.model.DataRange;
import org.semanticweb.HermiT.model.DataRange.Facets;
import org.semanticweb.HermiT.monitor.TableauMonitor;

public class DatatypeManager {
    protected final TableauMonitor m_tableauMonitor;
    protected final ExtensionManager m_extensionManager;
    protected final ExtensionTable.Retrieval m_binaryExtensionTableDeltaOldRetrieval;
    protected final ExtensionTable.Retrieval m_binaryExtensionTable1BoundRetrieval;
    protected final UnionDependencySet m_binaryUnionDependencySet;

    public DatatypeManager(Tableau tableau) {
        m_tableauMonitor=tableau.m_tableauMonitor;
        m_extensionManager=tableau.m_extensionManager;
        // retrieval object for all the datatype assertions in the changed part of the tuple table
        m_binaryExtensionTableDeltaOldRetrieval=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[2],ExtensionTable.View.DELTA_OLD);
        // retrieval object for all datatype assertions that use the same variable as one from the above retrieval
        m_binaryExtensionTable1BoundRetrieval=m_extensionManager.getBinaryExtensionTable().createRetrieval(new boolean[] { false,true },ExtensionTable.View.EXTENSION_THIS);
        m_binaryUnionDependencySet=new UnionDependencySet(2);
    }
    public void checkDatatypeConstraints() {
        if (m_tableauMonitor != null) {
            m_tableauMonitor.datatypeCheckingStarted();
        }
        boolean result = true;
        Object[] tupleBuffer=m_binaryExtensionTableDeltaOldRetrieval.getTupleBuffer();
        for (m_binaryExtensionTableDeltaOldRetrieval.open(); 
                !m_binaryExtensionTableDeltaOldRetrieval.afterLast(); 
                m_binaryExtensionTableDeltaOldRetrieval.next()) {
            if (tupleBuffer[0] instanceof DataRange) {
                boolean allSatisfiable = checkNewDatatypeAssertion((DataRange)tupleBuffer[0],(Node)tupleBuffer[1],m_binaryExtensionTableDeltaOldRetrieval.getDependencySet());
                if (!allSatisfiable) result = false;
            }
        }
        if (m_tableauMonitor != null) {
            m_tableauMonitor.datatypeCheckingFinished(result);
        }
    }
    protected boolean checkNewDatatypeAssertion(DataRange dataRange, 
            Node node, DependencySet dependencySet) {
        boolean result = true;
        Object[] tupleBuffer=m_binaryExtensionTable1BoundRetrieval.getTupleBuffer();
        m_binaryExtensionTable1BoundRetrieval.getBindingsBuffer()[1]=node;
        for (m_binaryExtensionTable1BoundRetrieval.open(); 
                !m_binaryExtensionTable1BoundRetrieval.afterLast(); 
                m_binaryExtensionTable1BoundRetrieval.next()) {
            if (!isCompatible(dataRange, (DataRange) tupleBuffer[0])) {
                if (m_tableauMonitor != null) {
                    result = false;
                    m_tableauMonitor.clashDetected(new Object[][] {
                            new Object[] { dataRange, node },
                            new Object[] { tupleBuffer[0], node } });
                    m_tableauMonitor.datatypeCheckingFinished(false);
                }
                m_binaryUnionDependencySet.m_dependencySets[0] = dependencySet;
                m_binaryUnionDependencySet.m_dependencySets[1] = m_binaryExtensionTable1BoundRetrieval.getDependencySet();
                m_extensionManager.setClash(m_binaryUnionDependencySet);
            }
        }
        return result;
    }
    protected boolean isCompatible(DataRange dataRange1, DataRange dataRange2) {
        //System.out.println(dataRange1 + " compatible with \n" + dataRange2);
        if (!dataRange1.isNegated()) {
            if (!dataRange2.isNegated()) {
                // neither range is negated
                if (dataRange1.getDatatypeURI().equals(dataRange2.getDatatypeURI())) {
                    // both are of the same type, e.g., integer
                    if (!dataRange1.getEqualsValues().isEmpty() 
                            && !dataRange2.getEqualsValues().isEmpty()) {
                        // both have some value restrictions, e.g., equals 18
                        for (String value : dataRange1.getEqualsValues()) {
                            // check if one of the equals values has a matching 
                            // one in the other restriction
                            if (dataRange2.getEqualsValues().contains(value)) {
                                return true;
                            }
                        }
                    } else if (dataRange1.supports(Facets.MIN_INCLUSIVE)) {
                        
                    } else {
                        // at least one of the restrictions has no further 
                        // equals restriction, so we are fine
                        return true;
                    }
                } 
            } else {
                // the second range is negated
                // if the negated one if top, we are screwed
                if (!dataRange2.isTop()) {
                    if (dataRange1.getDatatypeURI().equals(dataRange2.getDatatypeURI())) {
                        // the negated and the non-negated range are of the same 
                        // type, e.g., integer
                        if (!dataRange1.getEqualsValues().isEmpty() 
                                && !dataRange2.getEqualsValues().isEmpty()) {
                            // both have some restrictions, e.g., equals 18
                            for (String value : dataRange1.getEqualsValues()) {
                                // check if one of the equals values has no matching 
                                // one in the other restriction
                                if (!dataRange2.getEqualsValues().contains(value)) {
                                    return true;
                                }
                            }
                        } else {
                            // at least one of the restrictions has no further 
                            // equals restriction, so we are fine
                            return true;
                        }
                    } else {
                        // the negated type is a different data range, e.g., we have 
                        // some integer and not some string, which is fine
                        return true;
                    }
                }
            }
        } else {
            // if the negated one if top, we are screwed
            if (!dataRange1.isTop()) {
                if (!dataRange2.isNegated()) {
                    // only the first range is negated
                    if (dataRange1.getDatatypeURI().equals(dataRange2.getDatatypeURI())) {
                        // both are of the same type, e.g., integer
                        if (!dataRange1.getEqualsValues().isEmpty() 
                                && !dataRange2.getEqualsValues().isEmpty()) {
                            // both have some value restrictions, e.g., equals 18
                            for (String value : dataRange1.getEqualsValues()) {
                                // check if one of the negated equals values has no 
                                // matching one in the non-negated restriction
                                if (!dataRange2.getEqualsValues().contains(value)) {
                                    return true;
                                }
                            }
                        } else {
                            // at least one of the restrictions has no further 
                            // equals restriction, so we are fine
                            return true;
                        }
                    } else {
                        // the negated range has a different type, so we are fine
                        return true;
                    }
                } else {
                    // both ranges are negated
                    // if the negated one is top, we are screwed
                    if (!dataRange2.isTop()) {
                        // this is fine since we have infinitely many values to 
                        // choose from and an unknown data range
                        return true;
                    }
                }
            } 
        }
        //System.out.println("Check failed!");
        return false;
    }
}
