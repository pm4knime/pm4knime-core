package org.pm4knime.node.logmanipulation.merge.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
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
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.XLogUtil;

public class MergeTableUtil {
	public static final String CFG_ATTRIBUTE_SUFFIX = "-new";
	public static final String LOG_0_PREFIX = "log0:";
	public static final String LOG_1_PREFIX = "log1:";
	
	public static XAttribute cloneAttribute(XAttribute xattr, String newKey) {
		if (xattr instanceof XAttributeContinuous) {
			return new XAttributeContinuousImpl(newKey, ((XAttributeContinuous) xattr).getValue());
		} else if (xattr instanceof XAttributeDiscrete) {
			return new XAttributeDiscreteImpl(newKey, ((XAttributeDiscrete) xattr).getValue());
		} else if (xattr instanceof XAttributeLiteral) {
			return new XAttributeLiteralImpl(newKey, ((XAttributeLiteral) xattr).getValue());
		} else if (xattr instanceof XAttributeTimestamp) {
			return new XAttributeTimestampImpl(newKey, ((XAttributeTimestamp) xattr).getValue());
		}
		return null;
	}
	public static XEvent findEvent(XTrace trace, XEvent event, String eKeyInTrace, String eKey) {
		// TODO check if one trace contains the event with the current event classifier
		if (trace.contains(event))
			return event;

		XAttribute eAttr = event.getAttributes().get(eKey);
		for (XEvent e : trace) {
			XAttribute attr = e.getAttributes().get(eKeyInTrace);
			if (attr instanceof XAttributeLiteral) {
				if (((XAttributeLiteral) attr).getValue().equals(((XAttributeLiteral) eAttr).getValue()))
					return e;
			} else if (attr instanceof XAttributeDiscrete) {
				if (((XAttributeDiscrete) attr).getValue() == ((XAttributeDiscrete) eAttr).getValue())
					return e;
			}
		}

		return null;
	}
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
    

	// merge log with ignore second log strategy. But simple ignore the trace
	public static XLog mergeLogsIgnoreTrace(XLog log0, XLog log1, List<String> tKeys, ExecutionContext exec)
			throws CanceledExecutionException {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Table");
		mlog.getAttributes().putAll(log1.getAttributes());
		mlog.getGlobalTraceAttributes().addAll(log1.getGlobalTraceAttributes());
		mlog.getGlobalEventAttributes().addAll(log1.getGlobalEventAttributes());

		Set<String> caseIDSet = new HashSet();

		// put the first event log into the mlog
		for (XTrace trace : log0) {
			exec.checkCanceled();
			mlog.add((XTrace) trace.clone());

			// we need to get the values from it.. If we have string format
			// it is totally fine for me
			String attrValue = trace.getAttributes().get(tKeys.get(0)).toString();
			caseIDSet.add(attrValue);
		}

		// put the first event log into the mlog
		for (XTrace trace : log1) {
			exec.checkCanceled();
			String attrValue = trace.getAttributes().get(tKeys.get(1)).toString();
			if (!caseIDSet.contains(attrValue)) {
				mlog.add((XTrace) trace.clone());
			}

		}
		return mlog;
	}

	

	// merge log with ignore second log strategy. But simple ignore the trace,
	// do they have conflicts on some attributes?? I think so, so we need to deal
	// with the trace attributes
	// by using with one, we need to do this??
	// some attributes need change.
	public static XLog mergeLogsSeparateEvent(XLog log0, XLog log1, List<String> tKeys,
			List<XAttribute> exTraceAttrList0, List<XAttribute> traceAttrList1, ExecutionContext exec)
			throws CanceledExecutionException {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());

		for (XAttribute attr : traceAttrList1) {
			String newKey = attr.getKey() + CFG_ATTRIBUTE_SUFFIX;
			mlog.getGlobalTraceAttributes().add(cloneAttribute(attr, newKey));
		}

		Map<String, XTrace> tMap = new HashMap();

		// put the first event log into the mlog
		for (XTrace trace : log0) {
			exec.checkCanceled();
			XTrace nTrace = (XTrace) trace.clone();
			String attrValue = trace.getAttributes().get(tKeys.get(0)).toString();
			tMap.put(attrValue, nTrace);
		}

		// put the first event log into the mlog
		for (XTrace trace : log1) {
			exec.checkCanceled();
			String attrValue = trace.getAttributes().get(tKeys.get(1)).toString();

			if (tMap.keySet().contains(attrValue)) {

				XTrace oTrace = tMap.get(attrValue);
				// add all event from the current to oTrace
				for (XEvent event : trace) {
					oTrace.insertOrdered((XEvent) event.clone());
				}

				// only for the ones with same caseID, we do it!!
				for (XAttribute exAttr : exTraceAttrList0)
					oTrace.getAttributes().remove(exAttr.getKey());
				for (XAttribute inAttr : traceAttrList1) {
					String newKey = inAttr.getKey() + CFG_ATTRIBUTE_SUFFIX;
					// naming is important to show it out. fix the global attributes
					oTrace.getAttributes().put(newKey, cloneAttribute(inAttr, newKey));
				}

			} else // without the same identifier
				tMap.put(attrValue, trace);

		}
		// convert map to mlog
		mlog.addAll(tMap.values());

