package org.pm4knime.node.discovery.alpha;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

public class AlphaMinerNodeDialog extends DefaultNodeSettingsPane {
	private final SettingsModelString m_variant;
	
	protected AlphaMinerNodeDialog() {
        super();
        
        // we need to add two options, one is for the type option, 
        String[] variantList =  AlphaMinerNodeModel.variantList;
        m_variant = new SettingsModelString(AlphaMinerNodeModel.CFGKEY_VARIANT_TYPE, variantList[0]);
        
        // here we can have flowVariable
        addDialogComponent(new DialogComponentStringSelection(m_variant, "Select Inductive Miner Type", variantList));
        
	}
}
