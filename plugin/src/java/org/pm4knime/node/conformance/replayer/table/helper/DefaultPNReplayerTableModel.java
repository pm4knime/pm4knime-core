package org.pm4knime.node.conformance.replayer.table.helper;


import org.knime.core.data.DataTableSpec;
import org.knime.core.data.time.localdatetime.LocalDateTimeCellFactory;
import org.knime.core.data.time.zoneddatetime.ZonedDateTimeCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.conformance.replayer.DefaultPNReplayerNodeModel;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.CostBasedCompleteManifestParamTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.IPNReplayAlgorithmTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PNLogReplayerTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PNManifestFlattenerTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PNManifestReplayerParameterTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PetrinetReplayerILPRestrictedMoveModelTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PetrinetReplayerWithILPTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.PetrinetReplayerWithoutILPTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.SMAlignmentReplayParameterTable;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.TableEventLog;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.TransEvClassMappingTable;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.portobject.RepResultPortObjectSpecTable;
import org.pm4knime.portobject.RepResultPortObjectTable;
import org.pm4knime.settingsmodel.SMTable2XLogConfig;
import org.pm4knime.util.PetriNetUtil;
import org.pm4knime.util.ReplayerUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.manifestreplayer.PNManifestReplayer;
import org.processmining.plugins.petrinet.replayer.PNLogReplayer;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class DefaultPNReplayerTableModel extends DefaultNodeModel{
	private static final NodeLogger logger = NodeLogger.getLogger(DefaultPNReplayerNodeModel.class);
	private static final  String message  = "Replayer In Default";	
	public static String CFG_PARAMETER_NAME = "Parameter In " + message;
	
	protected static final int INPORT_LOG = 0;
	protected static final int INPORT_PETRINET = 1;
	
	SMAlignmentReplayParameterTable m_parameter;
	// it can't belong to this class
	String evClassDummy;
	
	RepResultPortObjectTable repResultPO;
	RepResultPortObjectSpecTable m_rSpec ;
    /**
     * Constructor for the node model.
     */
    protected DefaultPNReplayerTableModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { BufferedDataTable.TYPE, PetriNetPortObject.TYPE }, new PortType[] {RepResultPortObjectTable.TYPE });
    	evClassDummy = "dummy";
    	// need to initialize the parameters later, because it has different types there.
    	initializeParameter();
    }

    protected void initializeParameter() {
    	m_parameter = new SMAlignmentReplayParameterTable(CFG_PARAMETER_NAME);
    	
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
    	checkCanceled(exec);
    	
    	BufferedDataTable logPO = (BufferedDataTable) inData[INPORT_LOG];
    	PetriNetPortObject netPO = (PetriNetPortObject) inData[INPORT_PETRINET];
    	String eventClassifier = m_parameter.getMClassifierName().getStringValue();
    	String traceClassifier = m_parameter.getMClassifierTrace().getStringValue();
    	String timeClassifier = m_parameter.getMClassifierTime().getStringValue();
    	TableEventLog log = new TableEventLog(logPO, eventClassifier, traceClassifier, timeClassifier); 
    	AcceptingPetriNet anet = netPO.getANet();
    	
    	// here to change the operation on the classifier

    	PNRepResult repResult = null;
    	IPNReplayAlgorithmTable replayAlgorithm = null ;
    	
    	// for performance only
    	if(strategyName.equals(ReplayerUtil.strategyList[2])) {
    		//change the calculation with ILP from nonILP mining.
    		replayAlgorithm = new PetrinetReplayerILPRestrictedMoveModelTable();
    		
    		// different parameters need different get parameter methods. We need to go back to replayer node
    		PNManifestReplayerParameterTable manifestParameters = m_parameter.getPerfParameter(log, anet);
    		
    		PNManifestFlattenerTable flattener = new PNManifestFlattenerTable(anet.getNet(), manifestParameters);
    		CostBasedCompleteManifestParamTable parameter = new CostBasedCompleteManifestParamTable(flattener.getMapEvClass2Cost(),
    				flattener.getMapTrans2Cost(), flattener.getMapSync2Cost(),
    				flattener.getInitMarking(), flattener.getFinalMarkings(), manifestParameters.getMaxNumOfStates(),
    				flattener.getFragmentTrans());
    				
    		parameter.setGUIMode(false);
    		parameter.setCreateConn(false);
    		
    		PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
    				.getFutureResultAwarePluginContext(PNManifestReplayer.class);
    		// check cancellation of node before replaying the result
    		checkCanceled(pluginContext, exec);
    		PNLogReplayerTable replayer = new PNLogReplayerTable();
    		repResult = replayer.replayLog(pluginContext, flattener.getNet(), log, flattener.getMap(),
    				replayAlgorithm, parameter);
    		
    	}else {
    		// for conformance 
	    	if(strategyName.equals(ReplayerUtil.strategyList[0]) ) {
	    		replayAlgorithm = new PetrinetReplayerWithILPTable();
	    	}else if(strategyName.equals(ReplayerUtil.strategyList[1])) {
	    		replayAlgorithm = new PetrinetReplayerWithoutILPTable();
	    	}
	    	
	    	TransEvClassMappingTable mapping = PetriNetUtil.constructMapping(log, anet.getNet(), eventClassifier, evClassDummy);
	    	IPNReplayParameter parameters =  m_parameter.getConfParameter(log, anet, eventClassifier, evClassDummy);
	    	PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
					.getFutureResultAwarePluginContext(PNLogReplayer.class);
	    	// check cancellation of node before replaying the result
	    	checkCanceled(pluginContext, exec);
	    	repResult = replayAlgorithm.replayLog(pluginContext, anet.getNet(), log, mapping, parameters);
    	}
    	
    	// put the dummy event class and event classifier in the info table for reuse
    	//repResult.addInfo(XLogUtil.CFG_DUMMY_ECNAME, XLogUtil.serializeEventClass(evClassDummy));
    	//repResult.addInfo(XLogUtil.CFG_EVENTCLASSIFIER_NAME, m_parameter.getMClassifierName().getStringValue());
    	
    	// check cancellation of node after replaying the result
    	checkCanceled(exec);
    	
		repResultPO = new RepResultPortObjectTable(repResult, log, logPO, anet);
		m_rSpec.setMParameter(m_parameter);
		repResultPO.setSpec(m_rSpec);
    }
    
    public RepResultPortObjectTable getRepResultPO() {
		return repResultPO;
	}
    
  
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
    	if (!inSpecs[INPORT_LOG].getClass().equals(DataTableSpec.class))
			throw new InvalidSettingsException("Input is not a valid table log!");
    	

		if (!inSpecs[INPORT_PETRINET].getClass().equals(PetriNetPortObjectSpec.class))
			throw new InvalidSettingsException("Input is not a valid Petri net!");
		
		if(m_parameter.getMClassifierName().getStringValue().isEmpty())
			throw new InvalidSettingsException("Event classifier is not set!");
		
		String timeClass = m_parameter.getMClassifierTime().getStringValue();
		if(timeClass.isEmpty())
			throw new InvalidSettingsException("Time attribute is not set!");
		
		DataTableSpec spec = (DataTableSpec) inSpecs[INPORT_LOG];
		if(!spec.getColumnSpec(timeClass).getType().equals(ZonedDateTimeCellFactory.TYPE))
    		throw new InvalidSettingsException("The time stamp doesn't have the required format in ZonedDateTime!");
		
		m_rSpec = new RepResultPortObjectSpecTable();
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
    
    @Override
    protected void reset() {
    	initializeParameter();
    }
    

}
