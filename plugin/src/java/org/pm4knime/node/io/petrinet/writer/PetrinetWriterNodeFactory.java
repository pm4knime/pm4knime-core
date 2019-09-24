package org.pm4knime.node.io.petrinet.writer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "PetrinetWriter" Node.
 * Write Petri net into file to implement the serialization.
 *
 * @author 
 */
public class PetrinetWriterNodeFactory 
        extends NodeFactory<PetrinetWriterNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PetrinetWriterNodeModel createNodeModel() {
        return new PetrinetWriterNodeModel();
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
    public NodeView<PetrinetWriterNodeModel> createNodeView(final int viewIndex,
            final PetrinetWriterNodeModel nodeModel) {
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
        return new PetrinetWriterNodeDialog();
    }

}

