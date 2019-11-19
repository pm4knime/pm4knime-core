package org.pm4knime.node.discovery.ilpminer;

import java.io.File;
import java.io.IOException;

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
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.causalactivitygraph.models.CausalActivityGraph;
import org.processmining.causalactivitygraphcreator.algorithms.ConvertCausalActivityMatrixToCausalActivityGraphAlgorithm;
import org.processmining.causalactivitygraphcreator.algorithms.DiscoverCausalActivityGraphAlgorithm;
import org.processmining.causalactivitygraphcreator.parameters.DiscoverCausalActivityGraphParameters;
import org.processmining.causalactivitymatrix.models.CausalActivityMatrix;
import org.processmining.causalactivitymatrixminer.algorithms.DiscoverFromEventLogAlgorithm;
import org.processmining.causalactivitymatrixminer.parameters.DiscoverFromEventLogParameters;
import org.pm4knime.settingsmodel.SMILPMinerParameter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.hybridilpminer.parameters.XLogHybridILPMinerParametersImpl;
import org.processmining.hybridilpminer.plugins.HybridILPMinerPlugin;
import org.processmining.hybridilpminer.utils.XLogUtils;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;

/**
 * <code>NodeModel</code> for the "ILPMiner" node. 
 * This node is for ILPMiner based on Hybridilpminer in ProM. 
 * Input: 
 * 		event log
 * Output: 
 *   	Petri net (or causal graph?? generated from the values?? We might need it But at which sense?? )
 * Parameter: 
 * 		Normal Options:
 * 			-- event classifier
 * 			-- filter type and threshold :
 * 				  	LP-Filter : sequence-encoding filter, slack variable fiter, none
 * 			-- miner strategy
 * 		Advanced Options: 
 * 			-- LP Objective: unweighted values, weighted values/ relative absolute freq
 *  		-- LP Variable type: two variable per event, one variable per event
 *  		-- Discovery Strategy: mine a place per causal relation, a connection place between each pair
 *   
 * 		In default:
 * 			-- desired resulting net: Petri net
 * 			-- empty after completion
 * 			-- add sink place
 * 			-- use dot to previw graph
 * 
 * Steps:
 * 	1. create dialog only with normal options
 * 	2. code in NodeModel make it work
 * 	4. code in NodeModel..
 * 
 * Notes: I wonder if I could put all the parameters into one big class and follow this strategy for other nodes, too. 
 * The reason for it is to reduce the codes in one nodes. Make it bettter to use this. Other reasons to use other steps for it.
 * All of them is in default mode. SO we can use it 
 * @author Kefang Ding
 */
public class ILPMinerNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger
            .getLogger(ILPMinerNodeModel.class);
	public static String CFG_KEY_ILP_PARAMETER = "ILP Parameter";
	
	SMILPMinerParameter m_parameter; 
    /**
     * Constructor for the node model.
     */
    protected ILPMinerNodeModel() {
    	super(new PortType[] {XLogPortObject.TYPE}, new PortType[] {PetriNetPortObject.TYPE} );
    	
    	m_parameter = new SMILPMinerParameter(ILPMinerNodeModel.CFG_KEY_ILP_PARAMETER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Start : ILPMiner " );
    	XLogPortObject logPO = (XLogPortObject) inData[0];
        XLog log = logPO.getLog();
        
        final String startLabel = "[start>@" + System.currentTimeMillis();
		final String endLabel = "[end]@" + System.currentTimeMillis();
		XLog artifLog = XLogUtil.addArtificialStartAndEnd(log, startLabel, endLabel, m_parameter.getEventClassifier());
        
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
        // create the parameter
		XLogHybridILPMinerParametersImpl param = new XLogHybridILPMinerParametersImpl(context, artifLog);
		m_parameter.setDefaultParameter(param);
		// how to set the current values here for param
		param.setEventClassifier(m_parameter.getEventClassifier());
		
		// set the miner for causality graph 
		DiscoverCausalActivityGraphParameters gParam = new DiscoverCausalActivityGraphParameters(artifLog);
		// set gParam according to the miner type
		gParam.setMiner(m_parameter.getMalgorithm().getStringValue());
		// discover the causal graph
		DiscoverCausalActivityGraphAlgorithm algorithm = new DiscoverCausalActivityGraphAlgorithm();
		CausalActivityGraph cag = algorithm.apply(context, artifLog, gParam);
		param.getDiscoveryStrategy().setCausalActivityGraphParameters(gParam);
    	param.getDiscoveryStrategy().setCausalActivityGraph(cag);
        
    	
    	Object[] result = HybridILPMinerPlugin.discoverWithArtificialStartEnd(context, log, artifLog, param);
        
    	// create the accepting Petri net and PortObject
    	AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1],  (Marking) result[2]);
        PetriNetPortObject pnPO = new PetriNetPortObject(anet);
        
    	logger.info("End : ILPMiner " );
        return new PortObject[]{pnPO};
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

        // generate a new Petri net PortObject to it
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	PetriNetPortObjectSpec pnSpec = new PetriNetPortObjectSpec();
        return new PortObjectSpec[]{pnSpec};
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

}

