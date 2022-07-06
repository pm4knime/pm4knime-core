package org.pm4knime.node.discovery.cgminer.table;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.pm4knime.node.discovery.cgminer.CGMinerNodeModel;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;


public class TableCGMinerNodeDialog extends DefaultTableMinerDialog {
    
	public TableCGMinerNodeDialog(TableCGMinerNodeModel n) {
		super(n);
	}

	@Override
    public void init() {
    	addDialogComponent(new DialogComponentNumber(((TableCGMinerNodeModel) node).filter_a, CGMinerNodeModel.FILTER_ACTIVITY, 1));
    	addDialogComponent(new DialogComponentNumber(((TableCGMinerNodeModel) node).filter_t, CGMinerNodeModel.FILTER_TRACE, 0.1));
    	addDialogComponent(new DialogComponentNumber(((TableCGMinerNodeModel) node).t_certain, CGMinerNodeModel.THRESHOLD_CERTAIN_EDGES, 0.1));
    	addDialogComponent(new DialogComponentNumber(((TableCGMinerNodeModel) node).t_uncertain, CGMinerNodeModel.THRESHOLD_UNCERTAIN, 0.1));
    	addDialogComponent(new DialogComponentNumber(((TableCGMinerNodeModel) node).t_longDep, CGMinerNodeModel.THRESHOLD_LONG_DEPENDENCY, 0.1));
    	addDialogComponent(new DialogComponentNumber(((TableCGMinerNodeModel) node).weight, CGMinerNodeModel.WEIGHT, 0.1));    	
    }
	
	
}

