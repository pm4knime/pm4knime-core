package org.pm4knime.node.precision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
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
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.performance.PerformanceCheckerNodeModel;
import org.pm4knime.portobject.PetriNetPortObject;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.util.ReplayerUtil;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.multietc.plugins.MultiETCPlugin;
import org.processmining.plugins.multietc.reflected.ReflectedLog;
import org.processmining.plugins.multietc.sett.MultiETCSettings;
import org.processmining.plugins.multietc.sett.MultiETCSettingsUI;
import org.processmining.plugins.petrinet.replayresult.PNMatchInstancesRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.petrinet.replayresult.PNRepResultImpl;
import org.processmining.plugins.petrinet.replayresult.StepTypes;
import org.processmining.plugins.replayer.replayresult.AllSyncReplayResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

/**
 * <code>NodeModel</code> for the "PrecisionChecker" node.
 * This precision checker is based on the jar ETConformance in ProM. 
 * We need to verify the settings for conformance. 
 *  
 * Another possible move is :
 *  we accept the replayed alignment and use it as the input for next nodes, it inlcudes:
 *   <1> Conformance Checking and the exact information
 *   <2> Performance Checking 
 *   <3> Precision input here
 *   
 * In this way, the inputs are: 
 *  <1> Alignment result
 *  If we choose alignmnet result as the input, we need to design several different algorithm for replay
 *  which is feasible by just adding more algorithm choices at the beginning step
 *  // in another point, if we have the alignment, we should store the log and petri net with it
 *  
 * Parameters: 
 *   <1> Conformance Parameter [ignore the settings, with existing alignment replay result]
 *   <2> Addtional for precision :
 *       -- representation
 *       -- algorithm choice
 *       
 * Outputs: 
 *    <1> precision in table, resulted in form caleld MultiETCResult, 
 *       -- preision
 *       -- automation
 *       
 * The algorithm to use in our application.. 
 *  -- import the alignment result, it should include the log and net,\
 *  -- create reflected log
 *  -- calculate the precision
 *  
 * // need to assign the corresponding settings to it.. one is the algorithm, 
	// one is if it in order. We store those parameters into one java class
	// for align-1, we need the alignment from normal conformance checking
	// for all option, 
	// at end, the reflected log and net is compared. But difference algorithm chooses to use 
	// different replay algorithms. The complete way to do this is 
	// -- inputs: Petri net and Event Log
	// -- outputs: the same result to show like before
	// one difficulty here is they have different ReplayResult, how to make them in corresponding form?
	// based on the RepResult and convert it into another format. 
	 * But  
	 * 
 * @author Kefang Ding
 */
public class PrecisionCheckerNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(PrecisionCheckerNodeModel.class);
	private final static String ALIGN_1 = "1-Align Precision";
	private final static String ALIGN_ALL = "All-Align Precision";
	private final static String ALIGN_REPRE = "Representative-Align Precision";
	private final static String ETC = "ETC Precision (no invisible/duplicates allowed)";
	
	
	private RepResultPortObject repResultPO;
    /**
     * Constructor for the node model.
     */
    protected PrecisionCheckerNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { RepResultPortObject.TYPE }, new PortType[] {
    			BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Start: ManifestReplayer Performance Checking");
    	
    	repResultPO = (RepResultPortObject) inData[0];
    	
    	PNRepResult repResult = repResultPO.getRepResult();
    	XLog log = repResultPO.getLogPO().getLog();
		AcceptingPetriNet anet = repResultPO.getPNPO().getANet();
    	
		PNMatchInstancesRepResult matchResult = ReplayerUtil.convert2MatchInstances(repResult);
		
		// create reflected log
		ReflectedLog refLog = ReplayerUtil.extractRefLog(matchResult);
		
		MultiETCSettings sett = getParameter();
		
		// based on match result, get the precision indication
		Object[] result = ReplayerUtil.checkMultiETC(refLog, anet, sett);
		
		// convert result into a table 
		
        return new PortObject[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    }

    private MultiETCSettings getParameter() {
    	MultiETCSettings sett = new MultiETCSettings();
    	boolean isOrdered = true;
    	if(isOrdered) 
    		sett.put(MultiETCSettings.REPRESENTATION, MultiETCSettings.Representation.ORDERED);
		else 
			sett.put(MultiETCSettings.REPRESENTATION, MultiETCSettings.Representation.UNORDERED);
		
		//Get the Algorithm 
    	String algName = null;
		if( algName == ETC) 
			sett.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ETC);
		else if( algName == ALIGN_1) 
			sett.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_1);
		else if( algName == ALIGN_REPRE) 
			sett.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_REPRE);
		else if( algName == ALIGN_ALL) 
			sett.put(MultiETCSettings.ALGORITHM, MultiETCSettings.Algorithm.ALIGN_ALL);
    	
    	return sett;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {

        // TODO: generated method stub
        return new PortObjectSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
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

