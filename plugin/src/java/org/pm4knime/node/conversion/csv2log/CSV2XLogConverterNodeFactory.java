package org.pm4knime.node.conversion.csv2log;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "CVS2XLogConverter" node.
 *
 * @author Kefang Ding
 */
public class CSV2XLogConverterNodeFactory 
        extends NodeFactory<CSV2XLogConverterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CSV2XLogConverterNodeModel createNodeModel() {
		// Create and return a new node model.
        return new CSV2XLogConverterNodeModel();
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
    public NodeView<CSV2XLogConverterNodeModel> createNodeView(final int viewIndex,
            final CSV2XLogConverterNodeModel nodeModel) {
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
        return new CSV2XLogConverterNodeDialog();
    }

}

