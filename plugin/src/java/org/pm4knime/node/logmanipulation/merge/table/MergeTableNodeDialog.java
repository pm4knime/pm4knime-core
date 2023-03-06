package org.pm4knime.node.logmanipulation.merge.table;

import java.util.Arrays;
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
import org.knime.core.data.DataTableSpec;


public class MergeTableNodeDialog extends DefaultNodeSettingsPane {
	
	protected MergeTableNodeModel node;
	
	
	SettingsModelString m_strategy;
	protected SettingsModelString t_classifier_0;
	protected SettingsModelString t_classifier_1;
	protected DialogComponentStringSelection trace_classifierComp_0;	
	protected DialogComponentStringSelection trace_classifierComp_1;
	
    protected MergeTableNodeDialog(MergeTableNodeModel n) {
    	
        
    	// set parameters
    	this.node = n;
    	t_classifier_0 =  new SettingsModelString(DefaultTableMinerModel.KEY_TRACE_CLASSIFIER+"0", "");
    	t_classifier_1 =  new SettingsModelString(DefaultTableMinerModel.KEY_TRACE_CLASSIFIER+"1", "");
    	String[] classifierSet = new String[] {""};
    	
    	trace_classifierComp_0 = new DialogComponentStringSelection(t_classifier_0,
    			"Trace Classifier First Table", classifierSet);
        addDialogComponent(trace_classifierComp_0);
        trace_classifierComp_1 = new DialogComponentStringSelection(t_classifier_1,
    			"Trace Classifier Second Table", classifierSet);
        addDialogComponent(trace_classifierComp_1);
        
    	m_strategy = new SettingsModelString(MergeTableNodeModel.CFG_KEY_TRACE_STRATEGY, MergeTableNodeModel.CFG_TRACE_STRATEGY[0]);
    	DialogComponentStringSelection strategyComp = new DialogComponentStringSelection(m_strategy,"Merging Strategy" ,MergeTableNodeModel.CFG_TRACE_STRATEGY);
    	addDialogComponent(strategyComp);	
    	
    }
    
   
	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings) {	
		this.node.t_classifier_0 = this.t_classifier_0.getStringValue();
		this.node.t_classifier_1 = this.t_classifier_1.getStringValue();
	}
	
	
	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
			 final PortObjectSpec[] specs) throws NotConfigurableException {
		
		if(!(specs[0] instanceof DataTableSpec))
    		throw new NotConfigurableException("the spec does not have the right type");
		DataTableSpec logSpec0 = (DataTableSpec) specs[0];
    	if(!(specs[1] instanceof DataTableSpec))
    		throw new NotConfigurableException("the spec does not have the right type");
    	DataTableSpec logSpec1 = (DataTableSpec) specs[1];
    	
    	DataTableSpec[] logSpecs = new DataTableSpec[2];
        logSpecs[0] = logSpec0;
    	logSpecs[1] = logSpec1;
    	
    	String[] currentClasses = logSpec0.getColumnNames();
		if (currentClasses.length == 0) {
			throw new NotConfigurableException("Please make sure the connected table is in excution state");
		} else {
			try {
				this.node.loadValidatedSettingsFrom(settings);
	        } catch (InvalidSettingsException e) {
	        	System.out.println("Exception:" + e.toString());
	        }
			String tClassifier_0 = DefaultTableMinerDialog.getDefaultTraceClassifier(currentClasses, this.node.t_classifier_0);
			this.node.t_classifier_0 = tClassifier_0;
			trace_classifierComp_0.replaceListItems(Arrays.asList(currentClasses), tClassifier_0);
		}	
		
		currentClasses = logSpec1.getColumnNames();
		if (currentClasses.length == 0) {
			throw new NotConfigurableException("Please make sure the connected table is in excution state");
		} else {
			try {
				this.node.loadValidatedSettingsFrom(settings);
	        } catch (InvalidSettingsException e) {
	        	System.out.println("Exception:" + e.toString());
	        }
			String tClassifier_1 = DefaultTableMinerDialog.getDefaultTraceClassifier(currentClasses, this.node.t_classifier_1);
			this.node.t_classifier_1 = tClassifier_1;
			trace_classifierComp_1.replaceListItems(Arrays.asList(currentClasses), tClassifier_1);
		}	
    	
	}
}

