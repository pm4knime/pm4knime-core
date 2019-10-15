package org.pm4knime.node.logmanipulation.classify;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "RandomClassifier" Node.
 * RandomClassifier classifies the event log randomly, and assigns labels to the trace
 *
 * @author Kefang Ding
 */
public class RandomClassifierNodeFactory 
        extends NodeFactory<RandomClassifierNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public RandomClassifierNodeModel createNodeModel() {
        return new RandomClassifierNodeModel();
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
    public NodeView<RandomClassifierNodeModel> createNodeView(final int viewIndex,
            final RandomClassifierNodeModel nodeModel) {
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
        return new RandomClassifierNodeDialog();
    }

}

