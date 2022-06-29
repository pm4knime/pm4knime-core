package org.pm4knime.node.discovery.cgminer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;
import org.knime.core.node.port.PortObjectSpec;


public class CGMinerNodeDialog extends DefaultNodeSettingsPane {
    
	
	public CGMinerNodeDialog() {
		init();
	}
	
	
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

