package org.pm4knime.node.conformance;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;

/**
 * <code>NodeDialog</code> for the "UnifiedConformanceChecker" node.
 *
 * @author Kefang Ding
 */
public class UnifiedConformanceCheckerNodeDialog extends DefaultNodeSettingsPane {
	SettingsModelIntegerBounded[] m_defaultCostModels = UnifiedConformanceCheckerNodeModel.initializeCostModels();

	SettingsModelString m_strategy = UnifiedConformanceCheckerNodeModel.createStrategyModel();
	List<XEventClassifier> classifierList;
	List<String> classifierNames;
	SettingsModelString m_classifierName = UnifiedConformanceCheckerNodeModel.createClassifierNameModel();

	/**
	 * New pane for configuring the UnifiedConformanceChecker node.
	 */
	protected UnifiedConformanceCheckerNodeDialog() {

		// put the cost above here
		createNewGroup("Set Cost for Moves: ");
		setHorizontalPlacement(true);
		final DialogComponentNumberEdit[] defaultCostComps = new DialogComponentNumberEdit[UnifiedConformanceCheckerNodeModel.CFG_COST_TYPE_NUM];
		for (int i = 0; i < UnifiedConformanceCheckerNodeModel.CFG_COST_TYPE_NUM; i++) {
			defaultCostComps[i] = new DialogComponentNumberEdit(m_defaultCostModels[i],
					UnifiedConformanceCheckerNodeModel.CFG_MCOST_KEY[i], 5);
			addDialogComponent(defaultCostComps[i]);
		}
		closeCurrentGroup();
		setHorizontalPlacement(false);

		initializeClassifiers();
		final DialogComponentStringSelection m_classifierComp = new DialogComponentStringSelection(m_classifierName,
				"Select Classifier Name", classifierNames);

		addDialogComponent(m_classifierComp);

		final DialogComponentStringSelection m_strategyComp = new DialogComponentStringSelection(m_strategy,
				"Select Replay Strategy", ConformanceCheckerNodeModel.strategyList);
		addDialogComponent(m_strategyComp);

	}

	private void initializeClassifiers() {
		// TODO Auto-generated method stub
		classifierList = new ArrayList();
		classifierList.add(new XEventNameClassifier());
		classifierList.add(new XEventLifeTransClassifier());

		classifierNames = new ArrayList();
		for (final XEventClassifier clf : classifierList) {
			classifierNames.add(clf.name());
		}
	}
}
