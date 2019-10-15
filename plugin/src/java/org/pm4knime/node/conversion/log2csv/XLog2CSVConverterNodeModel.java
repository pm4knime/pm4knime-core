package org.pm4knime.node.conversion.log2csv;

import java.io.File;
import java.io.IOException;

import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;

/**
 * <code>NodeModel</code> for the "Xlog2CSVConverter" node. If we could change XLog into DataTable, 
 * we could convert it into CSV by using default CSV Writer in KNIME. Now, the thing here is how to convert
 * XLog into DataTable format. 
 * 
 * We need to have all the attributes of event class; It should include the attribute for trace, too.
 * If we use classifier, how could we store it into the DataTable?? 
 * -- get attributes and create table spec
 * -- fill each row for one event class 
 * 
 * ++ convert the DataTable into CSV files. But it is actually based on Datatable, we can operate on the DATATABEL. 
 * TableSpec, but how to fill it there?? Without data, but only the possible data there, check the csv reader codes
 * In ProM codes, it convert log directly into CVS files. It converts event log into string and output strings
 * 
 * @author Kefang
 */
public class XLog2CSVConverterNodeModel extends NodeModel {
    
    /**
     * Constructor for the node model.
     */
    protected XLog2CSVConverterNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super( new PortType[]{XLogPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	XLogPortObject logPortObject = null ;
    	for(PortObject obj: inData)
        	if(obj instanceof XLogPortObject) {
        		logPortObject = (XLogPortObject)obj;
        		break;
        	}
        
    	XLog log = logPortObject.getLog();
    	// FromXLogConverter.testAttrOverlapping(log);
    	
    	// FromXLogConverter.createSpec(log);
    	DataTableSpec outSpec = FromXLogConverter.createAttrSpec(log);
    	BufferedDataContainer bufCon = exec.createDataContainer(outSpec);
    	FromXLogConverter.convert(log, bufCon);
    	
    	bufCon.close();
        return new BufferedDataTable[]{bufCon.getTable()};
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

        // TODO: create a new DataTable there
    	if(!inSpecs[0].getClass().equals(XLogPortObjectSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	
		// how to get the configuration before the deal with the xlog file?
//		String logName = "logName";
//		DataTableSpec outSpec = new DataTableSpec(logName);
//    	
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

