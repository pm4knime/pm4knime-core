package org.pm4knime.node.conformance;

import java.io.File;
import java.io.IOException;
import java.util.Map;

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
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.RepResultPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;

/**
 * <code>NodeModel</code> for the "ConformanceChecker" node.
 *
 * @author 
 */
public class ConformanceCheckerNodeModel extends NodeModel implements PortObjectHolder{
	private static final NodeLogger logger = NodeLogger.getLogger(ConformanceCheckerNodeModel.class);
	
	private DataTableSpec m_tSpec;
	RepResultPortObject repResultPO;
	
	// it seems there is no parameters to add in this result shown...
    /**
     * Constructor for the node model.
     */
    protected ConformanceCheckerNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
    	super(new PortType[] { RepResultPortObject.TYPE}, new PortType[] {BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Start: Unified PNReplayer Conformance Checking");
    	repResultPO = (RepResultPortObject) inData[0];
    	
    	BufferedDataContainer buf = exec.createDataContainer(m_tSpec);
    	Map<String, Object> info = repResultPO.getRepResult().getInfo();
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
    	
        return new BufferedDataTable[]{};
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
    protected DataTableSpec[] configure(final DataTableSpec[] inSpecs)
            throws InvalidSettingsException {
    	if (!inSpecs[0].getClass().equals(RepResultPortObject.class))
			throw new InvalidSettingsException("Input is not a valid replay result!");

    	// here we should give its spec for statistics info
		DataColumnSpec[] cSpec = new DataColumnSpec[2];
    	cSpec[0] = new DataColumnSpecCreator("Type", StringCell.TYPE).createSpec();
    	cSpec[1] = new DataColumnSpecCreator("Value", DoubleCell.TYPE).createSpec();
    	
    	m_tSpec = new DataTableSpec(cSpec);
		
        return new DataTableSpec[]{m_tSpec};
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
		return new PortObject[] { repResultPO};
	}

	/*
	 * It defines methods to restore port objects. It is called after execution. 
	 * because the view is based on the input and output port objects, so an internal view is in need.
	 * Serialization is controlled by KNIME platform.
	 */
	@Override
	public void setInternalPortObjects(PortObject[] portObjects) {
		// TODO Auto-generated method stub
		
		repResultPO = (RepResultPortObject) portObjects[0];
	}


}

