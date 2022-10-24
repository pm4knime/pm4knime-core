package org.pm4knime.node.discovery.dfgminer.knimeTable;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "InductiveMinerDFGTable" node.
 * 
 * @author 
 */
public class InductiveMinerDFGTableNodeDialog extends DefaultNodeSettingsPane {


	private SettingsModelDoubleBounded m_noiseThreshold = null;
	private SettingsModelString m_variant;
    /**
     * New pane for configuring the InductiveMinerDFGTable node.
     */
    protected InductiveMinerDFGTableNodeDialog() {
		m_noiseThreshold = new SettingsModelDoubleBounded(InductiveMinerDFGTableNodeModel.CFGKEY_NOISE_THRESHOLD, 0.8, 0, 1.0);
		DialogComponentNumber noiseThresholdComp = new DialogComponentNumber(m_noiseThreshold,
				"Write the Noise Threshold", 0);
		addDialogComponent(noiseThresholdComp);

    }
}

