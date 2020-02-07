package org.pm4knime.node.logmanipulation.filter;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "Filter Log By Length" node. It has three options: Strategy, minimum length and maximum length. 
 * if the strategy only includes two values, keep or remove. We can use one boolean value there. 
 * @author Kefang Ding
 */
public class FilterLogByLengthNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the FilterLogByLength node.
     */
    protected FilterLogByLengthNodeDialog() {
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

