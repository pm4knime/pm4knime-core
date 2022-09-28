package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.semantics.petrinet.Marking;


import nl.tue.astar.impl.memefficient.CachedStorageAwareDelegate;

public class PILPDelegateTable extends AbstractPILPDelegateTable<PILPTailTable> implements CachedStorageAwareDelegate<PHeadTable, PILPTailTable> {
	
	protected PILPTailCompressorTable tailCompressor;

	public PILPDelegateTable(Petrinet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost, int delta, int threads,
			Marking... set) {
		this((PetrinetGraph) net, log, map, mapTrans2Cost, mapEvClass2Cost,
				new HashMap<Transition, Integer>(0), delta, threads, true, true, set);
	}

	public PILPDelegateTable(ResetNet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost, int delta, int threads,
			Marking... set) {
		this((PetrinetGraph) net, log,  map, mapTrans2Cost, mapEvClass2Cost,
				new HashMap<Transition, Integer>(0), delta, threads, true, true, set);
	}

	public PILPDelegateTable(InhibitorNet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost, int delta, int threads,
			Marking... set) {
		this((PetrinetGraph) net, log, map, mapTrans2Cost, mapEvClass2Cost,
				new HashMap<Transition, Integer>(0), delta, threads, true, true, set);
	}

	public PILPDelegateTable(ResetInhibitorNet net, TableEventLog log,  TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost, int delta, int threads,
			Marking... set) {
		this((PetrinetGraph) net, log, map, mapTrans2Cost, mapEvClass2Cost,
				new HashMap<Transition, Integer>(0), delta, threads, true, true, set);
	}

	/**
	 * The following constructors accept mapping from sync moves to cost
	 */

	public PILPDelegateTable(Petrinet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, Marking... set) {
		this((PetrinetGraph) net, log, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads,
				true, true, set);
	}

	public PILPDelegateTable(ResetNet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, Marking... set) {
		this((PetrinetGraph) net, log, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads,
				true, true, set);
	}

	public PILPDelegateTable(InhibitorNet net, TableEventLog log,  TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, Marking... set) {
		this((PetrinetGraph) net, log, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads,
				true, true, set);
	}

	public PILPDelegateTable(ResetInhibitorNet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, Marking... set) {
		this((PetrinetGraph) net, log,  map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads,
				true, true, set);
	}

	/**
	 * The following constructors accept mapping from sync moves to cost
	 */

	public PILPDelegateTable(Petrinet net, TableEventLog log,  TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, boolean useInts, boolean useFastLowerBounds,
			Marking... set) {
		this((PetrinetGraph) net, log,  map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads,
				useInts, useFastLowerBounds, set);
	}

	public PILPDelegateTable(ResetNet net, TableEventLog log,  TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, boolean useInts, boolean useFastLowerBounds,
			Marking... set) {
		this((PetrinetGraph) net, log, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads,
				useInts, useFastLowerBounds, set);
	}

	public PILPDelegateTable(InhibitorNet net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, boolean useInts, boolean useFastLowerBounds,
			Marking... set) {
		this((PetrinetGraph) net, log, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads,
				useInts, useFastLowerBounds, set);
	}

	public PILPDelegateTable(ResetInhibitorNet net, TableEventLog log , TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, boolean useInts, boolean useFastLowerBounds,
			Marking... set) {
		this((PetrinetGraph) net, log,  map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads,
				useInts, useFastLowerBounds, set);
	}

	/**
	 * For backwards compatibility
	 */
	@Deprecated
	protected PILPDelegateTable(PetrinetGraph net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, Marking... set) {
		this(net, log,map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads, true, true, set);
	}

	/**
	 * The main constructor that in the end is called by other constructors
	 */
	protected PILPDelegateTable(PetrinetGraph net, TableEventLog log, TransEvClassMappingTable map,
			Map<Transition, Integer> mapTrans2Cost, Map<String, Integer> mapEvClass2Cost,
			Map<Transition, Integer> mapSync2Cost, int delta, int threads, boolean useInts, boolean useFastLowerBounds,
			Marking... set) {
		super(net, log, map, mapTrans2Cost, mapEvClass2Cost, mapSync2Cost, delta, threads, useInts,
				useFastLowerBounds, set);

		this.tailCompressor = new PILPTailCompressorTable(2 * transitions + activities + resetArcs + set.length, places,
				activities);

	}

	/**
	 * Loads the required jar and dll files (from the location) provided by the
	 * user via the settings if not loaded already and creates a solverfactory
	 * 
	 * @return solverfactory
	 * @throws IOException
	 */
	public PILPTailTable createInitialTail(PHeadTable head) {
		return new PILPTailTable(this, head, 0);
	}

	public PILPTailCompressorTable getTailInflater() {
		return tailCompressor;
	}

	public PILPTailCompressorTable getTailDeflater() {
		return tailCompressor;
	}

	public PHeadTableCompressor<PILPTailTable> getHeadInflater() {
		return headCompressor;
	}

	public PHeadTableCompressor<PILPTailTable> getHeadDeflater() {
		return headCompressor;
	}
}
