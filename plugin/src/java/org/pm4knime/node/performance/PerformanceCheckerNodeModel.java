package org.pm4knime.node.performance;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.portobject.RepResultPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.settingsmodel.SMPerformanceParameter;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerNoILPRestrictedMoveModel;
import org.processmining.plugins.astar.petrinet.manifestreplay.CostBasedCompleteManifestParam;
import org.processmining.plugins.astar.petrinet.manifestreplay.ManifestFactory;
import org.processmining.plugins.astar.petrinet.manifestreplay.PNManifestFlattener;
import org.processmining.plugins.manifestanalysis.visualization.performance.IPerfCounter;
import org.processmining.plugins.manifestanalysis.visualization.performance.PerfCounter;
import org.processmining.plugins.manifestanalysis.visualization.performance.ReliablePerfCounter;
import org.processmining.plugins.manifestanalysis.visualization.performance.TimeFormatter;
import org.processmining.plugins.petrinet.manifestreplayer.EvClassPattern;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.manifestreplayer.TransClass2PatternMap;
import org.processmining.plugins.petrinet.manifestreplayer.conversion.Manifest2PNRepResult;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClasses;
import org.processmining.plugins.petrinet.manifestreplayresult.Manifest;
import org.processmining.plugins.petrinet.manifestreplayresult.ManifestEvClassPattern;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

/**
 * <code>NodeModel</code> for the "PerformanceChecker" node. 
 * Input: one XLogPortObject + PetriNetPortObject
 * Output:
 *    -- Alignment PortObject but it doesn't matter actually
 *    -- statistics information in three output tables, 
 *    		one for the global, 
 *    			-- @reference org.processmining.plugins.manifestanalysis.
 *    							visualization.performance.ManifestCaseStatPanel#showAllStats
 *    				
 *    		one is for transitions, 
 *    			-- @reference org.processmining.plugins.manifestanalysis.
 *                                 visualization.performance.ManifestElementStatPanel#showTransStats
 *    		one for source
 *    			-- @reference org.processmining.plugins.manifestanalysis.
 *                                 visualization.performance.ManifestElementStatPanel#showPlaceStats
 *    -- One view to show the Analysis result 
 *    -- No need to show it here:: one view to show the time between transitions But only the views there,
 *    or do we need another table to output it here??
 * 
 * Process: following the ones like ConformanceChecking and get the information there; but one stuff,
 * we don't want to popup too many things. avoid it if we can
 * @author Kefang Ding
 * @reference https://svn.win.tue.nl/repos/prom/Packages/PNetReplayer/Trunk/src/org/processmining/plugins/petrinet/manifestreplayer/PNManifestReplayer.java
 *     + https://github.com/rapidprom/rapidprom-source/blob/master/src/main/java/org/rapidprom/operators/conformance/PerformanceConformanceAnalysisOperator.java
 */
