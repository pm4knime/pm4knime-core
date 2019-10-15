package org.pm4knime.node.logmanipulation.sample;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SampleLog" Node.
 * Sample the event log by giving number or a precentage of whole size
 *
 * @author Kefang
 */
public class SampleLogNodeFactory 
        extends NodeFactory<SampleLogNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleLogNodeModel createNodeModel() {
        return new SampleLogNodeModel();
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
    public NodeView<SampleLogNodeModel> createNodeView(final int viewIndex,
            final SampleLogNodeModel nodeModel) {
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
        return new SampleLogNodeDialog();
    }

}

