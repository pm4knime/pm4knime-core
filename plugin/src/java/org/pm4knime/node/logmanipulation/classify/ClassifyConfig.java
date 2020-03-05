package org.pm4knime.node.logmanipulation.classify;

import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.config.ConfigRO;
import org.knime.core.node.config.ConfigWO;

/**
 * this node is used to store the parameters for event log classification. 
 * It will includes the following values,
 *  -- label name
 *  -- label values set
 *  -- label values percentages settings
 *  -- optional:: 
 *     list of special label values set for the event class of one trace variant. 
 *     Here, we can use map to store the values.
 *     
 *  
 * @author kefang-pads
 *
 */
public class ClassifyConfig {
//	private static final NodeLogger LOGGER = NodeLogger.getLogger(ClassifyConfig.class);
	public static String CFG_LABEL_NAME = "Label name";
	public static String CFG_LABEL_VALUES = "Label values";
	public static String CFG_LABEL_VALUE_PERCENTAGES = "Label value percentages";
	
	private final String m_configName;
	
	private String m_labelName;
	private Map<String, Double> vMap;

	
	public ClassifyConfig(final String configName) {
		m_configName = configName;
		m_labelName = "";
		vMap = new HashMap();
		// here is no labelName yet,  but we need to wait for this
	}
	
	public void saveSettingsTo(final NodeSettingsWO settings) {
        final ConfigWO config = settings.addConfig(m_configName);
        
        //TODO:  here comes the serialization part.
        config.addString(CFG_LABEL_NAME, m_labelName);
        config.addStringArray(CFG_LABEL_VALUES, vMap.keySet().toArray(new String[0]));
        // from java 1.8, this is available.
        double[] pValues = vMap.values().stream().mapToDouble(Double::doubleValue).toArray();
        config.addDoubleArray(CFG_LABEL_VALUE_PERCENTAGES,  pValues);
        
    }
	
	
	public void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        final ConfigRO config = settings.getConfig(m_configName);
        m_labelName = config.getString(CFG_LABEL_NAME);
        vMap.clear();
        String[] tValues = config.getStringArray(CFG_LABEL_VALUES);
        double[] tPercents = config.getDoubleArray(CFG_LABEL_VALUE_PERCENTAGES);
        for(int i = 0; i< tValues.length; i++) {
        	vMap.put(tValues[i], tPercents[i]);
        }
        
	}

	public Map<String, Double> getValueMap() {
		return vMap;
	}
	public String getLabelName() {
		// TODO Auto-generated method stub
		return m_labelName;
	}

	public void setLabelName(String m_labelName) {
		this.m_labelName = m_labelName;
	}

	
	public void addData(String value, double percent) {
		// How to make sure that they have the same values there?? Not really, 
		// so better to change the structure as the map!!!
		vMap.put(value, percent);
	}
	
}
