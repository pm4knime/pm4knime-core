package org.pm4knime.node.conversion.hpn2table;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class HybridPetriNet2TableConverterNodeFactory extends NodeFactory<HybridPetriNet2TableConverterNodeModel> {

    
	HybridPetriNet2TableConverterNodeModel node;
	/**
     * {@inheritDoc}
     */
    @Override
    public HybridPetriNet2TableConverterNodeModel createNodeModel() {
		// Create and return a new node model.
        node = new HybridPetriNet2TableConverterNodeModel();
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
    public NodeView<HybridPetriNet2TableConverterNodeModel> createNodeView(final int viewIndex,
            final HybridPetriNet2TableConverterNodeModel nodeModel) {
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
    //set this false

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
		// This example node has a dialog, hence we create and return it here. Also see "hasDialog()".
        return new HybridPetriNet2TableConverterNodeDialog(node);
    }
    //set this null

}

