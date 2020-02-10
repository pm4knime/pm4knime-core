package org.pm4knime.util;

import java.io.IOException;
import java.io.InputStream;
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

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
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
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
import org.deckfour.xes.out.XSerializer;
import org.deckfour.xes.out.XesXmlSerializer;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.processmining.incorporatenegativeinformation.help.EventLogUtilities;

/*
 * this class is created  to include the utility used to deal with event log in XLog format
 */
public class XLogUtil {
	
	public static final String CFG_DUMMY_ECNAME = "Dummy Event Class";
	public static final String CFG_EVENTCLASSIFIER_NAME = "Event Classifier";
	public static final String CFG_STRING_SEPERATOR = "::";
	public static final String CFG_ATTRIBUTE_SUFFIX = "-new";
	
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
	
	// this function can be included into the event classifier
	public static List<String> getECNames(List<XEventClassifier> classifierList) {
		// TODO Auto-generated method stub
		List<String> classifierNames = new ArrayList();
		for (XEventClassifier clf : classifierList) {
			classifierNames.add(clf.name());
		}
		return classifierNames;
	}

	
	public static XEventClassifier getEventClassifier(XLog log, String classifierName) {
 		// get the list of classifiers from the event log!!
 		
 		List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();// log.getClassifiers();
 		classifiers.addAll( log.getClassifiers());
 		// check the attributes as classifier here //and assign them as the XEventAttributeClassifier
 		for(XAttribute eAttr: log.getGlobalEventAttributes()) {
 			// create new classifier for the new eAttr here, given the name with prefix for it!!
 			XEventClassifier attrClf = new XEventAttributeClassifier(XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX + 
 					eAttr.getKey(), eAttr.getKey());
 			classifiers.add(attrClf);
 		}
 		
 		for(XEventClassifier clf: classifiers) {
 			if(clf.name().equals(classifierName))
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
	
	// get the event attributes available for the time stamp from event log
	public static List<String> getTSAttrNames(XLog log){
		// get the event attributes types, like start time and complete time
		// after checking this, we get the key names. They are the same.
		// so what we get is the keySet..
		// can't use it directly, because the global change there, so we need to get sth else
		List<XAttribute> attrList = new ArrayList(); 
		attrList.addAll(getTAttributes(log, 0.2));
		attrList.addAll(getEAttributes(log, 0.2));
		List<String> attrNames = new ArrayList<String>();
		
		// the result is reliable, we need to traverse the log and trace for this
		for(XAttribute attr : attrList) {
			if(attr instanceof XAttributeTimestampImpl || attr instanceof XAttributeTimestamp) {
				attrNames.add(attr.getKey());
			}
		}
		return attrNames;
	}
	
	// get the event attribute of an event log
	public static List<XAttribute> getEAttributes(XLog log, double percent){
		// the result is not reliable, so traverse each trace there
		List<XAttribute> attrList = new ArrayList(); 
		attrList.addAll(log.getGlobalEventAttributes()); 
		List<String> attrNames = getAttrNameList(attrList);
		int tmp = (int) (log.size() * percent) + 1;
		int num =  tmp > 20? 20: tmp;
		// we should count the number of visited values there
		for(XTrace trace: log) {
			for(XEvent event: trace) {
				for(String attrKey : event.getAttributes().keySet()) {
					if(!attrNames.contains(attrKey)) {
						attrNames.add(attrKey);
						attrList.add((XAttribute) event.getAttributes().get(attrKey).clone());
					}
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
		List<XAttribute> attrList = new ArrayList(); 
		attrList.addAll(log.getGlobalTraceAttributes());
		List<String> attrNames = getAttrNameList(attrList);
		int tmp = (int) (log.size() * percent) + 1;
		int num =  tmp > 20? 20: tmp;
		// we should count the number of visited values there
		for(XTrace trace: log) {
			for(String attrKey : trace.getAttributes().keySet()) {
				if(!attrNames.contains(attrKey)) {
					attrNames.add(attrKey);
					// here will add the attributes directly on log
					attrList.add((XAttribute)trace.getAttributes().get(attrKey).clone());
				}
			}
			num--;
			if(num < 0)
				break;
		}
		// after this, we make the value as default
		
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
		if (xattr instanceof XAttributeTimestamp || xattr instanceof XAttributeTimestampImpl) {
			return java.util.Date.class;
		} else if (xattr instanceof XAttributeContinuous || xattr instanceof XAttributeContinuousImpl) {
			return Double.class;
		} else if (xattr instanceof XAttributeDiscrete || xattr instanceof XAttributeDiscreteImpl) {
			return Integer.class;
		}else if (xattr instanceof XAttributeLiteral || xattr instanceof XAttributeLiteral) {
			return String.class;
		}
		throw new IllegalArgumentException("Not supported data type");
	}
	
	// merge two event logs with separate strategy
	public static XLog mergeLogsSeparate(XLog log0, XLog log1, ExecutionContext exec) throws CanceledExecutionException {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());
		mlog.getGlobalTraceAttributes().addAll(log1.getGlobalTraceAttributes());
		mlog.getGlobalEventAttributes().addAll(log1.getGlobalEventAttributes());
		
		// put the first event log into the mlog
		for(XTrace trace: log0) {
			exec.checkCanceled();
			mlog.add((XTrace) trace.clone());
		}
		
		// put the first event log into the mlog
		for(XTrace trace: log1) {
			exec.checkCanceled();
			mlog.add((XTrace) trace.clone());
		}
		
		return mlog;
	}
	
	// merge log with ignore second log strategy. But simple ignore the trace
	public static XLog mergeLogsIgnoreTrace(XLog log0, XLog log1, List<String> tKeys, ExecutionContext exec) throws CanceledExecutionException {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());
		mlog.getGlobalTraceAttributes().addAll(log1.getGlobalTraceAttributes());
		mlog.getGlobalEventAttributes().addAll(log1.getGlobalEventAttributes());
		
		Set<String> caseIDSet = new HashSet();
		
		// put the first event log into the mlog
		for(XTrace trace: log0) {
			exec.checkCanceled();
			mlog.add((XTrace) trace.clone());
			
			// we need to get the values from it.. If we have string format
			// it is totally fine for me
			String  attrValue = trace.getAttributes().get(tKeys.get(0)).toString();
			caseIDSet.add(attrValue);
		}
		
		// put the first event log into the mlog
		for(XTrace trace: log1) {
			exec.checkCanceled();
			String  attrValue = trace.getAttributes().get(tKeys.get(1)).toString();
			if(!caseIDSet.contains(attrValue)) {
				mlog.add((XTrace) trace.clone());
			}
			
		}
		return mlog;
	}
	
	
	// merge log with ignore second log strategy. But simple ignore the trace, 
	// do they have conflicts on some attributes?? I think so, so we need to deal with the trace attributes
	// by using with one, we need to do this??
	// some attributes need change. 
	public static XLog mergeLogsSeparateEvent(XLog log0, XLog log1, List<String> tKeys ,
			List<XAttribute> exTraceAttrList0, List<XAttribute> traceAttrList1, ExecutionContext exec) throws CanceledExecutionException {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());
		
		for(XAttribute attr : traceAttrList1) {
			String newKey = attr.getKey() + CFG_ATTRIBUTE_SUFFIX;
			mlog.getGlobalTraceAttributes().add(cloneAttribute(attr, newKey));
		}
		
		Map<String, XTrace> tMap = new HashMap();
		
		// put the first event log into the mlog
		for(XTrace trace: log0) {
			exec.checkCanceled();
			XTrace nTrace = (XTrace) trace.clone();
			String  attrValue = trace.getAttributes().get(tKeys.get(0)).toString();
			tMap.put(attrValue, nTrace);
		}
		
		// put the first event log into the mlog
		for(XTrace trace: log1) {
			exec.checkCanceled();
			String  attrValue = trace.getAttributes().get(tKeys.get(1)).toString();
			
			if(tMap.keySet().contains(attrValue)) {
			
				XTrace oTrace = tMap.get(attrValue);
				// add all event from the current to oTrace
				for(XEvent event: trace) {
					oTrace.insertOrdered((XEvent) event.clone());
				}
				
				// only for the ones with same caseID, we do it!! 
				for(XAttribute exAttr : exTraceAttrList0)
					oTrace.getAttributes().remove(exAttr.getKey());
				for(XAttribute inAttr : traceAttrList1) {
					String newKey = inAttr.getKey() + CFG_ATTRIBUTE_SUFFIX;
					//  naming is important to show it out. fix the global attributes
					oTrace.getAttributes().put(newKey,  cloneAttribute(inAttr, newKey));
				}
				
			}else // without the same identifier
				tMap.put(attrValue, trace);
			
		}
		// convert map to mlog
		mlog.addAll(tMap.values());
		
		return mlog;
	}
		
	
	public static XEvent findEvent(XTrace trace, XEvent event, String eKeyInTrace, String eKey) {
		// TODO check if one trace contains the event with the current event classifier
		if(trace.contains(event))
			return event;
		
		XAttribute eAttr  = event.getAttributes().get(eKey);
		for(XEvent e: trace) {
			XAttribute attr  = e.getAttributes().get(eKeyInTrace);
			if(attr instanceof XAttributeLiteral) {
				if(((XAttributeLiteral) attr).getValue().equals(((XAttributeLiteral) eAttr).getValue()))
					return e;
			}else if(attr instanceof XAttributeDiscrete) {
				if(((XAttributeDiscrete) attr).getValue() == ((XAttributeDiscrete) eAttr).getValue())
					return e;
			}
		}
		
		return null;
	}
	
	public static XAttribute cloneAttribute(XAttribute xattr, String newKey) {
		if (xattr instanceof XAttributeContinuous) {
			return new XAttributeContinuousImpl(newKey, ((XAttributeContinuous) xattr).getValue());
		} else if (xattr instanceof XAttributeDiscrete) {
			return new XAttributeDiscreteImpl(newKey, ((XAttributeDiscrete) xattr).getValue());
		}else if (xattr instanceof XAttributeLiteral) {
			return new XAttributeLiteralImpl(newKey, ((XAttributeLiteral) xattr).getValue());
		}else if(xattr instanceof XAttributeTimestamp) {
			return new XAttributeTimestampImpl(newKey, ((XAttributeTimestamp) xattr).getValue());
		}
		return null;
	}
	
	// if we want to use the merge with attributes and event attributes, the ways to do this is:: 
	// if they have the same caseId, then we merge them by using the trace and event attributes definde by before
	// if they don`t have the same caseId, they we put them together
	// another way to merge is if we ignore the ones with the same event in xlog. 
	// if not ignore, we put the events in the same trace.. But we might need sth attributes from them
	// to delete certain attributes, we can create another node extension for this. Add or delete attributes 
	// for trace or event attributes there. We can handle this one. Waht we can do, is to put them together
	// we don't need to choose the attribtues, just the strategy listed there
	// 
	// exTraceAttrList0, exEventAttrList0 is empty and traceAttrList1 and eventAttrList1 includes all the attributes 
	// we think they are separate traces and with different IDs. 
	// we can have one indicator for this. if separate, or not. IF they are separate, we insert all events there
	public static XLog mergeLogsInternal(XLog log0, XLog log1, List<String> tKeys, List<String> eKeys , List<XAttribute> exTraceAttrList0, List<XAttribute> exEventAttrList0, 
			List<XAttribute> traceAttrList1,  List<XAttribute> eventAttrList1, ExecutionContext exec) throws CanceledExecutionException {
		XLog mlog = XLogUtil.clonePureLog(log0, "Merged Log");
		mlog.getAttributes().putAll(log1.getAttributes());
		// the attributes we remove is only for the traces with the same caseID. So it means at end, we need to keep them all?? 
		// for other traces, we keep the old attributes there
		for(XAttribute attr : traceAttrList1) {
			String newKey = attr.getKey() + CFG_ATTRIBUTE_SUFFIX;
			mlog.getGlobalTraceAttributes().add( cloneAttribute(attr, newKey));
		}
		
		for(XAttribute attr : eventAttrList1) {
			String newKey = attr.getKey() + CFG_ATTRIBUTE_SUFFIX;
			mlog.getGlobalEventAttributes().add(cloneAttribute(attr, newKey));
		}
		
		// how to find and change the trace with the same id ramdomly?? 
		// create hash map, for the caseId and trace itself
		Map<String, XTrace> tMap = new HashMap();
		
		for(XTrace trace: log0) {
			exec.checkCanceled();
			XTrace nTrace = (XTrace) trace.clone();
			String  attrValue = trace.getAttributes().get(tKeys.get(0)).toString();// delete the attributes here with the same names... But are we talking about
			tMap.put(attrValue, nTrace);
//			mlog.add(nTrace);
		}
		
		// put the first event log into the mlog
		for(XTrace trace: log1) {
			exec.checkCanceled();
			XTrace nTrace = (XTrace) trace.clone();
			String  attrValue = trace.getAttributes().get(tKeys.get(1)).toString();// delete the attributes here with the same names... But are we talking about
			
			if(tMap.keySet().contains(attrValue)) {
				// combine them together for one event and one 
				XTrace oTrace = tMap.get(attrValue);
				for(XEvent event: trace) {
					// just for the event with same eventID, we do this operation to make them as one
					XEvent fe = findEvent(oTrace, event, eKeys.get(0), eKeys.get(1));
					if(fe!=null) {
						// check this event attributes and other attributes from this one
						
						// fe delete the exclused list and add addition ones
						for(XAttribute exAttr : exEventAttrList0)
							fe.getAttributes().remove(exAttr.getKey());
						// add additional attr from log1
						for(XAttribute inAttr : eventAttrList1) {
							// here we should add values from the current event here
							String newKey = inAttr.getKey() + CFG_ATTRIBUTE_SUFFIX;
							// because it is a map, so if we use the same key, it's going to rewrite the values
							// so the ways to do it, change the key for the new added attributes here.
							fe.getAttributes().put(newKey,  cloneAttribute(inAttr, newKey));
						
						}
					}else {
						// for different ones, keep them as they are, for excluded part, also
						oTrace.insertOrdered((XEvent) event.clone()); 
					}
					
				}
				
				// deal with the attributes for trace
				for(XAttribute exAttr : exTraceAttrList0)
					oTrace.getAttributes().remove(exAttr.getKey());
				for(XAttribute inAttr : traceAttrList1) {
					String newKey = inAttr.getKey() + CFG_ATTRIBUTE_SUFFIX;
					//  naming is important to show it out. fix the global attributes
					oTrace.getAttributes().put(newKey,  cloneAttribute(inAttr, newKey));
				}
				
			}else {
				tMap.put(attrValue, nTrace);
			}
			
		}
		
		// convert map to mlog
		mlog.addAll(tMap.values());
		return mlog;
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
	
	// add artificial start and end transitions to the log. 
	// refer to org.processmining.hybridilpminer.utils.XLogUtils , but allows the classifier to be specified
	// to adapt the end use
	public static XLog addArtificialStartAndEnd(XLog log, String artifStart, String artifEnd,
			XEventClassifier eventClassifier) {
		// TODO Auto-generated method stub
		XLog copy = (XLog) log.clone();
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		List<XEventClassifier> classifiers = log.getClassifiers();
		classifiers.add(eventClassifier);
		
		for (XTrace t : copy) {
			t.add(0, factory.createEvent(createArtificialAttributeMap(factory, classifiers, artifStart)));
			t.add(factory.createEvent(createArtificialAttributeMap(factory, classifiers, artifEnd)));
		}
		if (copy.isEmpty()) {
			XTrace t = factory.createTrace();
			t.add(0, factory.createEvent(createArtificialAttributeMap(factory, classifiers, artifStart)));
			t.add(factory.createEvent(createArtificialAttributeMap(factory, classifiers, artifEnd)));
			copy.add(t);
		}
		return copy;
		
	}
	
	public static XAttributeMap createArtificialAttributeMap(final XFactory factory,
			final List<XEventClassifier> classifiers, final String artificialLabel) {
		XAttributeMap map = factory.createAttributeMap();
		for (XEventClassifier classifier : classifiers) {
			for (String s : classifier.getDefiningAttributeKeys()) {
				map.put(s, factory.createAttributeLiteral(s, artificialLabel, null));
			}
		}
		return map;
	}
	
	public static XLog[] splitLogByTraceAttr(XLog log, String key, String value) {
		XLog klog = EventLogUtilities.clonePureLog(log, " kept log");
		XLog dlog = EventLogUtilities.clonePureLog(log, " disposed log");
		
		for (XTrace trace : log) {
			if (trace.getAttributes().containsKey(key)) {
				XAttribute attr = trace.getAttributes().get(key);
				if(attr.toString().equals(value))
					klog.add((XTrace) trace.clone());
				else
					dlog.add((XTrace) trace.clone());
			}else {
				klog.add((XTrace) trace.clone());
			}
			
		}
		
		return new XLog[] { klog, dlog };
	}
	
	/**
	 * split event log by event attributes. If there is one event, we need to save trace there
	 * @param log
	 * @param key
	 * @param value
	 * @return
	 */
	public static XLog[] splitLogByEventAttr(XLog log, String key, String value) {
		XLog klog = EventLogUtilities.clonePureLog(log, " kept log");
		XLog dlog = EventLogUtilities.clonePureLog(log, " disposed log");
		
		XTrace kTrace, dTrace ;
		for (XTrace trace : log) {
			kTrace = pureClone(trace);
			dTrace = pureClone(trace);
			for(XEvent event : trace) {
				
				if (event.getAttributes().containsKey(key)) {
					XAttribute attr = event.getAttributes().get(key);
					if(attr.toString().equals(value)) {
						kTrace.add((XEvent) event.clone());
						continue;
					}
				}
				// one thing, to remove only according to index, the index changes
				dTrace.add((XEvent) event.clone());
			}
			
			if(kTrace.size() > 0)
				klog.add(kTrace);
			if(dTrace.size() > 0)
				dlog.add(dTrace);
		}
		
		return new XLog[] { klog, dlog };
	}
	private static XTrace pureClone(XTrace trace) {
		// TODO Auto-generated method stub
		XTrace cTrace = new XTraceImpl(trace.getAttributes());
		
		return cTrace;
	}
	// filter the traces in the range of event log. If isKeep is true, those traces are kept, else those traces are removed.
	
}
