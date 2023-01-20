package org.pm4knime.node.discovery.ilpminer.Table.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.pm4knime.util.defaultnode.TraceVariant;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.Pair;
import org.processmining.hybridilpminer.algorithms.decorators.HybridILPDecoratorImpl;
import org.processmining.hybridilpminer.algorithms.decorators.LPDecorator;
import org.processmining.hybridilpminer.algorithms.decorators.SequenceEncodingFilterMaxInclusionHybridILPDecoratorImpl;
import org.processmining.hybridilpminer.algorithms.decorators.SlackBasedFilterHybridILPDecoratorImpl;
import org.processmining.hybridilpminer.algorithms.miners.HybridPairBasedDummyLPMiner;
import org.processmining.hybridilpminer.algorithms.miners.HybridPairBasedLPMiner;
import org.processmining.hybridilpminer.algorithms.miners.LPMiner;
import org.processmining.hybridilpminer.models.abstraction.interfaces.LPLogAbstraction;
import org.processmining.hybridilpminer.models.lp.variablemapping.factories.VariableMappingFactory;
import org.processmining.hybridilpminer.models.lp.variablemapping.interfaces.HybridVariableMapping;
import org.processmining.hybridilpminer.models.lp.variablemapping.interfaces.SlackBasedFilterHybridVariableMapping;
import org.processmining.models.causalgraph.Relation;
import org.processmining.models.causalgraph.SimpleCausalGraph;
import gnu.trove.set.hash.THashSet;

public class TableLPMinerFactory {
	

	public static LPMiner createLPMiner(TableHybridILPMinerParametersImpl configuration) {
		return createLPMiner(configuration, null);
	}

	public static LPMiner createLPMiner(TableHybridILPMinerParametersImpl configuration, PluginContext context) {
		LPMiner miner = null;
		Pair<Set<XEventClass>, Set<XEventClass>> variableDistribution = determineVariableDistribution(
				configuration.getLog(), configuration);

		LPLogAbstraction<XEventClass> logAbstraction = new TableLPXEventClassBasedXLogToIntArrAbstractionImpl(configuration.getLog(), configuration);
		miner = createLPMiner(configuration, context, logAbstraction, variableDistribution.getFirst(),
				variableDistribution.getSecond());

		return miner;
	}

	private static LPMiner createLPMiner(TableHybridILPMinerParametersImpl configuration, PluginContext context,
			LPLogAbstraction<XEventClass> logAbstraction, Set<XEventClass> singleVariables,
			Set<XEventClass> dualVariables) {
		LPMiner miner = null;
		switch (configuration.getFilter().getFilterType()) {
			case SLACK_VAR :
				SlackBasedFilterHybridVariableMapping<Integer, int[]> slackVarMap = VariableMappingFactory
						.createIntArrSlackBasedFilterHybridVariableMapping(configuration.getEngine(),
								logAbstraction.encode(singleVariables), logAbstraction.encode(dualVariables),
								logAbstraction);
				miner = createLPMiner(configuration.getLog(), configuration, context, logAbstraction, singleVariables,
						dualVariables, slackVarMap);

				break;
			case NONE :
			case SEQUENCE_ENCODING :
			default :
				HybridVariableMapping<Integer> hybridVarMap = VariableMappingFactory.createHybridVariableMapping(
						configuration.getEngine(), logAbstraction.encode(singleVariables),
						logAbstraction.encode(dualVariables));
				miner = createLPMiner(configuration.getLog(), configuration, context, logAbstraction, singleVariables,
						dualVariables, hybridVarMap);
				break;
		}
		return miner;
	}

	@SuppressWarnings("unchecked")
	private static <T extends HybridVariableMapping<Integer>> LPMiner createLPMiner(TraceVariantRepresentation log,
			TableHybridILPMinerParametersImpl configuration, PluginContext context,
			LPLogAbstraction<XEventClass> logAbstraction, Set<XEventClass> singleVariables,
			Set<XEventClass> dualVariables, T varMap) {
		LPMiner miner = null;
		switch (configuration.getFilter().getFilterType()) {
			case SLACK_VAR :
				LPDecorator slackFilterBasedDecorator = new SlackBasedFilterHybridILPDecoratorImpl<SlackBasedFilterHybridVariableMapping<Integer, int[]>>(
						(SlackBasedFilterHybridVariableMapping<Integer, int[]>) varMap, configuration, logAbstraction,
						configuration.getFilter().getThreshold());
				miner = createLPMiner(log, configuration, context, logAbstraction, singleVariables, dualVariables,
						varMap, slackFilterBasedDecorator);
				break;
			case SEQUENCE_ENCODING :
				LPDecorator sequenceEncodingBasedDecorator = new SequenceEncodingFilterMaxInclusionHybridILPDecoratorImpl<HybridVariableMapping<Integer>>(
						varMap, configuration, logAbstraction, configuration.getFilter().getThreshold());
				miner = createLPMiner(log, configuration, context, logAbstraction, singleVariables, dualVariables,
						varMap, sequenceEncodingBasedDecorator);
				break;
			case NONE :
			default :
				LPDecorator conventionalDecorator = new HybridILPDecoratorImpl<HybridVariableMapping<Integer>>(varMap,
						configuration, logAbstraction);
				miner = createLPMiner(log, configuration, context, logAbstraction, singleVariables, dualVariables,
						varMap, conventionalDecorator);
				break;
		}
		return miner;
	}

