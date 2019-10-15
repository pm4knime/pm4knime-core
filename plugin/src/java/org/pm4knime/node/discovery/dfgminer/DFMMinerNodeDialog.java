package org.pm4knime.node.discovery.dfgminer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "DFMMiner" node.
 * 
 * @author Kefang Ding
 */
public class DFMMinerNodeDialog extends DefaultNodeSettingsPane {

	private SettingsModelDoubleBounded m_noiseThreshold = null;
	private SettingsModelString m_clf = null;
	private SettingsModelString m_lcClf = null;
	
	
    /**
     * New pane for configuring the DFMMiner node.
     */
    protected DFMMinerNodeDialog() {
    	m_noiseThreshold = new SettingsModelDoubleBounded(DFMMinerNodeModel.CFG_NOISE_THRESHOLD_KEY, 0.8, 0, 1.0);
    	m_clf = new SettingsModelString(DFMMinerNodeModel.CFG_CLASSIFIER_KEY, "");
    	m_lcClf = new SettingsModelString(DFMMinerNodeModel.CFG_LCC_KEY, "");
    	
    	DialogComponentNumber noiseThresholdComponent = new DialogComponentNumber(m_noiseThreshold, "Set Fitness Threshold", 0.8);
    	DialogComponentStringSelection clfComp = new DialogComponentStringSelection(m_clf, "Select Classifier", DFMMinerNodeModel.defaultClfNames);
    	
    	DialogComponentStringSelection lcClfComp = new DialogComponentStringSelection(m_lcClf, "Select LifeCycle Classifier", DFMMinerNodeModel.defaultLcClfNames);
    	
    	addDialogComponent(noiseThresholdComponent);
    	addDialogComponent(clfComp);
    	addDialogComponent(lcClfComp);
    	
    }
}

