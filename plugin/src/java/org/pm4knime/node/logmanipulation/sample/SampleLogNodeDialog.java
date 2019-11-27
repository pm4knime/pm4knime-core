package org.pm4knime.node.logmanipulation.sample;


import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;

/**
 * <code>NodeDialog</code> for the "SampleLog" Node.
 * Sample the event log by giving number or a precentage of whole size
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Kefang
 */
public class SampleLogNodeDialog extends DefaultNodeSettingsPane {
	SettingsModelBoolean m_samplePref = SampleLogNodeModel.createSamplePerference();
	SettingsModelDoubleBounded m_samplePercentage = SampleLogNodeModel.createSamplePercentage();
    /**
     * New pane for configuring the SampleLog node.
     */
    protected SampleLogNodeDialog() {
    	
    	// create one checkbox for the m_samplePref 
    	DialogComponentBoolean prefrenceComp = new DialogComponentBoolean(m_samplePref, "Use Percentage to Sample");
    	
    	addDialogComponent(prefrenceComp);
    	DialogComponentNumber percentageComp = new DialogComponentNumber(m_samplePercentage,
                "Sample Number:", 0.1);
    	
    	addDialogComponent(percentageComp );
    	
    	// here we need to make sure the number value is not negative
    	
    }
}

