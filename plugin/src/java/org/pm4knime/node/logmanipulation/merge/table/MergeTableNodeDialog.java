package org.pm4knime.node.logmanipulation.merge.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerModel;
import org.pm4knime.node.logmanipulation.merge.table.MergeTableNodeModel;
import org.knime.core.data.DataTableSpec;
import org.pm4knime.util.ui.DialogComponentAttributesFilter;

/**
 * <code>NodeDialog</code> for the "MergeLog" node.
 * It includes dialog components for parameters: 
 *   <1> if to merge trace with same identifier
 *   	<2> only if the merge is chosen, the next chioces are enabled
 *   		if to merge the event with same label together?? 
 *   			<3> enabled due to the last merge, which event attributes are used?
 *   			choose the first one, or the second one?? How flexible are attributes?? 
 *   Another strategy can be:: 
 *   	-- it keeps some parts out!! If seeing the same identifier, then not use them!! 
 *   	But the different ones are merged together. Even, 
 *   	-- they only merge the traces with the same identifiers. The others are not used
 *   
 *   It might be called event log intersection, but not used so often. 
 *   
 *  treat the trace: 
 *     -- separate, 
 *        trace and event attributes no effect 
 *     -- ignore the repetive ones from the first or second one,
 *     	  trace and event attributes no effect
 *     -- merge the ones with same identifier but with conditions on the trace and event attributes:: 
 *        how to choose the trace and event attributes later?? 
 *        for the same trace attributes, we can also do the three choices but for all attributes!! 
 *        -- if we want to select the attributes for generated trace, we can't do it!! 
 *        But I'd like to say, it is better to provide the attributes list for them to choose 
 *        in the new generated event log, and also the event attributes there!! 
 *        We don't want the duplicated situations!! 
 *        
 * 
 * which event attributes you want to keep in the new merged data?? 
 * One is the trace attributes to choose, 
 * one is the event attributes to choose for the same trace 
 *  
 * It is a fancy feature to add some statistic information before merge?? Like 
 *  -- how many traces with the same identifier?? 
 *  -- how many event attributes are the same?
 *  We can always give the statistical information before merging. With data aware, but also the model information from it..
 *  Optional Step 1: show stat info: traces with same identifier, same trace attributes, same event attributes
 *  
 *  Step 1: create tables to choose trace attributes 
 *  Step 2: accept the parameters. If we choose the complex way, we'd better set a parameter for this. Else,
 *  a simple way is OK.. 
 *  
 *  Step 3 : deal with the action changes in the model.
 *  
 *  Modified: 2020/02/20. Simplified the codes by using information provided by PortObjectSpec
 * @author Kefang Ding
 */
public class MergeTableNodeDialog extends DefaultNodeSettingsPane {
	
	protected MergeTableNodeModel node;
	
	
	SettingsModelString m_strategy;
	
	SettingsModelString[] m_traceIDs;
	SettingsModelString[] m_eventIDs;
	SettingsModelFilterString m_traceAttrSet , m_eventAttrSet;
	DialogComponentAttributesFilter m_traceAttrFilterComp, m_eventAttrFilterComp;
	protected SettingsModelString t_classifier_0;
	protected SettingsModelString t_classifier_1;
	protected DialogComponentStringSelection trace_classifierComp_0;	
	protected DialogComponentStringSelection trace_classifierComp_1;
	
	private DialogComponentStringSelection[] tIDComps, eIDComps;
    /**
     * New pane for configuring the MergeLog node.
     */
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
    	
    	// add additional equation to choose the comparison
    	
    	m_traceIDs = new SettingsModelString[MergeTableNodeModel.CGF_INPUTS_NUM];
    	m_eventIDs = new SettingsModelString[MergeTableNodeModel.CGF_INPUTS_NUM];
    	tIDComps = new DialogComponentStringSelection[MergeTableNodeModel.CGF_INPUTS_NUM];
    	eIDComps = new DialogComponentStringSelection[MergeTableNodeModel.CGF_INPUTS_NUM];
    	