		return mlog;
	}

	// if we want to use the merge with attributes and event attributes, the ways to
	// do this is::
	// if they have the same caseId, then we merge them by using the trace and event
	// attributes definde by before
	// if they don`t have the same caseId, they we put them together
	// another way to merge is if we ignore the ones with the same event in xlog.
	// if not ignore, we put the events in the same trace.. But we might need sth
	// attributes from them
	// to delete certain attributes, we can create another node extension for this.
	// Add or delete attributes
	// for trace or event attributes there. We can handle this one. Waht we can do,
	// is to put them together
	// we don't need to choose the attribtues, just the strategy listed there
	//
	// exTraceAttrList0, exEventAttrList0 is empty and traceAttrList1 and
	// eventAttrList1 includes all the attributes
	// we think they are separate traces and with different IDs.
	// we can have one indicator for this. if separate, or not. IF they are
	// separate, we insert all events there
	public static XLog mergeLogsInternal(XLog log0, XLog log1, List<String> tKeys, List<String> eKeys,
			List<XAttribute> exTraceAttrList0, List<XAttribute> exEventAttrList0, List<XAttribute> traceAttrList1,
			List<XAttribute> eventAttrList1, ExecutionContext exec) throws CanceledExecutionException {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());
		// the attributes we remove is only for the traces with the same caseID. So it
		// means at end, we need to keep them all??
		// for other traces, we keep the old attributes there
		for (XAttribute attr : traceAttrList1) {
			String newKey = attr.getKey() + CFG_ATTRIBUTE_SUFFIX;
			mlog.getGlobalTraceAttributes().add(cloneAttribute(attr, newKey));
		}

		for (XAttribute attr : eventAttrList1) {
			String newKey = attr.getKey() + CFG_ATTRIBUTE_SUFFIX;
			mlog.getGlobalEventAttributes().add(cloneAttribute(attr, newKey));
		}

		// how to find and change the trace with the same id ramdomly??
		// create hash map, for the caseId and trace itself
		Map<String, XTrace> tMap = new HashMap();

		for (XTrace trace : log0) {
			exec.checkCanceled();
			XTrace nTrace = (XTrace) trace.clone();
			String attrValue = trace.getAttributes().get(tKeys.get(0)).toString();// delete the attributes here with the
																					// same names... But are we talking
																					// about
			tMap.put(attrValue, nTrace);
//				mlog.add(nTrace);
		}

		// put the first event log into the mlog
		for (XTrace trace : log1) {
			exec.checkCanceled();
			XTrace nTrace = (XTrace) trace.clone();
			String attrValue = trace.getAttributes().get(tKeys.get(1)).toString();// delete the attributes here with the
																					// same names... But are we talking
																					// about

			if (tMap.keySet().contains(attrValue)) {
				// combine them together for one event and one
				XTrace oTrace = tMap.get(attrValue);
				for (XEvent event : trace) {
					// just for the event with same eventID, we do this operation to make them as
					// one
					XEvent fe = findEvent(oTrace, event, eKeys.get(0), eKeys.get(1));
					if (fe != null) {
						// check this event attributes and other attributes from this one

						// fe delete the exclused list and add addition ones
						for (XAttribute exAttr : exEventAttrList0)
							fe.getAttributes().remove(exAttr.getKey());
						// add additional attr from log1
						for (XAttribute inAttr : eventAttrList1) {
							// here we should add values from the current event here
							String newKey = inAttr.getKey() + CFG_ATTRIBUTE_SUFFIX;
							// because it is a map, so if we use the same key, it's going to rewrite the
							// values
							// so the ways to do it, change the key for the new added attributes here.
							fe.getAttributes().put(newKey, cloneAttribute(inAttr, newKey));

						}
					} else {
						// for different ones, keep them as they are, for excluded part, also
						oTrace.insertOrdered((XEvent) event.clone());
					}

				}

				// deal with the attributes for trace
				for (XAttribute exAttr : exTraceAttrList0)
					oTrace.getAttributes().remove(exAttr.getKey());
				for (XAttribute inAttr : traceAttrList1) {
					String newKey = inAttr.getKey() + CFG_ATTRIBUTE_SUFFIX;
					// naming is important to show it out. fix the global attributes
					oTrace.getAttributes().put(newKey, cloneAttribute(inAttr, newKey));
				}

			} else {
				tMap.put(attrValue, nTrace);
			}

		}

		// convert map to mlog
		mlog.addAll(tMap.values());
		return mlog;
	}
}
