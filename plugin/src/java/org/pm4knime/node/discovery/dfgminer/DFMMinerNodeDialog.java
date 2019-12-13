package org.pm4knime.node.discovery.dfgminer;

import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.defaultnode.DefaultMinerNodeDialog;

/**
 * <code>NodeDialog</code> for the "DFMMiner" node.
 * 
 * @author Kefang Ding
 */
public class DFMMinerNodeDialog extends DefaultMinerNodeDialog {

	private SettingsModelDoubleBounded m_noiseThreshold = null;
	private SettingsModelString m_lcClf = null;

	@Override
	public void init() {
		m_lcClf = new SettingsModelString(DFMMinerNodeModel.CFG_LCC_KEY, "");
    	DialogComponentStringSelection lcClfComp = new DialogComponentStringSelection(m_lcClf, "LifeCycle Classifier",
    			XLogUtil.getECNames(DFMMinerNodeModel.lcClassifierList));
    	addDialogComponent(lcClfComp);
    	
    	m_noiseThreshold = new SettingsModelDoubleBounded(DFMMinerNodeModel.CFG_NOISE_THRESHOLD_KEY, 0.2, 0, 1.0);
    	DialogComponentNumber noiseThresholdComponent = new DialogComponentNumber(m_noiseThreshold, "Fitness Threshold", 0.8);
    	addDialogComponent(noiseThresholdComponent);
	}
}

