package org.pm4knime.node.discovery.cgminer;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.pm4knime.util.defaultnode.DefaultMinerNodeDialog;


public class CGMinerNodeDialog extends DefaultMinerNodeDialog {
    
	public void init() {
		
    	createNewGroup("Set Parameters: ");
    	
    	addDialogComponent(new DialogComponentNumber(CGMinerNodeModel.filter_a, CGMinerNodeModel.FILTER_ACTIVITY, 1));
    	addDialogComponent(new DialogComponentNumber(CGMinerNodeModel.filter_t, CGMinerNodeModel.FILTER_TRACE, 0.1));
    	addDialogComponent(new DialogComponentNumber(CGMinerNodeModel.t_certain, CGMinerNodeModel.THRESHOLD_CERTAIN_EDGES, 0.1));
    	addDialogComponent(new DialogComponentNumber(CGMinerNodeModel.t_uncertain, CGMinerNodeModel.THRESHOLD_UNCERTAIN, 0.1));
    	addDialogComponent(new DialogComponentNumber(CGMinerNodeModel.t_longDep, CGMinerNodeModel.THRESHOLD_LONG_DEPENDENCY, 0.1));
    	addDialogComponent(new DialogComponentNumber(CGMinerNodeModel.weight, CGMinerNodeModel.WEIGHT, 0.1));
    	
    	closeCurrentGroup();
    	
    }
}

