package org.pm4knime.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XTraceImpl;
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
		String objString = ecls.getClass().getName() + CFG_STRING_SEPERATOR + ecls.getId() + CFG_STRING_SEPERATOR
				+ ecls.getIndex() + CFG_STRING_SEPERATOR + ecls.size();
		return objString;
	}

	// deserialize event class from string
	public static XEventClass deserializeEventClass(String objString) {
		String[] attrs = objString.split(CFG_STRING_SEPERATOR);
		if (attrs[0].equals(XEventClass.class.getName())) {
			XEventClass ecls = new XEventClass(attrs[1], Integer.parseInt(attrs[2]));
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
		if (classifierName.contains(XLogSpecUtil.CFG_KEY_CLASSIFIER_SEPARATOR)) {
			classifierName = classifierName.split(XLogSpecUtil.CFG_KEY_CLASSIFIER_SEPARATOR)[1];
		}

		List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();// log.getClassifiers();
		classifiers.addAll(log.getClassifiers());
		// check the attributes as classifier here
		// and assign them as the XEventAttributeClassifier
		// if there is no global event attribute, we need to check the whole event log
		// and find the one existing there
		// the concept name is not used here then
		List<XAttribute> eAttrs;
		if (log.getGlobalEventAttributes().isEmpty())
			eAttrs = XLogUtil.getEAttributes(log, 0.2);
		else
			eAttrs = log.getGlobalEventAttributes();
		// more attributes need to check in events, not just the global attributes there

		for (XAttribute eAttr : eAttrs) {
			// create new classifier for the new eAttr here, given the name with prefix for
			// it!!
			XEventClassifier attrClf = new XEventAttributeClassifier(
					XLogSpecUtil.EVENT_ATTRIBUTE_PREFIX + eAttr.getKey(), eAttr.getKey());
			classifiers.add(attrClf);
		}

		for (XEventClassifier clf : classifiers) {
			if (clf.name().equals(classifierName))
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
		for (XEventClass eClass : eventClasses) {
			if (eventName.equals(eClass.getId()))
				return eClass;
		}

		return null;
	}

	/*
	 * this function collects the event classes from log and outputs the names in
	 * the sorted order
	 */
	public static List<String> extractAndSortECNames(XLog log, XEventClassifier classifier) {
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, classifier);
		Collection<XEventClass> eventClasses = summary.getEventClasses().getClasses();
		SortedSet<String> ecSet = new TreeSet<>();
		for (XEventClass ec : eventClasses) {
			if (!ec.getId().isEmpty())
				ecSet.add(ec.getId());
		}
		List<String> nameList = new ArrayList<String>();
		nameList.addAll(ecSet);
		return nameList;
	}

	// get the event attributes available for the time stamp from event log
	public static List<String> getTSAttrNames(XLog log) {
		// get the event attributes types, like start time and complete time
		// after checking this, we get the key names. They are the same.
		// so what we get is the keySet..
		// can't use it directly, because the global change there, so we need to get sth
		// else
		List<XAttribute> attrList = new ArrayList();
		attrList.addAll(getTAttributes(log, 0.2));
		attrList.addAll(getEAttributes(log, 0.2));
		List<String> attrNames = new ArrayList<String>();

		// the result is reliable, we need to traverse the log and trace for this
		for (XAttribute attr : attrList) {
			if (attr instanceof XAttributeTimestampImpl || attr instanceof XAttributeTimestamp) {
				attrNames.add(attr.getKey());
			}
		}
		return attrNames;
	}

	// get the event attribute of an event log
	public static List<XAttribute> getEAttributes(XLog log, double percent) {
		// the result is not reliable, so traverse each trace there
		List<XAttribute> attrList = new ArrayList();
		attrList.addAll(log.getGlobalEventAttributes());
		List<String> attrNames = getAttrNameList(attrList);
		int tmp = (int) (log.size() * percent) + 1;
		int num = tmp > 20 ? 20 : tmp;
		// we should count the number of visited values there
		for (XTrace trace : log) {
			for (XEvent event : trace) {
				for (String attrKey : event.getAttributes().keySet()) {
					if (!attrNames.contains(attrKey)) {
						attrNames.add(attrKey);
						attrList.add((XAttribute) event.getAttributes().get(attrKey).clone());
					}
				}
			}
			num--;
			if (num < 0)
				break;
		}

		return attrList;
	}

	// get the trace attributes of an event log...
	// but if we want to acess the attributes, we need its keys for this...
	public static List<XAttribute> getTAttributes(XLog log, double percent) {
		// the result is not reliable, so traverse each trace there
		List<XAttribute> attrList = new ArrayList();
		attrList.addAll(log.getGlobalTraceAttributes());
		List<String> attrNames = getAttrNameList(attrList);
		int tmp = (int) (log.size() * percent) + 1;
		int num = tmp > 20 ? 20 : tmp;
		// we should count the number of visited values there
		for (XTrace trace : log) {
			for (String attrKey : trace.getAttributes().keySet()) {
				if (!attrNames.contains(attrKey)) {
					attrNames.add(attrKey);
					// here will add the attributes directly on log
					attrList.add((XAttribute) trace.getAttributes().get(attrKey).clone());
				}
			}
			num--;
			if (num < 0)
				break;
		}
		// after this, we make the value as default

		return attrList;
	}

	public static List<String> getAttrNameList(List<XAttribute> attrList) {
		List<String> attrNames = new ArrayList();
		for (XAttribute attr : attrList) {
			attrNames.add(attr.getKey());
		}
		return attrNames;
	}

	// get the class for attribute according to atrribute name
	public static Class<?> getAttrClass(XLog log, String attrName) {
		XAttribute xattr = null;
		for (XTrace trace : log) {
			for (XEvent event : trace) {

				if (event.getAttributes().keySet().contains(attrName)) {
					xattr = event.getAttributes().get(attrName);
					return getAttrClass(xattr);
				}
			}

		}
		throw new IllegalArgumentException("Not supported attribute name");
	}

	// get the class for one attribute
	public static Class<?> getAttrClass(XAttribute xattr) {
		if (xattr instanceof XAttributeTimestamp || xattr instanceof XAttributeTimestampImpl) {
			return java.util.Date.class;
		} else if (xattr instanceof XAttributeContinuous || xattr instanceof XAttributeContinuousImpl) {
			return Double.class;
		} else if (xattr instanceof XAttributeDiscrete || xattr instanceof XAttributeDiscreteImpl) {
			return Integer.class;
		} else if (xattr instanceof XAttributeLiteral || xattr instanceof XAttributeLiteral) {
			return String.class;
		}
		throw new IllegalArgumentException("Not supported data type");
	}

	
	public static XLog clonePureLog(XLog log, String suffix) {
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		// TODO : check if it is like the attributes like trace attributes and event
		// attributes
		XLog newLog = factory.createLog((XAttributeMap) log.getAttributes().clone());
		XConceptExtension.instance().assignName(newLog, XConceptExtension.instance().extractName(log) + suffix);
		newLog.getGlobalTraceAttributes().addAll(log.getGlobalTraceAttributes());
		newLog.getGlobalEventAttributes().addAll(log.getGlobalEventAttributes());
		newLog.getExtensions().addAll(log.getExtensions());
		return newLog;
	}

	// add artificial start and end transitions to the log.
	// refer to org.processmining.hybridilpminer.utils.XLogUtils , but allows the
	// classifier to be specified
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
		XLog klog = XLogUtil.clonePureLog(log, " kept log");
		XLog dlog = XLogUtil.clonePureLog(log, " disposed log");

		for (XTrace trace : log) {
			if (trace.getAttributes().containsKey(key)) {
				XAttribute attr = trace.getAttributes().get(key);
				if (attr.toString().equals(value))
					klog.add((XTrace) trace.clone());
				else
					dlog.add((XTrace) trace.clone());
			} else {
				klog.add((XTrace) trace.clone());
			}

		}

		return new XLog[] { klog, dlog };
	}

	/**
	 * split event log by event attributes. If there is one event, we need to save
	 * trace there
	 * 
	 * @param log
	 * @param key
	 * @param value
	 * @return
	 */
	public static XLog[] splitLogByEventAttr(XLog log, String key, String value) {
		XLog klog = XLogUtil.clonePureLog(log, " kept log");
		XLog dlog = XLogUtil.clonePureLog(log, " disposed log");

		XTrace kTrace, dTrace;
		for (XTrace trace : log) {
			kTrace = pureClone(trace);
			dTrace = pureClone(trace);
			for (XEvent event : trace) {

				if (event.getAttributes().containsKey(key)) {
					XAttribute attr = event.getAttributes().get(key);
					if (attr.toString().equals(value)) {
						kTrace.add((XEvent) event.clone());
						continue;
					}
				}
				// one thing, to remove only according to index, the index changes
				dTrace.add((XEvent) event.clone());
			}

			if (kTrace.size() > 0)
				klog.add(kTrace);
			if (dTrace.size() > 0)
				dlog.add(dTrace);
		}

		return new XLog[] { klog, dlog };
	}

	private static XTrace pureClone(XTrace trace) {
		// TODO Auto-generated method stub
		XTrace cTrace = new XTraceImpl(trace.getAttributes());
		return cTrace;
	}

}
