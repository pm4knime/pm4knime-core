package org.pm4knime.node.discovery.defaultminer;

import java.util.Arrays;

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
	
	public static final String DEFAULT_TRACE_CLASS = "case:concept:name";
	public static final String DEFAULT_EVENT_CLASS = "concept:name";
	protected DefaultTableMinerModel node;
	protected SettingsModelString e_classifier ;
	protected DialogComponentStringSelection event_classifierComp ;
	protected SettingsModelString t_classifier ;
	protected DialogComponentStringSelection trace_classifierComp;	
	
	
	public DefaultTableMinerDialog(DefaultTableMinerModel n) {
		node = n;
		t_classifier =  new SettingsModelString(DefaultTableMinerModel.KEY_TRACE_CLASSIFIER, "");
		e_classifier =  new SettingsModelString(DefaultTableMinerModel.KEY_EVENT_CLASSIFIER, "");
		String[] classifierSet = new String[] {""};
		
		trace_classifierComp = new DialogComponentStringSelection(t_classifier,
				"Trace Classifier", classifierSet);
	    addDialogComponent(trace_classifierComp);
		
		event_classifierComp = new DialogComponentStringSelection(e_classifier,
				"Event Classifier", classifierSet);
	    addDialogComponent(event_classifierComp);
	    
	    init();
	}
	
	
	public abstract void init() ;
	
	
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
 
    	DataTableSpec logSpec = (DataTableSpec) specs[0];
		String[] currentClasses = logSpec.getColumnNames();
		if (currentClasses.length == 0) {
			throw new NotConfigurableException("Please make sure the connected table is in excution state");
		} else {
			try {
				this.node.loadValidatedSettingsFrom(settings);
	        } catch (InvalidSettingsException e) {
	        	System.out.println("Exception:" + e.toString());
	        }
			String tClassifier = getDefaultTraceClassifier(currentClasses, this.node.t_classifier);
			String eClassifier = getDefaultEventClassifier(currentClasses, this.node.e_classifier);
			this.node.setTraceClassifier(tClassifier);
			this.node.setEventClassifier(eClassifier);
			trace_classifierComp.replaceListItems(Arrays.asList(currentClasses), tClassifier);
			event_classifierComp.replaceListItems(Arrays.asList(currentClasses), eClassifier);
		}	
    }

    
	private String getDefaultTraceClassifier(String[] currentClasses, String oldValue) {		
		String res = currentClasses[0];
		for (String s: currentClasses) {
			if (s.equals(oldValue)) {
				return oldValue;
			}
			if (s.equals(DEFAULT_TRACE_CLASS)) {
				res = DEFAULT_TRACE_CLASS;
			}
		}
		return res;
	}
    
	
    private String getDefaultEventClassifier(String[] currentClasses, String oldValue) {		
    	String res;
    	if (currentClasses.length > 1) {
			res = currentClasses[1];
		} else {
			res = currentClasses[0];
		}
    	for (String s: currentClasses) {
    		if (s.equals(oldValue)) {
				return oldValue;
			}
			if (s.equals(DEFAULT_EVENT_CLASS)) {
				res = DEFAULT_EVENT_CLASS;
			}
		}   	
    	return res;
	}


	@Override
	 public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
	            throws InvalidSettingsException {
	    this.node.t_classifier = this.t_classifier.getStringValue();
		this.node.e_classifier = this.e_classifier.getStringValue();
		this.node.saveSettingsTo(settings);	    
	}

}
