package org.pm4knime.node.io.processtree.writer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "ProcessTreeWriter" Node.
 * Export process tree into file
 *
 * @author DKF
 */
public class ProcessTreeWriterNodeFactory 
        extends NodeFactory<ProcessTreeWriterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ProcessTreeWriterNodeModel createNodeModel() {
        return new ProcessTreeWriterNodeModel();
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
    public NodeView<ProcessTreeWriterNodeModel> createNodeView(final int viewIndex,
            final ProcessTreeWriterNodeModel nodeModel) {
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
        return new ProcessTreeWriterNodeDialog();
    }

}

