package org.pm4knime.node.io.log.reader;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;

import org.deckfour.xes.classification.XEventAttributeClassifier;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.extension.XExtensionParser;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeList;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.xesstandard.model.XesAttribute;
import org.xesstandard.model.XesClassifier;
import org.xesstandard.model.XesComponent;
import org.xesstandard.model.XesEvent;
import org.xesstandard.model.XesExtension;
import org.xesstandard.model.XesLog;
import org.xesstandard.model.XesTrace;
import org.xesstandard.model.attributes.XesBooleanAttribute;
import org.xesstandard.model.attributes.XesDateTimeAttribute;
import org.xesstandard.model.attributes.XesIdAttribute;
import org.xesstandard.model.attributes.XesIntegerNumberAttribute;
import org.xesstandard.model.attributes.XesListAttribute;
import org.xesstandard.model.attributes.XesRealNumberAttribute;
import org.xesstandard.model.attributes.XesStringAttribute;
import org.xml.sax.SAXException;

/**
 * Converts a Xes log to an Xlog.
 * @reference : 
 * https://svn.win.tue.nl/repos/prom/Packages/XESStandardConnector/Trunk/src/org/processmining/xesstandardconnector/algorithms/XesImportAbstractAlgorithm.java
 * @author HVERBEEK
 *
 */
public class XesConvertToXLogAlgorithm {

	/*
	 * The factory to create X objects.
	 */
	private XFactory factory;
	
	/**
	 * 
	 * @param log The given Xes log.
	 * @return The XLog that results from converting the given log.
	 */
	public XLog convertToLog(XesLog log) {
		// Create the factory.
		factory = XFactoryRegistry.instance().currentDefault();
		// Create an empty XLog.
		XLog convertedLog = factory.createLog();
		// Convert the extensions.
		for (XesExtension extension : log.getExtensions()) {
			XExtension convertedExtension = convert(extension);
			if (convertedExtension != null) {
				convertedLog.getExtensions().add(convert(extension));
			}
		}
		// Convert the global event attributes.
		for (XesAttribute attribute : log.getGlobalEventAttributes()) {
			convertedLog.getGlobalEventAttributes().add(convert(attribute));
		}
		// Convert the global trace attributes.
		for (XesAttribute attribute : log.getGlobalTraceAttributes()) {
			convertedLog.getGlobalTraceAttributes().add(convert(attribute));
		}
		// Convert the event classifiers.
		for (XesClassifier classifier : log.getEventClassifiers()) {
			convertedLog.getClassifiers().add(convert(classifier));
		}
		// Convert the log attributes.
		convert(log, convertedLog);
		// Convert the traces.
		for (XesTrace trace : log.getTraces()) {
			convertedLog.add(convert(trace));
		}
		// Convert the trace-less events: add them to proper traces. We need a trace classifier to do this.
		if (!log.getTraceClassifiers().isEmpty()) {
			// Take the first trace classifier.
			XEventClassifier classifier = convert(log.getTraceClassifiers().get(0));
			// Create a map from trace names to traces.
			Map<String, XTrace> name2Trace = new HashMap<String, XTrace>();
			for (XTrace trace : convertedLog) {
				String name = XConceptExtension.instance().extractName(trace);
				if (name != null) {
					name2Trace.put(name, trace);
				}
			}
			// Now assign every event to a proper trace.
			for (XesEvent event : log.getEvents()) {
				// Convert the event.
				XEvent convertedEvent = convert(event);
				// Get the trace name for this event, using the converted first trace classifier.
				String name = classifier.getClassIdentity(convertedEvent);
				// Create an empty trace if no trace for this name exists yet.
				if (!name2Trace.containsKey(name)) {
					XTrace newTrace = XFactoryRegistry.instance().currentDefault().createTrace();
					XConceptExtension.instance().assignName(newTrace, name);
					name2Trace.put(name, newTrace);
					convertedLog.add(newTrace);
				}
				// Add the event at the back of the trace with that name.
				name2Trace.get(name).add(convertedEvent);
			}
		}
		// Return the converted log.
		return convertedLog;
	}

