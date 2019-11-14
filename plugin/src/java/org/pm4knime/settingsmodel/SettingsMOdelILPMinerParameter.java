package org.pm4knime.settingsmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
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
import org.processmining.hybridilpminer.parameters.LPFilterType;

public class SettingsModelILPMinerParameter extends SettingsModel
	implements SettingsModelFlowVariableCompatible {

	public static final String CFG_KEY_CLASSIFIER = "Event Classifier";
	
	public static final String CFG_KEY_FILTER_TYPE = "Filter Type";
	
	public static final String[] CFG_FILTER_TYPES = {LPFilterType.NONE.name(), 
			LPFilterType.SEQUENCE_ENCODING.name(), LPFilterType.SLACK_VAR.name()};
	
	public static final String CFG_KEY_FILTER_THRESHOLD = "Noise Threshold";
	public static final String CFG_KEY_MINER_ALGORITHM = "Miner Algorithm";
	
	
	private String m_configName;
	
	SettingsModelString m_clf, m_filterType, m_algorithm;
	SettingsModelDoubleBounded m_filterThreshold;
	
	public SettingsModelILPMinerParameter(String configName) {
		if ((configName == null) || "".equals(configName)) {
            throw new IllegalArgumentException("The configName must be a "
                    + "non-empty string");
        }
		m_configName = configName;
		
		m_clf = new SettingsModelString(CFG_KEY_CLASSIFIER, "");
		m_filterType = new SettingsModelString(CFG_KEY_FILTER_TYPE, "");
		m_filterThreshold = new SettingsModelDoubleBounded(CFG_KEY_FILTER_THRESHOLD, 0.8, 0, 1.0);
		// add several choices to have the miner
		m_algorithm = new SettingsModelString(CFG_KEY_MINER_ALGORITHM, "");
		
	}
	
	public SettingsModelString getMclf() {
		return m_clf;
	}

	public void setMclf(SettingsModelString m_clf) {
		this.m_clf = m_clf;
	}

	public SettingsModelString getMfilterType() {
		return m_filterType;
	}

	public void setMfilterType(SettingsModelString m_filterType) {
		this.m_filterType = m_filterType;
	}

	public SettingsModelString getMalgorithm() {
		return m_algorithm;
	}

	public void setMalgorithm(SettingsModelString m_algorithm) {
		this.m_algorithm = m_algorithm;
	}

	public SettingsModelDoubleBounded getMfilterThreshold() {
		return m_filterThreshold;
	}

	public void setMfilterThreshold(SettingsModelDoubleBounded m_filterThreshold) {
		this.m_filterThreshold = m_filterThreshold;
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
	protected SettingsModelILPMinerParameter createClone() {
		// TODO Auto-generated method stub
		SettingsModelILPMinerParameter clone =  new SettingsModelILPMinerParameter(m_configName);
		clone.setMclf(m_clf);
		clone.setMfilterType(m_filterType);
		clone.setMfilterThreshold(m_filterThreshold);
		clone.setMalgorithm(m_algorithm);
		
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

	public static Collection<XEventClassifier> setDefaultClassifier(){
    	Collection<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();
		classifiers.add(new XEventNameClassifier());
		return classifiers;
    }
    
    public static List<String> getClassifierNames(Collection<XEventClassifier> classList){
    	List<String> nameList = new ArrayList();
		for(XEventClassifier clf: classList) {
			nameList.add(clf.name());
		}
		return nameList;
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
		final NodeSettingsWO subSettings =
	            settings.addNodeSettings(m_configName);
		m_clf.saveSettingsTo(subSettings);
		m_filterType.saveSettingsTo(subSettings);
		m_filterThreshold.saveSettingsTo(subSettings);
		m_algorithm.saveSettingsTo(subSettings);
		
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
		
		m_clf.loadSettingsFrom(subSettings);
		m_filterType.loadSettingsFrom(subSettings);
		m_filterThreshold.loadSettingsFrom(subSettings);
		m_algorithm.loadSettingsFrom(subSettings);
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
