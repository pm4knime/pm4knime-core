package org.pm4knime.node.logmanipulation.sample.knimetable;


import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.pm4knime.node.logmanipulation.filter.knimetable.FilterByFrequencyTableNodeModel;
import org.pm4knime.util.defaultnode.DefaultTableNodeDialog;

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
public class SampleLogTableNodeDialog extends DefaultTableNodeDialog {
	SettingsModelBoolean m_samplePref = SampleLogTableNodeModel.createSamplePerference();
	SettingsModelDoubleBounded m_samplePercentage = SampleLogTableNodeModel.createSamplePercentage();
    /**
     * New pane for configuring the SampleLogTable node.
     */

	@Override
	public void init() {
	   	// create one checkbox for the m_samplePref 

		m_samplePref  = SampleLogTableNodeModel.createSamplePerference();
  	    DialogComponentBoolean prefrenceComp = new DialogComponentBoolean(m_samplePref, "Use Percentage to Sample");
    	addDialogComponent(prefrenceComp);
    	
    	m_samplePercentage = SampleLogTableNodeModel.createSamplePercentage();
    	DialogComponentNumber percentageComp = new DialogComponentNumber(m_samplePercentage,
                "Sample Number:", 0.1);
    	
    	addDialogComponent(percentageComp );
    	
    	// here we need to make sure the number value is not negative
		
	}
}

