package org.pm4knime.node.discovery.ilpminer;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.settingsmodel.SettingsModelILPMinerParameter;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.PetriNetPortObjectSpec;

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
 * 			-- filter type and threshold
 * 			-- miner strategy
 * 		Advanced Options: 
 * 			-- LP-Filter : sequence-encoding filter, slack variable fiter, none
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
	
	SettingsModelILPMinerParameter m_parameter; 
    /**
     * Constructor for the node model.
     */
    protected ILPMinerNodeModel() {
    	super(new PortType[] {XLogPortObject.TYPE}, new PortType[] {PetriNetPortObject.TYPE} );
    	
    	m_parameter = new SettingsModelILPMinerParameter(ILPMinerNodeModel.CFG_KEY_ILP_PARAMETER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	System.out.println(m_parameter.getMalgorithm().getStringValue());
        // TODO: Return a BufferedDataTable for each output port 
        return new PortObject[]{};
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

