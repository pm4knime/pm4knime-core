package org.pm4knime.node.logmanipulation.split;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SplitLog" Node.
 * This node split one event log according to chosen attribute value, or group them together according to attribute value
 *
 * @author Kefang Ding
 */
public class SplitLogNodeFactory 
        extends NodeFactory<SplitLogNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SplitLogNodeModel createNodeModel() {
        return new SplitLogNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<SplitLogNodeModel> createNodeView(final int viewIndex,
            final SplitLogNodeModel nodeModel) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new SplitLogNodeDialog();
    }

}

