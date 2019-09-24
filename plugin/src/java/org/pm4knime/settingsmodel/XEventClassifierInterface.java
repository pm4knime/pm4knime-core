package org.pm4knime.settingsmodel;

import org.deckfour.xes.classification.XEventClassifier;
/**
 * this interface defines the common use of XEventClassifer in nodes, like the discovery, or conformance checking
 * it defines the common types and one method to get the classifier
 * @author kefang-pads
 *
 */
public interface XEventClassifierInterface {
	
	final String CKF_KEY_EVENT_CLASSIFIER = "event_classifier";
	final String CKF_DESC_EVENT_CLASSIFIER = "Specifies how to identify events within the event log, as defined in http://www.xes-standard.org/";
	
	XEventClassifier getXEventClassifier();
	// void setXEventClassifier(List<XEventClassifier> cList) ;
}
