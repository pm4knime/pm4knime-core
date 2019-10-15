package org.pm4knime.node.conformance;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.NodeView;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.visualization.projection.PNLogReplayProjectedVisPanel;

/**
 * <code>NodeView</code> for the "TesterCC" node.
 *
 * @author 
 */
public class TesterCCNodeView extends NodeView<TesterCCNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link TesterCCNodeModel})
     */
    protected TesterCCNodeView(final TesterCCNodeModel nodeModel) {
    	super(nodeModel);
        // TODO: create a view here for the ReplayerResult with Petri net 
        PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
        AcceptingPetriNet anet = nodeModel.getNetPO().getANet();
        XLog log = nodeModel.getLogPO().getLog();
        
        TransEvClassMapping map = nodeModel.getMapping();
        PNRepResult result = nodeModel.getRepResultPO().getRepResult();
        JComponent projectView;
		try {
			projectView = new PNLogReplayProjectedVisPanel(context, anet.getNet(), anet.getInitialMarking(), log, map, result);
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

