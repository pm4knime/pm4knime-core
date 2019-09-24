package org.pm4knime.node.conformance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.base.node.io.tablecreator.TableCreator2NodeSettings;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.portobject.RepResultPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.settingsmodel.XEventClassifierInterface;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

/**
 * <code>NodeModel</code> for the "ConformanceChecker" node. ProM Plugin
 * PNetReplayer is used to implement this function, which we could also get the
 * performance measure in this plugin. At first, only the fitness part.
 * Performance and Precision are in different nodes. It outputs the fitness info
 * in one table, the alignment info in another output. The colored Petri net are
 * in a view.
 *
 * The parameters in RapidProm differ from the ones in ProM, so we need to adapt
 * this change. The chosen parameters are:: <1> cost for log and transition move
 * for each transition and event class :: show them in a table in advance
 * setting <2> strategy chosen to calculate the alignment :: native library
 * problem <3> ?? Marking, the initial marking and final marking as the
 * parameters :: if not stored, we need to create by ourselves!!
 *
 * @author Kefang Ding
 */
public class ConformanceCheckerNodeModel extends NodeModel implements XEventClassifierInterface {

	private static final NodeLogger logger = NodeLogger.getLogger(ConformanceCheckerNodeModel.class);

	protected static final int INPORT_LOG = 0;
	protected static final int INPORT_PETRINET = 1;

	private XLogPortObject logPO;
	private PetriNetPortObject netPO;
	private TransEvClassMapping mapping;

	RepResultPortObject repResultPO;
	final static XEventClass evClassDummy = new XEventClass("dummy", 1);

	// model related parameters
	// choose algorithms to use for replay
	public static final String CFGKEY_STRATEGY_TYPE = "Strategy type";
	public static final String[] strategyList = { "ILP Replayer", "non-ILP Replayer" };

	public static final String CKF_KEY_EVENT_CLASSIFIER = "Event Classifier";
	SettingsModelString m_strategy = new SettingsModelString(CFGKEY_STRATEGY_TYPE, strategyList[0]);

	// set event classifier to choose from default List
	// but this can be changed due to different event log situation. So we need to
	// keep them different!!
	List<XEventClassifier> classifierList;
	public List<String> classifierNames;
	SettingsModelString m_classifierName = new SettingsModelString(CKF_KEY_EVENT_CLASSIFIER, "");

	TableCreator2NodeSettings m_costSettings;

	/**
	 * Constructor for the node model.
	 */
	protected ConformanceCheckerNodeModel() {

		// TODO: Specify the amount of input and output ports needed.
		// input ports include XLogPortObject, and PetriNetPortObject. Output ports
		// include One table to show the fitness info,
		// ont output PortObject for the alignment.

		super(new PortType[] { XLogPortObject.TYPE, PetriNetPortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE, PortObject.TYPE });

		initializeClassifiers();
		m_costSettings = new TableCreator2NodeSettings();
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		logger.info("Start: PNReplayer Conformance Checking");
		logPO = (XLogPortObject) inData[INPORT_LOG];
		netPO = (PetriNetPortObject) inData[INPORT_PETRINET];

		final XLog log = logPO.getLog();
		// to replay the net, we need marking there
		final AcceptingPetriNet anet = netPO.getANet();

		final XEventClassifier eventClassifier = getXEventClassifier();
		final IPNReplayParameter parameters = getParameters(log, anet, eventClassifier);
		// get the replayer for this, but it also depends on the parameters... we need
		// to find another word for this.

		final IPNReplayAlgorithm replayEngine = getReplayer();

		final PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(PNLogReplayer.class);

		//
		mapping = constructMapping(log, anet.getNet(), eventClassifier, evClassDummy);

		final PNRepResult result = replayEngine.replayLog(pluginContext, anet.getNet(), log, mapping, parameters);
		System.out.println("Replay result size : " + result.size());
		// after we have those result, we need to output the
		// a table include the fitness statistics information, one is alignment PO
		final BufferedDataTable bt = createInfoTable(result.getInfo(), exec);

		repResultPO = new RepResultPortObject(result, logPO);
		// alignPO.setRepResult(result);
		logger.info("End: PNReplayer Conformance Checking");
		return new PortObject[] { bt, repResultPO };
	}

