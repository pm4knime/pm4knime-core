package org.pm4knime.node.logmanipulation.filter.knimetable;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
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
import org.knime.core.node.defaultnodesettings.SettingsModelDoubleBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.pm4knime.node.discovery.alpha.AlphaMinerNodeModel;
import org.pm4knime.portobject.XLogPortObject;
import org.pm4knime.portobject.XLogPortObjectSpec;
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.defaultnode.DefaultNodeModel;
import org.pm4knime.util.defaultnode.DefaultTableNodeModel;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.log.utils.TraceVariantByClassifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
public class FilterByFrequencyTableNodeModel extends DefaultTableNodeModel {
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
    	
    	List<String> idAndTime =  Arrays.asList(m_variantCase.getStringValue(), m_variantTime.getStringValue());
    	boolean[] sort_asc = new boolean[2];
    	sort_asc[0] = true;
    	sort_asc[1] = true;
    	BufferedDataTableSorter sorted_log = new BufferedDataTableSorter(log, idAndTime , sort_asc);
    	
    	log = sorted_log.sort(exec);
    	
    	
    	System.out.println(log.getDataTableSpec().getNumColumns());
    	String[] arr_NumColumns = log.getDataTableSpec().getColumnNames();
    	for(int i=0 ; i < arr_NumColumns.length ; i++ ) { 
    		System.out.println(arr_NumColumns[i]);
    	}
    	
    	
		String curr_traceID = "";
		String trace = "";
		int totalTraces = 0;
		HashMap<String, Integer> freq = new HashMap<String, Integer>(); // activity sequence to freq 
		HashMap<String, ArrayList<String>> trace_array = new HashMap<String, ArrayList<String>>(); // id to activities
		HashMap<String, ArrayList<String>> tracetoIds = new HashMap<String, ArrayList<String>>(); // activity sequence to ids
		
		
		// Create Mappings
    	for (DataRow row : log) {
	    	
	    	DataCell activity = row.getCell(log.getDataTableSpec().findColumnIndex(m_variantActivity.getStringValue()));
	    	DataCell traceID = row.getCell(log.getDataTableSpec().findColumnIndex(m_variantCase.getStringValue()));
	
	    	String traceIDStr = traceID.toString();
	    	if (!traceIDStr.equals(curr_traceID)) {
	    		
	    		if(!curr_traceID.equals("")) {
		    		
		    		if (freq.containsKey(trace)) {
		    			totalTraces++;
		    			freq.put(trace, freq.get(trace) + 1);
		    		}else {
		    			freq.put(trace, 1);
		    			totalTraces++;
		    		}
		    		
		    		if (tracetoIds.containsKey(trace)) {
		    			
		    			tracetoIds.get(trace).add(curr_traceID);
		    		}else {
		    			ArrayList<String> traceid_list = new ArrayList<String>();
			    		traceid_list.add(curr_traceID);
			    		tracetoIds.put(trace, traceid_list);
		    		}
	    		}
	    		
	    		
	    		curr_traceID = traceIDStr;
	    		System.out.println(trace);
	    		
	    		
	    		ArrayList<String> activity_list = new ArrayList<String>();
	    		activity_list.add(activity.toString());
	    		trace_array.put(curr_traceID, activity_list);
	    		trace = activity.toString();
	    	} else {
	    		//freq.put(curr_traceID, freq.get(curr_traceID) + 1);
	    		trace_array.get(curr_traceID).add(activity.toString());
	    		trace = trace + "," + activity.toString();
	    	}
	    	
    	}
    	
    	if (freq.containsKey(trace)) {
			totalTraces++;
			freq.put(trace, freq.get(trace) + 1);
		}else {
			freq.put(trace, 1);
			totalTraces++;
		}
		
