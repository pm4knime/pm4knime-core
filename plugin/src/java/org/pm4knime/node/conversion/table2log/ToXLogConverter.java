package org.pm4knime.node.conversion.table2log;

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
import org.pm4knime.settingsmodel.SMTable2XLogConfig;
import org.pm4knime.util.XLogSpecUtil;
import org.pm4knime.util.XLogUtil;
import org.processmining.log.csvimport.config.CSVConversionConfig.CSVErrorHandlingMode;
import org.processmining.log.utils.XUtils;

/**
 * this class belongs to utility. But currently use is to create an EventLog from CSV file.
 * @author kefang-pads
 * @reference org.processmining.log.csvimport.handler.XESConversionHandlerImpl
 * @modify reason: add trace and event attribute sets. So we can assign them in category
 * @modify 16 Dec 2019. To change the model to add lifecycle transition for building event log.  
 */
public class ToXLogConverter {

	private XFactory factory = null;
	
	private XLog log = null;
	private XTrace currentTrace = null;
	private List<XEvent> currentEvents = new ArrayList<>();
	private boolean errorDetected = false;
	
	private XEvent currentEvent = null;
	private XEvent currentStartEvent;
	// need to check if we have the available values for it. But else, we don't need here
//	private int instanceCounter = 0;
	
	// save trace attributes set
	private Map<String, DataCell> traceAttrMap = new HashMap<String, DataCell>();
	
	DateTimeFormatter df ; //;
	
	SMTable2XLogConfig config = null;
	
