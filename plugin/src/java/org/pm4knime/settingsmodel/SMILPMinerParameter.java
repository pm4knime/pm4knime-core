package org.pm4knime.settingsmodel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.SettingsModel;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelFlowVariableCompatible;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.workflow.FlowVariable;
import org.knime.core.node.workflow.FlowVariable.Type;
import org.processmining.hybridilpminer.parameters.DiscoveryStrategy;
import org.processmining.hybridilpminer.parameters.DiscoveryStrategyType;
import org.processmining.hybridilpminer.parameters.LPConstraintType;
import org.processmining.hybridilpminer.parameters.LPFilter;
import org.processmining.hybridilpminer.parameters.LPFilterType;
import org.processmining.hybridilpminer.parameters.LPObjectiveType;
import org.processmining.hybridilpminer.parameters.LPVariableType;
import org.processmining.hybridilpminer.parameters.NetClass;
import org.processmining.hybridilpminer.parameters.XLogHybridILPMinerParametersImpl;
import org.processmining.lpengines.interfaces.LPEngine.EngineType;

/**
 * Parameter: 
 * 		Normal Options:
 * 			-- event classifier
 * 			-- filter type and threshold
 * 			-- miner strategy
 * 		Advanced Options: 
 * 			-- LP-Filter : sequence-encoding filter, slack variable fiter, none
 * 			-- LP Objective: unweighted values, weighted values/ relative absolute freq
 *  		-- LP Variable type: two variable per event, one variable per event
 *  		-- Discovery Strategy: mine a place per causal relation, a connection place between each pair
 *   
 * 		In default:
 * 			-- desired resulting net: Petri net
 * 			-- empty after completion
 * 			-- add sink place
 * 		
 * 		No need for this: use dot to previw graph
 * @author kefang-pads
 *
 */

public class SMILPMinerParameter extends SettingsModel
	implements SettingsModelFlowVariableCompatible {

	public static final String CFG_KEY_CLASSIFIER = "Event Classifier";
	public static final String CFG_KEY_FILTER_TYPE = "Filter Type";
	public static final String[] CFG_FILTER_TYPES = {LPFilterType.NONE.name(), 
			LPFilterType.SEQUENCE_ENCODING.name(), LPFilterType.SLACK_VAR.name()};
	public static final String CFG_KEY_FILTER_THRESHOLD = "Noise Threshold";
	public static final String CFG_KEY_MINER_ALGORITHM = "Miner Algorithm";
	
	private String m_configName;
	SettingsModelString m_filterType; //, m_algorithm;
	SettingsModelDoubleBounded m_filterThreshold;
	
	// advanced settings
	// LP Objective
	public static final String CFG_KEY_LPOBJ = "LP Objective";
	public static final String[] CFG_LPOBJ_TYPES = { 
			LPObjectiveType.WEIGHTED_ABSOLUTE_PARIKH.name(), LPObjectiveType.WEIGHTED_RELATIVE_PARIKH.name(),
			LPObjectiveType.UNWEIGHTED_PARIKH.name(), LPObjectiveType.MINIMIZE_ARCS.name()};
	// LP Variables
	public static final String CFG_KEY_LPVAR = "LP Variable";
	public static final String[] CFG_LPVAR_TYPES = {LPVariableType.DUAL.name(), LPVariableType.HYBRID.name(),
			LPVariableType.SINGLE.name()};
	// Discovery strategy 
	public static final String CFG_KEY_DS = "Discovery Strategy";
	public static final String[] CFG_DS_TYPES = {
			 DiscoveryStrategyType.CAUSAL_FLEX_HEUR.name(), DiscoveryStrategyType.TRANSITION_PAIR.name(),};
	
	SettingsModelString m_lpObj, m_lpVar, m_ds;
	
	
	public SMILPMinerParameter(String configName) {
		if ((configName == null) || "".equals(configName)) {
            throw new IllegalArgumentException("The configName must be a "
                    + "non-empty string");
        }
		m_configName = configName;
		
//		m_clf = new SettingsModelString(CFG_KEY_CLASSIFIER, "");
		m_filterType = new SettingsModelString(CFG_KEY_FILTER_TYPE, CFG_FILTER_TYPES[0]);
		m_filterThreshold = new SettingsModelDoubleBounded(CFG_KEY_FILTER_THRESHOLD, 0.25, 0, 1.0);
		m_filterThreshold.setEnabled(false);
		
		m_filterType.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if(m_filterType.getStringValue().equals(CFG_FILTER_TYPES[0])) {
					m_filterThreshold.setEnabled(false);
				}else
					m_filterThreshold.setEnabled(true);
			}
			
		});
		
		// advanced settings
		m_lpObj = new SettingsModelString(CFG_KEY_LPOBJ, CFG_LPOBJ_TYPES[0]);
		m_lpVar = new SettingsModelString(CFG_KEY_LPVAR, CFG_LPVAR_TYPES[0]);
		m_ds = new SettingsModelString(CFG_KEY_DS, CFG_DS_TYPES[0]);
		
	}
	
	public SettingsModelString getMLPObj() {
		return m_lpObj;
	}

	public void setMLPObj(SettingsModelString m_lpObj) {
		this.m_lpObj = m_lpObj;
	}

	public SettingsModelString getMLPVar() {
		return m_lpVar;
	}

	public void setMLPVar(SettingsModelString m_lpVar) {
		this.m_lpVar = m_lpVar;
	}

	public SettingsModelString getMDS() {
		return m_ds;
	}

	public void setMDS(SettingsModelString m_ds) {
		this.m_ds = m_ds;
	}
