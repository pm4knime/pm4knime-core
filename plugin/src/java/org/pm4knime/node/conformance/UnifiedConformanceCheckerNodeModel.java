package org.pm4knime.node.conformance;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
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
 * <code>NodeModel</code> for the "UnifiedConformanceChecker" node. It is
 * designed for workflow in loop without the option from the event log and Petri
 * net.
 *
 * @author Kefang Ding
 */
public class UnifiedConformanceCheckerNodeModel extends NodeModel {

	private static final NodeLogger logger = NodeLogger.getLogger(ConformanceCheckerNodeModel.class);

	protected static final int INPORT_LOG = 0;
	protected static final int INPORT_PETRINET = 1;

	private XLogPortObject logPO;
	private PetriNetPortObject netPO;
	private TransEvClassMapping mapping;

	RepResultPortObject repResultPO;
	final static XEventClass evClassDummy = new XEventClass("dummy", 1);

	public static final String CFGKEY_STRATEGY_TYPE = "Strategy type";
	public static final String[] strategyList = { "ILP Replayer", "non-ILP Replayer" };
	SettingsModelString m_strategy = createStrategyModel();

	List<XEventClassifier> classifierList;
	public List<String> classifierNames;
	SettingsModelString m_classifierName = createClassifierNameModel();

	// we add the default three move costs, also three list to store them
	public static final int CFG_COST_TYPE_NUM = 3;
	public static final String[] CFG_MCOST_KEY = { "model move cost", "log move cost", "sync move cost" };
	public static final int[] CFG_DEFAULT_MCOST = { 1, 1, 0 };
	private final SettingsModelIntegerBounded[] m_defaultCostModels;

	protected UnifiedConformanceCheckerNodeModel() {

		// TODO: Specify the types of input and output PortObject
		super(new PortType[] { XLogPortObject.TYPE, PetriNetPortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE, PortObject.TYPE });
		initializeClassifier();
		m_defaultCostModels = initializeCostModels();
	}

	public static SettingsModelIntegerBounded[] initializeCostModels() {
		// TODO Auto-generated method stub
		final SettingsModelIntegerBounded[] defaultCostModels = new SettingsModelIntegerBounded[CFG_COST_TYPE_NUM];
		int i = 0;
		for (; i < CFG_COST_TYPE_NUM; i++) {
			defaultCostModels[i] = new SettingsModelIntegerBounded(CFG_MCOST_KEY[i], CFG_DEFAULT_MCOST[i], 0,
					Integer.MAX_VALUE);
		}

		return defaultCostModels;
	}

	private void initializeClassifier() {
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

		logger.info("Start: Unified PNReplayer Conformance Checking");
		logPO = (XLogPortObject) inData[INPORT_LOG];
		netPO = (PetriNetPortObject) inData[INPORT_PETRINET];

		final XLog log = logPO.getLog();
		// to replay the net, we need marking there
		final AcceptingPetriNet anet = netPO.getANet();
		// the default costs array, we need to make them for sure
		final int[] defaultCosts = { m_defaultCostModels[0].getIntValue(), m_defaultCostModels[1].getIntValue(),
				m_defaultCostModels[2].getIntValue() };
		final XEventClassifier eventClassifier = getXEventClassifier();
		final IPNReplayParameter parameters = getParameters(log, anet, eventClassifier, defaultCosts);
		// get the replayer for this, but it also depends on the parameters... we need
		// to find another word for this.

		final IPNReplayAlgorithm replayEngine = getReplayer();

		final PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(PNLogReplayer.class);

		mapping = ConformanceCheckerNodeModel.constructMapping(log, anet.getNet(), eventClassifier, evClassDummy);

		final PNRepResult result = replayEngine.replayLog(pluginContext, anet.getNet(), log, mapping, parameters);
		System.out.println("Replay result size : " + result.size());
		// after we have those result, we need to output the
		// a table include the fitness statistics information, one is alignment PO
		// the order has changed, we should define the unified model at first, and then
		// the customized model
		// but now, next time think simpler at first
		final BufferedDataTable bt = ConformanceCheckerNodeModel.createInfoTable(result.getInfo(), exec);

		repResultPO = new RepResultPortObject(result, logPO);
		// alignPO.setRepResult(result);
		logger.info("End: Unified PNReplayer Conformance Checking");
		return new PortObject[] { bt, repResultPO };
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
			final XEventClassifier eventClassifier, final int[] defaultCosts) {
		// how to create a table to assign such values here??
		// if many event classes are available here??

		final XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		final Collection<XEventClass> eventClasses = logInfo.getEventClasses().getClasses();

		// here we need to add cost values here, if we have default values htere
		final IPNReplayParameter parameters = new CostBasedCompleteParam(eventClasses, evClassDummy,
				anet.getNet().getTransitions(), defaultCosts[0], defaultCosts[1]);

		// no option to set the synCost for the parameter

		// set all cost here
		// IPNReplayParameter parameters = createCostParameter(eventClasses,
		// anet.getNet().getTransitions());
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
		m_strategy.saveSettingsTo(settings);
		m_classifierName.saveSettingsTo(settings);

		for (int i = 0; i < CFG_COST_TYPE_NUM; i++) {
			m_defaultCostModels[i].saveSettingsTo(settings);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_strategy.loadSettingsFrom(settings);
		m_classifierName.loadSettingsFrom(settings);
		for (int i = 0; i < CFG_COST_TYPE_NUM; i++) {
			m_defaultCostModels[i].loadSettingsFrom(settings);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		// here we don't have many comments
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
	public XEventClassifier getXEventClassifier() {
		// TODO Auto-generated method stub
		for (final XEventClassifier clf : classifierList) {
			if (clf.name().equals(m_classifierName.getStringValue()))
				return clf;
		}
		return null;
	}

	static SettingsModelString createClassifierNameModel() {
		return new SettingsModelString(XEventClassifierInterface.CKF_KEY_EVENT_CLASSIFIER, "Event Name");
	}

	static SettingsModelString createStrategyModel() {
		return new SettingsModelString(CFGKEY_STRATEGY_TYPE, strategyList[0]);
	}
}
