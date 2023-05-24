package org.pm4knime.node.conversion.pn2table;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class PetriNet2TableConverterNodeFactory extends NodeFactory<PetriNet2TableConverterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PetriNet2TableConverterNodeModel createNodeModel() {
		// Create and return a new node model.
        return new PetriNet2TableConverterNodeModel();
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
    public NodeView<PetriNet2TableConverterNodeModel> createNodeView(final int viewIndex,
            final PetriNet2TableConverterNodeModel nodeModel) {
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
    //set this false

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
		// This example node has a dialog, hence we create and return it here. Also see "hasDialog()".
        return null;
    }
    //set this null

}