/*
	public SettingsModelString getMclf() {
		return m_clf;
	}

	public void setMclf(SettingsModelString m_clf) {
		this.m_clf = m_clf;
	}
*/
	public SettingsModelString getMfilterType() {
		return m_filterType;
	}

	public void setMfilterType(SettingsModelString m_filterType) {
		this.m_filterType = m_filterType;
	}
/*
	public SettingsModelString getMalgorithm() {
		return m_algorithm;
	}

	public void setMalgorithm(SettingsModelString m_algorithm) {
		this.m_algorithm = m_algorithm;
	}
*/
	public SettingsModelDoubleBounded getMfilterThreshold() {
		return m_filterThreshold;
	}

	public void setMfilterThreshold(SettingsModelDoubleBounded m_filterThreshold) {
		this.m_filterThreshold = m_filterThreshold;
	}

	
	// this is one part with the default settings
	// some parameters are decided by the advanced settings. Still there are sth one are for the advanced ones
	// due to the default values are already there, so we can have it directly..But need to distinguish if we 
	// use the advanced settings, or not 
	public XLogHybridILPMinerParametersImpl updateParameter(XLogHybridILPMinerParametersImpl param) {
		// set default values to param here, others we need to count it later
		param.setDiscoveryStrategy(new DiscoveryStrategy(DiscoveryStrategyType.valueOf(m_ds.getStringValue())));
		param.setObjectiveType(LPObjectiveType.valueOf(m_lpObj.getStringValue()));
		param.setVariableType(LPVariableType.valueOf(m_lpVar.getStringValue()));
		// set the filter type
		LPFilter filter = new LPFilter(LPFilterType.valueOf(m_filterType.getStringValue()),
				m_filterThreshold.getDoubleValue());
		param.setFilter(filter);
		
		// in default settings
		param.setNetClass(NetClass.PT_NET);
		param.getLPConstraintTypes().add(LPConstraintType.EMPTY_AFTER_COMPLETION);
		param.setFindSink(true); // add sink to model
		param.setEngineType(EngineType.LPSOLVE);
		param.setApplyStructuralRedundantPlaceRemoval(false);
		return param;
	} 
	
	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return m_configName;
	}

	@Override
	public Type getFlowVariableType() {
		// TODO Auto-generated method stub
		return FlowVariable.Type.CREDENTIALS;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SMILPMinerParameter createClone() {
		// TODO Auto-generated method stub
		SMILPMinerParameter clone =  new SMILPMinerParameter(m_configName);
//		clone.setMclf(m_clf);
		clone.setMfilterType(m_filterType);
		clone.setMfilterThreshold(m_filterThreshold);
//		clone.setMalgorithm(m_algorithm);
		
		clone.setMLPObj(m_lpObj);
		clone.setMLPVar(m_lpVar);
		clone.setMDS(m_ds);
		return clone;
	}

	@Override
	protected String getModelTypeID() {
		// TODO Auto-generated method stub
		return "SMID_"+ m_configName;
	}

	@Override
	protected String getConfigName() {
		// TODO Auto-generated method stub
		return m_configName;
	}

	
	@Override
	protected void loadSettingsForDialog(NodeSettingsRO settings, PortObjectSpec[] specs)
			throws NotConfigurableException {
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
		final NodeSettingsWO subSettings = settings;
//	            settings.addNodeSettings(m_configName);
//		m_clf.saveSettingsTo(subSettings);
		m_filterType.saveSettingsTo(subSettings);
		m_filterThreshold.saveSettingsTo(subSettings);
//		m_algorithm.saveSettingsTo(subSettings);
		
		// advanced settings
		m_lpObj.saveSettingsTo(subSettings);
		m_lpVar.saveSettingsTo(subSettings);
		m_ds.saveSettingsTo(subSettings);
	}

	@Override
	protected void validateSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadSettingsForModel(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		final NodeSettingsRO subSettings = settings;
//                settings.getNodeSettings(m_configName);
//		
		
//		m_clf.loadSettingsFrom(subSettings);
		m_filterType.loadSettingsFrom(subSettings);
		m_filterThreshold.loadSettingsFrom(subSettings);
//		m_algorithm.loadSettingsFrom(subSettings);
		
		// advanced settings
		m_lpObj.loadSettingsFrom(subSettings);
		m_lpVar.loadSettingsFrom(subSettings);
		m_ds.loadSettingsFrom(subSettings);
		
	}

	@Override
	protected void saveSettingsForModel(NodeSettingsWO settings) {
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
