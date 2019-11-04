package org.pm4knime.node.replayer;

import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "PNReplayer" node.
 * This class is saved for later use. Currently, we don't need it here
 * @author 
 */
public class DefaultPNReplayerNodeView extends NodeView<DefaultPNReplayerNodeModel> {

    /**
     * Creates a new view.
     * 
     * @param nodeModel The model (class: {@link DefaultPNReplayerNodeModel})
     */
    protected DefaultPNReplayerNodeView(final DefaultPNReplayerNodeModel nodeModel) {
        super(nodeModel);
        // there is no view here to show the alignment??
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
        // TODO: generated method stub
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {
        // TODO: generated method stub
    }

}

