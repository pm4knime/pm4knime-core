package org.pm4knime.node.conformance.replayer;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.settingsmodel.SMAlignmentReplayParameter;
import org.pm4knime.util.ReplayerUtil;

/**
 * <code>NodeDialog</code> for the "PNReplayer" node.
 * 
 * @author 
 */
public class DefaultPNReplayerNodeDialog extends DefaultNodeSettingsPane {

	DialogComponentStringSelection m_classifierComp ;
	protected SMAlignmentReplayParameter m_parameter;
	String[] strategyList = ReplayerUtil.strategyList;
	
    /**
     * New pane for configuring the PNReplayer node.
     */
    protected DefaultPNReplayerNodeDialog() {
    	
		specialInit();
		
    }
    
    protected void specialInit() {
		// TODO Auto-generated method stub
		m_parameter = new SMAlignmentReplayParameter(DefaultPNReplayerNodeModel.CFG_PARAMETER_NAME);
		commonInitPanel(m_parameter);
		
	}

    protected void commonInitPanel(SMAlignmentReplayParameter parameter) {
		// parameter = new SMAlignmentReplayParameter("Parameter in Tester");
		// we need to assign the classifier names 
    	
    	// modification at 21 Jan 2020. try to get the classifierNames from the configuration 
    	// how to assign values here, directly after this value?? if we use the list, check the later one 
    	// here, some improvements are in need. This is one constructor, we only assigns values once for the
    	// dialog components. But how to refresh their values according to spec?? 
    	
    	// check if it is an empty values here??
		m_classifierComp = new DialogComponentStringSelection(
				parameter.getMClassifierName(), "Select Classifier Name", 
				new String[ ]{""});
		
		addDialogComponent(m_classifierComp);

		parameter.getMStrategy().setStringValue(strategyList[0]);
		DialogComponentStringSelection m_strategyComp = new DialogComponentStringSelection(parameter.getMStrategy(),
				"Select Replay Strategy", strategyList);
		addDialogComponent(m_strategyComp);

		// change it to horizontal
		setHorizontalPlacement(true);
		DialogComponentNumberEdit[] defaultCostComps = new DialogComponentNumberEdit[SMAlignmentReplayParameter.CFG_COST_TYPE_NUM];
		for (int i = 0; i < SMAlignmentReplayParameter.CFG_COST_TYPE_NUM; i++) {
			defaultCostComps[i] = new DialogComponentNumberEdit(parameter.getMDefaultCosts()[i],
					SMAlignmentReplayParameter.CFG_MCOST_KEY[i], 5);
			addDialogComponent(defaultCostComps[i]);
		}
		
	}
    
    @Override
    public void loadAdditionalSettingsFrom(final NodeSettingsRO settings,
            final PortObjectSpec[] specs) throws NotConfigurableException {
    		
    		// m_parameter.getMClassifierName().loadSettingsFrom(settings);
    		String selectedItem = m_parameter.getMClassifierName().getStringValue();
    		
    		XLogPortObjectSpec logSpec = (XLogPortObjectSpec) specs[0];
    		if(!logSpec.getClassifiersMap().keySet().contains(
    				m_parameter.getMClassifierName().getStringValue())) {
				selectedItem = logSpec.getClassifiersMap().keySet().iterator().next();
				m_parameter.getMClassifierName().setStringValue(selectedItem);
				m_classifierComp.replaceListItems(logSpec.getClassifiersMap().keySet(), selectedItem);
			}
    		
    	
    }
}

