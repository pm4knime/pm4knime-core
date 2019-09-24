package org.pm4knime.node.discovery.inductiveminer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "InductiveMiner" Node.
 * use the inductive miner to do process discovery
 *
 * @author KFDing
 */
public class InductiveMinerNodeFactory 
        extends NodeFactory<InductiveMinerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public InductiveMinerNodeModel createNodeModel() {
        return new InductiveMinerNodeModel();
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
    public NodeView<InductiveMinerNodeModel> createNodeView(final int viewIndex,
            final InductiveMinerNodeModel nodeModel) {
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
        return new InductiveMinerNodeDialog();
    }

}

