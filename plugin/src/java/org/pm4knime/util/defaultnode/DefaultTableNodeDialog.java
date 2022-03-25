package org.pm4knime.util.defaultnode;

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
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.node.logmanipulation.filter.knimetable.FilterByFrequencyTableNodeModel;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;
/**
 * this class is used to provide the common structure for discovery algorithms. 
 * Make sure the first input PortObject is the event log. 
 * The first available benefits is the classifier selection. 
 * -- parameters:  
 *      classifierList 
 *      m_classifier SettingsModelString
 * -- methods:
 *    assign classifierList from the PortObjectSpec 
 *    
 * @author kefang-pads
 *
 */
public abstract class DefaultTableNodeDialog extends DefaultNodeSettingsPane {
	
	protected SettingsModelStringArray columnSet ;
	protected DialogComponentStringSelection columnCompCase,  columnCompTime, columnCompActivity;
	protected SettingsModelString m_variantCase, m_variantTime , m_variantActivity;
	
	public DefaultTableNodeDialog() {
		// choose one possible values from the available lists and assign it as first one
		// make some previous check on the values before saving the value
		//m_classifier =  new SettingsModelString(DefaultMinerNodeModel.CFG_KEY_CLASSIFIER, "");
		//classifierSet = new SettingsModelStringArray(DefaultMinerNodeModel.CFG_KEY_CLASSIFIER_SET, new String[] {""});
		
		//classifierComp = new DialogComponentStringSelection(m_classifier,
		//		"Event Classifier", classifierSet.getStringArrayValue());
        //addDialogComponent(classifierComp);
		
		columnSet = new SettingsModelStringArray(DefaultTableNodeModel.CFG_KEY_COLUMN_SET, new String[] {""});
        
      	createNewGroup("Choose the corresponding columns");
    	//String[] variantListCase =  FilterByFrequencyTableNodeModel.variantListCase;
        m_variantCase = new SettingsModelString(DefaultTableNodeModel.CFG_KEY_COLUMN_CASE, "");
        columnCompCase = new DialogComponentStringSelection(m_variantCase,"Case ID", columnSet.getStringArrayValue());
        addDialogComponent(columnCompCase);
        
       // String[] variantListTime =  FilterByFrequencyTableNodeModel.variantListTime;
        m_variantTime = new SettingsModelString(DefaultTableNodeModel.CFG_KEY_COLUMN_TIME, "");
        columnCompTime = new DialogComponentStringSelection(m_variantTime,"Timestamp", columnSet.getStringArrayValue());
        addDialogComponent(columnCompTime);
        
       // String[] variantListActivity =  FilterByFrequencyTableNodeModel.variantListActivity;
        m_variantActivity = new SettingsModelString(DefaultTableNodeModel.CFG_KEY_COLUMN_ACTIVITY, "");
        columnCompActivity = new DialogComponentStringSelection(m_variantActivity,"Activity", columnSet.getStringArrayValue());
        addDialogComponent(columnCompActivity);
        
        closeCurrentGroup();
        
        init();
	}
	/**
	 * this abstract method is used to initialize other parameters except the classifiers 
	 */
	
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
    	
    	
 
    	try {
    		
    		columnSet.loadSettingsFrom(settings);
    		List<String> configColumnSet = Arrays.asList(columnSet.getStringArrayValue());
    		
    		DataTableSpec tableSpec = (DataTableSpec) specs[0];
			List<String> specColumnSet = new ArrayList<String>(Arrays.asList(tableSpec.getColumnNames()));

			if(! configColumnSet.containsAll(specColumnSet) 
		    		 ||	!specColumnSet.containsAll(configColumnSet)){
				
				columnSet.setStringArrayValue(specColumnSet.toArray(new String[0]));
			}
	

			columnCompCase.replaceListItems(specColumnSet, specColumnSet.iterator().next());
			columnCompTime.replaceListItems(specColumnSet, specColumnSet.iterator().next());
			columnCompActivity.replaceListItems(specColumnSet, specColumnSet.iterator().next());
			
			m_variantCase.loadSettingsFrom(settings);
			m_variantTime.loadSettingsFrom(settings);
			m_variantActivity.loadSettingsFrom(settings);
			
		} catch (NullPointerException | InvalidSettingsException e) {
			// TODO Auto-generated catch block
			throw new NotConfigurableException("Please make sure the connected event log is in excution state");
			
		}
		
  	
    }
    
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        assert settings != null;
        
        columnSet.saveSettingsTo(settings);
    }
}
