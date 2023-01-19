package org.pm4knime.node.discovery.ilpminer.Table.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.pm4knime.util.defaultnode.TraceVariantRepresentation;
import org.processmining.algorithms.BerthelotAlgorithm;
import org.processmining.dataawarecnetminer.mining.classic.HeuristicsCausalGraphBuilder.HeuristicsConfig;
import org.processmining.dataawarecnetminer.mining.classic.HeuristicsCausalGraphMiner;
import org.processmining.dataawarecnetminer.model.EventRelationStorage;
import org.processmining.dataawarecnetminer.model.EventRelationStorageImpl;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.hybridilpminer.algorithms.miners.LPMiner;
import org.processmining.hybridilpminer.parameters.DiscoveryStrategyType;
import org.processmining.hybridilpminer.parameters.LPConstraintType;
import org.processmining.lpengines.factories.LPEngineFactory;
import org.processmining.models.causalgraph.SimpleCausalGraph;
import org.processmining.models.causalgraph.SimpleCausalGraphImpl;
import org.processmining.models.causalgraph.XEventClassifierAwareSimpleCausalGraph;
import org.processmining.models.connections.petrinets.behavioral.FinalMarkingConnection;
import org.processmining.models.connections.petrinets.behavioral.InitialMarkingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.parameters.BerthelotParameters;


public class TableHybridILPMinerPlugin {
	
	
	public static Object[] applyExpress(PluginContext context, TraceVariantRepresentation log,
			TableHybridILPMinerParametersImpl parameters) {
		context.getProgress().setMinimum(0);
		if (parameters == null) {
			parameters = new TableHybridILPMinerParametersImpl(context, log);
			context.log("ILP Based Discovery Using Express Settings");
			context.getProgress().setMaximum(4);
			context.getProgress().setValue(0);
			//		context.getProgress().setIndeterminate(true);

			context.log("Set Log");
			parameters.setLog(log);
			context.getProgress().inc();

		} else {
			context.getProgress().setMaximum(0);
			context.getProgress().setMaximum(2);
			context.getProgress().setValue(0);
		}
		parameters.setApplyStructuralRedundantPlaceRemoval(true);
		HeuristicsConfig heuristicsConfig = new HeuristicsConfig();
		heuristicsConfig.setAllTasksConnected(true);
		HeuristicsCausalGraphMiner miner = new HeuristicsCausalGraphMiner(new EventRelationStorageImpl(parameters.getEventClasses()));
		System.out.println("COUNT EVENT: " + miner.getEventRelations().countEvents());
		miner.setHeuristicsConfig(heuristicsConfig);

		context.getProgress().inc();
		context.log("Discover Causal Graph");
		SimpleCausalGraph scag = miner.mineCausalGraph();
		System.out.println(scag.getCausalRelations().toString());
		context.getProgress().inc();
		return applyFlexHeur(context, log, new SimpleCausalGraphImpl(scag.getSetActivities(), scag.getCausalRelations()), parameters);
	}


	public static Object[] applyFlexHeur(PluginContext context, TraceVariantRepresentation log, XEventClassifierAwareSimpleCausalGraph cag,
			TableHybridILPMinerParametersImpl params) {
		params.getDiscoveryStrategy().setDiscoveryStrategyType(DiscoveryStrategyType.CAUSAL_FLEX_HEUR);
		params.getDiscoveryStrategy().setSimpleCag(cag);
		final String artiStart = "ARTIFICIAL_START";
		final String artiEnd = "ARTIFICIAL_END";
		TraceVariantRepresentation artificial = TraceVariantRepresentation.addArtificialStartAndEnd(log.getNumberOfTraces(), log.getActivities(), log.getVariants(), artiStart, artiEnd);
		params.setLog(artificial);
		return discoverWithArtificialStartEnd(context, log, artificial, params);
	}

	public static Object[] discover(LPMiner miner, final TraceVariantRepresentation inputLog,
			final TableHybridILPMinerParametersImpl parameters, final String artificStartLabel,
			final String ArtificEndLabel, final boolean removeRedundant) {
		miner.run();
		Pair<Petrinet, Marking> netAndMarking = miner.synthesizeNet();
		Object[] result;
		result = processPetriNet(netAndMarking.getFirst(), artificStartLabel, ArtificEndLabel, parameters.isFindSink(),
				removeRedundant);
		if (!parameters.getLPConstraintTypes().contains(LPConstraintType.EMPTY_AFTER_COMPLETION)) {
			result[2] = null;
		}
		return result;
	}

