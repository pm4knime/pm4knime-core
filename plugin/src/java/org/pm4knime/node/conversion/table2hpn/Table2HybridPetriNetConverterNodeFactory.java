package org.pm4knime.node.conversion.table2hpn;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;
import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewRepresentation;
import org.pm4knime.node.visualizations.jsgraphviz.JSGraphVizViewValue;

public class Table2HybridPetriNetConverterNodeFactory extends NodeFactory<Table2HybridPetriNetConverterNodeModel> implements WizardNodeFactoryExtension<Table2HybridPetriNetConverterNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

	Table2HybridPetriNetConverterNodeModel node;
	
	/**
     * {@inheritDoc}
     */
    @Override
    public Table2HybridPetriNetConverterNodeModel createNodeModel() {
		// Create and return a new node model.
        node = new Table2HybridPetriNetConverterNodeModel();
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
    public NodeView<Table2HybridPetriNetConverterNodeModel> createNodeView(final int viewIndex,
            final Table2HybridPetriNetConverterNodeModel nodeModel) {
		// We return null as this example node does not provide a view. Also see "getNrNodeViews()".
		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
		// Indication whether the node has a dialog or not.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
		// This example node has a dialog, hence we create and return it here. Also see "hasDialog()".
        return null;
    }

}

