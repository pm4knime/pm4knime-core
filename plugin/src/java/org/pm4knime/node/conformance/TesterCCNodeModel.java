package org.pm4knime.node.conformance;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
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
import org.pm4knime.util.XLogUtil;
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
 * <code>NodeModel</code> for the "TesterCC" node.
 *
 * @author 
 */
public class TesterCCNodeModel extends NodeModel implements PortObjectHolder{
	protected static final int INPORT_LOG = 0;
	protected static final int INPORT_PETRINET = 1;
	
	// will this change due to log?? Here no!!
	static List<XEventClassifier> classifierList = XLogUtil.getECList();
	static String[] strategyList = {"ILP Replayer","non-ILP Replayer"};
	
	private static final NodeLogger logger = NodeLogger.getLogger(TesterCCNodeModel.class);
	
	// assign one parameter the same values here 
	SMAlignmentReplayParameter m_parameter;
	// it can't belong to this class
	XEventClass evClassDummy;
	
	XLogPortObject logPO;
	PetriNetPortObject netPO ;
	TransEvClassMapping mapping;
	RepResultPortObject repResultPO;
	private DataTableSpec m_tSpec;
	/**
     * Constructor for the node model.
     */
    protected TesterCCNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { XLogPortObject.TYPE, PetriNetPortObject.TYPE }, new PortType[] {BufferedDataTable.TYPE, RepResultPortObject.TYPE });
    	evClassDummy = new XEventClass("dummy", 1);
    	initializeParameter();
    }

    protected void initializeParameter() {
    	m_parameter = new SMAlignmentReplayParameter("Parameter in Tester");
    	
    }
     
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {

        // TODO: Return a BufferedDataTable for each output port
    	logger.info("Start: Unified PNReplayer Conformance Checking");
    	logPO = (XLogPortObject) inData[INPORT_LOG];
    	netPO = (PetriNetPortObject) inData[INPORT_PETRINET];
    	XLog log = logPO.getLog();
    	AcceptingPetriNet anet = netPO.getANet();
    	
    	
    	XEventClassifier eventClassifier = getXEventClassifier();
    	mapping = PetriNetUtil.constructMapping(log, anet.getNet(), eventClassifier, evClassDummy);
		
    	IPNReplayParameter parameters =  getParameters(log, anet, eventClassifier, evClassDummy);
    	IPNReplayAlgorithm replayEngine = getReplayer();
    	
    	
    	PluginContext pluginContext = PM4KNIMEGlobalContext.instance()
				.getFutureResultAwarePluginContext(PNLogReplayer.class);
    	// mapping are in need with dummy event class
		PNRepResult result = replayEngine.replayLog(pluginContext, anet.getNet(), log, mapping, parameters);
		System.out.println("Replay result size : " + result.size());
		BufferedDataTable bt = createInfoTable(result.getInfo(), exec);
		
		repResultPO = new RepResultPortObject(result, logPO, netPO);
		logger.info("End: Unified PNReplayer Conformance Checking");
        return new PortObject[]{bt, repResultPO};
    }

    protected BufferedDataTable createInfoTable(Map<String, Object> info, final ExecutionContext exec) {
    	// we are sure about the spec and create it here
//    	DataColumnSpec[] cSpec = new DataColumnSpec[2];
//    	cSpec[0] = new DataColumnSpecCreator("Type", StringCell.TYPE).createSpec();
//    	cSpec[1] = new DataColumnSpecCreator("Value", DoubleCell.TYPE).createSpec();
//    	
//    	DataTableSpec tSpec = new DataTableSpec(cSpec);
    	// can not define the name for this table 
    	BufferedDataContainer buf = exec.createDataContainer(m_tSpec);
    	int i=0;
    	for(String key : info.keySet()) {
    		Double value = (Double) info.get(key);
    		
    		DataCell[] currentRow = new DataCell[2];
    		currentRow[0] = new StringCell(key);
    		currentRow[1] = new DoubleCell(value);
    		buf.addRowToTable(new DefaultRow(i+"", currentRow));
    		i++;
    	}
    	buf.close();
    	BufferedDataTable bt = buf.getTable();
    	
    	return bt;
    }

    XEventClassifier getXEventClassifier() {
		// TODO Auto-generated method stub
    	for(XEventClassifier clf : classifierList) {
 			if(clf.name().equals(m_parameter.getMClassifierName().getStringValue()))
 				return clf;
 		}
 		return null;
	}

    public PetriNetPortObject getNetPO() {
		return netPO;
	}
    
    public XLogPortObject getLogPO() {
		return logPO;
	}
    
    
    
    public RepResultPortObject getRepResultPO() {
		return repResultPO;
	}
    
	private IPNReplayAlgorithm getReplayer() {
		// TODO Auto-generated method stub
		IPNReplayAlgorithm replayEngine = null;
    	if(m_parameter.getMStrategy().getStringValue().equals(strategyList[0])) {
    		replayEngine = new PetrinetReplayerWithILP();
    	}else if(m_parameter.getMStrategy().getStringValue().equals(strategyList[1])) {
    		replayEngine = new PetrinetReplayerWithoutILP();
    	}
    	
		return replayEngine;
	}

	protected IPNReplayParameter getParameters(XLog log, AcceptingPetriNet anet, XEventClassifier eventClassifier, XEventClass evClassDummy) {
		// TODO Auto-generated method stub
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, eventClassifier);
		Collection<XEventClass> eventClasses =  logInfo.getEventClasses().getClasses();
		
		int[] defaultCosts = new int [3];
		for(int i=0; i< SMAlignmentReplayParameter.CFG_COST_TYPE_NUM; i++){
			defaultCosts[i] = m_parameter.getMDefaultCosts()[i].getIntValue();
		}
		
		// here we also need dummy event class for the cost, but we don't need to share it in another
		// version. 
		IPNReplayParameter parameters = new CostBasedCompleteParam(eventClasses,
				evClassDummy, anet.getNet().getTransitions(), defaultCosts[0], defaultCosts[1]);
		
		parameters.setInitialMarking(anet.getInitialMarking());
		// here cast needed to transfer from Set<Marking> to Marking[]
		Marking[] fmList = new Marking[anet.getFinalMarkings().size()];
		int i = 0;
		for(Marking m : anet.getFinalMarkings())
			fmList[i++] = m;
    	
		parameters.setFinalMarkings(fmList);
    	parameters.setGUIMode(false);
		parameters.setCreateConn(false);
		
    	return parameters;
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

		// here we should give its spec for statistics info
		DataColumnSpec[] cSpec = new DataColumnSpec[2];
    	cSpec[0] = new DataColumnSpecCreator("Type", StringCell.TYPE).createSpec();
    	cSpec[1] = new DataColumnSpecCreator("Value", DoubleCell.TYPE).createSpec();
    	
    	m_tSpec = new DataTableSpec(cSpec);
		
		RepResultPortObjectSpec aSpec = new RepResultPortObjectSpec();
        return new PortObjectSpec[]{m_tSpec ,aSpec};
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
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {
        // TODO: generated method stub
    }

	@Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] { logPO, netPO, repResultPO};
	}

	/*
	 * It defines methods to restore port objects. It is called after execution. 
	 * because the view is based on the input and output port objects, so an internal view is in need.
	 * Serialization is controlled by KNIME platform.
	 */
	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		// TODO Auto-generated method stub
		logPO = (XLogPortObject) portObjects[0];
		netPO = (PetriNetPortObject ) portObjects[1];
		repResultPO = (RepResultPortObject) portObjects[2];
	}

}

