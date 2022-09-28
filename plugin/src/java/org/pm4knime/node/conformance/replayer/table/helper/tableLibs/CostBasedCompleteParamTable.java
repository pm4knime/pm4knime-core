package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.replayer.algorithms.AbstractDefaultPNReplayParam;

public class CostBasedCompleteParamTable extends AbstractDefaultPNReplayParam{
	private Map<String, Integer> mapEvClass2Cost = null;
	private Integer maxNumOfStates = null;
	private Map<Transition, Integer> mapTrans2Cost = null;
	private Marking initialMarking = null;
	private Marking[] finalMarkings = null;
	private Map<Transition, Integer> mapSync2Cost = null;
	private boolean usePartialOrderedEvents = false; // if true, events with same timestamps are treated as partially ordered 

	@SuppressWarnings("unused")
	private CostBasedCompleteParamTable() {
	}

	/**
	 * Constructor with default initialization. Cost of move on model: move on
	 * log = 2 : 5. If no dummy event class exist (i.e. an event class that does
	 * not exist in log, any transitions that are NOT silent and not mapped to
	 * any event class in the log is mapped to it), just put null
	 */
	public CostBasedCompleteParamTable(Collection<String> evClassCol, String dummyEvClass,
			Collection<Transition> transCol) {
		mapEvClass2Cost = new HashMap<String, Integer>();
		if (evClassCol != null) {
			for (String evClass : evClassCol) {
				mapEvClass2Cost.put(evClass, 5);
			}
		}
		if (dummyEvClass != null) {
			mapEvClass2Cost.put(dummyEvClass, 5);
		}
		maxNumOfStates = 200000;

		mapTrans2Cost = new HashMap<Transition, Integer>();
		if (transCol != null) {
			for (Transition t : transCol) {
				if (t.isInvisible()) {
					mapTrans2Cost.put(t, 0);
				} else {
					mapTrans2Cost.put(t, 2);
				}
			}
		}

		mapSync2Cost = new HashMap<Transition, Integer>(0);
		initialMarking = new Marking();
		finalMarkings = new Marking[0];
	}

	/**
	 * Constructor with default initialization. Cost of move on model: move on
	 * log can be adjusted. If no dummy event class exist (i.e. an event class
	 * that does not exist in log, any transitions that are NOT silent and not
	 * mapped to any event class in the log is mapped to it), just put null
	 */
	public CostBasedCompleteParamTable(Collection<String> evClassCol, String dummyEvClass,
			Collection<Transition> transCol, int defMoveOnModelCost, int defMoveOnLogCost) {
		mapEvClass2Cost = new HashMap<String, Integer>();
		if (evClassCol != null) {
			for (String evClass : evClassCol) {
				mapEvClass2Cost.put(evClass, defMoveOnLogCost);
			}
		}
		if (dummyEvClass != null) {
			mapEvClass2Cost.put(dummyEvClass, defMoveOnLogCost);
		}

		this.maxNumOfStates = 200000;

		mapTrans2Cost = new HashMap<Transition, Integer>();
		if (transCol != null) {
			for (Transition t : transCol) {
				if (t.isInvisible()) {
					mapTrans2Cost.put(t, 0);
				} else {
					mapTrans2Cost.put(t, defMoveOnModelCost);
				}
			}
		}
		this.mapSync2Cost = new HashMap<Transition, Integer>(0);
		this.initialMarking = new Marking();
		this.finalMarkings = new Marking[0];
	}

	/**
	 * Constructor with given cost mapping
	 */
	public CostBasedCompleteParamTable(Map<String, Integer> mapEvClass2Cost, Map<Transition, Integer> mapTrans2Cost) {
		this.mapEvClass2Cost = mapEvClass2Cost;
		this.maxNumOfStates = 200000;
		this.mapTrans2Cost = mapTrans2Cost;
		this.mapSync2Cost = new HashMap<Transition, Integer>(0);
		this.initialMarking = new Marking();
		this.finalMarkings = new Marking[0];
	}

