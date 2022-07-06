package org.pm4knime.node.discovery.dfgminer.dfgTableMiner;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.pm4knime.node.discovery.cgminer.table.TableCGMinerNodeModel;

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
		// The number of views the node should have, in this cases there is none.
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<DfgMinerTableNodeModel> createNodeView(final int viewIndex,
            final DfgMinerTableNodeModel nodeModel) {
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
        return new DfgMinerTableNodeDialog(node);
    }

}

