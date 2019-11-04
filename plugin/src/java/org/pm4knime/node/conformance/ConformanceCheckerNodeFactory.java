package org.pm4knime.node.conformance;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "ConformanceChecker" node.
 *
 * @author 
 */
public class ConformanceCheckerNodeFactory 
        extends NodeFactory<ConformanceCheckerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ConformanceCheckerNodeModel createNodeModel() {
		// Create and return a new node model.
        return new ConformanceCheckerNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
		// The number of views the node should have, in this cases there is none.
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<ConformanceCheckerNodeModel> createNodeView(final int viewIndex,
            final ConformanceCheckerNodeModel nodeModel) {
		// We return null as this example node does not provide a view. Also see "getNrNodeViews()".
		return new ConformanceCheckerNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
		// Indication whether the node has a dialog or not.
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
		// This example node has a dialog, hence we create and return it here. Also see "hasDialog()".
        return new ConformanceCheckerNodeDialog();
    }

}