	/**
	 * Constructor with given cost mapping (including cost of move sync)
	 */
	public CostBasedCompleteParamTable(Map<String, Integer> mapEvClass2Cost, Map<Transition, Integer> mapTrans2Cost,
			Map<Transition, Integer> mapSync2Cost) {
		this.mapEvClass2Cost = mapEvClass2Cost;
		this.maxNumOfStates = 200000;
		this.mapTrans2Cost = mapTrans2Cost;
		this.mapSync2Cost = mapSync2Cost;
		this.initialMarking = new Marking();
		this.finalMarkings = new Marking[0];
	}

	/**
	 * @return the initialMarking
	 */
	public Marking getInitialMarking() {
		return initialMarking;
	}

	/**
	 * @param initialMarking
	 *            the initialMarking to set
	 */
	public void setInitialMarking(Marking initialMarking) {
		this.initialMarking = initialMarking;
	}

	/**
	 * @return the finalMarkings
	 */
	public Marking[] getFinalMarkings() {
		return finalMarkings;
	}

	/**
	 * @param finalMarkings
	 *            the finalMarkings to set
	 */
	public void setFinalMarkings(Marking... finalMarkings) {
		this.finalMarkings = finalMarkings;
	}

	/**
	 * 
	 * @return
	 */
	public Map<String, Integer> getMapEvClass2Cost() {
		return mapEvClass2Cost;
	}

	/**
	 * 
	 * @return
	 */
	public Integer getMaxNumOfStates() {
		return maxNumOfStates;
	}

	/**
	 * 
	 * @return
	 */
	public Map<Transition, Integer> getMapTrans2Cost() {
		return mapTrans2Cost;
	}

	/**
	 * @param mapEvClass2Cost
	 *            the mapEvClass2Cost to set
	 */
	public void setMapEvClass2Cost(Map<String, Integer> mapEvClass2Cost) {
		this.mapEvClass2Cost = mapEvClass2Cost;
	}

	/**
	 * @param maxNumOfStates
	 *            the maxNumOfStates to set
	 */
	public void setMaxNumOfStates(Integer maxNumOfStates) {
		this.maxNumOfStates = maxNumOfStates;
	}

	/**
	 * @param mapTrans2Cost
	 *            the mapTrans2Cost to set
	 */
	public void setMapTrans2Cost(Map<Transition, Integer> mapTrans2Cost) {
		this.mapTrans2Cost = mapTrans2Cost;
	}

	/**
	 * @return the mapSync2Cost
	 */
	public Map<Transition, Integer> getMapSync2Cost() {
		return mapSync2Cost;
	}

	/**
	 * @param mapSync2Cost
	 *            the mapSync2Cost to set
	 */
	public void setMapSync2Cost(Map<Transition, Integer> mapSync2Cost) {
		this.mapSync2Cost = mapSync2Cost;
	}

	/**
	 * @return the usePartialOrderedEvents
	 */
	public boolean isPartiallyOrderedEvents() {
		return usePartialOrderedEvents;
	}

	/**
	 * @param usePartialOrderedEvents
	 *            the usePartialOrderedEvents to set
	 */
	public void setUsePartialOrderedEvents(boolean usePartialOrderedEvents) {
		this.usePartialOrderedEvents = usePartialOrderedEvents;
	}

	public void replaceTransitions(Map<Transition, Transition> configuration, boolean keepNonReplacedMapping) {
		Map<Transition, Integer> newCostMap = new HashMap<Transition, Integer>();
		for (Entry<Transition, Integer> entry : mapTrans2Cost.entrySet()) {
			Transition transReplacement = configuration.get(entry.getKey());
			if (transReplacement != null) {
				newCostMap.put(transReplacement, entry.getValue());
			} else if (keepNonReplacedMapping) {
				newCostMap.put(entry.getKey(), entry.getValue());
			}
		}
		this.mapTrans2Cost = newCostMap;
	}

}
