package org.pm4knime.settingsmodel;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
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
import org.pm4knime.util.ReplayerUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.manifestreplayer.EvClassPattern;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

/**
 * this class is used to store the parameter for the alignment replayer. 
 * It is served as IPNReplayParameter in KNIME. Possible solutions
 * It includes: 
 *    -- classifier name
 *    -- replayer strategy
 *    -- cost assign in a table or a default values for this... 
 *       [extend the unified class for the one with Input PortObject]
 * 
 * After creating this, we can convert this into the required parameter in NodeModel. 
 * Could node model change the Settings from Dialog?? Not really... 
 * what is the point to saveSettings in NodeModel ?? we just need to load it there!!  
 * For dialog, we need to save it into Model, but why is required to load from Model?
 * This is not from the Model but the serialization...  
 * Dialogs values shown  :: 
 *   1. constructed by default value... 
 *   2. assigned by the stored workflow... For this, we need loadMethod
 *   
 *   3. save values into workflow 
 * NodeModel ::
 *   1. assigned by load methods from the workflow
 *   2. save values into workflow... If there is any changes on the values, can correct it
 *   but we need to make sure that they are the same settings and same values there!!
 *   
 * Methods::
 *    -- getXX
 *    -- convert to parameter
 *    -- 
 * @author kefang-pads
 *
 */
public class SMAlignmentReplayParameter extends SettingsModel
implements SettingsModelFlowVariableCompatible {
	
	public static final int CFG_COST_TYPE_NUM = 3;
	public static final int[] CFG_DEFAULT_MCOST = {1,1,0};
	public static String[] CFG_MCOST_KEY = {"Move on log cost", "Move on model cost",  "Sync move cost"};
	static String[] CFG_MOVE_KEY = { "Move on log", "Move on model", "Sync move"};
	
	static final String CFGKEY_STRATEGY_TYPE = "Strategy type";
	final String CKF_KEY_EVENT_CLASSIFIER = "Event classifier";
	final String CFG_KEY_CLASSIFIER_SET = "Event classifier set";
	
	// remove the final before string
	private String m_configName;
	
	private SettingsModelString m_strategy;
	private SettingsModelString m_classifierName;
	private SettingsModelStringArray classifierSet; 
	private SettingsModelIntegerBounded[] m_defaultCosts;
	
	public SMAlignmentReplayParameter() {}
	
	public SMAlignmentReplayParameter(final String configName) {
		if ((configName == null) || "".equals(configName)) {
            throw new IllegalArgumentException("The configName must be a "
                    + "non-empty string");
        }
		m_configName = configName;
		
		// initialize the fileds, but the truly values, we need to set it later. make sure that we have choosen 
		// the default values in dialog
		m_strategy = new SettingsModelString(CFGKEY_STRATEGY_TYPE, "");
		m_classifierName = new SettingsModelString(CKF_KEY_EVENT_CLASSIFIER, "");
		classifierSet = new SettingsModelStringArray(CFG_KEY_CLASSIFIER_SET, 
					new String[] {""});
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

	public void setMClassifierName(SettingsModelString m_classifierName) {
		this.m_classifierName = m_classifierName;
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
	protected SMAlignmentReplayParameter createClone() {
		// TODO Auto-generated method stub
		SMAlignmentReplayParameter clone = new SMAlignmentReplayParameter(m_configName);
		clone.setMClassifierName(m_classifierName);
//		clone.setClassifierSet(classifierSet);
		clone.setMStrategy(m_strategy);
		clone.setMDefaultCosts(m_defaultCosts);
		
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
    	// one difficulty is how to get it from the classifierSet?? 
    	// how to get and save it here??
    	classifierSet.saveSettingsTo(subSettings);
    	
    	for(int i=0; i< CFG_COST_TYPE_NUM; i++){
    		m_defaultCosts[i].saveSettingsTo(subSettings);
    	}
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_strategy.validateSettings(settings);
		m_classifierName.validateSettings(settings);
		
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
    	classifierSet.loadSettingsFrom(subSettings);
    	
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
    public IPNReplayParameter getConfParameter(XLog log, AcceptingPetriNet anet, XEventClassifier eventClassifier,
    		XEventClass evClassDummy) {
    	XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		Collection<XEventClass> eventClasses =  logInfo.getEventClasses().getClasses();
		
		int[] defaultCosts = new int [3];
		for(int i=0; i< SMAlignmentReplayParameter.CFG_COST_TYPE_NUM; i++){
			defaultCosts[i] = getMDefaultCosts()[i].getIntValue();
		}
		
		// here we also need dummy event class for the cost, but we don't need to share it in another
		// version. 
		IPNReplayParameter parameters = new CostBasedCompleteParam(eventClasses,
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
	
}
