package org.pm4knime.node.conformance.performance;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.portobject.RepResultPortObjectSpec;
import org.pm4knime.settingsmodel.SMAlignmentReplayParameter;
import org.pm4knime.settingsmodel.SMPerformanceParameter;
import org.pm4knime.util.XLogUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.plugins.astar.petrinet.manifestreplay.ManifestFactory;
import org.processmining.plugins.astar.petrinet.manifestreplay.PNManifestFlattener;
import org.processmining.plugins.manifestanalysis.visualization.performance.PerfCounter;
import org.processmining.plugins.manifestanalysis.visualization.performance.ReliablePerfCounter;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

/**
 * <code>NodeModel</code> for the "PerformanceChecker" node. Input: one
 * XLogPortObject + PetriNetPortObject Output: -- Alignment PortObject but it
 * doesn't matter actually -- statistics information in three output tables, one
 * for the global, -- @reference org.processmining.plugins.manifestanalysis.
 * visualization.performance.ManifestCaseStatPanel#showAllStats
 * 
 * one is for transitions, -- @reference
 * org.processmining.plugins.manifestanalysis.
 * visualization.performance.ManifestElementStatPanel#showTransStats one for
 * source -- @reference org.processmining.plugins.manifestanalysis.
 * visualization.performance.ManifestElementStatPanel#showPlaceStats -- One view
 * to show the Analysis result -- No need to show it here:: one view to show the
 * time between transitions But only the views there, or do we need another
 * table to output it here??
 * 
 * Process: following the ones like ConformanceChecking and get the information
 * there; but one stuff, we don't want to popup too many things. avoid it if we
 * can
 * 
 * @author Kefang Ding
 * @reference https://svn.win.tue.nl/repos/prom/Packages/PNetReplayer/Trunk/src/org/processmining/plugins/petrinet/manifestreplayer/PNManifestReplayer.java
 *            +
 *            https://github.com/rapidprom/rapidprom-source/blob/master/src/main/java/org/rapidprom/operators/conformance/PerformanceConformanceAnalysisOperator.java
 */
