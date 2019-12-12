package org.pm4knime.node.discovery.heuritsicsminer;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.pm4knime.util.defaultnode.DefaultMinerNodeDialog;

/**
 * <code>NodeDialog</code> for the "HeuristicsMiner" node.
 * 
 * @author Kefang Ding
 */
public class HeuristicsMinerNodeDialog extends DefaultMinerNodeDialog {
	// here all the parameters are only allowed to be declared with no value assignment. 
	// because it uses the parents constructor at first, which refers to the init() method.
	// Only the parent is constructed, the current class atttribtues can be initialized.
	// In this ways, it causes the null in init methods!!
	
	private SettingsModelDoubleBounded m_r2b, m_dependency, m_length1Loop, m_length2Loop, m_longDistance;
	private SettingsModelBoolean m_allConnected , m_withLT;
    /**
     * New pane for configuring the HeuristicsMiner node.
     */
	@Override
	public void init() {
		m_r2b = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_R2B, 5, 0, 100);
		m_dependency = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_DEPENDENCY, 90, 0, 100);
		m_length1Loop  = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_LENGTH_ONE_LOOP, 90, 0, 100);
		m_length2Loop = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_LENGTH_TWO_LOOP, 90, 0, 100);
		m_longDistance = new SettingsModelDoubleBounded(HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_DISTANCE, 90, 0, 100);
		
		m_allConnected = new SettingsModelBoolean(HeuristicsMinerNodeModel.CFGKEY_TASK_CONNECTION, true);
		m_withLT = new SettingsModelBoolean(HeuristicsMinerNodeModel.CFGKEY_LONG_DEPENDENCY, false);
		
		addDialogComponent(new DialogComponentBoolean(m_allConnected, HeuristicsMinerNodeModel.CFGKEY_TASK_CONNECTION));
    	addDialogComponent(new DialogComponentBoolean(m_withLT, HeuristicsMinerNodeModel.CFGKEY_LONG_DEPENDENCY));
    	
    	// two groups to get the parameters of HeuristicsMiner 
    	createNewGroup("Set Thresholod: ");
    	
    	addDialogComponent(new DialogComponentNumber(m_r2b, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_R2B, 5));
    	addDialogComponent(new DialogComponentNumber(m_dependency, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_DEPENDENCY, 90));
    	addDialogComponent(new DialogComponentNumber(m_length1Loop, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_LENGTH_ONE_LOOP, 90));
    	addDialogComponent(new DialogComponentNumber(m_length2Loop, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_LENGTH_TWO_LOOP, 90));
    	addDialogComponent(new DialogComponentNumber(m_longDistance, HeuristicsMinerNodeModel.CFGKEY_THRESHOLD_DISTANCE, 90));
    	
    	closeCurrentGroup();
    	
    	
    }
}

