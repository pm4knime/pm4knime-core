package org.pm4knime.node.discovery.cgminer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;


public class CGMinerNodeDialog extends DefaultNodeSettingsPane {
    
	CGMinerNodeModel node;
	public CGMinerNodeDialog(CGMinerNodeModel n) {
		node = n;
		init();
	}
	
	
    public void init() {
		
    	createNewGroup("Set Parameters: ");
    	
    	addDialogComponent(new DialogComponentNumber(node.filter_a, CGMinerNodeModel.FILTER_ACTIVITY, 1));
    	addDialogComponent(new DialogComponentNumber(node.filter_t, CGMinerNodeModel.FILTER_TRACE, 0.1));
    	addDialogComponent(new DialogComponentNumber(node.t_certain, CGMinerNodeModel.THRESHOLD_CERTAIN_EDGES, 0.1));
    	addDialogComponent(new DialogComponentNumber(node.t_uncertain, CGMinerNodeModel.THRESHOLD_UNCERTAIN, 0.1));
    	addDialogComponent(new DialogComponentNumber(node.t_longDep, CGMinerNodeModel.THRESHOLD_LONG_DEPENDENCY, 0.1));
    	addDialogComponent(new DialogComponentNumber(node.weight, CGMinerNodeModel.WEIGHT, 0.1));
    	
    	closeCurrentGroup();
    	
    }
}