public class PerformanceCheckerNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(PerformanceCheckerNodeModel.class);

	// we create a similar nodeSetting like Conformance Checking?
	SMPerformanceParameter m_parameter;
	RepResultPortObjectSpec m_rSpec;
	private Manifest mResult;
	private PerfCounter counter;

	protected PerformanceCheckerNodeModel() {

		// TODO: Specify the amount of input and output ports needed.
		super(new PortType[] { RepResultPortObject.TYPE },
				new PortType[] { BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE });
		m_parameter = new SMPerformanceParameter("Performance Parameter");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected PortObject[] execute(final PortObject[] inData, final ExecutionContext exec) throws Exception {

		// TODO: Return a BufferedDataTable for each output port
		logger.info("Start: ManifestReplayer Performance Checking");
		RepResultPortObject repResultPO = (RepResultPortObject) inData[0];

		XLog log = repResultPO.getLog();
		AcceptingPetriNet anet = repResultPO.getNet();

		PNRepResult repResult = repResultPO.getRepResult();
		// here to syn the transitions from replay result and accepting petri net
//		

		// pass the values of specParameter to m_parameter
		// we can do it when it is in configuration state, or here??
		SMAlignmentReplayParameter specParameter = m_rSpec.getMParameter();
		// for the values specParameter has, we set the needed parameter there
		m_parameter.setClassifierSet(specParameter.getClassifierSet().getStringArrayValue());

		// we can also get the event classifer by checking the map from rSpec, it takes
		// more time..
		XEventClassifier eventClassifier = XLogUtil.getEventClassifier(log,
				specParameter.getMClassifierName().getStringValue());

		PNManifestReplayerParameter manifestParameters = specParameter.getPerfParameter(log, anet, eventClassifier);
		PNManifestFlattener flattener = new PNManifestFlattener(anet.getNet(), manifestParameters);
		// important is the transitions in flattener and replayer result should be the
		// same!!
		// how to make this happen?? After the generation of flattener,
		// it generates a new map during the building process. How to make the ones
		sync(repResult, flattener);

		// problem with mismatch of replay result and flattener.. How to change them??
		// can't change the flattener, then change the ones in replayer result.
		// it takes time, but it is what we need, so what!!

		mResult = ManifestFactory.construct(flattener.getNet(), flattener.getInitMarking(),
				flattener.getFinalMarkings(), log, flattener, repResult, manifestParameters.getMapping());
		// we need to set the classifier for the mResult here

		// global statistics information. It includes all the performance info, the
		// whole process
		// we need one view to show the result here
		if (m_parameter.isMWithSynMove().getBooleanValue()) {
			counter = new ReliablePerfCounter();
		} else
			counter = new PerfCounter();

		PerfCheckerInfoAssistant infoAssistant = new PerfCheckerInfoAssistant(m_parameter, mResult, counter);

		DataTableSpec gSpec = createGlobalStatsTableSpec();
		BufferedDataContainer gBuf = exec.createDataContainer(gSpec);
		// here to fill the values from result to gBuf
		infoAssistant.fillGlobalData(gBuf);

		// create one for transition, one for place there
		DataTableSpec tSpec = createElemenentStatsTableSpec("Transition");
		BufferedDataContainer tBuf = exec.createDataContainer(tSpec);
		infoAssistant.fillTransitionData(tBuf, flattener.getNet().getTransitions());

		DataTableSpec pSpec = createElemenentStatsTableSpec("Place");
		BufferedDataContainer pBuf = exec.createDataContainer(pSpec);
		infoAssistant.fillPlaceData(pBuf, flattener.getNet().getPlaces());

		gBuf.close();
		tBuf.close();
		pBuf.close();
		logger.info("End: ManifestReplayer Performance Evaluation");
		return new PortObject[] { gBuf.getTable(), tBuf.getTable(), pBuf.getTable() };
	}

	private void sync(PNRepResult repResult, PNManifestFlattener flattener) {
		// TODO make the transition in the same transition ids here
		// set a map here to record the connection?? Or, we can reload the nodes by
		// making the transition the same
		Map<Transition, Transition> resToFlattenerMap = new HashMap();

		for (SyncReplayResult alignment : repResult) {

			List<Object> nodeInstances = alignment.getNodeInstance();
			for (int idx = 0; idx < nodeInstances.size(); idx++) {
				Object node = nodeInstances.get(idx);
				if (node instanceof Transition) {
					Transition tInResult = (Transition) node;

					if (!resToFlattenerMap.containsKey(tInResult)) {
						Transition tValue = null;
						for (Transition t : flattener.getNet().getTransitions()) {
							if (tInResult.getLabel().equals(t.getLabel())) {
								tValue = t;
								break;
							}

						}
						resToFlattenerMap.put(tInResult, tValue);
					}

					// node = resToFlattenerMap.get(tInResult);
					nodeInstances.set(idx, resToFlattenerMap.get(tInResult));
				}
			}
		}

	}

	public Manifest getMainfestResult() {
		return mResult;
	}

	public SMPerformanceParameter getMParameter() {
		return m_parameter;
	}

	/**
	 * this method create a table for the element statistics info. It can be used
	 * for transitions, but also for the places. But how to get this?? We should
	 * have columnClassifier
	 * 
	 * From the parameters, we could create a table spec from it
	 * 
	 * @return
	 */
	private DataTableSpec createElemenentStatsTableSpec(String itemColName) {
		// here we need to change the table spec according to the places
		String[] columnNames = { itemColName, "Property", "Min.", "Max.", "Avg.", "Std. Dev", "Freq." };
		DataType[] columnTypes = { StringCell.TYPE, StringCell.TYPE, StringCell.TYPE, StringCell.TYPE, StringCell.TYPE,
				StringCell.TYPE, StringCell.TYPE };
		DataTableSpec tSpec = new DataTableSpec(itemColName + " Performance Table", columnNames, columnTypes);
		return tSpec;
	}

	/**
	 * there is one global table for this, so
	 */
	private DataTableSpec createGlobalStatsTableSpec() {
		String[] columnNames = { "Case Property", "Value" };
		DataType[] columnTypes = { StringCell.TYPE, StringCell.TYPE };
		DataTableSpec tSpec = new DataTableSpec("Global Performance Statistics Table", columnNames, columnTypes);
		return tSpec;
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

		if (!inSpecs[0].getClass().equals(RepResultPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid replay result!");

		// TODO : assign the table spec here
		m_rSpec = (RepResultPortObjectSpec) inSpecs[0];
		return new PortObjectSpec[] { null, null, null };

	}

	public PerfCounter getCounter() {
		return counter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		// TODO: generated method stub
		m_parameter.saveSettingsTo(settings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO: generated method stub
		m_parameter.loadSettingsFrom(settings);
	}

	@Override
	protected void loadInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void validateSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_parameter.validateSettings(settings);
	}

}
