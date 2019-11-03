package org.pm4knime.node.replayer;

import org.pm4knime.settingsmodel.SMAlignmentReplayParameterWithCT;

/**
 * <code>NodeModel</code> for the "PNReplayer" node.
 * It we allow multiple algorithms here, we need to allow the getParameter to accept the event class and others
 * @author Kefang Ding
 */
public class PNReplayerNodeModel extends DefaultPNReplayerNodeModel {
	
	@Override
	protected void initializeParameter() {
    	m_parameter = new SMAlignmentReplayParameterWithCT("Parameter in Replayer with CT");
    }

}

