package org.pm4knime.node.discovery.heuritsicsminer.table;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.pm4knime.node.discovery.ilpminer.Table.ILPMinerTableNodeModel;

/**
 * This is an example implementation of the node factory of the
 * "HeuristicsMiner" node.
 *
 * @author Kefang Ding
 */
public class HeuristicsMinerTableNodeFactory 
        extends NodeFactory<HeuristicsMinerTableNodeModel> {
	
	HeuristicsMinerTableNodeModel node;

    /**
     * {@inheritDoc}
     */
    @Override
    public HeuristicsMinerTableNodeModel createNodeModel() {
		// Create and return a new node model.
    	node = new HeuristicsMinerTableNodeModel();
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
    public NodeView<HeuristicsMinerTableNodeModel> createNodeView(final int viewIndex,
            final HeuristicsMinerTableNodeModel nodeModel) {
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
        return new HeuristicsMinerTableNodeDialog(node);
    }

}

