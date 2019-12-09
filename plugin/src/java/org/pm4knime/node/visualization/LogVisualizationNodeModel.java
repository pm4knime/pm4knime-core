package org.pm4knime.node.visualization;

import java.io.File;
import java.io.IOException;

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
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;

/**
 * <code>NodeModel</code> for the "LogVisualization" node. 
 * The output is nothing, right?? Or we allow it to be saved as one picture. 
 * No return type or the same event log is returned.  
 * @author Kefang Ding
 */
public class LogVisualizationNodeModel extends NodeModel  implements PortObjectHolder{
	private static final NodeLogger logger = NodeLogger
            .getLogger(LogVisualizationNodeModel.class);
	XLogPortObject logPO;
    /**
     * Constructor for the node model.
     */
    protected LogVisualizationNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super(new PortType[] {XLogPortObject.TYPE}, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Begin: Show the views of event log");
    	logPO = (XLogPortObject) inData[0];
    	
    	logger.info("End: Show the views of event log");
        return new PortObject[]{};
    }

    public XLogPortObject getLogPO() {
    	return logPO;
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
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
        return null;
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

	@Override
	public PortObject[] getInternalPortObjects() {
		// TODO Auto-generated method stub
		return new PortObject[] {logPO};
	}

	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		// TODO Auto-generated method stub
		logPO = (XLogPortObject) portObjects[0];
	}

}

