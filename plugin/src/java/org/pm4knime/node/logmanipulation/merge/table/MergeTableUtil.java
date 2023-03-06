package org.pm4knime.node.logmanipulation.merge.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;

public class MergeTableUtil {
	public static final String CFG_ATTRIBUTE_SUFFIX = "-new";
	public static final String LOG_0_PREFIX = "log0:";
	public static final String LOG_1_PREFIX = "log1:";
	
	
	// merge two event logs with separate strategy
	public static BufferedDataTable mergeTablesSeparate(BufferedDataTable log0, BufferedDataTable log1, String caseID0, String caseID1, ExecutionContext exec)
			throws CanceledExecutionException {
		
		
		DataTableSpec spec_0 = log0.getDataTableSpec();
		DataTableSpec spec_1 = log1.getDataTableSpec();
		
        DataTableSpec outSpec = createSpec(spec_0, spec_1, caseID1, caseID1);
        String[] columns = outSpec.getColumnNames();
    	
    	BufferedDataContainer bufCon = exec.createDataContainer(outSpec, false);
//		mlog.getAttributes().putAll(log1.getAttributes());
//		mlog.getGlobalTraceAttributes().addAll(log1.getGlobalTraceAttributes());
//		mlog.getGlobalEventAttributes().addAll(log1.getGlobalEventAttributes());
    	int eventCount = 0;
    	List<String> columns_0 = Arrays.asList(spec_0.getColumnNames());
        for (DataRow row : log0) {
        	DataCell[] allCells = new DataCell[columns.length];
        	
        	for (int index = 0; index<columns.length; index++) {
        		String current_col = columns[index];
        		DataCell cell;
        		System.out.println(current_col);
        		if (caseID0.equals(current_col)) {
        			System.out.println("CASE ID");
        			cell = row.getCell(spec_0.findColumnIndex(current_col));
        			cell = new StringCell(LOG_0_PREFIX + cell);
        		}
        		else if (columns_0.contains(current_col)) {
        			System.out.println("other");
        			cell = row.getCell(spec_0.findColumnIndex(current_col));
        		} else {
        			cell = new MissingCell("");
        			System.out.println("empty");
        		}
        		allCells[index] = cell;
        	}
        	
        	
	    	DataRow eventRow = new DefaultRow("Event " + (eventCount++), allCells);	
	    	bufCon.addRowToTable(eventRow);
	    	eventCount++;
		}
        
        List<String> columns_1 = Arrays.asList(spec_1.getColumnNames());
        for (DataRow row : log1) {
        	DataCell[] allCells = new DataCell[columns.length];
        	
        	for (int index = 0; index<columns.length; index++) {
        		String current_col = columns[index];
        		System.out.println(current_col);
        		DataCell cell;
        		if (caseID0.equals(current_col)) {
        			System.out.println("CASE ID");
        			cell = row.getCell(spec_1.findColumnIndex(caseID1));
        			cell = new StringCell(LOG_1_PREFIX + cell);
        		}
        		else if (columns_1.contains(current_col)) {
        			cell = row.getCell(spec_1.findColumnIndex(current_col));
        			System.out.println("other");
        		} else {
        			cell = new MissingCell("");
        			System.out.println("empty");
        		}
        		allCells[index] = cell;
        	}
        	
        	
	    	DataRow eventRow = new DefaultRow("Event " + (eventCount++), allCells);	
	    	bufCon.addRowToTable(eventRow);
	    	eventCount++;
		}
        
        bufCon.close();
	    return bufCon.getTable();
	}
	
    public static DataTableSpec createSpec(DataTableSpec spec0, DataTableSpec spec1, String caseID0, String caseID1) {
    	
    	List<String> attrNames = new ArrayList();
		List<DataType> attrTypes = new ArrayList();
		
		
		for (DataColumnSpec s: spec0) {
			String name = s.getName();
			attrNames.add(name);
			attrTypes.add(s.getType());
		}
		
		for (DataColumnSpec s: spec1) {
			String name = s.getName();
			if (!attrNames.contains(name)) {
				if (!name.equals(caseID1)) {
					attrNames.add(name);
					attrTypes.add(s.getType());
				}
				
			}
			
		}
		
		
		DataTableSpec outSpec = new DataTableSpec("Merged Table", 
				attrNames.toArray(new String[0]), attrTypes.toArray(new DataType[0]));
		
    	return outSpec;
    }
    
}
