package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.text.NumberFormat;

import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.connections.petrinets.PNRepResultAllRequiredParamConnection;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import nl.tue.astar.AStarException;

public class PNLogReplayerTable {
	@PluginVariant(variantLabel = "Complete parameters", requiredParameterLabels = { 0, 1, 2, 3, 4 })
	public PNRepResult replayLog(PluginContext context, Petrinet net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
		return replayLogPrivate(context, net, log, mapping, selectedAlg, parameters);
	}
	@PluginVariant(variantLabel = "Complete parameters", requiredParameterLabels = { 0, 1, 2, 3, 4 })
	public PNRepResult replayLog(PluginContext context, ResetNet net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
		return replayLogPrivate(context, net, log, mapping, selectedAlg, parameters);
	}
	@PluginVariant(variantLabel = "Complete parameters", requiredParameterLabels = { 0, 1, 2, 3, 4 })
	public PNRepResult replayLog(PluginContext context, ResetInhibitorNet net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
		return replayLogPrivate(context, net, log, mapping, selectedAlg, parameters);
	}
	@PluginVariant(variantLabel = "Complete parameters", requiredParameterLabels = { 0, 1, 2, 3, 4 })
	public PNRepResult replayLog(PluginContext context, InhibitorNet  net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
		return replayLogPrivate(context, net, log, mapping, selectedAlg, parameters);
	}
	
	public PNRepResult replayLog(PluginContext context, PetrinetGraph  net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
		return replayLogPrivate(context, net, log, mapping, selectedAlg, parameters);
	}

	/**
	 * Main method to replay log.
	 * 
	 * @param context
	 * @param net
	 * @param log
	 * @param mapping
	 * @param selectedAlg
	 * @param parameters
	 * @return
	 * @throws AStarException
	 */
	private PNRepResult replayLogPrivate(PluginContext context, PetrinetGraph net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters) throws AStarException {
		if (selectedAlg.isAllReqSatisfied(context, net, log, mapping, parameters)) {
			// for each trace, replay according to the algorithm. Only returns two objects
			PNRepResult replayRes = null;

			if (parameters.isGUIMode()) {
				long start = System.nanoTime();

				replayRes = selectedAlg.replayLog(context, net, log, mapping, parameters);

				long period = System.nanoTime() - start;
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMinimumFractionDigits(2);
				nf.setMaximumFractionDigits(2);

				context.log("Replay is finished in " + nf.format(period / 1000000000) + " seconds");
			} else {
				replayRes = selectedAlg.replayLog(context, net, log, mapping, parameters);
			}

			// add connection
			if (replayRes != null) {
				if (parameters.isCreatingConn()) {
					createConnections(context, net, log, mapping, selectedAlg, parameters, replayRes);
				}
			}

			return replayRes;
		} else {
			if (context != null) {
				context.log("The provided parameters is not valid for the selected algorithm.");
				context.getFutureResult(0).cancel(true);
			}
			return null;
		}
	}

	protected void createConnections(PluginContext context, PetrinetGraph net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters, PNRepResult replayRes) {
		context.addConnection(new PNRepResultAllRequiredParamConnectionTable(
				"Connection between replay result, Log" + ", and " + net.getLabel(), net, log, mapping, selectedAlg, parameters, replayRes));
	}

}
