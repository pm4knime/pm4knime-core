package org.pm4knime.node.conformance.replayer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttribute;
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
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerNoILPRestrictedMoveModel;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithILP;
import org.processmining.plugins.astar.petrinet.PetrinetReplayerWithoutILP;
import org.processmining.plugins.astar.petrinet.manifestreplay.CostBasedCompleteManifestParam;
import org.processmining.plugins.astar.petrinet.manifestreplay.PNManifestFlattener;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayer;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayerParameter;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
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
public class DefaultPNReplayerNodeModel extends NodeModel{
	private static final NodeLogger logger = NodeLogger.getLogger(DefaultPNReplayerNodeModel.class);
	private static final  String message  = "Replayer In Default";	
	public static String CFG_PARAMETER_NAME = "Parameter In " + message;
	
	protected static final int INPORT_LOG = 0;
	protected static final int INPORT_PETRINET = 1;
	
	SMAlignmentReplayParameter m_parameter;
	// it can't belong to this class
	XEventClass evClassDummy;
	
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

    	logger.info("Start: " + message);
    	String strategyName = m_parameter.getMStrategy().getStringValue();
    	
    	executeWithoutLogger(inData, exec, strategyName);
    	// in greed to output the strategy for replay
		logger.info("End: " + message + " for "+ strategyName);
		return new PortObject[]{repResultPO};
    }

    protected void executeWithoutLogger(final PortObject[] inData,
            final ExecutionContext exec, String strategyName) throws Exception{
      	// extract one method here to allow logger record its current class info
    	// check cancellation of node
    	exec.checkCanceled();
    	
    	XLogPortObject logPO = (XLogPortObject) inData[INPORT_LOG];
    	PetriNetPortObject netPO = (PetriNetPortObject) inData[INPORT_PETRINET];
    	XLog log = logPO.getLog();
    	AcceptingPetriNet anet = netPO.getANet();
    	
    	// here to change the operation on the classifier
    	XEventClassifier eventClassifier = XLogUtil.getEventClassifier(log, m_parameter.getMClassifierName().getStringValue());

    	PNRepResult repResult = null;
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
    				.getFutureResultAwarePluginContext(PNManifestReplayer.class);
    		// check cancellation of node before replaying the result
        	exec.checkCanceled();
    		PNLogReplayer replayer = new PNLogReplayer();
    		repResult = replayer.replayLog(pluginContext, flattener.getNet(), log, flattener.getMap(),
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
	    	// check cancellation of node before replaying the result
        	exec.checkCanceled();
	    	repResult = replayAlgorithm.replayLog(pluginContext, anet.getNet(), log, mapping, parameters);
    	}
    	
    	// put the dummy event class and event classifier in the info table for reuse
    	repResult.addInfo(XLogUtil.CFG_DUMMY_ECNAME, XLogUtil.serializeEventClass(evClassDummy));
    	repResult.addInfo(XLogUtil.CFG_EVENTCLASSIFIER_NAME, m_parameter.getMClassifierName().getStringValue());
    	
    	// check cancellation of node after replaying the result
    	exec.checkCanceled();
    	
		repResultPO = new RepResultPortObject(repResult, log, anet);
		m_rSpec.setMParameter(m_parameter);
		repResultPO.setSpec(m_rSpec);
    }
    
    public RepResultPortObject getRepResultPO() {
		return repResultPO;
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
		m_rSpec.setMParameter(m_parameter);
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
	protected void saveInternals(File nodeInternDir, ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		// TODO Auto-generated method stub
		
	}
    

}

