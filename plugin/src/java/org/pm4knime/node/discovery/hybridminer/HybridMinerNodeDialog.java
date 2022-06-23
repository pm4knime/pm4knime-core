package org.pm4knime.node.discovery.hybridminer;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.pm4knime.util.defaultnode.DefaultMinerNodeDialog;


public class HybridMinerNodeDialog extends DefaultMinerNodeDialog {
    
	public void init() {
    	createNewGroup("Set Parameters: ");
    	addDialogComponent(new DialogComponentNumber(HybridMinerNodeModel.t_cancel, HybridMinerNodeModel.THRESHOLD_CANCEL, 1000));
    	addDialogComponent(new DialogComponentStringSelection(HybridMinerNodeModel.type_fitness,
    			HybridMinerNodeModel.FITNESS_TYPE,
    			HybridMinerNodeModel.FITNESS_TYPES.values()));
    	addDialogComponent(new DialogComponentNumber(HybridMinerNodeModel.t_fitness, HybridMinerNodeModel.THRESHOLD_FITNESS, 0.1));
    	closeCurrentGroup();
    }
}

