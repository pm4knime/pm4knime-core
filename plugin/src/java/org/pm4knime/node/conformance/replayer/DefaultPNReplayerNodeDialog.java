package org.pm4knime.node.conformance.replayer;

import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponent;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.port.PortObjectSpec;
import org.pm4knime.settingsmodel.SMAlignmentReplayParameter;
import org.pm4knime.util.ReplayerUtil;
import org.pm4knime.util.XLogUtil;

/**
 * <code>NodeDialog</code> for the "PNReplayer" node.
 * 
 * @author 
 */
public class DefaultPNReplayerNodeDialog extends DefaultNodeSettingsPane {

	
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
		// we need to assign the classifier names tehre
		List<String> classifierNames = XLogUtil.getECNames(DefaultPNReplayerNodeModel.classifierList);
		parameter.getMClassifierName().setStringValue(classifierNames.get(0));
		DialogComponentStringSelection m_classifierComp = new DialogComponentStringSelection(
				parameter.getMClassifierName(), "Select Classifier Name", classifierNames);
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
}

