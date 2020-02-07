package org.pm4knime.node.logmanipulation.filter;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "FilterByFrequency" node.
 *
 * @author Kefang Ding
 */
public class FilterByFrequencyNodeFactory 
        extends NodeFactory<FilterByFrequencyNodeModel> {
    /**
     * {@inheritDoc}
     */
    @Override
    public FilterByFrequencyNodeModel createNodeModel() {
		// Create and return a new node model.
        return new FilterByFrequencyNodeModel();
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
    public NodeView<FilterByFrequencyNodeModel> createNodeView(final int viewIndex,
            final FilterByFrequencyNodeModel nodeModel) {
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
        return new FilterByFrequencyNodeDialog();
    }

}

