package org.pm4knime.node.conversion.table2pn;

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
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerModel;

public class Table2PetriNetConverterNodeDialog extends DefaultNodeSettingsPane {

	public static final String DEFAULT_TRACE_CLASS = "#Trace Attribute#concept:name"; //"case:concept:name"
	public static final String DEFAULT_EVENT_CLASS = "#Event Attribute#concept:name"; //"concept:name"
	
	protected Table2PetriNetConverterNodeModel node;
	protected SettingsModelString e_classifier ;
	protected DialogComponentStringSelection event_classifierComp ;
	protected SettingsModelString t_classifier ;
	protected DialogComponentStringSelection trace_classifierComp;	
	
	
	public Table2PetriNetConverterNodeDialog(Table2PetriNetConverterNodeModel n) {
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
	
	
	public void init() {
		
	};
	
	
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
		}	
    }

    
	public static String getDefaultTraceClassifier(String[] currentClasses, String oldValue) {		
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
    
	
	public static String getDefaultEventClassifier(String[] currentClasses, String oldValue) {		
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
		
	
	}
	
	

}
