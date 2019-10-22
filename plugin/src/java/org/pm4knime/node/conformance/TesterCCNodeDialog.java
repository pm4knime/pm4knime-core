package org.pm4knime.node.conformance;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
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
import org.pm4knime.util.XLogUtil;

/**
 * <code>NodeDialog</code> for the "TesterCC" node.
 * 
 * @author
 */
public class TesterCCNodeDialog extends NodeDialogPane {
	JPanel m_compositePanel;
	SMAlignmentReplayParameter m_parameter;
	final String[] strategyList = TesterCCNodeModel.strategyList;

	/**
	 * New pane for configuring the TesterCC node.
	 */
	protected TesterCCNodeDialog() {

		m_compositePanel = new JPanel();
		m_compositePanel.setLayout(new BoxLayout(m_compositePanel, BoxLayout.Y_AXIS));

		// we are going to use the customized SettingsModel for the node
		// how to import and output the fields from the model??
		// how to create the related dialog component?
		m_parameter = new SMAlignmentReplayParameter("Parameter in Tester");
		// we need to assign the classifier names tehre
		List<String> classifierNames = getECNames(TesterCCNodeModel.classifierList);
		DialogComponentStringSelection m_classifierComp = new DialogComponentStringSelection(
				m_parameter.getMClassifierName(), "Select Classifier Name", classifierNames);
		addDialogComponent(m_classifierComp);

		DialogComponentStringSelection m_strategyComp = new DialogComponentStringSelection(m_parameter.getMStrategy(),
				"Select Replay Strategy", strategyList);
		addDialogComponent(m_strategyComp);

		Box box = new Box(BoxLayout.X_AXIS);
		DialogComponentNumberEdit[] defaultCostComps = new DialogComponentNumberEdit[SMAlignmentReplayParameter.CFG_COST_TYPE_NUM];
		for (int i = 0; i < SMAlignmentReplayParameter.CFG_COST_TYPE_NUM; i++) {
			defaultCostComps[i] = new DialogComponentNumberEdit(m_parameter.getMDefaultCosts()[i],
					SMAlignmentReplayParameter.CFG_MCOST_KEY[i], 5);
			box.add(defaultCostComps[i].getComponentPanel());
		}
		m_compositePanel.add(box);
		super.addTab("Options", m_compositePanel);
	}

	private void addDialogComponent(final DialogComponent diaC) {
		// TODO Auto-generated method stub
		m_compositePanel.add(diaC.getComponentPanel());
	}

	// this function can be included into the event classifier
	static List<String> getECNames(List<XEventClassifier> classifierList) {
		// TODO Auto-generated method stub
		List<String> classifierNames = new ArrayList();
		for (XEventClassifier clf : classifierList) {
			classifierNames.add(clf.name());
		}
		return classifierNames;
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_parameter.saveSettingsTo(settings);
	}

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
