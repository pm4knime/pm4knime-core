package org.pm4knime.node.logmanipulation.split2;

import java.util.HashMap;
import java.util.Map;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.portobject.XLogPortObjectSpec;

/**
 * <code>NodeDialog</code> for the "LogSplitter" Node.
 * This log splitter is independent on the event log. Now, to match the spec, we only make the 
 * attribute key changed due to the spec, but the values, we need to assign it.
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Kefang
 */
public class LogSplitterNodeDialog extends DefaultNodeSettingsPane {
	SettingsModelString m_attributeKey = LogSplitterNodeModel.createSettingsModelAttributeKey();
	SettingsModelString m_attributeValue = LogSplitterNodeModel.createSettingsModelAttributeValue();
	DialogComponentStringSelection keyComp;
    /**
     * New pane for configuring the LogSplitter node.
     * we need two options to split the log, one is the attribute Key, one is the value
     */
    protected LogSplitterNodeDialog() {
    	// selection
    	keyComp = new DialogComponentStringSelection(m_attributeKey,
    			LogSplitterNodeModel.CFG_ATTRIBUTE_KEY, new String[] {""});
    	
    	DialogComponentString valueComp = new DialogComponentString(m_attributeValue, 
    			LogSplitterNodeModel.CFG_ATTRIBUTE_VALUE, true, 15, createFlowVariableModel(m_attributeValue));
    	addDialogComponent(keyComp);
    	addDialogComponent(valueComp);
    }
    
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
    	XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
    	// here we need to set the event attributes and trace attributes there
    	Map<String, Class> attrs = new HashMap(logSpec.getGTraceAttrMap());
    	attrs.putAll(logSpec.getGEventAttrMap());
    	String select = attrs.keySet().iterator().next();
		keyComp.replaceListItems(attrs.keySet(), select); 
    	
    	try {
			m_attributeKey.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
}

