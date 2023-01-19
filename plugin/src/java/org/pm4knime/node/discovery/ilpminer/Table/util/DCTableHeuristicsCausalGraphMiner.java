package org.pm4knime.node.discovery.ilpminer.Table.util;

import org.pm4knime.util.defaultnode.TraceVariantRepresentation;
import org.processmining.dataawarecnetminer.extension.dependencies.DependencyHeuristicConfig;
import org.processmining.dataawarecnetminer.model.DependencyAwareCausalGraph;

public final class DCTableHeuristicsCausalGraphMiner {
	
	private final TableEventRelationStorageImpl eventRelations;
	private final TableHeuristicsCausalGraphBuilder heuristicsGraphBuilder;

	public DCTableHeuristicsCausalGraphMiner(TraceVariantRepresentation log, TableHybridILPMinerParametersImpl parameters) {
		this(new TableEventRelationStorageImpl(parameters.getIndexMap()));
	}

//    private static XEventClasses createXEventClasses(TableHybridILPMinerParametersImpl parameters) {
//		XEventClasses classes = new XEventClasses(parameters.getEventClassifier(), parameters.getMap());	
//		return classes;
//	}

//	private static XEventClasses createXEventClasses() {
//		
//	}

	public DCTableHeuristicsCausalGraphMiner(TableEventRelationStorageImpl tableEventRelationStorageImpl) {
		this.eventRelations = tableEventRelationStorageImpl;
		this.heuristicsGraphBuilder = new TableHeuristicsCausalGraphBuilder(getEventRelations());
	}

	public DependencyAwareCausalGraph mineCausalGraph() {
		return heuristicsGraphBuilder.build();
	}
	
	public void setHeuristicsConfig(DependencyHeuristicConfig config) {
		heuristicsGraphBuilder.setConfig(config);
	}

	public DependencyHeuristicConfig getHeuristicsConfig() {
		return heuristicsGraphBuilder.getConfig();
	}

	public TableEventRelationStorageImpl getEventRelations() {
		return eventRelations;
	}

}
