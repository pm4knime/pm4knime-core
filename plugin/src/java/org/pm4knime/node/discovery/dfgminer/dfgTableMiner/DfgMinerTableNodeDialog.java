package org.pm4knime.node.discovery.dfgminer.dfgTableMiner;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;



/**
 * This is an example implementation of the node dialog of the
 * "DfgMinerTable" node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from
 * {@link org.knime.core.node.NodeDialogPane}. In general, one can create an
 * arbitrary complex dialog using Java Swing.
 * 
 * @author 
 */
public class DfgMinerTableNodeDialog extends DefaultTableMinerDialog {

	SettingsModelString m_variant;
	
	
	public DfgMinerTableNodeDialog(DfgMinerTableNodeModel n) {
		super(n);
	}	
	

	@Override
	public void init() {
		
		m_variant = new SettingsModelString(DfgMinerTableNodeModel.CFG_VARIANT_KEY, DfgMinerTableNodeModel.CFG_VARIANT_VALUES[0]);
		DialogComponentStringSelection variantComp = new DialogComponentStringSelection(m_variant, "Variant",
				DfgMinerTableNodeModel.CFG_VARIANT_VALUES);
    	addDialogComponent(variantComp);  	
	}
    

}

