package org.pm4knime.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;

/*
 * this class is created  to include the utility used to deal with event log in XLog format
 */
public class XLogUtil {

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
		List<XEventClassifier> classifierList= new ArrayList();
		classifierList.add(new XEventNameClassifier());
		classifierList.add(new XEventLifeTransClassifier());
		return classifierList;
	}
	
}
