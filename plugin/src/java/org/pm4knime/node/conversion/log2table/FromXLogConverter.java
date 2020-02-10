package org.pm4knime.node.conversion.log2table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeBoolean;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.MissingCell;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.BooleanCell.BooleanCellFactory;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.DoubleCell.DoubleCellFactory;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.IntCell.IntCellFactory;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.def.StringCell.StringCellFactory;
import org.knime.core.data.time.localdatetime.LocalDateTimeCell;
import org.knime.core.data.time.localdatetime.LocalDateTimeCellFactory;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.pm4knime.util.XLogSpecUtil;
/**
 * this class accepts an xlog file and convert it into a Datatable format, not in CSV format, please notice
 * @author kefang-pads
 *
 */
public class FromXLogConverter {
	final static XLifecycleExtension lfExt = XLifecycleExtension.instance();
	final static XConceptExtension cpExt=XConceptExtension.instance();
	final static XTimeExtension timeExt = XTimeExtension.instance();
	
	// we create spec from the attributes of trace and event. After testinng,
	// they don't overlap each other attributes
	static boolean testAttrOverlapping(XLog log) {
		for (XTrace trace : log) {
			System.out.println("Print the trace attributes");
			for (String attrKey : trace.getAttributes().keySet()) {
				
				System.out.println(attrKey);
				System.out.println(trace.getAttributes().get(attrKey));
			}
			for(XEvent event : trace) {
				System.out.println("Print the event attributes");
				for (String attrKey : event.getAttributes().keySet()) {
					System.out.println(attrKey);
					System.out.println(event.getAttributes().get(attrKey));
				}
			}
		}
		return false; // they donot overlap their attributes, so what we need to do is just to create the spec there 
	} 
	static void convert(XLog log, BufferedDataContainer buf, ExecutionContext exec) throws CanceledExecutionException {
		
		DataTableSpec spec = buf.getTableSpec();
		
		int colNum = spec.getColumnNames().length;
		int eventCount = 0;
		DataCell[] tCells = new DataCell[colNum];
		for (XTrace trace : log) {
			exec.checkCanceled();
			for (String attrKey : trace.getAttributes().keySet()) {
				int colIdx = spec.findColumnIndex(XLogSpecUtil.TRACE_ATTRIBUTE_PREFIX + attrKey);
				if(colIdx >= 0) {
					tCells[colIdx] = createDataCell(trace.getAttributes().get(attrKey));
				}
			}
			for(XEvent event : trace) {
				// test if this shallow copy, how?? If we change our values in trace if it changes too
				// but actually this helps a lot for thi
				DataCell[] cells = tCells.clone();
				
				// check the event attribute 
				for (String attrKey : event.getAttributes().keySet()) {
					int colIdx = spec.findColumnIndex(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX + attrKey);
					if(colIdx >= 0) {
						// if we already have the attributes there, put it there
						cells[colIdx] = createDataCell(event.getAttributes().get(attrKey));
					}
				}
				
				for(int i=0; i<colNum; i++) {
					if(cells[i]==null )
						cells[i] = new MissingCell("?");
				}
				
				DataRow eventRow = new DefaultRow("Event " + (eventCount++), cells);	
				buf.addRowToTable(eventRow);
			}
			
		}
		
	}
	
	static DataCell createDataCell(XAttribute attr) {
		
		if(attr instanceof XAttributeLiteral) {
			XAttributeLiteral tmp = (XAttributeLiteral) attr;
			return StringCellFactory.create(tmp.getValue());
		}else if(attr instanceof XAttributeBoolean) {
			XAttributeBoolean tmp = (XAttributeBoolean) attr;
			return BooleanCellFactory.create(tmp.getValue());
		}else if(attr instanceof XAttributeDiscrete) {
			XAttributeDiscrete tmp = (XAttributeDiscrete) attr;
			return IntCellFactory.create((int) tmp.getValue());
		}else if(attr instanceof XAttributeContinuous) {
			XAttributeContinuous tmp = (XAttributeContinuous) attr;
			return DoubleCellFactory.create(tmp.getValue());
		}else if(attr instanceof XAttributeTimestamp){
			XAttributeTimestamp tmp = (XAttributeTimestamp) attr;
			//TODO: convert the time format
			Instant instant = tmp.getValue().toInstant();
			
			// what if there is zoned information in the event log? what can we do it??
			/*
			ZonedDateTime ztime = instant.atZone(ZoneId.systemDefault());
			return ZonedDateTimeCellFactory.create(ztime);
			*/
			// to keep compatible with old formats, we use local time data here
			LocalDateTime ldt = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
			return LocalDateTimeCellFactory.create(ldt);
		}else
			System.out.println("Unknown attribute type there");
			
		return null;
	}

