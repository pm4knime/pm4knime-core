package org.pm4knime.node.conversion.pt2pn;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "PT2PNConverter" node.
 *
 * @author Kefang Ding
 */
public class PT2PNConverterNodeFactory 
        extends NodeFactory<PT2PNConverterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PT2PNConverterNodeModel createNodeModel() {
		// Create and return a new node model.
        return new PT2PNConverterNodeModel();
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
    public NodeView<PT2PNConverterNodeModel> createNodeView(final int viewIndex,
            final PT2PNConverterNodeModel nodeModel) {
		// We return null as this example node does not provide a view. Also see "getNrNodeViews()".
		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
		// Indication whether the node has a dialog or not.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
		// This example node has a dialog, hence we create and return it here. Also see "hasDialog()".
        return null;
    }

}

