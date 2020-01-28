package org.pm4knime.util.defaultnode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.portobject.XLogPortObjectSpec;
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
public abstract class DefaultMinerNodeDialog extends DefaultNodeSettingsPane {
	
	protected SettingsModelString m_classifier ;
	protected SettingsModelStringArray classifierSet ;
	protected DialogComponentStringSelection classifierComp ;
	
	public DefaultMinerNodeDialog() {
		// choose one possible values from the available lists and assign it as first one
		// make some previous check on the values before saving the value
		m_classifier =  new SettingsModelString(DefaultMinerNodeModel.CFG_KEY_CLASSIFIER, "");
		classifierSet = new SettingsModelStringArray(DefaultMinerNodeModel.CFG_KEY_CLASSIFIER_SET, new String[] {""});
		
		classifierComp = new DialogComponentStringSelection(m_classifier,
				"Event Classifier", classifierSet.getStringArrayValue());
        addDialogComponent(classifierComp);
        
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
    		
    		classifierSet.loadSettingsFrom(settings);
    		List<String> configClassifierSet = Arrays.asList(classifierSet.getStringArrayValue());
    		
    		XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
			List<String> specClassifierSet = new ArrayList<String>(logSpec.getClassifiersMap().keySet());
			
			
			if(! configClassifierSet.containsAll(specClassifierSet) 
		    		 ||	!specClassifierSet.containsAll(configClassifierSet)){
				classifierComp.replaceListItems(specClassifierSet, specClassifierSet.get(0));
				classifierSet.setStringArrayValue(specClassifierSet.toArray(new String[0]));
			}
			
			m_classifier.loadSettingsFrom(settings);
			
		} catch (InvalidSettingsException | NullPointerException e ) {
			// TODO Auto-generated catch block
			throw new NotConfigurableException("Please make sure the connected event log in eexcution state");
			
		}
  	
    }
    
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
        assert settings != null;
        
        classifierSet.saveSettingsTo(settings);
    }
}
