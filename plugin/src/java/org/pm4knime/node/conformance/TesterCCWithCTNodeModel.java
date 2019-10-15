package org.pm4knime.node.conformance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.table.TableModel;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.pm4knime.settingsmodel.SMAlignmentReplayParameterWithCT;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.XLogUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

/**
 * <code>NodeModel</code> for the "TesterCCWithCT" node.
 * this extends the node and implement the functions.
 * @author Kefang Ding
 */
public class TesterCCWithCTNodeModel extends TesterCCNodeModel {
    
	@Override
	protected void initializeParameter() {
    	m_parameter = new SMAlignmentReplayParameterWithCT("Parameter in Tester with CT");
    }
	
	@Override
	protected IPNReplayParameter getParameters(XLog log, AcceptingPetriNet anet, XEventClassifier eventClassifier, XEventClass evClassDummy) {
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		Collection<XEventClass> eventClasses =  logInfo.getEventClasses().getClasses();
		SMAlignmentReplayParameterWithCT m_tmp = (SMAlignmentReplayParameterWithCT) m_parameter;
		
		Map<XEventClass, Integer> mapEvClass2Cost = buildECCostMap(eventClasses, m_tmp.getMCostTMs()[0]);
		Map<Transition, Integer> mapTrans2Cost = buildTCostMap(anet.getNet().getTransitions(), m_tmp.getMCostTMs()[1]);
		Map<Transition, Integer> mapSync2Cost = buildTCostMap(anet.getNet().getTransitions(), m_tmp.getMCostTMs()[2]);
		
		IPNReplayParameter parameters = new CostBasedCompleteParam(mapEvClass2Cost, mapTrans2Cost, mapSync2Cost);// createCostParameter(eventClasses, anet.getNet().getTransitions());
		parameters.setInitialMarking(anet.getInitialMarking());
		// here cast needed to transfer from Set<Marking> to Marking[]
		Marking[] fmList = new Marking[anet.getFinalMarkings().size()];
		int i = 0;
		for(Marking m : anet.getFinalMarkings())
			fmList[i++] = m;
    	
		parameters.setFinalMarkings(fmList);
    	parameters.setGUIMode(false);
		parameters.setCreateConn(false);
		
    	return parameters;
	}
	
	private Map<XEventClass, Integer> buildECCostMap(Collection<XEventClass> eventClasses, TableModel tModel) {
		
		Map<XEventClass, Integer> mapEvClass2Cost = new HashMap();
		// get the cost parameters from the m_costTMs[0] for event classes
		for(int i=0; i< tModel.getRowCount(); i++) {
			String eventName = (String) tModel.getValueAt(i, 0);
			// find its cost and add them into 
			XEventClass eClass = XLogUtil.findEventClass(eventName, eventClasses);
			// meet dummy 
			if(eClass == null){
				eClass = super.evClassDummy;
			}
			int lmCost = Integer.parseInt( (String) tModel.getValueAt(i, 1));
			mapEvClass2Cost.put(eClass, lmCost);
		}
		return mapEvClass2Cost;
	}

	private Map<Transition, Integer> buildTCostMap(Collection<Transition> transitions, TableModel tModel) {
		
		Map<Transition, Integer> mapTrans2Cost = new HashMap();
		
		for(int i=0; i< tModel.getRowCount(); i++) {
			String eventNames = (String) tModel.getValueAt(i, 0);
			String[] splitStr = eventNames.split("[^a-zA-Z0-9\\s]");
			// find its cost and add them into 
			Transition t = PetriNetUtil.findTransition(splitStr[0], transitions);
			
			int mCost = Integer.parseInt( (String) tModel.getValueAt(i, 1));
			mapTrans2Cost.put(t, mCost);
		}
		return mapTrans2Cost;
	}

}

