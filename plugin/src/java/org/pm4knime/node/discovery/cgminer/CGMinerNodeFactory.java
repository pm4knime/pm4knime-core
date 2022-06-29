package org.pm4knime.node.discovery.cgminer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


public class CGMinerNodeFactory 
        extends NodeFactory<CGMinerNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CGMinerNodeModel createNodeModel() {
		return new CGMinerNodeModel();
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
    public NodeView<CGMinerNodeModel> createNodeView(final int viewIndex,
            final CGMinerNodeModel nodeModel) {
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
		return new CGMinerNodeDialog();
    }

}

