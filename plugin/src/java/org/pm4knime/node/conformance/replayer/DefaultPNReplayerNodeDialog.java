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
public class DefaultPNReplayerNodeDialog extends NodeDialogPane {

	JPanel m_compositePanel;
	protected SMAlignmentReplayParameter m_parameter;
	String[] strategyList = ReplayerUtil.strategyList;
    /**
     * New pane for configuring the PNReplayer node.
     */
    protected DefaultPNReplayerNodeDialog() {
    	m_compositePanel = new JPanel();
		m_compositePanel.setLayout(new BoxLayout(m_compositePanel, BoxLayout.Y_AXIS));

		// we are going to use the customized SettingsModel for the node
		// how to import and output the fields from the model??
		// how to create the related dialog component?
		specialInit();
		
		super.addTab("Options", m_compositePanel);
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
		DialogComponentStringSelection m_classifierComp = new DialogComponentStringSelection(
				parameter.getMClassifierName(), "Select Classifier Name", classifierNames);
		addDialogComponent(m_classifierComp);

		DialogComponentStringSelection m_strategyComp = new DialogComponentStringSelection(parameter.getMStrategy(),
				"Select Replay Strategy", strategyList);
		addDialogComponent(m_strategyComp);

		Box box = new Box(BoxLayout.X_AXIS);
		DialogComponentNumberEdit[] defaultCostComps = new DialogComponentNumberEdit[SMAlignmentReplayParameter.CFG_COST_TYPE_NUM];
		for (int i = 0; i < SMAlignmentReplayParameter.CFG_COST_TYPE_NUM; i++) {
			defaultCostComps[i] = new DialogComponentNumberEdit(parameter.getMDefaultCosts()[i],
					SMAlignmentReplayParameter.CFG_MCOST_KEY[i], 5);
			box.add(defaultCostComps[i].getComponentPanel());
		}
		m_compositePanel.add(box);
	}
	
	protected void addDialogComponent(final DialogComponent diaC) {
		// TODO Auto-generated method stub
		m_compositePanel.add(diaC.getComponentPanel());
	}


	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_parameter.saveSettingsTo(settings);
	}

	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
			throws NotConfigurableException {
		try {
			m_parameter.loadSettingsFrom(settings);
		} catch (InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
}

