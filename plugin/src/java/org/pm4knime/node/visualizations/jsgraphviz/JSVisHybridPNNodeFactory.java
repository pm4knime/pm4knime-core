package org.pm4knime.node.visualizations.jsgraphviz;

import org.knime.core.node.wizard.WizardNodeFactoryExtension;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;


public class JSVisHybridPNNodeFactory 
        extends NodeFactory<JSVisHybridPNNodeModel> implements WizardNodeFactoryExtension<JSVisHybridPNNodeModel, JSGraphVizViewRepresentation, JSGraphVizViewValue> {

    /**
     * {@inheritDoc}
     */
    @Override
    public JSVisHybridPNNodeModel createNodeModel() {
        return new JSVisHybridPNNodeModel();
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
    public NodeView<JSVisHybridPNNodeModel> createNodeView(final int viewIndex,
            final JSVisHybridPNNodeModel nodeModel) {
		return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return null;
    }

}