	/*
	 * COnvert an extension.
	 */
	private XExtension convert(XesExtension extension) {
		String name = extension.getName();
		if (name == null) {
			name = "<!-- name not set -->";
		}
		XExtension convertedExtension = XExtensionManager.instance().getByName(name);
		if (convertedExtension == null) {
			try {
				convertedExtension = XExtensionParser.instance().parse(extension.getURI());
			} catch (IOException | ParserConfigurationException | SAXException e) {
				// Cannot convert extension.
			}
		}
		return convertedExtension;
	}

	/*
	 * Convert an event classifier.
	 */
	private XEventClassifier convert(XesClassifier classifier) {
		String name = classifier.getName();
		if (name == null) {
			name = "<!-- name not set -->";
		}
		XEventClassifier convertedClassifier = new XEventAttributeClassifier(name,
				classifier.getKeys().toArray(new String[0]));
		return convertedClassifier;
	}

	/*
	 * Convert a trace.
	 */
	private XTrace convert(XesTrace trace) {
		XTrace convertedTrace = factory.createTrace();
		convert(trace, convertedTrace);
		for (XesEvent event : trace.getEvents()) {
			convertedTrace.add(convert(event));
		}
		return convertedTrace;
	}

	/*
	 * Convert an event.
	 */
	private XEvent convert(XesEvent event) {
		XEvent convertedEvent = factory.createEvent();
		convert(event, convertedEvent);
		return convertedEvent;
	}

	/*
	 * Convert all attributes of the component.
	 */
	private void convert(XesComponent component, XAttributable element) {
		for (XesAttribute attribute : component.getAttributes()) {
			element.getAttributes().put(attribute.getKey(), convert(attribute));
		}
	}

	/*
	 * Convert an attribute.
	 */
	private XAttribute convert(XesAttribute attribute) {
		XAttribute convertedAttribute = null;
		String key = attribute.getKey();
		if (key == null) {
			key = "<!-- key not set -->";
		}
		switch (attribute.getType()) {
			case STRING :
				String stringValue = ((XesStringAttribute) attribute).getValue();
				if (stringValue == null) {
					stringValue = "";
				}
				convertedAttribute = factory.createAttributeLiteral(key, stringValue, null);
				break;
			case DATETIME :
				Date dateValue = ((XesDateTimeAttribute) attribute).getValue();
				if (dateValue == null) {
					dateValue = new Date(0L);
				}
				convertedAttribute = factory.createAttributeTimestamp(key, dateValue, null);
				break;
			case INTEGERNUMBER :
				Long longValue = ((XesIntegerNumberAttribute) attribute).getValue();
				if (longValue == null) {
					longValue = 0L;
				}
				convertedAttribute = factory.createAttributeDiscrete(key, longValue, null);
				break;
			case REALNUMBER :
				Double doubleValue = ((XesRealNumberAttribute) attribute).getValue();
				if (doubleValue == null) {
					doubleValue = 0.0;
				}
				convertedAttribute = factory.createAttributeContinuous(key, doubleValue, null);
				break;
			case BOOLEAN :
				Boolean boolValue = ((XesBooleanAttribute) attribute).getValue();
				if (boolValue == null) {
					boolValue = false;
				}
				convertedAttribute = factory.createAttributeBoolean(key, boolValue, null);
				break;
			case ID :
				UUID idValue = ((XesIdAttribute) attribute).getValue();
				if (idValue == null) {
					idValue = new UUID(0, 0);
				}
				convertedAttribute = factory.createAttributeID(key, new XID(idValue), null);
				break;
			case LIST :
				convertedAttribute = factory.createAttributeList(attribute.getKey(), null);
				for (XesAttribute valueAttribute : ((XesListAttribute) attribute).getValues()) {
					((XAttributeList) convertedAttribute).addToCollection(convert(valueAttribute));
				}
				break;
			default :
		}
		convert(attribute, convertedAttribute);
		return convertedAttribute;
	}
}
