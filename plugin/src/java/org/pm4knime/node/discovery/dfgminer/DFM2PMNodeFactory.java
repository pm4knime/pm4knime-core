package org.pm4knime.node.discovery.dfgminer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "DFM2PM" node.
 *
 * @author Kefang Ding
 */
public class DFM2PMNodeFactory 
        extends NodeFactory<DFM2PMNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DFM2PMNodeModel createNodeModel() {
		// Create and return a new node model.
        return new DFM2PMNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
		// The number of views the node should have, in this cases there is none.
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DFM2PMNodeModel> createNodeView(final int viewIndex,
            final DFM2PMNodeModel nodeModel) {
		// We return null as this example node does not provide a view. Also see "getNrNodeViews()".
		return null;
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
        return new DFM2PMNodeDialog();
    }

}

