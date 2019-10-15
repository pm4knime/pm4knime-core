package org.pm4knime.node.logmanipulation.split2;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "LogSplitter" Node.
 * This log splitter is independent on the event log... we just give the choices , in benefit to use flowVariables
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Kefang
 */
public class LogSplitterNodeDialog extends DefaultNodeSettingsPane {
	SettingsModelString m_attributeKey = LogSplitterNodeModel.createSettingsModelAttributeKey();
	SettingsModelString m_attributeValue = LogSplitterNodeModel.createSettingsModelAttributeValue();
	
    /**
     * New pane for configuring the LogSplitter node.
     * we need two options to split the log, one is the attribute Key, one is the value
     */
    protected LogSplitterNodeDialog() {
    	DialogComponentString c_key = new DialogComponentString(m_attributeKey, 
    			LogSplitterNodeModel.CFG_ATTRIBUTE_KEY, true, 15, createFlowVariableModel(m_attributeKey));
    	
    	DialogComponentString c_value = new DialogComponentString(m_attributeValue, 
    			LogSplitterNodeModel.CFG_ATTRIBUTE_VALUE, true, 15, createFlowVariableModel(m_attributeValue));
    	addDialogComponent(c_key);
    	addDialogComponent(c_value);
    }
}

