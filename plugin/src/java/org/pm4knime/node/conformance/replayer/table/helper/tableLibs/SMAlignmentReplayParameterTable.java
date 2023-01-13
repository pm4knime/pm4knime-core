package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelFlowVariableCompatible;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

public class SMAlignmentReplayParameterTable extends SettingsModel
implements SettingsModelFlowVariableCompatible{
	
	
	public static final int CFG_COST_TYPE_NUM = 3;
	public static final int[] CFG_DEFAULT_MCOST = {1,1,0};
	public static String[] CFG_MCOST_KEY = {"Move on log cost", "Move on model cost",  "Sync move cost"};
	static String[] CFG_MOVE_KEY = { "Move on log", "Move on model", "Sync move"};
	
	static final String CFGKEY_STRATEGY_TYPE = "Strategy type";
	final String CKF_KEY_EVENT_CLASSIFIER = "Event classifier";
	final String CKF_KEY_TIME_CLASSIFIER = "Timestamp attribute";
	final String CFG_KEY_CLASSIFIER_SET = "Event classifier set";
	final String CKF_KEY_TRACE_CLASSIFIER = "Trace classifier";
	
	// remove the final before string
	private String m_configName;
	
	private SettingsModelString m_strategy;
	private SettingsModelString m_classifierName;
	private SettingsModelString m_classifierTime;
	private SettingsModelString m_classifierTrace;
	private SettingsModelStringArray classifierSet; 
	private SettingsModelIntegerBounded[] m_defaultCosts;
	
	public SMAlignmentReplayParameterTable() {}
	
	public SMAlignmentReplayParameterTable(final String configName) {
		if ((configName == null) || "".equals(configName)) {
            throw new IllegalArgumentException("The configName must be a "
                    + "non-empty string");
        }
		m_configName = configName;
		
		// initialize the fileds, but the truly values, we need to set it later. make sure that we have choosen 
		// the default values in dialog
		m_strategy = new SettingsModelString(CFGKEY_STRATEGY_TYPE, "");
		m_classifierName = new SettingsModelString(CKF_KEY_EVENT_CLASSIFIER, "");
		m_classifierTime = new SettingsModelString(CKF_KEY_TIME_CLASSIFIER, "");
		classifierSet = new SettingsModelStringArray(CFG_KEY_CLASSIFIER_SET, 
					new String[] {""});
		m_classifierTrace = new SettingsModelString(CKF_KEY_TRACE_CLASSIFIER, "");
		m_defaultCosts = new SettingsModelIntegerBounded[CFG_COST_TYPE_NUM];
		
		// m_defaultCosts here 
		for( int i=0; i< CFG_COST_TYPE_NUM ; i++) {
			m_defaultCosts[i] = new SettingsModelIntegerBounded(CFG_MCOST_KEY[i], CFG_DEFAULT_MCOST[i], 0, Integer.MAX_VALUE );
    	}
	}

	
	
	public String getConfigName() {
        return m_configName;
    }
	
	public SettingsModelString getMStrategy() {
		return m_strategy;
	}

	// but how to initialize strategy here?? 
	public void setMStrategy(SettingsModelString m_strategy) {
		this.m_strategy = m_strategy;
	}

	public SettingsModelString getMClassifierName() {
		return m_classifierName;
	}
	
	
	public SettingsModelString getMClassifierTime() {
		return m_classifierTime;
	}
	
	public SettingsModelString getMClassifierTrace() {
		return m_classifierTrace;
	}

	public void setMClassifierName(SettingsModelString m_classifierName) {
		this.m_classifierName = m_classifierName;
	}
	
	public void setMClassifierTime(SettingsModelString m_classifierTime) {
		this.m_classifierTime = m_classifierTime;
	}
	
	public void setMClassifierTrace(SettingsModelString m_classifierTrace) {
		this.m_classifierTrace = m_classifierTrace;
	}

	public SettingsModelIntegerBounded[] getMDefaultCosts() {
		return m_defaultCosts;
	}

	public void setMDefaultCosts(SettingsModelIntegerBounded[] defaultCosts) {
		this.m_defaultCosts = defaultCosts;
	}
	
	public SettingsModelStringArray getClassifierSet() {
		return classifierSet;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return m_configName;
	}

	@Override
	public Type getFlowVariableType() {
		// TODO Auto-generated method stub, not know the use is for what
		return FlowVariable.Type.STRING;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SMAlignmentReplayParameterTable createClone() {
		// TODO Auto-generated method stub
		SMAlignmentReplayParameterTable clone = new SMAlignmentReplayParameterTable(m_configName);
		clone.setMClassifierName(m_classifierName);
		clone.setMClassifierTime(m_classifierTime);
//		clone.setClassifierSet(classifierSet);
		clone.setMStrategy(m_strategy);
		clone.setMDefaultCosts(m_defaultCosts);
		clone.setMClassifierTrace(m_classifierTrace);
		
		return clone;
	}

	// here we only set the values for the array, not substitute the values
	public void setClassifierSet(String[] newValue) {
		classifierSet.setStringArrayValue(newValue);
	}

	@Override
	protected String getModelTypeID() {
		// TODO Auto-generated method stub
		return "SMID_"+ m_configName;
	}

	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		// TODO we get the setting from PortObjectSpec. How to load it ??
		try {
			
			loadSettingsForModel(settings);
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected void saveSettingsForDialog(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		final NodeSettingsWO subSettings  = settings; //= settings.addNodeSettings(m_configName);
		
		saveSettingsPure(subSettings);
	}
	
	protected void saveSettingsPure(NodeSettingsWO subSettings) {
		m_strategy.saveSettingsTo(subSettings);
    	m_classifierName.saveSettingsTo(subSettings);
    	m_classifierTime.saveSettingsTo(subSettings);
    	// one difficulty is how to get it from the classifierSet?? 
    	// how to get and save it here??
    	classifierSet.saveSettingsTo(subSettings);
    	m_classifierTrace.saveSettingsTo(subSettings);
    	
    	for(int i=0; i< CFG_COST_TYPE_NUM; i++){
    		m_defaultCosts[i].saveSettingsTo(subSettings);
    	}
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_strategy.validateSettings(settings);
		m_classifierName.validateSettings(settings);
		m_classifierTime.validateSettings(settings);
		m_classifierTrace.validateSettings(settings);
		classifierSet.validateSettings(settings);
		

    	for(int i=0; i< CFG_COST_TYPE_NUM; i++){
    		m_defaultCosts[i].validateSettings(settings);
    	}
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		final NodeSettingsRO subSettings =  settings; //   settings.getNodeSettings(m_configName);
		
		loadSettingsPure(subSettings);
	}
	
	protected void loadSettingsPure(NodeSettingsRO subSettings) throws InvalidSettingsException {
		
		m_strategy.loadSettingsFrom(subSettings);
		
		m_classifierName.loadSettingsFrom(subSettings);
		m_classifierTime.loadSettingsFrom(subSettings);
    	classifierSet.loadSettingsFrom(subSettings);
    	m_classifierTrace.loadSettingsFrom(subSettings);
    	
    		for(int i=0; i< CFG_COST_TYPE_NUM; i++){
    			if(subSettings.containsKey(CFG_MCOST_KEY[i])) {
    				m_defaultCosts[i].loadSettingsFrom(subSettings);
        	}
    	}
    	
	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		try {
			saveSettingsForDialog(settings);
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return getClass().getSimpleName() + " ('" + m_configName + "')";
	}
	

	// add conversion function to this parameter
    public IPNReplayParameter getConfParameter(TableEventLog log, AcceptingPetriNet anet, String eventClassifier,
    		String evClassDummy) {
		Collection<String> eventClasses = Arrays.asList(log.getActivties());
		
		int[] defaultCosts = new int [3];
		for(int i=0; i< SMAlignmentReplayParameterTable.CFG_COST_TYPE_NUM; i++){
			defaultCosts[i] = getMDefaultCosts()[i].getIntValue();
		}
		
		// here we also need dummy event class for the cost, but we don't need to share it in another
		// version. 
		IPNReplayParameter parameters = new CostBasedCompleteParamTable(eventClasses,
				evClassDummy, anet.getNet().getTransitions(), defaultCosts[0], defaultCosts[1]);
		
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
    
	// add another conversion function to this parameter
    public PNManifestReplayerParameterTable getPerfParameter(TableEventLog log, AcceptingPetriNet anet) {
    	// how to create a table to assign such values here?? 
		// if many event classes are available here?? 
    	
		Collection<String> eventClasses =  Arrays.asList(log.getActivties());

		PNManifestReplayerParameterTable parameters = new PNManifestReplayerParameterTable();
		//TODO : assign a better value here
		// parameters.setMaxNumOfStates(200000);
		
		// get the pattern map for transition & event classes 
		TransClasses tc = new TransClasses(anet.getNet());
		Map<TransClass, Set<EvClassPatternTable>> pattern = ReplayerUtilTable.buildPattern(tc, eventClasses);
		TransClass2PatternMapTable mapping = new TransClass2PatternMapTable(log, anet.getNet(), tc, pattern);
		
		parameters.setMapping(mapping);
		
		// set the move cost
		int lmCost = getMDefaultCosts()[0].getIntValue();
		Map<String, Integer> mapLMCost = ReplayerUtilTable.buildLMCostMap(eventClasses, lmCost);
		
		Set<TransClass> tauTC = new HashSet();
		for(Transition t : anet.getNet().getTransitions()) {
			if(t.isInvisible())
				tauTC.add(mapping.getTransClassOf(t));
		}
		int mmCost = getMDefaultCosts()[1].getIntValue();
		Map<TransClass, Integer> mapTMCost = ReplayerUtilTable.buildTMCostMap(tc, mmCost, tauTC);
		int smCost = getMDefaultCosts()[2].getIntValue();
		Map<TransClass, Integer> mapSMCost = ReplayerUtilTable.buildTMCostMap(tc, smCost, tauTC);
		
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
	

}
