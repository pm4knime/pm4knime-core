package org.pm4knime.node.discovery.cgminer.table;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.pm4knime.node.discovery.cgminer.CGMinerNodeModel;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;


public class TableCGMinerNodeDialog extends DefaultTableMinerDialog {
    
	@Override
    public void init() {
		
    	//createNewGroup("Set Parameters: ");
    	//addDialogComponent(new DialogComponentStringSelection(TableCGMinerNodeModel.trace_class,
    			//TableCGMinerNodeModel.TRACE_CLASS,
    			//TableCGMinerNodeModel.CLASSES));
    	addDialogComponent(new DialogComponentNumber(TableCGMinerNodeModel.filter_a, CGMinerNodeModel.FILTER_ACTIVITY, 1));
    	addDialogComponent(new DialogComponentNumber(TableCGMinerNodeModel.filter_t, CGMinerNodeModel.FILTER_TRACE, 0.1));
    	addDialogComponent(new DialogComponentNumber(TableCGMinerNodeModel.t_certain, CGMinerNodeModel.THRESHOLD_CERTAIN_EDGES, 0.1));
    	addDialogComponent(new DialogComponentNumber(TableCGMinerNodeModel.t_uncertain, CGMinerNodeModel.THRESHOLD_UNCERTAIN, 0.1));
    	addDialogComponent(new DialogComponentNumber(TableCGMinerNodeModel.t_longDep, CGMinerNodeModel.THRESHOLD_LONG_DEPENDENCY, 0.1));
    	addDialogComponent(new DialogComponentNumber(TableCGMinerNodeModel.weight, CGMinerNodeModel.WEIGHT, 0.1));
    	
    	//closeCurrentGroup();
    	
    }
	
	
}

