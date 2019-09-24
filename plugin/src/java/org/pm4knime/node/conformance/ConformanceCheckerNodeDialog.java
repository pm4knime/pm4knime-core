package org.pm4knime.node.conformance;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.base.node.io.filereader.ColProperty;
import org.knime.base.node.io.tablecreator.TableCreator2NodeSettings;
import org.knime.base.node.io.tablecreator.table.Spreadsheet;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.DataAwareNodeDialogPane;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentNumberEdit;
import org.knime.core.node.defaultnodesettings.DialogComponentStringSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.XLogPortObject;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;

/**
 * <code>NodeDialog</code> for the "ConformanceChecker" node. it reads from
 * event log and Petri net PO, choose the event classifier for them both get all
 * the event class and transitions from the input and arrange them in table
 *
 * @author Kefang Ding
 */
public class ConformanceCheckerNodeDialog extends DataAwareNodeDialogPane {
	private static final int CFG_COST_TYPE_NUM = 3;
	private static final int[] CFG_DEFAULT_MCOST = { 1, 1, 0 };

	public static String[] CFG_MCOST_KEY = { "model move cost", "log move cost", "sync move cost" };
	public static String[] CFG_MOVE_KEY = { "model move", "log move", "sync move" };

	XLogPortObject logPO;
	PetriNetPortObject netPO;
	// parameters to choose
	SettingsModelString m_strategy = new SettingsModelString(ConformanceCheckerNodeModel.CFGKEY_STRATEGY_TYPE,
			ConformanceCheckerNodeModel.strategyList[0]);

	// classifier to use for the event log, when the net is already shown there..
	List<XEventClassifier> classifierList = new ArrayList();
	List<String> classifierNames = new ArrayList();
	SettingsModelString m_classifierName = new SettingsModelString(ConformanceCheckerNodeModel.CKF_KEY_EVENT_CLASSIFIER,
			"");

	// create Jcomponent
	JPanel optionPanel;
	Spreadsheet m_spreadsheet;
	// to store the values from spreadsheet
	private final TableCreator2NodeSettings m_settings;
	private final SettingsModelIntegerBounded[] defaultCostModels;
	private final DialogComponentNumberEdit[] defaultCostComps;

	Map<XEventClass, Integer> mapEvClass2Cost = null;
	Map<Transition, Integer> mapTrans2Cost = null;
	Map<Transition, Integer> mapSync2Cost = null;

	/*
	 * To get those values, we should have multiple settings here. one is for the
	 * default model move or log move, as default values one is can be set by
	 * individual, the options are important at first!! one is the cost for syn
	 * move. we also need to give the values there.. But here, we need to have the
	 * information from event log, like the LogInfo in the Spec, then we need to
	 * serialize it here
	 */
	protected ConformanceCheckerNodeDialog() {
		// how to create new item here??
		// addDialogComponent(new DialogComponentStringSelection(m_variant, "Select
		// Inductive Miner Type", variantList));
		initializeClassifiers();

		optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.Y_AXIS));
		addTab("Options", optionPanel, true);

		final JPanel defaulCostPanel = new JPanel();
		defaulCostPanel.setLayout(new BoxLayout(defaulCostPanel, BoxLayout.X_AXIS));
		// before the m_spreadsheet, set default column values for the m_spreadsheet
		defaultCostModels = new SettingsModelIntegerBounded[CFG_COST_TYPE_NUM];
		// set the accepting field for it
		defaultCostComps = new DialogComponentNumberEdit[CFG_COST_TYPE_NUM];
		// shoule be added in group

		for (int i = 0; i < CFG_COST_TYPE_NUM; i++) {
			defaultCostModels[i] = new SettingsModelIntegerBounded(CFG_MCOST_KEY[i], CFG_DEFAULT_MCOST[i], 0,
					Integer.MAX_VALUE);
			defaultCostComps[i] = new DialogComponentNumberEdit(defaultCostModels[i], CFG_MCOST_KEY[i], 5);
			defaulCostPanel.add(defaultCostComps[i].getComponentPanel());
		}
		// the syn cost is set to 0 and add them into the optionPanel,
		// put the syncost into the same loop like before we have
