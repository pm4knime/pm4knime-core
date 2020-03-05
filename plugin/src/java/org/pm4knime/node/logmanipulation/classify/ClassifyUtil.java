package org.pm4knime.node.logmanipulation.classify;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.pm4knime.node.logmanipulation.sample.SampleUtil;
import org.processmining.log.utils.TraceVariantByClassifier;
import org.processmining.log.utils.XUtils;

import com.google.common.collect.ImmutableListMultimap;

/**
 * this class provides utilities for trace variant of event log. Trace variant of one event log is the set of traces 
 * which have the same sequence of event classes. 
 * In this class, the following function is provided:
 * -- create the trace variants from event log
 * -- add labels to trace variants
 * 
 * One modification here is that XES Variant is included. We should base our result on the existing ways. 
 * 
 * If we have XES Variant, we need to import the log enhancement package. How to add labels on the trace there??
 * XESTraceVariant has connection with ProM View Part. And also in another way, how to create the TraceVariant?? 
 * 
 * Due to the old codes provided for TraceVariant in ProM, some of the methods here is rewritten.
 * @author kefang-pads
 *
 */
public class ClassifyUtil {
	
	/**
	 * Randomly assign label categories according to its percents.
	 * because the number is calculated from the 0.5 to 0.8. 
	 * if the number is only 1, how to judge them?? We need to rejudge the percentage again
	 * 
	 * @param traceList the trace list to be labelled with the values here
	 * @param lMap The label value and percent to be assigned 
	 */
	public static void addLabelWithPercent(List<XTrace> traceList, Map<String, Double> lMap, String lName) {
		// sample the list of values from the percents, in descending order
		Map<String, Integer> result = new LinkedHashMap<String, Integer>();	
		
		double tNum = traceList.size();
		lMap.entrySet().stream()
				.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
				.forEach(e -> result.put( e.getKey() , (int) (e.getValue() * tNum)));
		
		
		// how to differentiate if we should assign all the sum here, or just the one for them?? 
		// we need to know the percentage amount here. 
		// one condition to do this, is that we assign all the trace variant with labels!! 
		int sum = result.values().parallelStream().reduce(0,(a,b) -> a + b);
		int diff = traceList.size() - sum;
		String highKey = result.keySet().iterator().next();
		result.put(highKey, result.get(highKey) + diff);
		// create another map for it
		addLabelWithNumber(traceList, result, lName);
	}
	
	public static void addLabelWithNumber(List<XTrace> traceList, Map<String, Integer> lMap, String lName) {
		
		// for each item in lMap, create the sub traceList for it.
		for(String key : lMap.keySet()) {
			int num = lMap.get(key);
			// we random the index list been used 
			List<Integer> subIndex= SampleUtil.sample(traceList.size(), num);
			
			List<XTrace> subList = new LinkedList<XTrace>();
			for(int idx : subIndex) {
				subList.add(traceList.get(idx));
			}
			
			addLabelWithList(subList, key, lName);
		}
	}
	// assign to the list of traceList with lValue... But which class do you want to assign them as one string value
	public static void addLabelWithList(List<XTrace> traceList, String lValue, String lName) {
		for(XTrace trace: traceList) {
			XAttributeLiteral attr = new XAttributeLiteralImpl(lName, lValue);
			trace.getAttributes().put(attr.getKey(), attr);
		}
		
		// check if the global log doesn't include this attributes, we need to add it into global??
	}
	
	public static ImmutableListMultimap<TraceVariantByClassifier, XTrace> getTraceVariant(XLog log) {
		XEventClasses eventClasses = XUtils.createEventClasses(new XEventNameClassifier(), log);
		return XUtils.getVariantsByClassifier(log, eventClasses);
	}
	
}
