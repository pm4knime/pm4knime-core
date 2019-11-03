package org.pm4knime.node.replayer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectHolder;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.portobject.RepResultPortObjectSpec;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.settingsmodel.SMAlignmentReplayParameter;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.ReplayerUtil;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerNoILPRestrictedMoveModel;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.astar.petrinet.manifestreplay.CostBasedCompleteManifestParam;
import org.processmining.plugins.astar.petrinet.manifestreplay.PNManifestFlattener;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

/**
 * <code>NodeModel</code> for the "PNReplayer" node.
 * This node should supports alignment replayer, but accepts different algorithms. 
 * Inputs:
 *    <1> XLog PortObject 
 *    <2> Petri net PortObject
 * Outputs:
 * 	  <1> RepResult PortObject
 * 
 * Parameters: 
 *   according to different algorithm, we need different parameters generated from the parameters
 *   to replayer node. 
 *   
 *    -- Normal conformance checking
 *    -- Mainfest replay for performance 
 *       ++ This is separate from the other replayer parameters,.we need different parameter to deal with it!! 
 *       ++ do it in the SMPerformanceParameter to generate the real parameters for its use. 
 *    -- ETC replay for precision
 *   
 * @refer The old codes in conformance check. It allows mroe algorithms. But should give the types in spec to avoid the 
 * unwanted result there. In later use. 
 * 
 *
 * @author 
 */
public class DefaultPNReplayerNodeModel extends NodeModel implements PortObjectHolder{
	private static final NodeLogger logger = NodeLogger.getLogger(DefaultPNReplayerNodeModel.class);
	
	protected static final int INPORT_LOG = 0;
	protected static final int INPORT_PETRINET = 1;
	
	static List<XEventClassifier> classifierList = XLogUtil.getECList();
	public static String CFG_PARAMETER_NAME = "Parameter in Replayer In Default";
	// assign one parameter the same values here 
	SMAlignmentReplayParameter m_parameter;
	// it can't belong to this class
	XEventClass evClassDummy;
	XEventClassifier eventClassifier ; 
	
	
	RepResultPortObject repResultPO;
	RepResultPortObjectSpec m_rSpec ;
    /**
     * Constructor for the node model.
     */
    protected DefaultPNReplayerNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { XLogPortObject.TYPE, PetriNetPortObject.TYPE }, new PortType[] {RepResultPortObject.TYPE });
    	evClassDummy = new XEventClass("dummy", 1);
    	// need to initialize the parameters later, because it has different types there.
    	// adjust childclass 
    	initializeParameter();
    }

    protected void initializeParameter() {
    	m_parameter = new SMAlignmentReplayParameter(CFG_PARAMETER_NAME);
    	
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

    	logger.info("Start: Unified PNReplayer");
    	XLogPortObject logPO = (XLogPortObject) inData[INPORT_LOG];
    	PetriNetPortObject netPO = (PetriNetPortObject) inData[INPORT_PETRINET];
    	XLog log = logPO.getLog();
    	AcceptingPetriNet anet = netPO.getANet();
    	
    	
    	XEventClassifier eventClassifier = XLogUtil.getXEventClassifier(m_parameter.getMClassifierName().getStringValue(), classifierList);
    	
    	// mapping is not in performance checker... 
    	// need to set it private
    	// according to the different types strategy there. 
    	// if the strategy belongs to the first two, we use this, else, we use different ones.. 
    	PNRepResult repResult = null;
    	
    	String strategyName = m_parameter.getMStrategy().getStringValue();
    	// for conformance checking
    	IPNReplayAlgorithm replayAlgorithm = null ;
    	
    	// for performance only
    	if(strategyName.equals(ReplayerUtil.strategyList[2])) {
    		replayAlgorithm = new PetrinetReplayerNoILPRestrictedMoveModel();
    		
    		// different parameters need different get parameter methods. We need to go back to replayer node
    		PNManifestReplayerParameter manifestParameters = m_parameter.getPerfParameter(log, anet, eventClassifier);
    		
    		PNManifestFlattener flattener = new PNManifestFlattener(anet.getNet(), manifestParameters);
    		CostBasedCompleteManifestParam parameter = new CostBasedCompleteManifestParam(flattener.getMapEvClass2Cost(),
    				flattener.getMapTrans2Cost(), flattener.getMapSync2Cost(),
    				flattener.getInitMarking(), flattener.getFinalMarkings(), manifestParameters.getMaxNumOfStates(),
    				flattener.getFragmentTrans());
    				
    		parameter.setGUIMode(false);
    		parameter.setCreateConn(false);
    		
    		PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
    				.getFutureResultAwarePluginContext(PNLogReplayer.class);
    		PNLogReplayer replayer = new PNLogReplayer();
    		repResult = replayer.replayLog(pluginContext, anet.getNet(), log, flattener.getMap(),
    				replayAlgorithm, parameter);
    		
    	}else {
    		// for conformance 
	    	if(strategyName.equals(ReplayerUtil.strategyList[0]) ) {
	    		replayAlgorithm = new PetrinetReplayerWithILP();
	    	}else if(strategyName.equals(ReplayerUtil.strategyList[1])) {
	    		replayAlgorithm = new PetrinetReplayerWithoutILP();
	    	}
	    	
	    	TransEvClassMapping mapping = PetriNetUtil.constructMapping(log, anet.getNet(), eventClassifier, evClassDummy);
	    	IPNReplayParameter parameters =  m_parameter.getConfParameter(log, anet, eventClassifier, evClassDummy);
	    	PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
					.getFutureResultAwarePluginContext(PNLogReplayer.class);
	    	
	    	repResult = replayAlgorithm.replayLog(pluginContext, anet.getNet(), log, mapping, parameters);
    	}
    	
    	
		repResultPO = new RepResultPortObject(repResult, log, anet);
		m_rSpec.setMParameter(m_parameter);
		repResultPO.setSpec(m_rSpec);
		logger.info("End: Default PNReplayer for "+ strategyName);
		return new PortObject[]{repResultPO};
    }

    
    public RepResultPortObject getRepResultPO() {
		return repResultPO;
	}
    
    XEventClassifier getXEventClassifier() {
    	return eventClassifier;
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
		
		m_rSpec = new RepResultPortObjectSpec();
		// one question, how to add the type information here to make them valid at first step??
		return new PortObjectSpec[]{ m_rSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	m_parameter.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    	m_parameter.loadSettingsFrom(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }
    
    @Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] { repResultPO};
	}

	/*
	 * It defines methods to restore port objects. It is called after execution. 
	 * because the view is based on the input and output port objects, so an internal view is in need.
	 * Serialization is controlled by KNIME platform.
	 */
	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		// TODO Auto-generated method stub
		
		repResultPO = (RepResultPortObject) portObjects[0];
	}

	@Override
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}

}

