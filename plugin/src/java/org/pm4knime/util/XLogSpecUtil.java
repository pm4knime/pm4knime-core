package org.pm4knime.util;

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
	public static final String CLASSIFIER_PREFIX = "#Classifier#";
	

	public static final String CFG_KEY_TRACE_ATTRSET = "Trace Attribute Set";
	public static final String CFG_KEY_EVENT_ATTRSET = "Event Attribute Set";
	
	public static XLogPortObjectSpec extractSpec(XLog log) {
		Map<String, String> tMap = null, eMap = null, cMap = null;
		
		// get the attributes from the log 
		List<XAttribute> attrs = log.getGlobalTraceAttributes();
		tMap = convertAttr2Str(attrs, TRACE_ATTRIBUTE_PREFIX);
		
		attrs = log.getGlobalEventAttributes();
		eMap = convertAttr2Str(attrs, EVENT_ATTRIBUTE_PREFIX);
		// convert those attribtues to map in string

		List<XEventClassifier> clfList  = log.getClassifiers();
		cMap = convertClf2Str(clfList, CLASSIFIER_PREFIX);
		
		return new XLogPortObjectSpec(tMap, eMap, cMap);
	}
	
	public static Map<String, String> convertAttr2Str(Collection<XAttribute> attrSet, String prefix){
		Map<String, String> aMap =  new HashMap<String, String>();
		for(XAttribute attr : attrSet) {
			aMap.put(prefix + attr.getKey(), attr.getClass().getSimpleName());
		}
		
		return aMap;
	}
	
	public static Map<String, String> convertClf2Str(Collection<XEventClassifier> clfSet, String prefix){
		Map<String, String> aMap =  new HashMap<String, String>();
		for(XEventClassifier clf : clfSet) {
			aMap.put(prefix + clf.name(), clf.getClass().getSimpleName());
		}
		
		return aMap;
	}
	
	// here we need to convert from the map to the attribute back 
	public static XAttribute convertStr2Attr(String key, String prefix, String cls) {
		if(prefix!=null && prefix.length() > 0) {
			// split the key due to the prefix
			key = key.split(prefix)[1];
		}
		
		// create the attribute due to the cls
		if(cls.equals(XAttributeLiteral.class.getSimpleName())) {
			// we don't care about the values here
			return new XAttributeLiteralImpl(key, "");
		}else if(cls.equals(XAttributeDiscrete.class.getSimpleName())) {
			// we don't care about the values here
			return new XAttributeDiscreteImpl(key, 0);
		}else if(cls.equals(XAttributeContinuous.class.getSimpleName())) {
			// we don't care about the values here
			return new XAttributeContinuousImpl(key, 0);
		}else  if(cls.equals(XAttributeTimestamp.class.getSimpleName())) {
			// we don't care about the values here
			return new XAttributeTimestampImpl(key, 0);
		}else {
			System.out.println("The attribute is not recognized");
		} 
		return null;
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
