package org.pm4knime.node.logmanipulation.sample.knimetable;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "SampleLog" Node.
 * Sample the event log by giving number or a precentage of whole size
 *
 * @author Kefang
 */
public class SampleLogTableNodeFactory 
        extends NodeFactory<SampleLogTableNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public SampleLogTableNodeModel createNodeModel() {
        return new SampleLogTableNodeModel();
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
    public NodeView<SampleLogTableNodeModel> createNodeView(final int viewIndex,
            final SampleLogTableNodeModel nodeModel) {
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
        return new SampleLogTableNodeDialog();
    }

}

