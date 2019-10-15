package org.pm4knime.node.conversion.csv2log;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension;
import org.deckfour.xes.extension.std.XOrganizationalExtension;
import org.deckfour.xes.extension.std.XTimeExtension;
import org.deckfour.xes.extension.std.XLifecycleExtension.StandardModel;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.info.impl.XLogInfoImpl;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.BooleanCell;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.time.localdatetime.LocalDateTimeCell;
import org.knime.core.data.time.localdatetime.LocalDateTimeCellFactory;
import org.knime.core.node.BufferedDataTable;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVErrorHandlingMode;
import org.processmining.log.utils.XUtils;

/**
 * this class belongs to utility. But currently use is to create an EventLog from CSV file.
 * @author kefang-pads
 * @reference org.processmining.log.csvimport.handler.XESConversionHandlerImpl
 * @modify reason: add trace and event attribute sets. So we can assign them in category
 */
public class ToXLogConverter {

	private XFactory factory = null;
	
	private XLog log = null;
	private XTrace currentTrace = null;
	private List<XEvent> currentEvents = new ArrayList<>();
	private boolean errorDetected = false;
	
	private XEvent currentEvent = null;
	private XEvent currentStartEvent;
	private int instanceCounter = 0;
	
	// save trace attributes set
	private Map<String, DataCell> traceAttrMap = new HashMap<String, DataCell>();
	
	CSV2XLogConfigModel config = null;
	
