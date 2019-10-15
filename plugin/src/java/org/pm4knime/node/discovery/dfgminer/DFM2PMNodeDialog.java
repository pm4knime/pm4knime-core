package org.pm4knime.node.discovery.dfgminer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;

/**
 * <code>NodeDialog</code> for the "DFM2PM" node.
 * 
 * @author Kefang Ding
 */
public class DFM2PMNodeDialog extends DefaultNodeSettingsPane {

	private SettingsModelDoubleBounded m_noiseThreshold = null;
	private SettingsModelBoolean m_useMT = null;
    /**
     * New pane for configuring the DFM2PM node.
     */
    protected DFM2PMNodeDialog() {
    	m_noiseThreshold = new SettingsModelDoubleBounded(DFM2PMNodeModel.CFGKEY_NOISE_THRESHOLD, 0, 0, 1.0);
    	
    	DialogComponentNumber noiseThresholdComp = new DialogComponentNumber(m_noiseThreshold, "Write the Noise Threshold", 0);
    	addDialogComponent(noiseThresholdComp);
    	
    	m_useMT = new SettingsModelBoolean(DFM2PMNodeModel.CFGKEY_USE_MULTITHREAD, true);
    	DialogComponentBoolean useMTComp = new DialogComponentBoolean(m_useMT, "Use MultiThreading");
    	addDialogComponent(useMTComp);
    }
}

