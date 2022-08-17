package org.pm4knime.node.discovery.hybridminer;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

public class HybridMinerNodeFactory 
        extends NodeFactory<HybridMinerNodeModel> {
	
	HybridMinerNodeModel node;
	

    /**
     * {@inheritDoc}
     */
    @Override
    public HybridMinerNodeModel createNodeModel() {
		node = new HybridMinerNodeModel();
		return node;
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
    public NodeView<HybridMinerNodeModel> createNodeView(final int viewIndex,
            final HybridMinerNodeModel nodeModel) {
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
		return new HybridMinerNodeDialog(this.node);
    }

}

