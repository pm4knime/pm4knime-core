package org.pm4knime.node.conformance.table.performance;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.knime.core.node.NodeView;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ManifestEvClassPatternTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ManifestPerfPanelTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ManifestTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PerfCounterTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.ReliablePerfCounterTable;
import org.pm4knime.settingsmodel.SMPerformanceParameter;
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.plugins.manifestanalysis.visualization.performance.TransPerfDecorator;


/**
 * <code>NodeView</code> for the "PerformanceChecker" node.
 * // to get the mainfest information there and show the result.. directly view here
    // to open the view after reloading, need data: 
    //  Manifest, parameter, counter generated, Petri net in connection. 
    // counter, and Petri net in connection, are easy to generate.. 
     * how to serialize manifest??
     *   
    // so we do the process again like in execution. 
     * considering the replayer list might change?? Tried to create Panel directly from data
     * however, too many functions can't be used.
    
 * @author Kefang Ding
 */
public class PerformanceCheckerNodeView extends NodeView<PerformanceCheckerNodeModel> {
	// zoom-related properties
	// The maximal zoom factor for the primary view on the transition system.
	private JPanel m_viewPanel;
	public static final int MAX_ZOOM = 1200;
		
	ScalableComponent scalable ;
	ProMJGraph graph;
	protected Map<Transition, TransPerfDecorator> decoratorMap = new HashMap<Transition, TransPerfDecorator>();
    /**
     * Creates a new view to show performance projected on model
     * 
     * @param nodeModel The model (class: {@link PerformanceCheckerNodeModel})
     * @param viewPanel 
     */
    protected PerformanceCheckerNodeView(final PerformanceCheckerNodeModel nodeModel, JPanel viewPanel) {
        super(nodeModel);
        // TODO: to show the performance log information
        // two situation, one is the one directly from execution 
        m_viewPanel = viewPanel; 
        setComponent(m_viewPanel);
    }

    
    
	private JPanel createProjectionPanel(final PerformanceCheckerNodeModel nodeModel) {
    	 
    	 
    	 ManifestTable mResult = nodeModel.getMainfestResult();
         PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
         // we add connection here to satisfy the creation of project Panel
         GraphLayoutConnection netConn = new GraphLayoutConnection(mResult.getNet());
         context.getConnectionManager().addConnection(netConn);
         // counter and timeAttr from m_parameter. we need to create it here
         SMPerformanceParameter m_parameter = nodeModel.getMParameter();
         
         PerfCounterTable counter;
         if (m_parameter.isMWithSynMove().getBooleanValue()) {
 			counter = new ReliablePerfCounterTable();
 		} else
 			counter = new PerfCounterTable();
         String timeAttr = m_parameter.getMTimeStamp().getStringValue();
         
         if(timeAttr.contains(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX)) {
         	timeAttr = timeAttr.split(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX)[1];
         }
         
         boolean withUnreliabelResult = m_parameter.isMWithUnreliableResult().getBooleanValue();
         try {
 			ManifestPerfPanelTable<ManifestEvClassPatternTable, PerfCounterTable> projectView = new ManifestPerfPanelTable<ManifestEvClassPatternTable, PerfCounterTable>(
 					context, (ManifestEvClassPatternTable) mResult, counter, timeAttr, withUnreliabelResult);
 			
 			projectView.setName("Performance Projection");
 			return projectView;
 		} catch (ConnectionCannotBeObtained e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
        return null;
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: if it is the first time to get this node model??
    	 if(getNodeModel() != null) {
    		 PerformanceCheckerNodeModel nodeModel = getNodeModel() ;
    		 if(nodeModel.getMainfestResult() != null)
    			 m_viewPanel.add(createProjectionPanel(nodeModel));
    	 }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO: generated method stub
    }

}

