package org.pm4knime.node.discovery.heuritsicsminer.table;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;
import org.pm4knime.node.discovery.ilpminer.Table.ILPMinerTableNodeModel;
import org.pm4knime.util.defaultnode.DefaultMinerNodeDialog;

/**
 * <code>NodeDialog</code> for the "HeuristicsMiner" node.
 * 
 * @author Kefang Ding
 */
public class HeuristicsMinerTableNodeDialog extends DefaultTableMinerDialog {
	// here all the parameters are only allowed to be declared with no value assignment. 
	// because it uses the parents constructor at first, which refers to the init() method.
	// Only the parent is constructed, the current class atttribtues can be initialized.
	// In this ways, it causes the null in init methods!!
	
	public HeuristicsMinerTableNodeDialog(HeuristicsMinerTableNodeModel n) {
		super(n);
	}
    /**
     * New pane for configuring the HeuristicsMiner node.
     */
	@Override
	public void init() {
		
		addDialogComponent(new DialogComponentBoolean(((HeuristicsMinerTableNodeModel) node).m_allConnected, HeuristicsMinerTableNodeModel.CFGKEY_TASK_CONNECTION));
    	addDialogComponent(new DialogComponentBoolean(((HeuristicsMinerTableNodeModel) node).m_withLT, HeuristicsMinerTableNodeModel.CFGKEY_LONG_DEPENDENCY));
    	
    	// two groups to get the parameters of HeuristicsMiner 
    	createNewGroup("Set Thresholod: ");
    	
    	addDialogComponent(new DialogComponentNumber(((HeuristicsMinerTableNodeModel) node).m_r2b, HeuristicsMinerTableNodeModel.CFGKEY_THRESHOLD_R2B, 5));
    	addDialogComponent(new DialogComponentNumber(((HeuristicsMinerTableNodeModel) node).m_dependency, HeuristicsMinerTableNodeModel.CFGKEY_THRESHOLD_DEPENDENCY, 90));
    	addDialogComponent(new DialogComponentNumber(((HeuristicsMinerTableNodeModel) node).m_length1Loop, HeuristicsMinerTableNodeModel.CFGKEY_THRESHOLD_LENGTH_ONE_LOOP, 90));
    	addDialogComponent(new DialogComponentNumber(((HeuristicsMinerTableNodeModel) node).m_length2Loop, HeuristicsMinerTableNodeModel.CFGKEY_THRESHOLD_LENGTH_TWO_LOOP, 90));
    	addDialogComponent(new DialogComponentNumber(((HeuristicsMinerTableNodeModel) node).m_longDistance, HeuristicsMinerTableNodeModel.CFGKEY_THRESHOLD_DISTANCE, 90));
    	
    	closeCurrentGroup();
    	
    	
    }
}

