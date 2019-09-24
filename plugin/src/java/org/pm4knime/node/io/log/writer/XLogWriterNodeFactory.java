package org.pm4knime.node.io.log.writer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "XLogWriter" Node.
 * It exports the event log into files, which seperates the codes from the reading part
 *
 * @author Kefang
 */
public class XLogWriterNodeFactory 
        extends NodeFactory<XLogWriterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public XLogWriterNodeModel createNodeModel() {
        return new XLogWriterNodeModel();
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
    public NodeView<XLogWriterNodeModel> createNodeView(final int viewIndex,
            final XLogWriterNodeModel nodeModel) {
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
        return new XLogWriterNodeDialog();
    }

}

