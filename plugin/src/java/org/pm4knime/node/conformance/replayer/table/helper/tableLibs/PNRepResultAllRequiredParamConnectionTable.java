package org.pm4knime.node.conformance.replayer.table.helper.tableLibs;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayAlgorithm;
import org.processmining.plugins.petrinet.replayer.algorithms.IPNReplayParameter;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class PNRepResultAllRequiredParamConnectionTable extends AbstractStrongReferencingConnection {
	public final static String PN = "Petrinet";
	public final static String LOG = "Log";
	public final static String TRANS2EVCLASSMAPPING = "Trans2EventClassMapping";
	public final static String REPLAYALGORITHM = "ReplayAlgorithm";
	public final static String REPLAYPARAMETERS = "ReplayParameters";
	public final static String PNREPRESULT = "PNReplayResult";

	public PNRepResultAllRequiredParamConnectionTable(String label, PetrinetGraph net, TableEventLog log,
			TransEvClassMappingTable mapping, IPNReplayAlgorithmTable selectedAlg, IPNReplayParameter parameters,
			PNRepResult repResult) {
		super(label);
		put(PN, net);
		put(LOG, log);
		putStrong(TRANS2EVCLASSMAPPING, mapping);
		putStrong(REPLAYALGORITHM, selectedAlg);
		putStrong(REPLAYPARAMETERS, parameters);
		put(PNREPRESULT, repResult);
	}
}
