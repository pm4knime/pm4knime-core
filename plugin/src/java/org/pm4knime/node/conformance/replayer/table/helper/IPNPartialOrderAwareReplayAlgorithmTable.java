package org.pm4knime.node.conformance.replayer.table.helper;

import org.processmining.plugins.astar.petrinet.PartialOrderBuilder;

public interface IPNPartialOrderAwareReplayAlgorithmTable extends IPNReplayAlgorithmTable{
	
	/**
	 * Set the partial order builder for any implementation of the IPN
	 * algorithm. By default, this should be the PartialOrderBuilder.DEFAULT,
	 * which builds partial orders based on timestamps.
	 * 
	 * @param poBuilder
	 */
	public void setPartialOrderBuilder(PartialOrderBuilder poBuilder);

	/**
	 * Returns the current partial order builder
	 * 
	 * @return
	 */
	public PartialOrderBuilder getPartialOrderBuilder();

}