public class PerformanceCheckerNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(PerformanceCheckerNodeModel.class);
	protected static final int INPORT_LOG = 0;
	protected static final int INPORT_PETRINET = 1;
	
	static String[] strategyList = {"A*-ILP Based manifest replay"};
	static List<XEventClassifier> classifierList = XLogUtil.getECList();
	// we create a similar nodeSetting like Conformance Checking?
	SMPerformanceParameter m_parameter;
	private XLogPortObject logPO ;
	private RepResultPortObject repResultPO;
	private PetriNetPortObject netPO ;
	private Manifest  mResult;
	private PerfCounter counter;
	
    protected PerformanceCheckerNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { XLogPortObject.TYPE, PetriNetPortObject.TYPE }, new PortType[] {
    			RepResultPortObject.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE, BufferedDataTable.TYPE});
    	m_parameter = new SMPerformanceParameter("Performance Parameter");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: Return a BufferedDataTable for each output port 
    	logger.info("Start: ManifestReplayer Performance Checking");
    	logPO = (XLogPortObject) inData[INPORT_LOG];
		netPO = (PetriNetPortObject) inData[INPORT_PETRINET];
    	
		XLog log = logPO.getLog();
		AcceptingPetriNet anet = netPO.getANet();
    	
		XEventClassifier eventClassifier = getXEventClassifier();
		
		PNManifestReplayerParameter manifestParameters = getParameters(log, anet, eventClassifier);
		
		PNManifestFlattener flattener = new PNManifestFlattener(anet.getNet(), manifestParameters);
		
		CostBasedCompleteManifestParam parameter = new CostBasedCompleteManifestParam(flattener.getMapEvClass2Cost(),
				flattener.getMapTrans2Cost(), flattener.getMapSync2Cost(),
				flattener.getInitMarking(), flattener.getFinalMarkings(), manifestParameters.getMaxNumOfStates(),
				flattener.getFragmentTrans());
				
		parameter.setGUIMode(false);
		parameter.setCreateConn(false);
		
		// we can choose the values here; if it is restricited here
		// get the replayer for this algorithm
		PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(PNLogReplayer.class);
		PNLogReplayer replayer = new PNLogReplayer();
		PetrinetReplayerNoILPRestrictedMoveModel replayAlgorithm = new PetrinetReplayerNoILPRestrictedMoveModel();
		
		
		PNRepResult alignment = replayer.replayLog(pluginContext, flattener.getNet(), log, flattener.getMap(),
				replayAlgorithm, parameter);
		mResult = ManifestFactory.construct(anet.getNet(), manifestParameters.getInitMarking(),
				manifestParameters.getFinalMarkings(), log, flattener, alignment,
				manifestParameters.getMapping());

		// based on the result, we get other information listed here
		PNRepResult pnRepResult = Manifest2PNRepResult.convert(mResult);
		
		repResultPO = new RepResultPortObject(pnRepResult, log, anet);
		
		
		// global statistics information. It includes all the performance info, the whole process
		// we need one view to show the result here
		if(m_parameter.isMWithSynMove().getBooleanValue()) {
			counter = new ReliablePerfCounter();
        }else
        	counter = new PerfCounter();
		
		
		PerfCheckerInfoAssistant infoAssistant = new PerfCheckerInfoAssistant(m_parameter, mResult, counter);
		
    	DataTableSpec gSpec = createGlobalStatsTableSpec();
    	BufferedDataContainer gBuf = exec.createDataContainer(gSpec);
    	// here to fill the values from result to gBuf
    	infoAssistant.fillGlobalData(gBuf);
    	
    	// create one for transition, one for place there
    	DataTableSpec tSpec = createElemenentStatsTableSpec("Transition");
    	BufferedDataContainer tBuf = exec.createDataContainer(tSpec);
    	infoAssistant.fillTransitionData(tBuf, anet.getNet().getTransitions());
    	
    	DataTableSpec pSpec = createElemenentStatsTableSpec("Place");
    	BufferedDataContainer pBuf = exec.createDataContainer(pSpec);
    	infoAssistant.fillPlaceData(pBuf, anet.getNet().getPlaces());
    	
    	gBuf.close();
    	tBuf.close();
    	pBuf.close();
    	logger.info("End: ManifestReplayer Performance Evaluation");
        return new PortObject[]{repResultPO, gBuf.getTable(), tBuf.getTable(), pBuf.getTable()};
    }

	public Manifest getMainfestResult() {
		return mResult;
	}
	public SMPerformanceParameter getMParameter() {
		return m_parameter;
	}

	

	/**
     * this method create a table for the element statistics info. It can be used for transitions,
     * but also for the places. But how to get this?? We should have columnClassifier
     * 
     * From the parameters, we could create a table spec from it
     * @return
     */
    private DataTableSpec createElemenentStatsTableSpec(String itemColName) {
    	// here we need to change the table spec according to the places 
    	String[] columnNames = {itemColName, "Property", "Min.", "Max.", "Avg.", "Std. Dev", "Freq."};
    	DataType[] columnTypes ={StringCell.TYPE, StringCell.TYPE, StringCell.TYPE,StringCell.TYPE, StringCell.TYPE, StringCell.TYPE, StringCell.TYPE};
    	DataTableSpec tSpec = new DataTableSpec(itemColName + " Table", columnNames, columnTypes);
    	return tSpec;
    }
    /**
     * there is one global table for this, so 
     */
    private DataTableSpec createGlobalStatsTableSpec() {
    	String[] columnNames = { "Case Property", "Value"};
    	DataType[] columnTypes ={StringCell.TYPE, StringCell.TYPE};
    	DataTableSpec tSpec = new DataTableSpec( "Global Performance Statistics Table", columnNames, columnTypes);
    	return tSpec;
    }
    
    XEventClassifier getXEventClassifier() {
		// TODO Auto-generated method stub
    	for(XEventClassifier clf : classifierList) {
 			if(clf.name().equals(m_parameter.getMClassifierName().getStringValue()))
 				return clf;
 		}
 		return null;
	}
    
    private PNManifestReplayerParameter getParameters(XLog log, AcceptingPetriNet anet, XEventClassifier eventClassifier ) {
    	// how to create a table to assign such values here?? 
		// if many event classes are available here?? 
    	
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		Collection<XEventClass> eventClasses =  logInfo.getEventClasses().getClasses();
		
		// here we need to add cost values here, if we have default values htere
//		IPNReplayParameter parameters = new CostBasedCompleteParam(eventClasses,
//				evClassDummy, anet.getNet().getTransitions(), 2, 5);
//		

		PNManifestReplayerParameter parameters = new PNManifestReplayerParameter();
		//TODO : assign a better value here
		parameters.setMaxNumOfStates(1000);
		
		// get the pattern map for transition & event classes 
		TransClasses tc = new TransClasses(anet.getNet());
		Map<TransClass, Set<EvClassPattern>> pattern = buildPattern(tc, eventClasses);
		TransClass2PatternMap mapping = new TransClass2PatternMap(log, anet.getNet(), eventClassifier, tc, pattern);
		parameters.setMapping(mapping);
		
		// set the move cost
		int lmCost = m_parameter.getMDefaultCosts()[0].getIntValue();
		Map<XEventClass, Integer> mapLMCost = buildLMCostMap(eventClasses, lmCost);
		int mmCost = m_parameter.getMDefaultCosts()[1].getIntValue();
		Map<TransClass, Integer> mapTMCost = buildTMCostMap(tc, mmCost);
		int smCost = m_parameter.getMDefaultCosts()[2].getIntValue();
		Map<TransClass, Integer> mapSMCost = buildTMCostMap(tc, smCost);
		
		parameters.setMapEvClass2Cost(mapLMCost);
		parameters.setTrans2Cost(mapTMCost);
		parameters.setTransSync2Cost(mapSMCost);
		
		
		parameters.setInitMarking(anet.getInitialMarking());
		Marking[] fmList = new Marking[anet.getFinalMarkings().size()];
		int i = 0;
		for(Marking m : anet.getFinalMarkings())
			fmList[i++] = m;
		
		parameters.setFinalMarkings(fmList);
		
		parameters.setGUIMode(false);
		
    	return parameters;
    } 
    
    private Map<TransClass, Integer> buildTMCostMap(TransClasses tc, int cost) {
		// TODO Auto-generated method stub
    	Map<TransClass, Integer> map= new HashMap<TransClass, Integer>();
		
		for (TransClass c : tc.getTransClasses()) {
			map.put(c, cost);
		}
		return map;
	}

	private Map<XEventClass, Integer> buildLMCostMap(Collection<XEventClass> eventClasses, int cost) {
		// TODO Auto-generated method stub
		Map<XEventClass, Integer> map= new HashMap<XEventClass, Integer>();
		for (XEventClass c : eventClasses) {
			map.put(c, cost);
		}
		
		// TODO : set dummy event class here and assign it value
		return map;
	}

	private Map<TransClass, Set<EvClassPattern>> buildPattern(TransClasses tc, Collection<XEventClass> eventClasses) {
    	
    	Map<TransClass, Set<EvClassPattern>> pattern = new HashMap<TransClass, Set<EvClassPattern>>();
    	
    	for (TransClass t : tc.getTransClasses()) {
			Set<EvClassPattern> p = new HashSet<EvClassPattern>();
			line: for (XEventClass clazz : eventClasses)
				// look for exact matches on the id
				if (clazz.getId().equals(t.getId())) {
					EvClassPattern pat = new EvClassPattern();
					pat.add(clazz);
					p.add(pat);
					pattern.put(t, p);
					break line;
				}

		}
    	return pattern;
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
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

    	if (!inSpecs[INPORT_LOG].getClass().equals(XLogPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid event log!");

		if (!inSpecs[INPORT_PETRINET].getClass().equals(PetriNetPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid Petri net!");
		
		// because we are not sure about the tableSpec, so we set it null
		RepResultPortObjectSpec aSpec = new RepResultPortObjectSpec();
        return new PortObjectSpec[]{aSpec, null, null, null };
    	
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
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
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
		
	}
	

}

