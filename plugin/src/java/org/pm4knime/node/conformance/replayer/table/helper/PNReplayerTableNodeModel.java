package org.pm4knime.node.conformance.replayer.table.helper;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.port.PortObject;
import org.pm4knime.node.conformance.replayer.table.helper.tableLibs.SMAlignmentReplayerParameterWithCTTable;

/**
 * <code>NodeModel</code> for the "PNReplayerTable" node.
 *
 * @author 
 */
public class PNReplayerTableNodeModel extends DefaultPNReplayerTableModel {
    
	private static final NodeLogger logger = NodeLogger.getLogger(PNReplayerTableNodeModel.class);
	private static final  String message  = "Replayer With Cost Tables";	
	
	@Override
	protected void initializeParameter() {
		
    	m_parameter = new SMAlignmentReplayerParameterWithCTTable(CFG_PARAMETER_NAME);
    }

	@Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
		// if the logger is used as here??
		// no it can't be used here, if we go to the instance structure, it turned 
		// to set it final because we just want to initialize it once!! 
		// but the static value still there, so we can't hold this!!! Change strategy here!! 
		logger.info("Start: " + message);
    	
    	String strategyName = m_parameter.getMStrategy().getStringValue();
    	super.executeWithoutLogger(inData, exec, strategyName);
    	// in greed to output the strategy for replay
		logger.info("End: " + message + " for "+ strategyName);
		return new PortObject[]{repResultPO};
		
	}

}

