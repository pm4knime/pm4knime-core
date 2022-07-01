package org.pm4knime.node.conformance.replayer.table.helper;
import java.util.HashMap;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;

public class TransEvClassMappingTable extends HashMap<Transition, String> {
	private static final long serialVersionUID = -4344051692440782096L;
	private String eventClassifier;
	private String dummyEventClass;
	
	
	@SuppressWarnings("unused")
	private TransEvClassMappingTable(){}; // this constructor is not allowed
	
	/**
	 * Allowed constructor
	 * @param eventClassifier
	 * @param dummyEventClass
	 */
	public TransEvClassMappingTable(String eventClassifier, String dummyEventClass){
		this.eventClassifier = eventClassifier;
		this.dummyEventClass = dummyEventClass;
	}
	
	/**
	 * get the classifier
	 * @return
	 */
	public String getEventClassifier(){
		return this.eventClassifier;
	}
	
	/**
	 * Get event class that is used to represent transition (not invisible ones) that is not mapped to 
	 * any activity
	 * 
	 * @return
	 */
	public String getDummyEventClass(){
		return this.dummyEventClass;
	}
}
