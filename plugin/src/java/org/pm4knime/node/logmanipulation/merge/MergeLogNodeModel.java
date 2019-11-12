package org.pm4knime.node.logmanipulation.merge;

import java.io.File;
import java.io.IOException;

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
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.logmanipulation.split.SplitLogNodeModel;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogUtil;

/**
 * <code>NodeModel</code> for the "MergeLog" node. This node accepts two event logs as input and merge them together as one event log. 
 * The merge strategies include:
 *  <1> if trace with same identifier separate or merge together?
 *  	<2> in merging traces, if they are same event attributes(name), list them separate or merge?
 *  	   	<3> if merge, which attributes are used as the start and complete time stamp for the new event??
 *  			Even, if they has different attributes, we merge them together!
 *
 * @author Kefang Ding
 */
public class MergeLogNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(MergeLogNodeModel.class);
	// check the m_strategy and m_string selection 
	// here are three options
	public static final String CFG_KEY_TRACE_STRATEGY = "Merge Strategy";
	public static final String[]  CFG_TRACE_STRATEGY = {"Separate", "Ignore", "Internal Merge" };
	public static final String CFG_KEY_TRACE_ATTRSET = "Trace Attribute Set";
	public static final String CFG_KEY_EVENT_ATTRSET = "Event Attribute Set";
	
	SettingsModelString m_strategy =  new SettingsModelString(CFG_KEY_TRACE_STRATEGY, CFG_TRACE_STRATEGY[2]);
	SettingsModelFilterString m_traceAttrSet = new SettingsModelFilterString(MergeLogNodeModel.CFG_KEY_TRACE_ATTRSET, new String[]{}, new String[]{}, true );
	SettingsModelFilterString m_eventAttrSet = new SettingsModelFilterString(MergeLogNodeModel.CFG_KEY_EVENT_ATTRSET, new String[]{}, new String[]{}, false );
	
    /**
     * Constructor for the node model.
     */
    protected MergeLogNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(new PortType[] { XLogPortObject.TYPE, XLogPortObject.TYPE }, new PortType[] { XLogPortObject.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Begin to merge event logs");
    	XLog log0 = ((XLogPortObject) inData[0]).getLog();
		XLog log1 = ((XLogPortObject) inData[1]).getLog();
		
		XLog mlog = null;
    	// according to the strategy choice, we have different methods to merge
		if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[0])) {
			// the traces are separately merged， even if they have the same identifiers
			// then global trace, they should have their different ones. To merge them
			mlog = XLogUtil.mergeLogsSeparate(log0, log1);
		}else if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[1])) {
			// ignore the traces with same identifier from the second event log
			mlog = XLogUtil.mergeLogsIgnoreTrace(log0, log1);
			
		}else if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[2])) {
			// need to merge according to its trace attributes and event attributes
			mlog = XLogUtil.mergeLogsIgnoreEvent(log0, log1);
		}else if(m_strategy.getStringValue().equals(CFG_TRACE_STRATEGY[3])) {
			// need to merge according to its trace attributes and event attributes
			mlog = XLogUtil.mergeLogsInternal(log0, log1);
		}else {
			System.out.println("Not such strategy");
		}
		
    	logger.info("End to merge event logs");
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

        // TODO: it generates another event log here
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
    	if(!inSpecs[1].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	// create new spec for output event log
    	XLogPortObjectSpec m_outSpec = new XLogPortObjectSpec();
        return new PortObjectSpec[]{m_outSpec};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_strategy.saveSettingsTo(settings);
    	m_traceAttrSet.saveSettingsTo(settings);
    	m_eventAttrSet.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
        // TODO: generated method stub
    	m_strategy.loadSettingsFrom(settings);
    	m_traceAttrSet.loadSettingsFrom(settings);
    	m_eventAttrSet.loadSettingsFrom(settings);
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

