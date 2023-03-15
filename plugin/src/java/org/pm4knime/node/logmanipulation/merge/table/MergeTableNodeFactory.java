package org.pm4knime.node.logmanipulation.merge.table;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * This is an example implementation of the node factory of the
 * "MergeLog" node.
 *
 * @author 
 */
public class MergeTableNodeFactory 
        extends NodeFactory<MergeTableNodeModel> {

    MergeTableNodeModel node;

	/**
     * {@inheritDoc}
     */
    @Override
    public MergeTableNodeModel createNodeModel() {
		// Create and return a new node model.
    	node = new MergeTableNodeModel();
    	return node;
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
    public NodeView<MergeTableNodeModel> createNodeView(final int viewIndex,
            final MergeTableNodeModel nodeModel) {
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
        return new MergeTableNodeDialog(this.node);
    }

}

