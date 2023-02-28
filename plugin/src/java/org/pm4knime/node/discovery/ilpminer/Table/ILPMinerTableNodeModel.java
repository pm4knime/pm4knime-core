package org.pm4knime.node.discovery.ilpminer.Table;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;
import org.pm4knime.settingsmodel.SMILPMinerParameter;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerModel;
import org.pm4knime.node.discovery.ilpminer.Table.util.TableHybridILPMinerParametersImpl;
import org.pm4knime.node.discovery.ilpminer.Table.util.TableHybridILPMinerPlugin;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;

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
 * 	
 * Modifications: 2020-02-12 
 *   Need to delete some options to avoid the flower model. 
 *      Casual_E_Vereek + its corresponding algorithms
 *      
 *  SLACK_VAR is slow but will be kept. 
 *   After this, we can merge the options in one Panel : Options. 
 * Notes: I wonder if I could put all the parameters into one big class and follow this strategy for other nodes, too. 
 * The reason for it is to reduce the codes in one nodes. Make it bettter to use this. Other reasons to use other steps for it.
 * All of them is in default mode. SO we can use it 
 * @author Kefang Ding
 */

/*
 * TODO:
 * 1. to replace all the event log to the table(BufferedDataTable)
 * 
 */
public class ILPMinerTableNodeModel extends DefaultTableMinerModel {
	
	private static final NodeLogger logger = NodeLogger
            .getLogger(ILPMinerTableNodeModel.class);
	
	public static String CFG_KEY_ILP_PARAMETER = "ILP Parameter";
	// it has its own parameter. Then how to combine it with another settings?? 
	// like the classifier ?? we can remove the classifier in ILPMiner
	SMILPMinerParameter m_parameter; 
	
	public static final String CFG_KEY_CONFIG = "Table to event log conveter config";

	
    /**
     * Constructor for the node model.
     */
    protected ILPMinerTableNodeModel() {
    	super(new PortType[] {BufferedDataTable.TYPE}, new PortType[] {PetriNetPortObject.TYPE} );
    	
    	m_parameter = new SMILPMinerParameter(ILPMinerTableNodeModel.CFG_KEY_ILP_PARAMETER);
    }
    

	protected PortObjectSpec[] configureOutSpec(DataTableSpec tableSpec) {
		// TODO Auto-generated method stub
		PetriNetPortObjectSpec pnSpec = new PetriNetPortObjectSpec();
        return new PortObjectSpec[]{pnSpec};
	}
	
	@Override
	protected PortObject mine(BufferedDataTable table, final ExecutionContext exec) throws Exception {
		
        logger.info("Start : ILPMiner " );
        
        final String startLabel = "ARTIFICIAL_START";
		final String endLabel = "ARTIFICIAL_END";
		
		TraceVariantRepresentation log = new TraceVariantRepresentation(table, this.getTraceClassifier(), this.getEventClassifier());
		TraceVariantRepresentation artifLog = TraceVariantRepresentation.addArtificialStartAndEnd(log.getNumberOfTraces(), log.getActivities(), log.getVariants(), startLabel, endLabel);
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
		checkCanceled(context, exec);
        // create the parameter
		TableHybridILPMinerParametersImpl param = new TableHybridILPMinerParametersImpl(context, artifLog);
		// here put some values from m_parameter to param
		m_parameter.updateParameter(param);
      
    	checkCanceled(context, exec);
    	Object[] result = TableHybridILPMinerPlugin.discoverWithArtificialStartEnd(context, log, artifLog, param);
        
    	// create the accepting Petri net and PortObject
    	AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1],  (Marking) result[2]);
    	checkCanceled(exec);
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

