package org.pm4knime.node.logmanipulation.split2;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "LogSplitter" Node.
 * This log splitter is independent on the event log... we just give the choices , in benefit to use flowVariables
 *
 * @author Kefang
 */
public class LogSplitterNodeFactory 
        extends NodeFactory<LogSplitterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LogSplitterNodeModel createNodeModel() {
        return new LogSplitterNodeModel();
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
    public NodeView<LogSplitterNodeModel> createNodeView(final int viewIndex,
            final LogSplitterNodeModel nodeModel) {
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
        return new LogSplitterNodeDialog();
    }

}

