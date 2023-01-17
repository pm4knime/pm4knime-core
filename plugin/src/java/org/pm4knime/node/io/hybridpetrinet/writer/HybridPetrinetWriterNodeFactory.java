package org.pm4knime.node.io.hybridpetrinet.writer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "PetrinetWriter" Node.
 * Write Petri net into file to implement the serialization.
 *
 * @author 
 */
public class HybridPetrinetWriterNodeFactory 
        extends NodeFactory<HybridPetrinetWriterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public HybridPetrinetWriterNodeModel createNodeModel() {
        return new HybridPetrinetWriterNodeModel();
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
    public NodeView<HybridPetrinetWriterNodeModel> createNodeView(final int viewIndex,
            final HybridPetrinetWriterNodeModel nodeModel) {
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
        return new HybridPetrinetWriterNodeDialog();
    }

}