	public void setConfig(SMTable2XLogConfig m_config) {
		this.config = m_config;
		factory = m_config.getFactory();
	}
	
	
	/**
	 * convert CSV data table into xlog, but we need to know the column index,
	 * so we know which one is which event?
	 * @param logName
	 */
	public void convertDataTable2Log(BufferedDataTable csvData) {
		
		// get trace and event attribute available sets here 
		List<String> traceColumns= config.getMTraceAttrSet().getIncludeList();
		// find the idx for it, for once 
		List<String> eventColumns= config.getMEventAttrSet().getIncludeList();
		
		int[] traceColIndices = csvData.getDataTableSpec().columnsToIndices(traceColumns.toArray(new String[0]));
		int[] eventColIndices = csvData.getDataTableSpec().columnsToIndices(eventColumns.toArray(new String[0]));
		boolean[] traceColVisited = new boolean[traceColIndices.length];
		boolean[] eventColVisited = new boolean[eventColIndices.length];
		
		int caseIDIdx = -1, eventClassIdx, tsIdx = -1;
		
		caseIDIdx = traceColumns.indexOf(config.getMCaseID().getStringValue());
		eventClassIdx = eventColumns.indexOf(config.getMEventClass().getStringValue());
		// complete time the time stamp here in default
		tsIdx = eventColumns.indexOf(config.getMTimeStamp().getStringValue());
		String tsFormat = config.getMTSFormat().getStringValue();
		df = DateTimeFormatter.ofPattern(tsFormat);
		
		// optional for lifecycle column, but there is no need to specify the event ID for it!!  Lifecycle is useful!!
		boolean withLifecycle = false; 
		int  lifecycleIdx = -1;
		if(!config.getMLifecycle().getStringValue().equals(SMTable2XLogConfig.CFG_NO_OPTION)) {
			// exception happens, when eventAttrSet excluses life-cycle column, which one is the optimal choices?
			// if we choose lifecycle there, then we should keep it into our event attr!! 
			// only when it is no-available, it can be excluded. But we test it in configuration part.
			withLifecycle = true;
			lifecycleIdx = eventColumns.indexOf(config.getMLifecycle().getStringValue());
			eventColVisited[lifecycleIdx] =  true;
		}
		
		traceColVisited[caseIDIdx] = true;
		eventColVisited[eventClassIdx] =true;
		eventColVisited[tsIdx] =true;
		
		
		String currentCaseID = "-1", newCaseID="";
		
		String logName = csvData.getSpec().getName();
		startLog(logName + " event log");
		
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
			DataCell eventClassData = row.getCell(eventColIndices[eventClassIdx]);
			eventClass = ((StringCell)eventClassData).getStringValue();
			
				
			try {
				// if the values there are also like this, what to do?? 
				// String cTime = ((StringCell) row.getCell(eventColIndices[cTimeIdx])).getStringValue();
				String cTime = row.getCell(eventColIndices[tsIdx]).toString();
				Date timeStamp = convertString2Date( cTime);
				
				// here we check the lifecycle transition and assign the values to it!! 
				String lifecycle = null ;
				if(withLifecycle) {
					// here we need to get the value from the column
					DataCell lifecycleData = row.getCell(eventColIndices[lifecycleIdx]);
					lifecycle = ((StringCell)lifecycleData).getStringValue();
					
				}
				
				
				startEvent(eventClass, timeStamp, lifecycle);
				
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
		
		endLog();
	}
	
	private void assignAttributeWithDataCell(XAttributable currentObj, DataCell otherData, String attrName) {
		// check if the attrName has the prefix of the event attributes, or not.
		// should we retrieve it back to the exact event log?? I think yes. We shouldn't change any information
		// to split here. TO avoid the additional prefix
		if(attrName.startsWith(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX))
			attrName = attrName.split(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX)[1];
		else if(attrName.startsWith(XLogSpecUtil.TRACE_ATTRIBUTE_PREFIX))
			attrName = attrName.split(XLogSpecUtil.TRACE_ATTRIBUTE_PREFIX)[1];
		 
		// add attributes to the log, we need to know the type of it.
		
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
	
	// we end the log by assigning the global attributes			
	public void endLog() {
		if(!log.isEmpty()) {
			// after this operation, traverse the trace and get its attributes from it
			// better way to do this is to assign it after this operation!!!
			List<XAttribute> tAttrs = XLogUtil.getTAttributes(log, 0.2);
			List<XAttribute> eAttrs = XLogUtil.getEAttributes(log, 0.2);
			log.getGlobalTraceAttributes().addAll(tAttrs);
			log.getGlobalEventAttributes().addAll(eAttrs);
		}
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
	
	public void startEvent(String eventClass, Date timeStamp, String lifecycle) {
		if (config.getErrorHandlingMode() == CSVErrorHandlingMode.OMIT_EVENT_ON_ERROR) {
			// Include the other events in that trace
			errorDetected = false;
		}
		
		currentEvent = factory.createEvent();
		
		assignName(factory, currentEvent, eventClass);
		assignTimestamp(factory, currentEvent, timeStamp);
		
//		if(instance!=null)
//			assignInstance(factory, currentEvent, instance);
		// just add the time stamp with the corresponding lifecycle
		if(lifecycle != null) {
			// find the corresponding conversion for the lifecycle to standard model change!
			assignLifecycleTransition(factory, currentEvent, lifecycle);
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

	private static void assignLifecycleTransition(XFactory factory, XAttributable a, String lifecycle) {
		// here we don't use the standard model implictly. How to get the transition value??
		// StandardModel lcModel = StandardModel.valueOf(lifecycle);
		// this is one way, another way, we just assign the lifecycle transition directly as one attribute
//		XesLifecycleTransition lfTransition = XesLifecycleTransition.valueOf(lifecycle);
//		
//		assignAttribute(a, factory.createAttributeLiteral(XLifecycleExtension.KEY_TRANSITION, lfTransition.getTransition(),
//				XLifecycleExtension.instance()));
		
		assignAttribute(a, factory.createAttributeLiteral(XLifecycleExtension.KEY_TRANSITION, lifecycle,
				XLifecycleExtension.instance()));
	}
	

	private static void assignName(XFactory factory, XAttributable a, String value) {
		// here if we assign it as the concept:name or as the activity?? If at end, all the 
		assignAttribute(a,
				factory.createAttributeLiteral(XConceptExtension.KEY_NAME, value, XConceptExtension.instance()));
	}


	private static void assignTimestamp(XFactory factory, XAttributable a, Date value) {
		assignAttribute(a,
				factory.createAttributeTimestamp(XTimeExtension.KEY_TIMESTAMP, value, XTimeExtension.instance()));
	}
	
	// convert string to DateTime there, one easy solution is to delete the zone in data and time
	public Date convertString2Date(String value) throws ParseException {
		// local time is ok now, use another simple format to process the date
		// Error: with the parse, and also errors with the reloading process
		LocalDateTime ldt = LocalDateTime.parse(value, df);
		Date date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		return date;
		// return Instant.parse(value).toDate();
	}

	
}
