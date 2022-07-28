package org.pm4knime.node.discovery.alpha.table;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;
import org.processmining.alphaminer.parameters.AlphaVersion;


public class AlphaMinerTableNodeDialog extends DefaultTableMinerDialog {
	public AlphaMinerTableNodeDialog(AlphaMinerTableNodeModel n) {
		super(n);
	}


	@Override
	public void init() {
		AlphaMinerTableNodeModel n = (AlphaMinerTableNodeModel) node;
		String[] variantList =  AlphaMinerTableNodeModel.variantList;        
		addDialogComponent(new DialogComponentStringSelection(n.m_variant,"Alpha Miner Variant", variantList));   
        createNewGroup("Parameters for : " + AlphaVersion.ROBUST.toString());
        addDialogComponent(new DialogComponentNumber(n.m_noiseTLF, AlphaMinerTableNodeModel.CFGKEY_THRESHOLD_NOISE_LF, 5));
        addDialogComponent(new DialogComponentNumber(n.m_noiseTMF, AlphaMinerTableNodeModel.CFGKEY_THRESHOLD_NOISE_MF, 5));
        addDialogComponent(new DialogComponentNumber(n.m_casualTH, AlphaMinerTableNodeModel.CFGKEY_THRESHOLD_CASUAL, 5));
        closeCurrentGroup();
        
        createNewGroup("Parameters for: "+ AlphaVersion.PLUS.toString());
        n.m_ignore_ll = new SettingsModelBoolean(AlphaMinerTableNodeModel.CFG_IGNORE_LL, false);
	    addDialogComponent(new DialogComponentBoolean(n.m_ignore_ll, AlphaMinerTableNodeModel.CFG_IGNORE_LL));
	    closeCurrentGroup();
	    n.m_variant.addChangeListener(e -> {
        	if (n.m_variant.getStringValue().equals(AlphaVersion.ROBUST.toString()))
            {
            	System.out.println(n.m_variant.getStringValue());
            	n.m_noiseTLF.setEnabled(true);
            	n.m_noiseTMF.setEnabled(true);
            	n.m_casualTH.setEnabled(true);
            	n.m_ignore_ll.setEnabled(false);
            } else if (n.m_variant.getStringValue().equals(AlphaVersion.PLUS.toString())) {
            	n.m_noiseTLF.setEnabled(false);
            	n.m_noiseTMF.setEnabled(false);
            	n.m_casualTH.setEnabled(false);
            	n.m_ignore_ll.setEnabled(true);
            } else {
            	n.m_noiseTLF.setEnabled(false);
            	n.m_noiseTMF.setEnabled(false);
            	n.m_casualTH.setEnabled(false);
            	n.m_ignore_ll.setEnabled(false);
            	
            }       	
        });
        n.m_variant.setStringValue(variantList[0]);	
	}
}
