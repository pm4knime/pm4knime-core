package org.pm4knime.settingsmodel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.ReplayerUtil;
import org.pm4knime.util.XLogUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.manifestreplayer.EvClassPattern;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

/**
 * this class will have a specified cost table for the parameter
 * 
 * @author kefang-pads
 *
 */
public class SMAlignmentReplayParameterWithCT extends SMAlignmentReplayParameter {
	private static final String DELIMETER = "::";
	private DefaultTableModel[] m_costTMs;
	
	
	public SMAlignmentReplayParameterWithCT(String configName) {
		// TODO Auto-generated constructor stub
		super(configName);

		m_costTMs = new DefaultTableModel[3];
		// we need to initialze the table model, but later??
		for(int i=0; i< CFG_COST_TYPE_NUM; i++){
			m_costTMs[i] = createTM(i);
		}
	}
	
	public DefaultTableModel[] getMCostTMs() {
		return m_costTMs;
	}

	public void setMCostTMs(DefaultTableModel[] m_costTMs) {
		this.m_costTMs = m_costTMs;
	}
	
	

	@Override
	protected void loadSettingsPure(NodeSettingsRO settings) throws InvalidSettingsException {
		// super.loadSettingsForModel(settings);
		// also load the cost values for the table, but at first to learn how to store
		super.loadSettingsPure(settings);
		
		for (int i = 0; i < CFG_COST_TYPE_NUM; i++) {
			loadTMFrom(i, settings);
		}
		
	}

	@Override
	protected void saveSettingsPure(NodeSettingsWO settings) {
		// here we can not override it, it will create more subSettings. 
		super.saveSettingsPure(settings);
		
		// use one indicator to say, we have saved the value here
		// on the contrast, we should also need to load the values from there
		
			for (int idx = 0; idx < CFG_COST_TYPE_NUM; idx++) {
				TableModel tModel = m_costTMs[idx];
				for (int i = 0; i < tModel.getRowCount(); i++) {
					settings.addString("T" + idx + DELIMETER + i,
							tModel.getValueAt(i, 0) + DELIMETER + tModel.getValueAt(i, 1));
					
				}
			}
		
	}

	private DefaultTableModel createTM(int idx) {
		DefaultTableModel tModel = new DefaultTableModel() {
			// private static final long serialVersionUID = 5238526181467190856L;
			@Override
			public boolean isCellEditable(int row, int column) {
				return (column != 0);
			};
		};

		tModel.addColumn(CFG_MOVE_KEY[idx]);
		tModel.addColumn(CFG_MCOST_KEY[idx]);

		return tModel;
	}

	public void setCostTM(Collection<String> nameList, int idx) {
		m_costTMs[idx].getDataVector().clear();
		// if this DataTable is new created, add column  names to it
		if(m_costTMs[idx].getDataVector().size() < 1) {
			// TODO : to distinguish the invisible transitions and set its cost to 0 
			// we have two parts from this.
   			for(String name : nameList ) {
   				if(PetriNetUtil.isTauName(name))
   					m_costTMs[idx].addRow(new Object[] {name, 0});
   				else
   					m_costTMs[idx].addRow(new Object[] {name, CFG_DEFAULT_MCOST[idx]});
			}
			
		}
		m_costTMs[idx].fireTableDataChanged();
	}
	private void loadTMFrom(int idx, NodeSettingsRO settings) throws InvalidSettingsException {

		DefaultTableModel tModel = m_costTMs[idx];
		if (tModel == null) {
			tModel = createTM(idx);
		}

		tModel.getDataVector().clear();

		// assign values to tables by adding rows there
		for (String key : settings.keySet()) {
			String[] splitStr = key.split(DELIMETER);
			if (splitStr[0].equals("T" + idx)) {
				// TODO: if settings save data in order or not
				// we meet the first values there, is there a need to do this??
				int rowIdx = Integer.valueOf(splitStr[1]);
				// System.out.println("the current read row id is " + rowIdx);
				// even if we want to add them, but how to do this?? are they in order at
				// first?? I don't think so
				String value = settings.getString(key);
				String[] sValues = value.split(DELIMETER);
				tModel.addRow(sValues);

			}
		}
		m_costTMs[idx] = tModel;
	}
	