		if (tracetoIds.containsKey(trace)) {
			
			tracetoIds.get(trace).add(curr_traceID);
		}else {
			ArrayList<String> traceid_list = new ArrayList<String>();
    		traceid_list.add(curr_traceID);
    		tracetoIds.put(trace, traceid_list);
		}
   
		
		//Sort 
		ArrayList<ArrayList<String>> listOfCaseId = new ArrayList<ArrayList<String>>(tracetoIds.values());
		Comparator<ArrayList<String>> sizeComparator = new Comparator<ArrayList<String>>()
	    {
	        @Override
	        public int compare(ArrayList<String> o1, ArrayList<String> o2)
	        {
	            return Integer.compare(o1.size(), o2.size());
	        }
	    };
		
	    Collections.sort(listOfCaseId, sizeComparator);
	    Collections.reverse(listOfCaseId);
	    

	    
	    // Create new BufferedDataTable without filtered rows
	    
	    ArrayList<String> containIDs = getContainedCases(listOfCaseId, iThreshold, trace_array);
	    
	    BufferedDataContainer buf = exec.createDataContainer(log.getDataTableSpec());
	    

	    for (DataRow row : log) {
	    	
	    	DataCell traceID = row.getCell(log.getDataTableSpec().findColumnIndex(m_variantCase.getStringValue()));

	    	if (containIDs.contains(traceID.toString())) {
		    	buf.addRowToTable(row);

	    	}
	  
	    }
	    buf.close();
	    

    	logger.info("End: filter log by trace frequency");
        return new BufferedDataTable[]{buf.getTable()};
    }
    
    
    protected ArrayList<String> getContainedCases(ArrayList<ArrayList<String>> listOfValues, int iThreshold, 
    														HashMap<String, ArrayList<String>> trace_array) {
    	
	    ArrayList<String> containIDs = new ArrayList<String>();
	    
	    
	    if (m_isForSingleTV.getBooleanValue()){
	    	
			if(m_isKeep.getBooleanValue()) {
				for(ArrayList<String> variant : listOfValues) {
		    		
		    		if(variant.size() * trace_array.get(variant.get(0)).size()  >= iThreshold) {
		    			containIDs.addAll(variant);
		    		}
		    	}
			}else {
				for(ArrayList<String> variant : listOfValues) {
		    		
		    		if(variant.size() * trace_array.get(variant.get(0)).size() < iThreshold) {
		    			containIDs.addAll(variant);
		    		}
		    	}
			}
	    	
	    }else {
	    	
	    	
	    	int sum = 0;
	    	if(m_isKeep.getBooleanValue()) {
	    		for(ArrayList<String> variant : listOfValues) {

	        		if(sum <= iThreshold) {
	        			containIDs.addAll(variant);
	        		}else
	        			break;
	        		sum += variant.size() * trace_array.get(variant.get(0)).size();
	    		}
	    		
	    	}else {
	    		for(ArrayList<String> variant : listOfValues) {

	        		if(sum <= iThreshold) {
	        			sum += variant.size() * trace_array.get(variant.get(0)).size();
	        			continue ;
	        		}else
	        			containIDs.addAll(variant);
	    		}
	    	}
	    	 
	    }
    	
	    return containIDs;
    }

   
    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configureOutSpec(DataTableSpec tableSpecs) {
        
    	
    	if(m_threshold.getDoubleValue() >= 1) {
    		m_threshold.setDoubleValue((int)m_threshold.getDoubleValue());
    	}

        return new PortObjectSpec[]{tableSpecs};
    }
    
    
    

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSpecificSettingsTo(final NodeSettingsWO settings) {
    	 m_isKeep.saveSettingsTo(settings);
    	 m_isForSingleTV.saveSettingsTo(settings);
    	 m_threshold.saveSettingsTo(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSpecificValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {
     
    	 m_isKeep.loadSettingsFrom(settings);
    	 m_isForSingleTV.loadSettingsFrom(settings);
    	 m_threshold.loadSettingsFrom(settings);
    	 
    }


	@Override
	protected void validateSpecificSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}


    

}

