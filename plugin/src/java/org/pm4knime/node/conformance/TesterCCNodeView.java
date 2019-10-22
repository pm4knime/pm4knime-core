package org.pm4knime.node.conformance;

import javax.swing.JComponent;

import org.deckfour.xes.model.XLog;
import org.knime.core.node.NodeView;
import org.pm4knime.util.PetriNetUtil;
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
        
        // pay attention to this method, because the new loaded event transitions and classes are different after reopening 
        // the workflow. It loads a different log and net.[especially net]. But ReplayResult uses old values
        // In this way, it makes the codes for mapping later not working, null return. 
        // Solutions: 
        // 1. How to make sure the serialization reloaded as the same model??
        //  Petrinet, the id is generated by adding transitions, not ensure the id same..
        // so we lose the points here, how to make the map as the same?? 
        // mapTrans2Int is generated from net, but the compared single transition is from replay result...
        // No way to change the codes here, except we code the pnml reading method by ourselves...  
        // serialization of net is hard to change.
        // 2. serialize view component. 
        //  -- generate view in NodeModel and pass it to view
        // not feasible due to the passing is only for port objects not for JComponent. But Jcomponent can be serialized
        TransEvClassMapping map = PetriNetUtil.constructMapping(log, anet.getNet(), nodeModel.getXEventClassifier(), nodeModel.evClassDummy);
        
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
     * mapping is in need to create the view there, could we create a JPanel here and then pass it to view
     * save the mapping w.r.t. the current event classfier and input data;; 
     * if serialization is in need, we need to serialize mapping at first by making it as one port object
     * or recreate the mapping. 
     * -- mapping = PetriNetUtil.constructMapping(log, anet.getNet(), eventClassifier, evClassDummy);
	   eventClassifier is in need and also the evClassDummy, couldn't pass it clearly. 
     * @return
     */
    
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
