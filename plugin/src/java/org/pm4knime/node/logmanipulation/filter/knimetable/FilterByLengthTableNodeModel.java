package org.pm4knime.node.logmanipulation.filter.knimetable;


import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;

import org.pm4knime.util.defaultnode.DefaultTableNodeModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <code>NodeModel</code> for the "FilterByFrequency" node.
 * one is to filter the trace over such percentage, one is over the whole event log
 * we only keep the most frequent traces over one threshold 
 * define it is for single trace, or for the whole event log.  
 * 
 * When the threshold is 1.0, what to decide??  
 * 1. forSingleTV, absolute value 1, it has no meaning, but all the values here. 
 * 2. for SingleTV, percentage value, it means that we need to filter the ones only with 100% trace variant
 * 
 * 3. for whole event log, 1.0 means that we want all the trace variant. But for absolute values, it means 
 * to filter all the trace over frequency over 1.0. It is also all the values. But 
 *  
 *   we will interpret it as one integer here. 
 *   threshold  > 1.0, it means that we use absolute values to filter the trace variants. If it is equal or greater 
 *   than this value. 
 * 
 *   Filter the whole event log, we will order the event log with descending frequency. and get the trace which 
 *   matches those criterias.  
 * @author Kefang Ding
 */
public class FilterByLengthTableNodeModel extends DefaultTableNodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(FilterByLengthTableNodeFactory.class);
	
	public static final String CFG_ISKEEP = "Keep";
	public static final String CFG_MININUM_LENGTH = "Minimum Length";
	public static final String CFG_MAXINUM_LENGTH = "Maximum Length";
	
	private SettingsModelBoolean m_isKeep = new SettingsModelBoolean(CFG_ISKEEP, true);
	// how to set the maximum length for each event log?? How to find the values there, 
	// to discover the max and set its threshold 
	private SettingsModelIntegerBounded m_minLength = new SettingsModelIntegerBounded(CFG_MININUM_LENGTH, 1, 1, Integer.MAX_VALUE);
	private SettingsModelIntegerBounded m_maxLength = new SettingsModelIntegerBounded(CFG_MAXINUM_LENGTH, 20, 1, Integer.MAX_VALUE);

    /**
     * Constructor for the node model.
     */
	 protected FilterByLengthTableNodeModel() {
    	 super(new PortType[] {BufferedDataTable.TYPE }, new PortType[] { BufferedDataTable.TYPE });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final PortObject[] inData,
            final ExecutionContext exec) throws Exception {
    	logger.info("Begin: filter log by trace frequency");
    	// we need to interpret the percentage into absolute value for both sides
    	// according to the whole log size This log forgets its global attributes there 
    	// so we need to extract them again to have this values. 
    	// now, if we copy the attributes from the original, we don't have that.
    	BufferedDataTable log = ((BufferedDataTable) inData[0]);
    	if(log.size() == 0) {
    		// sth happens to the execution. we can give some warning here??
    		logger.warn("This event log is empty, the original log will be returned");
    		return new BufferedDataTable[]{(BufferedDataTable) inData[0]};
    	}
    	checkCanceled(exec);

    
    	// Sorting
    	List<String> idAndTime =  Arrays.asList(m_variantCase.getStringValue());
    	boolean[] sort_asc = new boolean[1];
    	sort_asc[0] = true;
    	BufferedDataTableSorter sorted_log = new BufferedDataTableSorter(log, idAndTime , sort_asc);
    	log = sorted_log.sort(exec);
    	
    	
		String curr_traceID = "";
		int trace_length = 0;
		ArrayList<DataRow> trace_datarow = new ArrayList<DataRow>();
	    BufferedDataContainer buf = exec.createDataContainer(log.getDataTableSpec(), false);	
		
    	for (DataRow row : log) {
	    	
	    	DataCell traceID = row.getCell(log.getDataTableSpec().findColumnIndex(m_variantCase.getStringValue()));
	
	    	String traceIDStr = traceID.toString();
	    	if (!traceIDStr.equals(curr_traceID)) {
	    		exec.checkCanceled();
	    		
	    		if(!curr_traceID.equals("")) {
	    			if(m_isKeep.getBooleanValue() == 
	    					(trace_length >= m_minLength.getIntValue() 
	    						&& trace_length <= m_maxLength.getIntValue())) {
	    							for (DataRow trace_row : trace_datarow) {
	    								buf.addRowToTable(trace_row);
	    							}
	    			}
	    		}
	    		
	    		curr_traceID = traceIDStr;
	    		trace_length = 1;
	    		trace_datarow = new ArrayList<DataRow>();
	    	} else {
	    		trace_length++;
	    	}
	    	
	    	trace_datarow.add(row);
	    	
    	}
    	
    	if(!curr_traceID.equals("")) {
			if(m_isKeep.getBooleanValue() == 
					(trace_length >= m_minLength.getIntValue() 
						&& trace_length <= m_maxLength.getIntValue())) {
							for (DataRow trace_row : trace_datarow) {
								buf.addRowToTable(trace_row);
							}
			}
		}
    	buf.close();

    	logger.info("End: filter log by trace frequency");
        return new BufferedDataTable[]{buf.getTable()};
    }

   
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configureOutSpec(DataTableSpec tableSpecs) {
        

        return new PortObjectSpec[]{tableSpecs};
    }
    
    
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSpecificSettingsTo(final NodeSettingsWO settings) {
    	 m_isKeep.saveSettingsTo(settings);
    	 m_minLength.saveSettingsTo(settings);
         m_maxLength.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSpecificValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
     
    	 m_isKeep.loadSettingsFrom(settings);
         m_minLength.loadSettingsFrom(settings);
         m_maxLength.loadSettingsFrom(settings);
    	 
    }


	@Override
	protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
    	SettingsModelIntegerBounded minLength =  m_minLength.createCloneWithValidatedValue(settings);          
    	SettingsModelIntegerBounded maxLength =   m_maxLength.createCloneWithValidatedValue(settings);            
    	if (minLength.getIntValue() > maxLength.getIntValue()) {              
    		throw new InvalidSettingsException("The specified minimum "  + "must be smaller than the maximum value.");  
    	}
		
	}


    

}

