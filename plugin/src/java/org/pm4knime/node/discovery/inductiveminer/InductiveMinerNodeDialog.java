package org.pm4knime.node.discovery.inductiveminer;

import java.util.List;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "InductiveMiner" Node.
 * use the inductive miner to do process discovery
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author KFDing
 */
public class InductiveMinerNodeDialog extends DefaultNodeSettingsPane {

	private final SettingsModelString m_type;
	private SettingsModelDoubleBounded m_noiseThreshold = null;
	private SettingsModelString m_classifier = null;
    /**
     * New pane for configuring InductiveMiner node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected InductiveMinerNodeDialog() {
        super();
        
        // we need to add two options, one is for the type option, 
        String[] defaultValue =  InductiveMinerNodeModel.defaultType;
        m_type = new SettingsModelString(InductiveMinerNodeModel.CFGKEY_METHOD_TYPE, defaultValue[0]);
        
        m_noiseThreshold = new SettingsModelDoubleBounded(
        		InductiveMinerNodeModel.CFGKEY_NOISE_THRESHOLD, 0.2, 0, 1);
        DialogComponentNumber noiseThresholdComponent = new DialogComponentNumber(m_noiseThreshold, "Write the Noise Threshold", 0.1);
        addDialogComponent(new DialogComponentStringSelection(m_type, "Select Inductive Miner Type", defaultValue));
        addDialogComponent(noiseThresholdComponent);
        /*
        m_type.addChangeListener(new ChangeListener() {	
			@Override
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if(m_type.getStringValue().equals(defaultValue[0])) {
		        	noiseThresholdComponent.setEnabled(false);
		        	
		        }
			}
		});
		*/
        // for noise filter, which is triggered by the choice of m_type 
        // this is for choose information w.r.t. the InData
        List<String> classifierNames =  InductiveMinerNodeModel.defaultClassifer;
        DialogComponentStringSelection m_classifierComp ;
        m_classifier =  new SettingsModelString(InductiveMinerNodeModel.CFGKEY_CLASSIFIER, "select a classifier");
        
        m_classifierComp = new DialogComponentStringSelection(m_classifier, "Select One Classifier", classifierNames);
        addDialogComponent(m_classifierComp);
        
    }
}

