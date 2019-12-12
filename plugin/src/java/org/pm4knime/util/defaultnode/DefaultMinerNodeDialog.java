package org.pm4knime.util.defaultnode;

import java.util.Map;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
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
        // classifierList, we need to replace it with the new one!!
		classifierComp = new DialogComponentStringSelection(m_classifier, "Event Classifier", new String[] {""});
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
     */
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
    	System.out.println("Begin : event classifier value is: " + m_classifier.getStringValue());
    	
    	try {
			m_classifier.loadSettingsFrom(settings);
			if(m_classifier.getStringValue().isEmpty()) {
	    		System.out.println("After loading : event classifier value is: " + m_classifier.getStringValue());
	    		if(!specs[0].getClass().equals(XLogPortObjectSpec.class)) 
	        		throw new NotConfigurableException("Input is not a valid Event Log!");
	        	
	        	// get the spec 
	        	XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
	        	// the key is the name for classifier with prefix, the value is the class name for it
	        	Map<String, String>  clfMap = logSpec.getClassifiersMap();
	        	// add attributes as available classifier
	        	clfMap.putAll(logSpec.getGEventAttrMap());
	        	// set the default classifier as concept:name by checking the key 
	        	String defaultClassifier = clfMap.keySet().iterator().next();
	        	
	        	for(String key : clfMap.keySet()) {
	        		if(key.contains(XConceptExtension.KEY_NAME)) {
	        			// set it as the default classifier 
	        			m_classifier.setStringValue(key);
	        			defaultClassifier = key;
	        		}
	        		
	        	}
	        	
	        	classifierComp.replaceListItems(clfMap.keySet(), defaultClassifier);
	    	}
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
}
