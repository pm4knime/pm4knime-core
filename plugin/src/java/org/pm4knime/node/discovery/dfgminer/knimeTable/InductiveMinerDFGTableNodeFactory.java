package org.pm4knime.node.discovery.dfgminer.knimeTable;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;

/**
 * This is an example implementation of the node factory of the
 * "InductiveMinerDFGTable" node.
 *
 * @author 
 */
public class InductiveMinerDFGTableNodeFactory extends NodeFactory<InductiveMinerDFGTableNodeModel> implements WizardNodeFactoryExtension<InductiveMinerDFGTableNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

    /**
     * {@inheritDoc}
     */
    @Override
    public InductiveMinerDFGTableNodeModel createNodeModel() {
		// Create and return a new node model.
        return new InductiveMinerDFGTableNodeModel();
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
    public NodeView<InductiveMinerDFGTableNodeModel> createNodeView(final int viewIndex,
            final InductiveMinerDFGTableNodeModel nodeModel) {
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
        return new InductiveMinerDFGTableNodeDialog();
    }

}

