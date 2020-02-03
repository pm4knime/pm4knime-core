package org.pm4knime.node.conformance.fitness;

import java.util.Map;

import javax.swing.JComponent;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.NodeView;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.pnalignanalysis.visualization.projection.PNLogReplayProjectedVisPanel;

/**
 * <code>NodeView</code> for the "ConformanceChecker" node.
 *
 * @author 
 */
public class FitnessCheckerNodeView extends NodeView<FitnessCheckerNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link FitnessCheckerNodeModel})
     */
    protected FitnessCheckerNodeView(final FitnessCheckerNodeModel nodeModel) {
        super(nodeModel);
        // TODO: show the conformance projection here 
        RepResultPortObject repResultPO = nodeModel.getRepResultPO();
        XLog log = repResultPO.getLog();
        AcceptingPetriNet anet = repResultPO.getNet();
        PNRepResult result = repResultPO.getRepResult();
        Map<String, Object> infoMap = result.getInfo();
        // how to get the eventclassifier?? We need more stuff to do in XLogUtil, but the evClassDummy?? What to do with this?
        // This is only one model from the replay. Do we need them so badly?? 
        // to benefit the dummyEventClass, we can save it in the repResult info table and use it here!!
        String eclsString  = (String) infoMap.get(XLogUtil.CFG_DUMMY_ECNAME);
		XEventClass dummy = XLogUtil.deserializeEventClass(eclsString);
		
		String eclassifierString  = (String) infoMap.get(XLogUtil.CFG_EVENTCLASSIFIER_NAME);
		XEventClassifier eventClassifier = XLogUtil.getEventClassifier(log, eclassifierString);
        // anet is different from the ones that we used to replay. But we can't change the reading method. Write Emial to talk about this!!
        TransEvClassMapping map = PetriNetUtil.constructMapping(log, anet.getNet(), eventClassifier, dummy);
        // only one way to make them match is to change the transitions in replayed result!!! 
        // it is in the view. Only this one, we need to fix?? Or we need to fix all the ones after reloading??
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

