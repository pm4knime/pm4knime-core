package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.astar.petrinet.impl.PILPDelegate;
import org.processmining.plugins.astar.petrinet.impl.PILPTail;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayer.algorithms.costbasedcomplete.CostBasedCompleteParam;

public class PetrinetReplayerWithILPTable extends AbstractPetrinetReplayerTable<PILPTailTable, PILPDelegateTable>  {
	private final boolean useFastLowerBound;
	private final boolean useInt;

	public PetrinetReplayerWithILPTable() {
		this(true, true);
	}

	public PetrinetReplayerWithILPTable(boolean useInt, boolean useFastLowerBound) {
		this.useInt = useInt;
		this.useFastLowerBound = useFastLowerBound;

	}

	/**
	 * Return true if all replay inputs are correct
	 */
	public boolean isAllReqSatisfied(PluginContext context, PetrinetGraph net, TableEventLog log, TransEvClassMappingTable mapping,
			IPNReplayParameter parameter) {
		if (super.isAllReqSatisfied(context, net, log, mapping, parameter)) {
			Marking[] finalMarking = ((CostBasedCompleteParamTable) parameter).getFinalMarkings();
			if ((finalMarking != null) && (finalMarking.length > 0)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if input of replay without parameters are correct
	 */
	public boolean isReqWOParameterSatisfied(PluginContext context, PetrinetGraph net, TableEventLog log,
			TransEvClassMappingTable mapping) {
		return super.isReqWOParameterSatisfied(context, net, log, mapping);
	}

	public String toString() {
		return "ILP-based replayer assuming at most " + Short.MAX_VALUE + " tokens in each place.";
	}

	protected PILPDelegateTable getDelegate(PetrinetGraph net, TableEventLog log,  TransEvClassMappingTable mapping,
			int delta, int threads) {
		if (net instanceof ResetInhibitorNet) {
			return new PILPDelegateTable((ResetInhibitorNet) net, log, mapping, mapTrans2Cost, mapEvClass2Cost,
					mapSync2Cost, delta, threads, useInt, useFastLowerBound, finalMarkings);
		} else if (net instanceof ResetNet) {
			return new PILPDelegateTable((ResetNet) net, log, mapping, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost,
					delta, threads, useInt, useFastLowerBound, finalMarkings);
		} else if (net instanceof InhibitorNet) {
			return new PILPDelegateTable((InhibitorNet) net, log,  mapping, mapTrans2Cost, mapEvClass2Cost,
					mapSync2Cost, delta, threads, useInt, useFastLowerBound, finalMarkings);
		} else if (net instanceof Petrinet) {
			return new PILPDelegateTable((Petrinet) net, log,  mapping, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost,
					delta, threads, useInt, useFastLowerBound, finalMarkings);
		}

		return null;
	}

	@Override
	protected PILPDelegateTable getDelegate(PetrinetGraph net, TableEventLog log, String[] classes,
			TransEvClassMappingTable mapping, int delta, int threads) {
		if (net instanceof ResetInhibitorNet) {
			return new PILPDelegateTable((ResetInhibitorNet) net, log, mapping, mapTrans2Cost, mapEvClass2Cost,
					mapSync2Cost, delta, threads, useInt, useFastLowerBound, finalMarkings);
		} else if (net instanceof ResetNet) {
			return new PILPDelegateTable((ResetNet) net, log, mapping, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost,
					delta, threads, useInt, useFastLowerBound, finalMarkings);
		} else if (net instanceof InhibitorNet) {
			return new PILPDelegateTable((InhibitorNet) net, log,  mapping, mapTrans2Cost, mapEvClass2Cost,
					mapSync2Cost, delta, threads, useInt, useFastLowerBound, finalMarkings);
		} else if (net instanceof Petrinet) {
			return new PILPDelegateTable((Petrinet) net, log,  mapping, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost,
					delta, threads, useInt, useFastLowerBound, finalMarkings);
		}

		return null;
	}



}
