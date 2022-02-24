package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.deckfour.xes.model.XLog;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.defaultnode.DefaultNodeModel;

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
public class FilterByFrequencyTableNodeModel extends DefaultNodeModel {
	private static final NodeLogger logger = NodeLogger.getLogger(FilterByFrequencyTableNodeFactory.class);
	
	public static final String CFG_ISKEEP = "Keep traces";
	// give one trace threshold, one absolute value or a percentage is both OK.
	// when it is below 1, we think it is the percentage, else, we use the absolute number 
	public static final String CFG_ISFOR_SINGLETRACE_VARIANT = "For single trace variant";
	public static final String CFG_THRESHOLD = "Threshold";
	
	SettingsModelBoolean m_isKeep = new SettingsModelBoolean(CFG_ISKEEP, true);
	SettingsModelBoolean m_isForSingleTV = new SettingsModelBoolean(CFG_ISFOR_SINGLETRACE_VARIANT, true);
	SettingsModelDoubleBounded m_threshold = new SettingsModelDoubleBounded(
			CFG_THRESHOLD, 0.2, 0, Integer.MAX_VALUE);
	private DataTableSpec m_outSpec;
    /**
     * Constructor for the node model.
     */
	 protected FilterByFrequencyTableNodeModel() {
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
    	int iThreshold = 0;
    	if(m_threshold.getDoubleValue() < 1) {
    		iThreshold = (int) (m_threshold.getDoubleValue()  * log.size());
    	}else {
    		// we can deal with it with the listener 
    		iThreshold = (int) m_threshold.getDoubleValue();
    		
    	}
    	
    	List<String> idAndTime =  Arrays.asList("#Trace Attribute#concept:name", "#Event Attribute#time:timestamp");
    	boolean[] sort_asc = new boolean[2];
    	sort_asc[0] = true;
    	sort_asc[1] = true;
    	BufferedDataTableSorter sorted_log = new BufferedDataTableSorter(log, idAndTime , sort_asc);
    	
    	//private ExecutionContext m_execContext = new ExecutionContext();
    	//log = sorted_log.sort(m_execContext);
    	
    	log = sorted_log.sort(exec);
    	
    	
    	System.out.println(log.getDataTableSpec().getNumColumns());
    	String[] arr_NumColumns = log.getDataTableSpec().getColumnNames();
    	for(int i=0 ; i < arr_NumColumns.length ; i++ ) { 
    		System.out.println(arr_NumColumns[i]);
    	}
    	
    	for (DataRow row : log) {
    		// and columns
    		System.out.println(row.getKey());
    		String str_row = "";
	    	for (int i = 0; i < row.getNumCells(); i++) {
	    		DataCell cell = row.getCell(i); 
	    		str_row += cell.toString();
	    		
	    	 }
	    	System.out.println(str_row);
    	}
    	
    	XLog nlog ; 
    	// for SingleTV... we need to interpret the percentage into absolute value
    	/*if(m_isForSingleTV.getBooleanValue()) {
    		nlog  = XLogFilterUtil.filterBySingleTVFreq(log, m_isKeep.getBooleanValue(), iThreshold, exec);
    	}else {
    		nlog = XLogFilterUtil.filterByWholeLogFreq(log, m_isKeep.getBooleanValue(), iThreshold, exec);
    	} */
    	
    	//m_outSpec.setSpec(XLogSpecUtil.extractSpec(nlog));
    	//DataTable logPO = new DataTable(nlog);
    	
    	//logPO.setSpec(m_outSpec);
    	logger.info("End: filter log by trace frequency");
        return new BufferedDataTable[]{log};
    }

   
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        
    	if(!inSpecs[0].getClass().equals(DataTableSpec.class)) 
    		throw new InvalidSettingsException("Input is not a valid Event Log!");
    	// create new spec for output event log
    	
    	if(m_threshold.getDoubleValue() >= 1) {
    		m_threshold.setDoubleValue((int)m_threshold.getDoubleValue());
    	}
    	// here is a new spec for the event log... some of them might get lost
    	// but others stay, like the global attributes stay here, others go away from it
    	m_outSpec = new DataTableSpec();
        return new PortObjectSpec[]{inSpecs[0]};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) {
    	 m_isKeep.saveSettingsTo(settings);
    	 m_isForSingleTV.saveSettingsTo(settings);
    	 m_threshold.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
     
    	 m_isKeep.loadSettingsFrom(settings);
    	 m_isForSingleTV.loadSettingsFrom(settings);
    	 m_threshold.loadSettingsFrom(settings);
    	 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {
    }
    

}

