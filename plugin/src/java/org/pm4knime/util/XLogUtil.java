package org.pm4knime.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;

/*
 * this class is created  to include the utility used to deal with event log in XLog format
 */
public class XLogUtil {
	
	public static final String CFG_DUMMY_ECNAME = "Dummy Event Class";
	public static final String CFG_EVENTCLASSIFIER_NAME = "Event Classifier";
	public static final String CFG_STRING_SEPERATOR = "::";
	
	// serialize the event class into string
	public static String serializeEventClass(XEventClass ecls) {
		String objString = ecls.getClass().getName() + CFG_STRING_SEPERATOR +
				ecls.getId() + CFG_STRING_SEPERATOR +
				ecls.getIndex() + CFG_STRING_SEPERATOR +
				ecls.size();
		return objString;
	}
	// deserialize event class from string
	public static XEventClass deserializeEventClass(String objString) {
		String[] attrs = objString.split(CFG_STRING_SEPERATOR);
		if(attrs[0].equals(XEventClass.class.getName())) {
			XEventClass ecls  = new XEventClass(attrs[1], Integer.parseInt(attrs[2]));
			ecls.setSize(Integer.parseInt(attrs[3]));
			return ecls;
		}
		
		return null;
	}
	
	// serialize the event classifier
	public static String serializeEventClassifier(XEventClassifier eClassifier) {
		
		String objString = eClassifier.getClass().getName() + CFG_STRING_SEPERATOR + 
				eClassifier.name();
		// because we can save it, so here we don't use it 
//		for(String value : eClassifier.getDefiningAttributeKeys()) {
//			objString += value + CFG_STRING_SEPERATOR;
//		}
		return objString;
	}
	
	// deserialize event class from string
	public static XEventClassifier deserializeEventClassifier(String objString) {
		String[] attrs = objString.split(CFG_STRING_SEPERATOR);
		if(attrs[0].equals(XEventNameClassifier.class.getName())) {
			
			XEventClassifier eClassifier  = new XEventNameClassifier();
			eClassifier.setName(attrs[1]);
			return eClassifier;
		}else if(attrs[0].equals(XEventLifeTransClassifier.class.getName())) {
			XEventClassifier eClassifier  = new XEventLifeTransClassifier();
			eClassifier.setName(attrs[1]);
			return eClassifier;
		}else {
			System.out.println("Unknowm event classifier");
		}
		
		return null;
	}
		
	
	// this function can be included into the event classifier
	public static List<String> getECNames(List<XEventClassifier> classifierList) {
		// TODO Auto-generated method stub
		List<String> classifierNames = new ArrayList();
		for (XEventClassifier clf : classifierList) {
			classifierNames.add(clf.name());
		}
		return classifierNames;
	}

	
	public static XEventClassifier getXEventClassifier(String clfName, List<XEventClassifier> classifierList) {
		// TODO Auto-generated method stub
    	for(XEventClassifier clf : classifierList) {
 			if(clf.name().equals(clfName))
 				return clf;
 		}
 		return null;
	}
	
