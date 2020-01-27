package org.pm4knime.node.discovery.dfgminer;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "DFM2PM" node.
 * 
 * @author Kefang Ding
 */
public class DFM2PMNodeDialog extends DefaultNodeSettingsPane {

	private SettingsModelDoubleBounded m_noiseThreshold = null;
	private SettingsModelString m_variant;

	/**
	 * New pane for configuring the DFM2PM node.
	 */
	protected DFM2PMNodeDialog() {
		m_variant = new SettingsModelString(DFM2PMNodeModel.CFG_VARIANT_KEY, "");
		DialogComponentStringSelection variantComp = new DialogComponentStringSelection(m_variant, "Variant",
				DFM2PMNodeModel.CFG_VARIANT_VALUES);
		addDialogComponent(variantComp);

		m_noiseThreshold = new SettingsModelDoubleBounded(DFM2PMNodeModel.CFGKEY_NOISE_THRESHOLD, 0.8, 0, 1.0);
		DialogComponentNumber noiseThresholdComp = new DialogComponentNumber(m_noiseThreshold,
				"Write the Noise Threshold", 0);
		addDialogComponent(noiseThresholdComp);

	}
}
