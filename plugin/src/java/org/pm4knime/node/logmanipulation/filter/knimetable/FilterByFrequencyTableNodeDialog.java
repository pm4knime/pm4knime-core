package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.node.discovery.alpha.AlphaMinerNodeModel;
import org.pm4knime.util.defaultnode.DefaultTableNodeDialog;
import org.processmining.alphaminer.parameters.AlphaVersion;

/**
 * <code>NodeDialog</code> for the "FilterByFrequency" node.
 * 
 * @author Ralf
 */
public class FilterByFrequencyTableNodeDialog extends DefaultTableNodeDialog {
	private SettingsModelBoolean m_isKeep, m_isForSingleTV;
	private SettingsModelDoubleBounded m_threshold ;

	@Override
	public void init() {
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
    	
		
	}
}

