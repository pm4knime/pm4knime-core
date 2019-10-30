package org.pm4knime.util;
/**
 * this class lists unified names and descriptions for common variables. For example, for
 * output PortObject Petri net from InductiveMiner or AlphaMiner, we assign it with the unified name. 
 * Also, the parameter, like event classifier, we assign it the same name there.
 * But how do we do it in a xml file?
 * @author kefang-pads
 *
 */
public class UnifiedConfiguration {

	public final String CFG_PN_NAME = "Petri net";
	public final String CFG_PN_DESC = "Petri net is a common process model in Process Mining. "
			+ "It composes of the transitions and places.";
	
}
