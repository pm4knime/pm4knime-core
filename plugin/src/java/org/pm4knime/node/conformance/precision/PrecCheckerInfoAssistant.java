package org.pm4knime.node.conformance.precision;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.processmining.plugins.multietc.res.MultiETCResult;

/**
 * this class helps build output table or PortObject
 * @author kefang-pads
 *
 */
public class PrecCheckerInfoAssistant {
	
	public static DataTableSpec createGlobalStatsTableSpec(String tableName) {
    	String[] columnNames = { "Property", "Value"};
    	DataType[] columnTypes ={StringCell.TYPE, DoubleCell.TYPE};
    	DataTableSpec tSpec = new DataTableSpec( tableName, columnNames, columnTypes);
    	return tSpec;
    }
	
	public static void buildInfoTable(BufferedDataContainer tBuf, MultiETCResult res) {
		// how to set the table name?? 
		
		// set precision information
		DataCell[] currentRow = new DataCell[tBuf.getTableSpec().getNumColumns()];
		int rowCounter = 0;
		currentRow[0] = new StringCell(MultiETCResult.PRECISION); 
		currentRow[1] = new DoubleCell((double) res.getAttribute(MultiETCResult.PRECISION));
		tBuf.addRowToTable(new DefaultRow(rowCounter +"", currentRow));
		rowCounter++;
		
		currentRow[0] = new StringCell(MultiETCResult.BACK_PRECISION); 
		currentRow[1] = new DoubleCell((double) res.getAttribute(MultiETCResult.BACK_PRECISION));
		tBuf.addRowToTable(new DefaultRow(rowCounter +"", currentRow));
		rowCounter++;
		
		currentRow[0] = new StringCell(MultiETCResult.BALANCED_PRECISION); 
		currentRow[1] = new DoubleCell((double) res.getAttribute(MultiETCResult.BALANCED_PRECISION));
		tBuf.addRowToTable(new DefaultRow(rowCounter +"", currentRow));
		rowCounter++;
		
		// set data for auto states
		currentRow[0] = new StringCell(MultiETCResult.AUTO_STATES); 
		currentRow[1] = new DoubleCell( new Double( res.getAttribute(MultiETCResult.AUTO_STATES).toString()));
		tBuf.addRowToTable(new DefaultRow(rowCounter +"", currentRow));
		rowCounter++;
		
		currentRow[0] = new StringCell(MultiETCResult.AUTO_STATES_IN); 
		currentRow[1] = new DoubleCell(new Double(res.getAttribute(MultiETCResult.AUTO_STATES_IN).toString()));
		tBuf.addRowToTable(new DefaultRow(rowCounter +"", currentRow));
		rowCounter++;
		
		
		currentRow[0] = new StringCell(MultiETCResult.AUTO_STATES_OUT); 
		currentRow[1] = new DoubleCell(new Double( res.getAttribute(MultiETCResult.AUTO_STATES_OUT).toString()));
		tBuf.addRowToTable(new DefaultRow(rowCounter +"", currentRow));
		rowCounter++;
		
		// set data for state in back
		currentRow[0] = new StringCell(MultiETCResult.AUTO_STATES_BACK); 
		currentRow[1] = new DoubleCell(new Double(res.getAttribute(MultiETCResult.AUTO_STATES_BACK).toString()));
		tBuf.addRowToTable(new DefaultRow(rowCounter +"", currentRow));
		rowCounter++;
		

		currentRow[0] = new StringCell(MultiETCResult.AUTO_STATES_IN_BACK); 
		currentRow[1] = new DoubleCell(new Double( res.getAttribute(MultiETCResult.AUTO_STATES_IN_BACK).toString()));
		tBuf.addRowToTable(new DefaultRow(rowCounter +"", currentRow));
		rowCounter++;
		

		currentRow[0] = new StringCell(MultiETCResult.AUTO_STATES_OUT_BACK); 
		currentRow[1] = new DoubleCell(new Double(res.getAttribute(MultiETCResult.AUTO_STATES_OUT_BACK).toString()));
		tBuf.addRowToTable(new DefaultRow(rowCounter +"", currentRow));
		rowCounter++;
		
    }
    
}
