package org.pm4knime.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;

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
public class TraceVariantUtil {
	public static List<Integer> sampleTraceList(List<Double> percentList, int totalNum) {
		int sum = totalNum; 
		int count = 0;
		List<Integer> numList = new ArrayList<Integer>();
		for(double percent : percentList) {
			int num = (int) (totalNum * percent);
			count++;
			
			// how to judge to come to the last step?? 
			if(count == percentList.size() ) {
				num =  sum;
			}
			numList.add(num);
			sum -= num;
		}
		return numList;
	}
	
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
		
		
		
		int sum = result.values().parallelStream().reduce(0,(a,b) -> a + b);
		int diff = traceList.size() - sum;
		String highKey = result.keySet().iterator().next();
		result.put(highKey, result.get(highKey) + diff);
		// create another map for it
		addLabelWithNumber(traceList, result, lName);
	}
	
	public static void addLabelWithNumber(List<XTrace> traceList, Map<String, Integer> lMap, String lName) {
		// shuffle the index and get the random assignment for it. 
		// however, we can say that when we create the traceVariant, it might has no order, but for sure, 
		// we order them randomly again
		Collections.shuffle(traceList);
		int fromIndex = 0;
		
		// for each item in lMap, create the sub traceList for it.
		for(String key : lMap.keySet()) {
			int num = lMap.get(key);
			// get the subTraceList for the 
			List<XTrace> subList = traceList.subList(fromIndex, fromIndex + num);
			fromIndex += num;
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
	
	// how to get all the traceVariants to add attributes on them??
	// logViewModel.getTraceVariants().get(element.getElement());
	
}
