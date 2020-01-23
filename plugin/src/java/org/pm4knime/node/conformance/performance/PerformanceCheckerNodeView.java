package org.pm4knime.node.conformance.performance;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.NodeView;
import org.pm4knime.settingsmodel.SMPerformanceParameter;
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.plugins.manifestanalysis.visualization.performance.ManifestPerfPanel;
import org.processmining.plugins.manifestanalysis.visualization.performance.PerfCounter;
import org.processmining.plugins.manifestanalysis.visualization.performance.ReliablePerfCounter;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.manifestreplayresult.ManifestEvClassPattern;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.visualization.PNLogReplayResultVisPanel;

/**
 * <code>NodeView</code> for the "PerformanceChecker" node.
 *
 * @author Kefang Ding
 */
public class PerformanceCheckerNodeView extends NodeView<PerformanceCheckerNodeModel> {

    /**
     * Creates a new view to show performance projected on model
     * 
     * @param nodeModel The model (class: {@link PerformanceCheckerNodeModel})
     */
    protected PerformanceCheckerNodeView(final PerformanceCheckerNodeModel nodeModel) {
        super(nodeModel);
        // TODO: to show the performance log information
        // to get the mainfest information there and show the result.. directly view here
        
        Manifest mResult = nodeModel.getMainfestResult();
        PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
        // we add connection here to satisfy the creation of project Panel
        GraphLayoutConnection netConn = new GraphLayoutConnection(mResult.getNet());
        context.getConnectionManager().addConnection(netConn);
        // counter and timeAttr from m_parameter. we need to create it here
        SMPerformanceParameter m_parameter = nodeModel.getMParameter();
        
        PerfCounter counter = nodeModel.getCounter();
        String timeAttr = m_parameter.getMTimeStamp().getStringValue();
        
        if(timeAttr.contains(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX)) {
        	timeAttr = timeAttr.split(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX)[1];
        }
        
        boolean withUnreliabelResult = m_parameter.isMWithUnreliableResult().getBooleanValue();
        try {
			ManifestPerfPanel<ManifestEvClassPattern, PerfCounter> projectView = new ManifestPerfPanel<ManifestEvClassPattern, PerfCounter>(
					context, (ManifestEvClassPattern) mResult, counter, timeAttr, withUnreliabelResult);
			
			projectView.setName("Performance Projection");
			setComponent(projectView);
		} catch (ConnectionCannotBeObtained e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
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