    	// create the trace ID mapping for the event logs
    	this.setHorizontalPlacement(true);
    	for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    		
    		m_traceIDs[i] = new SettingsModelString(MergeTableNodeModel.CFG_KEY_CASE_ID[i], "");
    		// create comp for this traeID to make them equal
    		int j = i+1;
    		tIDComps[i] = new DialogComponentStringSelection(m_traceIDs[i],"CaseID for log " + j, new String[]{""});
    		addDialogComponent(tIDComps[i]);
    		
    	}
    	this.setHorizontalPlacement(false);
    	// create the event ID mapping for the event logs
    	
    	// create the group to choose trace attributes. Do we allow attributes with same names in trace?? 
    	// If it is not allowed, we check it at the configuration steps..
    	this.createNewGroup("Trace Attribute Set");
    	m_traceAttrSet = new SettingsModelFilterString(MergeTableNodeModel.CFG_KEY_TRACE_ATTRSET, new String[]{}, new String[]{}, true );
    	m_traceAttrFilterComp = new DialogComponentAttributesFilter (m_traceAttrSet,  true);
    	addDialogComponent(m_traceAttrFilterComp);
    	this.closeCurrentGroup();
    	
    	this.setHorizontalPlacement(true);
    	for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    		m_eventIDs[i] = new SettingsModelString(MergeTableNodeModel.CFG_KEY_EVENT_ID[i], "");	
    		int j = i+1;
    		eIDComps[i] = new DialogComponentStringSelection(m_eventIDs[i],"EventID for log " + j, new String[]{""});
    		addDialogComponent(eIDComps[i]);
    	}
    	
    	this.setHorizontalPlacement(false);
    	
    	this.createNewGroup("Event Attribute Set");
    	m_eventAttrSet = new SettingsModelFilterString(MergeTableNodeModel.CFG_KEY_EVENT_ATTRSET, new String[]{}, new String[]{}, true );
    	m_eventAttrFilterComp = new DialogComponentAttributesFilter (m_eventAttrSet, true);
    	addDialogComponent(m_eventAttrFilterComp);
    	this.closeCurrentGroup();
    	
    	checkEnableStates();
    	
    	// we always ignore the repeated caseId from second log
    	m_strategy.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				// TODO 
				checkEnableStates();
				// other stuff we need to do here is to get traceAttrset and eventAttr 
				
			}
    		
    	});
    	
    	
    }
    
    // this 
    private void checkEnableStates() {
    	
    	if(m_strategy.getStringValue().equals(MergeTableNodeModel.CFG_TRACE_STRATEGY[0])) {
			for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    			m_traceIDs[i].setEnabled(false);
    		}
			m_traceAttrSet.setEnabled(false);
			
			for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    			m_eventIDs[i].setEnabled(false);
    		}
			m_eventAttrSet.setEnabled(false);
		}else if(m_strategy.getStringValue().equals(MergeTableNodeModel.CFG_TRACE_STRATEGY[1])) {
			for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    			m_traceIDs[i].setEnabled(true);
    		}
			
			m_traceAttrSet.setEnabled(false);
			for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    			m_eventIDs[i].setEnabled(false);
    		}
			m_eventAttrSet.setEnabled(false);
		} else  if(m_strategy.getStringValue().equals(MergeTableNodeModel.CFG_TRACE_STRATEGY[2])) {
			for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    			m_traceIDs[i].setEnabled(true);
    		}
			
			m_traceAttrSet.setEnabled(true);
			
			for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    			m_eventIDs[i].setEnabled(false);
    		}
			m_eventAttrSet.setEnabled(false);
			// m_compositePanel.repaint();
		}else if(m_strategy.getStringValue().equals(MergeTableNodeModel.CFG_TRACE_STRATEGY[3])) {
			// internal event merge
			for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    			m_traceIDs[i].setEnabled(true);
    		}
			m_traceAttrSet.setEnabled(true);
			
			for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
    			m_eventIDs[i].setEnabled(true);
    		}
			m_eventAttrSet.setEnabled(true);
		}
    	
    }
    
    
	@Override
	public void saveAdditionalSettingsTo(final NodeSettingsWO settings) {
		
		this.node.t_classifier_0 = this.t_classifier_0.getStringValue();
		this.node.t_classifier_1 = this.t_classifier_1.getStringValue();
		m_traceAttrSet.saveSettingsTo(settings);
		m_eventAttrSet.saveSettingsTo(settings);
	}
	
	
	@Override
	public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
			 final PortObjectSpec[] specs) throws NotConfigurableException {
		// if we don't have such values, we need to search for event log. But if we have it. No need?
		
		if(!(specs[0] instanceof DataTableSpec))
    		throw new NotConfigurableException("the spec does not have the right type");
		DataTableSpec logSpec0 = (DataTableSpec) specs[0];
    	if(!(specs[1] instanceof DataTableSpec))
    		throw new NotConfigurableException("the spec does not have the right type");
    	DataTableSpec logSpec1 = (DataTableSpec) specs[1];
    	
    	DataTableSpec[] logSpecs = new DataTableSpec[MergeTableNodeModel.CGF_INPUTS_NUM];
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
    	
    	try {
	    	List<String>[] specTAArrays =  new List[MergeTableNodeModel.CGF_INPUTS_NUM];
	    	List<String>[] specEAArrays =  new List[MergeTableNodeModel.CGF_INPUTS_NUM];
	    	
	    	List<String> specTraceColumns = new ArrayList(); 
	    	List<String> specEventColumns = new ArrayList();
	    	
	    	for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
	    		List<String> traceColumns = new ArrayList(); 
	    		List<String> eventColumns = new ArrayList(); 
		    	for(String key: logSpecs[i].getColumnNames()) {
		    		traceColumns.add(MergeTableNodeModel.CFG_ATTRIBUTE_PREFIX + i + key );
		    	}
		    	
		    	for(String key: logSpecs[i].getColumnNames()) {
		    		eventColumns.add(MergeTableNodeModel.CFG_ATTRIBUTE_PREFIX + i + key );
		    	}
		    	specTAArrays[i] = traceColumns;
		    	specEAArrays[i] = eventColumns;
		    	
		    	specTraceColumns.addAll(traceColumns);
		    	specEventColumns.addAll(eventColumns);
	    	}
	    	
	    	// assign the traceIDs and event IDs there..Where can they have such values?
	    	// it is just the first one in the list. So, totally fine for it..
	    	for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
	    		tIDComps[i].replaceListItems(specTAArrays[i], specTAArrays[i].get(0));
	    		eIDComps[i].replaceListItems(specEAArrays[i], specEAArrays[i].get(0));

	    		if(!specTAArrays[i].contains(m_traceIDs[i].getStringValue())) {
	    			// we assign it the new value from spec, the first one
	    			m_traceIDs[i].setStringValue(specTAArrays[i].get(0));
	    		}
	    		
	    		if(!specEAArrays[i].contains(m_eventIDs[i].getStringValue())) {
	    			// we assign it the new value from spec, the first one
	    			m_eventIDs[i].setStringValue(specEAArrays[i].get(0));
	    		}
	    		
	    	}
			// Here, the traceSet can include attributes from different specs. So how to decide their values here?
	    	// We need to assign them with prefix for each log.
	    	// put all the attributes available to use. Then the users choose which to use.
	    	// if they choose all the attributes, all attributes will be used too.  Attributes will have suffix there
	    	
	    	List<String> configTraceAttrColumns = new ArrayList<String>(m_traceAttrSet.getIncludeList());
	    	configTraceAttrColumns.addAll(m_traceAttrSet.getExcludeList());
	    	List<String> configEventAttrColumns = new ArrayList<String>(m_eventAttrSet.getIncludeList());
	    	configEventAttrColumns.addAll(m_eventAttrSet.getExcludeList());
	    	
	    	
	    	// With Set, if they have the same names, only one is added. So change the strategy
	    	// add some prefix to the generation of the values
	    	
	    	if(!specTraceColumns.containsAll(configTraceAttrColumns) 
	    		|| !configTraceAttrColumns.containsAll(specTraceColumns)) {
	    		// here how to know if it has some changes, we have the excluded list there
	    		m_traceAttrSet.setIncludeList(specTraceColumns);
		    	m_traceAttrSet.setExcludeList(new String[0]);
		    	
	    	}
	    	
	    	
	    	if(!specEventColumns.containsAll(configEventAttrColumns) 
		    		|| !configEventAttrColumns.containsAll(specEventColumns)) {
		    	m_eventAttrSet.setIncludeList(specEventColumns);
		    	m_eventAttrSet.setExcludeList(new String[0]);
	    	}
			
	    	
	    	// reload the configuration.. 
	    	for(int i=0; i< MergeTableNodeModel.CGF_INPUTS_NUM; i++) {
	    		m_traceIDs[i].loadSettingsFrom(settings);
	    		m_eventIDs[i].loadSettingsFrom(settings);
	    		
	    	}
	    	
	    	
    	}catch(InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

