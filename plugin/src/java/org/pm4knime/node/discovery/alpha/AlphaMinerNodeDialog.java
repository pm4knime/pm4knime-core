package org.pm4knime.node.discovery.alpha;

import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;
import org.processmining.alphaminer.parameters.AlphaVersion;

/**
 * @Date: 20.11.2019 Need to add the choose of classifier to classify the log
 * We can have the classifiers from the XLogPortObjectSpec. Or we choose one from a defined list??
 * The first idea is good, because we can have the ones corresponding to the event log. 
 * But for this, we need to convert the classifier into lists
 * @author kefang-pads
 *
 */
public class AlphaMinerNodeDialog extends DefaultTableMinerDialog {
	public AlphaMinerNodeDialog(AlphaMinerNodeModel n) {
		super(n);
		// TODO Auto-generated constructor stub
	}


	//private SettingsModelString m_variant;
	//private SettingsModelDoubleBounded m_noiseTLF,m_noiseTMF, m_casualTH;
	//private SettingsModelBoolean m_ignore_ll;
	

	@Override
	public void init() {
		AlphaMinerNodeModel n = (AlphaMinerNodeModel) node;
		String[] variantList =  AlphaMinerNodeModel.variantList;
        //m_variant = new SettingsModelString(AlphaMinerNodeModel.CFGKEY_VARIANT_TYPE, variantList[1]);
        
        // flowVariable is implicitly defined by saveModel 


        addDialogComponent(new DialogComponentStringSelection(n.m_variant,"Alpha Miner Variant", variantList));
        
        createNewGroup("Parameters for : " + AlphaVersion.ROBUST.toString());
        	//m_noiseTLF = new SettingsModelDoubleBounded(AlphaMinerNodeModel.CFGKEY_THRESHOLD_NOISE_LF, 0, 0, 100);
        	//m_noiseTMF = new SettingsModelDoubleBounded(AlphaMinerNodeModel.CFGKEY_THRESHOLD_NOISE_MF, 0, 0, 100);
        	//m_casualTH = new SettingsModelDoubleBounded(AlphaMinerNodeModel.CFGKEY_THRESHOLD_CASUAL, 0, 0, 100);

        	addDialogComponent(new DialogComponentNumber(n.m_noiseTLF, AlphaMinerNodeModel.CFGKEY_THRESHOLD_NOISE_LF, 5));
        	addDialogComponent(new DialogComponentNumber(n.m_noiseTMF, AlphaMinerNodeModel.CFGKEY_THRESHOLD_NOISE_MF, 5));
        	addDialogComponent(new DialogComponentNumber(n.m_casualTH, AlphaMinerNodeModel.CFGKEY_THRESHOLD_CASUAL, 5));
        closeCurrentGroup();
        
        createNewGroup("Parameters for: "+ AlphaVersion.PLUS.toString());
        	n.m_ignore_ll = new SettingsModelBoolean(AlphaMinerNodeModel.CFG_IGNORE_LL, false);
	    	addDialogComponent(new DialogComponentBoolean(n.m_ignore_ll, AlphaMinerNodeModel.CFG_IGNORE_LL));
	    closeCurrentGroup();
	    
	  
    	
        
        n.m_variant.addChangeListener(e -> {
        	
            if (n.m_variant.getStringValue().equals(AlphaVersion.ROBUST.toString()))
            {
            	System.out.println(n.m_variant.getStringValue());
            	n.m_noiseTLF.setEnabled(true);
            	n.m_noiseTMF.setEnabled(true);
            	n.m_casualTH.setEnabled(true);
            	n.m_ignore_ll.setEnabled(false);
            }else if (n.m_variant.getStringValue().equals(AlphaVersion.PLUS.toString())) {
            	n.m_noiseTLF.setEnabled(false);
            	n.m_noiseTMF.setEnabled(false);
            	n.m_casualTH.setEnabled(false);
            	n.m_ignore_ll.setEnabled(true);
            }else {
            	n.m_noiseTLF.setEnabled(false);
            	n.m_noiseTMF.setEnabled(false);
            	n.m_casualTH.setEnabled(false);
            	n.m_ignore_ll.setEnabled(false);
            	
            }
        	
        });
        n.m_variant.setStringValue(variantList[0]);
		
	}
}
