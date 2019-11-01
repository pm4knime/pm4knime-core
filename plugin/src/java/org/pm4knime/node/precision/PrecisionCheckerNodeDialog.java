package org.pm4knime.node.precision;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.processmining.plugins.multietc.sett.MultiETCSettings;

/**
 * <code>NodeDialog</code> for the "PrecisionChecker" node.
 * The dialog needs to provide parameters:
 * <1> MultiETCSettings.ALGORITHM 
 * <2> MultiETCSettings.REPRESENTATION
 * 
 * According to the MultiETCSettings.REPRESENTATION, we have different conversion methods
 * to conver the RepResult into differnt format... One is like this, another methods is by using the MatchInstances there
 * 
 * Because there are only two parameters, we use the defaultComponents
 * @author Kefang Ding
 */
public class PrecisionCheckerNodeDialog extends DefaultNodeSettingsPane {

	private final SettingsModelBoolean m_isOrdered =  new SettingsModelBoolean(
			MultiETCSettings.REPRESENTATION, true);
	private final SettingsModelString m_algorithm =  new SettingsModelString(
			MultiETCSettings.ALGORITHM, PrecisionCheckerNodeModel.ALIGN_1);
    /**
     * New pane for configuring the PrecisionChecker node.
     */
    protected PrecisionCheckerNodeDialog() {
    	DialogComponentBoolean m_isOrderedComp = new DialogComponentBoolean(m_isOrdered, "Ordered Presentation");
    	addDialogComponent(m_isOrderedComp);
    	
    	DialogComponentStringSelection m_algorithmComp = new DialogComponentStringSelection(m_algorithm,
    			MultiETCSettings.ALGORITHM, PrecisionCheckerNodeModel.ALIGN_1, PrecisionCheckerNodeModel.ALIGN_ALL, 
    			PrecisionCheckerNodeModel.ALIGN_REPRE, PrecisionCheckerNodeModel.ETC) ;
 		addDialogComponent(m_algorithmComp);
    }
}

