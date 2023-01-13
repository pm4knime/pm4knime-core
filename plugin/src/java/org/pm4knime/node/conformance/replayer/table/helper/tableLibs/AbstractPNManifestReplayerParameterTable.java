package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.petrinet.manifestreplayer.transclassifier.TransClass;

import nl.tue.astar.AStarThread.Canceller;

public class AbstractPNManifestReplayerParameterTable {
	protected boolean gui = false; // if true, there might be messages printed to GUI
	protected boolean buildConnection = true; 
	protected Canceller canceller;

	// cost related
	protected Map<TransClass, Integer> transClass2Cost; 
	protected Map<String, Integer> mapEvClass2Cost;  
	protected Map<TransClass, Integer> transClassSync2Cost;

	protected Marking initMarking;
	protected Marking[] finalMarkings;

	/**
	 * @return the buildConnection
	 */
	public boolean isBuildConnection() {
		return buildConnection;
	}

	/**
	 * @param buildConnection the buildConnection to set
	 */
	public void setBuildConnection(boolean buildConnection) {
		this.buildConnection = buildConnection;
	}

	/**
	 * get cost of move model only (for each original transition)
	 * 
	 * @param t
	 * @return
	 */
	public int getMoveModelCost(TransClass t) {
		Integer cost = transClass2Cost.get(t);
		if (cost != null) {
			return cost.intValue();
		}
		return 0;
	}

	/**
	 * get cost of move log only
	 * 
	 * @param ec
	 * @return
	 */
	public int getMoveLogCost(XEventClass ec) {
		return mapEvClass2Cost.get(ec);
	}

	/**
	 * get cost of move synchronous
	 * 
	 * @param t
	 * @return
	 */
	public int getMoveSyncCost(TransClass t) {
		Integer cost = transClassSync2Cost.get(t);
		if (cost != null) {
			return cost.intValue();
		}
		return 0;
	}
	
	/**
	 * @return the initMarking
	 */
	public Marking getInitMarking() {
		return initMarking;
	}

	/**
	 * @return the finalMarkings
	 */
	public Marking[] getFinalMarkings() {
		return finalMarkings;
	}

	/**
	 * @return the mapEvClass2Cost
	 */
	public Map<String, Integer> getMapEvClass2Cost() {
		return mapEvClass2Cost;
	}
	
	

	/**
	 * @return the transClass2Cost
	 */
	public Map<TransClass, Integer> getTransClass2Cost() {
		return transClass2Cost;
	}


	/**
	 * @param trans2Cost the trans2Cost to set
	 */
	public void setTrans2Cost(Map<TransClass, Integer> trans2Cost) {
		this.transClass2Cost = trans2Cost;
	}

	/**
	 * @param mapEvClass2Cost the mapEvClass2Cost to set
	 */
	public void setMapEvClass2Cost(Map<String, Integer> mapEvClass2Cost) {
		this.mapEvClass2Cost = mapEvClass2Cost;
	}

	
	/**
	 * @param transClassSync2Cost the transClassSync2Cost to set
	 */
	public void setTransSync2Cost(Map<TransClass, Integer> transSync2Cost) {
		this.transClassSync2Cost = transSync2Cost;
	}

	/**
	 * @param initMarking the initMarking to set
	 */
	public void setInitMarking(Marking initMarking) {
		this.initMarking = initMarking;
	}

	/**
	 * @param finalMarkings the finalMarkings to set
	 */
	public void setFinalMarkings(Marking[] finalMarkings) {
		this.finalMarkings = finalMarkings;
	}

	/**
	 * @return true if GUIPluginContext is provided
	 */
	public boolean isGUIMode() {
		return gui;
	}
	
	public void setGUIMode(boolean value){
		this.gui = value;
	}

	public Canceller getCanceller() {
		return canceller;
	}

	public void setCanceller(Canceller canceller) {
		this.canceller = canceller;
	}
}
