package org.pm4knime.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeContinuous;
import org.deckfour.xes.model.XAttributeDiscrete;
import org.deckfour.xes.model.XAttributeLiteral;
import org.deckfour.xes.model.XAttributeTimestamp;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.impl.XAttributeContinuousImpl;
import org.deckfour.xes.model.impl.XAttributeDiscreteImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.pm4knime.portobject.XLogPortObjectSpec;

/**
 * this class provides the function to extract the spec from xlog,
 * convert the data from XLogPortObject and XLogPortObjectSpec 
 * @author kefang-pads
 *
 */
public class XLogSpecUtil {
	public static final String TRACE_ATTRIBUTE_PREFIX = "#Trace Attribute#";
	public static final String EVENT_ATTRIBUTE_PREFIX = "#Event Attribute#";
	public static final String CLASSIFIER_PREFIX = ""; // "#Classifier#";
	public final static String CFG_KEY_CLASSIFIER_SEPARATOR = "###";
	
	public static final String CFG_KEY_TRACE_ATTRSET = "Trace attribute set";
	public static final String CFG_KEY_EVENT_ATTRSET = "Event attribute set";
	
	public static XLogPortObjectSpec extractSpec(XLog log) {
		Map<String, Class> tMap = null, eMap = null, cMap = null;
		
		// get the attributes from the log 
		List<XAttribute> attrs = XLogUtil.getTAttributes(log, 0.2);
		tMap = convertAttr2Str(attrs, TRACE_ATTRIBUTE_PREFIX);
		
		attrs = XLogUtil.getEAttributes(log, 0.2);
		// more attributes need to check in events, not just the global attributes there		
		eMap = convertAttr2Str(attrs, EVENT_ATTRIBUTE_PREFIX);
		// convert those attribtues to map in string

		List<XEventClassifier> clfList  = log.getClassifiers();
		cMap = convertClf2Str(clfList, CLASSIFIER_PREFIX);
		// add the event attributes as classifiers here
		// before put, check if they repeat the data
		cMap.putAll(eMap);
		
		return new XLogPortObjectSpec(tMap, eMap, cMap);
	}
	
	
	public static List<String> getClassifierWithClsList(Map<String, Class>  clfMap) {
		// TODO Auto-generated method stub
		List<String> classifierWithClsList = new ArrayList();
		for(String key : clfMap.keySet()) {
			
			String clfWithClsStr = key + XLogSpecUtil.CFG_KEY_CLASSIFIER_SEPARATOR +
					clfMap.get(key).toString();
			classifierWithClsList.add(clfWithClsStr);
		}
		
		return classifierWithClsList;
	}

	
	public static Map<String, Class> convertAttr2Str(Collection<XAttribute> attrSet, String prefix){
		Map<String, Class> aMap =  new HashMap<String, Class>();
		for(XAttribute attr : attrSet) {
			// check if the key has already include the prefix??
			aMap.put(prefix + attr.getKey(), attr.getClass());
		}
		
		return aMap;
	}
	
	public static Map<String, Class> convertClf2Str(Collection<XEventClassifier> clfSet, String prefix){
		Map<String, Class> aMap =  new HashMap<String, Class>();
		for(XEventClassifier clf : clfSet) {
			aMap.put(prefix + clf.name(), clf.getClass());
		}
		
		return aMap;
	}
	
	// here we need to convert from the map to the attribute back 
	public static XAttribute convertStr2Attr(String key, String prefix, Collection<XAttribute> attrSet) {
		if(prefix!=null && prefix.length() > 0) {
			// split the key due to the prefix
			key = key.split(prefix)[1];
		}
		//compare the available attribtues and assign the values here
		for(XAttribute attr : attrSet) {
			if(attr.getKey().equals(key))
				return attr;
		}
		return null;
	}
}