	public static void saveLog(XLog log, OutputStream objOut) {
		XSerializer serializer = new XesXmlSerializer();
		try {
			serializer.serialize(log, objOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static XLog loadLog(InputStream objIn) {
		XParser parser = new XesXmlParser(XFactoryRegistry.instance().currentDefault());
		List<XLog> log = new ArrayList<>();
		try {
			// if we give it a new InputStream, to avoid its implicit close
			// any stream based on the current stream will be closed. 
			// So there is no real solution for this.
			log = parser.parse(objIn);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return log.get(0);
		
	}
	
	public static XEventClass findEventClass(String eventName, Collection<XEventClass> eventClasses) {
		// TODO Auto-generated method stub
		for(XEventClass eClass : eventClasses) {
			if(eventName.equals(eClass.getId()))
				return eClass;
		}
		
		return null;
	}
	
	/*
	 * this function collects the event classes from log and outputs the names in the sorted order 
	 */
	public static List<String> extractAndSortECNames(XLog log, XEventClassifier classifier){
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
		Collection<XEventClass> eventClasses =  summary.getEventClasses().getClasses();
		SortedSet<String> ecSet = new TreeSet<>();
		for(XEventClass ec : eventClasses) {
			ecSet.add(ec.getId());
		}
		List<String> nameList = new ArrayList<String>();
		nameList.addAll(ecSet);
		return nameList;
	}
	
	// one function to get the event classifier without log
	public static List<XEventClassifier> getECList(){
		List<XEventClassifier> classifierList= new ArrayList();
		classifierList.add(new XEventNameClassifier());
		classifierList.add(new XEventLifeTransClassifier());
		return classifierList;
	}
	
	// one function to get the event classifier with  log
	public static List<XEventClassifier> getECList(XLog log){
		// TODO : the difference is not clear
		List<XEventClassifier> classifierList= new ArrayList();
		classifierList.add(new XEventNameClassifier());
		classifierList.add(new XEventLifeTransClassifier());
		return classifierList;
	}
	
	// get the event attributes available for the time stamp from event log
	public static List<String> getTSAttrNames(XLog log){
		// get the event attributes types, like start time and complete time
		// after checking this, we get the key names. They are the same.
		// so what we get is the keySet..
		List<XAttribute> attrList = getTAttributes(log, 0.2);
		List<String> attrNames = new ArrayList<String>();
		
		// the result is reliable, we need to traverse the log and trace for this
		for(XAttribute attr : attrList) {
			if(attr instanceof XAttributeTimestamp) {
				attrNames.add(attr.getKey());
			}
		}
		return attrNames;
	}
	
	// get the event attribute of an event log
	public static List<XAttribute> getEAttributes(XLog log, double percent){
		// the result is not reliable, so traverse each trace there
		List<XAttribute> attrList = log.getGlobalEventAttributes();
		List<String> attrNames = getAttrNameList(attrList);
		int num = (int) (log.size() * percent);
		// we should count the number of visited values there
		for(XTrace trace: log) {
			for(XEvent event: trace) {
				for(String attrKey : event.getAttributes().keySet()) {
					if(!attrNames.contains(attrKey))
						attrList.add(event.getAttributes().get(attrKey));
				}
			}
			num--;
			if(num < 0)
				break;
		}
		
		return attrList;
	}
	
	// get the trace attributes of an event log... 
	// but if we want to acess the attributes, we need its keys for this...
	public static List<XAttribute> getTAttributes(XLog log, double percent){
		// the result is not reliable, so traverse each trace there
		List<XAttribute> attrList = log.getGlobalTraceAttributes();
		List<String> attrNames = getAttrNameList(attrList);
		int num = (int) (log.size() * percent);
		// we should count the number of visited values there
		for(XTrace trace: log) {
			for(String attrKey : trace.getAttributes().keySet()) {
				if(!attrNames.contains(attrKey))
					attrList.add(trace.getAttributes().get(attrKey));
			}
			num--;
			if(num < 0)
				break;
		}
		
		return attrList;
	}
	
	public static List<String> getAttrNameList(List<XAttribute> attrList){
		List<String> attrNames = new ArrayList();
		for(XAttribute attr : attrList) {
			attrNames.add(attr.getKey());
		}
		return attrNames;
	}
	
	// get the class for attribute according to atrribute name
	public static Class<?> getAttrClass(XLog log, String attrName){
		XAttribute xattr = null ;
		for(XTrace trace : log) {
			for(XEvent event : trace) {
				
				if(event.getAttributes().keySet().contains(attrName)) {
					xattr = event.getAttributes().get(attrName);
					return getAttrClass(xattr);
				}
			}
				
		}
		throw new IllegalArgumentException("Not supported attribute name");
	}
	
	// get the class for one attribute
	public static Class<?> getAttrClass(XAttribute xattr){
		if (xattr instanceof XAttributeTimestamp) {
			return java.util.Date.class;
		} else if (xattr instanceof XAttributeContinuous) {
			return Double.class;
		} else if (xattr instanceof XAttributeDiscrete) {
			return Integer.class;
		}
		throw new IllegalArgumentException("Not supported data type");
	}
	
	// merge two event logs with separate strategy
	public static XLog mergeLogsSeparate(XLog log0, XLog log1) {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());
		mlog.getGlobalTraceAttributes().addAll(log1.getGlobalTraceAttributes());
		mlog.getGlobalEventAttributes().addAll(log1.getGlobalEventAttributes());
		
		// put the first event log into the mlog
		for(XTrace trace: log0) {
			mlog.add((XTrace) trace.clone());
		}
		
		// put the first event log into the mlog
		for(XTrace trace: log1) {
			mlog.add((XTrace) trace.clone());
		}
		
		return mlog;
	}
	
	// merge log with ignore second log strategy. But simple ignore the trace
	public static XLog mergeLogsIgnoreTrace(XLog log0, XLog log1) {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());
		mlog.getGlobalTraceAttributes().addAll(log1.getGlobalTraceAttributes());
		mlog.getGlobalEventAttributes().addAll(log1.getGlobalEventAttributes());
		
		Set<Long> caseIDSet = new HashSet();
		String key = "concept:name";
		// put the first event log into the mlog
		for(XTrace trace: log0) {
			mlog.add((XTrace) trace.clone());
			XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(key);
			caseIDSet.add(attr.getValue());
		}
		
		// put the first event log into the mlog
		for(XTrace trace: log1) {
			XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(key);
			if(!caseIDSet.contains(attr.getValue())) {
				
				mlog.add((XTrace) trace.clone());
				caseIDSet.add(attr.getValue());
			}
			
		}
		
		return mlog;
	}
	
	
	// merge log with ignore second log strategy. But simple ignore the trace
	public static XLog mergeLogsIgnoreEvent(XLog log0, XLog log1) {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());
		mlog.getGlobalTraceAttributes().addAll(log1.getGlobalTraceAttributes());
		mlog.getGlobalEventAttributes().addAll(log1.getGlobalEventAttributes());
		
		Map<Long, XTrace> tMap = new HashMap();
		String key = "concept:name";
		// put the first event log into the mlog
		for(XTrace trace: log0) {
			XTrace nTrace = (XTrace) trace.clone();
			XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(key);
			tMap.put(attr.getValue(), nTrace);
		}
		
		// put the first event log into the mlog
		for(XTrace trace: log1) {
			XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(key);
			long caseID = attr.getValue();
			if(!tMap.keySet().contains(caseID)) {
				XTrace nTrace = (XTrace) trace.clone();
				tMap.put(attr.getValue(), nTrace);
			}else {
				// they have the same caseID, ignore the repetive events from second traces
				XTrace oTrace = tMap.get(caseID);
				
				// check the events in trace if they are in the current trace
				// the trace, we can't delete the event like this, but to remember the values there
				for(XEvent event: trace) {
					// TODO : more check on this
					if(!containEvent(trace, event, null)) {
						oTrace.insertOrdered((XEvent) event.clone());
					}
					
				}
				
			}
			
		}
		// convert map to mlog
		mlog.addAll(tMap.values());
		
		return mlog;
	}
		
	
	public static boolean containEvent(XTrace trace, XEvent event, XEventClassifier classifier) {
		// TODO check if one trace contains the event with the current event classifier
		if(trace.contains(event))
			return true;
		String key = "event name";
		XAttribute eAttr  = event.getAttributes().get(key);
		for(XEvent e: trace) {
			XAttribute attr  = e.getAttributes().get(key);
			if(attr instanceof XAttributeLiteral) {
				if(((XAttributeLiteral) attr).getValue().equals(((XAttributeLiteral) eAttr).getValue()))
					return true;
			}else if(attr instanceof XAttributeDiscrete) {
				if(((XAttributeDiscrete) attr).getValue() == ((XAttributeDiscrete) eAttr).getValue())
					return true;
			}
		}
		
		return false;
	}
	// if we want to use the merge with attributes and event attributes, the ways to do this is:: 
	// if they have the same caseId, then we merge them by using the trace and event attributes definde by before
	// if they don`t have the same caseId, they we put them together
	// another way to merge is if we ignore the ones with the same event in xlog. 
	// if not ignore, we put the events in the same trace.. But we might need sth attributes from them
	// to delete certain attributes, we can create another node extension for this. Add or delete attributes 
	// for trace or event attributes there. We can handle this one. Waht we can do, is to put them together
	// we don't need to choose the attribtues, just the strategy listed there
	public static XLog mergeLogsInternal(XLog log0, XLog log1, List<XAttribute> traceAttrList0, 
			List<XAttribute> traceAttrList1, List<XAttribute> eventAttrList0, List<XAttribute> eventAttrList1, 
			List<XAttribute> exTraceAttrList0, 
			List<XAttribute> exTraceAttrList1, List<XAttribute> exEventAttrList0, List<XAttribute> exEventAttrList1) {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());
		mlog.getGlobalTraceAttributes().clear();
		mlog.getGlobalTraceAttributes().addAll(traceAttrList0);
		mlog.getGlobalTraceAttributes().addAll(traceAttrList1);
		mlog.getGlobalEventAttributes().addAll(eventAttrList0);
		mlog.getGlobalEventAttributes().addAll(eventAttrList1);
		
		String key = "concept:name";
		// how to find and change the trace with the same id ramdomly?? 
		// create hash map, for the caseId and trace itself
		Map<Long, XTrace> tMap = new HashMap();
		
		for(XTrace trace: log0) {
			XTrace nTrace = (XTrace) trace.clone();
			XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(key);
			// delete the attributes here with the same names... But are we talking about
			tMap.put(attr.getValue(), nTrace);
//			mlog.add(nTrace);
		}
		
		// put the first event log into the mlog
		for(XTrace trace: log1) {
			XTrace nTrace = (XTrace) trace.clone();
			XAttributeDiscrete attr = (XAttributeDiscrete) trace.getAttributes().get(key);
			if(tMap.keySet().contains(attr.getValue())) {
				// combine them together for one event and one 
				XTrace oTrace = tMap.get(attr.getValue());
				for(XEvent event: trace) {
					oTrace.insertOrdered((XEvent) event.clone());
				}
				
			}else {
				tMap.put(attr.getValue(), nTrace);
			}
			
		}
		
		// convert map to mlog
		mlog.addAll(tMap.values());
		return mlog;
	}
	
