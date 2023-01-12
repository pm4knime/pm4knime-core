package org.pm4knime.node.discovery.ilpminer.Table;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XLog;
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
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.settingsmodel.SMILPMinerParameter;
import org.pm4knime.settingsmodel.SMTable2XLogConfig;
import org.pm4knime.util.XLogUtil;
import org.pm4knime.util.connectors.prom.PM4KNIMEGlobalContext;
import org.pm4knime.util.defaultnode.DefaultMinerNodeModel;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.impl.AcceptingPetriNetImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.hybridilpminer.parameters.XLogHybridILPMinerParametersImpl;
import org.processmining.hybridilpminer.plugins.HybridILPMinerPlugin;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.BufferedDataTable;
import org.pm4knime.node.conversion.table2log.Table2XLogConverterNodeModel;
import org.pm4knime.node.conversion.table2log.ToXLogConverter;
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
public class ILPMinerTableNodeModel extends DefaultMinerNodeModel {
	
	private static final NodeLogger logger = NodeLogger
            .getLogger(ILPMinerTableNodeModel.class);
	
	public static String CFG_KEY_ILP_PARAMETER = "ILP Parameter";
	// it has its own parameter. Then how to combine it with another settings?? 
	// like the classifier ?? we can remove the classifier in ILPMiner
	SMILPMinerParameter m_parameter; 
	
	public static final String CFG_KEY_CONFIG = "Table to event log conveter config";
	SMTable2XLogConfig m_config =  new SMTable2XLogConfig(CFG_KEY_CONFIG);
	
	XLogPortObject logPO;
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
		
		//BufferedDataTable copiedtable = new BufferedDataTable();
		
		logger.info("Start : ILPMinerTable " );
		checkCanceled(exec);
		XEventClassifier classifier = getEventClassifier();
		
		PortObject[] convertedLog = Table2XLogConverter(table, exec);
		
		final String startLabel = "[start>@" + System.currentTimeMillis();
		final String endLabel = "[end]@" + System.currentTimeMillis();
		
		XLog artifLog = XLogUtil.addArtificialStartAndEnd(convertedLog, startLabel, endLabel, classifier);
		
		PluginContext context = PM4KNIMEGlobalContext.instance().getPluginContext();
		checkCanceled(context, exec);
        // create the parameter
		XLogHybridILPMinerParametersImpl param = new XLogHybridILPMinerParametersImpl(context, artifLog);
		// here put some values from m_parameter to param
		m_parameter.updateParameter(param);
		
		param.setEventClassifier(classifier);
      
    	checkCanceled(context, exec);
    	Object[] result = HybridILPMinerPlugin.discoverWithArtificialStartEnd(context, convertedLog, artifLog, param);
        
    	// create the accepting Petri net and PortObject
    	AcceptingPetriNet anet = new AcceptingPetriNetImpl((Petrinet) result[0], (Marking) result[1],  (Marking) result[2]);
    	checkCanceled(exec);
        PetriNetPortObject pnPO = new PetriNetPortObject(anet);
        
    	logger.info("End : ILPMinerTable " );
        return pnPO;
		
		
	}
	
	protected PortObject[] Table2XLogConverter(final BufferedDataTable tableData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Start : Convert DataTable to Event Log" );
        // TODO: accept input DataTable, use the configuration columnNames, 
    	// convert the data into XLog
    	// how to check the type for this?
    	BufferedDataTable tData = tableData;
    	
    	// sort the table w.r.t. caseID column
    	List<String> m_inclList = new ArrayList<String>();
    	m_inclList.add(m_config.getMCaseID().getStringValue());
    	// here we might need to make sure they mean this
    	boolean[] m_sortOrder = {true};
    	boolean m_missingToEnd = false;
    	boolean m_sortInMemory = false;
    	BufferedDataTableSorter sorter = new BufferedDataTableSorter(
    			tData, m_inclList, m_sortOrder, m_missingToEnd);
    	
        sorter.setSortInMemory(m_sortInMemory);
        BufferedDataTable sortedTable = sorter.sort(exec);
    	
        checkCanceled();
    	// convert the string to date and sort them according to caseID? So we can read them easier for rows
    	// it creates the corresponding column spec and create another DataTable for it.
    	// one thing to remember, it is not so important to have order of timestamp. 
    	ToXLogConverter handler = new ToXLogConverter();
    	handler.setConfig(m_config);
    	handler.setLogger(logger);
    	
    	handler.convertDataTable2Log(sortedTable, exec);
    	XLog log = handler.getXLog();
    	
    	checkCanceled();
    	logPO = new XLogPortObject(log);
    	
    	logger.info("End : Convert DataTable to Event Log" );
        return new PortObject[]{logPO};
        
    }
    
	private void checkCanceled() {
		// TODO Auto-generated method stub
		
	}



}

