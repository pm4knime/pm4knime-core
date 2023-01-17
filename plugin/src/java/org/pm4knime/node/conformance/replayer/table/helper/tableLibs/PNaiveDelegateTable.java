package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;

import nl.tue.astar.impl.State;
import nl.tue.astar.impl.memefficient.TailInflater;
import nl.tue.astar.util.ShortShortMultiset;
import nl.tue.storage.CompressedHashSet;
import nl.tue.storage.Deflater;

public class PNaiveDelegateTable extends AbstractPDelegateTable<PNaiveTailTable> {
	
	private final PNaiveTailTable compressor;
	private final boolean allMarkingsAreFinal;

	public PNaiveDelegateTable(ResetNet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost, int delta,
			boolean allMarkingsAreFinal, Marking... set) {
		super(net, log, map, mapTrans2Cost, mapEvClass2Cost, delta, set);
		this.allMarkingsAreFinal = allMarkingsAreFinal;

		compressor = PNaiveTailTable.EMPTY;
	}

	public PNaiveDelegateTable(InhibitorNet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost, int delta,
			boolean allMarkingsAreFinal, Marking... set) {
		super(net, log, map, mapTrans2Cost, mapEvClass2Cost, delta, set);
		this.allMarkingsAreFinal = allMarkingsAreFinal;

		compressor = PNaiveTailTable.EMPTY;
	}

	public PNaiveDelegateTable(ResetInhibitorNet net, TableEventLog log,  TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost, int delta,
			boolean allMarkingsAreFinal, Marking... set) {
		super(net, log, map, mapTrans2Cost, mapEvClass2Cost, delta, set);
		this.allMarkingsAreFinal = allMarkingsAreFinal;

		compressor = PNaiveTailTable.EMPTY;
	}

	public PNaiveDelegateTable(Petrinet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost, int delta,
			boolean allMarkingsAreFinal, Marking... set) {
		super(net, log, map, mapTrans2Cost, mapEvClass2Cost, delta, set);
		this.allMarkingsAreFinal = allMarkingsAreFinal;

		compressor = PNaiveTailTable.EMPTY;
	}

	public PNaiveTailTable createInitialTail(PHeadTable head) {
		return PNaiveTailTable.EMPTY;
	}

	public TailInflater<PNaiveTailTable> getTailInflater() {
		return compressor;
	}

	public Deflater<PNaiveTailTable> getTailDeflater() {
		return compressor;
	}

	public void setStateSpace(CompressedHashSet<State<PHeadTable, PNaiveTailTable>> statespace) {

	}

	public boolean isFinal(ShortShortMultiset marking) {
		if (allMarkingsAreFinal) {
			return true;
		} else {
			if (finalMarkings.size() > 0) {
				return super.isFinal(marking);
			} else {
				return !hasEnabledTransitions(marking);
			}
		}
	}

}
