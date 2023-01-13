package org.pm4knime.node.discovery.ilpminer.Table;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.pm4knime.node.discovery.cgminer.table.TableCGMinerNodeModel;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;
import org.pm4knime.settingsmodel.SMILPMinerParameter;
import org.pm4knime.util.defaultnode.DefaultMinerNodeDialog;

/**
 * <code>NodeDialog</code> for the "ILPMiner" node. Since in DefaultNodeSettingsPane, we can't override the methods 
 * for saveSetting and loadSetting ...  
 * It's not a problem, because we can use it by adding there.. So what we need to do is getting it back. 
 * But how to get the settings again back?? 
 * In NodeModel, saveSetting, we can load it from the settings.. but something here are not nice, because of the flow variable.. 
 * 
 * By using the default addDialog, it puts the values directly to the Settings. SO here, if we want to do sth differently. 
 * We could save it in this way, but having the settings there, to control the load and setting stuff there. 
 * 
 * here we still use it, but in the loading part.. We need to check the values there. One thing, if we don't use the 
 * 
 * 14.11.2019 add advanced optional choices, it includes the
 *  Advanced Options: 
 * 			-- LP Objective: unweighted values, weighted values/ relative absolute freq
 *  		-- LP Variable type: two variable per event, one variable per event
 *  		-- Discovery Strategy: mine a place per causal relation, a connection place between each pair
 * 
 * @author Kefang Ding
 */
public class ILPMinerTableNodeDialog extends DefaultTableMinerDialog {

	public ILPMinerTableNodeDialog(ILPMinerTableNodeModel n) {
		super(n);
	}
	
	SMILPMinerParameter m_parameter; 
    /**
     * New pane for configuring the ILPMiner node.
     */
	
	@Override
	public void init() {
		m_parameter = new SMILPMinerParameter(ILPMinerTableNodeModel.CFG_KEY_ILP_PARAMETER);
  
    	DialogComponentStringSelection m_filterTypeComp = new DialogComponentStringSelection(
    			m_parameter.getMfilterType(), "Set Filter Type", SMILPMinerParameter.CFG_FILTER_TYPES);
    	addDialogComponent(m_filterTypeComp);
    	
    	// it is triggered from the last step to choose the type
    	DialogComponentNumber m_noiseThresholdComp = new DialogComponentNumber(
    			 m_parameter.getMfilterThreshold(), "Noise Threshold", 0.25);
    	addDialogComponent(m_noiseThresholdComp);
    	
    	
    	DialogComponentStringSelection m_lpObjComp = new DialogComponentStringSelection(
    			m_parameter.getMLPObj(), "Set Objective Function", SMILPMinerParameter.CFG_LPOBJ_TYPES);
    	addDialogComponent(m_lpObjComp);
    	
    	DialogComponentStringSelection m_lpVarComp = new DialogComponentStringSelection(
    			m_parameter.getMLPVar(), "Set Variable Distribution", SMILPMinerParameter.CFG_LPVAR_TYPES);
    	addDialogComponent(m_lpVarComp);
    	
    	DialogComponentStringSelection m_DSComp = new DialogComponentStringSelection(
    			m_parameter.getMDS(), "Set Discovery Strategy", SMILPMinerParameter.CFG_DS_TYPES);
    	addDialogComponent(m_DSComp);
	}
	
    
}

