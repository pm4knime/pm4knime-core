package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.node.discovery.alpha.AlphaMinerNodeModel;
import org.pm4knime.node.logmanipulation.filter.FilterLogByLengthNodeModel;
import org.pm4knime.util.defaultnode.DefaultTableNodeDialog;
import org.processmining.alphaminer.parameters.AlphaVersion;

/**
 * <code>NodeDialog</code> for the "FilterByFrequency" node.
 * 
 * @author Ralf
 */
public class FilterByLengthTableNodeDialog extends DefaultTableNodeDialog {

	@Override
	public void init() {
		SettingsModelBoolean m_isKeep = new SettingsModelBoolean(FilterLogByLengthNodeModel.CFG_ISKEEP, true);
    	DialogComponentBoolean isKeepComp = new DialogComponentBoolean(m_isKeep, FilterLogByLengthNodeModel.CFG_ISKEEP);
    	addDialogComponent(isKeepComp);
    	
    	// it has the lower value, but not the maximal value for it. 
    	SettingsModelIntegerBounded m_minLength = new SettingsModelIntegerBounded(
    			FilterLogByLengthNodeModel.CFG_MININUM_LENGTH, 1, 1, Integer.MAX_VALUE);
    	DialogComponentNumber minComp = new DialogComponentNumber(m_minLength,
    			FilterLogByLengthNodeModel.CFG_MININUM_LENGTH , 1);
    	addDialogComponent(minComp);
    	
    	SettingsModelIntegerBounded m_maxLength = new SettingsModelIntegerBounded(
    			FilterLogByLengthNodeModel.CFG_MAXINUM_LENGTH, 20, 1, Integer.MAX_VALUE);
    	DialogComponentNumber maxComp = new DialogComponentNumber(m_maxLength,
    			FilterLogByLengthNodeModel.CFG_MAXINUM_LENGTH , 1);
    	addDialogComponent(maxComp);
		
	}
}

