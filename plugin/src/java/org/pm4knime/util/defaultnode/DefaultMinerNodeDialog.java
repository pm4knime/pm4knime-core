package org.pm4knime.util.defaultnode;

import java.util.Arrays;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
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
	
	SettingsModelString m_classifier ;
	DialogComponentStringSelection classifierComp ;
	
	public DefaultMinerNodeDialog() {
		// choose one possible values from the available lists and assign it as first one
		// make some previous check on the values before saving the value
		m_classifier =  new SettingsModelString(DefaultMinerNodeModel.CFG_KEY_CLASSIFIER, "");
		
		classifierComp = new DialogComponentStringSelection(m_classifier,
				"Event Classifier", new String[]{""});
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
     *  If 
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
 
    	try {
			m_classifier.loadSettingsFrom(settings);
			String selectedItem = m_classifier.getStringValue();
			
			XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
	    	
			// we need to check the old model here to make sure they are the same one
			// use one test model and check the working step.. 
			if(!logSpec.getClassifiersMap().keySet().contains(m_classifier.getStringValue())) {
				selectedItem = logSpec.getClassifiersMap().keySet().iterator().next();
				m_classifier.setStringValue(selectedItem);
			}
			
			classifierComp.replaceListItems(logSpec.getClassifiersMap().keySet(), selectedItem);
		} catch (InvalidSettingsException | NullPointerException e ) {
			// TODO Auto-generated catch block
			throw new NotConfigurableException("Please make sure the connected event log in eexcution state");
			
		}
  	
    }
}
