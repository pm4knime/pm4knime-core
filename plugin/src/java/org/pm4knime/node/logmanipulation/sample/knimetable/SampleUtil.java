package org.pm4knime.node.logmanipulation.sample.knimetable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.pm4knime.util.XLogUtil;

public class SampleUtil {
	
	public static HashSet<DataCell> sample(long bound, double prob, Set<DataCell> traceIds) {

		int num = (int) (prob * bound);
		return sample(bound, num, traceIds);
	}

	public static HashSet<DataCell> sample(long bound, int num, Set<DataCell> traceIds) {
		ArrayList<DataCell> idx_list = new ArrayList<>();
		ArrayList<DataCell> trace_list = new ArrayList<DataCell>();
		trace_list.addAll(traceIds);
		Random random = new Random(44444);
		int index;
		while (num > 0) {
			
			index = random.nextInt(trace_list.size());
			
			idx_list.add(trace_list.get(index));
			trace_list.remove(index);
			num--;

			
		}
		return new HashSet<DataCell>(idx_list);
	}

	public static BufferedDataTable[] sampleLog(BufferedDataTable log, int number, 
											String traceIdString, ExecutionContext exec) {

		BufferedDataContainer slog = exec.createDataContainer(log.getDataTableSpec(), false);
		BufferedDataContainer dlog = exec.createDataContainer(log.getDataTableSpec(), false);
		
		

		// sample the index for the traces
    	Set<DataCell> traceIds = log.getDataTableSpec().getColumnSpec(traceIdString).getDomain().getValues();
    	if (traceIds == null){
			traceIds =  getUniqueValues(log, traceIdString);
		}
    	

    	if(number > traceIds.size()) {
			System.out.println("The chosen sample number is bigger than the log, will only output the whole log");
			number = (int) traceIds.size();
		}

    	HashSet<DataCell> sIdx = SampleUtil.sample(traceIds.size(), number, traceIds);

		for (DataRow row : log) {
			
	    	DataCell traceIDData = row.getCell(log.getDataTableSpec().findColumnIndex(traceIdString));
			if (sIdx.contains(traceIDData)) {
				slog.addRowToTable(row); // TODO: is that a deep copy?
			} else {
				dlog.addRowToTable(row); // TODO: is that a deep copy?
			}
		}
		
		slog.close();
		dlog.close();
		
		return new BufferedDataTable[] { slog.getTable(), dlog.getTable() };
	}

	/**
	 * sample the log in percentage of the whole size.
	 * 
	 * @param log
	 * @param percentage
	 *            0<=percentage<=1
	 * @returnerc
	 */
	public static BufferedDataTable[] sampleLog(BufferedDataTable log, double percentage, 
			String traceIdString, ExecutionContext exec) {
		
		// convert percentage to num
		Set<DataCell> traceIds = log.getDataTableSpec().getColumnSpec(traceIdString).getDomain().getValues();
		if (traceIds == null){
			traceIds =  getUniqueValues(log, traceIdString);
		}
		int num = (int) (traceIds.size() * percentage);

		return sampleLog(log, num, traceIdString, exec);
	}
	
	
	public static Set<DataCell> getUniqueValues(BufferedDataTable log, String traceIdString) {
		
		Set<DataCell> traceIds = new HashSet<DataCell>();
		for (DataRow row : log) {
	    	
	    	DataCell traceID = row.getCell(log.getDataTableSpec().findColumnIndex(traceIdString));
	    	traceIds.add(traceID);
	  
	    }
		return traceIds;
		
	}
}
