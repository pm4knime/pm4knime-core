package org.pm4knime.node.logmanipulation.sample;


import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDouble;

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
	SettingsModelDouble m_samplePercentage = SampleLogNodeModel.createSamplePercentage();
    /**
     * New pane for configuring the SampleLog node.
     */
    protected SampleLogNodeDialog() {
    	
    	/*
    	m_samplePref.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if(m_samplePref.getBooleanValue()) {
					// if true, then set the default value into the 0.2, else setting it to 10
					m_samplePercentage.setDoubleValue(0.3);
				}else {
					m_samplePercentage.setDoubleValue(10);
				}
			}
		});
		*/
    	// create one checkbox for the m_samplePref 
    	DialogComponentBoolean c_perference = new DialogComponentBoolean(m_samplePref, "Use Percentage To Sample:"
    			);
    	
    	addDialogComponent(c_perference);
    	DialogComponentNumber c_percentage = new DialogComponentNumber(m_samplePercentage,
                "Enter Sample Number:", 0.1);
    	
    	addDialogComponent(c_percentage );
    	
    }
}