	private static <T extends HybridVariableMapping<Integer>> LPMiner createLPMiner(TraceVariantRepresentation log,
			TableHybridILPMinerParametersImpl configuration, PluginContext context,
			LPLogAbstraction<XEventClass> logAbstraction, Set<XEventClass> singleVariables,
			Set<XEventClass> dualVariables, T varMap, LPDecorator decorator) {
		LPMiner miner = null;
		Set<Pair<XEventClass, XEventClass>> pairs = new HashSet<>();
		switch (configuration.getDiscoveryStrategy().getDiscoveryStrategyType()) {
			case TRANSITION_PAIR :
			default :
				pairs = getAllEventClassPairs(configuration);
				break;
			case CAUSAL_E_VERBEEK :
				pairs = configuration.getDiscoveryStrategy().getCausalActivityGraph().getSetCausalities();
				break;
			case CAUSAL_FLEX_HEUR :
				pairs = convertCausalities(configuration.getDiscoveryStrategy().getSimpleCag());

				break;
		}
		if (!configuration.isSolve()) {
			miner = new HybridPairBasedDummyLPMiner<LPDecorator, HybridVariableMapping<Integer>, XEventClass>(varMap,
					decorator, logAbstraction, pairs, context, "Event Table",
					configuration.getILPOutputLocation());
		} else {
			miner = new HybridPairBasedLPMiner<LPDecorator, HybridVariableMapping<Integer>, XEventClass>(varMap,
					decorator, logAbstraction, pairs, context);
		}
		return miner;
	}

	private static Set<Pair<XEventClass, XEventClass>> convertCausalities(SimpleCausalGraph cag) {
		Set<Pair<XEventClass, XEventClass>> pairs = new HashSet<>();
		for (Relation r : cag.getCausalRelations()) {
			pairs.add(new Pair<XEventClass, XEventClass>(r.getSource(), r.getTarget()));
		}
		return pairs;
	}

	private static Set<Pair<XEventClass, XEventClass>> getAllEventClassPairs(TableHybridILPMinerParametersImpl configuration) {
		Set<Pair<XEventClass, XEventClass>> pairs = new THashSet<>();
		
		for (String a1 : configuration.getMap().keySet()) {
			for (String a2 : configuration.getMap().keySet()) {
				pairs.add(new Pair<XEventClass, XEventClass>(configuration.getMap().get(a1), configuration.getMap().get(a2)));
			}
		}
		
		return pairs;
	}

	private static Pair<Set<XEventClass>, Set<XEventClass>> determineVariableDistribution(TraceVariantRepresentation log,
			TableHybridILPMinerParametersImpl configuration) {
		Set<XEventClass> singleVariables = new THashSet<>();
		Set<XEventClass> dualVariables = new THashSet<>();
//		LogRelations relations = LogRelationsFactory.constructAlphaLogRelations(log, logInfo);
		switch (configuration.getLPVaraibleType()) {
			case SINGLE :
				singleVariables.addAll(configuration.getMap().values());
				break;
			case DUAL :
				dualVariables.addAll(configuration.getMap().values());
				break;
			case HYBRID :
				singleVariables.addAll(configuration.getMap().values());
				for (Map.Entry<XEventClass, Double> e : getLengthOneLoops(log, configuration).entrySet()) {
					if (e.getValue() > 0) {
						singleVariables.remove(e.getKey());
						dualVariables.add(e.getKey());
					}
				}
				break;
		}
		return new Pair<>(singleVariables, dualVariables);
	}

	private static Map<XEventClass, Double> getLengthOneLoops(TraceVariantRepresentation log,
			TableHybridILPMinerParametersImpl configuration) {
		
		Map<String, XEventClass> classesString = configuration.getMap();
		Map<Integer, XEventClass> classes = configuration.getIndexMap();
		int[][] absoluteDirectlyFollowsMatrix = new int[classes.size()][classes.size()];
		
		for (TraceVariant variant : log.getVariants()) {
			ArrayList<String> trace = variant.getActivities();
			int freq = variant.getFrequency();
			if (!trace.isEmpty()) {
				for (int i = 0; i < trace.size() - 1; i++) {
					XEventClass from = classesString.get(trace.get(i));
					XEventClass to = classesString.get(trace.get(i + 1));
					absoluteDirectlyFollowsMatrix[from.getIndex()][to.getIndex()] = absoluteDirectlyFollowsMatrix[from.getIndex()][to.getIndex()] + freq;
				}
			}
		}
		
		Map<XEventClass, Double> result = new HashMap<XEventClass, Double>();
		for (int index = 0; index < classes.size(); index++) {
			if (absoluteDirectlyFollowsMatrix[index][index] > 0) {
				result.put(classes.get(index), (double) absoluteDirectlyFollowsMatrix[index][index]);
			}
		}
		return result;
	}

}
