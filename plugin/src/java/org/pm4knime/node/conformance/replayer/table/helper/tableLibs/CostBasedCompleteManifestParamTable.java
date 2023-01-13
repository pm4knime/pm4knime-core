package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Map;
import java.util.Set;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

public class CostBasedCompleteManifestParamTable extends CostBasedCompleteParamTable {
	/**
	 * Attribute
	 */
	private Set<Transition> restrictedTrans;
	
	/**
	 * Default constructor, assuming that the cost of move sync is 0
	 * 
	 * @param mapEvClass2Cost
	 * @param mapTrans2Cost
	 * @param initMarking
	 * @param finalMarkings
	 * @param maxNumOfStates
	 * @param moveModelTrans
	 * @param restrictedTrans
	 */
	public CostBasedCompleteManifestParamTable(Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapTrans2Cost, Marking initMarking, Marking[] finalMarkings, int maxNumOfStates,
			Set<Transition> restrictedTrans) {
		super(mapEvClass2Cost, mapTrans2Cost);
		setInitialMarking(initMarking);
		setFinalMarkings(finalMarkings);
		setMaxNumOfStates(maxNumOfStates);
		this.restrictedTrans = restrictedTrans;
	}
	
	/**
	 * Constructor with mapping from sync moves to cost
	 * 
	 * @param mapEvClass2Cost
	 * @param mapTrans2Cost
	 * @param initMarking
	 * @param finalMarkings
	 * @param maxNumOfStates
	 * @param moveModelTrans
	 * @param fragmentTrans
	 */
	public CostBasedCompleteManifestParamTable(Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapTrans2Cost, Map<Transition, Integer> mapSync2Cost, Marking initMarking, Marking[] finalMarkings, int maxNumOfStates,
			Set<Transition> fragmentTrans) {
		super(mapEvClass2Cost, mapTrans2Cost);
		setMapSync2Cost(mapSync2Cost);
		setInitialMarking(initMarking);
		setFinalMarkings(finalMarkings);
		setMaxNumOfStates(maxNumOfStates);
		this.restrictedTrans = fragmentTrans;
	}

	/**
	 * @return the notMoveModelTransitions
	 */
	public Set<Transition> getRestrictedTrans() {
		return restrictedTrans;
	}

	/**
	 * @param restrictedTrans the restrictedTrans to set
	 */
	public void setRestrictedTrans(Set<Transition> restrictedTrans) {
		this.restrictedTrans = restrictedTrans;
	}
}
