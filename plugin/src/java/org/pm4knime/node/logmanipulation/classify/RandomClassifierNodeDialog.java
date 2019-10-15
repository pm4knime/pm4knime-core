package org.pm4knime.node.logmanipulation.classify;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentString;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.node.logmanipulation.split2.LogSplitterNodeModel;

/**
 * <code>NodeDialog</code> for the "RandomClassifier" Node.
 * RandomClassifier classifies the event log randomly, and assigns labels to the trace
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 *  
 * 
 * @author Kefang Ding
 */
public class RandomClassifierNodeDialog extends DefaultNodeSettingsPane {
	SettingsModelString m_attributeKey = RandomClassifierNodeModel.createSettingsModelAttributeKey();
	// lack the values choices for this..
	SettingsModelDoubleBounded m_overlapRate = RandomClassifierNodeModel.createSettingsModelOverlapRate();
	SettingsModelDoubleBounded m_posRate = RandomClassifierNodeModel.createSettingsModelPosRate();
	
    /**
     * New pane for configuring the RandomClassifier node.
     */
    protected RandomClassifierNodeDialog() {
    	DialogComponentString c_key = new DialogComponentString(m_attributeKey, 
    			LogSplitterNodeModel.CFG_ATTRIBUTE_KEY, true, 15, createFlowVariableModel(m_attributeKey));
    	addDialogComponent(c_key);
    	
    	DialogComponentNumber overlapRateComponent = new DialogComponentNumber(m_overlapRate, "Overlap Rate: ", 0, createFlowVariableModel(m_overlapRate));
    	addDialogComponent(overlapRateComponent);
    	

    	DialogComponentNumber posRateComponent = new DialogComponentNumber(m_posRate, "Positive Rate: ", 0, createFlowVariableModel(m_posRate));
    	addDialogComponent(posRateComponent);
    	
    }
}

