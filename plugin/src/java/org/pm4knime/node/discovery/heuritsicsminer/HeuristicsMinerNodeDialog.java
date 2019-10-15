package org.pm4knime.node.discovery.heuritsicsminer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;

/**
 * <code>NodeDialog</code> for the "HeuristicsMiner" node.
 * 
 * @author Kefang Ding
 */
public class HeuristicsMinerNodeDialog extends DefaultNodeSettingsPane {

	private final SettingsModelDoubleBounded m_r2b = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_R2B, 5, 0, 100);
	private final SettingsModelDoubleBounded m_dependency = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_DEPENDENCY, 90, 0, 100);
	private final SettingsModelDoubleBounded m_length1Loop  = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_LENGTH_ONE_LOOP, 90, 0, 100);
	private final SettingsModelDoubleBounded m_length2Loop = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_LENGTH_TWO_LOOP, 90, 0, 100);
	private final SettingsModelDoubleBounded m_longDistance = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_DISTANCE, 90, 0, 100);
	
	private final SettingsModelBoolean m_allConnected = new SettingsModelBoolean(HeuristicsMinerNodeModel.CFGKEY_TASK_CONNECTION, true);
	private final SettingsModelBoolean m_withLT = new SettingsModelBoolean(HeuristicsMinerNodeModel.CFGKEY_LONG_DEPENDENCY, false);
	
    /**
     * New pane for configuring the HeuristicsMiner node.
     */
    protected HeuristicsMinerNodeDialog() {
    	
    	// two groups to get the parameters of HeuristicsMiner 
    	createNewGroup("Set Thresholod: ");
    	
    	addDialogComponent(new DialogComponentNumber(m_r2b, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_R2B, 5));
    	addDialogComponent(new DialogComponentNumber(m_dependency, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_DEPENDENCY, 90));
    	addDialogComponent(new DialogComponentNumber(m_length1Loop, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_LENGTH_ONE_LOOP, 90));
    	addDialogComponent(new DialogComponentNumber(m_length2Loop, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_LENGTH_TWO_LOOP, 90));
    	addDialogComponent(new DialogComponentNumber(m_longDistance, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_DISTANCE, 90));
    	
    	closeCurrentGroup();
    	
    	addDialogComponent(new DialogComponentBoolean(m_allConnected, HeuristicsMinerNodeModel.CFGKEY_TASK_CONNECTION));
    	addDialogComponent(new DialogComponentBoolean(m_withLT, HeuristicsMinerNodeModel.CFGKEY_LONG_DEPENDENCY));
    	
    	
    }
}

