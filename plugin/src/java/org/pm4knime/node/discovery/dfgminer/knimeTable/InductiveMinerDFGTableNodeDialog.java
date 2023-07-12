package org.pm4knime.node.discovery.dfgminer.knimeTable;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.pm4knime.node.discovery.cgminer.CGMinerNodeModel;

/**
 * <code>NodeDialog</code> for the "InductiveMinerDFGTable" node.
 * 
 * @author 
 */
public class InductiveMinerDFGTableNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring the InductiveMinerDFGTable node.
     */
	InductiveMinerDFGTableNodeModel node;
	protected InductiveMinerDFGTableNodeDialog(InductiveMinerDFGTableNodeModel n) {
		node = n;
		addDialogComponent(new DialogComponentNumber(node.m_noiseThreshold, "Write the Noise Threshold", 0));
    }
}