//    	defaultCostModels[i] = new SettingsModelIntegerBounded(CFG_MCOST_KEY[i], CFG_DEFAULT_MCOST[i], 0, Integer.MAX_VALUE );
//		defaultCostComps[i] = new DialogComponentNumberEdit(defaultCostModels[i] , CFG_MCOST_KEY[i], 5);
//		defaulCostPanel.add(defaultCostComps[i].getComponentPanel());

		optionPanel.add(defaulCostPanel);

		m_spreadsheet = new Spreadsheet();
		m_settings = new TableCreator2NodeSettings();
		// check the change of m_spreadsheet, reduce the row and column numbers to show
		m_spreadsheet.scrollRectToVisible(new Rectangle(50, 50, 100, 100));
		optionPanel.add(m_spreadsheet);

		// add other component in JPanel here
		final DialogComponentStringSelection m_strategyComp = new DialogComponentStringSelection(m_strategy,
				"Select Replay Strategy", ConformanceCheckerNodeModel.strategyList);
		optionPanel.add(m_strategyComp.getComponentPanel());

		// setClassifierNames();
		final DialogComponentStringSelection m_ClassifierComp = new DialogComponentStringSelection(m_classifierName,
				"Select Classifier Name", classifierNames);
		optionPanel.add(m_ClassifierComp.getComponentPanel());

		// here we need to add listener in the dialog
		setListener();
	}

	private void setListener() {
		// TODO change the column values of m_spreadsheet according to defaultCostModel
		// changes
		// if all the values differ from the default value, what to do ?? Nothing here
		for (int i = 0; i < CFG_COST_TYPE_NUM; i++) {
			final int idx = i;// add one final to accept i
			defaultCostModels[idx].addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(final ChangeEvent e) {
					// if the defaultCost changes, then the column in spreadsheet changes

					final int costValue = defaultCostModels[idx].getIntValue();
					changeColumnCost(idx * 2 + 1, costValue);

				}

			});

		}

	}

	private void changeColumnCost(final int colIdx, final int costValue) {
		// TODO Auto-generated method stub
		// current colIdx should be colIdx*2 + 1 for the real data

		// find all the values with the colIdx is the currentIdx
		final int[] colIndices = m_spreadsheet.getColumnIndices();
		final String[] values = m_spreadsheet.getValues();
		// get the refIdx for colIndices
		// List<Integer> refIdx = new ArrayList();
		for (int i = 0; i < colIndices.length; i++) {
			if (colIndices[i] == colIdx) {
				values[i] = costValue + "";
				// refIdx.add(i);
			}
		}

		// after chaning the value, repaint the graph
		m_spreadsheet.setData(m_spreadsheet.getColumnProperties(), m_spreadsheet.getRowIndices(),
				m_spreadsheet.getColumnIndices(), values);
		// m_spreadsheet.repaint();
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO for the ones that could be saved, then we save it there, others values,
		// not saving..
		m_strategy.saveSettingsTo(settings);
		m_classifierName.saveSettingsTo(settings);

		m_spreadsheet.stopCellEditing();
		if (m_spreadsheet.hasParseErrors()) {
			throw new InvalidSettingsException("Some cells cannot be parsed.");
		}
		m_settings.setRowIndices(m_spreadsheet.getRowIndices());
		m_settings.setColumnIndices(m_spreadsheet.getColumnIndices());
		m_settings.setValues(m_spreadsheet.getValues());
		m_settings.setColumnProperties(m_spreadsheet.getColumnProperties());
		m_settings.setRowIdPrefix(m_spreadsheet.getRowIdPrefix());
		m_settings.setRowIdSuffix(m_spreadsheet.getRowIdSuffix());
		m_settings.setRowIdStartValue(m_spreadsheet.getRowIdStartValue());
		m_settings.setHightlightOutputTable(m_spreadsheet.getHightLightOutputTable());
		m_settings.saveSettings(settings);
	}

	// here we create table to get the cost-table mapping, or we just get the event
	// names and transitions
	// but the relation to event classifier, we need to consider it. In default, we
	// choose the EventName here
	@Override
	protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObject[] input)
			throws NotConfigurableException {
		// inputs are XLog and Petri net

		try {
			m_strategy.loadSettingsFrom(settings);
			m_classifierName.loadSettingsFrom(settings);

			m_settings.loadSettings(settings);
		} catch (final InvalidSettingsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// there are old settings which we can use it here to load the old settings
		// there.
		if (m_settings.getValues().length > 0) {
			m_spreadsheet.setData(m_settings.getColumnProperties(), m_settings.getRowIndices(),
					m_settings.getColumnIndices(), m_settings.getValues());
			m_spreadsheet.setRowIdPrefix(m_settings.getRowIdPrefix());
			m_spreadsheet.setRowIdSuffix(m_settings.getRowIdSuffix());
			m_spreadsheet.setRowIdStartValue(m_settings.getRowIdStartValue());
			m_spreadsheet.setHighlightOutputTable(m_settings.getHightlightOutputTable());
			// I (Heiko) would rather like to give the table the focus, but this
			// does not seem to work, instead clear focused cell
			m_spreadsheet.clearFocusedCell();

		} else {
			// we create the new setting from the input data
			if (!(input[ConformanceCheckerNodeModel.INPORT_LOG] instanceof XLogPortObject))
				throw new NotConfigurableException("Input is not a valid event log!");

			if (!(input[ConformanceCheckerNodeModel.INPORT_PETRINET] instanceof PetriNetPortObject))
				throw new NotConfigurableException("Input is not a valid Petri net!");

			logPO = (XLogPortObject) input[ConformanceCheckerNodeModel.INPORT_LOG];
			netPO = (PetriNetPortObject) input[ConformanceCheckerNodeModel.INPORT_PETRINET];

			// but could we store the event classifier somewhere?? So we don't need to?? Not
			// really!!
			// we can set the default values
			final XLog log = logPO.getLog();
			final AcceptingPetriNet anet = netPO.getANet();

			// TODO: different classifier available
			final XEventClassifier eventClassifier = new XEventNameClassifier();
			final XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

			final XEventClass evClassDummy = ConformanceCheckerNodeModel.evClassDummy;
			final TransEvClassMapping mapping = ConformanceCheckerNodeModel.constructMapping(log, anet.getNet(),
					eventClassifier, evClassDummy);

			final Collection<XEventClass> eventClasses = summary.getEventClasses().getClasses();
			// add one dummy event class here, to make sure they are the one, we can add
			// other value

			final Collection<Transition> transitions = anet.getNet().getTransitions();

			final SortedSet<String> ecSet = new TreeSet<>();
			for (final XEventClass ec : eventClasses) {
				ecSet.add(ec.getId());
			}
			ecSet.add(evClassDummy.getId());

			final SortedSet<String> tSet = new TreeSet();
			for (final Transition t : transitions) {
				tSet.add(t.getLabel());
			}

			final SortedSet<String> sSet = new TreeSet();
			for (final Transition t : mapping.keySet()) {
				sSet.add(t.getLabel() + " : " + mapping.get(t).getId());
			}

			// to show in the table, we just need the name of transitions,
			// so we can create another container to store the values here! Not use the
			// collection methods
			setCostTable(ecSet, tSet, sSet);
		}

	}

	// we create 1 table for three types of cost to show them at first, but based on
	// the spreadsheet
	// without classifier at first
	private void setCostTable(final Collection<String> ecSet, final Collection<String> tSet,
			final Collection<String> sSet) {

		final DataColumnSpec[] specs = new DataColumnSpec[CFG_COST_TYPE_NUM * 2];

		final SortedMap<Integer, ColProperty> props = new TreeMap();

		int i = 0;
		while (i < CFG_COST_TYPE_NUM * 2) {
			specs[i] = new DataColumnSpecCreator(CFG_MOVE_KEY[i / 2], StringCell.TYPE).createSpec();
			final ColProperty moveNamePro = new ColProperty();
			moveNamePro.setColumnSpec(specs[i]);
			moveNamePro.setUserSettings(false);
			moveNamePro.setMissingValuePattern("");
			props.put(i, moveNamePro);

			i++;

			specs[i] = new DataColumnSpecCreator(CFG_MCOST_KEY[i / 2], IntCell.TYPE).createSpec();
			final ColProperty moveCostPro = new ColProperty();
			moveCostPro.setColumnSpec(specs[i]);
			moveCostPro.setUserSettings(false);
			// set default cost as 1
			moveCostPro.setMissingValuePattern(CFG_DEFAULT_MCOST[i / 2] + "");
			props.put(i, moveCostPro);

			i++;
		}

		// first to use the List to store the variable data
		final List<Integer> rowIdxList = new ArrayList();
		final List<Integer> colIdxList = new ArrayList();
		// List<Integer> moveCostList = new ArrayList();
		// List<String> moveNameList = new ArrayList();
		final List<String> moveList = new ArrayList();
		final List<Collection<String>> nameList = new ArrayList();
		nameList.add(ecSet);
		nameList.add(tSet);
		nameList.add(sSet);

		for (int colIdx = 0; colIdx < CFG_COST_TYPE_NUM * 2; colIdx += 2) {
			final Collection<String> nSet = nameList.get(colIdx / 2);

			int rowIdx = 0;
			for (final String name : nSet) {
				// column 0 and 1 we need to use it
				rowIdxList.add(rowIdx);
				colIdxList.add(colIdx);
				moveList.add(name);

				rowIdxList.add(rowIdx);
				colIdxList.add(colIdx + 1);
				moveList.add(CFG_DEFAULT_MCOST[colIdx / 2] + "");

				rowIdx++;
			}

		}

		final int[] rowIndices = convertIntList2Array(rowIdxList);
		final int[] columnIndices = convertIntList2Array(colIdxList);
		final String[] valueMove = moveList.toArray(new String[moveList.size()]);

		// here we are wrong about the final types here. All are in string value
		m_spreadsheet.setData(props, rowIndices, columnIndices, valueMove);

	}

	private int[] convertIntList2Array(final List<Integer> valueList) {
		final int[] valueArray = new int[valueList.size()];
		int i = 0;
		for (final Integer v : valueList) {
			valueArray[i++] = v;
		}
		return valueArray;
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
