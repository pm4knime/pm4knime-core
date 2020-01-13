package org.pm4knime.node.conversion.log2table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.time.localdatetime.LocalDateTimeCell;
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
import org.knime.core.node.defaultnodesettings.SettingsModelFilterString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;

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
 * Here, we need more extensions to say if we need this, or not. We just convert it directly to CSV file??
 * They are only DataTable conversion!! Rename the nodes, please!!!
 * @author Kefang
 */
public class XLog2TableConverterNodeModel extends NodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(XLog2TableConverterNodeModel.class);
	
	static final String CFG_TABLE_NAME = "Converted Data Table from Event Log";
	
	private SettingsModelFilterString m_traceAttrSet  = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_TRACE_ATTRSET, new String[]{}, new String[]{}, false );
	
	private SettingsModelFilterString m_eventAttrSet = new SettingsModelFilterString(XLogSpecUtil.CFG_KEY_EVENT_ATTRSET, new String[]{}, new String[]{}, false );
	
	private XLogPortObjectSpec m_inSpec;
    /**
     * Constructor for the node model.
     */
    protected XLog2TableConverterNodeModel() {
    
        // TODO: Specify the amount of input and output ports needed.
        super( new PortType[]{XLogPortObject.TYPE}, new PortType[]{BufferedDataTable.TYPE});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Start : Convert Event log to DataTable" );
    	XLogPortObject logPortObject = null ;
    	for(PortObject obj: inData)
        	if(obj instanceof XLogPortObject) {
        		logPortObject = (XLogPortObject)obj;
        		break;
        	}
        
    	XLog log = logPortObject.getLog();
    	// check the trace attributes we have chosen for the output!!  
    	// should we keep the attributes here. 
    	// FromXLogConverter.createSpec(log);
    	// DataTableSpec outSpec = FromXLogConverter.createAttrSpec(log);
    	
    	// if we have the dialog choices, we can create the outSpec easier than this one. 
    	DataTableSpec outSpec = createSpec();
    	
    	BufferedDataContainer bufCon = exec.createDataContainer(outSpec);
    	FromXLogConverter.convert(log, bufCon);
    	
    	bufCon.close();
    	logger.info("End : Convert Event log to DataTable" );
        return new BufferedDataTable[]{bufCon.getTable()};
    }

    private DataTableSpec createSpec() {
    	
    	List<String> attrNames = new ArrayList();
		List<DataType> attrTypes = new ArrayList();
		
		// from the trace attr to event attr here
		for (String attrKey : m_traceAttrSet.getIncludeList()) {
			// how to get the traceTypes here?? We have the m_spec from the input, we need to use it!!
			attrNames.add(attrKey);
			String attrType = m_inSpec.getGTraceAttrMap().get(attrKey);
			attrTypes.add(findDataType(attrType));
		}
		
		for (String attrKey : m_eventAttrSet.getIncludeList()) {
			// how to get the traceTypes here?? We have the m_spec from the input, we need to use it!!
			attrNames.add(attrKey);
			String attrType = m_inSpec.getGEventAttrMap().get(attrKey);
			attrTypes.add(findDataType(attrType));
		}
		
		DataTableSpec outSpec = new DataTableSpec(CFG_TABLE_NAME, 
				attrNames.toArray(new String[0]), attrTypes.toArray(new DataType[0]));
		
    	return outSpec;
    }
    
    
    private DataType findDataType(String cls) {
		// TODO according to the type in string, we get the right data type for it
    	if(cls.equals(XAttributeLiteralImpl.class.getSimpleName())) {
			// we don't care about the values here
			return StringCell.TYPE;
		}else if(cls.equals(XAttributeBooleanImpl.class.getSimpleName())) {
			// we don't care about the values here
			return BooleanCell.TYPE;
		}else if(cls.equals(XAttributeDiscreteImpl.class.getSimpleName())) {
			// we don't care about the values here
			return IntCell.TYPE;
		}else if(cls.equals(XAttributeContinuousImpl.class.getSimpleName())) {
			// we don't care about the values here
			return DoubleCell.TYPE;
		}else  if(cls.equals(XAttributeTimestampImpl.class.getSimpleName())) {
			// we don't care about the values here
			return DataType.getType(LocalDateTimeCell.class);
		}else {
			System.out.println("The attribute is not recognized");
		} 
		return null;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        // TODO: generated method stub
    	m_traceAttrSet.setIncludeList(new String[0]);
    	m_traceAttrSet.setExcludeList(new String[0]);
    	

    	m_eventAttrSet.setIncludeList(new String[0]);
    	m_eventAttrSet.setExcludeList(new String[0]);
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
    	
    	m_inSpec = (XLogPortObjectSpec) inSpecs[0];
	
        return new PortObjectSpec[]{null};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
         // TODO: generated method stub
    	m_traceAttrSet.saveSettingsTo(settings);
    	m_eventAttrSet.saveSettingsTo(settings);
    	
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
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

