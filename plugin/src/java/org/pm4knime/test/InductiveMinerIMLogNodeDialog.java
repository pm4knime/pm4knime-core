package org.pm4knime.test;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.pm4knime.node.discovery.inductiveminer.InductiveMinerNodeModel;
import org.pm4knime.util.defaultnode.DefaultMinerNodeDialog;

/**
 * <code>NodeDialog</code> for the "InductiveMinerIMLog" node.
 * 
 * @author 
 */
public class InductiveMinerIMLogNodeDialog extends DefaultMinerNodeDialog {

	private SettingsModelString m_type ;
	private SettingsModelDoubleBounded m_noiseThreshold;
	@Override
	public void init() {
		// TODO put other parameters here except classifier
		String[] defaultValue =  InductiveMinerNodeModel.defaultType;
        m_type = new SettingsModelString(InductiveMinerNodeModel.CFGKEY_METHOD_TYPE, defaultValue[1]);
        addDialogComponent(new DialogComponentStringSelection(m_type, "Inductive Miner Variant", defaultValue));
        
        m_noiseThreshold = new SettingsModelDoubleBounded(
        		InductiveMinerNodeModel.CFGKEY_NOISE_THRESHOLD, 0.2, 0, 1);
        DialogComponentNumber noiseThresholdComponent = new DialogComponentNumber(m_noiseThreshold, "Noise threshold", 0.1);
        m_noiseThreshold.setEnabled(true);
        addDialogComponent(noiseThresholdComponent);
        
        m_type.addChangeListener(new ChangeListener() {	
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if(m_type.getStringValue().equals(defaultValue[0])) {
					m_noiseThreshold.setEnabled(false);
		        }else
		        	m_noiseThreshold.setEnabled(true);
			}
		});
	}
}

