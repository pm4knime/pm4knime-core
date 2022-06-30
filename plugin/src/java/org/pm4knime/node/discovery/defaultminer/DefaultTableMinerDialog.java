package org.pm4knime.node.discovery.defaultminer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;

public abstract class DefaultTableMinerDialog extends DefaultNodeSettingsPane {
	
	//protected SettingsModelStringArray classifierSet ;
	protected SettingsModelString e_classifier ;
	protected DialogComponentStringSelection event_classifierComp ;
	protected SettingsModelString t_classifier ;
	protected DialogComponentStringSelection trace_classifierComp;
	
	public DefaultTableMinerDialog() {
		
		t_classifier =  new SettingsModelString(DefaultTableMinerModel.KEY_TRACE_CLASSIFIER, "");
		e_classifier =  new SettingsModelString(DefaultTableMinerModel.KEY_EVENT_CLASSIFIER, "");
		//classifierSet = new SettingsModelStringArray(DefaultMinerNodeModelBuffTable.KEY_CLASSIFIER_SET, new String[] {""});
		
		trace_classifierComp = new DialogComponentStringSelection(t_classifier,
				"Trace Classifier", new String[] {""});
	    addDialogComponent(trace_classifierComp);
		
		event_classifierComp = new DialogComponentStringSelection(e_classifier,
				"Event Classifier", new String[] {""});
	    addDialogComponent(event_classifierComp);
	    
	    init();
	}
	
	
	public abstract void init() ;
	 /**
     * This method loads possible classifier choices from the XLogPortObjectSpec. 
     * It should be saved as an abstract class, and other class just follows its implementation.
     * here different thing happens if we load the values for the m_classifier.
     * 
     * one task here is to check when to use the old version, when is the new one..
     * The configuration is changed due to :: 
     *  -- first to construct
     *  -- connection changes
     *  
     *  reloading can change the configuration?? 
     *   -- it can't affect the configuration, we use settings value;; 
     *   -- it affect the configuration, but still no need to do this
     *    
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
 
		DataTableSpec logSpec = (DataTableSpec) specs[0];
			List<String> classAsList = Arrays.asList(logSpec.getColumnNames());
			
			if (classAsList.size() == 0) {
				classAsList = new ArrayList<String>();
				classAsList.add("");
			}			
			String tClassifier = getDefaultTraceClassifier(classAsList);
			String eClassifier = getDefaultEventClassifier(classAsList);
			t_classifier.setStringValue(tClassifier);
			e_classifier.setStringValue(eClassifier);
			trace_classifierComp.replaceListItems(classAsList, tClassifier);
			event_classifierComp.replaceListItems(classAsList, eClassifier);
    }
    
    private String getDefaultTraceClassifier(List<String> classAsList) {		
		if (classAsList.contains("case:concept:name")) {
			return "case:concept:name";
		} else {
			return classAsList.get(0);
		}
	}
    
    private String getDefaultEventClassifier(List<String> classAsList) {		
		if (classAsList.contains("concept:name")) {
			return "concept:name";
		} else if (classAsList.size() > 1) {
			return classAsList.get(1);
		} else {
			return classAsList.get(0);
		}
	}


	@Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        assert settings != null;        
    }

}