	/*
	 * if we only check the first trace and event log, it is possible that we don't have enough 
		information to get full spec!! My solution is?? We can read all log and then give the 
		specification. One possible solution, read more trace and event, 
		we sample 5 traces and read all its events.
		
		Nana, I tried again to extract the Spec for the table, but now it seems repeative work
	 */
	static DataTableSpec createAttrSpec(XLog log) {
		
		List<String> attrNames = new ArrayList();
		List<DataType> attrTypes = new ArrayList();
		// choose 5 random traces
		int size = (int) (0.2 * log.size())+1;
		Random random = new Random();
		List<Integer> traceIndices = new ArrayList();
		int i=0;
		while(i< size) {
			// 0 included but upper bound exclusive
			int idx = random.nextInt(log.size());
			if(!traceIndices.contains(idx)) {
				traceIndices.add(idx);
				i++;
			}
		}
		for(int idx : traceIndices) {
			XTrace trace = log.get(idx);
	//		System.out.println("Print the trace attributes");
			for (String attrKey : trace.getAttributes().keySet()) {
				if(attrNames.contains(XLogSpecUtil.TRACE_ATTRIBUTE_PREFIX + attrKey))
					continue;
				attrNames.add(XLogSpecUtil.TRACE_ATTRIBUTE_PREFIX + attrKey);
				attrTypes.add(findDataType(trace.getAttributes().get(attrKey)));
			}
			
			for(XEvent event :trace) {
				for (String attrKey : event.getAttributes().keySet()) {
					if(attrNames.contains(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX + attrKey))
						continue;
					attrNames.add(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX+ attrKey);
					attrTypes.add(findDataType(event.getAttributes().get(attrKey)));
				}
			}
		}
		
		XAttributeMap logAttrMap = log.getAttributes();
		// no need to get the log name
		String tName = XLog2TableConverterNodeModel.CFG_TABLE_NAME;
		
		String[] colNames = new String[attrNames.size()];
		attrNames.toArray(colNames);
		DataType[] colTypes = new DataType[attrTypes.size()];
		attrTypes.toArray(colTypes);
		
		DataTableSpec outSpec = new DataTableSpec(tName, colNames, colTypes);
		
		return outSpec;
	}
	// we need to get all the attributes of an xlog
	static DataTableSpec createGlobalSpec(XLog log) {
		
		System.out.println("Begin reading attributes");
		String tName = XLog2TableConverterNodeModel.CFG_TABLE_NAME;;
		
		
		List<String> attrNames = new ArrayList();
		List<DataType> attrTypes = new ArrayList();
		
		// what if people partially store sth. in global attributes, sth not there??
		// so we need to insist on the trace attribute, but don't forget the global attributes there
		
		// another thing if there are sth overlapping of event attributes and trace attributes
		// we need to have a test on it
		List<XAttribute> traceAttrs = log.getGlobalTraceAttributes();
		for(XAttribute attr : traceAttrs) {
			attrNames.add(XLogSpecUtil.TRACE_ATTRIBUTE_PREFIX +attr.getKey());
			attrTypes.add(findDataType(attr));
		}
		
		List<XAttribute> eventAttrs = log.getGlobalEventAttributes();
		
		for(XAttribute attr : eventAttrs) {
			attrNames.add(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX  + attr.getKey());
			attrTypes.add(findDataType(attr));
		}
		
		System.out.println("Finish reading attributes");
		// after getting all the attributes, we creat name and types for them
		String[] colNames = new String[attrNames.size()];
		attrNames.toArray(colNames);
		DataType[] colTypes = new DataType[attrTypes.size()];
		attrTypes.toArray(colTypes);
		
		// DataTableSpec.createColumnSpecs(colNames, colTypes);
		DataTableSpec outSpec = new DataTableSpec(tName, colNames, colTypes);
		
		return outSpec;
	}

	private static DataType findDataType(XAttribute attr) {
		// TODO Auto-generated method stub
		if(attr instanceof XAttributeLiteral) {
			return StringCell.TYPE;
		}else if(attr instanceof XAttributeBoolean) {
			return BooleanCell.TYPE;
		}else if(attr instanceof XAttributeDiscrete) {
			return IntCell.TYPE;
		}else if(attr instanceof XAttributeContinuous) {
			return DoubleCell.TYPE;
		}else if(attr instanceof XAttributeTimestamp){
			return DataType.getType(LocalDateTimeCell.class);
		}else
			System.out.println("Unknown attribute type there");
		
		return null;
	}
	public static void convertWithColRearranger(XLog log, ColumnRearranger c) {
		// this doesn't work, the reason is ColumnRearranger depends on input DataTable
		// to append more columns into this table. But our input is an XLog. 
		// to create an empty table for this, and then to create values from Xlog
		// the creation method limits.  
		// BufferedDataTable out = exec.createColumnRearrangeTable(in[0], c, exec);
		
	}
}