	public void setConfig(CSV2XLogConfigModel config) {
		this.config = config;
		factory = config.getFactory();
	}
	
	
	/**
	 * convert CSV data table into xlog, but we need to know the column index,
	 * so we know which one is which event?
	 * @param logName
	 */
	public void convertCVS2Log(BufferedDataTable csvData) {
		
		// get trace and event attribute available sets here 
		List<String> traceColumns= config.getMTraceAttrSet().getIncludeList();
		// find the idx for it, for once 
		List<String> eventColumns= config.getMEventAttrSet().getIncludeList();
		
		int[] traceColIndices = csvData.getDataTableSpec().columnsToIndices(traceColumns.toArray(new String[0]));
		int[] eventColIndices = csvData.getDataTableSpec().columnsToIndices(eventColumns.toArray(new String[0]));
		boolean[] traceColVisited = new boolean[traceColIndices.length];
		boolean[] eventColVisited = new boolean[eventColIndices.length];
		
		int caseIDIdx = -1, eventIDIdx = -1, cTimeIdx = -1, sTimeIdx = -1;
		
		caseIDIdx = traceColumns.indexOf(config.getMCaseID().getStringValue());
		eventIDIdx = eventColumns.indexOf(config.getMEventID().getStringValue());
		cTimeIdx = eventColumns.indexOf(config.getMCompleteTime().getStringValue());
		
		traceColVisited[caseIDIdx] = true;
		eventColVisited[eventIDIdx] =true;
		eventColVisited[cTimeIdx] =true;
		if(config.isShouldAddStartEventAttributes()) {
			sTimeIdx = eventColumns.indexOf(config.getMCompleteTime().getStringValue());
			eventColVisited[sTimeIdx] = true;
		}
		String currentCaseID = "-1", newCaseID="";
		
		String cFormat = config.getMCFormat().getStringValue();
		String sFormat = null;
		if(config.isShouldAddStartEventAttributes())
			sFormat = config.getMSFormat().getStringValue();
		
		
		String logName = csvData.getSpec().getName();
		startLog(logName + "event log");
		
		for(DataRow row : csvData) {
			// when it is a integer or string, not matter, right?? 
			DataCell traceIDData = row.getCell(traceColIndices[caseIDIdx]);
			
			newCaseID = traceIDData.toString();
			
			if(!newCaseID.equals(currentCaseID)) {
				// we meet a new trace, end old one and begin new one
				if(!currentCaseID.equals("-1"))
					endTrace(currentCaseID); // make it as a string
				
				currentCaseID = newCaseID;
				startTrace(currentCaseID);
			}
			
			// get trace attributes 
			
			for(int tIdx = 0; tIdx< traceColIndices.length ; tIdx++) {
				if(traceColVisited[tIdx])
					continue; 
				if(traceAttrMap.containsKey(traceColumns.get(traceColIndices[tIdx]))) {
					// if contains value, compare if they are same 
					if(!traceAttrMap.get(traceColumns.get(tIdx)).equals(row.getCell(traceColIndices[tIdx]))) {
						System.out.println("Error happens with the trace Attributes here");
						errorDetected = true;
						break;
					}
				}else {
					// for the values there, we deal with it later 
					traceAttrMap.put(traceColumns.get(tIdx), row.getCell(traceColIndices[tIdx]));
				}
			}

			// deal with new event class, it can be in discrete value
			String eventClass = null;
			DataCell eventIDData = row.getCell(eventColIndices[eventIDIdx]);
			if(eventIDData.getType().equals(IntCell.TYPE)) {
				eventClass = ((IntCell)eventIDData).getIntValue() + "";
			}else if(eventIDData.getType().equals(StringCell.TYPE)) {
				eventClass = ((StringCell)eventIDData).getStringValue();
			}
				
			// read time stamp, one way, to convert due to the use of DataTable, 
			// another way, to convert them one by one
			
			// if we don't use the original convertion in knime, it should work
			try {
				// if the values there are also like this, what to do?? 
				// String cTime = ((StringCell) row.getCell(eventColIndices[cTimeIdx])).getStringValue();
				String cTime = row.getCell(eventColIndices[cTimeIdx]).toString();
				Date cTimeDate = convertString2Date(cFormat, cTime);
				Date sTimeDate = null;
				if(config.isShouldAddStartEventAttributes()) {
					sTimeIdx = eventColumns.indexOf(config.getMCompleteTime().getStringValue());
					// String sTime = ((StringCell) row.getCell(eventColIndices[sTimeIdx])).getStringValue();// need to make sure the format same??
					String sTime = row.getCell(eventColIndices[sTimeIdx]).toString();
					sTimeDate = convertString2Date(sFormat, sTime);
				}
				startEvent(eventClass, cTimeDate, sTimeDate);
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// after this, we process other attributes, like resource, costs;; At this point, we need to differ their types 
			// and add attributes to the currentEventClass...
			for(int eIdx =0; eIdx< eventColIndices.length; eIdx++) {
				if(eventColVisited[eIdx])
					continue;

				DataCell otherData = row.getCell(eventColIndices[eIdx]);
				String attrName = eventColumns.get(eIdx);
				assignAttributeWithDataCell(currentEvent, otherData, attrName);
			}
			
			endEvent();
		}
		// Close last trace
		endTrace(currentCaseID + "");	
	}
	
	private void assignAttributeWithDataCell(XAttributable currentObj,DataCell otherData, String attrName) {
		
		if(otherData.getType().equals(IntCell.TYPE)){
			IntCell iCell = (IntCell) otherData;
			// here we set extension as null, but later we should improve it
			assignAttribute(currentObj, factory.createAttributeDiscrete(attrName, iCell.getIntValue(), null));
		
		}else if(otherData.getType().equals(DoubleCell.TYPE)){
			DoubleCell dCell = (DoubleCell) otherData;
			// here we set extension as null, but later we should improve it
			assignAttribute(currentObj, factory.createAttributeContinuous(attrName ,dCell.getDoubleValue(), null));
		
		}else if(otherData.getType().equals(StringCell.TYPE)){
			StringCell sCell = (StringCell) otherData;
			// here we set extension as null, but later we should improve it
			assignAttribute(currentObj, factory.createAttributeLiteral(attrName ,sCell.getStringValue(), null));
		
		}else if(otherData.getType().equals(BooleanCell.TYPE)){
			BooleanCell bCell = (BooleanCell) otherData;
			// here we set extension as null, but later we should improve it
			assignAttribute(currentObj, factory.createAttributeBoolean(attrName ,bCell.getBooleanValue(), null));
		}else if(otherData.getType().equals(LocalDateTimeCellFactory.TYPE)){
			LocalDateTimeCell tCell = (LocalDateTimeCell) otherData;
			LocalDateTime ldt = tCell.getLocalDateTime();
			Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
			assignAttribute(currentObj, factory.createAttributeTimestamp(attrName, date, null));
		}else if(otherData.isMissing()) {
			// System.out.println("Missing values to attribute " + attrName); // but still we can assign it there to show missing
			assignAttribute(currentObj, factory.createAttributeLiteral(attrName ,otherData.toString(), null));
			
		}else {
			System.out.println("Unknown data type");
		}
		// here could be DateTime type, but how to say it ??
		
	}
	
	/*
	 * create a log file w.r.t. DataTable input here
	 */
	public void startLog(String logName) {
		log = factory.createLog();
		
		assignName(factory, log, logName);
		// assign EventName Classifier to log
		log.getExtensions().add(XConceptExtension.instance());
		log.getClassifiers().add(XLogInfoImpl.NAME_CLASSIFIER);
		
		// assign time stamp related attributes
		log.getExtensions().add(XTimeExtension.instance());
		log.getExtensions().add(XLifecycleExtension.instance());
		log.getClassifiers().add(XUtils.STANDARDCLASSIFIER);
		
		// add other extensions for each column here
		// for organization 
		XExtension orgExt =XOrganizationalExtension.instance();	
		log.getExtensions().add(orgExt);
		
	}
	
	public void startTrace(String caseId) {
		currentEvents.clear();
		// clear map and begin a new one
		traceAttrMap.clear();
		errorDetected = false;
		currentTrace = factory.createTrace();
		
		assignName(factory, currentTrace, caseId);
	}

	
	
	public void endTrace(String caseId) {
		if (errorDetected && config.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_TRACE_ON_ERROR) {
			// Skip the entire trace
			return;
		}
		
		currentTrace.addAll(currentEvents);
		// add trace attribute to currentTrace 
		for(String attrKey : traceAttrMap.keySet()) {
			DataCell data = traceAttrMap.get(attrKey);
			assignAttributeWithDataCell(currentTrace, data, attrKey);
		}
		
		log.add(currentTrace);
	}
	
	public void startEvent(String eventClass, Date completionTime, Date startTime) {
		if (config.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_EVENT_ON_ERROR) {
			// Include the other events in that trace
			errorDetected = false;
		}
		
		currentEvent = factory.createEvent();
		if (eventClass != null) {
			assignName(factory, currentEvent, eventClass);
		}

		if (startTime == null && completionTime == null) {
			// Both times are unknown only create an event assuming it is the completion event
			assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.COMPLETE);
		} else if (startTime != null && completionTime != null) {
			// Both start and complete are present
			String instance = String.valueOf((instanceCounter++));

			// Assign attribute for complete event (currentEvent)			
			assignTimestamp(factory, currentEvent, completionTime);
			assignInstance(factory, currentEvent, instance);
			assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.COMPLETE);

			// Add additional start event
			currentStartEvent = factory.createEvent();
			if (eventClass != null) {
				assignName(factory, currentStartEvent, eventClass);
			}
			assignTimestamp(factory, currentStartEvent, startTime);
			assignInstance(factory, currentStartEvent, instance);
			assignLifecycleTransition(factory, currentStartEvent, XLifecycleExtension.StandardModel.START);

		} else {
			// Either start or complete are present
			if (completionTime != null) {
				// Only create Complete
				assignTimestamp(factory, currentEvent, completionTime);
				assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.COMPLETE);
			} else if (startTime != null) {
				// Only create Start
				assignTimestamp(factory, currentEvent, startTime);
				assignLifecycleTransition(factory, currentEvent, XLifecycleExtension.StandardModel.START);
			} else {
				throw new IllegalStateException(
						"Both start and complete time are NULL. This should never be the case here!");
			}
		}
	}

	
	public void endEvent() {
		if (errorDetected && config.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_EVENT_ON_ERROR) {
			// Do not include the event
			return;
		}
		// Add start event before complete event to guarantee order for events with same time-stamp
		if (currentStartEvent != null) {
			currentEvents.add(currentStartEvent);
			currentStartEvent = null;
		}
		currentEvents.add(currentEvent);
		currentEvent = null;
		
	}
	
	
	public XLog getXLog() {
		return log;
	}
	
	private static void assignAttribute(XAttributable a, XAttribute value) {
		XUtils.putAttribute(a, value);
	}

	private static void assignLifecycleTransition(XFactory factory, XAttributable a, StandardModel lifecycle) {
		assignAttribute(a, factory.createAttributeLiteral(XLifecycleExtension.KEY_TRANSITION, lifecycle.getEncoding(),
				XLifecycleExtension.instance()));
	}

	private static void assignInstance(XFactory factory, XAttributable a, String value) {
		assignAttribute(a,
				factory.createAttributeLiteral(XConceptExtension.KEY_INSTANCE, value, XConceptExtension.instance()));
	}

	private static void assignTimestamp(XFactory factory, XAttributable a, Date value) {
		assignAttribute(a,
				factory.createAttributeTimestamp(XTimeExtension.KEY_TIMESTAMP, value, XTimeExtension.instance()));
	}

	private static void assignName(XFactory factory, XAttributable a, String value) {
		assignAttribute(a,
				factory.createAttributeLiteral(XConceptExtension.KEY_NAME, value, XConceptExtension.instance()));
	}
	
	// convert string to DateTime there, one easy solution is to delete the zone in data and time
	public Date convertString2Date(String format, String value) throws ParseException {
		// we need the predefined format in knime 
		
		DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
		LocalDateTime ldt = LocalDateTime.parse(value, df);
		Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		return date;
		// return Instant.parse(value).toDate();
	}
	
}
