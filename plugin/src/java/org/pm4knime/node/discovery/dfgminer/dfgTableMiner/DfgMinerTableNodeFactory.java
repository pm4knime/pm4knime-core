package org.pm4knime.node.discovery.dfgminer.dfgTableMiner;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.pm4knime.node.discovery.defaultminer.DefaultTableMinerDialog;

/**
 * This is an example implementation of the node factory of the
 * "DfgMinerTable" node.
 *
 * @author 
 */
public class DfgMinerTableNodeFactory 
        extends NodeFactory<DfgMinerTableNodeModel> {
	DfgMinerTableNodeModel node;

    /**
     * {@inheritDoc}
     */
    @Override
    public DfgMinerTableNodeModel createNodeModel() {
    	node = new DfgMinerTableNodeModel();
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
    public NodeView<DfgMinerTableNodeModel> createNodeView(final int viewIndex,
            final DfgMinerTableNodeModel nodeModel) {
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
		return new DefaultTableMinerDialog(this.node);
    }

}

