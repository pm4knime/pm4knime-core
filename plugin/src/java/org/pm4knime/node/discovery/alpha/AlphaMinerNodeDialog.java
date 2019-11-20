package org.pm4knime.node.discovery.alpha;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * @Date: 20.11.2019 Need to add the choose of classifier to classify the log
 * We can have the classifiers from the XLogPortObjectSpec. Or we choose one from a defined list??
 * The first idea is good, because we can have the ones corresponding to the event log. 
 * But for this, we need to convert the classifier into lists
 * @author kefang-pads
 *
 */
public class AlphaMinerNodeDialog extends DefaultNodeSettingsPane {
	private final SettingsModelString m_variant;
	
	protected AlphaMinerNodeDialog() {
        super();
        
        
        // we need to add two options, one is for the type option, 
        String[] variantList =  AlphaMinerNodeModel.variantList;
        m_variant = new SettingsModelString(AlphaMinerNodeModel.CFGKEY_VARIANT_TYPE, variantList[0]);
        // flowVariable is implicitly defined by saveModel 
        addDialogComponent(new DialogComponentStringSelection(m_variant, "Select Algorithm", variantList));
        
	}
}
