package org.pm4knime.node.discovery.ilpminer.Table.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.pm4knime.util.defaultnode.TraceVariantRepresentation;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.hybridilpminer.parameters.DiscoveryStrategy;
import org.processmining.hybridilpminer.parameters.DiscoveryStrategyType;
import org.processmining.hybridilpminer.parameters.HybridILPMinerParametersImpl;
import org.processmining.hybridilpminer.parameters.LPConstraintType;
import org.processmining.hybridilpminer.parameters.LPFilter;
import org.processmining.hybridilpminer.parameters.LPFilterType;
import org.processmining.hybridilpminer.parameters.LPObjectiveType;
import org.processmining.hybridilpminer.parameters.LPVariableType;
import org.processmining.hybridilpminer.parameters.NetClass;
import org.processmining.lpengines.interfaces.LPEngine.EngineType;

public class TableHybridILPMinerParametersImpl extends HybridILPMinerParametersImpl {

	private boolean applyStructuralRedundantPlaceRemoval = false;
	private TraceVariantRepresentation log = null;
//	private XEventClassifier classifier;
	private HashMap<String, XEventClass> map;
	private HashMap<Integer, XEventClass> indexMap;
	
	public TableHybridILPMinerParametersImpl(PluginContext context) {
		super(context);
	}

	public TableHybridILPMinerParametersImpl(PluginContext context, EngineType engine,
			DiscoveryStrategy discoveryStrategy, NetClass netClass, Collection<LPConstraintType> constraints,
			LPObjectiveType objectiveType, LPVariableType variableType, LPFilter filter, boolean solve, TraceVariantRepresentation log,
			String classifier) {
		super(context, engine, discoveryStrategy, netClass, constraints, objectiveType, variableType, filter, solve);
		this.log = log;
		
		map = new HashMap<String, XEventClass>();
		indexMap = new HashMap<Integer, XEventClass>();
		int i = 0;
		for (String act : log.getActivities()) {
			map.put(act, new XEventClass(act, i));
			indexMap.put(i, new XEventClass(act, i));
			i++;
		}
	}

	public TableHybridILPMinerParametersImpl(final PluginContext context, final TraceVariantRepresentation log) {
		super(context);
		this.log = log;
		
		map = new HashMap<String, XEventClass>();
		indexMap = new HashMap<Integer, XEventClass>();
		int i = 0;
		for (String act : log.getActivities()) {
			map.put(act, new XEventClass(act, i));
			indexMap.put(i, new XEventClass(act, i));
			i++;
		}
	}

	public TableHybridILPMinerParametersImpl(final PluginContext context, final TraceVariantRepresentation log,
			final String classifier) {
		this(context, log);
	}


	public TraceVariantRepresentation getLog() {
		return log;
	}

	public String htmlPrettyPrint() {
		String result = "<html><table>";
		result += "<tr><td>Net class</td><td>" + getNetClass().toString() + "</td></tr>";
		result += "<tr><td>Constraints</td><td>" + getLPConstraintTypes().toString() + "</td></tr>";
		result += "<tr><td>Filter</td><td>" + getFilter().getFilterType().toString() + "</td></tr>";
		if (getFilter().getFilterType() != LPFilterType.NONE) {
			result += "<tr><td>Filter Threshold</td><td>" + Double.toString(getFilter().getThreshold()) + "</td></tr>";
			;
		}
		result += "<tr><td>Discovery Strategy</td><td>" + getDiscoveryStrategy().getDiscoveryStrategyType().toString()
				+ "</td></tr>";
		if (getDiscoveryStrategy().getDiscoveryStrategyType().equals(DiscoveryStrategyType.CAUSAL_E_VERBEEK)) {
			result += "<tr><td>Causal Graph</td><td>"
					+ getDiscoveryStrategy().getCausalActivityGraphParameters().getMiner();
			result += "</td></tr>";
			result += "<tr><td>Causal Miner Parameters</td><td>";
			result += "zero value: " + getDiscoveryStrategy().getCausalActivityGraphParameters().getZeroValue();
			result += ", include threshold: "
					+ getDiscoveryStrategy().getCausalActivityGraphParameters().getIncludeThreshold();
			result += ", concurrency ratio: "
					+ getDiscoveryStrategy().getCausalActivityGraphParameters().getConcurrencyRatio() + "</td></tr>";
		}
		result += "<tr><td>Objective</td><td>" + getObjectiveType().toString() + "</td></tr>";
		result += "<tr><td>Variables</td><td>" + getLPVaraibleType().toString() + "</td></tr>";
		result += "<tr><td>Engine</td><td>" + getEngine().toString() + "</td></tr>";
		result += "</table></html>";
		return result;
	}

	public boolean isApplyStructuralRedundantPlaceRemoval() {
		return applyStructuralRedundantPlaceRemoval;
	}

	public void setApplyStructuralRedundantPlaceRemoval(boolean applyStructuralRedundantPlaceRemoval) {
		this.applyStructuralRedundantPlaceRemoval = applyStructuralRedundantPlaceRemoval;
	}

	public void setLog(TraceVariantRepresentation log) {
		this.log = log;
		map = new HashMap<String, XEventClass>();
		indexMap = new HashMap<Integer, XEventClass>();
		int i = 0;
		for (String act : log.getActivities()) {
			map.put(act, new XEventClass(act, i));
			indexMap.put(i, new XEventClass(act, i));
			i++;
		}
	}

	@Override
	public String toString() {
		String result = "[";
		result += "cons: " + getLPConstraintTypes().toString() + ", ";
		result += "fil: " + getFilter().getFilterType().toString();
		if (getFilter().getFilterType() != LPFilterType.NONE) {
			result += ", threshold: " + Double.toString(getFilter().getThreshold());
		}
		result += ", ";
		result += "stra: " + getDiscoveryStrategy().getDiscoveryStrategyType().toString() + ", ";
		result += "obj: " + getObjectiveType().toString() + ", ";
		result += "var: " + getLPVaraibleType().toString() + ", ";
		result += "engine: " + getEngine().toString();
		result += "]";
		return result;
	}

//	public XEventClassifier getEventClassifier() {
//		// TODO Auto-generated method stub
//		return classifier;
//	}

	public Map<String, XEventClass> getMap() {
		// TODO Auto-generated method stub
		return this.map;
	}
	
	public Map<Integer, XEventClass> getIndexMap() {
		// TODO Auto-generated method stub
		return this.indexMap;
	}

	public XEventClasses getEventClasses() {
		return new TableEventClasses(this.map);
	
	}

}
