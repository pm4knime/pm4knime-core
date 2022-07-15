package org.pm4knime.node.discovery.hybridminer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;


public class HybridMinerNodeDialog extends DefaultNodeSettingsPane {
	
	HybridMinerNodeModel node;
    
	public HybridMinerNodeDialog(HybridMinerNodeModel n) {
		this.node = n;
    	createNewGroup("Set Parameters: ");
    	addDialogComponent(new DialogComponentNumber(node.t_cancel, HybridMinerNodeModel.THRESHOLD_CANCEL, 1000));
    	addDialogComponent(new DialogComponentStringSelection(node.type_fitness,
    			HybridMinerNodeModel.FITNESS_TYPE,
    			HybridMinerNodeModel.FITNESS_TYPES.values()));
    	addDialogComponent(new DialogComponentNumber(node.t_fitness, HybridMinerNodeModel.THRESHOLD_FITNESS, 0.1));
    	closeCurrentGroup();
    }
}

