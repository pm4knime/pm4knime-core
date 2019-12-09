package org.pm4knime.node.discovery.dfgminer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.util.XLogUtil;

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
    	m_noiseThreshold = new SettingsModelDoubleBounded(DFMMinerNodeModel.CFG_NOISE_THRESHOLD_KEY, 0.2, 0, 1.0);
    	m_clf = new SettingsModelString(DFMMinerNodeModel.CFG_CLASSIFIER_KEY, "");
    	m_lcClf = new SettingsModelString(DFMMinerNodeModel.CFG_LCC_KEY, "");
    	
    	DialogComponentNumber noiseThresholdComponent = new DialogComponentNumber(m_noiseThreshold, "Set Fitness Threshold", 0.8);
    	DialogComponentStringSelection clfComp = new DialogComponentStringSelection(m_clf, "Select Classifier", XLogUtil.getECNames(DFMMinerNodeModel.classifierList));
    	
    	DialogComponentStringSelection lcClfComp = new DialogComponentStringSelection(m_lcClf, "Select LifeCycle Classifier", XLogUtil.getECNames(DFMMinerNodeModel.lcClassifierList));
    	
    	addDialogComponent(noiseThresholdComponent);
    	addDialogComponent(clfComp);
    	addDialogComponent(lcClfComp);
    	
    }
}

