package org.pm4knime.node.discovery.cgminer.table;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


public class TableCGMinerNodeFactory 
        extends NodeFactory<TableCGMinerNodeModel> {
	TableCGMinerNodeModel node;

    /**
     * {@inheritDoc}
     */
    @Override
    public TableCGMinerNodeModel createNodeModel() {
		node = new TableCGMinerNodeModel();
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
    public NodeView<TableCGMinerNodeModel> createNodeView(final int viewIndex,
            final TableCGMinerNodeModel nodeModel) {
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
		return new TableCGMinerNodeDialog(node);
    }

}

