package org.pm4knime.settingsmodel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelFlowVariableCompatible;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Type;

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
	
	public static String[] CFG_MCOST_KEY = {"log move cost", "model move cost",  "sync move cost"};
	static String[] CFG_MOVE_KEY = { "log move", "model move", "sync move"};

	static final String CFGKEY_STRATEGY_TYPE = "Strategy type";
	final String CKF_KEY_EVENT_CLASSIFIER = "Event classifier";
	
	protected final String m_configName;
	private SettingsModelString m_strategy;
	private SettingsModelString m_classifierName;
	private SettingsModelIntegerBounded[] m_defaultCosts;
	
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
		clone.setMStrategy(m_strategy);
		clone.setMDefaultCosts(m_defaultCosts);
		
		return clone;
	}

	@Override
	protected String getModelTypeID() {
		// TODO Auto-generated method stub
		return "SMID_"+ m_configName;
	}

	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
		// TODO Auto-generated method stub
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
		final NodeSettingsWO subSettings =
	            settings.addNodeSettings(m_configName);
		
		saveSettingsPure(subSettings);
	}
	
	protected void saveSettingsPure(NodeSettingsWO subSettings) {
		m_strategy.saveSettingsTo(subSettings);
    	m_classifierName.saveSettingsTo(subSettings);
    	
    	for(int i=0; i< CFG_COST_TYPE_NUM; i++){
    		m_defaultCosts[i].saveSettingsTo(subSettings);
    	}
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		final NodeSettingsRO subSettings =
                settings.getNodeSettings(m_configName);
		
		loadSettingsPure(subSettings);
	}
	
	protected void loadSettingsPure(NodeSettingsRO subSettings) throws InvalidSettingsException {
		
		m_strategy.loadSettingsFrom(subSettings);
    	m_classifierName.loadSettingsFrom(subSettings);
    	for(int i=0; i< CFG_COST_TYPE_NUM; i++){
    		m_defaultCosts[i].loadSettingsFrom(subSettings);
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
	
}
