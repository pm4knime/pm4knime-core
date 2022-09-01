package org.pm4knime.node.conformance.replayer.table.helper;

import org.processmining.models.graphbased.directed.petrinet.InhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.ResetInhibitorNet;
import org.processmining.models.graphbased.directed.petrinet.ResetNet;
import org.processmining.plugins.astar.petrinet.AbstractPetrinetReplayer;
import org.processmining.plugins.astar.petrinet.impl.PNaiveDelegate;
import org.processmining.plugins.astar.petrinet.impl.PNaiveTail;


public class PetrinetReplayerWithoutILPTable extends AbstractPetrinetReplayerTable<PNaiveTailTable, PNaiveDelegateTable>   {
	public String toString() {
		return "Dijkstra-based replayer assuming at most " + Short.MAX_VALUE + " tokens in each place.";
	}

	protected PNaiveDelegateTable getDelegate(PetrinetGraph net, TableEventLog log,String[] classes,
			TransEvClassMappingTable mapping, int delta, int threads) {
		if (net instanceof ResetInhibitorNet) {
			return new PNaiveDelegateTable((ResetInhibitorNet) net, log, mapping, mapTrans2Cost, mapEvClass2Cost,
					delta, false, finalMarkings);
		} else if (net instanceof ResetNet) {
			return new PNaiveDelegateTable((ResetNet) net, log, mapping, mapTrans2Cost, mapEvClass2Cost, delta,
					false, finalMarkings);
		} else if (net instanceof InhibitorNet) {
			return new PNaiveDelegateTable((InhibitorNet) net, log, mapping, mapTrans2Cost, mapEvClass2Cost, delta,
					false, finalMarkings);
		} else if (net instanceof Petrinet) {
			return new PNaiveDelegateTable((Petrinet) net, log, mapping, mapTrans2Cost, mapEvClass2Cost, delta,
					false, finalMarkings);
		}
		return null;

	}


}
