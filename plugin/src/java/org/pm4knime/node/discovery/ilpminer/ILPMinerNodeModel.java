package org.pm4knime.node.discovery.ilpminer;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
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
import org.processmining.causalactivitygraphcreator.algorithms.DiscoverCausalActivityGraphAlgorithm;
import org.processmining.causalactivitygraphcreator.parameters.DiscoverCausalActivityGraphParameters;
import org.pm4knime.settingsmodel.SMILPMinerParameter;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.hybridilpminer.parameters.XLogHybridILPMinerParametersImpl;
import org.processmining.hybridilpminer.plugins.HybridILPMinerPlugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.DefaultMinerNodeModel;

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
public class ILPMinerNodeModel extends DefaultMinerNodeModel {
	private static final NodeLogger logger = NodeLogger
            .getLogger(ILPMinerNodeModel.class);
	
	public static String CFG_KEY_ILP_PARAMETER = "ILP Parameter";
	// it has its own parameter. Then how to combine it with another settings?? 
	// like the classifier ?? we can remove the classifier in ILPMiner
	SMILPMinerParameter m_parameter; 
    /**
     * Constructor for the node model.
     */
    protected ILPMinerNodeModel() {
    	super(new PortType[] {XLogPortObject.TYPE}, new PortType[] {PetriNetPortObject.TYPE} );
    	
    	m_parameter = new SMILPMinerParameter(ILPMinerNodeModel.CFG_KEY_ILP_PARAMETER);
    }


	@Override
	protected PortObjectSpec[] configureOutSpec(XLogPortObjectSpec logSpec) {
		// TODO Auto-generated method stub
		PetriNetPortObjectSpec pnSpec = new PetriNetPortObjectSpec();
        return new PortObjectSpec[]{pnSpec};
	}

	@Override
	protected PortObject mine(XLog log) throws Exception {
		// TODO Auto-generated method stub
		logger.info("Start : ILPMiner " );
        
        XEventClassifier classifier = getEventClassifier();
        final String startLabel = "[start>@" + System.currentTimeMillis();
		final String endLabel = "[end]@" + System.currentTimeMillis();
		XLog artifLog = XLogUtil.addArtificialStartAndEnd(log, startLabel, endLabel, classifier);
        
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
        // create the parameter
		XLogHybridILPMinerParametersImpl param = new XLogHybridILPMinerParametersImpl(context, artifLog);
		// here put some values from m_parameter to param
		m_parameter.setDefaultParameter(param);
		
		param.setEventClassifier(classifier);
		
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
        return pnPO;
	}

	@Override
	protected void saveSpecificSettingsTo(NodeSettingsWO settings) {
		// TODO Auto-generated method stub
		m_parameter.saveSettingsTo(settings);
	}

	@Override
	protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_parameter.validateSettings(settings);
	}

	@Override
	protected void loadSpecificValidatedSettingsFrom(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		m_parameter.loadSettingsFrom(settings);
	}

}

