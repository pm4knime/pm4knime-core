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
public class InductiveMinerNodeFactory2 
        extends NodeFactory<InductiveMinerNodeModel2> {

    /**
     * {@inheritDoc}
     */
    @Override
    public InductiveMinerNodeModel2 createNodeModel() {
        return new InductiveMinerNodeModel2();
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
    public NodeView<InductiveMinerNodeModel2> createNodeView(final int viewIndex,
            final InductiveMinerNodeModel2 nodeModel) {
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
        return new InductiveMinerNodeDialog2();
    }

}