	// for performance parameter
	@Override
    public PNManifestReplayerParameter getPerfParameter(XLog log, AcceptingPetriNet anet, XEventClassifier eventClassifier ) {
    	// how to create a table to assign such values here?? 
		// if many event classes are available here?? 
    	
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		Collection<XEventClass> eventClasses =  logInfo.getEventClasses().getClasses();

		PNManifestReplayerParameter parameters = new PNManifestReplayerParameter();
		//TODO : assign a better value here
		parameters.setMaxNumOfStates(1000);
		
		// get the pattern map for transition & event classes 
		TransClasses tc = new TransClasses(anet.getNet());
		Map<TransClass, Set<EvClassPattern>> pattern = ReplayerUtil.buildPattern(tc, eventClasses);
		TransClass2PatternMap mapping = new TransClass2PatternMap(log, anet.getNet(), eventClassifier, tc, pattern);
		parameters.setMapping(mapping);
		
		// set the move cost
		// TODO: if it is set by customized ?? We need to make it flexible
		int lmCost = getMDefaultCosts()[0].getIntValue();
		Map<XEventClass, Integer> mapLMCost = ReplayerUtil.buildLMCostMap(eventClasses, lmCost);
		int mmCost = getMDefaultCosts()[1].getIntValue();
		Map<TransClass, Integer> mapTMCost = ReplayerUtil.buildTMCostMap(tc, mmCost);
		int smCost = getMDefaultCosts()[2].getIntValue();
		Map<TransClass, Integer> mapSMCost = ReplayerUtil.buildTMCostMap(tc, smCost);
		
		parameters.setMapEvClass2Cost(mapLMCost);
		parameters.setTrans2Cost(mapTMCost);
		parameters.setTransSync2Cost(mapSMCost);
		
		
		parameters.setInitMarking(anet.getInitialMarking());
		Marking[] fmList = new Marking[anet.getFinalMarkings().size()];
		int i = 0;
		for(Marking m : anet.getFinalMarkings())
			fmList[i++] = m;
		
		parameters.setFinalMarkings(fmList);
		
		parameters.setGUIMode(false);
		
    	return parameters;
    } 

	// add conversion function to this parameter
	@Override
    public IPNReplayParameter getConfParameter(XLog log, AcceptingPetriNet anet, XEventClassifier eventClassifier,
    		XEventClass evClassDummy) {
    	XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		Collection<XEventClass> eventClasses =  logInfo.getEventClasses().getClasses();
		
		Map<XEventClass, Integer> mapEvClass2Cost = buildECCostMap(eventClasses, getMCostTMs()[0], evClassDummy);
		Map<Transition, Integer> mapTrans2Cost = buildTCostMap(anet.getNet().getTransitions(), getMCostTMs()[1]);
		Map<Transition, Integer> mapSync2Cost = buildTCostMap(anet.getNet().getTransitions(), getMCostTMs()[2]);
		
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
	
	
    private static Map<XEventClass, Integer> buildECCostMap(Collection<XEventClass> eventClasses, TableModel tModel, XEventClass dummyEC) {
		
		Map<XEventClass, Integer> mapEvClass2Cost = new HashMap();
		// get the cost parameters from the m_costTMs[0] for event classes
		for(int i=0; i< tModel.getRowCount(); i++) {
			String eventName = (String) tModel.getValueAt(i, 0);
			// find its cost and add them into 
			XEventClass eClass = XLogUtil.findEventClass(eventName, eventClasses);
			// meet dummy 
//			if(eClass == null){
//				eClass = super.evClassDummy;
//			}
			int lmCost = Integer.parseInt( (String) tModel.getValueAt(i, 1));
			mapEvClass2Cost.put(eClass, lmCost);
		}
		
		// need to add dummy event class here for compensate the use. The cost is 0 or not??
		// It can't happen, the shown model has event names we can't find it...
		//TODO : the cost for dummy event class
		mapEvClass2Cost.put(dummyEC, 0);
		return mapEvClass2Cost;
	}

	private static Map<Transition, Integer> buildTCostMap(Collection<Transition> transitions, TableModel tModel) {
		
		Map<Transition, Integer> mapTrans2Cost = new HashMap();
		for(Transition t : transitions) {
			 
			for(int i=0; i< tModel.getRowCount(); i++) {
				String tNames = (String) tModel.getValueAt(i, 0);
				String tName = tNames.split("[^a-zA-Z0-9\\s]")[0];
				
				if(tName.trim().equals(t.getLabel().trim())) {
					int mCost = Integer.parseInt( (String) tModel.getValueAt(i, 1));
					mapTrans2Cost.put(t, mCost);
					break;
				}
			}
			
		}
		/*
		for(int i=0; i< tModel.getRowCount(); i++) {
			String eventNames = (String) tModel.getValueAt(i, 0);
			String[] splitStr = eventNames.split("[^a-zA-Z0-9\\s]");
			// find its cost and add them into 
			Transition t = PetriNetUtil.findTransition(splitStr[0], transitions);
			// TODO: if the transition is invisible, the cost is zero there 
			int mCost = Integer.parseInt( (String) tModel.getValueAt(i, 1));
			mapTrans2Cost.put(t, mCost);
		}
		*/
		return mapTrans2Cost;
	}

	
	public static void main(String[] args) {
		// test this class... firstly about the clear function for TableModel
		SMAlignmentReplayParameterWithCT param = new SMAlignmentReplayParameterWithCT("test parameter");

		DefaultTableModel tModel = param.createTM(0);
		String[] sValues = { "reg", 1 + "" };
		tModel.addRow(sValues);
		tModel.addRow(sValues);

		tModel.setValueAt("name", 1, 0);

		tModel.getDataVector().clear();
		// after clear, there is no row, we need to take steps to add rows
		tModel.addRow(sValues);
		tModel.setValueAt("name", 1, 0);

	}
}