	public static BufferedDataTable createInfoTable(final Map<String, Object> info, final ExecutionContext exec) {

		final DataColumnSpec[] cSpec = new DataColumnSpec[2];
		cSpec[0] = new DataColumnSpecCreator("Type", StringCell.TYPE).createSpec();
		cSpec[1] = new DataColumnSpecCreator("Value", DoubleCell.TYPE).createSpec();

		final DataTableSpec tSpec = new DataTableSpec(cSpec);
		// can not define the name for this table
		final BufferedDataContainer buf = exec.createDataContainer(tSpec);
		int i = 0;
		for (final String key : info.keySet()) {
			final Double value = (Double) info.get(key);

			final DataCell[] currentRow = new DataCell[2];
			currentRow[0] = new StringCell(key);
			currentRow[1] = new DoubleCell(value);
			buf.addRowToTable(new DefaultRow(i + "", currentRow));
			i++;
		}
		buf.close();
		final BufferedDataTable bt = buf.getTable();

		return bt;
	}

	public static TransEvClassMapping constructMapping(final XLog log, final Petrinet net,
			final XEventClassifier eventClassifier, final XEventClass dummyEvent) {
		final TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvent);

		final XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);

		for (final Transition t : net.getTransitions()) {
			for (final XEventClass evClass : summary.getEventClasses().getClasses()) {
				final String id = evClass.getId();

				if (t.getLabel().equals(id)) {
					mapping.put(t, evClass);
					break;
				}
			}

		}

		return mapping;
	}

	public XLogPortObject getLogPO() {
		return logPO;
	}

	public void setLogPO(final XLogPortObject logPO) {
		this.logPO = logPO;
	}

	public PetriNetPortObject getNetPO() {
		return netPO;
	}

	public void setNetPO(final PetriNetPortObject netPO) {
		this.netPO = netPO;
	}

	public TransEvClassMapping getMapping() {
		return mapping;
	}

	public void setMapping(final TransEvClassMapping mapping) {
		this.mapping = mapping;
	}

	public RepResultPortObject getRepResultPO() {
		return repResultPO;
	}

	public void setRepResultPO(final RepResultPortObject repResultPO) {
		this.repResultPO = repResultPO;
	}

	private IPNReplayAlgorithm getReplayer() {
		// TODO according to dialog parameter, create petri net replayer
		// currently we only has two types
		IPNReplayAlgorithm replayEngine = null;
		if (m_strategy.getStringValue().equals(strategyList[0])) {
			replayEngine = new PetrinetReplayerWithILP();
		} else if (m_strategy.getStringValue().equals(strategyList[1])) {
			replayEngine = new PetrinetReplayerWithoutILP();
		}

		return replayEngine;
	}

	private IPNReplayParameter getParameters(final XLog log, final AcceptingPetriNet anet,
			final XEventClassifier eventClassifier) {
		// how to create a table to assign such values here??
		// if many event classes are available here??

		final XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		final Collection<XEventClass> eventClasses = logInfo.getEventClasses().getClasses();

		// here we need to add cost values here, if we have default values htere
//		IPNReplayParameter parameters = new CostBasedCompleteParam(eventClasses,
//				evClassDummy, anet.getNet().getTransitions(), 2, 5);
//
		// set all cost here
		final IPNReplayParameter parameters = createCostParameter(eventClasses, anet.getNet().getTransitions());
		parameters.setInitialMarking(anet.getInitialMarking());
		// here cast needed to transfer from Set<Marking> to Marking[]
		final Marking[] fmList = new Marking[anet.getFinalMarkings().size()];
		int i = 0;
		for (final Marking m : anet.getFinalMarkings())
			fmList[i++] = m;

		parameters.setFinalMarkings(fmList);

		parameters.setGUIMode(false);
		parameters.setCreateConn(false);

		return parameters;
	}

	private IPNReplayParameter createCostParameter(final Collection<XEventClass> eventClasses,
			final Collection<Transition> tCol) {

		// put dummy transition into the map
		final Map<Transition, Integer> mapTrans2Cost = new HashMap();
		final Map<XEventClass, Integer> mapEvClass2Cost = new HashMap();
		final Map<Transition, Integer> mapSync2Cost = new HashMap();

		final int[] rowIndices = m_costSettings.getRowIndices();
		final int[] colIndices = m_costSettings.getColumnIndices();
		final String[] valueList = m_costSettings.getValues();

		// recover the setting, change the names from string to EventClass or Transition
		for (int i = 0; i < rowIndices.length; i += 2) {
			// currently, rowIdx=rowIdx = 0, colIdx=0, colIdx = 1, values = A,1;
			switch (colIndices[i]) {
			case 0: // the first model move name
				final String transitionName = valueList[i];
				// find the corresponding event class
				final Transition t = findTransition(transitionName, tCol);

				final int mmCost = Integer.valueOf(valueList[i + 1]);
				mapTrans2Cost.put(t, mmCost);

				break;
			case 2: // log move name and cost

				final String eventName = valueList[i];
				// find the corresponding event class
				final XEventClass eClass = findEventClass(eventName, eventClasses);

				final int lmCost = Integer.valueOf(valueList[i + 1]);
				mapEvClass2Cost.put(eClass, lmCost);
				break;
			case 4:
				final String stName = valueList[i];
				// find the corresponding event class
				final Transition st = findTransition(stName.split(" :")[0], tCol);

				final int smCost = Integer.valueOf(valueList[i + 1]);
				mapSync2Cost.put(st, smCost);
				break;
			default:
				System.out.println("Other situation exists");
			}

		}

		return new CostBasedCompleteParam(mapEvClass2Cost, mapTrans2Cost, mapSync2Cost);

	}

	static Transition findTransition(final String transitionName, final Collection<Transition> tCol) {
		// TODO given transition name, we can find the transition in net

		for (final Transition t : tCol) {
			if (transitionName.equals(t.getLabel()))
				return t;

		}

		return null;
	}

	static XEventClass findEventClass(final String eventName, final Collection<XEventClass> eventClasses) {
		// TODO Auto-generated method stub
		for (final XEventClass eClass : eventClasses) {
			if (eventName.equals(eClass.getId()))
				return eClass;
		}
		if (eventName.equals(evClassDummy.getId()))
			return evClassDummy;

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void reset() {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs) throws InvalidSettingsException {

		if (!inSpecs[INPORT_LOG].getClass().equals(XLogPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid event log!");

		if (!inSpecs[INPORT_PETRINET].getClass().equals(PetriNetPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid Petri net!");

		final RepResultPortObjectSpec aSpec = new RepResultPortObjectSpec();
		return new PortObjectSpec[] { null, aSpec };
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		// TODO: generated method stub
		m_strategy.saveSettingsTo(settings);
		m_classifierName.saveSettingsTo(settings);

		m_costSettings.saveSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO: generated method stub
		m_strategy.loadSettingsFrom(settings);
		m_classifierName.loadSettingsFrom(settings);

		m_costSettings.loadSettings(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO: generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO: generated method stub
	}

	// implement the event classifier interface to get the event classifier
	// implementation
	@Override
	public XEventClassifier getXEventClassifier() {
		// TODO Auto-generated method stub
		for (final XEventClassifier clf : classifierList) {
			if (clf.name().equals(m_classifierName.getStringValue()))
				return clf;
		}
		return null;
	}

}
