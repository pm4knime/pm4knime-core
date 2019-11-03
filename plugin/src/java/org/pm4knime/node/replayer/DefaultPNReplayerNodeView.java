package org.pm4knime.node.replayer;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.NodeView;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.visualization.projection.PNLogReplayProjectedVisPanel;

/**
 * <code>NodeView</code> for the "PNReplayer" node.
 *
 * @author 
 */
public class DefaultPNReplayerNodeView extends NodeView<DefaultPNReplayerNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link DefaultPNReplayerNodeModel})
     */
    protected DefaultPNReplayerNodeView(final DefaultPNReplayerNodeModel nodeModel) {
        super(nodeModel);
        
        RepResultPortObject reResultPO = nodeModel.getRepResultPO();
        XLog log = reResultPO.getLog();
        AcceptingPetriNet anet = reResultPO.getNet();
        PNRepResult result = reResultPO.getRepResult();
        
        TransEvClassMapping map = PetriNetUtil.constructMapping(log, anet.getNet(), nodeModel.getXEventClassifier(), nodeModel.evClassDummy);
        
        JComponent projectView;
		try {
			PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
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