	public static XLog mergeLogsInternal(XLog log0, XLog log1) {
		return log0;
	}
	/**
	 * this method adds or delete trace and event attributes of log. It depends on the trace attributes and event attributes
	 * we have choosen. But better used as delete this attributes. It is unique.
	 * Modify can also include changing the name or the type of those attributes. TO change an existing attributes, 
	 * -- list the info for current attributes. The type and others
	 * -- the info for new attributes, like the name and type. 
	 * change the attributes name?? from KPI to output-come?? 
	 * change the type of it?? It is like adding the attributes to trace and event attributes
	 * Some things else, we have attributes manipluation for one event log!!! 
	 * If there is some attributes calculated from another event log.. We don't do it!!
	 * But to add the attrs, we need the expression from the addAttrs? Or random there, we need to do it in JavaSnippet.
	 * It is a better way. To delete, then delete it. 
	 * @param log
	 * @return
	 */
	public static XLog modifyAttributes(XLog log, List<XAttribute> addAttrs, List<XAttribute> deleteAttrs, 
			List<XAttribute> modifyAttrs) {
		XLog mlog = XLogUtil.clonePureLog(log, "Modified Log");
		
		// we have a list of addAttr, but also the values here for them?? Or something else?? 
		// attrs related to trace and events, the value can't be simply changed in this way. It needs more calculation
		// so for attributes, what we can do is to delete the attributes. But we can get the list of those operations?
		// not really!! We can sort the attributes and show the information about it. But we already have the view. 
		// SO this function is to be saved
		
		return log;
	}
	
	
	
	public static XLog clonePureLog(XLog log, String suffix) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		// TODO : check if it is like the attributes like trace attributes and event attributes
		XLog newLog = factory.createLog((XAttributeMap) log.getAttributes().clone());
		XConceptExtension.instance().assignName(newLog, XConceptExtension.instance().extractName(log) + suffix);
		newLog.getGlobalTraceAttributes().addAll(log.getGlobalTraceAttributes());
		newLog.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
		newLog.getExtensions().addAll(log.getExtensions());
		return newLog;
	}
}
