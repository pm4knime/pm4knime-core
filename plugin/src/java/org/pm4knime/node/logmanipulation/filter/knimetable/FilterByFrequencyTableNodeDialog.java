package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;

/**
 * <code>NodeDialog</code> for the "FilterByFrequency" node.
 * 
 * @author Kefang Ding
 */
public class FilterByFrequencyTableNodeDialog extends DefaultNodeSettingsPane {
	SettingsModelBoolean m_isKeep, m_isForSingleTV;
	SettingsModelDoubleBounded m_threshold ;
	
    /**
     * New pane for configuring the FilterByFrequency node.
     */
    protected FilterByFrequencyTableNodeDialog() {
    	m_isKeep = new SettingsModelBoolean(FilterByFrequencyTableNodeModel.CFG_ISKEEP, true);
    	DialogComponentBoolean isKeepComp = new DialogComponentBoolean(m_isKeep, FilterByFrequencyTableNodeModel.CFG_ISKEEP);
    	addDialogComponent(isKeepComp);
    	
    	m_isForSingleTV  = new SettingsModelBoolean(FilterByFrequencyTableNodeModel.CFG_ISFOR_SINGLETRACE_VARIANT, true);
    	DialogComponentBoolean isForSingleTVComp = new DialogComponentBoolean(m_isForSingleTV, FilterByFrequencyTableNodeModel.CFG_ISFOR_SINGLETRACE_VARIANT);
    	addDialogComponent(isForSingleTVComp);
    	
    	m_threshold = new SettingsModelDoubleBounded(
    			FilterByFrequencyTableNodeModel.CFG_THRESHOLD, 0.2, 0, Integer.MAX_VALUE);
    	DialogComponentNumberEdit thresholdComp = new DialogComponentNumberEdit(m_threshold,
    			FilterByFrequencyTableNodeModel.CFG_THRESHOLD , 5);
    	addDialogComponent(thresholdComp);
    	/*
    	m_threshold.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if(m_threshold.getDoubleValue() > 1.0) {
					m_threshold.setDoubleValue((int) m_threshold.getDoubleValue());
					thresholdComp.getComponentPanel().repaint();
				}
			}
    		
    	});
    	*/
   }
}

