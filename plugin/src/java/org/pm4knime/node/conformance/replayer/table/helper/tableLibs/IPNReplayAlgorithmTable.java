package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParamProvider;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import nl.tue.astar.AStarException;

public interface IPNReplayAlgorithmTable {
	/**
	 * Replay log assuming GUI exist (progress bars, etc)
	 * 
	 * @param context
	 * @param net
	 * @param log
	 * @param mapping
	 * @param parameter
	 * @return
	 */
	public PNRepResult replayLog(PluginContext context, PetrinetGraph net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayParameter parameter) throws AStarException;

	/**
	 * HTML explanation of the algorithm
	 * 
	 * @return
	 */
	public String getHTMLInfo();

	/**
	 * The name of the algorithm
	 * 
	 * @return
	 */
	public String toString();

	/**
	 * construct GUI in which the parameter for this algorithm can be obtained
	 * 
	 * @param context
	 * @param net
	 * @param log
	 * @param mapping
	 * @return
	 */
	public IPNReplayParamProvider constructParamProvider(PluginContext context, PetrinetGraph net, TableEventLog log,
			TransEvClassMappingTable mapping);

	/**
	 * Return true if input of replay without parameters are correct (e.g. the
	 * net should have a dead marking, start with a single starting place, etc)
	 * 
	 * @param net
	 * @param log
	 * @param mapping
	 * @return
	 */
	public boolean isReqWOParameterSatisfied(PluginContext context, PetrinetGraph net, TableEventLog log,
			TransEvClassMappingTable mapping);

	/**
	 * Return true if all replay inputs are correct
	 * 
	 * @param net
	 * @param log
	 * @param mapping
	 * @param parameter
	 * @return
	 */
	public boolean isAllReqSatisfied(PluginContext context, PetrinetGraph net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayParameter parameter);

}
