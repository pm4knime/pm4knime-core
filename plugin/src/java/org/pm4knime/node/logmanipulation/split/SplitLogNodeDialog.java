package org.pm4knime.node.logmanipulation.split;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObject;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.collection.AlphanumComparator;
import org.processmining.incorporatenegativeinformation.dialogs.ui.AttributeLogFilter_UI;
import org.processmining.incorporatenegativeinformation.models.AttributeLogFilter;

/**
 * <code>NodeDialog</code> for the "SplitLog" Node.
 * This node split one event log according to chosen attribute value, or group them together according to attribute value
 * This is used to check thec combination also from the existing panel, to save the time
 * 
 * @author Kefang Ding
 */
public class SplitLogNodeDialog extends DataAwareNodeDialogPane {

	private static final NodeLogger logger =
			NodeLogger.getLogger(SplitLogNodeDialog.class);
	
	private String m_defaultTabTitle = "Customized Options";
	
	
	SettingsModelString m_filterOn;
	SettingsModelString m_attributeKey;
	SettingsModelStringArray m_attributeValue;
	XLog log;
	AttributeLogFilter filter;
	AttributeLogFilter_UI attributePanel;
	// public static TreeSet<String> m_attributeNames = null;
	// public static TreeSet<String> m_attributeValues = null; 
    /**
     * New pane for configuring the SplitLog node.
     */
    protected SplitLogNodeDialog() {
    	/*
    	 * BY adding new tab to Options, but actually it works not so well..
    	 * Because it is just Panel on it, can not pass the values on it.
    	 * How to create the ports for them?? Back home
    	*/
    	
    	
    	// here is used to pass the filter values to NodeModel 
    	m_filterOn = SplitLogNodeModel.createSettingsModelFilterOn();
    	m_attributeKey = SplitLogNodeModel.createSettingsModelAttributeKey();
    	m_attributeValue = SplitLogNodeModel.createSettingsModelAttributeValue();
    	
    }
    
    
    private void updateValues() {
        // save the value from filter into m_***?
    	
    	attributePanel.getFilterValues(filter);
    	System.out.println(filter.attribute_filterOn);
    	System.out.println(filter.attribute_key);
    	
    	m_filterOn.setStringValue(filter.attribute_filterOn);
    	m_attributeKey.setStringValue(filter.attribute_key);
    	String[] values = new String[filter.attribute_values.size()];
    	int i=0;
    	for(String svalue: filter.attribute_values)
    		values[i++] = svalue;
    	
    	m_attributeValue.setStringArrayValue(values);
    	// System.out.println(values);
    	
    }
    
    @Override
    public void onClose() {
    	// TODO Auto-generated method stub
    	updateValues();
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO 
		updateValues();
		m_filterOn.saveSettingsTo(settings);
		m_attributeKey.saveSettingsTo(settings);
		m_attributeValue.saveSettingsTo(settings);
	}
	
	// we set the data component according to the input data
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings,
			final PortObject[] input) throws NotConfigurableException {
		if(input[0] instanceof XLogPortObject) {
			// we can get the log object and then set all the choice of them, but at first, we need to put all the stuff here again
			XLogPortObject logPortObject = (XLogPortObject) input[0];
			log = logPortObject.getLog();
			// System.out.println(log.getAttributes().size());
			
			//  here we need to adapt the setting from here
			filter = new AttributeLogFilter(log);
			attributePanel = new AttributeLogFilter_UI(filter);
	    	
	    	super.removeTab(m_defaultTabTitle);
	    	super.addTab(m_defaultTabTitle, attributePanel);
	    	
			
		}else {
			throw new NotConfigurableException("the input type is not right!");
		}
	}

}