	public static Object[] discoverWithArtificialStartEnd(final PluginContext context, final TraceVariantRepresentation originalLog,
			final TraceVariantRepresentation artificialLog, final TableHybridILPMinerParametersImpl parameters) {
		if (parameters.getDiscoveryStrategy().getDiscoveryStrategyType().equals(DiscoveryStrategyType.CAUSAL_FLEX_HEUR)
				&& parameters.getDiscoveryStrategy().getSimpleCag() == null) {
			return applyExpress(context, originalLog, parameters);
		}
		ArrayList<String> firstTrace = artificialLog.variants.get(0).getActivities();
		String artificStartLabel = firstTrace.get(0);
		String artificEndLabel = firstTrace.get(firstTrace.size() - 1);
		return discoverWithArtificialStartEnd(context, artificialLog, parameters, artificStartLabel, artificEndLabel);
	}

	public static Object[] discoverWithArtificialStartEnd(final PluginContext context, final TraceVariantRepresentation log,
			final TableHybridILPMinerParametersImpl parameters, final String artificStartLabel,
			final String artificEndLabel) {
		context.log("Establishing connection to selected LP-Engine");
		// establish some engineType connection.
		LPEngineFactory.createLPEngine(parameters.getEngine());
		context.log("Connected to Engine");
		LPMiner miner = TableLPMinerFactory.createLPMiner(parameters, context);
		Object[] result = discover(miner, log, parameters, artificStartLabel, artificEndLabel,
				parameters.isApplyStructuralRedundantPlaceRemoval());
		Connection iMarking = new InitialMarkingConnection((Petrinet) result[0], (Marking) result[1]);
		context.getConnectionManager().addConnection(iMarking);
		if (parameters.getLPConstraintTypes().contains(LPConstraintType.EMPTY_AFTER_COMPLETION)) {
			Connection finalMarking = new FinalMarkingConnection((Petrinet) result[0], (Marking) result[2]);
			context.getConnectionManager().addConnection(finalMarking);
		}
		return result;
	}

	private static Object[] processPetriNet(Petrinet net, final String startName, final String endName,
			boolean findSink, final boolean removeRedundant) {
		Place ini = null, fin = null, unconnected = null;
		Transition start = null, end = null;
		Iterator<Transition> trit = net.getTransitions().iterator();
		Set<Transition> remove = new HashSet<>();
		while (trit.hasNext()) {
			Transition t = trit.next();
			if (t.getLabel().equals(startName)) {
				t.setInvisible(true);
				start = t;
				ini = net.addPlace("source");
				net.addArc(ini, t);
			} else if (t.getLabel().equals(endName)) {
				t.setInvisible(true);
				end = t;
				if (findSink) {
					fin = net.addPlace("sink");
					net.addArc(t, fin);
				}
			} else {
				if (net.getInEdges(t).isEmpty() && net.getOutEdges(t).isEmpty()) {
					if (unconnected == null) {
						unconnected = net.addPlace("p" + net.getPlaces().size());
					}
					net.addArc(unconnected, t);
					net.addArc(t, unconnected);
				}
			}
		}
		if (unconnected != null) {
			net.addArc(start, unconnected);
			net.addArc(unconnected, end);
		}
		for (Transition t : remove) {
			net.removeTransition(t);
		}

		Marking initialMarking = new Marking(Collections.singleton(ini));
		Marking finalMarking = findSink ? new Marking(Collections.singleton(fin)) : new Marking();
		if (findSink && removeRedundant) {
			BerthelotParameters berthelotParameters = new BerthelotParameters();
			berthelotParameters.setInitialMarking(initialMarking);
			berthelotParameters.setFinalMarkings(Collections.singleton(finalMarking));
			BerthelotAlgorithm berthelotAlgorithm = new BerthelotAlgorithm();
			net = berthelotAlgorithm.apply(null, net, berthelotParameters);
			initialMarking = berthelotParameters.getInitialBerthelotMarking();
			finalMarking = berthelotParameters.getFinalBerthelotMarkings().iterator().next();
		}
		return new Object[] { net, initialMarking, finalMarking };

	}

}
