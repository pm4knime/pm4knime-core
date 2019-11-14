package org.pm4knime.node.discovery.ilpminer;

import java.util.ArrayList;
import java.util.List;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.settingsmodel.SettingsModelILPMinerParameter;
import org.processmining.causalactivitymatrixminer.miners.MatrixMiner;
import org.processmining.causalactivitymatrixminer.miners.MatrixMinerManager;

/**
 * <code>NodeDialog</code> for the "ILPMiner" node. Since in DefaultNodeSettingsPane, we can't override the methods 
 * for saveSetting and loadSetting ...  
 * It's not a problem, because we can use it by adding there.. So what we need to do is getting it back. 
 * But how to get the settings again back?? 
 * In NodeModel,  saveSetting, we can load it from the settings..
 * @author Kefang Ding
 */
public class ILPMinerNodeDialog extends DefaultNodeSettingsPane {

	
	SettingsModelILPMinerParameter m_parameter; 
    /**
     * New pane for configuring the ILPMiner node.
     */
    protected ILPMinerNodeDialog() {
    	
    	m_parameter = new SettingsModelILPMinerParameter(ILPMinerNodeModel.CFG_KEY_ILP_PARAMETER);
    	// list the strings here
    	List<String> classifierNames = SettingsModelILPMinerParameter.getClassifierNames(SettingsModelILPMinerParameter.setDefaultClassifier());
    	// add the event classifier
    	DialogComponentStringSelection m_classifierComp = new DialogComponentStringSelection(
    			m_parameter.getMclf(), "Set Classifier Name", classifierNames);
    	addDialogComponent(m_classifierComp);
    	
    	DialogComponentStringSelection m_filterTypeComp = new DialogComponentStringSelection(
    			m_parameter.getMfilterType(), "Set Filter Type", SettingsModelILPMinerParameter.CFG_FILTER_TYPES);
    	addDialogComponent(m_filterTypeComp);
    	
    	// it is triggered from the last step to choose the type
    	DialogComponentNumber m_noiseThresholdComp = new DialogComponentNumber(
    			 m_parameter.getMfilterThreshold(), "Set the Noise Threshold", 0.25);
    	addDialogComponent(m_noiseThresholdComp); 
    	
    	
    	// add static initialization
		List<String> miners = new ArrayList<String>();
		for (MatrixMiner miner : MatrixMinerManager.getInstance().getMiners()) {
			miners.add(miner.getName());
		}
    	DialogComponentStringSelection m_algorithmComp = new DialogComponentStringSelection(
    			m_parameter.getMalgorithm(), "Set Algorithm", miners);
    	addDialogComponent(m_algorithmComp);
    	
    }
    
    /*
     * the thing is if it saves the values there again, or not...How about the interfaces it shows?? 
     * we add additional ones.. Let us check
     */
    @Override
    public void saveAdditionalSettingsTo(final NodeSettingsWO settings)
            throws InvalidSettingsException {
    	m_parameter.saveSettingsTo(settings);
    }
    
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
    	
    	try {
			m_parameter.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}

