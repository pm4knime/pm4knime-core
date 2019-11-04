package org.pm4knime.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.in.XParser;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
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
		List<XAttribute> attrList = getEAttributes(log, 0.2);
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
		List<String> attrNames = getNameList(attrList);
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
		List<String> attrNames = getNameList(attrList);
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
	
	public static List<String> getNameList(List<XAttribute> attrList){
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
}
