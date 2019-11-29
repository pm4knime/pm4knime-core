package org.pm4knime.node.logmanipulation.classify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
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
	private List<String> m_valueList;
	private List<Double> m_percentageList;
	
	// optional later
	
	public ClassifyConfig(final String configName) {
		m_configName = configName;
		
		// here is no labelName yet,  but we need to wait for this
		m_valueList = new ArrayList<String>();
		m_percentageList = new ArrayList<Double>();
	}
	
	public void saveSettingsTo(final NodeSettingsWO settings) {
        final ConfigWO config = settings.addConfig(m_configName);
        
        //TODO:  here comes the serialization part.
        config.addString(CFG_LABEL_NAME, m_labelName);
        config.addStringArray(CFG_LABEL_VALUES, m_valueList.toArray(new String[0]));
        // from java 1.8, this is available.
        double[] pValues = m_percentageList.stream().mapToDouble(Double::doubleValue).toArray();
        config.addDoubleArray(CFG_LABEL_VALUE_PERCENTAGES,  pValues);
        
    }
	
	
	public void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
        final ConfigRO config = settings.getConfig(m_configName);
        m_labelName = config.getString(CFG_LABEL_NAME);
        
        String[] tValues = config.getStringArray(CFG_LABEL_VALUES);
        m_valueList.clear();
        m_valueList = Arrays.asList(tValues);
        
        double[] tPercentages = config.getDoubleArray(CFG_LABEL_VALUE_PERCENTAGES);
        m_percentageList.clear();
        // TODO : a bit complex, must over java 1.8 to use it
        m_percentageList = Arrays.stream(tPercentages).boxed().collect(Collectors.toList());
        
	}

	public String getLabelName() {
		// TODO Auto-generated method stub
		return m_labelName;
	}

	public List<String> getValueList() {
		return m_valueList;
	}

	public void setValueList(List<String> m_valueList) {
		this.m_valueList = m_valueList;
	}

	public List<Double> getPercentageList() {
		return m_percentageList;
	}

	public void setPercentageList(List<Double> m_percentageList) {
		this.m_percentageList = m_percentageList;
	}

	public void setLabelName(String m_labelName) {
		this.m_labelName = m_labelName;
	}

	public boolean addValueList(String value) {
		// TODO add one value to the list
		if(m_valueList.contains(value)) {
			System.out.println("Already exist the value");
			return false;
		}else
			m_valueList.add(value);
		return true;
	}

	public void addPercentageList(String value) {
		// TODO Auto-generated method stub
		
	}
	
	
}
